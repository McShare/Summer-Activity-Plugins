package cc.venja.minebbs.battle.scores;

import cc.venja.minebbs.login.enums.Team;

public class TeamScoreHandle {
    public TeamScore getScore() {
        return score;
    }

    TeamScore score;

    public TeamScoreHandle(Team team) {
        this.score = new TeamScore(team);
    }

    public void addScoreByMidPointOccupy() throws Exception {
        this.score.add(1, "Mid Point Occupy");
    }

    public void addScoreByOccupyStrongHold() throws Exception {
        this.score.add(50, "Occupy StrongHold");
    }

    public void deductScoreByStrongHoldOccupied() throws Exception {
        this.score.deduct(40, "StrongHold Occupied");
    }
}
