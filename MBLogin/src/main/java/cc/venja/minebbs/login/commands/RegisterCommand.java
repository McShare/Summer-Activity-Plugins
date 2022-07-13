package cc.venja.minebbs.login.commands;

import cc.venja.minebbs.login.LoginMain;
import cc.venja.minebbs.login.database.PlayerInfo;
import cc.venja.minebbs.login.database.dao.PlayerInfoDao;
import cc.venja.minebbs.login.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Objects;

public class RegisterCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return execute(commandSender, s, strings);
    }

    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§c(!) 该指令只能在游戏内使用");
            return false;
        }
        if (args.length != 2) {
            commandSender.sendMessage("§c(!) 指令参数不正确, 用法 /register <密码> <确认密码>");
            return false;
        }
        if (LoginMain.instance.onlinePlayers.get(player) != LoginMain.Status.NOT_REGISTER) {
            commandSender.sendMessage("§c(!) 你不需要进行注册");
            return false;
        }

        try {
            var playerName = commandSender.getName();
            PlayerInfoDao playerInfoDao = new PlayerInfoDao();
            PlayerInfo user = playerInfoDao.queryByPlayerName(playerName);

            if (user != null) {
                commandSender.sendMessage("§c(!) 该账户已完成注册");
                return false;
            } else {
                if (!args[0].equals(args[1])) {
                    commandSender.sendMessage("§c(!) 两次密码不同，请重试");
                    return false;
                }

                user = new PlayerInfo();
                user.setPlayerName(playerName);
                user.setPassword(Utils.md5DigestAsHex(args[0].getBytes()));
                user.setLastLoginIp(Objects.requireNonNull(player.getAddress()).getAddress().toString());

                playerInfoDao.addPlayer(user);

                commandSender.sendMessage("§a(*) 注册成功，欢迎加入~");

                LoginMain.instance.onlinePlayers.put(player, LoginMain.Status.LOGIN);
                player.setGameMode(Objects.requireNonNull(GameMode.getByValue(user.getLastGameMode())));
            }
        } catch (SQLException e) {
            LoginMain.instance.getLogger().warning(e.toString());
        }


//        var playerName = commandSender.getName().toLowerCase();
//        var file = new File(LoginMain.instance.getDataFolder().toPath().resolve("data").resolve(playerName + ".yml").toString());
//
//        if (file.exists()) {
//            commandSender.sendMessage("§c(!) 该账户已完成注册");
//            return false;
//        } else {
//            if (!args[0].equals(args[1])) {
//                commandSender.sendMessage("§c(!) 两次密码不同，请重试");
//                return false;
//            }
//
//            var yaml = YamlConfiguration.loadConfiguration(file);
//
//            var playerData = new PlayerData();
//            playerData.password = Utils.md5DigestAsHex(args[0].getBytes());
//            playerData.lastLoginIp = Objects.requireNonNull(player.getAddress()).getAddress().toString();
//
//            try {
//                yaml = playerData.reflectToConfigSection(yaml);
//                yaml.save(file);
//
//                commandSender.sendMessage("§a(*) 注册成功，欢迎加入~");
//
//                LoginMain.instance.onlinePlayers.put(player, LoginMain.Status.LOGIN);
//                player.setGameMode(Objects.requireNonNull(GameMode.getByValue(playerData.lastGameMode)));
//            } catch (Exception e) {
//                LoginMain.instance.getLogger().warning(e.toString());
//            }
//        }

        return false;
    }
}
