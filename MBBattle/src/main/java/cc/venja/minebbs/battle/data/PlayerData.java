package cc.venja.minebbs.battle.data;

import cc.venja.minebbs.battle.enums.Role;
import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Field;

public class PlayerData {

    public Integer point = 0;
    public Integer role = Role.Player.getValue();

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
            if (value == null) {
                PlayerData self = new PlayerData();
                Class<?> selfClazz = self.getClass();
                value = selfClazz.getField(name).get(self);
            }
            field.set(data, value);
        }

        return data;
    }

    @Override
    public String toString() {
        return "PlayerData{" +
                "point=" + point +
                ", role=" + role +
                '}';
    }
}
