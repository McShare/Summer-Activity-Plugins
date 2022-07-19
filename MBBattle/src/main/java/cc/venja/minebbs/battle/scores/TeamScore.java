package cc.venja.minebbs.battle.scores;

import cc.venja.minebbs.battle.BattleMain;
import cc.venja.minebbs.login.enums.Team;

public class TeamScore {
    Team team;

    public TeamScore(Team team) {
        this.team = team;
        try {
            if (!BattleMain.teamScore.contains(team.getName()))
                initScore();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initScore() throws Exception {
        BattleMain.teamScore.set(team.getName(), 0);
        saveScoreToFile();
    }

    public int getScore() {
        return BattleMain.teamScore.getInt(team.getName());
    }

    public void setScore(int score) throws Exception {
        BattleMain.teamScore.set(team.getName(), score);
        saveScoreToFile();
    }

    public void addScore(int score) throws Exception {
        this.setScore(BattleMain.teamScore.getInt(team.getName())+score);
    }

    public void deductScore(int score) throws Exception {
        this.setScore(BattleMain.teamScore.getInt(team.getName())-score);
    }

    private void saveScoreToFile() throws Exception {
        BattleMain.teamScore.save(BattleMain.teamScoreFile);
    }
}
