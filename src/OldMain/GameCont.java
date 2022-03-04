package OldMain;

import Calcs.SimplexNoise;
import Calcs.Vector2D;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zwickelstorfer Felix
 * @version 1.8.8
 */
public class GameCont {

    public static final int widthBlocks = 28;//150;//8;//;

    private final Map<String, Image> allImgs = new HashMap<>();

//    private final Stage stage;

    private final Rectangle[] backgroundOverlay = new Rectangle[4];

    private final Vector2D format = new Vector2D(16, 9);

    private final Map<Integer, OldPlayer> allPlayer = new HashMap<>();

    private OldPlayer linkedOldPlayer;

    private double blockSize;

    private double seed;

    public OldPlayer getLinkedPlayer() {
        return linkedOldPlayer;
    }

    private Scene scene;

    private Pane pane = new Pane();

    private Canvas canvas;

    private GraphicsContext gc;

    private final AnimationTimer timer = new AnimationTimer() {
        private long timeTillNextUse = 0;

        @Override
        public void handle(long l) {
            if (timeTillNextUse < System.currentTimeMillis()) {
                timeTillNextUse = System.currentTimeMillis() + 1000 / 64;// 10;//
//                System.out.println("now");
                try {
                    update();
                } catch (MalformedURLException ignored) {
                }
            }
        }
    };

    public GameCont() {
//        this.stage = stage;
        init();
        System.out.println(pane);
    }

    public void setSeed(double seed) {
        this.seed = seed;
    }

    public void setPos(Vector2D pos) {
        linkedOldPlayer.setPos(pos);
    }

    public void addPlayer(int id, OldPlayer p) {
        allPlayer.put(id, p);
    }

    public void setLinkedPlayer(OldPlayer linkedOldPlayer) {
        this.linkedOldPlayer = linkedOldPlayer;
    }


    private void update() throws MalformedURLException {
        allPlayer.forEach((key, value) -> value.updatePos());
//        linkedOldPlayer.updatePos();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        resize();
        drawEnvironmentAndPlayer();

//            gc.drawImage(new Image(String.valueOf(Paths.getMyClient("res/Screen.jpg").toUri().toURL())), 0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawEnvironmentAndPlayer() {
        Block[][] all = new Block[(int) (canvas.getWidth() / blockSize) + 1][(int) (canvas.getHeight() / blockSize) + 1];
        for (int i = 0; i < all.length; i++) {
            for (int j = 0; j < all[i].length; j++) {
                int x = (int) (i + linkedOldPlayer.getPos().getX() - (all.length / 2));
                int y = (int) (j + linkedOldPlayer.getPos().getY() - (all[i].length / 2));
                if (isTreeFirst(x, y)) {
                    all[i][j] = Block.TreeL;
                    gc.drawImage(allImgs.get("Grass0"), i * blockSize, j * blockSize, blockSize, blockSize);
                } else if (isTree(x, y)) {
                    all[i][j] = Block.Tree;
                    gc.drawImage(allImgs.get("Grass0"), i * blockSize, j * blockSize, blockSize, blockSize);
                } else {
                    all[i][j] = getBlock(x, y);
                    gc.drawImage(allImgs.get("Grass" + getRndImgNbr(x, y)), i * blockSize, j * blockSize, blockSize, blockSize);
                }
                switch (all[i][j]) {
                    case Grass -> gc.drawImage(allImgs.get("BigGrass"), blockSize * (i - 0.05), blockSize * (j - 0.05), blockSize * 1.1, blockSize * 1.1);
                    case Heal -> gc.drawImage(allImgs.get("Heal"), blockSize * (i - 0.05), blockSize * (j - 0.05), blockSize * 1.1, blockSize * 1.1);
                }
            }
        }

        for (int j = 0; j < all[0].length; j++) {
            for (int i = 0; i < all.length; i++) {
                int x1 = (int) (i + linkedOldPlayer.getPos().getX() - (all.length / 2));
                int y1 = (int) (j + linkedOldPlayer.getPos().getY() - (all[i].length / 2));
                int finalI = i;
                int finalJ = j;
                allPlayer.entrySet().stream().filter(e -> e.getValue().getPos().getX() == x1 && e.getValue().getPos().getY() == y1).forEach(e -> {
                    if (e.getValue().getCurMoved().equals(new Vector2D(0, 0))) {
                        if (all[finalI][finalJ] == Block.Grass) {
                            //draws the "half"-player
                            gc.drawImage(allImgs.get("Player" + e.getValue().getSkinID() + e.getValue().getLookingDir() + "normal"), 0, 0, allImgs.get("Player" + e.getValue().getSkinID() + e.getValue().getLookingDir() + "normal").getWidth(), allImgs.get("Player" + e.getValue().getSkinID() + e.getValue().getLookingDir() + "normal").getHeight() * 0.7, blockSize * (finalI + 0.05 - e.getValue().getCurMoved().getX()), blockSize * (finalJ - 0.35 - e.getValue().getCurMoved().getY()), blockSize * 0.9, blockSize * 0.9);
                        } else {
                            gc.drawImage(allImgs.get("Player" + e.getValue().getSkinID() + e.getValue().getLookingDir() + "normal"), blockSize * (finalI + 0.05 - e.getValue().getCurMoved().getX()), blockSize * (finalJ - 0.35 - e.getValue().getCurMoved().getY()), blockSize * 0.9, blockSize * (0.9 / 0.7));
                        }
                    } else {
                        e.getValue().setCurhands(e.getValue().getCurhands().update());
                        gc.drawImage(allImgs.get("Player" + e.getValue().getSkinID() + e.getValue().getLookingDir() + e.getValue().getCurhands().update()), blockSize * (finalI + 0.05 - e.getValue().getCurMoved().getX()), blockSize * (finalJ - 0.35 - e.getValue().getCurMoved().getY()), blockSize * 0.9, blockSize * (0.9 / 0.7));
                    }
                });
                if (all[i][j] == Block.TreeL) {
                    gc.drawImage(allImgs.get("Tree"), blockSize * (i - 0.05), blockSize * (j - 1.1), blockSize * 2.1, blockSize * 3.2);
                }
            }
        }
    }

