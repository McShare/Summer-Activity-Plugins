package cc.venja.minebbs.battle;

import cc.venja.minebbs.battle.enums.Team;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cc.venja.minebbs.robot.RobotMain;

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
                        put("RespawnPosition", new int[] {0, 0, 0});
                    }
                };

                configuration.set("TeamRED", teamConfig);
                configuration.set("TeamBLUE", teamConfig);
                configuration.set("TeamGRAY", teamConfig);
                configuration.set("TeamYELLOW", teamConfig);

                configuration.set("RespawnCooldown", 20);
            }

            configuration.save(configFile);

            this.getServer().getPluginManager().registerEvents(this, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagee = event.getEntity();
        if (damager.getType().equals(EntityType.PLAYER) && damagee.getType().equals(EntityType.PLAYER)) {
            if (RobotMain.getPlayerTeam(damager.getName()) == RobotMain.getPlayerTeam(damagee.getName())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        Location location = event.getRespawnedLocation();

        var teamValue = RobotMain.getPlayerTeam(event.getPlayer().getName());
        String team = "Team" + Team.getByValue(teamValue);

        List<Integer> respawnPosition = Objects.requireNonNull(configuration.getConfigurationSection(team)).
                getIntegerList("RespawnPosition");
        World world = location.getWorld();
        int x = respawnPosition.get(0);
        int y = respawnPosition.get(1);
        int z = respawnPosition.get(2);

        Location respawnLocation = new Location(world, x, y, z);

        player.teleport(respawnLocation);
        player.setBedSpawnLocation(respawnLocation, true);
        player.setGameMode(GameMode.SPECTATOR);
        new Thread(() -> {
            try {
                Thread.sleep(configuration.getInt("RespawnCooldown"));
                player.teleport(respawnLocation);
                player.setGameMode(GameMode.SURVIVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
