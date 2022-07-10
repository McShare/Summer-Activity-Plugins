package cc.venja.minebbs.robot;

import cc.venja.minebbs.robot.dao.PlayerDao;
import cc.venja.minebbs.robot.handler.RobotDelWhitelistHandler;
import cc.venja.minebbs.robot.handler.RobotGetTeamHandler;
import cc.venja.minebbs.robot.handler.RobotSetTeamHandler;
import cc.venja.minebbs.robot.handler.RobotWhitelistHandler;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
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
        if (!existsWhitelist(Objects.requireNonNull(event.getPlayerProfile().getName()))) {
            event.kickMessage(Component.text("§c你不在本服务器白名单内"));
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST);
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
    public static void addPlayerTeam(String player, int team) throws IOException {
        instance.team.set(player.toLowerCase(), team);
        instance.team.save(instance.teamFile);
    }

    public static void removePlayerTeam(String player) throws IOException {
        instance.team.set(player.toLowerCase(), null);
        instance.team.save(instance.teamFile);
    }

    public static boolean existsPlayerTeam(String player) {
        return instance.team.contains(player.toLowerCase());
    }

    public static int getPlayerTeam(String player) {
        return instance.team.getInt(player.toLowerCase());
    }

}
