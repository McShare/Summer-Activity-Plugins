package cc.venja.minebbs.battle.scores;

import cc.venja.minebbs.battle.BattleMain;
import org.bukkit.entity.Player;


public class PersonalScore {
    Player player;
    String playerName;

    public Player getPlayer() {
        return player;
    }

    public PersonalScore(Player player) {
        this.player = player;
        this.playerName = player.getName().toLowerCase();

        try {
            if (!BattleMain.personalScore.contains(playerName))
                init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int get() {
        return BattleMain.personalScore.getInt(playerName);
    }

    public void init() throws Exception {
        BattleMain.personalScore.set(playerName, 0);
        saveScoreToFile();
    }

    public void set(int score) throws Exception {
        BattleMain.personalScore.set(playerName, score);
        saveScoreToFile();
    }

    public void add(int score) throws Exception {
        this.set(BattleMain.personalScore.getInt(playerName)+score);
        showScores show = new showScores();
        show.UpdateScoreboard();
    }

    public void deduct(int score) throws Exception {
        this.set(BattleMain.personalScore.getInt(playerName)-score);
        showScores show = new showScores();
        show.UpdateScoreboard();
    }

    private void saveScoreToFile() throws Exception {
        BattleMain.personalScore.save(BattleMain.personalScoreFile);
    }
}
