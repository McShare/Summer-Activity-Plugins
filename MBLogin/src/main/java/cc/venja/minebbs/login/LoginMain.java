package cc.venja.minebbs.login;

import org.bukkit.plugin.java.JavaPlugin;

public class LoginMain extends JavaPlugin {

    public static LoginMain instance;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {

    }
}
