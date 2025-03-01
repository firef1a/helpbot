package dev.fire.chat;

import com.google.gson.*;
import dev.fire.FileManager;
import dev.fire.Mod;
import net.minecraft.text.Text;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Random;

import static java.util.Map.entry;


public class ResponseManager {
    public static String prefix = "!";

    private static final String balance_file = Mod.MOD_ID + "_balances.json";
    private static final int max_amount = 100000;

    private Map<String, Integer> balances;
    private ArrayList<String> responses;

    public ResponseManager() {
        responses = new ArrayList<>();
        readPlayerData();
    }

    private int getBankBalance() { return balances.getOrDefault("bank", 0); }
    private void setBankBalance(int val) { balances.put("bank", val); }


    private void readPlayerData() {
        balances = new HashMap<>();
        try {
            JsonObject object = new JsonParser().parse(FileManager.readFile(balance_file)).getAsJsonObject();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();


            object.keySet().forEach(key -> {
                try {
                    JsonElement element = object.get(key);
                    Integer value = gson.fromJson(element, Integer.class);
                    balances.put(key, value);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });

        } catch (Exception exception) {
            Mod.LOGGER.info("Config didn't load: " + exception);
            Mod.LOGGER.info("Making a new one.");
            save();
        }
    }

    public void save() {
        try {
            JsonObject object = new JsonObject();

            for (Map.Entry<String, Integer> entry : balances.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();

                object.addProperty(key, value);
            }
            FileManager.writeFile(balance_file, object.toString());
        } catch (Exception e) {
            Mod.LOGGER.info("Couldn't save file: " + e);
        }
    }

    public Text process(Text text) {
        String content = text.getString();
        command(content);
        return text;
    }
    public void command(String content){
        Matcher msgMatch;

        msgMatch = Pattern.compile("^\\[(.*) → You] (.*)", Pattern.CASE_INSENSITIVE).matcher(content);
        if (msgMatch.find()) {
            String player_name = msgMatch.group(1);
            String message = msgMatch.group(2);
            Mod.consoleLog("player messaged bot: " + message);

            Matcher cmd_matcher;
            cmd_matcher = Pattern.compile("!balance", Pattern.CASE_INSENSITIVE).matcher(message);

            if (cmd_matcher.find()){
                int balance = balances.getOrDefault(player_name, 0);
                respond(player_name, "Your balance: " + balance + " coins.");
            }

            cmd_matcher = Pattern.compile("!withdraw (.*)", Pattern.CASE_INSENSITIVE).matcher(message);
            if (cmd_matcher.find()){
                try {
                    int balance = balances.getOrDefault(player_name, 0);
                    int amount = 0;
                    String group = cmd_matcher.group(1);
                    if (group.equals("all")) {
                        if (balance > 0) {
                            amount = balance;
                        } else {
                            respond(player_name, "lmfao broke");
                            return;
                        }
                    } else {
                        amount = Integer.parseInt(group);
                    }

                    if (amount > 0) {
                        if (balance >= amount) {
                            if (amount <= getBankBalance()) {
                                int new_balance = balance - amount;
                                ChatUtils.sendMessageAsPlayer("@pay " + player_name + " " + amount);
                                //respond(player_name, "Your balance is now " + new_balance + " coins.");
                                balances.put(player_name, new_balance);
                                setBankBalance(getBankBalance()-amount);
                            } else {
                                respond(player_name, "Unable to withdraw coins, bank has insufficient fund (" + getBankBalance() + " coins)");
                            }
                        } else {
                            respond(player_name, "Can't withdraw " + amount +" coins! (Your balance: "+ balance + ")");
                        }

                    } else {
                        respond(player_name, "Invalid withdraw amount!");
                    }
                } catch (Exception e) {
                    respond(player_name, "Error occurred while parsing amount, nothing was withdrawn.");
                }
            }

            cmd_matcher = Pattern.compile("!gamble (.*)", Pattern.CASE_INSENSITIVE).matcher(message);
            if (cmd_matcher.find()){
                try {
                    int balance = balances.getOrDefault(player_name, 0);
                    int amount = 0;
                    String group = cmd_matcher.group(1);
                    if (group.equals("all")) {
                        if (balance >= 10) {
                            amount = balance;
                        } else {
                            respond(player_name, "lmfao broke");
                            return;
                        }
                    } else {
                        amount = Integer.parseInt(group);
                    }

                    if (amount >= 10) {

                        if (balance >= amount) {

                            if (amount <= max_amount) {
                                int r = (int) (Math.random()*2);
                                if (r == 1) r = 2;

                                //double kr = (double) ((int) (r * 100)) / 100;
                                int gamble = (int) (amount * r);

                                int new_balance = (balance - amount) + gamble;
                                int gamble_diff = gamble - amount;
                                String gamble_text = String.valueOf(gamble_diff);
                                if (gamble_diff > 0) gamble_text = "+" + gamble_text;
                                respond(player_name, r + "x → " + gamble_text + " coins! (New balance: "+ new_balance + ")");
                                balances.put(player_name, new_balance);
                            } else {
                                respond(player_name, "Your wager of " + amount + " exceeds the maximum wager limit of " + max_amount + ".");
                            }
                        } else {
                            respond(player_name, "Insufficient funds! (Your balance: "+ balance + ")");
                        }

                    } else {
                        respond(player_name, "Invalid amount, minimum is 10 coins.");
                    }
                } catch (Exception e) {
                    respond(player_name, "Error occurred while parsing amount, no gambling occurred :(");
                }
            }

            cmd_matcher = Pattern.compile("!help", Pattern.CASE_INSENSITIVE).matcher(message);
            if (cmd_matcher.find()){
                respond(player_name, "To get started, @pay sitebot <amount> to deposit coins, use !balance to check your balance, and use !gamble <amount> to coin toss! Then use !withdraw <amount> to retrieve your coins!");
            }
        }

        msgMatch = Pattern.compile("^You have received (.\\d*) money from (.*)\\.", Pattern.CASE_INSENSITIVE).matcher(content);
        if (msgMatch.find()){
            int amount = Integer.parseInt(msgMatch.group(1));
            String sender_name = msgMatch.group(2);

            int balance = balances.getOrDefault(sender_name, 0);
            balance += amount;
            balances.put(sender_name, balance);
            Mod.consoleLog(balances.toString());

            String s = "s";
            if (amount == 1) s = "";
            respond(sender_name, "Deposited " + amount + " coin" + s + ", your balance is now " + balance + ".");
            setBankBalance(getBankBalance()+amount);
        }
    }

    private void respond(String name, String response) {
        responses.add("/msg " + name + " [!] › " + response);
    }

    public void sendResponseQueue() {
        Mod.consoleLog("check queue");
        if (!responses.isEmpty()) {
            String msg = responses.removeFirst();
            ChatUtils.sendMessageAsPlayer(msg);

        }

    }

    public void saveFile() {
        this.save();
    }

}
