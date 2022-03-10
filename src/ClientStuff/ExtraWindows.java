package ClientStuff;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class ExtraWindows {

    public static int askQuestion(String q, String title, String... ans) {
        Stage sta = new Stage();
        sta.setResizable(false);
        sta.setTitle(title);
        GridPane p = new GridPane();
        Scene scene = new Scene(p, 250, 100);
        sta.setScene(scene);
        Text t = new Text(q);
        p.add(t, 0, 0);
        p.setPadding(new Insets(10, 10, 10, 10));
        RowConstraints rc = new RowConstraints(30, 30, 30, Priority.ALWAYS, VPos.BOTTOM, true);
        ColumnConstraints cc = new ColumnConstraints(60, 60, 60, Priority.ALWAYS, HPos.LEFT, true);
        p.getRowConstraints().addAll(rc);
        p.getColumnConstraints().add(cc);
        AtomicInteger res = new AtomicInteger();
        for (int i = 0; i < ans.length; i++) {
            Button b = new Button(ans[i]);
            int finalI = i;
            b.setOnAction(e -> {
                res.set(finalI);
                sta.hide();
            });
            p.add(b, i, 1);
        }
        sta.showAndWait();
        return res.get();
    }

    public static String askQuestionToType(String q, String title, String suggestion, String regEx, String... notAllowed) {
        Stage sta = new Stage();
        sta.setResizable(false);
        sta.setTitle(title);
        GridPane p = new GridPane();
        Scene scene = new Scene(p, 300, 180);
        sta.setScene(scene);
        Text t = new Text(q);
        p.add(t, 0, 0);
        p.setPadding(new Insets(10, 10, 10, 10));
        RowConstraints rc = new RowConstraints(40, 40, 40, Priority.ALWAYS, VPos.BOTTOM, true);
        ColumnConstraints cc = new ColumnConstraints(100, 100, 100, Priority.ALWAYS, HPos.LEFT, true);
        p.getRowConstraints().addAll(rc, rc, rc);
        p.getColumnConstraints().add(cc);
        AtomicBoolean isMeantToChange = new AtomicBoolean(false);
        TextField res = new TextField(suggestion);
        res.setMinWidth(200);
        res.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) checkForNotAllowed(sta, isMeantToChange, res, p, regEx, notAllowed);
        });
        p.add(res, 0, 1);
        Button submit = new Button("Submit");
        submit.setOnAction(e -> checkForNotAllowed(sta, isMeantToChange, res, p, regEx, notAllowed));
        p.add(submit, 0, 2);
        sta.showAndWait();
        return isMeantToChange.get() ? res.getText() : suggestion;
    }

    private static void checkForNotAllowed(Stage sta, AtomicBoolean isMeantToChange, TextField res, GridPane p, String regEx, String[] notAllowed) {
        if (Pattern.matches(regEx, res.getText())) {
            if (!Arrays.asList(notAllowed).contains(res.getText())) {
                isMeantToChange.set(true);
                sta.hide();
                return;
            }
        }
        Optional<Node> tex = p.getChildren().stream().skip(1).filter(Text.class::isInstance).findAny();
        if (tex.isEmpty()) {
            Text t = new Text("You tried to do something illegal, \nplease try again!");
            p.add(t, 0, 3);
        } else {
            Text t = (Text) tex.get();
            t.setText(t.getText().substring(0, "You tried to do something illegal".length()) + " again" + t.getText().substring("You tried to do something illegal".length()));
        }
    }
}
