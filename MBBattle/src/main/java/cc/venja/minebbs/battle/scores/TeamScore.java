package cc.venja.minebbs.battle.scores;

import cc.venja.minebbs.battle.BattleMain;
import cc.venja.minebbs.login.enums.Team;

import java.io.BufferedWriter;
import java.io.FileWriter;

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

        ShowScore show = new ShowScore();
        show.UpdateScoreboard();

        BattleMain.instance.teamCsvWriter = new BufferedWriter(new FileWriter(BattleMain.instance.teamCsvFile));
        BattleMain.instance.teamCsvWriter.newLine();
        BattleMain.instance.teamCsvWriter.write(String.format("%s,%s,%s", team.getName(), score, reason));
        BattleMain.instance.teamCsvWriter.flush();
        BattleMain.instance.teamCsvWriter.close();
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
