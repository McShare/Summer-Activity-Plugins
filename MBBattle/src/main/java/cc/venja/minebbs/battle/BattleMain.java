package cc.venja.minebbs.battle;

import cc.venja.minebbs.battle.arena.ArenaSystem;
import cc.venja.minebbs.battle.commands.GenerateStrongHoldCommand;
import cc.venja.minebbs.battle.data.PlayerData;
import cc.venja.minebbs.battle.scores.ScoreHandle;
import cc.venja.minebbs.login.enums.Team;
import cc.venja.minebbs.robot.RobotMain;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;
import java.util.*;

public class BattleMain extends JavaPlugin implements Listener {
    public static BattleMain instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    public static File configFile;
    public static YamlConfiguration configuration;

    public static File dataFile;
    public static YamlConfiguration data;

    public static File teamScoreFile;
    public static YamlConfiguration teamScore;

    public static File personalScoreFile;
    public static YamlConfiguration personalScore;


    public Map<String, List<Player>> occupies = new HashMap<>();

    public ArenaSystem arenaSystem;

    @Override
    public void onEnable() {
        Objects.requireNonNull(this.getServer().getPluginCommand("generate-stronghold")).setExecutor(new GenerateStrongHoldCommand());

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
                                put("Id", "0");
                                put("Position", new int[] {0, 0, 0});
                                put("OwnerTeam", "TeamRED");
                                put("Range", new int[] {7, 7, 7});
                                put("OccupyTime", 10);
                            }
                        });
                    }
                });
            }

            configuration.save(configFile);

            dataFile = new File(this.getDataFolder().toPath().resolve("data.yml").toString()).getAbsoluteFile();
            data = YamlConfiguration.loadConfiguration(dataFile);

            teamScoreFile = new File(this.getDataFolder().toPath().resolve("scores").resolve("team.yml").toString()).getAbsoluteFile();
            teamScore = YamlConfiguration.loadConfiguration(dataFile);

            teamScoreFile = new File(this.getDataFolder().toPath().resolve("scores").resolve("personal.yml").toString()).getAbsoluteFile();
            teamScore = YamlConfiguration.loadConfiguration(dataFile);

            this.getServer().getPluginManager().registerEvents(this, this);

            arenaSystem = new ArenaSystem();
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
            Player pDamager = (Player) damager;
            Player pDamagee = (Player) damagee;
            if (event.getFinalDamage() >= pDamagee.getHealth()) {
                ScoreHandle.onKillPlayer(pDamager);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent event) throws SQLException {
        Player player = event.getPlayer();
        Location location = event.getRespawnedLocation();

        Team teamValue = RobotMain.getPlayerTeam(event.getPlayer().getName());
        String team = Objects.requireNonNull(teamValue).getName();

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

                Block ironBlock;
                for (int x2 = -1; x2 < 2; x2++) {
                    for (int z2 = -1; z2 < 2; z2++) {
                        ironBlock = world.getBlockAt(x+x2, y-2, z+z2);
                        ironBlock.setType(Material.IRON_BLOCK);
                    }
                }

                if (data.get(map.get("Id").toString()) == null) {
                    data.set(map.get("Id").toString(), new HashMap<String, Object>() {
                        {
                            put("OccupyPercentage", 0.0);
                            put("OccupyTeam", "");
                        }
                    });
                    try {
                        data.save(dataFile);
                    } catch (Exception e) {
                        this.getLogger().warning(e.toString());
                    }

                }

                this.getLogger().info(String.format("位于(%d, %d, %d)的%s据点已成功生成", x, y, z, ownerTeam));
            }
        });

        OccupyDetect();
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(protectAreaOfStrongHold(event.getBlock()));
        }
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
    public void onPlayerDeath(PlayerDeathEvent event) {
        ScoreHandle.onPlayerDeath(event.getPlayer());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(protectAreaOfStrongHold(event.getBlock()));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        List<Map<?, ?>> strongholdList = configuration.getMapList("StrongHold");

        for (Map<?, ?> stronghold : strongholdList) {
            String strongholdId = stronghold.get("Id").toString();

            List<Player> occupyPlayers = occupies.get(strongholdId);

            for (int i = 0; i < occupyPlayers.size(); i++) {
                Player player1 = occupyPlayers.get(i);
                if (player1.getName().equals(event.getPlayer().getName())) {
                    occupyPlayers.remove(i);
                    i -= 1;
                }
            }
        }
    }

    private void OccupyDetect() {
        Map<String, BossBar> bossBarMap = new HashMap<>();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            Collection<? extends Player> online = Bukkit.getOnlinePlayers();
            List<Map<?, ?>> strongholdList = configuration.getMapList("StrongHold");

            for (Map<?, ?> stronghold : strongholdList) {
                @SuppressWarnings("unchecked")
                List<Integer> position = (List<Integer>) stronghold.get("Position");
                int x = position.get(0);
                int y = position.get(1);
                int z = position.get(2);

                @SuppressWarnings("unchecked")
                List<Integer> range = (List<Integer>) stronghold.get("Range");
                int rangeX = range.get(0);
                int rangeY = range.get(1);
                int rangeZ = range.get(2);

                int xRange = (rangeX - 1) / 2;
                int yRange = (rangeY - 1) / 2;
                int zRange = (rangeZ - 1) / 2;

                String strongholdId = stronghold.get("Id").toString();

                ConfigurationSection section = data.getConfigurationSection(strongholdId);

                String ownerTeam = stronghold.get("OwnerTeam").toString();

                if (!occupies.containsKey(strongholdId)) {
                    occupies.put(strongholdId, new ArrayList<>() {});
                }

                List<Player> occupyPlayers = occupies.get(strongholdId);
                int occupiersNumber = occupyPlayers.size();

                if (!bossBarMap.containsKey(strongholdId)) {
                    Team team = Team.getByName(ownerTeam);
                    String ownerTeamWithColor = Team.getColorCode(team)+team.getName()+"§r";
                    String bossBarTitle = String.format("%s 所属队伍: %s", strongholdId, ownerTeamWithColor);
                    Map<Team, BarColor> TeamColorRefer = new HashMap<>() {
                        {
                            put(Team.RED, BarColor.RED);
                            put(Team.BLUE, BarColor.BLUE);
                            put(Team.GREY, BarColor.WHITE);
                            put(Team.YELLOW, BarColor.YELLOW);
                        }
                    };
                    var occupyShow = Bukkit.createBossBar(bossBarTitle,
                            TeamColorRefer.get(Team.getByName(ownerTeam)), BarStyle.SOLID, BarFlag.CREATE_FOG);
                    bossBarMap.put(strongholdId, occupyShow);
                }
                var occupyShow = bossBarMap.get(strongholdId);

                for (Player player: online) {
                    Location location = player.getLocation();

                    if (location.getX() >= x - xRange && location.getX() <= x + xRange &&
                            location.getY() >= y - yRange && location.getY() <= y + yRange &&
                            location.getZ() >= z - zRange && location.getZ() <= z + zRange) {
                        if (occupies.containsKey(strongholdId)) {
                            boolean hasPlayer = false;
                            for (Player player1 : occupyPlayers) {
                                if (player1.getName().equals(player.getName())) {
                                    hasPlayer = true;
                                }
                            }
                            if (!hasPlayer) {
                                occupyShow.addPlayer(player);
                                occupyPlayers.add(player);
                            }
                        }
                    } else {
                        for (int i = 0; i < occupyPlayers.size(); i++) {
                            Player player1 = occupyPlayers.get(i);
                            if (player1.getName().equals(player.getName())) {
                                occupyShow.removePlayer(player);
                                occupyPlayers.remove(i);
                                i -= 1;
                            }
                        }
                    }
                }

                if (occupiersNumber >= 1) {
                    try {
                        List<Team> occupiersTeam = new ArrayList<>();
                        for (Player player: occupyPlayers) {
                            String name = player.getName();
                            Team team = RobotMain.getPlayerTeam(name);
                            if (!occupiersTeam.contains(team)) {
                                occupiersTeam.add(team);
                            }
                        }

                        if (section != null) {
                            if (occupiersTeam.size() == 1) {
                                Team team = occupiersTeam.get(0);
                                double occupyPercentage = section.getDouble("OccupyPercentage");

                                String OccupyTeam = team.getName();
                                double OccupyTime = (double) stronghold.get("OccupyTime");
                                if (!ownerTeam.equals(OccupyTeam)) {
                                    if (occupyPercentage < 1.0) {
                                        section.set("OccupyTeam", OccupyTeam);
                                        occupyPercentage += 1.0/OccupyTime;
                                    }

                                    if (occupyPercentage > 1.0) {
                                        occupyPercentage = 1.0;
                                    }

                                    if (occupyPercentage == 1.0) {
                                        String occupied = "";
                                        String occupyTeam = section.getString("OccupyTeam");
                                        if (!Objects.equals(occupyTeam, "")) {
                                            Team occupyTeamObj = Team.getByName(occupyTeam);
                                            String occupyTeamWithColor = Team.getColorCode(occupyTeamObj)+occupyTeam+"§r";
                                            occupied = String.format("已被 %s 占领", occupyTeamWithColor);
                                        }
                                        Team ownerTeamObj = Team.getByName(ownerTeam);
                                        String ownerTeamWithColor = Team.getColorCode(ownerTeamObj)+ownerTeam+"§r";
                                        occupyShow.setTitle(
                                                String.format("%s 所属队伍: %s %s",
                                                        strongholdId, ownerTeamWithColor, occupied)
                                        );
                                    }
                                } else {
                                    if (occupyPercentage != 0.0) {
                                        section.set("OccupyTeam", "");
                                        occupyPercentage -= 1.0/OccupyTime;
                                    }
                                }
                                occupyShow.setProgress(occupyPercentage);
                                section.set("OccupyPercentage", occupyPercentage);
                                data.save(dataFile);
                            }
                        }
                    } catch (Exception e) {
                        this.getLogger().warning(e.toString());
                    }
                }
            }
        }, 0, 20);
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

            if (location.getBlockX() == x && location.getBlockZ() == z && location.getBlockY() > y) {
                if (block.getBlockData().getMaterial().isOccluding()) {
                    return true;
                } else {
                    Material material = block.getBlockData().getMaterial();
                    String materialString = block.getBlockData().getMaterial().toString();
                    if (material.equals(Material.TINTED_GLASS) || materialString.contains("STAINED")) {
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
