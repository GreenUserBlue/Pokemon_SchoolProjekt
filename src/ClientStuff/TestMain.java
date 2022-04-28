package ClientStuff;

import JsonParser.JSONParser;
import JsonParser.JSONValue;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

//--module-path "C:\Program Files\Java\openjfx-16_windows-x64_bin-sdk\javafx-sdk-16\lib" --add-modules javafx.controls,javafx.fxml

public class TestMain extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    public static Map<Integer, JSONValue> eventTexts = new HashMap<>();

    @Override
    public void start(Stage stage) {
        BorderPane p = new BorderPane();

        Text txtF = new Text("Hallo Welt\n, kannst du mich umbringen?");
        TextArea t = new TextArea();
//        p.setBottom(t);
        GridPane pa = new GridPane();
        pa.add(t, 0, 1);
//        pa.add(txtF, 0, 0);


        t.setWrapText(true);
        Pane pane2 = new Pane();
        pane2.getChildren().add(txtF);
        t.widthProperty().addListener((a, oldVal, newVal) -> {
            t.setFont(new Font((t.getHeight()) / 2.55 - 7.5));
            txtF.setFont(new Font((((Pane) txtF.getParent()).getHeight()) / 2.55 - 7.5));
        });

//        t.setPrefRowCount(2);
        t.heightProperty().addListener((a, oldVal, newVal) -> {
            t.setFont(new Font((t.getHeight()) / 2.55 - 7.5));
            ScrollBar sc = (ScrollBar) t.lookup(".scroll-bar:vertical");
            if (sc != null) {
                sc.setDisable(true);
                sc.setOpacity(0);
            }
        });
        ColumnConstraints c = new ColumnConstraints();

        RowConstraints r = new RowConstraints();
        RowConstraints r2 = new RowConstraints();
        c.setPercentWidth(100);
        r.setPercentHeight(85);
        r2.setPercentHeight(15);
        pa.getColumnConstraints().add(c);
        pa.getRowConstraints().addAll(r, r2);

        initTexts();
        TextEvent txt = new TextEvent(eventTexts.get(1), t, null);
        txt.nextLine();
        Scene s = new Scene(pa, 300, 300);
        stage.setScene(s);
        stage.show();
        s.setOnMouseClicked(a -> {
            if (txt.getState().toString().equals(TextEvent.TextEventState.reading.toString())) {
                txt.nextLine();
                System.out.println("now");
            }
            if (txt.getState().toString().equals(TextEvent.TextEventState.selection.toString())) {
                System.out.println("selection");
                VBox v = (VBox) txt.getOptionsNode();
                pa.add(v, 0, 0);
//                txt.updateSize(v);

            }
            if (txt.getState() == TextEvent.TextEventState.nothing) {
                System.out.println("finished");
            }
            System.out.println(txt.getState());
            System.out.println(p.getRight());
        });
    }


    private void initTexts() {
        Map<String, JSONValue> c = JSONParser.read(Path.of("./res/DataSets/texts.json"));
        c.forEach((key, value) -> value.getMap().forEach((a, b) -> eventTexts.put(Integer.parseInt(a) + Integer.parseInt(key), b)));
    }

}
