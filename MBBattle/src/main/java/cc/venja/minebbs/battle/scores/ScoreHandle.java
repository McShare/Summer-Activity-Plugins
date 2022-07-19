package cc.venja.minebbs.battle.scores;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScoreHandle {
    public static void onPlayerDeath(Player player) {
        try {
            PersonalScore score = new PersonalScore(player);
            score.deduct(5);
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.toString());
        }
    }

    public static void onKillPlayer(Player player) {
        try {
            PersonalScore score = new PersonalScore(player);
            score.add(10);
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.toString());
        }
    }
}
