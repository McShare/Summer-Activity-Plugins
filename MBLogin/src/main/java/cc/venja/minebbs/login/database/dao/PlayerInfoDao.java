package cc.venja.minebbs.login.database.dao;

import cc.venja.minebbs.login.LoginMain;
import cc.venja.minebbs.login.database.PlayerInfo;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerInfoDao {

    public PlayerInfoDao() {
    }

    public void addPlayer(PlayerInfo playerInfo) throws SQLException {
        String sql = "insert into player_info(player_name, password, last_login_ip, last_game_mode, enable_auto_login, team, account_KHL)" +
                "values(?,?,?,?,?,?,?)";

        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);

        statement.setString(1, playerInfo.getPlayerName());
        statement.setString(2, playerInfo.getPassword());
        statement.setString(3, playerInfo.getLastLoginIp());
        statement.setInt(4, playerInfo.getLastGameMode());
        statement.setBoolean(5, playerInfo.isEnableAutoLogin());
        statement.setInt(6, playerInfo.getTeam());
        statement.setString(7, playerInfo.getKhl());

        LoginMain.instance.getLogger().info(statement.toString());
        statement.execute();
    }

    public void updatePlayer(PlayerInfo playerInfo) throws SQLException {
        String sql = "update player_info set password=?, last_login_ip=?, last_game_mode=?, enable_auto_login=?, team=?, account_KHL=? where player_name=?";

        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);

        statement.setString(1, playerInfo.getPassword());
        statement.setString(2, playerInfo.getLastLoginIp());
        statement.setInt(3, playerInfo.getLastGameMode());
        statement.setBoolean(4, playerInfo.isEnableAutoLogin());
        statement.setInt(5, playerInfo.getTeam());
        statement.setString(6, playerInfo.getKhl());
        statement.setString(7, playerInfo.getPlayerName());

        LoginMain.instance.getLogger().info(statement.toString());
        statement.execute();
    }

    public List<PlayerInfo> query() throws SQLException {
        Statement statement = LoginMain.instance.databaseConnection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from player_info");

        return buildPlayers(resultSet);
    }

    public List<PlayerInfo> queryPlayersByTeam(int team) throws SQLException {
        String sql = "select * from player_info where team=?";
        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);
        statement.setInt(1, team);

        ResultSet resultSet = statement.executeQuery();

        return buildPlayers(resultSet);
    }

    @NotNull
    private List<PlayerInfo> buildPlayers(ResultSet resultSet) throws SQLException {
        List<PlayerInfo> players = new ArrayList<>();
        while (resultSet.next()) {
            players.add(new PlayerInfo(
                    resultSet.getString("player_name"),
                    resultSet.getString("password"),
                    resultSet.getInt("team"),
                    resultSet.getString("account_KHL"),
                    resultSet.getString("last_login_ip"),
                    resultSet.getInt("last_game_mode"),
                    resultSet.getBoolean("enable_auto_login")
            ));
        }
        return players;
    }

    public PlayerInfo getPlayerByName(String playerName) throws SQLException {
        playerName = playerName.toLowerCase();
        String sql = "select * from player_info where player_name=?";
        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);
        statement.setString(1, playerName);

        ResultSet resultSet = statement.executeQuery();
        PlayerInfo player = null;

        if (resultSet.next()) {
            player = new PlayerInfo(
                    resultSet.getString("player_name"),
                    resultSet.getString("password"),
                    resultSet.getInt("team"),
                    resultSet.getString("account_KHL"),
                    resultSet.getString("last_login_ip"),
                    resultSet.getInt("last_game_mode"),
                    resultSet.getBoolean("enable_auto_login")
            );
        }

        return player;
    }

    public void delPlayerByName(String playerName) throws SQLException {
        playerName = playerName.toLowerCase();
        String sql = "del from player_info where player_name=?";

        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);

        statement.setString(1, playerName);

        statement.execute();
    }
}
