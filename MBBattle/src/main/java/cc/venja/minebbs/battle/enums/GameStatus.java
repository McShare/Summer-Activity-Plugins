package cc.venja.minebbs.battle.enums;

public enum GameStatus {
    PEACETIME(0, "和平期"),
    BATTLE_DAY(1, "战斗日"),
    OFF_DEF_DAY(2, "要塞攻防日"),
    FINAL_BATTLE_DAY(3, "最终战斗日");

    final int value;
    final String name;

    GameStatus(int value, String name) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }
}
