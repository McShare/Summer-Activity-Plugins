package cc.venja.minebbs.login.database;

import lombok.Getter;
import lombok.Setter;

public class PlayerInfo {

    private String playerName;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private int team;
    @Getter
    @Setter
    private String khl;
    @Getter
    @Setter
    private String lastLoginIp;
    @Getter
    @Setter
    private int lastGameMode;
    @Getter
    @Setter
    private boolean enableAutoLogin;

    public PlayerInfo() {
    }

    public PlayerInfo(
            String playerName,
            String password,
            int team,
            String khl,
            String lastLoginIp,
            int lastGameMode,
            boolean enableAutoLogin
    ) {
        this.playerName = playerName;
        this.password = password;
        this.team = team;
        this.khl = khl;
        this.lastLoginIp = lastLoginIp;
        this.lastGameMode = lastGameMode;
        this.enableAutoLogin = enableAutoLogin;
    }

    public String getPlayerName() {
        return playerName.toLowerCase();
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName.toLowerCase();
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "playerName='" + playerName + '\'' +
                ", password='" + password + '\'' +
                ", team=" + team +
                ", khl='" + khl + '\'' +
                ", lastLoginIp='" + lastLoginIp + '\'' +
                ", lastGameMode=" + lastGameMode +
                ", enableAutoLogin=" + enableAutoLogin +
                '}';
    }
}
