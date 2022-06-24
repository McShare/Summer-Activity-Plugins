package cc.venja.minebbs.login.data;

import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Field;

public class PlayerData {

    public String password;
    public String lastLoginIp;

    public YamlConfiguration reflectToConfigSection() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();

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
            String value = section.getString(name, "");
            field.set(data, value);
            break;
        }

        return data;
    }

}
