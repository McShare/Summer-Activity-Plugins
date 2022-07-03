package cc.venja.minebbs.robot;

import cc.venja.minebbs.robot.dao.PlayerDao;
import cc.venja.minebbs.robot.handler.RobotGetTeamHandler;
import cc.venja.minebbs.robot.handler.RobotWhitelistHandler;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class RobotMain extends JavaPlugin {

    public static RobotMain instance;
    public static final Gson gson = new Gson();

    @Override
    public void onLoad() {
        instance = this;
    }

    public File whitelistFile;
    public YamlConfiguration whitelist;

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
        } catch (Exception e) {
            e.printStackTrace();
        }

        var address = new InetSocketAddress(configuration.getInt("ListenPort", 22222));
        try {
            server = HttpServer.create(address, 0);

            server.createContext("/whitelist", new RobotWhitelistHandler());
            server.createContext("/getteam", new RobotGetTeamHandler());
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            getLogger().info("Server is listening on port 22222");

        } catch (Exception e) {
            getLogger().warning(e.toString());
        }
    }

    @Override
    public void onDisable() {
        server.stop(1);
    }

    public static void addWhitelist(PlayerDao player) throws IOException {
        instance.whitelist.set(player.playerName.toLowerCase(), player.KHL);
        instance.whitelist.save(instance.whitelistFile);
    }

    public static boolean existsWhitelist(PlayerDao player) {
        return instance.whitelist.contains(player.playerName.toLowerCase());
    }

    public static void removeWhitelist(PlayerDao playerDao) throws IOException {
        instance.whitelist.set(playerDao.playerName, null);
        instance.whitelist.save(instance.whitelistFile);
    }


}
