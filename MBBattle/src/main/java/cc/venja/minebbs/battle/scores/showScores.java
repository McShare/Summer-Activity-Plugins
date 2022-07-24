package cc.venja.minebbs.battle.scores;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import cc.venja.minebbs.battle.BattleMain;
import cc.venja.minebbs.login.enums.Team;
import cc.venja.minebbs.robot.RobotMain;

import java.sql.SQLException;
import java.util.*;


public class showScores {
    private final ScoreboardManager manager = Bukkit.getScoreboardManager(); // 取得计分板管理器
    private final Scoreboard scoreboard = manager.getMainScoreboard(); // 新建计分板
    private Objective Board;

    public void UpdateScoreboard() throws SQLException {
        if (Board != null) {
            Board.unregister();
        } else {
            Board = scoreboard.registerNewObjective("jifenban", "dummy", Component.text("§l积分榜"));

        }

        ArrayList<String> content = new ArrayList<>(); // 创建内容清单，便于之后有顺序的列出计分项
        content.add("§2积分前5的玩家");

        Map<String, Integer> PlayersScores = new TreeMap<>(); //创建玩家积分缓存
        for (PlayerScoreHandle scoreHandle : BattleMain.instance.playerScoreHandleList) {
            PersonalScore scores = scoreHandle.getScore();
            PlayersScores.put(scores.playerName, scores.get());
        }
        List<Map.Entry<String, Integer>> PlayerList = new ArrayList<>(PlayersScores.entrySet());
        PlayerList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue())); //对玩家缓存进行倒序

        int i = 0;
        for (Map.Entry<String, Integer> mapping : PlayerList) {
            if (i >= 5) {
                break;
            }
            content.add(PlayerName2TeamColor(mapping.getKey())+mapping.getKey()+": §4"+mapping.getValue());
            i++;
        }
        content.add("§2团队总分");
        Map<String,Integer> TeamScores = new TreeMap<>(); //创建团队积分缓存
        for (TeamScoreHandle scoreHandle : BattleMain.instance.teamScoreHandleList){
            TeamScore scores = scoreHandle.getScore();
            TeamScores.put(Objects.requireNonNull(scores.team).getName(),scores.get());
        }
        List<Map.Entry<String,Integer>> TeamList = new ArrayList<>(TeamScores.entrySet());
        TeamList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue())); //团队积分倒序

        for (Map.Entry<String,Integer> mapping: TeamList){
            content.add(TeamName2TeamColor(mapping.getKey())+mapping.getKey()+": §4"+mapping.getValue());
        }
        int size = content.size();
        while (size >= 0) {
            Score score = Board.getScore(content.get(size));
            score.setScore(size);
            size--;
        }

        Collection<? extends Player> AllPlayer = Bukkit.getOnlinePlayers(); // 获取玩家在线名单用于发送新的积分榜
        for(Player p:AllPlayer){
            p.setScoreboard(scoreboard);
        }
    }

    public static String PlayerName2TeamColor(String PlayerName) throws SQLException { //玩家名转颜色
        Team team = RobotMain.getPlayerTeam(PlayerName);
        String teamStr = Objects.requireNonNull(team).getName();
        return TeamName2TeamColor(teamStr);
    }

    public static String TeamName2TeamColor(String TeamName){ //队名转颜色
        return switch (TeamName) {
            case "TeamRED" -> "§c";
            case "TeamBLUE" -> "§9";
            case "TeamGREY" -> "§7";
            case "TeamYELLOW" -> "§e";
            default -> "";
        };
    }
}



