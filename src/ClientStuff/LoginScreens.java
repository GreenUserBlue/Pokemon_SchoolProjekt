package ClientStuff;

import ServerStuff.MessageType;
import ServerStuff.User;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
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
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Zwickelstorfer Felix
 * creates a login, register and delete screen
 */
public class LoginScreens {


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

    private static void sendLogin(Client c, TextField name, TextField pwd) {
        c.setUsername(name.getText());
        User.storePwd(name.getText(), pwd.getText());
        if (c.getCrypto() == null) return;
        c.send(MessageType.toStr(MessageType.login) + "{name='" + name.getText() + "', pwd='" + c.getCrypto().encrypt(pwd.getText()) + "'}");
    }

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

    private static void sendRegister(Client c, TextField name, TextField pwd, TextField email) {
        c.setUsername(name.getText());
        User.storePwd(name.getText(), pwd.getText());
        c.send(MessageType.toStr(MessageType.register) + "{name='" + name.getText() + "', pwd='" + c.getCrypto().encrypt(pwd.getText()) + "', email='" + email.getText() + "'}");
    }

    public static Pane getRegionSelectScreen(Stage stage, Client c) {
        if (c.getUsername() == null) {
            return getLoginScene(stage, c, null);
        }
        Pane p = new Pane();
        Button logout = new Button("Logout");
        Button delete = new Button("Delete");
        ComboBox<String> combo = new ComboBox<>();
        Text error = new Text();
        c.setErrorTxt(error);
        Button send = new Button("Send");
        combo.getItems().addAll(
                "Kanto",
                "Johto",
                "Sinnoh"
        );
        combo.setPromptText("Region");
        combo.setValue("Kanto");
        p.getChildren().addAll(combo, send, logout, delete, error);
        send.setOnAction(e -> {
            c.getErrorTxt().setText(combo.getValue());
            c.getErrorTxt().setVisible(false);
            c.send(MessageType.toStr(MessageType.region) + "{name='" + combo.getValue() + "'}");
        });
        combo.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                c.getErrorTxt().setText(combo.getValue());
                c.getErrorTxt().setVisible(false);
                c.send(MessageType.toStr(MessageType.region) + "{name='" + combo.getValue() + "'}");
            }
        });
        logout.setOnAction(e -> sendLogout(c, stage));
        delete.setOnAction(e -> stage.getScene().setRoot(getDeleteScreen(stage, c)));
        for (int i = 0; i < p.getChildren().size(); i++) {
            p.getChildren().get(i).setLayoutX(90 * i + 10);
            p.getChildren().get(i).setLayoutY(10);
        }
        error.setLayoutX(10);
        error.setLayoutY(50);
        return p;
    }

    private static Pane getDeleteScreen(Stage stage, Client c) {
        Pane p = new Pane();
        Text question = new Text("Are you sure you want to delete your User? Please enter your password to confirm!");
        Text error = new Text("");
        c.setErrorTxt(error);
        Button send = new Button("Yes");
        Button back = new Button("No");
        PasswordField pwd = new PasswordField();

        send.setOnAction(e -> c.send(MessageType.toStr(MessageType.delete) + "{name='" + c.getUsername() + "', pwd='" + c.getCrypto().encrypt(pwd.getText()) + "'}"));
        back.setOnAction(e -> stage.getScene().setRoot(getRegionSelectScreen(stage, c)));

        p.getChildren().addAll(question, pwd, error, send, back);
        for (int i = 0; i < p.getChildren().size(); i++) {
            p.getChildren().get(i).setLayoutX(10);
            p.getChildren().get(i).setLayoutY(50 * i + 10);
        }
        question.setY(30);
        return p;
    }

    private static void sendLogout(Client c, Stage stage) {
        String s = MessageType.toStr(MessageType.logout);
        User.delLoginData();
        stage.getScene().setRoot(getLoginScene(stage, c, null));
        c.setUsername(null);
        c.send(s);
    }

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
                if (ps[finalI].getChildren().size() > 1 && ps[finalI].getChildren().get(1) instanceof Rectangle background && curState.get() == 0 && (e.getX()  > background.getWidth()+ background.getLayoutX() || e.getY() > background.getHeight()  + background.getLayoutY() || e.getX() < background.getLayoutX() || e.getY() < background.getLayoutY())) {
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
                    if (client.getProfiles()[finalI].name == null) {
                        if (ps[finalI].getChildren().get(2) instanceof Text t) {
                            showNameChangeWindow(t, client, finalI, ps[finalI], stage);
                        }
                    } else {
                        System.out.println("LoginScreens.getProfileSelectScreen: do some shit with login and so");
//                        curState.set(-1);
                    }
                } else if (curState.get() == 2) {
                    curState.set(1);
                    Rectangle rec = new Rectangle();
                    rec.setOpacity(0);
                    Text name = new Text(client.getProfiles()[finalI].name == null ? "Set Username" : client.getProfiles()[finalI].name);
                    Text poke = new Text("Pokemon: " + client.getProfiles()[finalI].poke);
                    Text badge = new Text("Badges: " + client.getProfiles()[finalI].badge);
                    name.setFill(Color.WHITE);
                    poke.setFill(Color.WHITE);
                    badge.setFill(Color.WHITE);
                    name.setOpacity(0);
                    poke.setOpacity(0);
                    badge.setOpacity(0);
                    Button changeName = new Button();
                    changeName.setFocusTraversable(false);
                    try {
                        changeName.setBackground(new Background(new BackgroundImage(new Image(String.valueOf(Paths.get("./res/logScreen/ChangeName.png").toUri().toURL())), BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, fullSize)));
                        changeName.setOpacity(0);
                        r.setFill(new ImagePattern(new Image(String.valueOf(Path.of("./res/LogScreen/ProfileSelectBallOpen.gif").toUri().toURL()))));
                        rec.setFill(new ImagePattern(new Image(String.valueOf(Path.of("./res/LogScreen/Poke/" + finalI + ".png").toUri().toURL()))));
                    } catch (MalformedURLException ignored) {
                    }
                    ps[finalI].getChildren().addAll(rec, name, poke, badge, changeName);
                    client.getProfiles()[finalI].textField = name;
                    changeName.setOnMouseClicked(f -> showNameChangeWindow(name, client, finalI, ps[finalI], stage));
                    new Thread(() -> {
                        try {
                            Thread.sleep(400);
                            curState.set(0);
                            FadeTransition[] f = new FadeTransition[]{
                                    new FadeTransition(Duration.millis(300), rec),
                                    new FadeTransition(Duration.millis(600), name),
                                    new FadeTransition(Duration.millis(600), changeName),
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

    private static void showNameChangeWindow(Text name, Client client, int indexOfArray, Pane p, Stage stage) {
        TextField t = new TextField(name.getText());
        t.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                if (client.getErrorTxt() != null && client.getErrorTxt().getParent() != null) {
                    for (int i = 0; i < stage.getScene().getRoot().getChildrenUnmodifiable().size() && stage.getScene().getRoot().getChildrenUnmodifiable().get(i) instanceof Pane pane; i++) {
                        pane.getChildren().remove(client.getErrorTxt());
                    }
                }
                client.setErrorTxt(new Text("Please wait ..."));
                client.getErrorTxt().setFill(Color.WHITE);
                client.send(MessageType.toStr(MessageType.profile) + '0' + indexOfArray + t.getText());
                if (!p.getChildren().contains(client.getErrorTxt())) p.getChildren().add(client.getErrorTxt());
            } else if (e.getCode() == KeyCode.ESCAPE) {
                int index = p.getChildren().indexOf(t);
                p.getChildren().remove(index);
                p.getChildren().remove(client.getErrorTxt());
                p.getChildren().add(index, name);
            }
        });
        t.setOpacity(0.8);
        t.setStyle("""
                -fx-background-color: transparent;
                   -fx-background-insets: 0, 0 0 0 0 ;
                   -fx-background-radius: 0 ;
                   -fx-padding: 0;
                   -fx-margin: 0;
                   -fx-background-insets: 0;
                   -fx-text-fill: #FFFFFF;""".indent(1));
        int index = p.getChildren().indexOf(name);
        if (index != -1) {
            p.getChildren().remove(index);
            p.getChildren().add(index, t);
        }
        t.requestFocus();
    }

    static class PlayerProfile {

        private String name;

        private int poke;

        private int badge;

        public Text getTextField() {
            return textField;
        }

        private Text textField;

        @Override
        public String toString() {
            return "PlayerProfile{" +
                    "name='" + name + '\'' +
                    ", poke=" + poke +
                    ", badge=" + badge +
                    '}';
        }

        public PlayerProfile(String str) {
            if (str != null && str.length() > 0) {
                String[] s = str.split(",");
                if (s.length == 3) {
                    this.name = s[0];
                    poke = Integer.parseInt(s[1]);
                    badge = Integer.parseInt(s[2]);
                }
            }
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}