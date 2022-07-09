package cc.venja.minebbs.battle;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class BattleMain extends JavaPlugin implements Listener {
    public static BattleMain instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    public File teamFile;
    public YamlConfiguration team;

    @Override
    public void onEnable() {
        try {
            teamFile = new File(this.getDataFolder().toPath().resolve("team.yml").toString()).getAbsoluteFile();
            team = YamlConfiguration.loadConfiguration(teamFile);
            team.save(teamFile);

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
            if (getPlayerTeam(damager.getName()) == getPlayerTeam(damagee.getName())) {
                event.setCancelled(true);
            }
        }
    }

    public static int getPlayerTeam(String player) {
        return instance.team.getInt(player.toLowerCase());
    }
}
