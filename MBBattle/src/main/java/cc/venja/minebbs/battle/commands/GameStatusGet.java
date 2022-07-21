package cc.venja.minebbs.battle.commands;

import cc.venja.minebbs.battle.BattleMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GameStatusGet implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return execute(commandSender, s, strings);
    }

    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] args) {
        String message = String.format("当前时段为 %s", BattleMain.status.getName());
        commandSender.sendMessage(message);
        return false;
    }
}