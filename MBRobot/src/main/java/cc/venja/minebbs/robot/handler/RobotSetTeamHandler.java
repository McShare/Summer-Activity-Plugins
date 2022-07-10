package cc.venja.minebbs.robot.handler;

import cc.venja.minebbs.robot.RobotMain;
import cc.venja.minebbs.robot.dao.RespondDao;
import cc.venja.minebbs.robot.dao.TeamDao;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class RobotSetTeamHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {
        var requestMethod = exchange.getRequestMethod();
        if (requestMethod.equalsIgnoreCase("POST")) {
            var body = RobotMain.inputStreamToString(exchange.getRequestBody());

            var responseHeaders = exchange.getResponseHeaders();
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

            responseHeaders.set("Content-Type", "text/plain");
            exchange.sendResponseHeaders(respondDao.respondCode, 0);


            responseBody.write(RobotMain.gson.toJson(respondDao).getBytes(StandardCharsets.UTF_8));
            responseBody.close();
        }
    }

}