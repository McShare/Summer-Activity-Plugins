package cc.venja.minebbs.battle.scores;

import cc.venja.minebbs.battle.BattleMain;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.FileWriter;


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

    public void set(int score, String reason) throws Exception {
        BattleMain.personalScore.set(playerName, score);
        saveScoreToFile();

        ShowScore show = new ShowScore();
        show.UpdateScoreboard();

        BattleMain.instance.personalCsvWriter = new BufferedWriter(new FileWriter(BattleMain.instance.personalCsvFile));
        BattleMain.instance.personalCsvWriter.newLine();
        BattleMain.instance.personalCsvWriter.write(String.format("%s,%s,%s", playerName, score, reason));
        BattleMain.instance.personalCsvWriter.flush();
        BattleMain.instance.personalCsvWriter.close();
    }

    public void add(int score, String reason) throws Exception {
        this.set(BattleMain.personalScore.getInt(playerName)+score, reason);
    }

    public void deduct(int score, String reason) throws Exception {
        this.set(BattleMain.personalScore.getInt(playerName)-score, reason);
    }

    private void saveScoreToFile() throws Exception {
        BattleMain.personalScore.save(BattleMain.personalScoreFile);
    }
}