    private void init() {
        canvas = new Canvas();
        gc = canvas.getGraphicsContext2D();
        pane = new Pane();
        initBackground();
        scene = new Scene(pane, 800, 450);
        pane.getChildren().addAll(canvas);
        pane.getChildren().addAll(Arrays.stream(backgroundOverlay).toList());
        initImgs();
        timer.start();
//        timer.start();
    }

    private void initBackground() {
        for (int i = 0; i < 4; i++) {
            Rectangle r = new Rectangle();
            r.setFill(Color.rgb(123, 244, 123));
            backgroundOverlay[i] = r;
        }
    }

    private void initImgs() {
        try {
            for (int i = 0; i < 4; i++)
                allImgs.put("Grass" + i, new Image(String.valueOf(Paths.get("res/Envir/Grass" + i + ".png").toUri().toURL())));
            allImgs.put("BigGrass", new Image(String.valueOf(Paths.get("res/Envir/BigGrass.png").toUri().toURL())));
            allImgs.put("Tree", new Image(String.valueOf(Paths.get("res/Envir/Tree.png").toUri().toURL())));
            allImgs.put("Heal", new Image(String.valueOf(Paths.get("res/Envir/Heal.png").toUri().toURL())));
            for (int i = 0; i < 1; i++) {
                for (Dir dir : Dir.values()) {
                    for (OldPlayer.CurHands h : OldPlayer.CurHands.values()) {
                        allImgs.put("Player" + i + dir + h, new Image(String.valueOf(Paths.get("res/OldPlayer/" + i + "/" + dir + "/" + h + ".png").toUri().toURL())));
                    }
                }
            }
            System.out.println(allImgs);
        } catch (MalformedURLException ignored) {
        }
    }

    private void setOverlay(double x0, double y0) {
        backgroundOverlay[0].setWidth(x0);
        backgroundOverlay[0].setHeight(scene.getHeight());
        backgroundOverlay[1].setWidth(scene.getWidth());
        backgroundOverlay[1].setHeight(y0);
        backgroundOverlay[2].relocate(scene.getWidth() - x0, 0);
        backgroundOverlay[2].setWidth(x0);
        backgroundOverlay[2].setHeight(scene.getHeight());
        backgroundOverlay[3].relocate(0, scene.getHeight() - y0);
        backgroundOverlay[3].setWidth(scene.getWidth());
        backgroundOverlay[3].setHeight(y0);
    }

    private Block getBlock(int x, int y) {
        double d = getNoise(x, y);
        return d > 0 && d < 0.6 ? Block.Free : d > 0.9 ? Block.Heal : d > -0.4 ? Block.Grass : Block.Tree;
    }

    private double getNoise(int x, int y) {
        return SimplexNoise.noise((x) / 27.0 + seed, (y) / 27.0 + seed, 0.4);
    }

    private boolean isTreeFirst(int x, int y) {
        return isTree(x, y) && x == x + ((Math.abs(x)) % 2 == 0 ? 0 : 1) && y == y + ((Math.abs(y)) % 2 == 0 ? 0 : 1);
    }

    public Scene getScene() {
        return scene;
    }

    private boolean isTree(int x, int y) {
        x -= (Math.abs(x)) % 2;
        y -= (Math.abs(y)) % 2;
        for (int i = 0; i < 4; i++) {
            if (getBlock(x + (i / 2), y + (i % 2)) == Block.Tree) {
                return true;
            }
        }
        return false;
    }

    private int getRndImgNbr(int a, int b) {
        double target = ((getNoise(a, b) + 1) * 500) % 10;
        if (target < 0.4) return 1;
        if (target < 5.4) return 0;
        if (target < 6) return 2;
        if (target < 8) return 0;
        if (target < 8.8) return 3;
        return 0;
//        return Math.random() < 0.8 ? 0 : (int) (Math.random() * (probs - 1) + 1);
    }

    public void resize() {
        double w = scene.getWidth();
        double h = scene.getHeight();
        if (w / h > format.getX() / format.getY()) {      // bigger smaller char change to reverse the effect
            h = w / format.getX() * format.getY();
        } else if (w / h < format.getX() / format.getY()) {
            w = h * format.getX() / format.getY();
        }
        double extraSize = 2.5;
        blockSize = w / widthBlocks;
        canvas.setLayoutX(-((w - scene.getWidth()) / 2) - extraSize * blockSize + linkedOldPlayer.getCurMoved().getX() * blockSize);
        canvas.setWidth(w + 2 * extraSize * blockSize);
        canvas.setLayoutY(-((h - scene.getHeight()) / 2) - extraSize * blockSize + linkedOldPlayer.getCurMoved().getY() * blockSize);
        canvas.setHeight(h + 2 * extraSize * blockSize);
        setOverlay(-((w - scene.getWidth()) / 2), -((h - scene.getHeight()) / 2));
    }

    public void moveTo(Vector2D vector2D) {
        linkedOldPlayer.moveTo(vector2D);
    }

    public enum Block {
        Free,
        Grass,
        Tree,
        Heal,
        TreeL
    }

    enum Dir {
        Up, Down, Left, Right
    }
}
