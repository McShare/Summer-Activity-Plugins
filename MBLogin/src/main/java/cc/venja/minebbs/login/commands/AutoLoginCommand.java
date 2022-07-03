package cc.venja.minebbs.login.commands;

import cc.venja.minebbs.login.LoginMain;
import cc.venja.minebbs.login.data.PlayerData;
import cc.venja.minebbs.login.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public class AutoLoginCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return execute(commandSender, s, strings);
    }

    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("§c(!) 该指令只能在游戏内使用");
            return false;
        }
        if (LoginMain.instance.onlinePlayers.get(player) != LoginMain.Status.LOGIN) {
            commandSender.sendMessage("§c(!) 你应该先完成登录");
            return false;
        }

        var playerName = commandSender.getName().toLowerCase();
        var file = new File(LoginMain.instance.getDataFolder().toPath().resolve("data").resolve(playerName + ".yml").toString());
        var yaml = YamlConfiguration.loadConfiguration(file);

        try {
            var playerData = new PlayerData().applyConfigSection(yaml);
            playerData.enableAutoLogin = !playerData.enableAutoLogin;

            if (playerData.enableAutoLogin) {
                commandSender.sendMessage("§a(*) 启用同IP自动登录，该操作有一定风险，请确保你的IP不会跟别人重复~");
            } else {
                commandSender.sendMessage("§a(*) 关闭同IP自动登录~");
            }

            yaml = playerData.reflectToConfigSection(yaml);
            yaml.save(file);
        } catch (Exception e) {
            LoginMain.instance.getLogger().warning(e.toString());
        }

        return false;
    }
}
