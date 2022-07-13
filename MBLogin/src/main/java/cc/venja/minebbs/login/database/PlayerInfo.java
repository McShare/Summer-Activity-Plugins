package cc.venja.minebbs.login.database;

import lombok.Getter;
import lombok.Setter;

public class PlayerInfo {

    @Getter
    @Setter
    private String playerName;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    private int team;
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
            String lastLoginIp,
            int lastGameMode,
            boolean enableAutoLogin
    ) {
        this.playerName = playerName;
        this.password = password;
        this.team = team;
        this.lastLoginIp = lastLoginIp;
        this.lastGameMode = lastGameMode;
        this.enableAutoLogin = enableAutoLogin;
    }
}
