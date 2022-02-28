package ClientStuff;

import ServerStuff.MessageType;
import ServerStuff.User;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
        //TODO another pane for menu and so on
        p.getChildren().addAll(can, menu);
        return p;
    }
}