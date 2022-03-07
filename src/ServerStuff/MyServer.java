package ServerStuff;

import Calcs.Vector2D;
import ClientStuff.Keys;
import ClientStuff.Player;
import Envir.World;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MyServer {

//    Todo setonNextMsg(Consumer) und setonNextMsgOnly(Consumer)

    public static Server getServer() {
        return server;
    }

    private static Server server;

    public static void main(String[] args) throws IOException, SQLException {
        Database.init();
        initServer();
    }

private static void initServer() throws IOException {
        server = new Server(33333, "localhost", 5);
        server.setAcceptAll(true);
        server.startUpdates();
//        server.setOnConnect(true, c -> c.send(7708));
        server.setOnConnect(true, a -> a.send(MessageType.toStr(MessageType.hellman) + a.getCrypto()));
        server.setOnMessage(true, (c, msg) -> {
            if (msg instanceof String s && !s.startsWith(MessageType.toStr(MessageType.keysPres))) {
                System.out.printf("From %d: \"%s\"\n", c.getId(), msg);
            }
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
                    case region -> doRegion(c, s);
                    case keysPres -> doKeys(c, s.substring(3));
                    case error -> System.out.println("ERROR-Message: " + s);
                }
            }
        });
        server.getRegions().add(new World(696969, "Kanto"));
        server.getRegions().add(new World(187420, "Johto"));

    }

    private static void doKeys(Server.ClientHandler c, String s) {
        synchronized (c.getKeysPressed()) {
            c.getKeysPressed().clear();
            c.getKeysPressed().addAll(Keys.getKeysFromString(s).stream().filter(e -> !c.getKeysPressed().contains(e)).collect(Collectors.toList()));
        }
    }

    private static void doRegion(Server.ClientHandler c, String s) {
        Matcher mRegion = Pattern.compile("name='(.*?)'[,}]").matcher(s);
        String region = mRegion.find() ? mRegion.group(1) : "";
        Optional<World> w = server.getRegions().stream().filter(e -> e.getName().equals(region)).findFirst();
        if (w.isPresent()) {
            c.send(MessageType.toStr(MessageType.region) + 0 + w.get().getSeed() + "," + c.getUsername());
            c.setPlayer(initPlayer(c.getUsername(), region));
            sendPosUpdate(c);
            update(c);
        } else {
            c.send(MessageType.toStr(MessageType.region) + 1);
        }
    }

    private static void update(Server.ClientHandler c) {
        server.setOnUpdate(false, client -> {
            synchronized (client.getKeysPressed()) {
                Vector2D tar = Vector2D.add(client.getPlayer().getPos(), Player.Dir.getDirFromKeys(client.getKeysPressed()));
                client.getPlayer().setTargetPos(tar);
                Optional<World> w = server.getRegions().stream().filter(e -> e.getName().equals(c.getPlayer().getRegion())).findFirst();
                w.ifPresent(world -> client.getPlayer().updatePos(client, client.getKeysPressed().contains(Keys.decline), world));
            }
            sendPosUpdate(client);
        }, (int) c.getId());
    }

    private static Player initPlayer(String name, String region) {
        ResultSet curPlayer = Database.get("select * from User inner join Player P on User.PK_User_ID = FK_User_ID where name='" + name + "' OR email='" + name + "';");
        Vector2D pos = new Vector2D();
        int skinID = 0;
        try {
            if (curPlayer == null) throw new SQLException();
            if (curPlayer.first()) {
                pos.setX((Integer) curPlayer.getObject("posX"));
                pos.setY((Integer) curPlayer.getObject("posY"));
                skinID = (int) curPlayer.getObject("skinID");
            } else throw new SQLException();
        } catch (SQLException ignored) {
        }
        return new Player(name, pos, skinID, region);
    }

    private static void sendPosUpdate(Server.ClientHandler c) {
        int loadingAreaWidth = 28;
        List<Player> all = new ArrayList<>(server.getClients().values()).parallelStream().filter(Objects::nonNull).map(Server.ClientHandler::getPlayer).filter(e -> e != null && e.getRegion().equals(c.getPlayer().getRegion()) && Math.abs(e.getPos().getX() - c.getPlayer().getPos().getX()) < loadingAreaWidth && Math.abs(e.getPos().getY() - c.getPlayer().getPos().getY()) < loadingAreaWidth && (c.getPlayer().getHouseEntrancePos() == null ? e.getHouseEntrancePos() == null : c.getPlayer().getHouseEntrancePos().equals(e.getHouseEntrancePos()))).collect(Collectors.toList());
        StringBuilder str = new StringBuilder();
        all.forEach(e -> str.append('{').append(e.getName()).append(',').append(e.getPos().getX() + e.getCurWalked().getX()).append(',').append(e.getPos().getY() + e.getCurWalked().getY()).append(',').append(e.getSkin()).append('}'));
        c.send(MessageType.toStr(MessageType.updatePos) + str);
    }

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

    private static void doLogin(Server.ClientHandler c, String s) {
        Matcher mName = Pattern.compile("name='(.*?)'[,}]").matcher(s);
        Matcher mPwd = Pattern.compile("pwd='(.*?)'[,}]").matcher(s);
        String name = mName.find() ? mName.group(1) : "";
        String pwd = c.getCrypto().decrypt(mPwd.find() ? mPwd.group(1) : "");
        if (server.getClients().values().stream().anyMatch(e -> e != null && name.equals(e.getUsername()))) {
            c.send(MessageType.toStr(MessageType.login) + 3);
        } else {
            int error = User.isCorrect(name, pwd);
            if (error == 0) c.setUsername(name);
            c.send(MessageType.toStr(MessageType.login) + error);
            System.out.println("Login request received: " + name);
        }
    }

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

    private static void doHellman(Server.ClientHandler c, String s) {
        Matcher m = Pattern.compile("pub=([0-9]+)[,}]").matcher(s);
        c.getCrypto().createKey(new BigInteger(m.find() ? m.group(1) : "1"), c.getCrypto().getPub().intValue());
    }
}