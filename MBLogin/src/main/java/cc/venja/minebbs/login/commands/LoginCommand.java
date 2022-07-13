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

import java.util.Objects;

public class LoginCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return execute(commandSender, s, strings);
    }

    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§c(!) 该指令只能在游戏内使用");
            return false;
        }
        if (args.length != 1) {
            commandSender.sendMessage("§c(!) 指令参数不正确, 用法 /login <密码>");
            return false;
        }
        if (LoginMain.instance.onlinePlayers.get(player) != LoginMain.Status.NOT_LOGIN) {
            commandSender.sendMessage("§c(!) 你应该先完成注册");
            return false;
        }

        try {
            var playerName = commandSender.getName();
            var playerInfoDao = new PlayerInfoDao();
            var user = playerInfoDao.getPlayerByName(playerName);

            user.setLastLoginIp(Objects.requireNonNull(player.getAddress()).getAddress().toString());

            if (user.getPassword().equals(Utils.md5DigestAsHex(args[0].getBytes()))) {
                playerInfoDao.updatePlayer(user);

                commandSender.sendMessage("§a(*) 登录成功，欢迎回来~");

                LoginMain.instance.onlinePlayers.put(player, LoginMain.Status.LOGIN);
                player.setGameMode(Objects.requireNonNull(GameMode.getByValue(user.getLastGameMode())));
            } else {
                commandSender.sendMessage("§c(!) 密码错误, 请重试");
            }
        } catch (Exception e) {
            LoginMain.instance.getLogger().warning(e.toString());
        }

        return false;
    }

}
