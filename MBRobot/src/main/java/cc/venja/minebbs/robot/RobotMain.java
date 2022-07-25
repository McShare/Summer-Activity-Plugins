package cc.venja.minebbs.robot;

import cc.venja.minebbs.login.LoginMain;
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
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
        try {
            reloadTeamListMap();
            reloadLatestTeamStat();
        } catch (Exception e) {
            getLogger().info(e.toString());
        }
    }

    @EventHandler(
            priority = EventPriority.LOWEST
    )
    public void onPreJoin(AsyncPlayerPreLoginEvent event) throws SQLException {
        var playerName = event.getPlayerProfile().getName();
        var floodgatePlayer = FloodgateApi.getInstance().getPlayer(event.getPlayerProfile().getId());
        if (floodgatePlayer != null) {
            playerName = floodgatePlayer.getUsername();
        }
        if (!existsWhitelist(Objects.requireNonNull(playerName))) {
            event.kickMessage(Component.text("§c你不在本服务器白名单内"));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST);
        }
        if (getPlayerTeam(Objects.requireNonNull(playerName)) == null) {
            addPlayerTeam(playerName, getLowestMemberTeam().getValue(), getPlayerKHL(playerName));
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("doupdate")) {
            try {
                reloadTeamListMap();
                reloadLatestTeamStat();
            } catch (Exception e) {
                getLogger().info(e.toString());
            }
        }
        if (command.getName().equals("analyse")) {
            for (OfflinePlayer player : this.getServer().getOfflinePlayers()) {
                try {
                    String name = getRealPlayerName(player.getName());
                    if (existsPlayerTeam(name)) {
                        String sql = "insert into record(player_name, last_login_time, team) values(?,?,?)";

                        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);

                        statement.setString(1, Objects.requireNonNull(name).toLowerCase());
                        statement.setDate(2, new Date(player.getLastLogin()));
                        statement.setInt(3, Objects.requireNonNull(getPlayerTeam(name)).getValue());

                        getLogger().info(statement.toString().replace("com.mysql.cj.jdbc.ClientPreparedStatement: ", ""));
                        statement.execute();
                    }
                } catch (SQLException exception) {
                    getLogger().info(exception.toString());
                }
            }
        }
        return false;
    }

    public static void reloadTeamListMap() throws SQLException {
        PlayerInfoDao dao = new PlayerInfoDao();
        for (Team team : Team.values()) {
            List<String> players = new ArrayList<>();
            for (PlayerInfo playerInfo : dao.queryPlayersByTeam(team.getValue())) {
                instance.getLogger().info(team + " > 缓存 > " + playerInfo.getPlayerName());
                players.add(playerInfo.getPlayerName());
            }
            teamListMap.put(team, players);
        }
    }

    static SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
    public static Map<Team, List<String>> newTeamListMap = new ConcurrentHashMap<>();

    public static void reloadLatestTeamStat() throws Exception {
        String sql = "select * from record";
        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);

        ResultSet resultSet = statement.executeQuery();
        List<Info> filteredInfo = new ArrayList<>();

        while (resultSet.next()) {
            Info info = new Info(
                    resultSet.getString("player_name"),
                    resultSet.getDate("last_login_time"),
                    resultSet.getInt("team")
            );
            java.util.Date d = f.parse("2022-07-25");
            if (info.date >= d.getTime()) {
                filteredInfo.add(info);
                instance.getLogger().info(info.toString());
            }
        }
        instance.getLogger().info(String.valueOf(filteredInfo.size()));

        for (Info info : filteredInfo) {
            List<String> players = newTeamListMap.getOrDefault(info.team, new ArrayList<>());
            players.add(info.playerName.toLowerCase());
            newTeamListMap.put(info.team, players);
        }

        int num = 0;
        for (List<String> value : newTeamListMap.values()) {
            num += value.size();
        }
        instance.getLogger().info(String.valueOf(num));
    }

    public static class Info {
        public String playerName;
        public Long date;
        public Team team;

        public Info() {
        }

        public Info(String playerName, Date date, int team) {
            this.playerName = playerName;
            this.date = date.getTime();
            this.team = Team.getByValue(team);
        }

        @Override
        public String toString() {
            return "Info{" +
                    "playerName='" + playerName + '\'' +
                    ", date=" + date +
                    ", team=" + team +
                    '}';
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
        instance.whitelist = YamlConfiguration.loadConfiguration(instance.whitelistFile);
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
        player = getRealPlayerName(player);
        updatePlayer(player, team, khl);

        List<String> players = teamListMap.get(Team.getByValue(team));
        players.add(player);
        teamListMap.put(Team.getByValue(team), players);

        players.clear();
        players = newTeamListMap.get(Team.getByValue(team));
        players.add(player);
        newTeamListMap.put(Team.getByValue(team), players);
    }

    public static void removePlayerTeam(String player) throws SQLException {
        player = getRealPlayerName(player);
        Team team = getPlayerTeam(player);
        updatePlayer(player, -1);

        List<String> players = teamListMap.get(team);
        players.remove(player);
        teamListMap.put(team, players);

        players.clear();
        players = newTeamListMap.get(team);
        players.remove(player);
        newTeamListMap.put(team, players);
    }

    private static void updatePlayer(String player, int team) throws SQLException {
        player = getRealPlayerName(player);
        updatePlayer(player, team, "");
    }

    private static void updatePlayer(String player, int team, String khl) throws SQLException {
        player = getRealPlayerName(player);
        PlayerInfoDao dao = new PlayerInfoDao();
        PlayerInfo playerInfo = dao.getPlayerByName(player);
        if (playerInfo == null) {
            instance.getLogger().info("执行创建");
            dao.addPlayer(new PlayerInfo(player, "", team, khl, "", 0, true));
        } else {
            instance.getLogger().info("执行更新");
            playerInfo.setTeam(instance.team.getInt(player));
            playerInfo.setKhl(khl);
            dao.updatePlayer(playerInfo);
        }
    }

    public static boolean existsPlayerTeam(String player) throws SQLException {
        return getPlayerTeam(player) != null;
    }

    @Nullable
    public static Team getPlayerTeam(String player) throws SQLException {
        player = getRealPlayerName(player);
        Team team = null;
        for (var entry : teamListMap.entrySet()) {
            for (String teamPlayer : entry.getValue()) {
                if (teamPlayer.equalsIgnoreCase(player)) {
                    team = entry.getKey();
                    break;
                }
            }
        }
        if (team == null) {
            PlayerInfoDao playerInfoDao = new PlayerInfoDao();
            PlayerInfo playerInfo = playerInfoDao.getPlayerByName(player);
            if (playerInfo != null) {
                team = Team.getByValue(playerInfo.getTeam());
            }

            if (team != null) {
                List<String> players = teamListMap.get(team);
                players.add(player);
                teamListMap.put(team, players);
            }
        }
        return team;
    }

    public static String getRealPlayerName(String player) {
        Player exactPlayer = instance.getServer().getPlayerExact(player);
        if (exactPlayer != null) {
            var floodgatePlayer = FloodgateApi.getInstance().getPlayer(exactPlayer.getUniqueId());
            if (floodgatePlayer != null) {
                return floodgatePlayer.getUsername();
            }
        }
        return player.replace("BE+", "");
    }

    public static Team getLowestMemberTeam() {
        int tempNum = 9999;
        Team team = Team.RED;
        for (Map.Entry<Team, List<String>> entry : teamListMap.entrySet()) {
            Team key = entry.getKey();
            if (key.getValue() <= 3) {
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
