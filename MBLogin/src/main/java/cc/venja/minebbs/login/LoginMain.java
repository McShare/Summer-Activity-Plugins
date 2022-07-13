package cc.venja.minebbs.login;

import cc.venja.minebbs.login.commands.AutoLoginCommand;
import cc.venja.minebbs.login.commands.LoginCommand;
import cc.venja.minebbs.login.commands.RegisterCommand;
import cc.venja.minebbs.login.data.PlayerData;
import cc.venja.minebbs.login.database.UserInfo;
import cc.venja.minebbs.login.database.UserInfoDao;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

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
                        put("Host", "127.0.0.1:3306/player");
                        put("Username", "username");
                        put("Password", "password");
                    }
                };

                configuration.set("Database", databaseConfig);
            } else {
                ConfigurationSection databaseSection = Objects.requireNonNull(
                        configuration.getConfigurationSection("Database"));
                Class.forName("com.mysql.jdbc.Driver");
                databaseConnection = DriverManager.getConnection(
                        String.format("jdbc:mysql://%s", databaseSection.get("Host")),
                        Objects.requireNonNull(databaseSection.get("Username")).toString(),
                        Objects.requireNonNull(databaseSection.get("Password")).toString()
                );
            }

            configuration.save(configFile);

            this.getServer().getPluginManager().registerEvents(this, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        UserInfoDao userInfoDao = new UserInfoDao();
        UserInfo user = userInfoDao.queryByUsername(playerName);

        if (user == null) {
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

//        var playerName = event.getPlayer().getName().toLowerCase();
//        var file = new File(this.getDataFolder().toPath().resolve("data").resolve(playerName + ".yml").toString());
//        if (!file.exists()) {
//            onlinePlayers.put(event.getPlayer(), Status.NOT_REGISTER);
//            event.getPlayer().sendMessage("§6>>> 请输入/register <密码> <确认密码>, 完成注册");
//        } else {
//            onlinePlayers.put(event.getPlayer(), Status.NOT_LOGIN);
//
//            var yaml = YamlConfiguration.loadConfiguration(file);
//            var playerData = new PlayerData().applyConfigSection(yaml);
//
//            if (playerData.lastLoginIp.equals(Objects.requireNonNull(event.getPlayer().getAddress()).getAddress().toString())) {
//                if (playerData.enableAutoLogin) {
//                    event.getPlayer().sendMessage("§a(*) 与上次登录IP相同，自动登录，欢迎回来~");
//
//                    LoginMain.instance.onlinePlayers.put(event.getPlayer(), LoginMain.Status.LOGIN);
//                    event.getPlayer().setGameMode(Objects.requireNonNull(GameMode.getByValue(playerData.lastGameMode)));
//                    return;
//                }
//            }
//            event.getPlayer().sendMessage("§6>>> 请输入/login <密码>, 进行登录");
//        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) throws Exception {
        if (FloodgateApi.getInstance().getPlayer(event.getPlayer().getUniqueId()) != null) return;

        if (onlinePlayers.get(event.getPlayer()) == Status.LOGIN) {
            var playerName = event.getPlayer().getName();

            String sql = "update user_info set last_game_mode=? where username=?";
            PreparedStatement statement = databaseConnection.prepareStatement(sql);
            statement.setInt(1, event.getPlayer().getGameMode().getValue());
            statement.setString(2, playerName);

            statement.execute();

//            var playerName = event.getPlayer().getName().toLowerCase();
//            var file = new File(this.getDataFolder().toPath().resolve("data").resolve(playerName + ".yml").toString());
//
//            var yaml = YamlConfiguration.loadConfiguration(file);
//            var playerData = new PlayerData().applyConfigSection(yaml);
//            playerData.lastGameMode = event.getPlayer().getGameMode().getValue();
//            yaml = playerData.reflectToConfigSection(yaml);
//            yaml.save(file);
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
