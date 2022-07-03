package cc.venja.minebbs.battle.data;

import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Field;

public class TeamData {

    public Integer point = 0;

    public YamlConfiguration reflectToConfigSection(YamlConfiguration yaml) throws Exception {

        Class<?> dataClazz = this.getClass();

        for (Field field : dataClazz.getDeclaredFields()) {
            yaml.set(field.getName(), field.get(this));
        }

        return yaml;
    }

    public TeamData applyConfigSection(YamlConfiguration section) throws Exception {
        TeamData data = new TeamData();
        Class<?> dataClazz = data.getClass();

        for (Field field : dataClazz.getDeclaredFields()) {
            String name = field.getName();
            Object value = section.get(name);
            if (value == null) {
                TeamData self = new TeamData();
                Class<?> selfClazz = self.getClass();
                value = selfClazz.getField(name).get(self);
            }
            field.set(data, value);
        }

        return data;
    }

    @Override
    public String toString() {
        return "TeamData{" +
                "point=" + point +
                '}';
    }
}
