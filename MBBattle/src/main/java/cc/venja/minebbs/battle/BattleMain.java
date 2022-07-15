package cc.venja.minebbs.battle;

import cc.venja.minebbs.login.enums.Team;
import cc.venja.minebbs.robot.RobotMain;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.units.qual.N;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class BattleMain extends JavaPlugin implements Listener {
    public static BattleMain instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    public File configFile;
    public YamlConfiguration configuration;

    @Override
    public void onEnable() {
        try {
            configFile = new File(this.getDataFolder().toPath().resolve("config.yml").toString()).getAbsoluteFile();
            var configExists = configFile.exists();
            configuration = YamlConfiguration.loadConfiguration(configFile);
            if (!configExists) {
                Map<String, Object> teamConfig = new HashMap<>() {
                    {
                        put("RespawnPosition", new double[] {0, 0, 0});
                    }
                };

                configuration.set("TeamRED", teamConfig);
                configuration.set("TeamBLUE", teamConfig);
                configuration.set("TeamGRAY", teamConfig);
                configuration.set("TeamYELLOW", teamConfig);

                configuration.set("RespawnCooldown", 20);

                configuration.set("StrongHold", new ArrayList<>() {
                    {
                        add(new HashMap<String, Object>() {
                            {
                                put("Position", new int[] {0, 0, 0});
                                put("OwnerTeam", "TeamRED");
                            }
                        });
                    }
                });
            }

            configuration.save(configFile);

            this.getServer().getPluginManager().registerEvents(this, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) throws SQLException {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        if (damager.getType().equals(EntityType.PLAYER) && damagee.getType().equals(EntityType.PLAYER)) {
            if (RobotMain.getPlayerTeam(damager.getName()) == RobotMain.getPlayerTeam(damagee.getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent event) throws SQLException {
        Player player = event.getPlayer();
        Location location = event.getRespawnedLocation();

        Team teamValue = RobotMain.getPlayerTeam(event.getPlayer().getName());
        String team = "Team" + teamValue;

        List<Double> respawnPosition = Objects.requireNonNull(configuration.getConfigurationSection(team)).
                getDoubleList("RespawnPosition");
        World world = location.getWorld();
        double x = respawnPosition.get(0);
        double y = respawnPosition.get(1);
        double z = respawnPosition.get(2);

        Location respawnLocation = new Location(world, x, y, z);

        player.teleport(respawnLocation);
        player.setBedSpawnLocation(respawnLocation, true);
        player.setGameMode(GameMode.SPECTATOR);
        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.teleport(respawnLocation);
            player.setGameMode(GameMode.SURVIVAL);
        }, configuration.getInt("RespawnCooldown") * 20L);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        List<Map<?, ?>> strongholdList = configuration.getMapList("StrongHold");
        if (strongholdList.size() == 1) {
            this.getLogger().warning("据点尚未配置，无法生成据点");
            return;
        }

        World world = Bukkit.getWorld("world");

        strongholdList.forEach(map -> {
            if (world == null) return;

            @SuppressWarnings("unchecked")
            List<Integer> position = (List<Integer>) map.get("Position");
            int x = position.get(0);
            int y = position.get(1);
            int z = position.get(2);

            Block beacon = world.getBlockAt(x, y-1, z);
            if (!beacon.getType().equals(Material.BEACON)) {
                beacon.setType(Material.BEACON);

                String ownerTeam = map.get("OwnerTeam").toString();

                Material glassMaterial = switch (ownerTeam) {
                    case "TeamRED" -> Material.RED_STAINED_GLASS;
                    case "TeamBLUE" -> Material.BLUE_STAINED_GLASS;
                    case "TeamGRAY" -> Material.GRAY_STAINED_GLASS;
                    case "TeamYELLOW" -> Material.YELLOW_STAINED_GLASS;
                    default -> Material.GLASS;
                };

                Block glass = world.getBlockAt(x, y, z);
                glass.setType(glassMaterial);

                Block ironBlock = null;
                for (int x2 = -1; x2 < 2; x2++) {
                    for (int z2 = -1; z2 < 2; z2++) {
                        ironBlock = world.getBlockAt(x+x2, y-2, z+z2);
                        ironBlock.setType(Material.IRON_BLOCK);
                    }
                }

                this.getLogger().info(String.format("位于(%d, %d, %d)的%s据点已成功生成", x, y, z, ownerTeam));
            }
        });
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        event.setCancelled(protectAreaOfStrongHold(event.getBlock()));
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> blocks = event.blockList();
        for (Block block: blocks) {
            if (protectAreaOfStrongHold(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();
        for (Block block: blocks) {
            if (protectAreaOfStrongHold(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        List<Block> blocks = event.getBlocks();
        for (Block block: blocks) {
            if (protectAreaOfStrongHold(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        event.setCancelled(protectAreaOfStrongHold(event.getBlock()));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> blocks = event.blockList();
        for (Block block: blocks) {
            if (protectAreaOfStrongHold(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(protectAreaOfStrongHold(event.getBlock()));
    }

    private boolean protectAreaOfStrongHold(Block block) {
        List<Map<?, ?>> strongholdList = configuration.getMapList("StrongHold");

        World world = block.getWorld();
        Location location = block.getLocation();

        for (Map<?, ?> map : strongholdList) {
            @SuppressWarnings("unchecked")
            List<Integer> position = (List<Integer>) map.get("Position");
            int x = position.get(0);
            int y = position.get(1);
            int z = position.get(2);

            Location minLocation = new Location(world, x, y, z).subtract(5, 5, 5);
            Location maxLocation = new Location(world, x, y, z).add(5, 5, 5);

            if (location.getBlockX() >= minLocation.getBlockX() && location.getBlockX() <= maxLocation.getBlockX()) {
                if (location.getBlockY() >= minLocation.getBlockY() && location.getBlockY() <= maxLocation.getBlockY()) {
                    if (location.getBlockZ() >= minLocation.getBlockZ() && location.getBlockZ() <= maxLocation.getBlockZ()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Deprecated
    private boolean protectStrongHold(Block block) {
        List<Map<?, ?>> strongholdList = configuration.getMapList("StrongHold");

        World world = block.getWorld();

        Location location = block.getLocation();
        for (Map<?, ?> map : strongholdList) {
            @SuppressWarnings("unchecked")
            List<Integer> position = (List<Integer>) map.get("Position");
            int x = position.get(0);
            int y = position.get(1);
            int z = position.get(2);

            Location nowLocation = new Location(world, x, y, z);

            if (location.equals(nowLocation)) {
                return block.getType().equals(Material.RED_STAINED_GLASS) ||
                        block.getType().equals(Material.BLUE_STAINED_GLASS) ||
                        block.getType().equals(Material.GRAY_STAINED_GLASS) ||
                        block.getType().equals(Material.YELLOW_STAINED_GLASS);
            }

            nowLocation.subtract(0, 1, 0);

            if (location.equals(nowLocation)) {
                return block.getType().equals(Material.BEACON);
            }

            for (int x2 = -1; x2 < 2; x2++) {
                for (int z2 = -1; z2 < 2; z2++) {
                    nowLocation.set(x+x2, y-2, z+z2);

                    if (location.equals(nowLocation)) {
                        if (block.getType().equals(Material.IRON_BLOCK)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
