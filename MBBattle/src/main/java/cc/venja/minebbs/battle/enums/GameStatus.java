package cc.venja.minebbs.battle.enums;

public enum GameStatus {
    PEACETIME("和平期"),
    BATTLE_DAY("战斗日"),
    OFF_DEF_DAY("要塞攻防日"),
    FINAL_BATTLE_DAY("最终战斗日");

    final String name;

    GameStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
