package cc.venja.minebbs.battle.commands;

import cc.venja.minebbs.battle.BattleMain;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ArenaManageCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return execute(commandSender, s, strings);
    }

    public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] args) {
        if (!commandSender.isOp()) {
            commandSender.sendMessage("§c(!) 你没有权限!");
            return false;
        }

        if (args.length == 0) return false;

        switch (args[0]) {
            case "wall": {
                if (args.length == 2) {
                    String team = args[1].toUpperCase();
                    String teamStr = "EnableTeam" + team;

                    if (BattleMain.instance.arenaSystem.configuration.contains(teamStr)) {
                        boolean bool = BattleMain.instance.arenaSystem.configuration.getBoolean(teamStr);
                        BattleMain.instance.arenaSystem.configuration.set(teamStr, !bool);
                        String response = (!bool) ? "开启" + team + "的墙" : "关闭" + team + "的墙";
                        commandSender.sendMessage("§6(*) " + response);
                    }
                }
                break;
            }
            case "opencenter": {
                boolean bool = BattleMain.instance.arenaSystem.configuration.getBoolean("CenterAccess");
                BattleMain.instance.arenaSystem.configuration.set("CenterAccess", !bool);
                String response = (!bool) ? "开启中心区域" : "关闭中心区域";
                commandSender.sendMessage("§6(*) " + response);
            }
            case "theday": {
                BattleMain.instance.arenaSystem.configuration.set("CenterAccess", true);
                BattleMain.instance.arenaSystem.configuration.set("CenterEnable", true);
                commandSender.sendMessage("§6(*) 决战日");
                // 传送所有玩家
            }
        }
        try {
            BattleMain.instance.arenaSystem.configuration.save(BattleMain.instance.arenaSystem.configFile);
        } catch (IOException exception) {
            BattleMain.instance.getLogger().info(exception.toString());
        }
        return false;
    }
}
