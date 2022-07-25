package cc.venja.minebbs.battle.arena;

import cc.venja.minebbs.battle.BattleMain;
import cc.venja.minebbs.battle.calculation.GFG;
import cc.venja.minebbs.login.LoginMain;
import cc.venja.minebbs.login.enums.Team;
import cc.venja.minebbs.robot.RobotMain;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Sound.BLOCK_END_PORTAL_SPAWN;

public class ArenaSystem implements Listener {
    public static ArenaSystem instance;
    public static Map<Player, Location> lastLocation = new ConcurrentHashMap<>();
    public File configFile;
    public YamlConfiguration configuration;

    public ArenaSystem() throws IOException {
        instance = this;

        BattleMain.instance.getServer().getPluginManager().registerEvents(this, BattleMain.instance);

        configFile = new File(BattleMain.instance.getDataFolder().toPath().resolve("arena.yml").toString()).getAbsoluteFile();
        var configExists = configFile.exists();
        configuration = YamlConfiguration.loadConfiguration(configFile);
        if (!configExists) {
            configuration.set("TeamRED", new ArrayList<String>() {{
                //平原 NK 右上 *
                add("1334,229");
                add("1851,229");
                add("1851,1110");
                add("1294,1010");
                add("1082,812");
                add("1114,412");
            }});
            configuration.set("EnableTeamRED", true);

            configuration.set("TeamBLUE", new ArrayList<String>() {{
                //雪山 PM 左上 *
                add("229,229");
                add("229,830");
                add("844,1030");
                add("1160,230");
            }});
            configuration.set("EnableTeamBLUE", true);

            configuration.set("TeamGREY", new ArrayList<String>() {{
                //丛林 BDS 右下 *
                add("1851,1130");
                add("1370,1075");
                add("1170,1310");
                add("1170,1851");
                add("1851,1851");
            }});
            configuration.set("EnableTeamGREY", true);

            configuration.set("TeamYELLOW", new ArrayList<String>() {{
                //沙漠 Geyser 左下
                add("1115,1851");
                add("1115,1300");
                add("229,860");
                add("229,1851");
            }});
            configuration.set("EnableTeamYELLOW", true);

            configuration.set("EnablePortal", false);

            configuration.set("Assembling", false);
            configuration.set("TeamREDAssemblePoint", "");
            configuration.set("TeamREDAssemblePoint", "");
            configuration.set("TeamGREYAssemblePoint", "");
            configuration.set("TeamYELLOWAssemblePoint", "");

            configuration.set("CenterArea",new ArrayList<String>(){{
                add("800,1110");
                add("1067,842");
                add("1322,1038");
                add("1160,1265");
            }});
            configuration.set("CenterAccess", false);
            configuration.set("CenterEnable", false);
        }

        configuration.save(configFile);
    }

