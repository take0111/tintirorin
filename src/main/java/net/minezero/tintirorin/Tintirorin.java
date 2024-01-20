package net.minezero.tintirorin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.bukkit.Bukkit.getServer;
import static org.bukkit.Bukkit.savePlayers;

public final class Tintirorin extends JavaPlugin implements Listener {
    VaultManager vault;
    public static JavaPlugin plugin;
    static String prefix = "§7[§c§lチンチロ§r§7]§r ";
    static List<Player> sankasya = new ArrayList<>();
    double bet;
    int num;
    static int cnt = 0;
    int[] dice = {0, 0, 0};
    String parent;
    boolean game = false;
    int result;

    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        vault = new VaultManager(plugin);
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        if (command.getName().equals("dice")) {
            if (args.length == 1) {
                if (args[0].equals("join")) {
                    if (!game) {
                        sender.sendMessage(prefix + "§c§l開催中のゲームはありません");
                        return true;
                    }
                    if (!sankasya.contains(p)) {
                        sender.sendMessage(prefix + "§d§l開催中のゲームにエントリーしました");
                        sankasya.add((Player) sender);
                    }else {
                        sender.sendMessage(prefix + "§c§l既に参加済みです");
                    }
                }
            }
            if (args.length == 2) {
                if (!game) {
                    bet = Integer.parseInt(args[0]);
                    num = Integer.parseInt(args[1]);
                    sankasya.add((Player) sender);
                    parent = sender.getName();
                    gameStart(p,bet,num);
                }else {
                    sender.sendMessage(prefix + "§c§l開催中のゲームがあります");
                }
            }
            return false;
        }
        return false;
    }

    private void timer(Player p) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getServer().broadcastMessage(prefix + "§7開始まで5秒");
            for (Player j : sankasya) {
                j.playSound(j, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            }
        }, 100);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getServer().broadcastMessage(prefix + "§7開始まで4秒");
            for (Player j : sankasya) {
                j.playSound(j, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            }
        }, 120);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getServer().broadcastMessage(prefix + "§7開始まで3秒");
            for (Player j : sankasya) {
                j.playSound(j, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            }
        }, 140);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getServer().broadcastMessage(prefix + "§7開始まで2秒");
            for (Player j : sankasya) {
                j.playSound(j, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            }
        }, 160);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getServer().broadcastMessage(prefix + "§7開始まで1秒");
            for (Player j : sankasya) {
                j.playSound(j, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            }
        }, 180);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            gamePush(p);
        }, 200);
    }

    private void gameStart(Player p, double bet, int num) {
        game = true;
        getServer().broadcastMessage(prefix + "§3§l" + p.getName() + "§f§lさんにより§c§l" + num + "§f§l人募集の§e§l" + bet + "§f§l円チンチロが開始されました!");
        TextComponent message1 = new TextComponent(prefix + "§e[§l参加する§r§e]");
        message1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dice join"));
        getServer().spigot().broadcast(message1);
        timer(p);
    }

    public void gamePush(Player p) {
        int late = 50;
        Bukkit.broadcastMessage(prefix + "§a§lチンチロリンがスタートしました!");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            sendSankasya("§3§l" + parent + "§f§lさん§9§l(親)§f§lがサイコロを振っています...§e§l§kOwO");
        }, 20);
        for (int i = 0; i < 3; i++) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                result = judgement(dice);
                switch (result) {
                    case 111:
                       sendSankasya("ピンゾロ");
                       break;
                    case 333:
                        sendSankasya("ゾロ目");
                        break;
                    case 456:
                        sendSankasya("シゴロ");
                        break;
                    case 123:
                        sendSankasya("sakime");
                        break;
                    case 6:
                        sendSankasya("6の目");
                        break;
                    case 5:
                        sendSankasya("5の目");
                        break;
                    case 4:
                        sendSankasya("4の目");
                        break;
                    case 3:
                        sendSankasya("3の目");
                        break;
                    case 2:
                        sendSankasya("2の目");
                        break;
                    case 1:
                        sendSankasya("1の目");
                        break;
                    default:
                        sendSankasya("目無し");
                }
            }, late);
            late += 30;
        }
    }

    public static int judgement(int[] dice) {
        Random rand = new Random();
        for (int j = 0; j < 3; j++) {
            dice[j] = rand.nextInt(6) + 1;
        }
        cnt++;
        sendSankasya("§f§l" + cnt + "回目§e§l " + dice[0] + "§f§l,§e§l" + dice[1] + "§f§l,§e§l" + dice[2] + "§r");
        Arrays.sort(dice);

        if (dice[0] == dice[2]) {
            if (dice[0] == 1) {
                return 111;
            }
            return 333;
        }
        if (dice[0] == dice[1]) {
            return dice[2];
        }
        if (dice[1] == dice[2]) {
            return dice[0];
        }
        if (dice[0] == 4 && dice[1] == 5 && dice[2] == 6) {
            return 456;
        }
        if (dice[0] == 1 && dice[1] == 2 && dice[2] == 3) {
            return 123;
        }
        return 0;
    }

    public static void sendSankasya(String message){
        for(Player p : sankasya){
            p.sendMessage(prefix + message);
        }
    }
}

