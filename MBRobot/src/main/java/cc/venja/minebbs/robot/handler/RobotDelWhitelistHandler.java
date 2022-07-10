package cc.venja.minebbs.robot.handler;

import cc.venja.minebbs.robot.RobotMain;
import cc.venja.minebbs.robot.dao.PlayerDao;
import cc.venja.minebbs.robot.dao.RespondDao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RobotDelWhitelistHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        var requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("POST")) {
            var body = RobotMain.inputStreamToString(exchange.getRequestBody());

            var responseHeaders = exchange.getResponseHeaders();
            var responseBody = exchange.getResponseBody();

            var playerDao = RobotMain.gson.fromJson(body.toString(), PlayerDao.class);
            var respondDao = new RespondDao();
            if (playerDao.isValid()) {
                if (RobotMain.existsWhitelist(playerDao.playerName)) {
                    if (RobotMain.getPlayerKHL(playerDao.playerName).equalsIgnoreCase(playerDao.KHL)) {
                        RobotMain.removeWhitelist(playerDao.playerName);
                        respondDao.respondCode = RespondDao.RespondCode.SUCCESS.getValue();
                        respondDao.respondData = "Removed whitelist";
                    } else {
                        respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                        respondDao.respondData = "KHL account not match to record";
                    }
                } else {
                    respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                    respondDao.respondData = "Player don't have a whitelist yet";
                }
            } else {
                respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                respondDao.respondData = "POST Body invalid";
            }
            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(respondDao.respondCode, 0);

            responseBody.write(RobotMain.gson.toJson(respondDao).getBytes(StandardCharsets.UTF_8));
            responseBody.close();
        }
    }

}