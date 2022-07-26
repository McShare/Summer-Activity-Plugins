package cc.venja.minebbs.battle.scores;

import cc.venja.minebbs.battle.BattleMain;
import org.bukkit.entity.Player;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


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
    }

    public void add(int score, String reason) throws Exception {
        this.set(BattleMain.personalScore.getInt(playerName)+score, reason);
        //记录玩家从何获得加分
        if(BattleMain.instance.personalCsvFile.length() == 0){
            BattleMain.instance.personalCsvWriter.write("玩家名,分数,原因,时间\n");
        }
        BattleMain.instance.personalCsvWriter.write(playerName+","+score+","+reason+","+LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+"\n");
        BattleMain.instance.personalCsvWriter.flush();
    }

    public void deduct(int score, String reason) throws Exception {
        this.set(BattleMain.personalScore.getInt(playerName)-score, reason);
    }

    private void saveScoreToFile() throws Exception {
        BattleMain.personalScore.save(BattleMain.personalScoreFile);
    }
}
