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

    public void onPlayerDeath() {
        try {
            score.deduct(5);
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.toString());
        }
    }

    public void onKillPlayer() {
        try {
            score.add(10);
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.toString());
        }
    }
}
