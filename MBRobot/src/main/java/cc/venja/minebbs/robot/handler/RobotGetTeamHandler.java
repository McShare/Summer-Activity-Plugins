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
            var responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);

            var requestHeaders = exchange.getRequestHeaders();

            var respondDao = new RespondDao();
            var responseBody = exchange.getResponseBody();

            if (requestHeaders.containsKey("PlayerName")) {
                var playerName = requestHeaders.get("PlayerName").toString();
                if (RobotMain.instance.team.contains(playerName.toLowerCase())) {
                    respondDao.respondCode = RespondDao.RespondCode.SUCCESS.getValue();
                    respondDao.respondData = RobotMain.instance.team.getString(playerName);
                } else {
                    respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                    respondDao.respondData = "Player does not have team.";
                }
            } else {
                respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                respondDao.respondData = "Invalid Header";
            }

            responseBody.write(RobotMain.gson.toJson(respondDao).getBytes(StandardCharsets.UTF_8));
            responseBody.close();
        }
    }

}
