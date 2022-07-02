package cc.venja.minebbs.login.data;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Field;

public class PlayerData {

    public String password = "";
    public String lastLoginIp = "";
    public Integer lastGameMode = GameMode.SURVIVAL.getValue();

    public YamlConfiguration reflectToConfigSection(YamlConfiguration yaml) throws Exception {

        Class<?> dataClazz = this.getClass();

        for (Field field : dataClazz.getDeclaredFields()) {
            yaml.set(field.getName(), field.get(this));
        }

        return yaml;
    }

    public PlayerData applyConfigSection(YamlConfiguration section) throws Exception {
        PlayerData data = new PlayerData();
        Class<?> dataClazz = data.getClass();

        for (Field field : dataClazz.getDeclaredFields()) {
            String name = field.getName();
            Object value = section.get(name);
            field.set(data, value);
        }

        Bukkit.getLogger().info(data.toString());
        return data;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "password='" + password + '\'' +
                ", lastLoginIp='" + lastLoginIp + '\'' +
                ", lastGameMode=" + lastGameMode +
                '}';
    }
}
