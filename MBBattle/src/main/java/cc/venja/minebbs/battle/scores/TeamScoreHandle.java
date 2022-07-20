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
}
