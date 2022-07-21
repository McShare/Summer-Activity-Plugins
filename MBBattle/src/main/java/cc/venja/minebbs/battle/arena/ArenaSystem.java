package cc.venja.minebbs.battle.arena;

import cc.venja.minebbs.battle.BattleMain;
import cc.venja.minebbs.battle.calculation.GFG;
import cc.venja.minebbs.login.LoginMain;
import cc.venja.minebbs.login.enums.Team;
import cc.venja.minebbs.robot.RobotMain;
import io.papermc.paper.event.entity.EntityMoveEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ArenaSystem implements Listener {
    public static ArenaSystem instance;

    public File configFile;
    public YamlConfiguration configuration;
    public Map<Player, Location> playerLocationMap = new ConcurrentHashMap<>();

    public ArenaSystem() throws IOException {
        BattleMain.instance.getServer().getPluginManager().registerEvents(this, BattleMain.instance);

        configFile = new File(BattleMain.instance.getDataFolder().toPath().resolve("arena.yml").toString()).getAbsoluteFile();
        var configExists = configFile.exists();
        configuration = YamlConfiguration.loadConfiguration(configFile);
        if (!configExists) {
            configuration.set("TeamRED", new ArrayList<String>() {{
                //平原 NK 右上
                add("1334,230");
                add("1850,230");
                add("1850,1110");
                add("1294,1010");
                add("1082,812");
                add("1114,412");
            }});
            configuration.set("EnableTeamRED", true);

            configuration.set("TeamBLUE", new ArrayList<String>() {{
                //雪山 PM 左上
                add("230,230");
                add("1314,230");
                add("1094,402");
                add("1060,832");
                add("888,1022");
                add("230,910");
            }});
            configuration.set("EnableTeamBLUE", true);

            configuration.set("TeamGREY", new ArrayList<String>() {{
                //丛林 BDS 右下
                add("1460,1850");
                add("1850,1850");
                add("1850,1130");
                add("1294,1032");
                add("1280,1178");
                add("1182,1272");
                add("1072,1272");
            }});
            configuration.set("EnableTeamGREY", true);

            configuration.set("TeamYELLOW", new ArrayList<String>() {{
                //沙漠 Geyser 左下
                add("230,930");
                add("230,1850");
                add("1260,1850");
                add("1052,1272");
                add("1012,1272");
                add("900,1176");
                add("880,1052");
            }});
            configuration.set("EnableTeamYELLOW", true);

            configuration.set("EnablePortal", true);
            configuration.set("TeamREDPortal", new ArrayList<String>() {{
                //平原 NK 右上
                add("1848,330");
                add("1848,336");
            }});
            configuration.set("TeamREDPortalEnd", "1103,72,918");
            configuration.set("TeamBLUEPortal", new ArrayList<String>() {{
                //雪山 PM 左上
                add("374,328");
                add("374,334");
            }});
            configuration.set("TeamBLUEPortalEnd", "913,76,1108");
            configuration.set("TeamGREYPortal", new ArrayList<String>() {{
                //丛林 BDS 右下
                add("1730,1707");
                add("1730,1713");
            }});
            configuration.set("TeamGREYPortalEnd", "1293,89,1108");
            configuration.set("TeamYELLOWPortal", new ArrayList<String>() {{
                //沙漠 Geyser 左下
                add("294,1577");
                add("294,1581");
            }});
            configuration.set("TeamYELLOWPortalEnd", "1103,108,1298");

            configuration.set("Assembling", false);
            configuration.set("TeamREDAssemblePoint", "");
            configuration.set("TeamBLUEAssemblePoint", "");
            configuration.set("TeamGREYAssemblePoint", "");
            configuration.set("TeamYELLOWAssemblePoint", "");

            configuration.set("CenterPos", "1040,1040");
            configuration.set("CenterRadius", 400);
            configuration.set("CenterAccess", false);
            configuration.set("CenterEnable", false);
        }

        configuration.save(configFile);

        (new Timer()).schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    syncPlayerLocation();
                } catch (SQLException exception) {
                    BattleMain.instance.getLogger().info(exception.toString());
                }
            }
        }, 0, 1000);
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

                Vector center = strToVector(Objects.requireNonNull(configuration.getString("CenterPos")));
                if (!configuration.getBoolean("CenterAccess") && !configuration.getBoolean("CenterEnable")) {
                    if (distance(center, to) <= configuration.getDouble("CenterRadius")) {
                        event.setCancelled(true);
                        event.getEntity().setVelocity(new Vector(0, 0, 0));
                        event.getEntity().remove();
                        Audience.audience(player).sendActionBar(Component.text("§c非决斗日禁止进入中心区"));
                    }
                }

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

    @EventHandler
    public void onMove(PlayerMoveEvent event) throws SQLException {
        if (LoginMain.instance.onlinePlayers.get(event.getPlayer()) == LoginMain.Status.LOGIN) {
            Team team = RobotMain.getPlayerTeam(event.getPlayer().getName());
            String teamStr = Objects.requireNonNull(team).getName();
            List<String> vectorStr = configuration.getStringList(teamStr);
            List<Vector> vectors = new ArrayList<>();
            for (String str : vectorStr) {
                vectors.add(strToVector(str));
            }
            // Bukkit.getLogger().info(event.getTo().toString());
            Vector to = new Vector(event.getTo().getX(), event.getTo().getZ(), 0);

            Vector center = strToVector(Objects.requireNonNull(configuration.getString("CenterPos")));
            if (!configuration.getBoolean("CenterAccess") && !configuration.getBoolean("CenterEnable")) {
                if (distance(center, to) <= configuration.getDouble("CenterRadius")) {
                    if (!event.getPlayer().isOp()) {
                        Location location = playerLocationMap.get(event.getPlayer());
                        if (location != null) {
                            event.getPlayer().teleport(location.set(235.0, -60, 235.0));
                            // Bukkit.getLogger().info(location.toString());
                        }

                        event.getPlayer().setVelocity(getVelocity((event.getTo().getX() - event.getFrom().getX()), (event.getTo().getZ() - event.getFrom().getZ()), 0.4));
                        event.setCancelled(true);
                        Audience.audience(event.getPlayer()).sendActionBar(Component.text("§c非决斗日禁止进入中心区"));
                    }
                }
            }

            String enable = "Enable" + teamStr;
            if (configuration.getBoolean(enable)) {
                if (!GFG.isInside(vectors.toArray(Vector[]::new), vectors.size(), to)) {
                    if (!event.getPlayer().isOp()) {
                        event.setCancelled(true);
                        Location location = playerLocationMap.get(event.getPlayer());
                        if (location != null) {
                            event.getPlayer().teleport(location);
                            // Bukkit.getLogger().info(location.toString());
                        }
                        Audience.audience(event.getPlayer()).sendActionBar(Component.text("§c不可逾越允许活动范围"));
                    }
                }
            }
            isPlayerEnterPortal(teamStr, event.getPlayer());
        }
    }

    private Vector getVelocity(double x, double z, double speed) {
        double y = 0.3333;
        double multiplier = Math.sqrt((speed*speed) / (x*x + y*y + z*z));
        return new Vector(x, y, z).multiply(multiplier).setY(y);
    }

    public void isPlayerEnterPortal(String TeamStr, Player event) {
        Location loc = event.getLocation();
        List<String> scope = configuration.getStringList(TeamStr + "Portal");// 对应队伍传送门范围
        List<Vector> scopePoint = new ArrayList<>();
        for (String str : scope) {
            scopePoint.add(strToVector(str));
        }

        if (scopePoint.get(0).getX() == loc.getX() && Math.max(scopePoint.get(0).getY(), loc.getZ()) == Math.min(loc.getZ(), scopePoint.get(1).getY())) {
            //在区域内
            String end = configuration.getString(TeamStr + "PortalEnd");
            assert end != null;
            Vector endPoint = new Vector(Integer.parseInt(end.split(",")[0]), Integer.parseInt(end.split(",")[1]), Integer.parseInt(end.split(",")[2]));
            Location newLocation = new Location(event.getWorld(), endPoint.getX(), endPoint.getY(), endPoint.getZ());
            event.teleport(newLocation);
        }
    }

    public void syncPlayerLocation() throws SQLException {
        for (Player player : BattleMain.instance.getServer().getOnlinePlayers()) {
            Team team = RobotMain.getPlayerTeam(player.getName());
            String teamStr = Objects.requireNonNull(team).getName();
            String enable = "Enable" + teamStr;
            if (configuration.getBoolean(enable)) {
                List<String> vectorStr = configuration.getStringList(teamStr);
                List<Vector> vectors = new ArrayList<>();
                for (String str : vectorStr) {
                    vectors.add(strToVector(str));
                }

                Vector playerV2 = new Vector(player.getLocation().getX(), player.getLocation().getZ(), 0);

                boolean center = false;
                Vector centerPos = strToVector(Objects.requireNonNull(configuration.getString("CenterPos")));
                if (!configuration.getBoolean("CenterAccess") && !configuration.getBoolean("CenterEnable")) {
                    center = (distance(centerPos, playerV2) > configuration.getDouble("CenterRadius"));
                }

                boolean inside = GFG.isInside(vectors.toArray(Vector[]::new), vectors.size(), playerV2);

                if (playerLocationMap.get(player) != null) {
                    Audience.audience(player).sendActionBar(Component.text(playerLocationMap.get(player).toString()));
                }
                if (center && inside) {
                    playerLocationMap.put(player, player.getLocation());
                }
            }
        }
    }

    private Vector strToVector(String str) {
        return new Vector(Integer.parseInt(str.split(",")[0]), Integer.parseInt(str.split(",")[1]), 0);
    }

    private double distance(Vector v1, Vector v2) {
        return Math.sqrt(NumberConversions.square(v1.getX() - v2.getX()) + NumberConversions.square(v1.getY() - v2.getY()));
    }

}
