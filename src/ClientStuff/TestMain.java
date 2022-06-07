package ClientStuff;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//--module-path "C:\Program Files\Java\openjfx-16_windows-x64_bin-sdk\javafx-sdk-16\lib" --add-modules javafx.controls,javafx.fxml

/**
 * @author Zwickelstorfer Felix and Clemenzzzzzzzz Hodina
 * @version idk about 3-4
 */
public class TestMain extends Application {

    /**
     * tests stuff, before implementing it
     *
     * @param args commandline parameter
     */
    public static void main(String[] args) {
        launch(args);
    }

    private final HashMap<Integer, Image> allImgs = new HashMap<>();

    @Override
    public void start(Stage stage) {

        stage.show();
        /*initPokeImgs();

        Pane p = new Pane();
        Scene s = new Scene(p, 400, 400);
        stage.setScene(s);
        stage.show();
        int max = 151;

        final Integer[] id = {1};
        Button next = new Button("Next");
        next.setLayoutX(60);
        next.setLayoutY(10);
        Button bef = new Button("Before");
        bef.setLayoutX(10);
        bef.setLayoutY(10);

        Text t = new Text(id[0] + "");
        t.setLayoutY(50);
        t.setLayoutX(80);

        int size = 150;
        Rectangle r1 = new Rectangle(size, size);

        Rectangle r2 = new Rectangle(size, size);

        r1.setLayoutX(50);
        r1.setLayoutY(100);
        r2.setLayoutX(200);
        r2.setLayoutY(100);

        r1.setFill(new ImagePattern(allImgs.getItem(id[0])));
        r2.setFill(new ImagePattern(allImgs.getItem(-id[0])));
        next.setOnAction(a -> {
            id[0] = id[0] + 1;
            if (id[0] > 151) id[0] = 1;
            t.setText(id[0] + "");
            r1.setFill(new ImagePattern(allImgs.getItem(id[0])));
            r2.setFill(new ImagePattern(allImgs.getItem(-id[0])));
        });

        bef.setOnAction(a -> {
            id[0] = id[0] - 1;
            if (id[0] < 1) id[0] = max;
            t.setText(id[0] + "");
            r1.setFill(new ImagePattern(allImgs.getItem(id[0])));
            r2.setFill(new ImagePattern(allImgs.getItem(-id[0])));
        });
        p.getChildren().addAll(bef, next, t, r1, r2);*/
    }

    private void initPokeImgs() {
        try {
            int maxPoke = 151;
            if (!Files.exists(Path.of("./res/PokemonImgs/1.png"))) {
                Files.deleteIfExists(Path.of("./res/PokemonImgs/"));
                Files.createDirectory(Path.of("./res/PokemonImgs/"));
                for (int i = 1; i <= maxPoke; i++) {
                    try (InputStream in = new URL("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + i + ".png").openStream()) {
                        Files.copy(in, Paths.get("./res/PokemonImgs/" + i + ".png"));
                    } catch (IOException ignored) {
                    }
                    try (InputStream in = new URL("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/" + i + ".png").openStream()) {
                        Files.copy(in, Paths.get("./res/PokemonImgs/" + i + "_back.png"));
                    } catch (IOException ignored) {
                    }
                }
            }
            Files.walk(Path.of("./res/PokemonImgs/")).forEach(f -> {
                Matcher m = Pattern.compile("([0-9]+)(_back)?.[pP][nN][gG]").matcher(f.getFileName().toString());
                if (m.find()) {
                    try {
                        allImgs.put(Integer.parseInt(m.group(1)) * (m.group(2) != null ? -1 : 1), new Image(String.valueOf(f.toFile().toURI().toURL())));
                    } catch (MalformedURLException ignored) {
                    }
                }
            });

            for (int i = 1; i <= maxPoke; i++) {
                if (allImgs.get(i) == null) {
                    allImgs.put(i, new Image(String.valueOf(Path.of("./res/LogScreen/ImgNotFound.png").toUri().toURL())));
                }

                if (allImgs.get(-i) == null) {
                    allImgs.put(-i, new Image(String.valueOf(Path.of("./res/LogScreen/ImgNotFound.png").toUri().toURL())));
                }
            }

        } catch (IOException ignored) {
        }
    }
}
