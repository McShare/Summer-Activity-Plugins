package cc.venja.minebbs.login.database;

public class UserInfo {
    private String username;
    private String password;
    private String lastLoginIp;
    private int lastGameMode;
    private boolean enableAutoLogin;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    public int getLastGameMode() {
        return lastGameMode;
    }

    public void setLastGameMode(int lastGameMode) {
        this.lastGameMode = lastGameMode;
    }

    public boolean isEnableAutoLogin() {
        return enableAutoLogin;
    }

    public void setEnableAutoLogin(boolean enableAutoLogin) {
        this.enableAutoLogin = enableAutoLogin;
    }
}
