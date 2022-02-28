package OldMain;

import Calcs.Vector2D;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;

import java.nio.file.Paths;

public class Main extends Application {

    public boolean[] keysPressed = new boolean[4];

    public boolean[] keysPressedArrow = new boolean[4];

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        GameCont g = new GameCont();
        stage.setTitle("Pokémon");
        stage.setScene(g.getScene());
        stage.getIcons().add(new Image(String.valueOf(Paths.get("res/icon.png").toUri().toURL())));
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        g.getScene().setOnKeyPressed(e -> {
            char ch = Character.toUpperCase(e.getCode().getChar().charAt(0));
            if (ch == 'W') {
                keysPressed[0] = true;
            } else if (ch == 'A') {
                keysPressed[1] = true;
            } else if (ch == 'S') {
                keysPressed[2] = true;
            } else if (ch == 'D') {
                keysPressed[3] = true;
            }

            if (e.getCode() == KeyCode.UP) {
                keysPressedArrow[0] = true;
            } else if (e.getCode() == KeyCode.LEFT) {
                keysPressedArrow[1] = true;
            } else if (e.getCode() == KeyCode.DOWN) {
                keysPressedArrow[2] = true;
            } else if (e.getCode() == KeyCode.RIGHT) {
                keysPressedArrow[3] = true;
            }
//            System.out.println(Arrays.toStr(keysPressedArrow));
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(!stage.isFullScreen());
                g.resize();
            }
        });
        g.getScene().setOnKeyReleased(e -> {
            char ch = Character.toUpperCase(e.getCode().getChar().charAt(0));
            if (ch == 'W') {
                keysPressed[0] = false;
            } else if (ch == 'A') {
                keysPressed[1] = false;
            } else if (ch == 'S') {
                keysPressed[2] = false;
            } else if (ch == 'D') {
                keysPressed[3] = false;
            }

            if (e.getCode() == KeyCode.UP) {
                keysPressedArrow[0] = false;
            } else if (e.getCode() == KeyCode.LEFT) {
                keysPressedArrow[1] = false;
            } else if (e.getCode() == KeyCode.DOWN) {
                keysPressedArrow[2] = false;
            } else if (e.getCode() == KeyCode.RIGHT) {
                keysPressedArrow[3] = false;
            }
        });
        OldPlayer pl = new OldPlayer();
        g.setLinkedPlayer(pl);
        pl.setSkinID(0);
        g.setPos(new Vector2D(0, 1));
        g.setSeed(420 + 13D / 17);
        g.addPlayer(0, pl);
        OldPlayer pl2 = new OldPlayer();
        pl2.setSkinID(0);
        pl2.setPos(new Vector2D(-2, 12));
        g.addPlayer(1, pl2);


        OldPlayer pl3 = new OldPlayer();
        pl3.setSkinID(0);
        pl3.setPos(new Vector2D(2, 12));
        g.addPlayer(2, pl3);
        g.setLinkedPlayer(pl);
        stage.show();

        new AnimationTimer() {
            long timeTillNextUse = 0;
            @Override
            public void handle(long l) {
                if (timeTillNextUse < System.currentTimeMillis()) {
                    timeTillNextUse = System.currentTimeMillis() + 100;
                    g.moveTo(Vector2D.add(g.getLinkedPlayer().getPos(), new Vector2D(keysPressed[1] ? -1 : keysPressed[3] ? 1 : 0, keysPressed[0] ? -1 : keysPressed[2] ? 1 : 0)));
                    pl2.moveTo(Vector2D.add(pl2.getPos(), new Vector2D(keysPressedArrow[1] ? -1 : keysPressedArrow[3] ? 1 : 0, keysPressedArrow[0] ? -1 : keysPressedArrow[2] ? 1 : 0)));
//                    g.moveTo(new Vector2D(keysPressed[1] ? -1 : keysPressed[3] ? 1 : 0, keysPressed[0] ? -1 : keysPressed[2] ? 1 : 0));
                }
            }
        }.start();
    }
}
