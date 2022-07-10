package cc.venja.minebbs.robot.handler;

import cc.venja.minebbs.robot.RobotMain;
import cc.venja.minebbs.robot.dao.PlayerDao;
import cc.venja.minebbs.robot.dao.RespondDao;
import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class RobotWhitelistHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        var requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("POST")) {
            var inputStream = exchange.getRequestBody();
            var body = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                int c;
                while ((c = reader.read()) != -1) {
                    body.append((char) c);
                }
            }

            var responseBody = exchange.getResponseBody();

            var playerDao = RobotMain.gson.fromJson(body.toString(), PlayerDao.class);
            var respondDao = new RespondDao();
            Bukkit.getLogger().info(String.valueOf(playerDao.isValid()));
            if (playerDao.isValid()) {
                if (RobotMain.existsWhitelist(playerDao.playerName)) {
                    respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                    respondDao.respondData = "Player already added to whitelist";
                } else {
                    RobotMain.addWhitelist(playerDao.playerName, playerDao.KHL);
                    respondDao.respondCode = RespondDao.RespondCode.SUCCESS.getValue();
                    respondDao.respondData = "Whitelist added";
                }
            } else {
                respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                respondDao.respondData = "POST Body invalid";
            }

            var responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(respondDao.respondCode, 0);

            responseBody.write(RobotMain.gson.toJson(respondDao).getBytes(StandardCharsets.UTF_8));
            responseBody.close();
        }
    }

}