package ClientStuff;

import Calcs.Crypto;
import Calcs.Vector2D;
import Envir.House;
import Envir.World;
import ServerStuff.MessageType;
import ServerStuff.User;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//--module-path "C:\Program Files\Java\openjfx-16_windows-x64_bin-sdk\javafx-sdk-16\lib" --add-modules javafx.controls,javafx.fxml
public class MyClient extends Application {

    private Client client;

    private Stage stage;

    private long timeTillClosedMenu = 0;

    private final Map<String, Image> allImgs = new HashMap<>();

    private final AnimationTimer animationTimer = new AnimationTimer() {
        /**
         * min Time which needs to Pass till the animationTimer is called the next time
         */
        private long timeTillNextUse = 0;

        private int count = 0;

        List<Keys> lastKeysPressed;


        @Override
        public void handle(long l) {
            if (System.currentTimeMillis() > timeTillNextUse) {
                timeTillNextUse = System.currentTimeMillis() + 20;
                Canvas c = (Canvas) (stage.getScene().getRoot().getChildrenUnmodifiable().get(0));
                c.setWidth(stage.getScene().getWidth());
                c.setHeight(stage.getScene().getHeight());
                synchronized (client.getPlayers()) {
                    if (client.getPlayers().get(0).getHouseEntrancePos() == null) {
                        client.getWorld().drawEnvir(c, client.getPlayers(), new Vector2D(stage.getScene().getWidth(), stage.getScene().getHeight()), allImgs);
                    } else {
                        client.getWorld().drawInsideHouse(c, client.getPlayers(), new Vector2D(stage.getScene().getWidth(), stage.getScene().getHeight()), allImgs);
                    }
                    List<Keys> keys = (Keys.getSmartKeys(keysPressed));
                    if (keys.contains(Keys.menu) && client.getPlayers().get(0).getActivity() != Player.Activity.menu) {
                        if (timeTillClosedMenu < System.currentTimeMillis()) {
                            client.getPlayers().get(0).setActivity(Player.Activity.menu);
                            client.send(MessageType.toStr(MessageType.keysPres));
                            showMenu();
                        }
                    }
                    if (((count++) & 0b11) == 0) {
                        client.getPlayers().forEach(Player::updateHands);
                        if (client.getPlayers().get(0).getActivity() == Player.Activity.moving || client.getPlayers().get(0).getActivity() == Player.Activity.standing) {
                            StringBuilder res = new StringBuilder();
                            keys = getUpdatedKeysToSendAndUpdatePlayerDir(lastKeysPressed, keys, client.getPlayers().get(0));
                            keys.stream().forEach(e -> res.append(e.ordinal()));
                            client.send(MessageType.toStr(MessageType.keysPres) + res);
                        } else if (client.getPlayers().get(0).getActivity() == Player.Activity.menu) {
                            Pane p = (Pane) stage.getScene().getRoot().getChildrenUnmodifiable().get(1);
                            ObservableList<Node> child = p.getChildren();
                            for (int i = 0; i < child.size(); i++) {
                                if (child.get(i) instanceof Button b) {
                                    b.setLayoutX(stage.getScene().getWidth() - 100);
                                    b.setLayoutY((i + 1) * 40);
                                    b.setPrefWidth(80);
                                    b.setPrefHeight(35);
                                }
                            }
                            p.setVisible(true);
                        }
                    }
                    lastKeysPressed = keys;
                }
            }
        }
    };

    private List<Keys> getUpdatedKeysToSendAndUpdatePlayerDir(List<Keys> lastKeysPressed, List<Keys> keys, Player p) {
        List<Keys> k = new ArrayList<>();
        if (keys.stream().anyMatch(a -> p.getDir().toString().equalsIgnoreCase(a.toString()))) {
            return keys;
        } else {
            //TODO some stuff
           return keys.stream().filter(e -> lastKeysPressed.contains(e) && (e == Keys.up || e == Keys.down || e == Keys.left || e == Keys.right || e == Keys.decline)).toList();
        }
    }

