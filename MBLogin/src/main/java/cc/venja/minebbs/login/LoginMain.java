package cc.venja.minebbs.login;

import cc.venja.minebbs.login.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginMain extends JavaPlugin implements Listener{

    public static LoginMain instance;

    public Map<Player, Status> onlinePlayers = new ConcurrentHashMap<>();

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
    }


    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName().toLowerCase();
        File file = new File(this.getDataFolder().toPath().resolve("data").resolve(playerName + ".yml").toString());
        if (!file.exists()) {
            onlinePlayers.put(event.getPlayer(), Status.NOT_REGISTER);
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            event.getPlayer().sendMessage("§6>>> 请输入/register <密码> <确认密码>, 完成注册");
        } else {
            onlinePlayers.put(event.getPlayer(), Status.NOT_LOGIN);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        onlinePlayers.remove(event.getPlayer());
    }

    public enum Status {
        NOT_REGISTER,
        REGISTER,
        NOT_LOGIN,
        LOGIN,
    }

    public static void main(String[] args) {
    }
}
