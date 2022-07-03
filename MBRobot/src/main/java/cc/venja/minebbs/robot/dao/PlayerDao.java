package cc.venja.minebbs.robot.dao;

public class PlayerDao {

    public String playerName = null;
    public String KHL = null;

    public boolean isValid() {
        return (playerName != null) && (KHL != null);
    }

    @Override
    public String toString() {
        return "PlayerDao{" +
                "playerName='" + playerName + '\'' +
                ", KHL='" + KHL + '\'' +
                '}';
    }
}
