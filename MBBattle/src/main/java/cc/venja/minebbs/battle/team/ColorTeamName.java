package cc.venja.minebbs.battle.team;


import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class ColorTeamName {
    public Team red;
    public Team blue;
    public Team grey;
    public Team yellow;
    public Team none;
    public Scoreboard scoreboard;
    public ColorTeamName() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        red = scoreboard.registerNewTeam("red");
        red.setPrefix("§c");
        blue = scoreboard.registerNewTeam("blue");
        blue.setPrefix("§9");
        grey = scoreboard.registerNewTeam("grey");
        grey.setPrefix("§7");
        yellow = scoreboard.registerNewTeam("yellow");
        yellow.setPrefix("§e");
        none = scoreboard.registerNewTeam("none");

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
