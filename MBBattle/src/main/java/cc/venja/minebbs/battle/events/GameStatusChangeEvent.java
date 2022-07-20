package cc.venja.minebbs.battle.events;

import cc.venja.minebbs.battle.enums.GameStatus;

public class GameStatusChangeEvent {
    public GameStatusChangeEvent(GameStatus beforeStatus, GameStatus nowStatus) {
        this.beforeStatus = beforeStatus;
        this.nowStatus = nowStatus;
    }

    public GameStatus beforeStatus;
    public GameStatus nowStatus;
}
