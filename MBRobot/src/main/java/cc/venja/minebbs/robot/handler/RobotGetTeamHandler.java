package cc.venja.minebbs.robot.handler;

import cc.venja.minebbs.robot.RobotMain;
import cc.venja.minebbs.robot.dao.RespondDao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.Bukkit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class RobotGetTeamHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("GET")) {
            var requestHeaders = exchange.getRequestHeaders();

            var responseHeaders = exchange.getResponseHeaders();
            var responseBody = exchange.getResponseBody();

            var respondDao = new RespondDao();
            if (requestHeaders.containsKey("PlayerName")) {
                var playerName = requestHeaders.get("PlayerName").get(0);
                try {
                    if (RobotMain.existsPlayerTeam(playerName)) {
                        respondDao.respondCode = RespondDao.RespondCode.SUCCESS.getValue();
                        respondDao.respondData = String.valueOf(RobotMain.getPlayerTeam(playerName));
                    } else {
                        respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                        respondDao.respondData = "Player does not have team.";
                    }
                } catch (SQLException e) {
                    RobotMain.instance.getLogger().info(e.toString());
                }
            } else {
                respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                respondDao.respondData = "Invalid Header";
            }
            Bukkit.getLogger().info(respondDao.respondData);

            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(respondDao.respondCode, 0);

            responseBody.write(RobotMain.gson.toJson(respondDao).getBytes(StandardCharsets.UTF_8));
            responseBody.close();
        }
    }

}
