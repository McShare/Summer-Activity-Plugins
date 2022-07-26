package cc.venja.minebbs.battle.scores;

import cc.venja.minebbs.login.enums.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerScoreHandle {
    public PersonalScore getScore() {
        return score;
    }

    PersonalScore score;

    public PlayerScoreHandle(Player player) {
        this.score = new PersonalScore(player);
    }

    public void addScoreByKillOtherPlayer() throws Exception {
        this.score.add(10, "Kill Player");
    }

    public void deductScoreByDeath() throws Exception {
        this.score.deduct(5, "Player Death");
    }
}
