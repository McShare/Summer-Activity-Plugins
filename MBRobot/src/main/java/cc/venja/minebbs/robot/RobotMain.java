package cc.venja.minebbs.robot;

import cc.venja.minebbs.login.enums.Team;
import cc.venja.minebbs.login.database.PlayerInfo;
import cc.venja.minebbs.login.database.dao.PlayerInfoDao;
import cc.venja.minebbs.robot.handler.RobotDelWhitelistHandler;
import cc.venja.minebbs.robot.handler.RobotGetTeamHandler;
import cc.venja.minebbs.robot.handler.RobotSetTeamHandler;
import cc.venja.minebbs.robot.handler.RobotWhitelistHandler;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class RobotMain extends JavaPlugin implements Listener {

    public static RobotMain instance;
    public static final Gson gson = new Gson();

    @Override
    public void onLoad() {
        instance = this;
    }

    public File whitelistFile;
    public YamlConfiguration whitelist;

    public File teamFile;
    public YamlConfiguration team;

    public File configFile;
    public YamlConfiguration configuration;

    private HttpServer server;

    @Override
    public void onEnable() {
        try {
            configFile = new File(this.getDataFolder().toPath().resolve("config.yml").toString()).getAbsoluteFile();
            var configExists = configFile.exists();
            configuration = YamlConfiguration.loadConfiguration(configFile);
            if (!configExists)
                configuration.set("ListenPort", 22222);
            configuration.save(configFile);

            whitelistFile = new File(this.getDataFolder().toPath().resolve("whitelist.yml").toString()).getAbsoluteFile();
            whitelist = YamlConfiguration.loadConfiguration(whitelistFile);
            whitelist.save(whitelistFile);

            teamFile = new File(this.getDataFolder().toPath().resolve("team.yml").toString()).getAbsoluteFile();
            team = YamlConfiguration.loadConfiguration(teamFile);
            team.save(teamFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        var address = new InetSocketAddress(configuration.getInt("ListenPort", 22222));
        try {
            server = HttpServer.create(address, 0);

            server.createContext("/delwhitelist", new RobotDelWhitelistHandler());
            server.createContext("/whitelist", new RobotWhitelistHandler());
            server.createContext("/getteam", new RobotGetTeamHandler());
            server.createContext("/setteam", new RobotSetTeamHandler());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            getLogger().info("Server is listening on port 22222");

        } catch (Exception e) {
            getLogger().warning(e.toString());
        }

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        server.stop(1);
    }

    @EventHandler(
            priority = EventPriority.LOWEST
    )
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        var playerName = event.getPlayerProfile().getName();
        var floodgatePlayer = FloodgateApi.getInstance().getPlayer(event.getPlayerProfile().getId());
        if (floodgatePlayer != null) {
            playerName = floodgatePlayer.getUsername();
        }
        if (!existsWhitelist(Objects.requireNonNull(playerName))) {
            event.kickMessage(Component.text("§c你不在本服务器白名单内"));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("doupdate")) {
            try {
                updateLocalDataToDatabase();
                reloadTeamListMap();
            } catch (SQLException e) {
                getLogger().info(e.toString());
            }
        }
        return false;
    }

    private static void updateLocalDataToDatabase() throws SQLException {
        for (String player : instance.team.getKeys(false)) {
            updatePlayer(player, instance.team.getInt(player));
        }
        for (String player : instance.whitelist.getKeys(false)) {
            instance.getLogger().info(instance.whitelist.getString(player));
            int team = getPlayerTeam(player) == null ? -1 : Objects.requireNonNull(getPlayerTeam(player)).getValue();
            updatePlayer(player, team, instance.whitelist.getString(player));
        }
    }

    public static void reloadTeamListMap() throws SQLException {
        PlayerInfoDao dao = new PlayerInfoDao();
        for (Team team : Team.values()) {
            List<String> players = new ArrayList<>();
            for (PlayerInfo playerInfo : dao.queryPlayersByTeam(team.getValue())) {
                players.add(playerInfo.getPlayerName());
            }
            teamListMap.put(team, players);
        }
    }

    /**
     * Whitelist APIs
     */
    public static void addWhitelist(String player, String KHL) throws IOException {
        instance.whitelist.set(player.toLowerCase(), KHL);
        instance.whitelist.save(instance.whitelistFile);
    }

    public static void removeWhitelist(String player) throws IOException {
        instance.whitelist.set(player.toLowerCase(), null);
        instance.whitelist.save(instance.whitelistFile);
    }

    public static boolean existsWhitelist(String player) {
        return instance.whitelist.contains(player.toLowerCase());
    }

    public static String getPlayerKHL(String player) {
        return instance.whitelist.getString(player.toLowerCase());
    }

    /**
     * Team APIs
     */
    public static Map<Team, List<String>> teamListMap = new ConcurrentHashMap<>();

    public static void addPlayerTeam(String player, int team, String khl) throws SQLException {
        updatePlayer(player, team, khl);

        List<String> players = teamListMap.get(Team.getByValue(team));
        players.add(player);
        teamListMap.put(Team.getByValue(team), players);
    }

    public static void removePlayerTeam(String player) throws SQLException {
        Team team = getPlayerTeam(player);
        updatePlayer(player, -1);

        List<String> players = teamListMap.get(team);
        players.remove(player);
        teamListMap.put(team, players);
    }

    private static void updatePlayer(String player, int team) throws SQLException {
        updatePlayer(player, team, "");
    }

    private static void updatePlayer(String player, int team, String khl) throws SQLException {
        PlayerInfoDao dao = new PlayerInfoDao();
        PlayerInfo playerInfo = dao.getPlayerByName(player);
        if (playerInfo == null) {
            dao.addPlayer(new PlayerInfo(player, "", team, khl, "", 0, true));
        } else {
            playerInfo.setTeam(instance.team.getInt(player));
            playerInfo.setKhl(khl);
            dao.updatePlayer(playerInfo);
        }
    }

    public static boolean existsPlayerTeam(String player) throws SQLException {
        PlayerInfoDao dao = new PlayerInfoDao();
        PlayerInfo playerInfo = dao.getPlayerByName(player);
        if (playerInfo == null) {
            return false;
        }
        return playerInfo.getTeam() != -1;
    }

    @Nullable
    public static Team getPlayerTeam(String player) throws SQLException {
        PlayerInfoDao dao = new PlayerInfoDao();
        PlayerInfo playerInfo = dao.getPlayerByName(player);
        if (playerInfo == null) {
            return null;
        }
        if (playerInfo.getTeam() == -1) {
            return null;
        }
        return Team.getByValue(playerInfo.getTeam());
    }

    public static Team getLowestMemberTeam() {
        int tempNum = 9999;
        Team team = Team.RED;
        for (Map.Entry<Team, List<String>> entry : teamListMap.entrySet()) {
            Team key = entry.getKey();
            if (key.getValue() <= 3) {
                instance.getLogger().info(key + " => " + entry.getValue().size());
                if (entry.getValue().size() < tempNum) {
                    team = key;
                    tempNum = entry.getValue().size();
                }
            }
        }
        return team;
    }

    public static StringBuilder inputStreamToString(InputStream inputStream) throws IOException {
        var body = new StringBuilder();
        try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            int c;
            while ((c = reader.read()) != -1) {
                body.append((char) c);
            }
        }
        return body;
    }
}
