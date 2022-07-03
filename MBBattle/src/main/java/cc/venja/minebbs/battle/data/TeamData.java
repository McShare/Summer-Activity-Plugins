package cc.venja.minebbs.battle.data;

import org.bukkit.configuration.file.YamlConfiguration;

import java.lang.reflect.Field;

public class TeamData {

    public Integer point = 0;

    public YamlConfiguration reflectToConfigSection(YamlConfiguration yaml) throws Exception {

        var dataClazz = this.getClass();

        for (Field field : dataClazz.getDeclaredFields()) {
            yaml.set(field.getName(), field.get(this));
        }

        return yaml;
    }

    public TeamData applyConfigSection(YamlConfiguration section) throws Exception {
        var data = new TeamData();
        var dataClazz = data.getClass();

        for (Field field : dataClazz.getDeclaredFields()) {
            var name = field.getName();
            var value = section.get(name);
            if (value == null) {
                var self = new TeamData();
                var selfClazz = self.getClass();
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
