package cc.venja.minebbs.login.database.dao;

import cc.venja.minebbs.login.LoginMain;
import cc.venja.minebbs.login.database.PlayerInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlayerInfoDao {

    public PlayerInfo player = null;

    public PlayerInfoDao() {}

    public void addPlayer(PlayerInfo playerInfo) throws SQLException {
        String sql = "insert into player_info(player_name, password, last_login_ip, last_game_mode, enable_auto_login)"+
                "values(?,?,?,?,?)";

        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);

        statement.setString(1, playerInfo.getUsername());
        statement.setString(2, playerInfo.getPassword());
        statement.setString(3, playerInfo.getLastLoginIp());
        statement.setInt(4, playerInfo.getLastGameMode());
        statement.setBoolean(5, playerInfo.isEnableAutoLogin());

        statement.execute();
    }

    public void updatePlayer(PlayerInfo playerInfo) throws SQLException {
        String sql = "update player_info set player_name=?, password=?, last_login_ip=?, last_game_mode=?, enable_auto_login=?";

        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);

        statement.setString(1, playerInfo.getUsername());
        statement.setString(2, playerInfo.getPassword());
        statement.setString(3, playerInfo.getLastLoginIp());
        statement.setInt(4, playerInfo.getLastGameMode());
        statement.setBoolean(5, playerInfo.isEnableAutoLogin());

        statement.execute();
    }

    public void delPlayerByName(PlayerInfo playerInfo) throws SQLException {
        String sql = "del from player_info where player_name=?";

        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);

        statement.setString(1, playerInfo.getUsername());

        statement.execute();
    }

    public List<PlayerInfo> query() throws SQLException {
        Statement statement = LoginMain.instance.databaseConnection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from player_info");

        List<PlayerInfo> players = new ArrayList<>();
        PlayerInfo player = null;
        while (resultSet.next()) {
            player = new PlayerInfo();
            player.setPlayerName(resultSet.getString("player_name"));
            player.setPassword(resultSet.getString("password"));
            player.setLastLoginIp(resultSet.getString("last_login_ip"));
            player.setLastGameMode(resultSet.getInt("last_game_mode"));
            player.setEnableAutoLogin(resultSet.getBoolean("enable_auto_login"));

            players.add(player);
        }

        return players;
    }

    public PlayerInfo queryByPlayerName(String player_name) throws SQLException {
        String sql = "select * from player_info where player_name=?";
        PreparedStatement statement = LoginMain.instance.databaseConnection.prepareStatement(sql);
        statement.setString(1, player_name);

        ResultSet resultSet = statement.executeQuery();
        PlayerInfo player = null;
        while (resultSet.next()) {
            player = new PlayerInfo();
            player.setPlayerName(resultSet.getString("player_name"));
            player.setPassword(resultSet.getString("password"));
            player.setLastLoginIp(resultSet.getString("last_login_ip"));
            player.setLastGameMode(resultSet.getInt("last_game_mode"));
            player.setEnableAutoLogin(resultSet.getBoolean("enable_auto_login"));
        }

        return player;
    }
}
