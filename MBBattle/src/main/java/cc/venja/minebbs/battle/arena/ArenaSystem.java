package cc.venja.minebbs.battle.arena;

import cc.venja.minebbs.battle.BattleMain;
import cc.venja.minebbs.battle.calculation.GFG;
import cc.venja.minebbs.login.enums.Team;
import cc.venja.minebbs.robot.RobotMain;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArenaSystem implements Listener {

    public File configFile;
    public YamlConfiguration configuration;

    public ArenaSystem() throws IOException {
        BattleMain.instance.getServer().getPluginManager().registerEvents(this, BattleMain.instance);

        configFile = new File(BattleMain.instance.getDataFolder().toPath().resolve("arena.yml").toString()).getAbsoluteFile();
        var configExists = configFile.exists();
        configuration = YamlConfiguration.loadConfiguration(configFile);
        if (!configExists) {
            configuration.set("TeamRED", new ArrayList<String>() {{
                add("230,230");
                add("1314,230");
                add("1094,402");
                add("1060,832");
                add("888,1022");
                add("230,910");
            }});
            configuration.set("TeamBLUE", new ArrayList<String>() {{
                add("1334,230");
                add("1850,230");
                add("1850,1110");
                add("1294,1010");
                add("1082,812");
                add("1114,412");
            }});
            configuration.set("TeamGRAY", new ArrayList<String>() {{
                add("230,930");
                add("230,1850");
                add("1260,1850");
                add("1052,1272");
                add("1012,1272");
                add("900,1176");
                add("880,1052");
            }});
            configuration.set("TeamYELLOW", new ArrayList<String>() {{
                add("1460,1850");
                add("1850,1850");
                add("1850,1130");
                add("1294,1032");
                add("1280,1178");
                add("1182,1272");
                add("1072,1272");
            }});
            configuration.set("Center", 400);
        }

        configuration.save(configFile);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) throws SQLException {
        Team team = RobotMain.getPlayerTeam(event.getPlayer().getName());
        String teamStr = "Team" + Objects.requireNonNull(team);
        List<String> vectorStr = configuration.getStringList(teamStr);
        List<Vector> vectors = new ArrayList<>();
        for (String str : vectorStr) {
            vectors.add(strToVector(str));
        }
        Vector to = new Vector(event.getTo().getX(), event.getTo().getZ(), 0);
        if (!GFG.isInside(vectors.toArray(Vector[]::new), vectors.size(), to)) {
            if (!event.getPlayer().isOp()) {
                event.setCancelled(true);
                Audience.audience(event.getPlayer()).sendActionBar(Component.text("§c不可逾越允许活动范围"));
            }
        }
    }

    private Vector strToVector(String str) {
        return new Vector(Integer.parseInt(str.split(",")[0]), Integer.parseInt(str.split(",")[1]), 0);
    }

}
