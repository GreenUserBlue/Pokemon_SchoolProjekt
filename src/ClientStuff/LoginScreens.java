package ClientStuff;

import ServerStuff.MessageType;
import ServerStuff.User;
import javafx.animation.FadeTransition;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
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
                if (curState.get() == 0) {
                    System.out.println("send player data");
                } else if (curState.get() != 1) {
                    curState.set(1);
                    Rectangle rec = new Rectangle();
                    rec.setOpacity(0);
                    try {
                        r.setFill(new ImagePattern(new Image(String.valueOf(Path.of("./res/LogScreen/ProfileSelectBallOpen.gif").toUri().toURL()))));
                    } catch (MalformedURLException ignored) {
                    }
                    try {
                        rec.setFill(new ImagePattern(new Image(String.valueOf(Path.of("./res/LogScreen/Poke/" + finalI + ".png").toUri().toURL()))));
                    } catch (MalformedURLException ignored1) {
                    }
                    ps[finalI].getChildren().add(rec);
                    new Thread(() -> {
                        try {
                            Thread.sleep(400);
                            curState.set(0);
                            FadeTransition f = new FadeTransition(Duration.millis(300), rec);
                            f.setFromValue(0);
                            f.setToValue(.8);
                            f.playFromStart();
                        } catch (InterruptedException ignored) {
                        }
                    }).start();
                }
            });
            p.getChildren().add(ps[i]);
        }
        return p;
    }
}

// c18104176