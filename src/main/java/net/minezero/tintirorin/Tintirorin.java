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

public final class Tintirorin extends JavaPlugin implements Listener {
    VaultManager vault;
    public static JavaPlugin plugin;
    static String prefix = "§f[§cMZC§f]§r ";
    static List<Player> list = new ArrayList<>();
    static List<Player> children = new ArrayList<>();
    boolean power = true;
    boolean recruit = false;
    boolean game = false;
    boolean parentTurn = false;
    boolean childrenTurn = false;
    boolean multiplier = false;
    Player parent;
    int parentMultiplier;
    double bet;
    int num;
    int childCnt = 1;
    int parentResult = 0;
    int childrenResult = 0;
    int[][] parentDice = { {0,0,0} , {0,0,0} , {0,0,0} };
    int[][] childrenDice = { {0,0,0} , {0,0,0} , {0,0,0} };

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

        if (command.getName().equals("mzc")) {
            if (args.length == 0) {
                p.sendMessage("§7------------- " + prefix + "§7-------------");
                if (!power) {
                    sendErrorMessage(p,"§c§l現在ゲームの募集はできません");
                }
                p.sendMessage("§a/mzc new §e<金額> <最大募集人数> §8→ §f親としてゲームを開催します");
                p.sendMessage("§a/mzc join §8→ §f子として募集中のゲームに参加します");
                p.sendMessage("§a/mzc rule §8→ §fルールを表示します");
                p.sendMessage("§a/mzc role §8→ §f役一覧を表示します");
                if (p.isOp()) {
                    p.sendMessage("§7------------ §f[§cADMIN§f] §7------------");
                    p.sendMessage("§a/mzc on§7/§aoff §8→ §fゲームの募集を許可/禁止する");
                    p.sendMessage("§a/mzc cancel §8→ §f進行中のゲームを中止します ※掛け金は返却されます");
                }
                if (recruit) {
                    p.sendMessage("§7------------- §f[§d§l募集§f] §7-------------");
                    p.sendMessage("§c§l親§f§l:§f§l " + parent.getName());
                    p.sendMessage("§e§l掛け金§f§l:§f§l " + vault.getJpyBal(bet) + "円 §e§l必要金額§f§l:§f§l " + vault.getJpyBal(bet * 10) + "円");
                    p.sendMessage("§3§l参加者§f§l:§f§l " + children.size() + "§7§l/§f§l" + num);
                }
                p.sendMessage("§7--------------------------------");
                return true;
            }
            if (args.length == 1) {
                if (args[0].equals("join")) {
                    if (list.contains(p)) {
                        sendErrorMessage(p,"§c既に参加済みです");
                        return true;
                    }
                    if (children.size() >= num) {
                        sendErrorMessage(p,"§c募集人数に達しているため参加できませんでした");
                        return true;
                    }
                    if (vault.getBal(p.getUniqueId()) < bet * 10) {
                        sendErrorMessage(p,"§c所持金が不足しています (必要金額: " + vault.getJpyBal(bet * 10) + "円)");
                        return true;
                    }
                    if (!recruit) {
                        sendErrorMessage(p,"§c現在募集中のゲームは存在しません");
                        return false;
                    }
                    p.sendMessage(prefix + "§d募集中のゲームに参加しました");
                    list.add(p);
                    children.add(p);
                    vault.withdraw(p,bet * 10);
                    return true;
                }
                if (args[0].equals("role")) {
                    p.sendMessage("§7------------- §f[§6§l役一覧§f] §7-------------");
                    p.sendMessage("§7( §e§l1§f§l,§e§l1§f§l,§e§l1 §7| §f§l5倍づけ§7 ) §f§l全ての目が§e§l1§f§l");
                    p.sendMessage("§7( §e§l6§f§l,§e§l6§f§l,§e§l6 §7| §f§l3倍づけ§7 ) §f§l全ての目が同数");
                    p.sendMessage("§7( §e§l4§f§l,§e§l5§f§l,§e§l6 §7| §f§l2倍づけ§7 ) §e§l4§f§lと§e§l5§f§lと§e§l6§f§lの組み合わせ、順不同");
                    p.sendMessage("§7( §e§l3§f§l,§e§l3§f§l,§e§l4 §7| §f§l1倍§7 ) §f§l同数のペアと余り、余りが§6§l役§f§lになり数字が§c§l大きいほど強い");
                    p.sendMessage("§7( §e§l1§f§l,§e§l2§f§l,§e§l3 §7| §f§l2倍払い§7 ) §e§l1§f§lと§e§l2§f§lと§e§l3§f§lの組み合わせ、順不同");
                    p.sendMessage("");
                    p.sendMessage("§f§l上記以外の組み合わせは§7§l目無し§f§lとなり役が出るまで最大§e§l3§f§l回振ることができる");
                    p.sendMessage("§7---------------------------------");
                }
                if (args[0].equals("rule")) {
                    p.sendMessage("§7------------- §f[§6§lルール§f] §7-------------");
                    p.sendMessage("§c§l基本的なルールは通常通り");
                    p.sendMessage("");
                    p.sendMessage("§c§l親§f§lの即勝ち、即負け、交代は§7§l無し§f§l。一巡で終了する");
                    p.sendMessage("§c§l親§f§lと§b§l子§f§lが同じ役の場合は§7§l引き分け§f§lとなる");
                    p.sendMessage("§f§l倍率同士の場合は差分で倍率が決まる");
                    p.sendMessage("§7(例: 親がゾロ目(3倍づけ)、子がシゴロ(2倍づけ)の場合 3-2 で親の1倍勝ち)");
                    p.sendMessage("§f§lただし§c§lヒフミ§f§l(2倍払い)と倍率の場合は乗じる");
                    p.sendMessage("§7(例: 親がゾロ目(3倍づけ)、子がヒフミ(2倍払い)の場合 3×2 で親の6倍勝ち)");
                    p.sendMessage("§c§l親§f§lは §e§l掛け金の10倍 §f§l× §a§l募集人数 §f§l分所持していないと§c§l募集不可");
                    p.sendMessage("§b§l子§f§lは §e§l掛け金の§e§l10倍 §f§l分所持していないと§c§l参加不可");
                    p.sendMessage("§7(ピンゾロ(5倍づけ) と ヒフミ(2倍払い) が重なると10倍づけになる為)");
                    p.sendMessage("§7---------------------------------");
                }
                if (args[0].equals("on")) {
                    if (!p.isOp()) {
                        sendErrorMessage(p,"§cあなたには権限がありません");
                        return true;
                    }
                    if (power) {
                        sendErrorMessage(p,"§c既にONです");
                        return true;
                    }
                    power = true;
                    Bukkit.broadcastMessage(prefix + "§a§lチンチロリンの新規募集を再開しました");
                    return true;
                }
                if (args[0].equals("off")) {
                    if (!p.isOp()) {
                        sendErrorMessage(p,"§cあなたには権限がありません");
                        return true;
                    }
                    if (!power) {
                        sendErrorMessage(p,"§c既にOFFです");
                        return true;
                    }
                    power = false;
                    Bukkit.broadcastMessage(prefix + "§c§lチンチロリンの新規募集を中止しました");
                    return true;
                }
                if (args[0].equals("cancel")) {
                    if (!p.isOp()) {
                        sendErrorMessage(p,"§cあなたには権限がありません");
                        return true;
                    }
                    if (!game) {
                        sendErrorMessage(p,"§c開催中のゲームは存在しません");
                        return true;
                    }
                    Bukkit.broadcastMessage(prefix + "§c§l運営によってゲームがキャンセルされました");
                    for (Player c : children) {
                        vault.deposit(c,bet * 10);
                    }
                    vault.deposit(parent,bet * parentMultiplier);
                    list.clear();
                    children.clear();
                    childrenTurn = false;
                    parentResult = 0;
                    childrenResult = 0;
                    childCnt = 1;
                    multiplier = false;
                    game = false;
                    return true;
                }
            }
            if (args.length == 2) {
                sendErrorMessage(p,"§c引数が不足しています");
            }
            if (args.length == 3) {
                if (args[0].equals("new")) {
                    try {
                        bet = Integer.parseInt(args[1]);
                        num = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sendErrorMessage(p,"§c掛け金と募集人数は整数で入力してください");
                        return false;
                    }
                    if (num <= 0 || num >= 11) {
                        sendErrorMessage(p,"§c募集人数は1人以上10人以下で入力してください");
                        return true;
                    }
                    if (vault.getBal(p.getUniqueId()) < bet * num * 10) {
                        sendErrorMessage(p,"§c所持金が不足しています (必要金額: " + vault.getJpyBal(bet * num * 10) + "円)");
                        return true;
                    }
                    if (recruit) {
                        sendErrorMessage(p,"§c既に募集中のゲームが存在します");
                        return true;
                    }
                    if (game) {
                        sendErrorMessage(p,"§c既に開催中のゲームが存在します");
                        return true;
                    }
                    if (!power) {
                        sendErrorMessage(p,"§c現在新規募集は許可されていません");
                        return true;
                    }
                    parent = p;
                    list.add(p);
                    this.bet = Double.parseDouble(args[1]);
                    this.num = Integer.parseInt(args[2]);
                    parentMultiplier = num * 10;
                    gameStart(p, bet, num);
                }
            }
            return false;
        }
        return false;
    }

    private void timer() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getServer().broadcastMessage(prefix + "§7開始まで5秒");
            for (Player j : list) {
                j.playSound(j, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            }
        }, 100);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getServer().broadcastMessage(prefix + "§7開始まで4秒");
            for (Player j : list) {
                j.playSound(j, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            }
        }, 120);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getServer().broadcastMessage(prefix + "§7開始まで3秒");
            for (Player j : list) {
                j.playSound(j, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            }
        }, 140);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getServer().broadcastMessage(prefix + "§7開始まで2秒");
            for (Player j : list) {
                j.playSound(j, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            }
        }, 160);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            getServer().broadcastMessage(prefix + "§7開始まで1秒");
            for (Player j : list) {
                j.playSound(j, Sound.UI_BUTTON_CLICK, 1.0f, 2.0f);
            }
        }, 180);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!children.isEmpty()) {
                gamePushParent();
            } else {
                getServer().broadcastMessage(prefix + "§c参加者が集まりませんでした");
                vault.deposit(parent,bet * num * 10);
                for (Player c : children) {
                    vault.deposit(c,bet * 10);
                }
                list.clear();
                children.clear();
                recruit = false;
            }
        }, 200);
    }

    private void gameStart(Player p, double bet, int num) {
        recruit = true;
        getServer().broadcastMessage(prefix + "§c§l" + parent.getName() + "§f§lさんにより最大§b§l" + num + "§f§l人募集の§e§l" + vault.getJpyBal(bet) + "円§f§lチンチロが募集されました");
        vault.withdraw(p,bet * parentMultiplier);
        TextComponent message1 = new TextComponent(prefix + "§e[§l参加する§r§e]");
        message1.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mzc join"));
        getServer().spigot().broadcast(message1);
        timer();
    }

    public void gamePushParent() {
        game = true;
        parentTurn = true;
        recruit = false;

        Bukkit.broadcastMessage(prefix + "§a§lゲームがスタートしました!");
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            sendMessage("§3§l" + parent.getName() + "§f§lさん§c§l(親)§f§lがサイコロを振っています...§e§l§kOwO");
        }, 30);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int cnt = -1;
            do {
                cnt++;
                judgement(diceRoll(cnt));
            }while (parentResult == 0 && cnt < 2);

            if (cnt == 0 || cnt == 1 || cnt == 2) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    sendMessage("§f§l1回目 §e§l" + parentDice[0][0] + "§f§l,§e§l" + parentDice[0][1] + "§f§l,§e§l" + parentDice[0][2] + "§r");
                }, 40);
                if (cnt == 0) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        getRole();
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            sendMessage("§a§l" + childCnt + "人目の子のターンが開始されました!");
                            gamePushChildren(children.get(0));
                        } ,40);
                    }, 70);
                }
            }
            if (cnt == 1 || cnt == 2) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    sendMessage("§f§l2回目 §e§l" + parentDice[1][0] + "§f§l,§e§l" + parentDice[1][1] + "§f§l,§e§l" + parentDice[1][2] + "§r");
                }, 70);
                if (cnt == 1) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        getRole();
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            sendMessage("§a§l" + childCnt + "人目の子のターンが開始されました!");
                            gamePushChildren(children.get(0));
                        } ,70);
                    }, 100);
                }
            }
            if (cnt == 2) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    sendMessage("§f§l3回目 §e§l" + parentDice[2][0] + "§f§l,§e§l" + parentDice[2][1] + "§f§l,§e§l" + parentDice[2][2] + "§r");
                }, 100);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    getRole();
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        sendMessage("§a§l" + childCnt + "人目の子のターンが開始されました!");
                        gamePushChildren(children.get(0));
                    } ,100);
                }, 130);
            }
        }, 30);
    }

    public void gamePushChildren(Player child) {
        parentTurn = false;
        childrenTurn = true;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            sendMessage("§3§l" + child.getName() + "§f§lさん§b§l(子)§f§lがサイコロを振っています...§e§l§kOwO");
        }, 30);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            int cnt = -1;
            do {
                cnt++;
                judgement(diceRoll(cnt));
            }while (childrenResult == 0 && cnt < 2);

            if (cnt == 0 || cnt == 1 || cnt == 2) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    sendMessage("§f§l1回目 §e§l" + childrenDice[0][0] + "§f§l,§e§l" + childrenDice[0][1] + "§f§l,§e§l" + childrenDice[0][2] + "§r");
                }, 40);
                if (cnt == 0) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        getRole();
                        children.remove(0);
                        parentVsChildren(child);
                    }, 70);
                }
            }
            if (cnt == 1 || cnt == 2) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    sendMessage("§f§l2回目 §e§l" + childrenDice[1][0] + "§f§l,§e§l" + childrenDice[1][1] + "§f§l,§e§l" + childrenDice[1][2] + "§r");
                }, 70);
                if (cnt == 1) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        getRole();
                        children.remove(0);
                        parentVsChildren(child);
                    }, 100);
                }
            }
            if (cnt == 2) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    sendMessage("§f§l3回目 §e§l" + childrenDice[2][0] + "§f§l,§e§l" + childrenDice[2][1] + "§f§l,§e§l" + childrenDice[2][2] + "§r");
                }, 100);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    getRole();
                    children.remove(0);
                    parentVsChildren(child);
                }, 130);
            }
        }, 30);
    }

    public int[] diceRoll(int cnt) {
        if (parentTurn) {
            int[] dice = {0, 0, 0};
            Random rand = new Random();
            for (int i = 0; i < 3; i++) {
                dice[i] = rand.nextInt(6) + 1;
                parentDice[cnt][i] = dice[i];
            }
            Arrays.sort(dice);
            return dice;
        }
        if (childrenTurn) {
            int[] dice = {0, 0, 0};
            Random rand = new Random();
            for (int i = 0; i < 3; i++) {
                dice[i] = rand.nextInt(6) + 1;
                childrenDice[cnt][i] = dice[i];
            }
            Arrays.sort(dice);
            return dice;
        }
        return new int[0];
    }

    public void judgement(int[] dice) {
        if (parentTurn) {
            if (dice[0] == dice[1]) {
                parentResult = dice[2];
            }
            if (dice[1] == dice[2]) {
                parentResult = dice[0];
            }
            if (dice[0] == 4 && dice[1] == 5 && dice[2] == 6) {
                parentResult = 20;
                multiplier = true;
            }
            if (dice[0] == 1 && dice[1] == 2 && dice[2] == 3) {
                parentResult = -20;
                multiplier = true;
            }
            if (dice[0] == dice[2]) {
                parentResult = 30;
                multiplier = true;
                if (dice[0] == 1) {
                    parentResult = 50;
                }
            }
        }
        if (childrenTurn) {
            if (dice[0] == dice[1]) {
                childrenResult = dice[2];
            }
            if (dice[1] == dice[2]) {
                childrenResult = dice[0];
            }
            if (dice[0] == 4 && dice[1] == 5 && dice[2] == 6) {
                childrenResult = 20;
                multiplier = true;
            }
            if (dice[0] == 1 && dice[1] == 2 && dice[2] == 3) {
                childrenResult = -20;
                multiplier = true;
            }
            if (dice[0] == dice[2]) {
                childrenResult = 30;
                multiplier = true;
                if (dice[0] == 1) {
                    childrenResult = 50;
                }
            }
        }
    }

    public void getRole() {
        if (parentTurn) {
            if (parentResult == 0) {
                sendMessage("§c§l親§f§lの§6§l役§f§l: §7§l目無し");
            }
            if (parentResult > 0 && parentResult < 7) {
                sendMessage("§c§l親§f§lの§6§l役§f§l:§e§l " + parentResult);
            }
            if (parentResult == -20) {
                sendMessage("§c§l親§f§lの§6§l役§f§l: §c§lヒフミ");
            }
            if (parentResult == 20) {
                sendMessage("§c§l親§f§lの§6§l役§f§l: §d§lシゴロ");
            }
            if (parentResult == 30) {
                sendMessage("§c§l親§f§lの§6§l役§f§l: §5§lゾロ目");
            }
            if (parentResult == 50) {
                sendMessage("§c§l親§f§lの§6§l役§f§l: §4§lピンゾロ");
            }
        }
        if (childrenTurn){
            if (childrenResult == 0) {
                sendMessage("§b§l子§f§lの§6§l役§f§l: §7§l目無し");
            }
            if (childrenResult > 0 && childrenResult < 7) {
                sendMessage("§b§l子§f§lの§6§l役§f§l: §e§l" + childrenResult);
            }
            if (childrenResult == -20) {
                sendMessage("§b§l子§f§lの§6§l役§f§l: §c§lヒフミ");
            }
            if (childrenResult == 20) {
                sendMessage("§b§l子§f§lの§6§l役§f§l: §d§lシゴロ");
            }
            if (childrenResult == 30) {
                sendMessage("§b§l子§f§lの§6§l役§f§l: §5§lゾロ目");
            }
            if (childrenResult == 50) {
                sendMessage("§b§l子§f§lの§6§l役§f§l: §4§lピンゾロ");
            }
        }
    }

    public void parentVsChildren(Player child) {
        if (parentResult == childrenResult) {
            sendMessage("§e§l結果§f§l: §7§l引き分け!");
            child.sendMessage(prefix + "§f§l収支は§e§l+0円§f§lです");
            vault.deposit(child,bet * 10);
        } else if (!multiplier) {
            if (parentResult > childrenResult) {
                sendMessage("§e§l結果§f§l: §c§l親§f§lの勝利!");
                parentMultiplier++;
                child.sendMessage(prefix + "§f§l収支は§c§l-" + vault.getJpyBal(bet) + "円§f§lです");
                vault.deposit(child,bet * 9);
            }
            if (parentResult < childrenResult) {
                sendMessage("§e§l結果§f§l: §b§l子§f§lの勝利!");
                parentMultiplier--;
                child.sendMessage(prefix + "§f§l収支は§e§l+" + vault.getJpyBal(bet) + "円§f§lです");
                vault.deposit(child,bet * 11);
            }
        } else {
            if (parentResult < 7 && parentResult > 0) {
                parentResult = 0;
            }
            if (childrenResult < 7 && childrenResult > 0) {
                childrenResult = 0;
            }
            if (parentResult == 50 && childrenResult == -20) {
                sendMessage("§e§l結果§f§l: " + "§c§l親§f§lの勝利! 倍率: §4§l10倍!");
                parentMultiplier += 10;
                child.sendMessage(prefix + "§f§l収支は§c§l-" + vault.getJpyBal(bet * 10) + "円§f§lです");
            } else if (parentResult == -20 && childrenResult == 50) {
                sendMessage("§e§l結果§f§l: " + "§b§l子§f§lの勝利! 倍率: §4§l10倍!");
                parentMultiplier -= 10;
                child.sendMessage(prefix + "§f§l収支は§e§l+" + vault.getJpyBal(bet * 10) + "円§f§lです");
                vault.deposit(child,bet * 20);
            } else if (parentResult > childrenResult) {
                int num = (parentResult - childrenResult) / 10;
                parentMultiplier += num;
                sendMessage("§e§l結果§f§l: " + "§c§l親§f§lの勝利! 倍率: §a§l" + num + "倍!");
                child.sendMessage(prefix + "§f§l収支は§c§l-" + vault.getJpyBal(bet * num) + "円§f§lです");
                vault.deposit(child, bet * 10 - bet * num);
            } else {
                int num = (childrenResult - parentResult) / 10;
                parentMultiplier -= num;
                sendMessage("§e§l結果§f§l: " + "§b§l子§f§lの勝利! 倍率: §a§l" + num + "倍!");
                child.sendMessage(prefix + "§f§l収支は§e§l+" + vault.getJpyBal(bet * num) + "円§f§lです");
                vault.deposit(child,bet * num);
            }
        }

        multiplier = false;
        childrenResult = 0;

        if (!children.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                childCnt++;
                sendMessage("§a§l" + childCnt + "人目の子のターンに移ります");
                gamePushChildren(children.get(0));
            }, 50);
        } else {
            gameEnd();
        }
    }

    public void gameEnd() {
        list.clear();
        children.clear();
        childrenTurn = false;
        parentResult = 0;
        childrenResult = 0;
        childCnt = 1;
        multiplier = false;
        game = false;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if ( (bet * (parentMultiplier - 10 * num)) < 0 ) {
                parent.sendMessage(prefix + "§f§l収支は§c§l" + vault.getJpyBal( bet * (parentMultiplier - 10 * num) ) + "円§f§lです");
            } else {
                parent.sendMessage(prefix + "§f§l収支は§e§l+" + vault.getJpyBal( bet * (parentMultiplier - 10 * num) ) + "円§f§lです");
            }
            vault.deposit(parent,bet * parentMultiplier);
            Bukkit.broadcastMessage(prefix + "§a§lゲームが終了しました");
        }, 40);
    }

    public void sendMessage(String message){
        for(Player p : list){
            p.sendMessage(prefix + message);
        }
    }

    public void sendErrorMessage(Player p, String message) {
        p.sendMessage(prefix + message);
        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.2f);
    }

}

