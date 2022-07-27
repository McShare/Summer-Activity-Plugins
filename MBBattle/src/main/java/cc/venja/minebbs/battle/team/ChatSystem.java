package cc.venja.minebbs.battle.team;

import cc.venja.minebbs.login.LoginMain;
import cc.venja.minebbs.login.enums.Team;
import cc.venja.minebbs.robot.RobotMain;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.sql.SQLException;


public class ChatSystem implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent e) throws SQLException {
        Player p = e.getPlayer();
        String msg = e.getMessage();
        Team team = RobotMain.getPlayerTeam(p.getName());
        e.setCancelled(true);
        if (e.getMessage().startsWith("!")) {
            Bukkit.broadcast(Component.text( String.format("§l§7[§c公共§7]%s %s §7> §r%s", Team.getColorCode(team), p.getDisplayName(),  msg.substring(1))));
        } else {
            sendMessageToTeam(p, msg, team);
        }
    }


    public static void sendMessageToTeam(Player p, String msg, Team team) throws SQLException {
        String pMsg = String.format("§7[§e队伍§7]%s %s §7> §r%s", Team.getColorCode(team), p.getDisplayName(),  msg);
        Bukkit.getConsoleSender().sendMessage(Component.text(pMsg));
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (RobotMain.getPlayerTeam(player.getName()) == team || player.isOp()) {
                player.sendMessage(Component.text(pMsg));
            }
        }
    }
}