    public void runPlayerDetectionTask() {
        List<String> CenterVectorStr = configuration.getStringList("CenterArea");
        List<Vector> CenterVectors = new ArrayList<>();
        for (String str : CenterVectorStr) {
            CenterVectors.add(strToVector(str));
        }
        //提前加载中心区域范围

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!lastLocation.containsKey(p)) {
                            lastLocation.put(p, p.getLocation());
                        }
                        if (LoginMain.instance.onlinePlayers.get(p) == LoginMain.Status.LOGIN) {
                            Team team = RobotMain.getPlayerTeam(p.getName());
                            String teamStr = Objects.requireNonNull(team).getName();

                            if (isPlayerEnterPortal(teamStr, p)) {
                                continue;
                            }
                            List<String> vectorStr = configuration.getStringList(teamStr);
                            List<Vector> vectors = new ArrayList<>();
                            for (String str : vectorStr) {
                                vectors.add(strToVector(str));
                            }
                            Vector to = new Vector(p.getLocation().getX(), p.getLocation().getZ(), 0);

                            boolean updateLocation = true;

                            String enable = "Enable" + teamStr;
                            if (configuration.getBoolean(enable)) {//判断是否在队伍区域内
                                if (!p.isOp()) {
                                    if (!GFG.isInside(vectors.toArray(Vector[]::new), vectors.size(), to)) {

                                        //判断是否在中央区域内
                                        if (!GFG.isInside(CenterVectors.toArray(Vector[]::new), CenterVectors.size(), to)) {
                                            if (configuration.getBoolean("CenterAccess") && configuration.getBoolean("CenterEnable")) {
                                                updateLocation = false;
                                                new BukkitRunnable() {
                                                    @Override
                                                    public void run() {
                                                        p.teleport(lastLocation.get(p));
                                                    }
                                                }.runTask(BattleMain.instance);
                                                Audience.audience(p).sendActionBar(Component.text("§c禁止离开中心区"));
                                            } else {
                                                // 把玩家送回基地
                                                BattleMain.instance.teleportPlayerToTeamBase(p);
                                            }

                                        }
                                    } else {
                                        updateLocation = false;
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                p.teleport(lastLocation.get(p));
                                            }

                                        }.runTask(BattleMain.instance);
                                        Audience.audience(p).sendActionBar(Component.text("§c不可逾越允许活动范围"));
                                    }
                                }
                            }


                            if (updateLocation) {
                                lastLocation.put(p, p.getLocation());
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }.runTaskTimerAsynchronously(BattleMain.instance, 0L, 0L);
    }

    @EventHandler
    public void onEntityMove(ProjectileHitEvent event) throws SQLException {
        if (event.getEntity().getShooter() instanceof Player player) {
            Team team = RobotMain.getPlayerTeam(player.getName());
            String teamStr = Objects.requireNonNull(team).getName();
            List<String> vectorStr = configuration.getStringList(teamStr);
            List<Vector> vectors = new ArrayList<>();
            for (String str : vectorStr) {
                vectors.add(strToVector(str));
            }
            Location location = null;
            if (event.getHitEntity() != null) {
                location = event.getHitEntity().getLocation();
            }
            if (event.getHitBlock() != null) {
                location = event.getHitBlock().getLocation();
            }
            if (location != null) {
                Vector to = new Vector(location.getX(), location.getZ(), 0);
                String enable = "Enable" + teamStr;
                if (configuration.getBoolean(enable)) {
                    if (!GFG.isInside(vectors.toArray(Vector[]::new), vectors.size(), to)) {
                        event.setCancelled(true);
                        event.getEntity().setVelocity(new Vector(0, 0, 0));
                        event.getEntity().remove();
                        Audience.audience(player).sendActionBar(Component.text("§c不可逾越允许活动范围"));
                    }
                }
            }
        }
    }

    public boolean isPlayerEnterPortal(String TeamStr, Player event) {
        if (!configuration.getBoolean("EnablePortal")){
            return false;
        }

        Location loc = event.getLocation();
        int x = (int) loc.getX();
        int z = (int) loc.getZ();

        if (Objects.equals(TeamStr, "TeamRED")) {
            if (x == 1848 && Math.max(330, z) == Math.min(z, 336)) {
                event.playSound(event, BLOCK_END_PORTAL_SPAWN, 1F, 0F);
                Location toLoc = new Location(event.getWorld(), 1075, 71, 925);
                runSync(() -> event.teleport(toLoc));
                return true;
            }
        }
        if (Objects.equals(TeamStr, "TeamBLUE")){
            if (x == 374 && Math.max(328,z) == Math.min(z,334)) {
                event.playSound(event,BLOCK_END_PORTAL_SPAWN,1F,0F);
                Location toLoc = new Location(event.getWorld(), 895,71,1090);
                runSync(() -> event.teleport(toLoc));
                return true;
            }
        }
        if (Objects.equals(TeamStr, "TeamGREY")){
            if (x == 1730 && Math.max(1707,z) == Math.min(z,1713)) {
                event.playSound(event,BLOCK_END_PORTAL_SPAWN,1F,0F);
                Location toLoc = new Location(event.getWorld(), 1260,69,1045);
                runSync(() -> event.teleport(toLoc));
                return true;
            }
        }
        if (Objects.equals(TeamStr, "TeamYELLOW")){
            if (x == 294 && Math.max(1577,z) == Math.min(z,1581)) {
                event.playSound(event,BLOCK_END_PORTAL_SPAWN,1F,0F);
                Location toLoc = new Location(event.getWorld(), 1140,97,1220);
                runSync(() -> event.teleport(toLoc));
                return true;
            }
        }
        return false;
   }

    private Vector strToVector(String str) {
        return new Vector(Integer.parseInt(str.split(",")[0]), Integer.parseInt(str.split(",")[1]), 0);
    }

    private double distance(Vector v1, Vector v2) {
        return Math.sqrt(NumberConversions.square(v1.getX() - v2.getX()) + NumberConversions.square(v1.getY() - v2.getY()));
    }

    public static void forceTeleport(Player p, Location loc) {
        lastLocation.put(p, loc);
        p.teleport(loc);
    }

    private void runSync(Runnable runnable) {
        Bukkit.getScheduler().runTask(BattleMain.instance, runnable);
    }

}