    private void showMenu() {
        Pane p = (Pane) stage.getScene().getRoot().getChildrenUnmodifiable().get(1);
        ObservableList<Node> child = p.getChildren();
        p.setVisible(false);
        child.clear();
        Button pokemon = new Button("Pokemon");
        Button map = new Button("Map");
        Button items = new Button("Items");
        p.getChildren().addAll(pokemon, map, items);
        p.setPrefWidth(800);
        p.setPrefHeight(800);
        p.setOnKeyPressed(e -> {
            if (Keys.getKeys(Collections.singletonList(e.getCode())).contains(Keys.menu)) {
                synchronized (client.getPlayers()) {
                    client.getPlayers().get(0).setActivity(Player.Activity.moving);
                    p.setVisible(false);
                    timeTillClosedMenu = System.currentTimeMillis() + 400;
                }
            }
        });
    }

    private void initImgs() {
        try {
            for (int i = 0; i < 4; i++)
                allImgs.put("Grass" + i, new Image(String.valueOf(Paths.get("res/Envir/Grass" + i + ".png").toUri().toURL())));
            allImgs.put("BigGrass", new Image(String.valueOf(Paths.get("res/Envir/BigGrass.png").toUri().toURL())));
            allImgs.put("Tree", new Image(String.valueOf(Paths.get("res/Envir/Tree.png").toUri().toURL())));
            allImgs.put("Water", new Image(String.valueOf(Paths.get("res/Envir/Water.png").toUri().toURL())));
            for (int i = 0; i < 1; i++) {
                for (String dir : Arrays.stream(Player.Dir.values()).map(Enum::toString).collect(Collectors.toList())) {
                    for (String h : Arrays.stream(Player.Hands.values()).map(Enum::toString).collect(Collectors.toList())) {
                        allImgs.put("Player" + i + dir + h, new Image(String.valueOf(Paths.get("res/Player/" + i + "/" + dir + "/" + h + ".png").toUri().toURL())));
                    }
                }
            }
            for (String type : Arrays.stream(House.Type.values()).map(Enum::toString).toList()) {
                for (String view : List.of("Outside", "Floor", "Wall")) {
                    allImgs.put("House" + type + view, new Image(String.valueOf(Paths.get("res/Buildings/" + type + "/" + view + ".png").toUri().toURL())));
                }
            }
            allImgs.put("HouseBigShelf", new Image(String.valueOf(Paths.get("res/Buildings/Market/BigShelf.png").toUri().toURL())));
            allImgs.put("HouseSmallShelf", new Image(String.valueOf(Paths.get("res/Buildings/Market/SmallShelf.png").toUri().toURL())));
        } catch (MalformedURLException ignored) {
        }
    }

    private final List<KeyCode> keysPressed = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws MalformedURLException {
        stage = primaryStage;
        stage.setX(1000);
        stage.setY(80);
        initImgs();
        client = new Client(33333, "127.0.0.1", true);
        client.onMessage((a, b) -> {
            if (b instanceof String s && !s.startsWith(MessageType.toStr(MessageType.updatePos))) {
                System.out.println("From Server: '" + b + '\'');
            }
        });
        stage.setScene(new Scene(LoginScreens.getLoginScene(stage, client, null), 500, 500));
        addListener();
        stage.setTitle("Pokemon");
        stage.getIcons().add(new Image(String.valueOf(Paths.get("res/icon.png").toUri().toURL())));
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.show();
    }

    private void addListener() {
        stage.getScene().setOnKeyPressed(e -> {
            if (!keysPressed.contains(e.getCode())) keysPressed.add(e.getCode());
            if (e.getCode() == KeyCode.F11) stage.setFullScreen(!stage.isFullScreen());
        });
        stage.getScene().setOnKeyReleased(e -> keysPressed.remove(e.getCode()));
        client.onMessage((a, b) -> {
            if (b instanceof String s) {
                MessageType mT = MessageType.getType(s.length() > 2 && Pattern.matches("[0-9]{3}", s.substring(0, 3)) ? Integer.parseInt(s.substring(0, 3)) : 999);
                switch (mT) {
                    case hellman, register, login, delete -> doLogin(mT, s);
                    case region -> {
                        if (s.charAt(3) - '0' != 0) client.getErrorTxt().setVisible(true);
                        switch (s.charAt(3) - '0') {
                            case 0 -> {
                                client.setWorld(new World(Integer.parseInt(s.substring(4, s.indexOf(","))), client.getErrorTxt().getText()));
                                stage.getScene().setRoot(LoginScreens.getGameScreen());
                                client.setUsername(s.substring(s.indexOf(",") + 1));
                                client.getPlayers().add(new Player(client.getUsername(), new Vector2D(3, 2), 0, client.getErrorTxt().getText()));
                                animationTimer.start();
                            }
                            case 1 -> client.getErrorTxt().setText("region does not exist");
                            case 2 -> client.getErrorTxt().setText("you are not logged in");
                            default -> client.getErrorTxt().setText("Something went wrong. Please check if your program is on the latest version.");
                        }
                    }
                    case updatePos -> updatePos(s);
                }
            }
        });
    }

