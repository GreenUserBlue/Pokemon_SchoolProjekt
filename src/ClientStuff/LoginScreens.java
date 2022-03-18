package ClientStuff;

import ServerStuff.MessageType;
import ServerStuff.User;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Zwickelstorfer Felix
 * creates a login, register and delete screen
 */
public class LoginScreens {

    /**
     * creates and returns a pane to let the user login
     *
     * @param stage the stage where everything will be shown
     * @param c     the client to send the selected region
     * @param s     the username which was entered at the registerScreen (if there was none, then its null)
     */
    public static Pane getLoginScene(Stage stage, Client c, String s) {
        Pane p = new Pane();
        String dataLog = User.getLogin();
        TextField name = new TextField(s == null ? (dataLog == null ? "" : dataLog.split(";")[0]) : s);
        PasswordField pwd = new PasswordField();
        if (s == null && dataLog != null) pwd.setText(dataLog.split(";", 2)[1]);
        Text error = new Text();
        c.setErrorTxt(error);
        Button login = new Button("Login");
        Button register = new Button("Create Account");
        name.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) pwd.requestFocus();
        });
        pwd.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) sendLogin(c, name, pwd);
        });
        register.setOnAction(e -> stage.getScene().setRoot(getRegisterPane(stage, c, name.getText())));
        login.setOnAction(e -> sendLogin(c, name, pwd));
        p.getChildren().addAll(name, pwd, error, login, register);
        for (int i = 0; i < p.getChildren().size(); i++) {
            p.getChildren().get(i).setLayoutX(10);
            p.getChildren().get(i).setLayoutY(40 * i + 10);
        }
        return p;
    }


    /**
     * sends a message to the server that you want to login
     *
     * @param c    the client to send the data
     * @param name the name or email of the user
     * @param pwd  the password of the user
     */
    private static void sendLogin(Client c, TextField name, TextField pwd) {
        c.setUsername(name.getText());
        User.storePwd(name.getText(), pwd.getText());
        if (c.getCrypto() == null) return;
        c.send(MessageType.toStr(MessageType.login) + "{name='" + name.getText() + "', pwd='" + c.getCrypto().encrypt(pwd.getText()) + "'}");
    }

    /**
     * creates and returns a pane to let the user register
     *
     * @param stage the stage where everything will be shown
     * @param c     the client to send the selected region
     * @param s     the username which was entered at the loginScreen (if there was none, then its null)
     */
    private static Pane getRegisterPane(Stage stage, Client c, String s) {
        Pane p = new Pane();
        TextField name = new TextField(s == null ? "" : s);
        TextField email = new TextField();
        PasswordField pwd = new PasswordField();
        PasswordField pwd2 = new PasswordField();
        Text error = new Text();
        name.setPromptText("Username");
        pwd.setPromptText("Password");
        pwd2.setPromptText("Confirm Password");
        email.setPromptText("E-Mail");
        c.setErrorTxt(error);
        Button send = new Button("Register");
        Button backToLogin = new Button("Login");
        name.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) email.requestFocus();
        });
        email.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) pwd.requestFocus();
        });
        pwd.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) pwd2.requestFocus();
        });
        pwd2.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (pwd.getText().equals(pwd2.getText())) {
                    sendRegister(c, name, pwd, email);
                } else {
                    error.setText("Passwords are not identical!");
                }
            }
        });
        backToLogin.setOnAction(e -> stage.getScene().setRoot(getLoginScene(stage, c, name.getText().isBlank() ? null : name.getText())));
        send.setOnAction(e -> {
            if (pwd.getText().equals(pwd2.getText())) {
                sendRegister(c, name, pwd, email);
            } else {
                error.setText("Passwords are not identical!");
            }
        });
        p.getChildren().addAll(name, email, pwd, pwd2, error, send, backToLogin);
        for (int i = 0; i < p.getChildren().size(); i++) {
            p.getChildren().get(i).setLayoutX(10);
            p.getChildren().get(i).setLayoutY(40 * i + 10);
        }
        return p;
    }

    /**
     * sends a message to the server that you want to register
     *
     * @param c     the client to send the data
     * @param name  the name of the user
     * @param pwd   the password of the user
     * @param email the email of the user
     */
    private static void sendRegister(Client c, TextField name, TextField pwd, TextField email) {
        c.setUsername(name.getText());
        User.storePwd(name.getText(), pwd.getText());
        c.send(MessageType.toStr(MessageType.register) + "{name='" + name.getText() + "', pwd='" + c.getCrypto().encrypt(pwd.getText()) + "', email='" + email.getText() + "'}");
    }

    /**
     * creates and return the screen to select in which world you want to play
     *
     * @param stage the stage where everything will be shown
     * @param c     the client to send the selected region
     */
    public static Pane getWorldSelectScreen(Stage stage, Client c) {
        if (c.getUsername() == null) return getLoginScene(stage, c, null);
        Pane p = new Pane();
        Image img = null;
        try {
            img = new Image(String.valueOf(Path.of("./res/LogScreen/ProfileSelect.png").toUri().toURL()));
        } catch (MalformedURLException ignored) {
        }
        assert img != null;
        BackgroundSize fullSize = new BackgroundSize(-1.0D, -1.0D, true, true, true, true);
        p.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, fullSize)));
        Button logout = new Button("Logout");
        Button delete = new Button("Delete");
        Button ownWorld = new Button("Own World");
        TextField otherUser = new TextField();
        otherUser.setPromptText("Enter Username");
        Text error = new Text();
        c.setErrorTxt(error);
        Button send = new Button("Send");
        p.getChildren().addAll(ownWorld, otherUser, send, error);

        ownWorld.setOnAction(e -> {
            c.getErrorTxt().setText("");
            c.getErrorTxt().setVisible(false);
            c.send(MessageType.toStr(MessageType.worldSelect) + "{n='" + c.getUsername() + "'}");
        });
        send.setOnAction(e -> {
            c.getErrorTxt().setText("");
            c.getErrorTxt().setVisible(false);
            c.send(MessageType.toStr(MessageType.worldSelect) + "{n='" + otherUser.getText() + "'}");
        });
        logout.setOnAction(e -> sendLogout(c, stage));
        delete.setOnAction(e -> stage.getScene().setRoot(getDeleteScreen(stage, c)));

        for (int i = 0; i < p.getChildren().size(); i++) {
            p.getChildren().get(i).setLayoutX(130 * i + 10);
            p.getChildren().get(i).setLayoutY(10);
        }
        error.setLayoutX(10);
        error.setLayoutY(50);
        Button[] buttons = new Button[5];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Button("World " + (i + 1));
            buttons[i].setLayoutX(10);
            buttons[i].setLayoutY(30 * (i + 3));
            int finalI = i;
            buttons[i].setOnAction(e -> {
                c.getErrorTxt().setText("");
                c.getErrorTxt().setVisible(false);
                c.send(MessageType.toStr(MessageType.worldSelect) + "{n='" + finalI + "'}");
            });
            p.getChildren().add(buttons[i]);
        }

        return p;
    }

    /**
     * creates and returns a screen to delete the user
     *
     * @param stage the stage to draw on
     * @param c     the client to send the confirmation to the server
     */
    private static Pane getDeleteScreen(Stage stage, Client c) {
        Pane p = new Pane();
        Text question = new Text("Are you sure you want to delete your User? Please enter your password to confirm!");
        Text error = new Text("");
        c.setErrorTxt(error);
        Button send = new Button("Yes");
        Button back = new Button("No");
        PasswordField pwd = new PasswordField();

        send.setOnAction(e -> c.send(MessageType.toStr(MessageType.delete) + "{name='" + c.getUsername() + "', pwd='" + c.getCrypto().encrypt(pwd.getText()) + "'}"));
        back.setOnAction(e -> stage.getScene().setRoot(getWorldSelectScreen(stage, c)));

        p.getChildren().addAll(question, pwd, error, send, back);
        for (int i = 0; i < p.getChildren().size(); i++) {
            p.getChildren().get(i).setLayoutX(10);
            p.getChildren().get(i).setLayoutY(50 * i + 10);
        }
        question.setY(30);
        return p;
    }

    /**
     * sends aa message to the server that the client wants to logout
     *
     * @param c     the client to send the data
     * @param stage the stage to update the scene
     */
    private static void sendLogout(Client c, Stage stage) {
        String s = MessageType.toStr(MessageType.logout);
        User.delLoginData();
        stage.getScene().setRoot(getLoginScene(stage, c, null));
        c.setUsername(null);
        c.send(s);
    }

    /**
     * returns the gameScreen to draw on
     */
    public static Pane getGameScreen() {
        Pane p = new Pane();
        Canvas can = new Canvas();
        Pane menu = new Pane();
        menu.setMinWidth(1000);
        menu.setMinHeight(1000);
        Button b = new Button("Pokemon");
        b.setPrefWidth(200);
        b.setLayoutX(800);
        menu.getChildren().add(b);
        menu.setVisible(false);
        p.getChildren().addAll(can, menu);
        return p;
    }

    /**
     * creates and returns the loadingScreen
     *
     * @throws MalformedURLException if the background image is not found
     */
    public static Pane getLoadingScreen() throws MalformedURLException {
        Pane p = new Pane();
        Image img = new Image(String.valueOf(Path.of("./res/LogScreen/Intro.png").toUri().toURL()));
        BackgroundSize fullSize = new BackgroundSize(-1.0D, -1.0D, true, true, true, true);
        p.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, fullSize)));
        ProgressBar bar = new ProgressBar();
        ProgressBar barHidden = new ProgressBar();
        bar.setProgress(0);
        bar.setVisible(false);
        barHidden.setVisible(false);
        p.getChildren().addAll(bar, barHidden);
        return p;
    }

    /**
     * creates and returns the Pane where all nodes for the profile select are on
     *
     * @param stage  the parent stage
     * @param client the client to communicate with the server
     */
    public static Pane getProfileSelectScreen(Stage stage, Client client) {
        Pane p = new Pane();
        Image img = null;
        try {
            img = new Image(String.valueOf(Path.of("./res/LogScreen/ProfileSelect.png").toUri().toURL()));
        } catch (MalformedURLException ignored) {
        }
        assert img != null;
        BackgroundSize fullSize = new BackgroundSize(-1.0D, -1.0D, true, true, true, true);
        p.setBackground(new Background(new BackgroundImage(img, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, fullSize)));
        Pane[] ps = new Pane[3];
        ImagePattern pattern = null;
        try {
            pattern = new ImagePattern(new Image(String.valueOf(Path.of("./res/LogScreen/ProfileSelectBall.png").toUri().toURL())));
        } catch (MalformedURLException ignored) {
        }
        for (int i = 0; i < ps.length; i++) {
            ps[i] = new Pane();
            Rectangle r = new Rectangle();
            r.setFill(pattern);
            ps[i].getChildren().add(r);
            AtomicInteger curState = new AtomicInteger(2);
            int finalI = i;
            ps[i].setOnMouseClicked(e -> {
                if (ps[finalI].getChildren().size() > 1 && ps[finalI].getChildren().get(1) instanceof Rectangle background && curState.get() == 0 && (e.getX() > background.getWidth() + background.getLayoutX() || e.getY() > background.getHeight() + background.getLayoutY() || e.getX() < background.getLayoutX() || e.getY() < background.getLayoutY())) {
                    curState.set(1);
                    try {
                        r.setFill(new ImagePattern(new Image(String.valueOf(Path.of("./res/LogScreen/ProfileSelectBallClose.gif").toUri().toURL()))));
                    } catch (MalformedURLException ignored) {
                    }
                    new Thread(() -> {
                        try {
                            ps[finalI].getChildren().stream().skip(1).forEach(f -> {
                                FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), f);
                                fadeTransition.setFromValue(0.8);
                                fadeTransition.setToValue(0);
                                fadeTransition.playFromStart();
                            });
                            Thread.sleep(500);
                            curState.set(2);
                            Platform.runLater(() -> {
                                synchronized (ps[finalI].getChildren()) {
                                    while (ps[finalI].getChildren().size() > 1) {
                                        ps[finalI].getChildren().remove(1);
                                    }
                                }
                            });
                        } catch (InterruptedException ignored) {
                        }
                    }).start();
                } else if (curState.get() == 0) {

//                    System.out.println("LoginScreens.getProfileSelectScreen: do some shit with login and so");
                    curState.set(-1);
                    client.send(MessageType.toStr(MessageType.profile) + 1 + finalI);
                    new Thread(() -> {
                        try {
                            p.getChildren().forEach(f -> {
                                FadeTransition fadeTransition = new FadeTransition(Duration.millis(400), f);
                                fadeTransition.setFromValue(f.getOpacity());
                                fadeTransition.setToValue(0);
                                fadeTransition.playFromStart();
                            });
                            Thread.sleep(500);
                            Platform.runLater(() -> {
                                synchronized (p.getChildren()) {
                                    while (p.getChildren().size() > 0) {
                                        p.getChildren().remove(0);
                                    }
                                }
                            });
                            stage.getScene().setRoot(getWorldSelectScreen(stage, client));
                            stage.getScene().getRoot().getChildrenUnmodifiable().forEach(f -> {
                                FadeTransition fadeTransition = new FadeTransition(Duration.millis(900), f);
                                fadeTransition.setFromValue(0);
                                fadeTransition.setToValue(1);
                                fadeTransition.playFromStart();
                            });
                        } catch (InterruptedException ignored) {
                        }
                    }).start();
                } else if (curState.get() == 2) {
                    curState.set(1);
                    Rectangle rec = new Rectangle();
                    rec.setOpacity(0);
                    Text poke = new Text("Poke: " + client.getProfiles()[finalI].poke);
                    Text badge = new Text("Bad: " + client.getProfiles()[finalI].badge);
                    poke.setFill(Color.WHITE);
                    badge.setFill(Color.WHITE);
                    poke.setOpacity(0);
                    badge.setOpacity(0);
                    try {
                        r.setFill(new ImagePattern(new Image(String.valueOf(Path.of("./res/LogScreen/ProfileSelectBallOpen.gif").toUri().toURL()))));
                        rec.setFill(new ImagePattern(new Image(String.valueOf(Path.of("./res/LogScreen/Poke/" + finalI + ".png").toUri().toURL()))));
                    } catch (MalformedURLException ignored) {
                    }
                    ps[finalI].getChildren().addAll(rec, poke, badge);
                    new Thread(() -> {
                        try {
                            Thread.sleep(400);
                            curState.set(0);
                            FadeTransition[] f = new FadeTransition[]{
                                    new FadeTransition(Duration.millis(300), rec),
                                    new FadeTransition(Duration.millis(1000), poke),
                                    new FadeTransition(Duration.millis(1400), badge),
                            };
                            for (FadeTransition fadeTransition : f) {
                                fadeTransition.setFromValue(0);
                                fadeTransition.setToValue(0.8);
                                fadeTransition.playFromStart();
                            }
                        } catch (InterruptedException ignored) {
                        }
                    }).start();
                }
            });
            p.getChildren().add(ps[i]);
        }
        return p;
    }

    /**
     * shows a PlayerProfile with name, nbr of badges and nbr of pokemon
     * just to display at the ProfileSelectScreen
     */
    static class PlayerProfile {

        /**
         * the number of pokemon
         */
        private int poke;

        /**
         * the number of badges
         */
        private int badge;

        public Text getTextField() {
            return textField;
        }

        /**
         * the text where the name is displayed on
         */
        private final Text textField = new Text();

        @Override
        public String toString() {
            return "PlayerProfile{" +
                    ", poke=" + poke +
                    ", badge=" + badge +
                    '}';
        }

        /**
         * inits the Profiles
         *
         * @param str in Format "'Name',NbrOfPokemon,NbrOfBadges"
         */
        public PlayerProfile(String str) {
            if (str != null && str.length() > 0) {
                String[] s = str.split(",");
                if (s.length == 2) {
                    poke = Integer.parseInt(s[0]);
                    badge = Integer.parseInt(s[1]);
                }
            }
        }
    }
}