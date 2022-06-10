package ClientStuff;

import Calcs.Vector2D;
import Envir.House;
import InGame.Attack;
import InGame.Pokemon;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    private final HashMap<String, Image> allImgs = new HashMap<>();

    TextEvent txt = new TextEvent();

    private final FightGUI fightGUI = new FightGUI(txt);


    @Override
    public void start(Stage stage) throws IOException {
        Pokemon.init(true);
        TextEvent.initTexts();
        initImgs();
        Pane p = new Pane();
        Canvas can = new Canvas();
        p.getChildren().addAll(can, txt.getGrid());
        int height = 400;
        Scene s = new Scene(p, height * 16D / 9, height);
        stage.setScene(s);
//        stage.show();
        AnimationTimer t = new AnimationTimer() {
            long timeTillNextUse = 0;

            @Override
            public void handle(long l) {
                if (System.currentTimeMillis() > timeTillNextUse) {
                    timeTillNextUse = System.currentTimeMillis() + 500;
                    fightGUI.draw(can, new Vector2D(s.getWidth(), s.getHeight()), allImgs);
//                    System.out.println("hi");
                }
            }
        };
//        fightGUI.startTests();
//        t.start();
//        txt.startNewText(1, null);

        Attack[] iv = new Attack[]{
                Attack.getFromTemp(1),
                Attack.getFromTemp(2),
                Attack.getFromTemp(3),
                Attack.getFromTemp(4),
        };
        Attack[] iv2 = iv.clone();
        Attack[] iv3 = deepClone(iv);
        iv[2] = Attack.getFromTemp(7);
        iv[3].use();
        Arrays.stream(iv).forEach(System.out::println);
        System.out.println();
        Arrays.stream(iv2).forEach(System.out::println);
        System.out.println();
        Arrays.stream(iv3).forEach(System.out::println);
    }

    private <K extends Attack, State> K[] deepClone(K[] iv) {
        K[] res = iv.clone();
        for (int i = 0; i < iv.length; i++) {
            res[i] = (K) iv[i].clone();
        }
        return res;
    }

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
        } catch (MalformedURLException ignored) {
        }
    }
}
