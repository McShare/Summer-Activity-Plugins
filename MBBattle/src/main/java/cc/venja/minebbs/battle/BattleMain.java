package cc.venja.minebbs.battle;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

import cc.venja.minebbs.robot.RobotMain;

public class BattleMain extends JavaPlugin implements Listener {
    public static BattleMain instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
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
}
