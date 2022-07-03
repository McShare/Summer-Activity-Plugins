package cc.venja.minebbs.battle.enums;

import java.util.HashMap;
import java.util.Map;

public enum Role {
    Player(0),
    Leader(1),
    Imposter(2);

    final int value;
    static final Map<Integer, Role> BY_ID = new HashMap<>();

    Role(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static Role getByValue(int value) {
        return BY_ID.get(value);
    }

    static {
        for (Role role : values()) {
            BY_ID.put(role.getValue(), role);
        }
    }
}