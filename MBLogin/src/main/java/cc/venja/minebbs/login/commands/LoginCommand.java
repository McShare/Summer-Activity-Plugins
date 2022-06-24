package cc.venja.minebbs.login.commands;

import cc.venja.minebbs.login.LoginMain;
import cc.venja.minebbs.login.data.PlayerData;
import cc.venja.minebbs.login.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class LoginCommand extends Command {

    protected LoginCommand(@NotNull String name) {
        super(name, "Login Command", "", new ArrayList<>());
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

        String playerName = commandSender.getName().toLowerCase();
        File file = new File(LoginMain.instance.getDataFolder().toPath().resolve("data").resolve(playerName + ".yml").toString());
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        try {
            PlayerData playerData = new PlayerData().applyConfigSection(yaml);
            if (playerData.password.equals(Utils.md5DigestAsHex(args[0].getBytes()))) {
                LoginMain.instance.onlinePlayers.put(player, LoginMain.Status.LOGIN);
            } else {
                commandSender.sendMessage("§c(!) 密码错误, 请重试");
            }
        } catch (Exception e) {
            LoginMain.instance.getLogger().warning(e.toString());
        }

        return false;
    }
}
