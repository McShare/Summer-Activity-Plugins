package cc.venja.minebbs.battle.scores;

import cc.venja.minebbs.battle.BattleMain;
import cc.venja.minebbs.login.enums.Team;

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

    public void set(int score) throws Exception {
        BattleMain.teamScore.set(team.getName(), score);
        saveScoreToFile();
    }

    public void add(int score) throws Exception {
        this.set(BattleMain.teamScore.getInt(team.getName())+score);
    }

    public void deduct(int score) throws Exception {
        this.set(BattleMain.teamScore.getInt(team.getName())-score);
    }

    private void saveScoreToFile() throws Exception {
        BattleMain.teamScore.save(BattleMain.teamScoreFile);
    }
}
