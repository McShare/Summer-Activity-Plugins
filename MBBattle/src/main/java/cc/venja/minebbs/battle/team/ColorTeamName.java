package cc.venja.minebbs.battle.team;


import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;

public class ColorTeamName {
    public Team red;
    public Team blue;
    public Team grey;
    public Team yellow;
    public Team none;
    public Scoreboard scoreboard;
    public ColorTeamName() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        red = registerNewTeamIfNotExist("red");
        red.setPrefix("§c");
        blue = registerNewTeamIfNotExist("blue");
        blue.setPrefix("§9");
        grey = registerNewTeamIfNotExist("grey");
        grey.setPrefix("§7");
        yellow = registerNewTeamIfNotExist("yellow");
        yellow.setPrefix("§e");
        none = registerNewTeamIfNotExist("none");
    }

    public Team registerNewTeamIfNotExist(String name) {
        Team team = scoreboard.getTeam(name);
        if (team != null) {
            return team;
        }
        return scoreboard.registerNewTeam(name);
    }

    public Team getTeamByTeamName(String teamName) {
        return switch (teamName) {
            case "TeamRED" -> red;
            case "TeamBLUE" -> blue;
            case "TeamGREY" -> grey;
            case "TeamYELLOW" -> yellow;
            default -> none;
        };

    }
}
