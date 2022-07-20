package cc.venja.minebbs.battle.commands;

import cc.venja.minebbs.battle.BattleMain;
import cc.venja.minebbs.login.enums.Team;
import cc.venja.minebbs.robot.RobotMain;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

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
            case "wall" -> {
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
            }
            case "opencenter" -> {
                boolean bool = BattleMain.instance.arenaSystem.configuration.getBoolean("CenterAccess");
                BattleMain.instance.arenaSystem.configuration.set("CenterAccess", !bool);
                String response = (!bool) ? "开启中心区域" : "关闭中心区域";
                commandSender.sendMessage("§6(*) " + response);
            }
            case "theday" -> {
                BattleMain.instance.arenaSystem.configuration.set("CenterAccess", true);
                BattleMain.instance.arenaSystem.configuration.set("CenterEnable", true);
                BattleMain.instance.arenaSystem.configuration.set("Assembling", true);
                commandSender.sendMessage("§6(*) 决战日");
                // 传送所有玩家
                for (Player player : BattleMain.instance.getServer().getOnlinePlayers()) {
                    try {
                        Team team = RobotMain.getPlayerTeam(player.getName());
                        String teamStr = Objects.requireNonNull(team).getName();
                        String string = teamStr + "AssemblePoint";

                        String end = BattleMain.instance.arenaSystem.configuration.getString(string);
                        assert end != null;
                        Vector endPoint = new Vector(Integer.parseInt(end.split(",")[0]), Integer.parseInt(end.split(",")[1]), Integer.parseInt(end.split(",")[2]));
                        Location newLocation = new Location(player.getWorld(), endPoint.getX(), endPoint.getY(), endPoint.getZ());

                        player.teleport(newLocation);
                    } catch (Exception e) {
                        Bukkit.getLogger().info(e.toString());
                    }
                }
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
