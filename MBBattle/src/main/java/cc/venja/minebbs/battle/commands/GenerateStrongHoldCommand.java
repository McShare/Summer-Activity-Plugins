package cc.venja.minebbs.battle.commands;

import cc.venja.minebbs.battle.BattleMain;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class GenerateStrongHoldCommand implements CommandExecutor {
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
            commandSender.sendMessage("§c(!) 指令参数不正确, 用法 /generate-stronghold <ID>");
            return false;
        }

        List<Map<?, ?>> strongholdList = BattleMain.configuration.getMapList("StrongHold");
        for (Map<?, ?> stronghold : strongholdList) {
            String Id = stronghold.get("Id").toString();
            if (args[0].equals(Id)) {
                @SuppressWarnings("unchecked")
                List<Integer> position = (List<Integer>) stronghold.get("Position");
                int x = position.get(0);
                int y = position.get(1);
                int z = position.get(2);
                World world = Bukkit.getWorld("world");
                if (world != null) {
                    Block beacon = world.getBlockAt(x, y-1, z);

                    beacon.setType(Material.BEACON);

                    String ownerTeam = stronghold.get("OwnerTeam").toString();

                    Material glassMaterial = switch (ownerTeam) {
                        case "TeamRED" -> Material.RED_STAINED_GLASS;
                        case "TeamBLUE" -> Material.BLUE_STAINED_GLASS;
                        case "TeamGRAY" -> Material.GRAY_STAINED_GLASS;
                        case "TeamYELLOW" -> Material.YELLOW_STAINED_GLASS;
                        default -> Material.GLASS;
                    };

                    Block glass = world.getBlockAt(x, y, z);
                    glass.setType(glassMaterial);

                    Block ironBlock;
                    for (int x2 = -1; x2 < 2; x2++) {
                        for (int z2 = -1; z2 < 2; z2++) {
                            ironBlock = world.getBlockAt(x+x2, y-2, z+z2);
                            ironBlock.setType(Material.IRON_BLOCK);
                        }
                    }
                }
            }
        }

        return false;
    }
}