    private void updatePos(String s) {
        Matcher m = Pattern.compile("\\{(.*?)}").matcher(s.substring(3));
        List<String> curNames = new ArrayList<>();
        while (m.find()) {
            String[] values = m.group(1).split(",");
            synchronized (client.getPlayers()) {
                Optional<Player> player = client.getPlayers().stream().filter(e -> e.getName().equals(values[0])).findFirst();
                if (player.isEmpty()) {
                    client.getPlayers().add(new Player(values[0], new Vector2D(Double.parseDouble(values[1]), Double.parseDouble(values[2])), Integer.parseInt(values[3]), client.getWorld().getName()));
                } else {
                    player.get().updateNewPos(new Vector2D(Double.parseDouble(values[1]), Double.parseDouble(values[2])), client);
                }
                curNames.add(values[0]);
            }
        }
        synchronized (client.getPlayers()) {
            client.getPlayers().removeIf(e -> !curNames.contains(e.getName()) && !client.getUsername().equals(e.getName()));
        }
    }

    private void doLogin(MessageType mT, String s) {
        switch (mT) {
            case hellman:
                Matcher maP = Pattern.compile("p=([0-9]+)[,}]").matcher(s);
                Matcher maG = Pattern.compile("g=([0-9]+)[,}]").matcher(s);
                Matcher maPub = Pattern.compile("serPub=([0-9]+)[,}]").matcher(s);
                Matcher maIv = Pattern.compile("iv=\\[([0-9-,]+)]").matcher(s.replaceAll("\s+", ""));
                client.setCrypto(new Crypto(new BigInteger(maP.find() ? maP.group(1) : "1"), new BigInteger(maG.find() ? maG.group(1) : "1"), maIv.find() ? maIv.group(1) : "[1,2]"));
                client.getCrypto().createKey(new BigInteger(maPub.find() ? maPub.group(1) : "1"), new BigInteger(maPub.group(1)).intValue());
                client.send(MessageType.toStr(MessageType.hellman) + "{pub=" + client.getCrypto().getPub() + "}");
                break;
            case register:
                switch (s.charAt(3) - '0') {
                    case 0 -> stage.getScene().setRoot(LoginScreens.getRegionSelectScreen(stage, client));
                    case 1 -> client.getErrorTxt().setText("Username too short. Min 4 Character");
                    case 2 -> client.getErrorTxt().setText("Username illegal, only (a-zA-z0-9_)");
                    case 3 -> client.getErrorTxt().setText("password too short");
                    case 4 -> client.getErrorTxt().setText("email illegal");
                    case 5 -> client.getErrorTxt().setText("username exists");
                    case 6 -> client.getErrorTxt().setText("email exists");
                    default -> client.getErrorTxt().setText("Something went wrong. Please check if your program is on the latest version.");
                }
                break;
            case login:
                switch (s.charAt(3) - '0') {
                    case 0 -> stage.getScene().setRoot(LoginScreens.getRegionSelectScreen(stage, client));
                    case 1 -> client.getErrorTxt().setText("username/email not found");
                    case 2 -> client.getErrorTxt().setText("wrong password");
                    case 3 -> client.getErrorTxt().setText("someone is already logged in");
                    default -> client.getErrorTxt().setText("Something went wrong. Please check if your program is on the latest version.");
                }
                break;
            case delete:
                switch (s.charAt(3) - '0') {
                    case 0 -> {
                        User.delLoginData();
                        stage.getScene().setRoot(LoginScreens.getLoginScene(stage, client, null));
                    }
                    case 1 -> client.getErrorTxt().setText("username/email not found");
                    case 2 -> client.getErrorTxt().setText("wrong password");
                    default -> client.getErrorTxt().setText("Something went wrong. Please check if your program is on the latest version.");
                }
                break;
        }
    }

    @Override
    public void stop() {
        client.disconnect();
    }
}
