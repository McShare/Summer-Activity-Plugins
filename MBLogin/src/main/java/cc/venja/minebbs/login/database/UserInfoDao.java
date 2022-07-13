package cc.venja.minebbs.login.database;

import cc.venja.minebbs.login.LoginMain;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserInfoDao {
    public boolean addUser(UserInfo userInfo) throws SQLException {
        Connection conn = LoginMain.instance.databaseConnection;
        String sql = "insert into user_info(username, password, last_login_ip, last_game_mode, enable_auto_login)"+
                "values(?,?,?,?,?)";

        PreparedStatement statement = conn.prepareStatement(sql);

        statement.setString(1, userInfo.getUsername());
        statement.setString(2, userInfo.getPassword());
        statement.setString(3, userInfo.getLastLoginIp());
        statement.setInt(4, userInfo.getLastGameMode());
        statement.setBoolean(5, userInfo.isEnableAutoLogin());

        return statement.execute();
    }

    public boolean updateUser(UserInfo userInfo) throws SQLException {
        Connection conn = LoginMain.instance.databaseConnection;
        String sql = "update user_info set username=?, password=?, last_login_ip=?, last_game_mode=?, enable_auto_login=?";

        PreparedStatement statement = conn.prepareStatement(sql);

        statement.setString(1, userInfo.getUsername());
        statement.setString(2, userInfo.getPassword());
        statement.setString(3, userInfo.getLastLoginIp());
        statement.setInt(4, userInfo.getLastGameMode());
        statement.setBoolean(5, userInfo.isEnableAutoLogin());

        return statement.execute();
    }

    public boolean delUser(UserInfo userInfo) throws SQLException {
        Connection conn = LoginMain.instance.databaseConnection;
        String sql = "update user_info set username=?, password=?, last_login_ip=?, last_game_mode=?, enable_auto_login=?";

        PreparedStatement statement = conn.prepareStatement(sql);

        statement.setString(1, userInfo.getUsername());
        statement.setString(2, userInfo.getPassword());
        statement.setString(3, userInfo.getLastLoginIp());
        statement.setInt(4, userInfo.getLastGameMode());
        statement.setBoolean(5, userInfo.isEnableAutoLogin());

        return statement.execute();
    }

    public List<UserInfo> query() throws SQLException {
        Connection conn = LoginMain.instance.databaseConnection;
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from user_info");

        List<UserInfo> users = new ArrayList<>();
        UserInfo user = null;
        while (resultSet.next()) {
            user = new UserInfo();
            user.setUsername(resultSet.getString("username"));
            user.setPassword(resultSet.getString("password"));
            user.setLastLoginIp(resultSet.getString("last_login_ip"));
            user.setLastGameMode(resultSet.getInt("last_game_mode"));
            user.setEnableAutoLogin(resultSet.getBoolean("enable_auto_login"));

            users.add(user);
        }

        return users;
    }

    public UserInfo queryByUsername(String username) throws SQLException {
        Connection conn = LoginMain.instance.databaseConnection;
        String sql = "select * from user_info where username=?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setString(1, username);

        ResultSet resultSet = statement.executeQuery();
        UserInfo user = null;
        while (resultSet.next()) {
            user = new UserInfo();
            user.setUsername(resultSet.getString("username"));
            user.setPassword(resultSet.getString("password"));
            user.setLastLoginIp(resultSet.getString("last_login_ip"));
            user.setLastGameMode(resultSet.getInt("last_game_mode"));
            user.setEnableAutoLogin(resultSet.getBoolean("enable_auto_login"));
        }

        return user;
    }
}
