package cc.venja.minebbs.battle.enums;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public enum Team {
    RED(0),
    BLUE(1),
    GREY(2),
    YELLOW(3),
    IMPOSTER(4),
    ADMIN(5);

    final int value;
    static final Map<Integer, Team> BY_ID = new HashMap<>();

    Team(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static Team getByValue(int value) {
        return BY_ID.get(value);
    }

    public static String getColorCode(@NotNull Team team) {
        switch (team) {
            case RED -> {
                return "§c";
            }
            case BLUE -> {
                return "§b";
            }
            case GREY -> {
                return "§2";
            }
            case YELLOW -> {
                return "§e";
            }
        }
        return "";
    }

    static {
        for (Team role : values()) {
            BY_ID.put(role.getValue(), role);
        }
    }
}
