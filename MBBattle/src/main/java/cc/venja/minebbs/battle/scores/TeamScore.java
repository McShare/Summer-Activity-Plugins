package cc.venja.minebbs.battle.scores;

import cc.venja.minebbs.battle.BattleMain;
import cc.venja.minebbs.login.enums.Team;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TeamScore {
    public Team getTeam() {
        return team;
    }

    Team team;

    public TeamScore(Team team) {
        this.team = team;
        try {
            if (!BattleMain.teamScore.contains(team.getName()))
                init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() throws Exception {
        BattleMain.teamScore.set(team.getName(), 0);
        saveScoreToFile();
    }

    public int get() {
        return BattleMain.teamScore.getInt(team.getName());
    }

    public void set(int score, String reason) throws Exception {
        BattleMain.teamScore.set(team.getName(), score);
        saveScoreToFile();

        //记录团队从何处获得积分
        if (BattleMain.instance.teamCsvFile.length()==0){
            BattleMain.instance.teamCsvWriter.write("队名,分数,原因,时间\n");
            BattleMain.instance.teamCsvWriter.flush();
        }
        BattleMain.instance.teamCsvWriter.write(team.getName()+","+ score+","+ reason+"," +LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+"\n");
        BattleMain.instance.teamCsvWriter.flush();
        ShowScore show = new ShowScore();

        show.UpdateScoreboard();
    }

    public void add(int score, String reason) throws Exception {
        this.set(BattleMain.teamScore.getInt(team.getName())+score, reason);
    }

    public void deduct(int score, String reason) throws Exception {
        this.set(BattleMain.teamScore.getInt(team.getName())-score, reason);
    }

    private void saveScoreToFile() throws Exception {
        BattleMain.teamScore.save(BattleMain.teamScoreFile);
    }
}
