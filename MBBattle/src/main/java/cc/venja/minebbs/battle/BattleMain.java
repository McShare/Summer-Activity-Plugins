package cc.venja.minebbs.battle;

import cc.venja.minebbs.battle.arena.ArenaSystem;
import cc.venja.minebbs.battle.commands.ArenaManageCommand;
import cc.venja.minebbs.battle.commands.GameStatusChange;
import cc.venja.minebbs.battle.commands.GameStatusGet;
import cc.venja.minebbs.battle.commands.GenerateStrongHoldCommand;
import cc.venja.minebbs.battle.enums.GameStatus;
import cc.venja.minebbs.battle.events.GameStatusChangeEvent;
import cc.venja.minebbs.battle.scores.PlayerScoreHandle;
import cc.venja.minebbs.battle.scores.ShowScore;
import cc.venja.minebbs.battle.scores.TeamScoreHandle;
import cc.venja.minebbs.battle.team.ChatSystem;
import cc.venja.minebbs.battle.team.ColorTeamName;
import cc.venja.minebbs.login.enums.Team;
import cc.venja.minebbs.robot.RobotMain;
import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.util.Vector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public static File joinRecordFile;
    public static YamlConfiguration joinRecord;

    public Map<Player, Player> lastAttacker = new HashMap<>();
    public Map<String, List<Player>> occupies = new HashMap<>();

    public ColorTeamName colorTeamName;
    public ArenaSystem arenaSystem;
    public List<TeamScoreHandle> teamScoreHandleList = new ArrayList<>();
    public List<PlayerScoreHandle> playerScoreHandleList = new ArrayList<>();

    public static GameStatus status;

    public World world;

    public File personalCsvFile;
    public BufferedWriter personalCsvWriter;

    public File teamCsvFile;
    public BufferedWriter teamCsvWriter;

    @Override
    public void onEnable() {
        //注册指令
        Objects.requireNonNull(this.getServer().getPluginCommand("generate-stronghold")).setExecutor(new GenerateStrongHoldCommand());
        Objects.requireNonNull(this.getServer().getPluginCommand("gamestatus-change")).setExecutor(new GameStatusChange());
        Objects.requireNonNull(this.getServer().getPluginCommand("gamestatus")).setExecutor(new GameStatusGet());
        Objects.requireNonNull(this.getServer().getPluginCommand("arena")).setExecutor(new ArenaManageCommand());
        //初始化配置文件
        try {
            configFile = new File(this.getDataFolder().toPath().resolve("config.yml").toString()).getAbsoluteFile();
            var configExists = configFile.exists();
            configuration = YamlConfiguration.loadConfiguration(configFile);
            if (!configExists) {
                Map<String, Object> teamConfig = new HashMap<>() {
                    {
                        put("RespawnPosition", new double[]{0, 0, 0});
                    }
                };

                configuration.set("TeamRED", teamConfig);
                configuration.set("TeamBLUE", teamConfig);
                configuration.set("TeamGREY", teamConfig);
                configuration.set("TeamYELLOW", teamConfig);

                configuration.set("RespawnCooldown", 20);

                configuration.set("StrongHold", new ArrayList<>() {
                    {
                        add(new HashMap<String, Object>() {
                            {
                                put("Id", "0");
                                put("Position", new int[]{0, 0, 0});
                                put("OwnerTeam", "TeamRED");
                                put("ProtectRange", new int[]{7, 7, 7});
                                put("OccupyTime", 10);
                                put("OccupyRange", new int[]{5, 5, 5});
                            }
                        });
                    }
                });
            }

            configuration.save(configFile);
            //初始化数据文件
            dataFile = new File(this.getDataFolder().toPath().resolve("data.yml").toString()).getAbsoluteFile();
            var dataExists = configFile.exists();
            data = YamlConfiguration.loadConfiguration(dataFile);
            status = GameStatus.values()[data.getInt("status")];
            if (!dataExists) {
                data.set("status", GameStatus.PEACETIME.getValue());
                data.save(dataFile);
            }
            //初始化队伍计分文件
            teamScoreFile = new File(this.getDataFolder().toPath().resolve("scores").resolve("team.yml").toString()).getAbsoluteFile();
            teamScore = YamlConfiguration.loadConfiguration(teamScoreFile);
            //初始化个人积分文件
            personalScoreFile = new File(this.getDataFolder().toPath().resolve("scores").resolve("personal.yml").toString()).getAbsoluteFile();
            personalScore = YamlConfiguration.loadConfiguration(personalScoreFile);
            //初始化进服记录文件
            joinRecordFile = new File(this.getDataFolder().toPath().resolve("record.yml").toString()).getAbsoluteFile();
            joinRecord = YamlConfiguration.loadConfiguration(joinRecordFile);
            //初始化个人积分变更日志文件
            personalCsvFile = new File(this.getDataFolder().toPath().resolve("scores").resolve("personal_record.csv").toString()).getAbsoluteFile();
            personalCsvWriter = new BufferedWriter(new FileWriter(personalCsvFile));
            //初始化团队积分变更日志文件
            teamCsvFile = new File(this.getDataFolder().toPath().resolve("scores").resolve("team_record.csv").toString()).getAbsoluteFile();
            teamCsvWriter = new BufferedWriter(new FileWriter(teamCsvFile));

            this.getServer().getPluginManager().registerEvents(this, this);
            this.getServer().getPluginManager().registerEvents(new ChatSystem(), this);
            arenaSystem = new ArenaSystem();

            teamScoreHandleList.add(new TeamScoreHandle(Team.RED));
            teamScoreHandleList.add(new TeamScoreHandle(Team.BLUE));
            teamScoreHandleList.add(new TeamScoreHandle(Team.GREY));
            teamScoreHandleList.add(new TeamScoreHandle(Team.YELLOW));

            arenaSystem.runPlayerDetectionTask();
            colorTeamName = new ColorTeamName();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            //注意需开启文件续写功能
            teamCsvWriter = new BufferedWriter(new FileWriter(BattleMain.instance.teamCsvFile,true));
            personalCsvWriter = new BufferedWriter(new FileWriter(BattleMain.instance.personalCsvFile,true));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onDisable() {
        //关闭csv积分日志文件
        try {
            teamCsvWriter.close();
            personalCsvWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
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

        world = Bukkit.getWorld("world");

        strongholdList.forEach(map -> {
            if (world == null) return;

            @SuppressWarnings("unchecked")
            List<Integer> position = (List<Integer>) map.get("Position");
            int x = position.get(0);
            int y = position.get(1);
            int z = position.get(2);

            Block beacon = world.getBlockAt(x, y - 1, z);
            if (!beacon.getType().equals(Material.BEACON)) {
                beacon.setType(Material.BEACON);

                String ownerTeam = map.get("OwnerTeam").toString();

                Material glassMaterial = switch (ownerTeam) {
                    case "TeamRED" -> Material.RED_STAINED_GLASS;
                    case "TeamBLUE" -> Material.BLUE_STAINED_GLASS;
                    case "TeamGREY" -> Material.GRAY_STAINED_GLASS;
                    case "TeamYELLOW" -> Material.YELLOW_STAINED_GLASS;
                    default -> Material.GLASS;
                };

                Block glass = world.getBlockAt(x, y, z);
                glass.setType(glassMaterial);

                Block ironBlock;
                for (int x2 = -1; x2 < 2; x2++) {
                    for (int z2 = -1; z2 < 2; z2++) {
                        ironBlock = world.getBlockAt(x + x2, y - 2, z + z2);
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

        middleCollectPointDetect();
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(protectAreaOfStrongHold(event.getBlock()));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!event.getPlayer().isOp()) {
            event.setCancelled(protectAreaOfStrongHold(event.getBlock()));
        }
    }

    @EventHandler
    public void onBlockDestroy(BlockDestroyEvent event) {
        event.setCancelled(protectAreaOfStrongHold(event.getBlock()));
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        List<Block> blocks = event.blockList();
        for (Block block : blocks) {
            if (protectAreaOfStrongHold(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();
        for (Block block : blocks) {
            if (protectAreaOfStrongHold(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        List<Block> blocks = event.getBlocks();
        for (Block block : blocks) {
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
        for (Block block : blocks) {
            if (protectAreaOfStrongHold(block)) {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        if (event.getSource().getType().equals(Material.WATER) || event.getSource().getType().equals(Material.LAVA)) {
            event.setCancelled(protectAreaOfStrongHold(event.getBlock()));
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) throws Exception {
        Player player = event.getPlayer();
        for (PlayerScoreHandle scoreHandle : playerScoreHandleList) {
            if (player.getName().equals(scoreHandle.getScore().getPlayer().getName())) {
                scoreHandle.deductScoreByDeath();
            }
        }
        if (player.getKiller() != null) {
            Player killer = player.getKiller();
            for (PlayerScoreHandle scoreHandle : playerScoreHandleList) {
                if (killer.getName().equals(scoreHandle.getScore().getPlayer().getName())) {
                    scoreHandle.addScoreByKillOtherPlayer();
                }
            }
        }
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

        Player player = event.getPlayer();
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

        if (configuration.getBoolean("CenterAccess") && configuration.getBoolean("CenterEnable")) {
            if (arenaSystem.isInCenter(new Vector(player.getLocation().getX(), player.getLocation().getZ(), 0))) {
                if (player.getHealth() < 14) {
                    if (lastAttacker.containsKey(player)) {
                        player.damage(114514, lastAttacker.get(player));
                        lastAttacker.remove(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            Player player = event.getPlayer();

            playerScoreHandleList.add(new PlayerScoreHandle(player));

            String name = RobotMain.getRealPlayerName(player.getName());
            Team team = RobotMain.getPlayerTeam(name);
            String teamName = Objects.requireNonNull(team).getName();

            org.bukkit.scoreboard.Team scoreboardTeam = colorTeamName.getTeamByTeamName(teamName);
            if (!scoreboardTeam.hasEntry(player.getName())) {
                scoreboardTeam.addEntry(player.getName());
            }

            if (!joinRecord.contains(name.toLowerCase())) {
                joinRecord.set(name.toLowerCase(), true);
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                    try {
                        joinRecord.save(joinRecordFile);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                });
                teleportPlayerToTeamBase(player);
            }

            if (player.getGameMode().equals(GameMode.SPECTATOR)) {


                List<Double> respawnPosition = Objects.requireNonNull(configuration.getConfigurationSection(teamName)).
                        getDoubleList("RespawnPosition");
                double x = respawnPosition.get(0);
                double y = respawnPosition.get(1);
                double z = respawnPosition.get(2);

                Location respawnLocation = new Location(world, x, y, z);

                player.teleport(respawnLocation);
                player.setBedSpawnLocation(respawnLocation, true);
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    player.teleport(respawnLocation);
                    player.setGameMode(GameMode.SURVIVAL);
                }, configuration.getInt("RespawnCooldown") * 20L);
            }

            player.displayName(Component.text(Team.getColorCode(Objects.requireNonNull(team)) + name));
            ShowScore show = new ShowScore();
            show.UpdateScoreboard();
        } catch (Exception e) {
            this.getLogger().warning(e.toString());
        }
    }

    public void onGameStatusChange(GameStatusChangeEvent event) {
        try {
            data.set("status", event.nowStatus.getValue());
            data.save(dataFile);

            String title = String.format("§4%s 已结束！", event.beforeStatus.getName());
            String subtitle = String.format("§2当前为 %s", event.nowStatus.getName());
            for (Player player : Bukkit.getOnlinePlayers()) {
                Audience.audience(player).showTitle(Title.title(Component.text(title), Component.text(subtitle)));
            }

            if (event.beforeStatus.equals(GameStatus.OFF_DEF_DAY)) {

                List<Map<?, ?>> strongholdList = configuration.getMapList("StrongHold");

                for (Map<?, ?> stronghold : strongholdList) {
                    String strongholdId = stronghold.get("Id").toString();
                    ConfigurationSection section = data.getConfigurationSection(strongholdId);

                    if (section != null) {
                        String occupyTeamString = section.getString("OccupyTeam");
                        String ownerString = stronghold.get("OwnerTeam").toString();

                        Team occupier = Team.getByName(occupyTeamString);
                        Team owner = Team.getByName(ownerString);

                        if (!owner.equals(Team.ADMIN)) {
                            if (!Objects.equals(occupyTeamString, "") ||
                                    !Objects.equals(occupyTeamString, ownerString)) {

                                for (TeamScoreHandle scoreHandle : teamScoreHandleList) {
                                    if (scoreHandle.getScore().getTeam().equals(owner)) {
                                        scoreHandle.deductScoreByStrongHoldOccupied();
                                    } else if (scoreHandle.getScore().getTeam().equals(occupier)) {
                                        scoreHandle.addScoreByOccupyStrongHold();
                                    }
                                }
                            }
                        }
                    }
                }

                teleportAllPlayerToCorrTeamBase();
            } else if (event.beforeStatus.equals(GameStatus.BATTLE_DAY)) {
                teleportAllPlayerToCorrTeamBase();
                ArenaSystem.instance.configuration.set("EnablePortal", false);
                ArenaSystem.instance.configuration.set("CenterAccess", false);
                ArenaSystem.instance.configuration.set("CenterEnable", false);
                ArenaSystem.instance.configuration.save(ArenaSystem.instance.configFile);
            } else if (event.nowStatus.equals(GameStatus.BATTLE_DAY)) {
                teleportAllPlayerToCorrTeamBase();
                ArenaSystem.instance.configuration.set("EnablePortal", true);
                ArenaSystem.instance.configuration.set("CenterAccess", true);
                ArenaSystem.instance.configuration.set("CenterEnable", true);
                ArenaSystem.instance.configuration.save(ArenaSystem.instance.configFile);
            }
        } catch (Exception e) {
            this.getLogger().warning(e.toString());
        }
    }

    private void teleportAllPlayerToCorrTeamBase() throws Exception {
        for (Player player : Bukkit.getOnlinePlayers()) {
            teleportPlayerToTeamBase(player);
        }
    }

    public void teleportPlayerToTeamBase(Player player) throws Exception {
        Team teamValue = RobotMain.getPlayerTeam(player.getName());
        String team = Objects.requireNonNull(teamValue).getName();

        List<Double> respawnPosition = Objects.requireNonNull(configuration.getConfigurationSection(team)).
                getDoubleList("RespawnPosition");
        World world = Bukkit.getWorld("world");
        double x = respawnPosition.get(0);
        double y = respawnPosition.get(1);
        double z = respawnPosition.get(2);

        Location respawnLocation = new Location(world, x, y, z);

        ArenaSystem.forceTeleport(player, respawnLocation);
    }

    private void OccupyDetect() {
        Map<String, BossBar> bossBarMap = new HashMap<>();

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            Collection<? extends Player> online = Bukkit.getOnlinePlayers();
            List<Map<?, ?>> strongholdList = configuration.getMapList("StrongHold");

            for (Map<?, ?> stronghold : strongholdList) {
                @SuppressWarnings("unchecked")
                List<Integer> position = (List<Integer>) stronghold.get("Position");
                int x = position.get(0);
                int y = position.get(1);
                int z = position.get(2);

                @SuppressWarnings("unchecked")
                List<Integer> range = (List<Integer>) stronghold.get("OccupyRange");
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
                    occupies.put(strongholdId, new ArrayList<>() {
                    });
                }

                List<Player> occupyPlayers = occupies.get(strongholdId);
                int occupiersNumber = occupyPlayers.size();

                if (!bossBarMap.containsKey(strongholdId)) {
                    Team team = Team.getByName(ownerTeam);
                    String ownerTeamWithColor = Team.getColorCode(team) + team.getName() + "§r";
                    String bossBarTitle = String.format("%s 所属队伍: %s", strongholdId, ownerTeamWithColor);
                    Map<Team, BarColor> TeamColorRefer = new HashMap<>() {
                        {
                            put(Team.RED, BarColor.RED);
                            put(Team.BLUE, BarColor.BLUE);
                            put(Team.GREY, BarColor.WHITE);
                            put(Team.YELLOW, BarColor.YELLOW);
                            put(Team.ADMIN, BarColor.GREEN);
                        }
                    };
                    var occupyShow = Bukkit.createBossBar(bossBarTitle,
                            TeamColorRefer.get(team), BarStyle.SOLID, BarFlag.CREATE_FOG);
                    bossBarMap.put(strongholdId, occupyShow);
                }
                var occupyShow = bossBarMap.get(strongholdId);

                for (Player player : online) {
                    if (!player.getGameMode().equals(GameMode.SPECTATOR)) {
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
                }

                if (occupiersNumber >= 1) {
                    try {
                        List<Team> occupiersTeam = new ArrayList<>();
                        for (Player player : occupyPlayers) {
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

                                String occupyTeam = team.getName();
                                double occupyTime = (int) stronghold.get("OccupyTime");

                                String occupiedTeam = section.getString("OccupyTeam");

                                if (!Objects.equals(occupyTeam, occupiedTeam) && !Objects.equals(occupyTeam, ownerTeam)) {
                                    occupyPercentage = 0.0;
                                }
                                Block glass = world.getBlockAt(x, y, z);

                                if (!ownerTeam.equals(occupyTeam)) {

                                    if (occupyPercentage < 1.0) {
                                        section.set("OccupyTeam", occupyTeam);
                                        occupyPercentage += 1.0 / occupyTime;
                                    }

                                    if (occupyPercentage > 1.0) {
                                        occupyPercentage = 1.0;
                                    }

                                    if (occupyPercentage == 1.0) {
                                        String occupied;
                                        if (!Objects.equals(occupyTeam, "")) {
                                            Team occupyTeamObj = Team.getByName(occupyTeam);
                                            String occupyTeamWithColor = Team.getColorCode(occupyTeamObj) + occupyTeam + "§r";
                                            occupied = String.format("已被 %s 占领", occupyTeamWithColor);

                                            Team ownerTeamObj = Team.getByName(ownerTeam);
                                            String ownerTeamWithColor = Team.getColorCode(ownerTeamObj) + ownerTeam + "§r";
                                            occupyShow.setTitle(
                                                    String.format("%s 所属队伍: %s %s",
                                                            strongholdId, ownerTeamWithColor, occupied)
                                            );

                                            Material glassMaterial = switch (occupyTeam) {
                                                case "TeamRED" -> Material.RED_STAINED_GLASS;
                                                case "TeamBLUE" -> Material.BLUE_STAINED_GLASS;
                                                case "TeamGREY" -> Material.GRAY_STAINED_GLASS;
                                                case "TeamYELLOW" -> Material.YELLOW_STAINED_GLASS;
                                                default -> Material.GLASS;
                                            };
                                            if (!glass.getType().equals(glassMaterial)) {
                                                glass.setType(glassMaterial);

                                                for (Player player : Bukkit.getOnlinePlayers()) {
                                                    String name = player.getName();
                                                    Team playerTeam = RobotMain.getPlayerTeam(name);
                                                    Audience audience = Audience.audience(player);

                                                    if (Objects.equals(playerTeam, ownerTeamObj)) {
                                                        String title = String.format("我方据点 %s", strongholdId);
                                                        String teamName = Team.getColorCode(occupyTeamObj) + occupyTeamObj;
                                                        String subtitle = String.format("已被 %s 占领！", teamName);
                                                        audience.showTitle(Title.title(Component.text(title), Component.text(subtitle)));
                                                    } else if (Objects.equals(playerTeam, occupyTeamObj)) {
                                                        if (!ownerTeamObj.equals(Team.ADMIN)) {
                                                            String teamName = Team.getColorCode(ownerTeamObj) + ownerTeam;
                                                            String subtitle = String.format("%s 据点 %s！", teamName, strongholdId);
                                                            audience.showTitle(Title.title(Component.text("我方以占领"), Component.text(subtitle)));
                                                        } else {
                                                            audience.showTitle(Title.title(Component.text("我方以占领中央据点！"), Component.text("")));
                                                        }
                                                    } else if (ownerTeamObj.equals(Team.ADMIN)) {
                                                        String teamName = Team.getColorCode(occupyTeamObj) + occupyTeamObj;
                                                        String subtitle = String.format("已被 %s 占领！", teamName);
                                                        audience.showTitle(Title.title(Component.text("中央据点"), Component.text(subtitle)));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (occupyPercentage > 0.0) {
                                        occupyPercentage -= 1.0 / occupyTime;
                                    }

                                    if (occupyPercentage < 0) {
                                        occupyPercentage = 0;
                                    }
                                    Team ownerTeamObj = Team.getByName(ownerTeam);

                                    if (occupyPercentage == 0.0) {
                                        if (!Objects.equals(section.getString("OccupyTeam"), "")) {
                                            section.set("OccupyTeam", "");
                                            for (Player player : Bukkit.getOnlinePlayers()) {
                                                String name = player.getName();
                                                Team playerTeam = RobotMain.getPlayerTeam(name);
                                                Audience audience = Audience.audience(player);

                                                if (Objects.equals(playerTeam, ownerTeamObj)) {
                                                    String title = String.format("我方据点 %s 已被收回", strongholdId);
                                                    audience.showTitle(Title.title(Component.text(title), Component.text("")));

                                                    Material glassMaterial = switch (ownerTeam) {
                                                        case "TeamRED" -> Material.RED_STAINED_GLASS;
                                                        case "TeamBLUE" -> Material.BLUE_STAINED_GLASS;
                                                        case "TeamGREY" -> Material.GRAY_STAINED_GLASS;
                                                        case "TeamYELLOW" -> Material.YELLOW_STAINED_GLASS;
                                                        default -> Material.GLASS;
                                                    };

                                                    if (!glass.getType().equals(glassMaterial)) {
                                                        glass.setType(glassMaterial);
                                                    }

                                                    String ownerTeamWithColor = Team.getColorCode(ownerTeamObj) + ownerTeam + "§r";
                                                    occupyShow.setTitle(String.format("%s 所属队伍: %s", strongholdId, ownerTeamWithColor)
                                                    );
                                                }
                                            }
                                        }
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

    private void middleCollectPointDetect() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            try {
                List<Map<?, ?>> strongholdList = configuration.getMapList("StrongHold");
                for (Map<?, ?> stronghold : strongholdList) {
                    String ownerString = stronghold.get("OwnerTeam").toString();
                    Team owner = Team.getByName(ownerString);

                    if (owner.equals(Team.ADMIN)) {
                        String strongholdId = stronghold.get("Id").toString();
                        ConfigurationSection section = data.getConfigurationSection(strongholdId);

                        if (section != null) {
                            String occupyTeamString = section.getString("OccupyTeam");
                            Team occupier = Team.getByName(occupyTeamString);

                            for (TeamScoreHandle scoreHandle : teamScoreHandleList) {
                                if (scoreHandle.getScore().getTeam().equals(occupier)) {
                                    scoreHandle.addScoreByMidPointOccupy();
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                this.getLogger().warning(e.toString());
            }
        }, 0, 20 * 60);
    }

    private boolean protectAreaOfStrongHold(Block block) {
        //String xyz = String.format("(%s %s %s)", block.getX(), block.getY(), block.getZ());
        //String warning = String.format("正在尝试保护位于 %s 的 方块 %s", xyz, block.getType());
        //this.getLogger().warning(warning);

        List<Map<?, ?>> strongholdList = configuration.getMapList("StrongHold");

        World world = block.getWorld();
        Location location = block.getLocation();

        for (Map<?, ?> map : strongholdList) {
            @SuppressWarnings("unchecked")
            List<Integer> position = (List<Integer>) map.get("Position");
            int x = position.get(0);
            int y = position.get(1) - 1;
            int z = position.get(2);

            @SuppressWarnings("unchecked")
            List<Integer> range = (List<Integer>) map.get("ProtectRange");
            int rangeX = range.get(0);
            int rangeY = range.get(1);
            int rangeZ = range.get(2);

            int xRange = (rangeX - 1) / 2;
            int yRange = (rangeY - 1) / 2;
            int zRange = (rangeZ - 1) / 2;

            Location minLocation = new Location(world, x, y, z).subtract(xRange, yRange, zRange);
            Location maxLocation = new Location(world, x, y, z).add(xRange, yRange, zRange);

            //this.getLogger().warning(String.format("检测据点 %s", map.get("Id")));
            //this.getLogger().warning(String.format("匹配据点 %s X轴", map.get("Id")));
            //this.getLogger().warning(String.format("最大: %s | 中间: %s | 最小: %s | 位于: %s",
            //maxLocation.getBlockX(), x, minLocation.getBlockX(), location.getBlockX()));

            if (location.getBlockX() >= minLocation.getBlockX() && location.getBlockX() <= maxLocation.getBlockX()) {
                // this.getLogger().warning(String.format("匹配据点 %s Y轴", map.get("Id")));
                // this.getLogger().warning(String.format("最大: %s | 中间: %s | 最小: %s | 位于: %s",
                // maxLocation.getBlockY(), y, minLocation.getBlockY(), location.getBlockY()));

                if (location.getBlockY() >= minLocation.getBlockY() && location.getBlockY() <= maxLocation.getBlockY()) {
                    //this.getLogger().warning(String.format("匹配据点 %s Z轴", map.get("Id")));
                    //this.getLogger().warning(String.format("最大: %s | 中间: %s | 最小: %s | 位于: %s",
                    // maxLocation.getBlockZ(), z, minLocation.getBlockZ(), location.getBlockZ()));

                    if (location.getBlockZ() >= minLocation.getBlockZ() && location.getBlockZ() <= maxLocation.getBlockZ()) {
                        //xyz = String.format("(%s %s %s)", block.getX(), block.getY(), block.getZ());
                        //warning = String.format("拦截位于 %s 的 方块 %s 成功", xyz, block.getType());
                        //this.getLogger().warning(warning);

                        return true;
                    }
                }
            }

            if (location.getBlockX() == x && location.getBlockZ() == z && location.getBlockY() > y) {
                //this.getLogger().warning("检测方块是否遮挡信标柱");
                if (block.getBlockData().getMaterial().isOccluding()) {
                    //warning = String.format("拦截位于 %s 的 方块 %s 成功", xyz, block.getType());
                    //this.getLogger().warning(warning);
                    return true;
                } else {
                    Material material = block.getBlockData().getMaterial();
                    String materialString = block.getBlockData().getMaterial().toString();
                    if (material.equals(Material.TINTED_GLASS) || materialString.contains("STAINED")) {
                        //warning = String.format("拦截位于 %s 的 方块 %s 成功", xyz, block.getType());
                        //this.getLogger().warning(warning);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) throws SQLException {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        if (damager.getType().equals(EntityType.PLAYER) && damagee.getType().equals(EntityType.PLAYER)) {
            if (RobotMain.getPlayerTeam(damager.getName()) == RobotMain.getPlayerTeam(damagee.getName())) {
                event.setCancelled(true);
            } else {
                setLastAttacker((Player)damagee, (Player)damager);
            }
        } else if (damagee.getType().equals(EntityType.PLAYER) && damager instanceof Projectile projectile) {
            if (projectile.getShooter() != null) {
                if (projectile.getShooter() instanceof Player p) {
                    if (RobotMain.getPlayerTeam(p.getName()) == RobotMain.getPlayerTeam(damagee.getName())) {
                        event.setCancelled(true);
                    } else {
                        setLastAttacker((Player) damagee, p);
                    }
                }
            }
        }
    }


    public void setLastAttacker(Player victim, Player attacker) {
        lastAttacker.put(victim, attacker);
        new BukkitRunnable() {
            @Override
            public void run() {
                lastAttacker.remove(victim);
            }
        }.runTaskLater(this, 300);

    }
}
