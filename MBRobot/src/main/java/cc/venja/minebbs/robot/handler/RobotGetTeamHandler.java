package cc.venja.minebbs.robot.handler;

import cc.venja.minebbs.robot.RobotMain;
import cc.venja.minebbs.robot.dao.RespondDao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

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
                if (RobotMain.existsPlayerTeam(playerName)) {
                    respondDao.respondCode = RespondDao.RespondCode.SUCCESS.getValue();
                    respondDao.respondData = String.valueOf(RobotMain.getPlayerTeam(playerName));
                } else {
                    respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                    respondDao.respondData = "Player does not have team.";
                }
            } else {
                respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                respondDao.respondData = "Invalid Header";
            }

            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(respondDao.respondCode, 0);

            responseBody.write(RobotMain.gson.toJson(respondDao).getBytes(StandardCharsets.UTF_8));
            responseBody.close();
        }
    }

}
