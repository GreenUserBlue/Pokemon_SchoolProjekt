package ClientStuff;

import Calcs.Crypto;
import Calcs.Vector2D;
import Envir.House;
import Envir.World;
import InGame.Item;
import InGame.Pokemon;
import ServerStuff.MessageType;
import ServerStuff.User;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
//Hod
//--module-path "D:\Alle Apps\javafx-sdk-11.0.2\lib" --add-modules javafx.controls,javafx.fxml

//Zwi
//--module-path "C:\Program Files\Java\openjfx-16_windows-x64_bin-sdk\javafx-sdk-16\lib" --add-modules javafx.controls,javafx.fxml
public class MyClient extends Application {

    /**
     * the client which communicates with the server
     */
    private Client client;

    public Client getClient() {
        return client;
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * the stage where the program runs on
     */
    private Stage stage;

    /**
     * the inGame menu for the player
     */
    private Menu menu;

    /**
     * the textBox when the player talks to someone or something
     */
    private final TextEvent txt = new TextEvent();

    private final MarketGUI marketGUI = new MarketGUI(txt);

    private final FightGUI fightGUI = new FightGUI(txt);

    /**
     * all Images which are needed for graphics
     */
    private final Map<String, Image> allImgs = new HashMap<>();

    private MyClient getMyClient() {
        return this;
    }

    /**
     * sends updates and changes the positions for all Players
     */
    private final AnimationTimer animationTimer = new AnimationTimer() {
        /**
         * min Time which needs to Pass till the animationTimer is called the next time
         */
        private long timeTillNextUse = 0;

        /**
         * counts the current loops
         */
        private int count = 0;

        /**
         * all keys which were pressed on the last check
         */
        private List<Keys> lastKeysPressed = new ArrayList<>();

        @Override
        public void handle(long l) {
            if (System.currentTimeMillis() > timeTillNextUse) {
                timeTillNextUse = System.currentTimeMillis() + 20;
                txt.updateSize(stage.getScene());
                Canvas c = (Canvas) (stage.getScene().getRoot().getChildrenUnmodifiable().get(0));
                synchronized (client.getPlayers()) {
                    if (client.getPlayers().get(0).getActivity() == Player.Activity.fight) {
                        fightGUI.draw(c, new Vector2D(stage.getScene().getWidth(), stage.getScene().getHeight()), allImgs);
                        List<Keys> keys = (Keys.getSmartKeys(client.getKeysPressed()));
                        if (keys.contains(Keys.confirm)) {
                            txt.nextLine();
                        } else if (keys.contains(Keys.decline)) {
                            txt.decline(client.getPlayers().get(0));
                        }
                    } else {
                        doRunning(c);
                    }
                }
            }
        }

        /**
         * what happens when the player is not Fighting
         * @param c the canvas to draw on
         */
        private void doRunning(Canvas c) {
            if (client.getPlayers().get(0).getHouseEntrancePos() == null) {
                client.getWorld().drawEnvir(c, client.getPlayers(), new Vector2D(stage.getScene().getWidth(), stage.getScene().getHeight()), allImgs);
            } else {
                client.getWorld().drawInsideHouse(c, client.getPlayers(), new Vector2D(stage.getScene().getWidth(), stage.getScene().getHeight()), allImgs);
            }
            List<Keys> keys = (Keys.getSmartKeys(client.getKeysPressed()));
            if (keys.contains(Keys.menu) && client.getPlayers().get(0).getActivity() != Player.Activity.menu) {
                menu = new Menu(getMyClient());
                menu.showMenu();
            } else if (keys.contains(Keys.confirm) && client.getPlayers().get(0).getActivity() == Player.Activity.textEvent) {
                if (txt.nextLine()) {
                    if (txt.getState() == TextEvent.TextEventState.selection) {
                        System.out.println("MyClient.handle: now in selection");
                    } else if (txt.isWalkableAfterwards()) {
                        client.getPlayers().get(0).setActivity(Player.Activity.standing);
                        System.out.println("MyClient.handle: finished");
                    } else {
                        System.out.println("MyClient.handle: finished");
                    }
                }
            } else if (client.getPlayers().get(0).getActivity() == Player.Activity.textEvent) {
                if (keys.contains(Keys.decline)) {
                    txt.decline(client.getPlayers().get(0));
                }
            }
            if (((count++) & 0b11) == 0) {
                client.getPlayers().forEach(Player::updateHands);
                if (client.getPlayers().get(0).getActivity() == Player.Activity.moving || client.getPlayers().get(0).getActivity() == Player.Activity.standing) {
                    StringBuilder res = new StringBuilder();
                    keys = getUpdatedKeysToSendAndUpdatePlayerDir(lastKeysPressed, keys, client.getPlayers().get(0));
                    keys.forEach(e -> res.append(e.ordinal()));
                    if (keys.contains(Keys.confirm)) {
                        res.append(",").append(client.getPlayers().get(0).getDir().ordinal());
                    }
                    client.send(MessageType.toStr(MessageType.keysPres) + res);
                } else if (client.getPlayers().get(0).getActivity() == Player.Activity.menu) {
                    menu.updatePlayerMenuPos();
                }
            }
            lastKeysPressed = keys;
        }
    };

/* a   private World.Block getNextBlock(Player player) {
        if (player.getHouseEntrancePos() == null) {
            return client.getWorld().getBlockEnvir((int) player.getPos().getX(), (int) player.getPos().getY(), false);
        } else {
            House h = client.getWorld().getHouse(player.getHouseEntrancePos());
            return h.getBlockInside((int) player.getPos().getX(), (int) player.getPos().getY());
        }
    }*/

    /**
     * updates the design for the login parts
     */
    private final AnimationTimer designUpdater = new AnimationTimer() {

        long timeTillNextUse = 0;

        long timeTillFadeEnds = 0;

        @Override
        public void handle(long l) {
            if (timeTillNextUse < System.currentTimeMillis()) {
                timeTillNextUse = System.currentTimeMillis() + 20;
                if (stage.getScene().getRoot().getChildrenUnmodifiable().size() >= 2 && stage.getScene().getRoot().getChildrenUnmodifiable().get(0) instanceof ProgressBar bar) {
                    updateBarsAndPos(bar);
                } else if (stage.getScene().getRoot().getChildrenUnmodifiable().size() >= 3 && stage.getScene().getRoot().getChildrenUnmodifiable().get(0) instanceof javafx.scene.layout.Pane) {
                    resizeProfileSelect();
                }
            }
        }

        /**
         * updates the progressbar which is displayed at the beginning
         * @param bar the bar
         */
        private void updateBarsAndPos(ProgressBar bar) {
            if (stage.getScene().getRoot().getChildrenUnmodifiable().get(1) instanceof ProgressBar barHidden) {
                bar.setPrefWidth(stage.getWidth() / 3 * 2);
                bar.setLayoutX(stage.getWidth() / 6);
                bar.setLayoutY(stage.getScene().getHeight() - 50);
                bar.setVisible(true);
                if (bar.getProgress() < barHidden.getProgress()) {
                    bar.setProgress(bar.getProgress() + 0.05);
                }
                if (bar.getProgress() >= 1) {
                    if (timeTillFadeEnds == 0) {
                        timeTillFadeEnds = System.currentTimeMillis() + 300;
                        FadeTransition f = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
                        f.setFromValue(1);
                        f.setToValue(0);
                        f.play();
                    }
                    if (System.currentTimeMillis() > timeTillFadeEnds) {
                        Parent p;
                        if (barHidden.getProgress() > 1.05) p = (LoginScreens.getLoginScene(stage, client, null));
                        else p = (LoginScreens.getProfileSelectScreen(stage, client));
                        FadeTransition f = new FadeTransition(Duration.millis(300), p);
                        p.setOpacity(0);
                        f.setFromValue(0);
                        f.setToValue(1);
                        f.playFromStart();
                        stage.getScene().setRoot(p);
                    }
                }
            }
        }
    };

    /**
     * resizes the profileSelect Screen
     */
    private void resizeProfileSelect() {
        List<Node> l = stage.getScene().getRoot().getChildrenUnmodifiable();
        Vector2D gaps = new Vector2D(stage.getScene().getWidth() * 0.06, stage.getScene().getHeight() * 0.06);
        double gapsEdge = stage.getScene().getWidth() * 0.05;
        Vector2D size = new Vector2D(stage.getWidth() * 0.22, stage.getScene().getHeight() * 0.22);
        for (int i = 0; i < l.size() && l.get(i) instanceof Pane p; i++) {
            synchronized (p.getChildren()) {
                p.setPrefSize(size.getX(), size.getY());
                p.setLayoutX((i + 1) * gaps.getX() + i * size.getX() + gapsEdge);
                p.setLayoutY(gaps.getY() + size.getY());
                if (p.getChildren().get(0) instanceof Rectangle pokeball) {
                    pokeball.setWidth(p.getWidth());
                    pokeball.setHeight(p.getWidth());
                    if (p.getChildren().size() > 1 && p.getChildren().get(1) instanceof Rectangle r) {
                        r.setLayoutX(p.getWidth() * 0.1);
                        r.setLayoutY(p.getHeight() * 0.15);
                        r.setWidth(p.getWidth() * 0.8);
                        r.setHeight(r.getWidth() * 1200 / 1920);
                        r.setArcHeight(r.getWidth() * 0.2);
                        r.setArcWidth(r.getWidth() * 0.2);
                        if (p.getChildren().size() > 2) {
                            /* a if (p.getChildren().getItem(2) instanceof TextField poke) {
                                poke.setLayoutX(r.getLayoutX() + r.getWidth() * 0.1);
                                poke.setFont(new Font(r.getHeight() * 0.12));
                                poke.setLayoutY(r.getLayoutY() + poke.getFont().getSize());
                            } else*/
                            if (p.getChildren().get(2) instanceof Text poke) {
                                poke.setLayoutX(r.getLayoutX() + r.getWidth() * 0.1);
                                poke.setFont(new Font(r.getHeight() * 0.12));
                                poke.setLayoutY(r.getLayoutY() + r.getHeight() * 0.1 + poke.getFont().getSize());
                            }
                            if (p.getChildren().size() > 3 && p.getChildren().get(3) instanceof Text badge) {
                                badge.setLayoutX(r.getLayoutX() + r.getWidth() * 0.1);
                                badge.setFont(new Font(r.getHeight() * 0.1));
                                badge.setLayoutY(r.getLayoutY() + r.getHeight() * 0.3 + badge.getFont().getSize());
                               /*a if (p.getChildren().size() > 4 && p.getChildren().getItem(4) instanceof Text badge) {
                                    badge.setLayoutX(r.getLayoutX() + r.getWidth() * 0.1);
                                    badge.setFont(new Font(r.getHeight() * 0.1));
                                    badge.setLayoutY(r.getLayoutY() + r.getHeight() * 0.42 + badge.getFont().getSize());
                                    if (p.getChildren().size() > 5 && p.getChildren().getItem(5) instanceof Button changeName) {
                                        changeName.setLayoutX(r.getLayoutX() + r.getWidth() * 0.85);
                                        changeName.setLayoutY(r.getLayoutY() + r.getHeight() * 0.1);
                                        changeName.setMaxSize(r.getHeight() * 0.15, r.getHeight() * 0.15);
                                        changeName.setMinSize(r.getHeight() * 0.15, r.getHeight() * 0.15);
                                        if (p.getChildren().size() > 6 && p.getChildren().getItem(6) instanceof Text error) {
                                            error.setLayoutX(r.getLayoutX() + r.getWidth() * 0.1);
                                            error.setFont(new Font(r.getHeight() * 0.1));
                                            error.setLayoutY(r.getLayoutY() + r.getHeight() * 0.62 + error.getFont().getSize());
                                        }
                                    }
                                }*/
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * read the f*cking name of the method
     */
    private List<Keys> getUpdatedKeysToSendAndUpdatePlayerDir(List<Keys> lastKeysPressed, List<Keys> keys, Player p) {
        if (keys.stream().anyMatch(a -> p.getDir().toString().equalsIgnoreCase(a.toString()))) return keys;
        else {
//            keys = keys.stream().filter(e -> (e == Keys.up || e == Keys.down || e == Keys.left || e == Keys.right || e == Keys.decline || e == Keys.confirm)).toList();
            List<Keys> k = new ArrayList<>(keys);
            k.remove(Keys.decline);
            k.remove(Keys.confirm);
            k.remove(Keys.menu);
            if ((k.size() == 1)) p.setDir(Player.Dir.valueOf(k.get(0).toString()));
            return keys.stream().filter(lastKeysPressed::contains).toList();
        }
    }

    /**
     * initializes allImgs
     */
    private void initImgs() {
        try {
            for (int i = 0; i < 4; i++)
                allImgs.put("Grass" + i, new Image(String.valueOf(Paths.get("res/Envir/Grass" + i + ".png").toUri().toURL())));
            allImgs.put("BigGrass", new Image(String.valueOf(Paths.get("res/Envir/BigGrass.png").toUri().toURL())));
            allImgs.put("Tree", new Image(String.valueOf(Paths.get("res/Envir/Tree.png").toUri().toURL())));
            allImgs.put("Water", new Image(String.valueOf(Paths.get("res/Envir/Water.png").toUri().toURL())));
            allImgs.put("WaterEdge", new Image(String.valueOf(Paths.get("res/Envir/WaterEdge.png").toUri().toURL())));
            allImgs.put("FightBottomGrass", new Image(String.valueOf(Paths.get("res/Fight/BackGrass.png").toUri().toURL())));
            for (int i = 0; i < 1; i++) {
                for (String dir : Arrays.stream(Player.Dir.values()).map(Enum::toString).collect(Collectors.toList())) {
                    for (String h : Arrays.stream(Player.Hands.values()).map(Enum::toString).collect(Collectors.toList())) {
                        allImgs.put("Player" + i + dir + h, new Image(String.valueOf(Paths.get("res/Player/" + i + "/" + dir + "/" + h + ".png").toUri().toURL())));
                    }
                }
            }
            for (String type : Arrays.stream(House.Type.values()).map(Enum::toString).toList()) {
                for (String view : List.of("Outside", "Floor", "Wall", "Door")) {
                    allImgs.put("House" + type + view, new Image(String.valueOf(Paths.get("res/Buildings/" + type + "/" + view + ".png").toUri().toURL())));
                }
            }
            allImgs.put("HouseBigShelf", new Image(String.valueOf(Paths.get("res/Buildings/Market/BigShelf.png").toUri().toURL())));
            allImgs.put("HouseSmallShelf", new Image(String.valueOf(Paths.get("res/Buildings/Market/SmallShelf.png").toUri().toURL())));
            for (int i = 0; i < 3; i++)
                allImgs.put("Ball" + (i + 1), new Image(String.valueOf(Paths.get("./res/Balls/" + (i + 1) + ".png").toUri().toURL())));
        } catch (MalformedURLException ignored) {
        }
    }

    /**
     * Starts the program
     *
     * @param args CommandLine parameter
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
        int height = 300;
        stage.setScene(new Scene(LoginScreens.getLoadingScreen(), height / 9D * 16, height));
        //set Stage boundaries to the top right corner of the visible bounds of the main screen
        int rnd = (int) (Math.random() * 80);
        stage.setX(primaryScreenBounds.getMinX() + primaryScreenBounds.getWidth() - stage.getScene().getWidth() - 80 + rnd);
        stage.setY(80 - rnd);
        initImgs();
        TextEvent.initTexts();
        Pokemon.init(true);
        Item.init(Path.of("./res/DataSets/Items.csv"));

        //noinspection unchecked
        client = new Client(33333, "127.0.0.1", false, (a, b) -> {
            if (b instanceof String s && !s.startsWith(MessageType.toStr(MessageType.updatePos)) && !s.startsWith(MessageType.toStr(MessageType.textEvent)) && !s.startsWith(MessageType.toStr(MessageType.itemData)))
                System.out.println("From Server: '" + b + '\'');
        }, getOnMsgClient());
        txt.setClient(client);//33,85
        marketGUI.setClient(client);
        fightGUI.setClient(client);

        addListener();
        stage.setTitle("Pokemon OW");
        stage.getIcons().add(new Image(String.valueOf(Paths.get("res/icon.png").toUri().toURL())));
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        designUpdater.start();
        stage.getScene().setFill(Color.BLACK);
        stage.show();
    }

    /**
     * adds all the listeners to the stage/client
     */
    private void addListener() {
        stage.getScene().setOnKeyPressed(e -> {
            if (!client.getKeysPressed().contains(e.getCode())) client.getKeysPressed().add(e.getCode());
            if (e.getCode() == KeyCode.F11) stage.setFullScreen(!stage.isFullScreen());
        });
        stage.getScene().setOnKeyReleased(e -> client.getKeysPressed().remove(e.getCode()));
    }

    /**
     * @return what happens when the client gets a message from the server
     */
    private BiConsumer<Client, Object> getOnMsgClient() {
        return (a, b) -> {
            if (b instanceof String s) {
                MessageType mT = MessageType.getType(s.length() > 2 && Pattern.matches("[0-9]{3}", s.substring(0, 3)) ? Integer.parseInt(s.substring(0, 3)) : 999);
                switch (mT) {
                    case hellman, register, login, delete -> doLogin(mT, s);
                    case profile -> doProfiles(s.substring(3));
                    case worldSelect -> doWorldSelect(s);
                    case updatePos -> updatePos(s);
                    case textEvent -> doTextEvent(s.substring(3));
                    case itemData -> updateItemData(s.substring(MessageType.toStr(MessageType.itemData).length()));
                    case fightData -> startFight(s);
                    case inFightUpdate -> fightGUI.updateAll(s.substring(MessageType.toStr(MessageType.inFightUpdate).length()));
                    case error -> System.out.println("something went wrong");
                }
            }
        };
    }

    /**
     * starts a fight
     *
     * @param s the msg from the server
     */
    private void startFight(String s) {
        synchronized (client.getPlayers().get(0)) {
            fightGUI.setPlayer(client.getPlayers().get(0));
            client.getPlayers().get(0).setActivity(Player.Activity.fight);
            fightGUI.startNewFight(s.split("N", 2)[1]);
        }
    }

    /**
     * updates the items the player possesses currently
     *
     * @param str the message from the server
     */
    private void updateItemData(String str) {
        synchronized (client.getPlayers().get(0)) {
            client.getPlayers().get(0).setMoney(Long.parseLong(str.split(";")[1].trim()));
            Arrays.stream(str.split(";")).skip(2).forEach(a -> {
                String[] s = a.split(",");
                client.getPlayers().get(0).getItems().put(Integer.parseInt(s[0]), Integer.parseInt(s[1]));
            });
        }
    }

    /**
     * starts textEvents
     *
     * @param s the message from the server
     */
    private void doTextEvent(String s) {
        Player p = client.getPlayers().get(0);
        switch (s.charAt(0) - '0') {
            case 0 -> {
                synchronized (client.getPlayers().get(0)) {
                    if (p.getActivity() != Player.Activity.textEvent) {
                        p.setActivity(Player.Activity.textEvent);
                        if (s.split(",").length == 1) {
                            int id = Integer.parseInt(s.substring(1));
                            Platform.runLater(() -> txt.startNewText(id, null));
                        } else doTextEventsWithData(s);
                    }
                }
            }
            case 1 -> {
                p.setActivity(Player.Activity.textEvent);
                if (s.split(",").length == 1) {
                    int id = Integer.parseInt(s.substring(1));
                    Platform.runLater(() -> txt.startNewText(id, null));
                    txt.addOnFin(null);
                } else doTextEventsWithData(s);
            }
        }
    }

    /**
     * starts a textEvent with multiple texts
     *
     * @param s the message from the server
     */
    private void doTextEventsWithData(String s) {
        int id = Integer.parseInt(s.substring(1).split(",")[0]);
        System.out.println("MyClient.doTextEvent: " + id);
        if (id == TextEvent.TextEventIDsTranslator.MarketShopMeet.getId()) {
            Platform.runLater(() -> marketGUI.startNewMarket(client.getPlayers().get(0), Integer.parseInt(s.split(",")[1])));
        } else {
            HashMap<String, String> data = new HashMap<>();
            Arrays.stream(s.substring(1).split(",")).skip(1).forEach(a -> data.put(a.split(":")[0], a.split(":")[1]));
            if (id == TextEvent.TextEventIDsTranslator.PlayersMeetAns.getId() || id == TextEvent.TextEventIDsTranslator.PlayersMeetQues.getId()) {
                changeLookingDirection(data);
            }
            Platform.runLater(() -> {
                txt.startNewText(id, data);
                if (id == TextEvent.TextEventIDsTranslator.PlayersMeetAns.getId()) {
                    ObservableList<Node> children = txt.getOptionsNode().getChildren();
                    ((Button) children.get(0)).setOnAction(e -> client.send(MessageType.toStr(MessageType.textEvent) + "1" + 0));
                    ((Button) children.get(1)).setOnAction(e -> {
                        HashMap<String, String> keys = new HashMap<>();
                        keys.put("name", "You have");
                        txt.startNewText(TextEvent.TextEventIDsTranslator.PlayersMeetDeclineFight.getId(), keys);
                        client.send(MessageType.toStr(MessageType.textEvent) + "1" + 1);
                    });
                }
            });
        }
    }

    /**
     * change the directions where the player is looking when talking to another player
     *
     * @param data the name of the other player
     */
    private void changeLookingDirection(HashMap<String, String> data) {
        Optional<Player> op = client.getPlayers().stream().filter(a -> a.getName().equals(data.get("name"))).findFirst();
        op.ifPresent(a -> {
            client.getPlayers().get(0).setDir(Player.Dir.getDir(Vector2D.sub(a.getPos(), (client.getPlayers().get(0).getPos())), Player.Dir.none));
            a.setDir(Player.Dir.getDir(Vector2D.sub((client.getPlayers().get(0).getPos()), a.getPos()), Player.Dir.none));
        });
        client.getPlayers().get(0).setDir(Player.Dir.getDir(
                Vector2D.sub(client.getPlayers().stream()
                                .filter(a -> a.getName().equals(data.get("name")))
                                .map(Player::getPos)
                                .findFirst().orElse(new Vector2D()),
                        (client.getPlayers().get(0).getPos())), Player.Dir.none));
    }

    /**
     * updates everything for the profileSelect
     *
     * @param str the message from the server
     */
    private void doProfiles(String str) {
        switch (str.charAt(0) - '0') {
            case 0 -> {
                Matcher m = Pattern.compile("\\{((.)*?,(.)*?)??}").matcher(str);
                for (int i = 0; i < 3; i++)
                    client.getProfiles()[i] = new LoginScreens.PlayerProfile(m.find() ? m.group(1) : null);
            }
            case 1 -> {
                if (stage.getScene().getRoot().getChildrenUnmodifiable().get(Integer.parseInt(str.charAt(1) + "")) instanceof Pane p) {
                    Platform.runLater(() -> {
                        p.getChildren().remove(client.getErrorTxt());
                        if (p.getChildren().get(2) instanceof TextField txt) {
                            Text t = client.getProfiles()[Integer.parseInt(str.charAt(1) + "")].getTextField();
                            t.setText(txt.getText());
                            p.getChildren().add(2, t);
                            p.getChildren().remove(txt);
                        }
                    });
                }
            }
            case 2 -> client.getErrorTxt().setText("Min 4 Chars");
            case 3 -> client.getErrorTxt().setText("Not allowed chars");
            case 4 -> client.getErrorTxt().setText("Max 25 Chars");
            case 5 -> client.getErrorTxt().setText("Something went wrong. Please check if your program is on the latest version.");
        }
    }

    /**
     * puts the player in the world
     *
     * @param s the message from the server
     */
    private void doWorldSelect(String s) {
        if (s.charAt(3) - '0' != 0) client.getErrorTxt().setVisible(true);
        switch (s.charAt(3) - '0') {
            case 0 -> {
                client.setWorld(new World(Integer.parseInt(s.substring(4, s.indexOf(","))), client.getErrorTxt().getText()));
                stage.getScene().setRoot(LoginScreens.getGameScreen(txt));
                client.setUsername(s.substring(s.indexOf(",") + 1));
                client.getPlayers().add(new Player(client.getUsername(), new Vector2D(3, 2), 0, client.getErrorTxt().getText()));
                animationTimer.start();
            }
            case 1 -> client.getErrorTxt().setText("Player/World does not exist");
            case 2 -> client.getErrorTxt().setText("you are not logged in");
            default -> client.getErrorTxt().setText("Something went wrong. Please check if your program is on the latest version.");
        }
    }

    /**
     * updates the positions for all Players
     *
     * @param s the String from the server
     */
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

    /**
     * processes all Login data which are sent by the server
     *
     * @param mT the type of message which was sent
     * @param s  the String which was sent
     */
    private void doLogin(MessageType mT, String s) {
        switch (mT) {
            case hellman:
                Platform.runLater(() -> ((ProgressBar) (stage.getScene().getRoot().getChildrenUnmodifiable().get(1))).setProgress(0.1));
                Matcher maP = Pattern.compile("p=([0-9]+)[,}]").matcher(s);
                Matcher maG = Pattern.compile("g=([0-9]+)[,}]").matcher(s);
                Matcher maPub = Pattern.compile("serPub=([0-9]+)[,}]").matcher(s);
                Matcher maIv = Pattern.compile("iv=\\[([0-9-,]+)]").matcher(s.replaceAll("\s+", ""));
                client.setCrypto(new Crypto(new BigInteger(maP.find() ? maP.group(1) : "1"), new BigInteger(maG.find() ? maG.group(1) : "1"), maIv.find() ? maIv.group(1) : "[1,2]"));
                client.getCrypto().createKey(new BigInteger(maPub.find() ? maPub.group(1) : "1"), new BigInteger(maPub.group(1)).intValue());
                client.send(MessageType.toStr(MessageType.hellman) + "{pub=" + client.getCrypto().getPub() + "}");
                String login = User.getLogin();
                if (login == null)
                    Platform.runLater(() -> ((ProgressBar) (stage.getScene().getRoot().getChildrenUnmodifiable().get(1))).setProgress(1.1));
                else {
                    Platform.runLater(() -> ((ProgressBar) (stage.getScene().getRoot().getChildrenUnmodifiable().get(1))).setProgress(0.5));
                    client.setUsername(login.split(";")[0]);
                    client.send(MessageType.toStr(MessageType.login) + "{name='" + login.split(";")[0] + "', pwd='" + client.getCrypto().encrypt(login.split(";", 2)[1]) + "'}");
                    Platform.runLater(() -> ((ProgressBar) (stage.getScene().getRoot().getChildrenUnmodifiable().get(1))).setProgress(0.7));
                }
                break;
            case register:
                switch (s.charAt(3) - '0') {
                    case 0 -> stage.getScene().setRoot(LoginScreens.getProfileSelectScreen(stage, client));
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
                    case 0 -> {
                        if (stage.getScene().getRoot().getChildrenUnmodifiable().get(0) instanceof ProgressBar) {
                            Platform.runLater(() -> ((ProgressBar) (stage.getScene().getRoot().getChildrenUnmodifiable().get(1))).setProgress(1));
                        } else {
                            stage.getScene().setRoot(LoginScreens.getProfileSelectScreen(stage, client));
                        }
                        return;
                    }
                    case 1 -> client.getErrorTxt().setText("username/email not found");
                    case 2 -> client.getErrorTxt().setText("wrong password");
                    case 3 -> client.getErrorTxt().setText("someone is already logged in");
                    default -> client.getErrorTxt().setText("Something went wrong. Please check if your program is on the latest version.");
                }
                if (stage.getScene().getRoot().getChildrenUnmodifiable().get(1) instanceof ProgressBar p) {
                    Platform.runLater(() -> p.setProgress(1.1));
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
