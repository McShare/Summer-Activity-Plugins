package cc.venja.minebbs.login;

import cc.venja.minebbs.login.commands.AutoLoginCommand;
import cc.venja.minebbs.login.commands.LoginCommand;
import cc.venja.minebbs.login.commands.RegisterCommand;
import cc.venja.minebbs.login.database.PlayerInfo;
import cc.venja.minebbs.login.database.dao.PlayerInfoDao;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginMain extends JavaPlugin implements Listener {

    public static LoginMain instance;

    public Map<Player, Status> onlinePlayers = new ConcurrentHashMap<>();

    @Override
    public void onLoad() {
        instance = this;
    }

    public File configFile;
    public YamlConfiguration configuration;
    public Connection databaseConnection;

    private Timer timer = new Timer();

    @Override
    public void onEnable() {
        this.getLogger().warning("§b____   ____                 __        ");
        this.getLogger().warning("§b\\   \\ /   /____   ____     |__|____   ");
        this.getLogger().warning("§b \\   Y   // __ \\ /    \\    |  \\__  \\  ");
        this.getLogger().warning("§b  \\     /\\  ___/|   |  \\   |  |/ __ \\_");
        this.getLogger().warning("§b   \\___/  \\___  >___|  /\\__|  (____  /");
        this.getLogger().warning("§b              \\/     \\/\\______|    \\/ ");
        this.getLogger().warning("§b-------------------------------------- ");
        this.getLogger().warning("§bThis plug-in loading...");
        this.getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(this.getServer().getPluginCommand("login")).setExecutor(new LoginCommand());
        Objects.requireNonNull(this.getServer().getPluginCommand("register")).setExecutor(new RegisterCommand());
        Objects.requireNonNull(this.getServer().getPluginCommand("autologin")).setExecutor(new AutoLoginCommand());

        try {
            configFile = new File(this.getDataFolder().toPath().resolve("config.yml").toString()).getAbsoluteFile();
            var configExists = configFile.exists();
            configuration = YamlConfiguration.loadConfiguration(configFile);
            if (!configExists) {
                Map<String, Object> databaseConfig = new HashMap<>() {
                    {
                        put("Host", "127.0.0.1:3306/database");
                        put("Username", "username");
                        put("Password", "password");
                    }
                };

                configuration.set("Database", databaseConfig);
            }
            configuration.save(configFile);

            tryMakeConnection();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    LoginMain.instance.getLogger().warning("Trying keep MySQL connection....");
                    try {
                        databaseConnection.prepareStatement("select * from player_info where player_name='onlyfortest'").execute();
                    } catch (SQLTimeoutException | CommunicationsException e) {
                        try {
                            tryMakeConnection();
                        } catch (Exception exception) {
                            LoginMain.instance.getLogger().warning("Really unable to connect!!! Service down!!!");
                            LoginMain.instance.getLogger().warning(exception.toString());
                        }
                    } catch (SQLException exception) {
                        LoginMain.instance.getLogger().warning("Unable to process!!! Error!!!");
                        LoginMain.instance.getLogger().warning(exception.toString());
                    }
                }
            }, 0, 3 * 60 * 1000);

            this.getServer().getPluginManager().registerEvents(this, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            databaseConnection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tryMakeConnection() throws SQLException, ClassNotFoundException {
        var databaseSection = Objects.requireNonNull(
                configuration.getConfigurationSection("Database"));
        Class.forName("com.mysql.jdbc.Driver");
        databaseConnection = DriverManager.getConnection(
                String.format("jdbc:mysql://%s?autoReconnect=true&tcpKeepAlive=true&characterEncoding=utf8&useSSL=true", databaseSection.get("Host")),
                Objects.requireNonNull(databaseSection.get("Username")).toString(),
                Objects.requireNonNull(databaseSection.get("Password")).toString()
        );
    }

    @EventHandler (
            priority = EventPriority.HIGHEST
    )
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

        Player player = Bukkit.getPlayer(Objects.requireNonNull(event.getPlayerProfile().getName()));
        if (player == null) return;
        var status = onlinePlayers.getOrDefault(player, Status.NOT_LOGIN);
        switch (status) {
            case LOGIN -> {
                event.kickMessage(Component.text("§c该账户已登录，禁止顶号，如有异常请联系管理"));
                event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_FULL);
            }
            case REGISTER, NOT_LOGIN, NOT_REGISTER -> player.kick(Component.text("§c异地登录，你被顶下线了，如有异常请联系管理"));
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) throws Exception {
        if (FloodgateApi.getInstance().getPlayer(event.getPlayer().getUniqueId()) != null) {
            LoginMain.instance.onlinePlayers.put(event.getPlayer(), LoginMain.Status.LOGIN);
            event.getPlayer().sendMessage("§a(*) 已通过XBox验证，无需登录，欢迎回来~");
            return;
        }

        event.getPlayer().setGameMode(GameMode.SPECTATOR);
        var playerName = event.getPlayer().getName();

        var playerInfoDao = new PlayerInfoDao();
        var user = playerInfoDao.getPlayerByName(playerName);

        if (user == null || user.getPassword().equals("")) {
            onlinePlayers.put(event.getPlayer(), Status.NOT_REGISTER);
            event.getPlayer().sendMessage("§6>>> 请输入/register <密码> <确认密码>, 完成注册");
        } else {
            onlinePlayers.put(event.getPlayer(), Status.NOT_LOGIN);
            if (user.getLastLoginIp()
                    .equals(Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().toString())) {
                if (user.isEnableAutoLogin()) {
                    event.getPlayer().sendMessage("§a(*) 与上次登录IP相同，自动登录，欢迎回来~");

                    LoginMain.instance.onlinePlayers.put(event.getPlayer(), LoginMain.Status.LOGIN);
                    event.getPlayer().setGameMode(Objects.requireNonNull(GameMode.getByValue(user.getLastGameMode())));
                    return;
                }
            }
            event.getPlayer().sendMessage("§6>>> 请输入/login <密码>, 进行登录");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (FloodgateApi.getInstance().getPlayer(event.getPlayer().getUniqueId()) != null) return;

        if (onlinePlayers.get(event.getPlayer()) == Status.LOGIN) {
            var playerName = event.getPlayer().getName();

            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        var playerDao = new PlayerInfoDao();
                        PlayerInfo player;
                        player = playerDao.getPlayerByName(playerName);
                        player.setLastGameMode(event.getPlayer().getGameMode().getValue());
                        playerDao.updatePlayer(player);
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                }
            }, 10);
        }

        onlinePlayers.remove(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!onlinePlayers.getOrDefault(event.getPlayer(), Status.NOT_REGISTER).equals(Status.LOGIN)) {
            event.setCancelled(true);
            Audience.audience(event.getPlayer()).sendActionBar(Component.text("§c请先注册/登录后进行游戏"));
        }
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (!onlinePlayers.getOrDefault(event.getPlayer(), Status.NOT_REGISTER).equals(Status.LOGIN)) {
            event.setCancelled(true);
            Audience.audience(event.getPlayer()).sendActionBar(Component.text("§c请先注册/登录后进行对话"));
        }
    }

    public enum Status {
        NOT_REGISTER,
        REGISTER,
        NOT_LOGIN,
        LOGIN,
    }

}
