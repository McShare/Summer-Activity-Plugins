package cc.venja.minebbs.login.enums;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public enum Team {
    RED(0, "TeamRED"),
    BLUE(1, "TeamBLUE"),
    GREY(2, "TeamGREY"),
    YELLOW(3, "TeamYELLOW"),
    IMPOSTER(4, "TeamIMPOSTER"),
    ADMIN(5, "TeamADMIN");

    final int value;
    final String name;
    static final Map<Integer, Team> BY_ID = new HashMap<>();
    static final Map<String, Team> BY_NAME = new HashMap<>();

    Team(int value, String name) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return this.value;
    }
    public String getName() {
        return this.name;
    }

    public static Team getByValue(int value) {
        return BY_ID.get(value);
    }
    public static Team getByName(String value) {
        return BY_NAME.get(value);
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
        for (Team team : values()) {
            BY_ID.put(team.getValue(), team);
            BY_NAME.put(team.getName(), team);
        }
    }
}
