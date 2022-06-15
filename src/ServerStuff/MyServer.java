package ServerStuff;

import Calcs.Utils;
import Calcs.Vector2D;
import ClientStuff.FightGUI;
import ClientStuff.Keys;
import ClientStuff.Player;
import ClientStuff.TextEvent;
import Envir.World;
import InGame.Ball;
import InGame.Item;
import InGame.Pokemon;
import InGame.Potion;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MyServer {

//    Todo setonNextMsg(Consumer) und setonNextMsgOnly(Consumer)

    public static Server getServer() {
        return server;
    }

    /**
     * the server for the connection
     */
    private static Server server;

    public static void main(String[] args) throws IOException, SQLException {
        Database.init();
        Item.init();
        Pokemon.init(false);
        initServer();
    }

    /**
     * starts the server
     */
    private static void initServer() throws IOException {
        server = new Server(33333, "localhost", 5);
        server.setAcceptAll(true);
        server.startUpdates();
        server.setOnConnect(true, a -> a.send(MessageType.toStr(MessageType.hellman) + a.getCrypto()));
        server.setOnMessage(true, (c, msg) -> {
            if (msg instanceof String s && !s.startsWith(MessageType.toStr(MessageType.keysPres)))
                System.out.printf("From %d: \"%s\"\n", c.getId(), msg);
        });
        server.setOnMessage(true, (c, msg) -> {
            if (msg instanceof String s && !s.isBlank() && s.length() >= 3) {
                MessageType mT = MessageType.getType(Pattern.matches("[0-9]{3}", s.substring(0, 3)) ? Integer.parseInt(s.substring(0, 3)) : 999);
                switch (mT) {
                    case hellman -> doHellman(c, s);
                    case register -> doRegister(c, s);
                    case login -> doLogin(c, s);
                    case logout -> c.setUsername(null);
                    case delete -> doDel(c, s);
                    case profile -> doProfile(c, s.substring(MessageType.toStr(MessageType.badgeRequest).length()));
                    case worldSelect -> doRegion(c, s);
                    case keysPres -> doKeys(c, s.substring(MessageType.toStr(MessageType.badgeRequest).length()));
                    case textEvent -> doTextEvents(c, s.substring(MessageType.toStr(MessageType.badgeRequest).length()));
                    case itemBuy -> doItemBuy(c, s.substring(MessageType.toStr(MessageType.badgeRequest).length()));
                    case inFightChoice -> doInFightChoice(c, s.substring(MessageType.toStr(MessageType.badgeRequest).length()));
                    //TODO Clemenzzzzzz zB wenn Client sagt, ich moechte angreifen, dann kommt das hier hin (on Message halt)
                    case error -> System.out.println("ERROR-Message: " + s);
                }
            }
        });
        Random r = new Random();
        for (int i = 0; i < 5; i++) server.getWorlds().add(new World((int) (69420 + r.nextDouble() * 100000), "" + i));
        server.getWorlds().add(new World(696969, "K"));
    }

    private static void doInFightChoice(Server.ClientHandler c, String msg) {
        if (c.getOtherClient() != null) {
            synchronized (c.getOtherClient().getPlayer()) {
                if (c.getOtherClient().getPlayer().getMsgForFightWaiting() == null) {
                    c.getPlayer().setMsgForFightWaiting(msg);
                } else {
                    System.out.println("MyServer.doInFightChoice: " + "now starting fight");
                }
            }
        } else {
            updateFightAgainstPoke(c, msg);
        }
    }


    private static void updateFightAgainstPoke(Server.ClientHandler c, String msg) {
        String[] s = msg.split(",");
        FightGUI.FightChoice choice = FightGUI.FightChoice.valueOf(s[0]);
        StringBuilder sToSend = new StringBuilder(MessageType.toStr(MessageType.inFightUpdate));
        final Player player = c.getPlayer();
        switch (choice) {
            case Surrender -> {
                c.setOtherPoke(null);
                synchronized (player) {
                    player.setActivity(Player.Activity.standing);
                }
                sendPosUpdate(c);
                sToSend.append(choice).append("-|-").append(0).append("-|-").append(".|.");
//                c.send(sToSend.toString());
//                c.send(MessageType.toStr(MessageType.inFightUpdate) + choice + "-|-" + 0);
//                return;
            }
            case Switch -> {
                synchronized (player) {
                    Utils.switchObjects(player.getPoke(), Utils.toInt(s[1]));
                }
                sToSend.append(choice).append("-|-").append(0).append("-|-").append(s[1]).append("._.");
            }
            case Item -> {
                synchronized (player) {
                    int itemID = Utils.toInt(s[1]);
                    if (player.getItems().get(itemID) != null && player.getItems().get(itemID) > 0) {
                        player.getItems().put(itemID, player.getItems().get(itemID) - 1);
                        sToSend.append(choice).append("-|-").append(0).append("-|-").append(itemID).append("-|-");
//                    append(useItemAndGetToSendString(itemID, player, c.getOtherPoke(), c))
                        Item it = Item.getItem(itemID);
                        if (it instanceof Potion potion) {
                            player.getPoke().get(0).heal(potion.getHealQuantity());
                            sToSend.append(player.getPoke().get(0).getCurHP());
                        } else if (it instanceof Ball b) {
                           /* if (player.getPoke().size() < 6) {
                                sToSend.append("t");// stands for "too many pokemon already"
                            } else */
                            if (c.getOtherPoke().getsCaptured(b)) {
                                player.setActivity(Player.Activity.standing);
                                player.getPoke().add(c.getOtherPoke());
                                c.setOtherPoke(null);
                                sendPosUpdate(c);
                                sToSend.append("c"); // stands for "captured"
                            } else {
                                sToSend.append("f"); // stands for "failed to capture"
                            }
                        } else {
                            sToSend.append("ERROR");

                        }
                        sToSend.append("._.");
                    }
                }
            }
        }
        c.send(sToSend.toString());
    }

    /**
     * what happens when a player tries to buy an item
     *
     * @param c the client where the player is from
     * @param s the message from the client
     */
    private static void doItemBuy(Server.ClientHandler c, String s) {
        String[] strs = s.split(",");
        Item it = Item.getItem(Integer.parseInt(strs[1].trim()));
        int amount = Integer.parseInt(strs[2].trim());
        synchronized (c.getPlayer()) {
            long curMoney = c.getPlayer().getMoney();
            if (curMoney >= (long) amount * it.getPrize()) {
                c.getPlayer().setMoney(curMoney - (long) amount * it.getPrize());
                c.getPlayer().getItems().putIfAbsent(it.getId(), 0);
                c.getPlayer().getItems().put(it.getId(), c.getPlayer().getItems().get(it.getId()) + amount);
            }
        }
    }

    /**
     * handels what happens when the player gives information about the textFields
     *
     * @param c the client where the player is from
     * @param s the message from the client
     */
    private static void doTextEvents(Server.ClientHandler c, String s) {
        if (s.startsWith("0")) {
            s = s.substring(1);
            if (s.split(";").length == 1) {
                int val = Integer.parseInt(s);
                if (val < 100) {
                    c.setTimeTillNextTextField(System.currentTimeMillis() + 500);
                    c.getPlayer().setActivity(Player.Activity.standing);
                }
            }
            //
        } else if (s.startsWith("1")) {
            s = s.substring(1);
            if (s.charAt(0) == '1') {
                System.out.println("MyServer.doTextEvents: " + c.getOtherClient().getUsername());
                c.getOtherClient().send(MessageType.toStr(MessageType.textEvent) + 1 + TextEvent.TextEventIDsTranslator.PlayersMeetDeclineFight.getId() + ",name:" + c.getUsername() + " has");
                c.getOtherClient().setOtherClient(null);
                c.setOtherClient(null);
                System.out.println("now sending");
            } else {
                System.out.println("Der Kampf wurde accepted");
//       a         StringBuilder sToSendThis = new StringBuilder(MessageType.toStr(MessageType.fightData));
//                StringBuilder sToSendOther = new StringBuilder(MessageType.toStr(MessageType.fightData));
                synchronized (c.getPlayer()) {
                    synchronized (c.getOtherClient().getPlayer()) {
                        Player otherP = c.getOtherClient().getPlayer();
                        System.out.println(c.getOtherClient().getOtherClient());
                        c.getPlayer().sendItemData(c);
                        otherP.sendItemData(c.getOtherClient());

                        c.getPlayer().setActivity(Player.Activity.fight);
                        otherP.setActivity(Player.Activity.fight);

                        otherP.sendPokeData(c.getOtherClient(), c.getPlayer().getPoke().get(0));
                        c.getPlayer().sendPokeData(c, otherP.getPoke().get(0));
/*//              a          sToSendThis.append(otherP.getPoke().get(0).toMsg());
//                        sToSendOther.append(c.getPlayer().getPoke().get(0).toMsg());

//                        otherP.getPoke().forEach(a -> sToSendOther.append("$").append(a.toMsg()));
//                        c.getPlayer().getPoke().forEach(a -> sToSendThis.append("$").append(a.toMsg()));*/
                    }
                }
                //TODO something here, also alle Daten senden an beide (Pokemon und so)
/*//                System.out.println(sToSendThis);
//                System.out.println(sToSendOther);*/
            }
        }
    }

    /**
     * what should happen when a player tries to login
     *
     * @param c the client where the player is from
     * @param s the message from the client
     */
    private static void doProfile(Server.ClientHandler c, String s) {
        try {
            System.out.println("MyServer.doProfile: " + s.charAt(1));
            ResultSet exists = Database.get("select count(*) as nbr from User inner join Player P on User.PK_User_ID = P.FK_User_ID where P.startPokID = " + s.charAt(1) + " && User.name = '" + c.getUsername() + "';");
            if (exists != null && exists.next() && !(exists.getInt("nbr") > 0)) {
                Database.execute("insert into player (skinID, startPokID, FK_User_ID, language) VALUE (0," + s.charAt(1) + ",(select PK_User_ID from User where name='" + c.getUsername() + "'),'eng');");
                System.out.println("add Pokemon to this new Player");
            }
            String statement = "select * from User inner join Player P on User.PK_User_ID = P.FK_User_ID where P.startPokID = " + s.charAt(1) + " && User.name = '" + c.getUsername() + "';";
            System.out.println(statement);
            ResultSet data = Database.get(statement);
            if (data != null && data.next()) {
                c.setPlayer(initPlayer(c.getUsername(), data.getInt("PK_Player_ID")));
//                System.out.println("Player initialized: " + data.getInt("PK_Player_ID"));
            }
        } catch (SQLException ignored) {
        }
    }

    private static void updateDatabase(Player p) {
        Database.execute("delete from Pokemon WHERE FK_Player_ID = " + p.getIdForDB() + ";");
        for (int i = 0; i < p.getPoke().size(); i++) {
            Database.execute("insert into Pokemon (Message, FK_Player_ID) VALUES ('" + p.getPoke().get(i).toMsg() + "'," + p.getIdForDB() + ");");
        }

        Database.execute("delete from MyPosition WHERE FK_PK_Player_ID = " + p.getIdForDB() + ";");
        Database.execute("insert into MyPosition (FK_PK_Player_ID, FK_PK_World_ID, posX, posY) VALUES ("
                + p.getIdForDB() + "," + p.getWorld() + "," + p.getPos().getX() + "," + p.getPos().getY() + ");");
    }

    private static void getFromDatabase(Player p){
        //für postion beim world select, pokemon sind schon bei doProfile
    }

    /**
     * @param c the client where the player is from
     * @param s the message from the client
     */
    private static void doKeys(Server.ClientHandler c, String s) {
        String keys = s.replaceAll(",.*", "");
        String dir = s.replaceAll(".*,", "");

        Optional<Player.Dir> p = Arrays.stream(Player.Dir.values()).filter(a -> dir.equals("" + a.ordinal())).findFirst();

        synchronized (c.getPlayer()) {
            if (c.getPlayer().getActivity() == Player.Activity.standing) {
                p.ifPresent(a -> c.getPlayer().setDir(a));
            }
        }
        synchronized (c.getKeysPressed()) {
            c.getKeysPressed().clear();
            c.getKeysPressed().addAll(Keys.getKeysFromString(keys).stream().filter(e -> !c.getKeysPressed().contains(e)).collect(Collectors.toList()));
        }
    }

    /**
     * @param c the client where the player is from
     * @param s the message from the client
     */
    private static void doRegion(Server.ClientHandler c, String s) {
        Matcher mRegion = Pattern.compile("n='(.*?)'[,}]").matcher(s);
        String worldName = mRegion.find() ? mRegion.group(1) : "";
        Optional<World> w = server.getWorlds().stream().filter(e -> e.getName().equals(worldName)).findFirst();
        if (w.isPresent()) {
            c.getPlayer().setWorld(worldName);
            c.send(MessageType.toStr(MessageType.worldSelect) + 0 + w.get().getSeed() + "," + c.getUsername());
            sendPosUpdate(c);
            update(c);
        } else {
            if (c.getUsername().equals(worldName)) {
                server.getWorlds().add(new World(worldName.hashCode(), worldName));
                c.getPlayer().setWorld(worldName);
                c.send(MessageType.toStr(MessageType.worldSelect) + 0 + worldName.hashCode() + "," + worldName);
                sendPosUpdate(c);
                update(c);
            } else {
                ResultSet nbr = Database.get("select * from World inner join User U on World.FK_User_ID = U.PK_User_ID where U.name='" + worldName + "';");
                try {
                    if (nbr != null && nbr.first()) {
                        server.getWorlds().add(new World(worldName.hashCode(), worldName));
                        c.getPlayer().setWorld(worldName);
                        c.send(MessageType.toStr(MessageType.worldSelect) + 0 + nbr.getInt("seed") + "," + c.getUsername());
                        sendPosUpdate(c);
                        update(c);
                    } else {
                        c.send(MessageType.toStr(MessageType.worldSelect) + 1);
                    }
                } catch (SQLException ignored) {
                    c.send(MessageType.toStr(MessageType.worldSelect) + 1);
                }
            }
        }
        System.out.println("MyServer.doRegion: " + c.getPlayer().getPos());
    }

    /**
     * @param c which clientHandler should be updated
     */
    private static void update(Server.ClientHandler c) {

        server.setOnUpdate(false, client -> {
            synchronized (client.getKeysPressed()) {
                Vector2D tar = Vector2D.add(client.getPlayer().getPos(), Player.Dir.getDirFromKeys(client.getKeysPressed()));
                client.getPlayer().setTargetPos(tar);
                Optional<World> w = server.getWorlds().stream().filter(e -> e.getName().equals(c.getPlayer().getWorld())).findFirst();
                w.ifPresent(world -> {
                    if (client.getPlayer().getActivity() != Player.Activity.fight) {
                        client.getPlayer().updatePos(client, client.getKeysPressed().contains(Keys.decline), world);
                        client.getPlayer().updateTextEvents(client, client.getKeysPressed(), world, server.getClients());
                        client.getPlayer().checkToStartFightInGrass(client, w.get());
                    }
                    if (client.getPlayer().getActivity() == Player.Activity.textEvent || client.getPlayer().getActivity() == Player.Activity.fight)
                        client.getKeysPressed().clear();
                });
            }
            sendPosUpdate(client);
//            System.out.println("MyServer.update: " + client.getPlayer().getActivity());
        }, (int) c.getId());
    }

    /**
     * inits a new player
     *
     * @param name    the name of the player/email
     * @param idForDB the id from the database
     * @return the player object with all the information
     */
    private static Player initPlayer(String name, int idForDB) {
        String statement = "select * from Player where PK_Player_ID=" + idForDB;
        ResultSet curPlayer = Database.get(statement);

        Vector2D pos = new Vector2D();
        int skinID = 0;
        long money = 0;
        int idFromPlayer = 0;
        try {
            if (curPlayer == null) throw new SQLException();
            if (curPlayer.first()) {
//                ResultSet curPos = Database.getItem("select MP.* from Player join MyPosition MP on Player.PK_Player_ID = MP.FK_PK_Player_ID join World W on W.PK_World_ID = MP.FK_PK_World_ID ");
                skinID = (int) curPlayer.getObject("skinID");
                money = (int) curPlayer.getObject("money");
                idFromPlayer = (int) curPlayer.getObject("startPokID");
                pos.setX((Integer) curPlayer.getObject("posX"));
                pos.setY((Integer) curPlayer.getObject("posY"));
            } else throw new SQLException();
        } catch (SQLException ignored) {
        }
        Player p = new Player(name, pos, skinID, idFromPlayer, idForDB, money);

        try {
            assert curPlayer != null;
            ResultSet itemsInDB = Database.get("select user.name, Item_ID, quantity from user inner join Player P on User.PK_User_ID = P.FK_User_ID inner join ItemToPlayer ITP on P.PK_Player_ID = ITP.FK_Player where PK_Player_ID =" + curPlayer.getObject("PK_Player_ID") + ";");
            assert itemsInDB != null;
            while (itemsInDB.next()) {
                p.getItems().put(itemsInDB.getInt("Item_ID"), itemsInDB.getInt("quantity"));
            }
            System.out.println(p.getItems());
        } catch (SQLException ignored) {
        }

        if (p.getPoke().size() == 0) {
            System.out.println("MyServer.initPlayer: " + "starter created");
            System.out.println("MyServer.initPlayer: " + idFromPlayer);
            p.getPoke().add(Pokemon.createStarter(idFromPlayer));
            p.getPoke().add(Pokemon.createPokemon(new Vector2D(100, 7666), World.Block.Water));
            p.getPoke().add(Pokemon.createPokemon(new Vector2D(100, 420), World.Block.Grass));
            p.getPoke().get(0).setCurHP(10);
            p.getPoke().get(1).setCurHP(10);
        } else System.out.println("MyServer.initPlayer: " + p.getPoke());
        return p;
    }

    /**
     * sends an update to the player about the positions for all the other players in the area
     *
     * @param c the client where the player is from
     */
    private static void sendPosUpdate(Server.ClientHandler c) {
        int loadingAreaWidth = 28;
        List<Player> all = new ArrayList<>(server.getClients().values())
                .parallelStream()
                .filter(Objects::nonNull)
                .map(Server.ClientHandler::getPlayer)
                .filter(e -> e != null && c.getPlayer().getWorld().equals(e.getWorld()))
                .filter(e -> Math.abs(e.getPos().getX() - c.getPlayer().getPos().getX()) < loadingAreaWidth)
                .filter(e -> Math.abs(e.getPos().getY() - c.getPlayer().getPos().getY()) < loadingAreaWidth)
                .filter(e -> (c.getPlayer().getHouseEntrancePos() == null ? e.getHouseEntrancePos() == null : c.getPlayer().getHouseEntrancePos().equals(e.getHouseEntrancePos())))
                .collect(Collectors.toList());
        StringBuilder str = new StringBuilder();
        all.forEach(e -> str.append('{').append(e.getName()).append(',').append(e.getPos().getX() + e.getCurWalked().getX()).append(',').append(e.getPos().getY() + e.getCurWalked().getY()).append(',').append(e.getSkin()).append('}'));
        c.send(MessageType.toStr(MessageType.updatePos) + str);
    }

    /**
     * when a user tries to delete its account
     *
     * @param c the client where the player is from
     * @param s the message from the client
     */
    private static void doDel(Server.ClientHandler c, String s) {
        Matcher mName = Pattern.compile("name='(.*?)'[,}]").matcher(s);
        Matcher mPwd = Pattern.compile("pwd='(.*?)'[,}]").matcher(s);
        String name = mName.find() ? mName.group(1) : "";
        String pwd = c.getCrypto().decrypt(mPwd.find() ? mPwd.group(1) : "");
        int error = User.delete(name, pwd);
        if (error == 0) c.setUsername(null);
        c.send(MessageType.toStr(MessageType.delete) + error);
        System.out.println("User-Delete request received: " + name);
    }

    /**
     * when a User tries to Login
     *
     * @param c the client where the player is from
     * @param s the message from the client
     */
    private static void doLogin(Server.ClientHandler c, String s) {
        Matcher mName = Pattern.compile("name='(.*?)'[,}]").matcher(s);
        Matcher mPwd = Pattern.compile("pwd='(.*?)'[,}]").matcher(s);
        String name = mName.find() ? mName.group(1) : "";
        String pwd = c.getCrypto().decrypt(mPwd.find() ? mPwd.group(1) : "");
        if (server.getClients().values().stream().anyMatch(e -> e != null && name.equals(e.getUsername()))) {
            c.send(MessageType.toStr(MessageType.login) + 3);
        } else {
            int error = User.isCorrect(name, pwd);
            c.send(MessageType.toStr(MessageType.login) + error);
            if (error == 0) sendPlayerProfiles(c, name);
            System.out.println("Login request received: " + name);
        }
    }

    /**
     * sending the client all data about the playerProfiles
     *
     * @param c    the client where the player is from
     * @param name the name of the player
     */
    private static void sendPlayerProfiles(Server.ClientHandler c, String name) {
        c.setUsername(name);
        ResultSet r = Database.get("select PK_Player_ID,skinID,startPokID,language from Player INNER JOIN User U on Player.FK_User_ID = U.PK_User_ID where U.name='" + name + "' OR email='" + name + "';");
        if (r != null) {
            StringBuilder msgToSend = new StringBuilder(MessageType.toStr(MessageType.profile) + '0');
            HashMap<Integer, String> hMap = new HashMap<>();
            try {
                while (r.next()) {
                    String msgToSendSingle = "{";
                    ResultSet badges = Database.get("select  count(PK_Badge_ID) as nbr from Badge inner join Player P on Badge.FK_Player_ID = P.PK_Player_ID where P.PK_Player_ID = " + r.getObject("PK_Player_ID") + ";");
                    if (badges != null && badges.next()) msgToSendSingle += badges.getString("nbr") + ",";
                    ResultSet poke = Database.get("select count(PK_Poke_ID) as nbr from Pokemon inner join Player P on Pokemon.FK_Player_ID = P.PK_Player_ID where P.PK_Player_ID=" + r.getObject("PK_Player_ID") + ";");
                    if (poke != null && poke.next()) msgToSendSingle += poke.getString("nbr") + "}";
                    hMap.put(r.getInt("startPokID"), msgToSendSingle);
                }
            } catch (SQLException ignored) {
            }
            for (int i = 0; i < 3; i++) msgToSend.append(hMap.get(i) == null ? "{}" : hMap.get(i));
            c.send(msgToSend.toString());
        }
    }

    /**
     * when a user tries to register
     *
     * @param c the client where the player is from
     * @param s the message from the client
     */
    private static void doRegister(Server.ClientHandler c, String s) {
        Matcher mName = Pattern.compile("name='(.*?)'[,}]").matcher(s);
        Matcher mPwd = Pattern.compile("pwd='(.*?)'[,}]").matcher(s);
        Matcher mEmail = Pattern.compile("email='(.*?)'[,}]").matcher(s);
        String name = mName.find() ? mName.group(1) : "";
        String pwd = c.getCrypto().decrypt(mPwd.find() ? mPwd.group(1) : "");
        String email = mEmail.find() ? mEmail.group(1) : "";
        String error = User.add(name, pwd, email);
        if (error == null) {
            c.send(MessageType.toStr(MessageType.register) + "0");
            c.setUsername(name);
        } else {
            c.setUsername(null);
            if (error.contains("Password")) {
                c.send(MessageType.toStr(MessageType.register) + "3");
            } else if (error.contains("Duplicate")) {
                if (error.contains("'name")) c.send("0015");
                else c.send(MessageType.toStr(MessageType.register) + "6");
            } else {
                Matcher m = Pattern.compile("CONSTRAINT_([0-9]+)").matcher(error);
                c.send(MessageType.toStr(MessageType.register) + (m.find() ? m.group(1) : "9"));
            }
        }
    }

    /**
     * for the encryption for the password
     *
     * @param c the client where the player is from
     * @param s the message from the client
     */
    private static void doHellman(Server.ClientHandler c, String s) {
        Matcher m = Pattern.compile("pub=([0-9]+)[,}]").matcher(s);
        c.getCrypto().createKey(new BigInteger(m.find() ? m.group(1) : "1"), c.getCrypto().getPub().intValue());
    }
}