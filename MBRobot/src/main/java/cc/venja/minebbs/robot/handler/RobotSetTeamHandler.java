package cc.venja.minebbs.robot.handler;

import cc.venja.minebbs.robot.RobotMain;
import cc.venja.minebbs.robot.dao.RespondDao;
import cc.venja.minebbs.robot.dao.TeamDao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RobotSetTeamHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        var requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("POST")) {
            var responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, 0);

            var inputStream = exchange.getRequestBody();
            var body = new StringBuilder();
            try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                int c;
                while ((c = reader.read()) != -1) {
                    body.append((char) c);
                }
            }

            var responseBody = exchange.getResponseBody();

            var teamDao = RobotMain.gson.fromJson(body.toString(), TeamDao.class);
            var respondDao = new RespondDao();
            if (teamDao.isValid()) {
                if (RobotMain.existsWhitelist(teamDao.playerName)) {
                    if (RobotMain.getPlayerKHL(teamDao.playerName).equalsIgnoreCase(teamDao.KHL)) {
                        if (teamDao.team > 0 && teamDao.team < 3) {
                            if (!RobotMain.existsPlayerTeam(teamDao.playerName)) {
                                RobotMain.addPlayerTeam(teamDao.playerName, teamDao.team);
                                respondDao.respondCode = RespondDao.RespondCode.SUCCESS.getValue();
                                respondDao.respondData = "Team added";
                            } else {
                                respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                                respondDao.respondData = "Player already have team";
                            }
                        } else {
                            respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                            respondDao.respondData = "Team invalid. Must be 0 - 3";
                        }
                    } else {
                        respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                        respondDao.respondData = "Player's KHL account is not matched";
                    }
                } else {
                    respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                    respondDao.respondData = "Player does not have whitelist";
                }
            } else {
                respondDao.respondCode = RespondDao.RespondCode.FAILED.getValue();
                respondDao.respondData = "POST Body invalid";
            }

            responseBody.write(RobotMain.gson.toJson(respondDao).getBytes(StandardCharsets.UTF_8));
            responseBody.close();
        }
    }

}