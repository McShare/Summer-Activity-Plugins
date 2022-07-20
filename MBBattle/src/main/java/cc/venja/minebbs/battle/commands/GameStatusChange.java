package cc.venja.minebbs.battle.commands;

import cc.venja.minebbs.battle.BattleMain;
import cc.venja.minebbs.battle.enums.GameStatus;
import cc.venja.minebbs.battle.events.GameStatusChangeEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class GameStatusChange implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return execute(commandSender, s, strings);
    }

    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] args) {
        if (!commandSender.isOp()) {
            commandSender.sendMessage("§c(!) 你没有权限!");
            return false;
        }

        if (args.length != 1) {
            commandSender.sendMessage("§c(!) 指令参数不正确, 用法 /gamestatus-change <status>");
            return false;
        }

        int val = Integer.parseInt(args[0]);
        GameStatus status = GameStatus.values()[val];

        BattleMain.instance.onGameStatusChange(new GameStatusChangeEvent(BattleMain.status, status));

        BattleMain.status = status;

        return false;
    }
}
