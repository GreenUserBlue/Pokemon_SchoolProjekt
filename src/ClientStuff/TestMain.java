package ClientStuff;

import JsonParser.JSONParser;
import JsonParser.JSONValue;
import javafx.application.Application;
import javafx.scene.Scene;
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
        initTexts();
        TextEvent txt = new TextEvent();
        txt.startNewText(eventTexts.get(1), null);
        Scene s = new Scene(txt.getGrid(), 800, 800 / 16D * 9);
        stage.setScene(s);
        stage.show();
        s.setOnMouseClicked(a -> {
            if (txt.getState().toString().equals(TextEvent.TextEventState.reading.toString())) {
                txt.nextLine();
                System.out.println("now");
            }
            if (txt.getState().toString().equals(TextEvent.TextEventState.selection.toString())) {
                System.out.println("selection");
                txt.updateSize();
            }
            if (txt.getState() == TextEvent.TextEventState.nothing) {
                System.out.println("finished");
            }
        });
    }

    private void initTexts() {
        Map<String, JSONValue> c = JSONParser.read(Path.of("./res/DataSets/texts.json"));
        c.forEach((key, value) -> value.getMap().forEach((a, b) -> eventTexts.put(Integer.parseInt(a) + Integer.parseInt(key), b)));
    }
}
