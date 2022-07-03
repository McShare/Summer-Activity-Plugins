package cc.venja.minebbs.login.data;

import org.bukkit.GameMode;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Field;

public class PlayerData {

    public String password = "";
    public String lastLoginIp = "";
    public Integer lastGameMode = GameMode.SURVIVAL.getValue();

    public YamlConfiguration reflectToConfigSection(YamlConfiguration yaml) throws Exception {
        var dataClazz = this.getClass();

        for (Field field : dataClazz.getDeclaredFields()) {
            yaml.set(field.getName(), field.get(this));
        }

        return yaml;
    }

    public PlayerData applyConfigSection(YamlConfiguration section) throws Exception {
        var data = new PlayerData();
        var dataClazz = data.getClass();

        for (Field field : dataClazz.getDeclaredFields()) {
            var name = field.getName();
            var value = section.get(name);
            if (value == null) {
                var self = new PlayerData();
                var selfClazz = self.getClass();
                value = selfClazz.getField(name).get(self);
            }
            field.set(data, value);
        }

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
