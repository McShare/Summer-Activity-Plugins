package cc.venja.minebbs.robot.dao;

public class TeamDao {

    public String playerName = null;
    public String KHL = null;
    public Integer team = -1;

    public TeamDao(){ }

    public TeamDao(String playerName) {
        this.playerName = playerName;
    }

    public boolean isValid() {
        return (playerName != null) && (KHL != null) && (team != -1);
    }

    @Override
    public String toString() {
        return "TeamDao{" +
                "playerName='" + playerName + '\'' +
                ", KHL='" + KHL + '\'' +
                ", team='" + team + '\'' +
                '}';
    }
}
