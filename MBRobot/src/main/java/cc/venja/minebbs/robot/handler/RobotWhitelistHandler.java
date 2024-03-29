package cc.venja.minebbs.robot.handler;

import cc.venja.minebbs.robot.RobotMain;
import cc.venja.minebbs.robot.dao.PlayerDao;
import cc.venja.minebbs.robot.dao.RespondDao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RobotWhitelistHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        var requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("POST")) {
            var body = RobotMain.inputStreamToString(exchange.getRequestBody());

            var responseHeaders = exchange.getResponseHeaders();
            var responseBody = exchange.getResponseBody();

            var playerDao = RobotMain.gson.fromJson(body.toString(), PlayerDao.class);
            var respondDao = new RespondDao();
            if (playerDao.isValid()) {
                RobotMain.addWhitelist(playerDao.playerName, playerDao.KHL);
                respondDao.respondCode = RespondDao.RespondCode.SUCCESS.getValue();
//                try {
//                    RobotMain.addPlayerTeam(playerDao.playerName, RobotMain.getLowestMemberTeam().getValue(), playerDao.KHL);
//                    respondDao.respondData = String.valueOf(Objects.requireNonNull(RobotMain.getPlayerTeam(playerDao.playerName)).getValue());
//                } catch (SQLException e) {
//                    Bukkit.getLogger().info(e.toString());
//                }
                respondDao.respondData = "Success";
            } else {
                respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                respondDao.respondData = "POST Body invalid";
            }
            Bukkit.getLogger().info(respondDao.respondData);

            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(respondDao.respondCode, 0);

            responseBody.write(RobotMain.gson.toJson(respondDao).getBytes(StandardCharsets.UTF_8));
            responseBody.close();
        }
    }

}