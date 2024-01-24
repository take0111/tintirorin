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
import java.util.concurrent.atomic.AtomicBoolean;

public final class Tintirorin extends JavaPlugin implements Listener {
    VaultManager vault;
    public static JavaPlugin plugin;
    static String prefix = "§7[§c§lチンチロ§r§7]§r ";
    static List<Player> sankasya = new ArrayList<>();
    static List<Player> ko = new ArrayList<>();
    static Player oya;
    double bet;
    int num;
    static int cnt;
    String parent;
    boolean game = false;
    String result;

    int parentResult = 0;
    int childrenResult = 0;

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
                        sankasya.add((Player) sender);
                        ko.add((Player) sender);
                        sender.sendMessage(prefix + "§d§l開催中のゲームにエントリーしました");
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
            if (!ko.isEmpty()) {
                gamePushParent();
            } else {
                getServer().broadcastMessage(prefix + "§c参加者が集まりませんでした");
                sankasya.clear();
                ko.clear();
                game = false;
            }
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

    public void gamePushParent() {
        Bukkit.broadcastMessage(prefix + "§a§lチンチロリンがスタートしました!");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            sendSankasya("§3§l" + parent + "§f§lさん§9§l(親)§f§lがサイコロを振っています...§e§l§kOwO");
        }, 30);
        cnt = 0;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            result = judgement(diceRoll());
            if (!result.equals("目無し")) {
                sendSankasya("§9§l親§f§lの役は" + result + "§f§lです");
                gamePushKo(ko.get(0));
                cnt = 10;
            }
        }, 60);
        cnt++;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (cnt < 3) {
                result = judgement(diceRoll());
                if (!result.equals("目無し")) {
                    sendSankasya("§9§l親§f§lの役は" + result + "§f§lです");
                    gamePushKo(ko.get(0));
                    cnt = 10;
                }
            }
        }, 100);
        cnt++;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (cnt < 3) {
                result = judgement(diceRoll());
                sendSankasya("§9§l親§f§lの役は" + result + "§f§lです");
                gamePushKo(ko.get(0));
            }
        }, 140);
    }

    public void gamePushKo(Player children) {
        AtomicBoolean humu = new AtomicBoolean(false);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            sendSankasya("§3§l" + children.getName() + "§f§lさん§7§l(子)§f§lがサイコロを振っています...§e§l§kOwO");
        }, 20);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            result = judgement(diceRoll());
            if (!result.equals("目無し")) {
                humu.set(true);
                cnt = 0;
                ko.remove(children);
                sendSankasya("§7§l子§f§lの役は" + result + "§f§lです！");
                if (!ko.isEmpty()) {
                    gamePushKo(ko.get(0));
                } else {
                    sendSankasya("ゲームが終了しました");
                    game = false;
                    sankasya.clear();
                }
            }
        }, 50);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            result = judgement(diceRoll());
            if (humu.equals(false)) {
                cnt = 0;
                ko.remove(children);
                sendSankasya("§7§l子§f§lの役は" + result + "§f§lです！");
                if (!ko.isEmpty()) {
                    gamePushKo(ko.get(0));
                } else {
                    sendSankasya("ゲームが終了しました");
                    game = false;
                    sankasya.clear();
                }
            }
        }, 80);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (humu.equals(false)) {
                result = judgement(diceRoll());
                sendSankasya("§7§l子§f§lの役は" + result + "§f§lです！");
                cnt = 0;
                ko.remove(children);
                if (!ko.isEmpty()) {
                    gamePushKo(ko.get(0));
                } else {
                    sendSankasya("ゲームが終了しました");
                    game = false;
                    sankasya.clear();
                }
            } else {
                sendSankasya("§7§l子§f§lの役は" + result + "§f§lです！");
            }
        }, 120);
    }

    public int[] diceRoll(){
        int dice[] = {0,0,0};
        Random rand = new Random();
        for (int j = 0; j < 3; j++) {
            dice[j] = rand.nextInt(6) + 1;
        }
        cnt++;
        sendSankasya("§f§l" + cnt + "回目§e§l " + dice[0] + "§f§l,§e§l" + dice[1] + "§f§l,§e§l" + dice[2] + "§r");
        Arrays.sort(dice);
        return dice;
    }

    public String judgement(int[] dice) {
        if (dice[0] == dice[2]) {
            if (dice[0] == 1) {
                return "§c§lピンゾロ";
            }
            return "§9§lゾロ目";
        }
        if (dice[0] == dice[1]) {
            return "§e§l" + dice[2] + "§e§lの目";
        }
        if (dice[1] == dice[2]) {
            return "§e§l" + dice[0] + "§e§lの目";
        }
        if (dice[0] == 4 && dice[1] == 5 && dice[2] == 6) {
            return "§5§lシゴロ";
        }
        if (dice[0] == 1 && dice[1] == 2 && dice[2] == 3) {
            return "§c§lヒフミ";
        }
        return "目無し";
    }

    public void sendSankasya(String message){
        for(Player p : sankasya){
            p.sendMessage(prefix + message);
        }
    }
}

