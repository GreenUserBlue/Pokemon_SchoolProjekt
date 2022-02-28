package Envir;

import Calcs.SimplexNoise;
import Calcs.Vector2D;
import ClientStuff.Player;
import ServerStuff.Database;
import ServerStuff.MyServer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class World {

    /**
     * zaehlt alle aktuelle Welten
     */
    private static int worldCount = 1;

    private final int seed;

    private final int id;

    private final Random rnd;

    private final List<City> cities = new ArrayList<>();

    private final String name;

    private final HashMap<Vector2D, House> curHouses = new HashMap<>();


    private static final Vector2D format = new Vector2D(16, 9);

    private static final int widthBlocks = 28;//80;//150;//8;//;

    private static final double extraSize = 5;


    public String getName() {
        return name;
    }

    public int getSeed() {
        return seed;
    }

    public World(int seed, String name) {
        id = worldCount++;
        if (Database.isConnected()) {
            try {
                if (!Objects.requireNonNull(Database.get("select * from World where PK_World_ID = " + id + ";")).first()) {
                    Database.execute("insert into world (PK_World_ID, seed) VALUE (" + id + ", " + seed + ");");
                } else {
                    ResultSet s = Database.get("select * from World where PK_World_ID = " + id + ";");
                    assert s != null;
                    if (s.first() && s.getInt("seed") != seed) {
                        Database.execute("update world set seed=" + seed + " where PK_World_ID=" + id + ";");
                    }
                }
            } catch (SQLException ignored) {
            }
        }
        // maybe make only one random
        this.seed = seed;
        this.name = name;
        rnd = new Random(seed);
        Random cities = new Random(135);
        int last = 0;
        int houseIDs = 1;//wegen Datenbank mit 1
        for (int i = 0; i < 2; i++) {
            last += cities.nextInt(15) + 10;
            int rad = cities.nextInt();
            Vector2D start = new Vector2D((int) (Math.sin(rad) * last), (int) (Math.cos(rad) * last));
            this.cities.add(new City(start, rnd, new Vector2D(30, 15), true, true, id, houseIDs));
            houseIDs += this.cities.get(this.cities.size() - 1).getHouses().size();
        }
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder("World{\n");
        for (City c : cities) {
            res.append(c.toString()).append("\n");
        }
        res.append("}");
        return res.toString();
    }

    /**
     * draws the current Scene on Screen for the Client if he is is in an open area (no fight or menu)
     *
     * @param canvas  the Canvas to draw on
     * @param players the players of the all the players on Screen (first is the "main" OldPlayer)
     * @param size    how big the scene/Canvas is
     */
    public void drawEnvir(Canvas canvas, List<Player> players, Vector2D size, Map<String, Image> allImgs) {
        if (size.getX() > 100_000) System.out.println(rnd);
        double blockSize;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, size.getX(), size.getY());
        double w = size.getX();
        double h = size.getY();

        blockSize = getBlockSizeAndResize(canvas, players, size, w, h);

        /*a for (int i = 0; i < widthBlocks + extraSize * 2; i++) {
            for (int j = 0; j < widthBlocks * format.getX() / format.getY() + extraSize * 2; j++) {
                gc.fillRect(i * blockSize + 10, blockSize * j + 5, blockSize - 20, blockSize - 10);
            }
        }*/
        int maxX = (int) (canvas.getWidth() / blockSize) + 1;
        int maxY = (int) (canvas.getHeight() / blockSize) + 1;
        Block[][] blocks = getBlocksEnvir((int) players.get(0).getPos().getX() - (maxX / 2), (int) players.get(0).getPos().getY() - (maxY / 2), maxX, maxY);
        for (int i = 0; i < maxX; i++) {
            for (int j = 0; j < maxY; j++) {
                int x = (i + (int) players.get(0).getPos().getX() - (maxX / 2));
                int y = (j + (int) players.get(0).getPos().getY() - (maxY / 2));
                if (blocks[i][j] == Block.Water) {
                    gc.drawImage(allImgs.get("Water"), blockSize * (i - 0.05), blockSize * (j - 0.05), blockSize * 1.1, blockSize * 1.1);
                } else {
                    gc.drawImage(allImgs.get("Grass" + getGrassGround(x, y)), i * blockSize, j * blockSize, blockSize + 1, blockSize + 1);
                }
            }
        }

        for (int i = 0; i < maxX; i++) {
            for (int j = 0; j < maxY; j++) {
                int x = (i + (int) players.get(0).getPos().getX() - (maxX / 2));
                int y = (j + (int) players.get(0).getPos().getY() - (maxY / 2));
                Block cur = blocks[i][j];
                if (!isTree(x, y)) {
                    if (cur == Block.Grass) {
                        gc.drawImage(allImgs.get("BigGrass"), blockSize * (i - 0.05), blockSize * (j - 0.05), blockSize * 1.1, blockSize * 1.1);
                    }
                }
            }
        }
        for (int j = 0; j < maxY; j++) {
            for (int i = 0; i < maxX; i++) {
                int x = (i + (int) players.get(0).getPos().getX() - (maxX / 2));
                int y = (j + (int) players.get(0).getPos().getY() - (maxY / 2));
                int finalI = i;
                int finalJ = j;
                Block cur = blocks[i][j];
                if (cur == Block.TreeL && isTreeL(x, y)) {
                    gc.drawImage(allImgs.get("Tree"), blockSize * (i - 0.05), blockSize * (j - 2.1), blockSize * 2.1, blockSize * 3.2);
                } else if (cur == Block.HouseL) {
                    House curHouse = curHouses.get(new Vector2D(i, j));
                    int sizeHouseX = (int) curHouse.getSize().getX();
                    Image img = allImgs.get("House" + curHouse.getType() + "Outside");
                    int sizeImgY = (int) (img.getHeight() / img.getWidth() * sizeHouseX);
                    gc.drawImage(img, blockSize * (finalI), blockSize * (finalJ - sizeImgY + 1), blockSize * (sizeHouseX), blockSize * sizeImgY);
                }
                players.stream().filter(e -> (int) e.getPos().getX() == x && (int) e.getPos().getY() == y).forEach(e -> {
                    if (e.getActivity() != Player.Activity.moving && (e.getActivity() != Player.Activity.menu || (e.getPos().getX() % 1 == 0 && e.getPos().getY() % 1 == 0))) {
                        if (cur == Block.Grass) {
                            //draws the "half"-player in Grass
                            Image img = allImgs.get("Player" + e.getSkin() + e.getDir() + "normal");
                            gc.drawImage(img, 0, 0, img.getWidth(), img.getHeight() * 0.7, blockSize * (finalI + 0.05 - e.getPos().getX() % 1), blockSize * (finalJ - 0.35 - e.getPos().getY() % 1), blockSize * 0.9, blockSize * 0.9);
                        } else {
                            gc.drawImage(allImgs.get("Player" + e.getSkin() + e.getDir() + "normal"), blockSize * (finalI + 0.05 - e.getPos().getX() % 1 * -1), blockSize * (finalJ - 0.35 - e.getPos().getY() % 1 * -1), blockSize * 0.9, blockSize * (0.9 / 0.7));
                        }
                    } else {
                        gc.drawImage(allImgs.get("Player" + e.getSkin() + e.getDir() + e.getHands()), blockSize * (finalI + 0.05 - e.getPos().getX() % 1 * -1), blockSize * (finalJ - 0.35 - e.getPos().getY() % 1 * -1), blockSize * 0.9, blockSize * (0.9 / 0.7));
                    }
                });
            }
        }
    }

    private Block[][] getBlocksEnvir(int startX, int startY, int maxX, int maxY) {
        World.Block[][] res = new Block[maxX][maxY];
        for (int i = 0; i < maxX; i++) {
            for (int j = 0; j < maxY; j++) {
                int x = i + startX;
                int y = j + startY;
                res[i][j] = getSingleBlockEnvir(i, j, x, y);
            }
        }
        return res;
    }

    private Block getSingleBlockEnvir(int i, int j, int x, int y) {
        Vector2D pos = new Vector2D(x, y);
        Optional<City> op = cities.stream().filter(a -> a.isInCity(pos)).findFirst();
        if (op.isPresent()) {
            return getHouseBlock(i, j, x, y, op.get());
        } else {
            return getBlockEnvir(x, y, false);
        }
    }

    private Block getHouseBlock(int i, int j, int x, int y, City c) {
        Optional<House> hOp = c.getHouses().stream().filter(a -> a.getBlockEnvir(x, y) != Block.none).findFirst();
        if (hOp.isEmpty()) {
            return Block.none;
        } else {
            curHouses.put(new Vector2D(i, j), hOp.get());
            return hOp.get().getBlockEnvir(x, y);
        }
    }

    private double getBlockSizeAndResize(Canvas canvas, List<Player> pos, Vector2D size, double w, double h) {
        double blockSize;
        if (w / h > World.format.getX() / World.format.getY()) {      // bigger smaller char change to reverse the effect
            h = w / World.format.getX() * World.format.getY();
        } else if (w / h < World.format.getX() / World.format.getY()) {
            w = h * World.format.getX() / World.format.getY();
        }
        blockSize = w / World.widthBlocks;
        canvas.setLayoutX(-((w - size.getX()) / 2) - World.extraSize * blockSize + pos.get(0).getPos().getX() % 1 * -1 * blockSize - blockSize * 0.5);
        canvas.setWidth(w + 2 * World.extraSize * blockSize);
        canvas.setLayoutY(-((h - size.getY()) / 2) - World.extraSize * blockSize + pos.get(0).getPos().getY() % 1 * -1 * blockSize);
        canvas.setHeight(h + 2 * World.extraSize * blockSize);
        return blockSize;
    }

    private double getNoise(int x, int y) {
        return SimplexNoise.noise((x) / 27.0 + seed, (y) / 27.0 + seed, 0.4);
    }

    private int getGrassGround(int a, int b) {
        double target = ((getNoise(a, b) + 1) * 500) % 10;
        if (target < 0.4) return 1;
        if (target < 5.4) return 0;
        if (target < 6) return 2;
        if (target < 8) return 0;
        if (target < 8.8) return 3;
        return 0;
//        return Math.random() < 0.8 ? 0 : (int) (Math.random() * (probs - 1) + 1);
    }

    private Block getBlockEnvir(int x, int y, boolean rec) {
        if (!rec && isTreeL(x, y)) return Block.TreeL;
        if (!rec && isTree(x, y)) return Block.Tree;
        double d = getNoise(x, y);
        return d > 0 && d < 0.6 ? Block.Free : d > 0.9 ? Block.Water : d > -0.4 ? Block.Grass : Block.Tree;
    }

    public void drawInsideHouse(Canvas canvas, List<Player> players, Vector2D size, Map<String, Image> allImgs) {
        double blockSize;
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, size.getX(), size.getY());
        double w = size.getX();
        double h = size.getY();

        blockSize = getBlockSizeAndResize(canvas, players, size, w, h);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int maxX = (int) (canvas.getWidth() / blockSize) + 1;
        int maxY = (int) (canvas.getHeight() / blockSize) + 1;
        House house = getHouse(players.get(0).getHouseEntrancePos());
        for (int j = 0; j < maxY; j++) {
            for (int i = 0; i < maxX; i++) {
                int x = i + (int) players.get(0).getPos().getX() - (maxX / 2);
                int y = j + (int) players.get(0).getPos().getY() - (maxY / 2);
                if (house.getBlockInside(x, y) != Block.none && house.getBlockInside(x, y) != Block.HouseDoor) {
                    gc.drawImage(allImgs.get("House" + house.getType() + "Floor"), blockSize * (i), blockSize * (j), blockSize, blockSize);
                }
            }
        }
        for (int j = 0; j < maxY; j++) {
            for (int i = 0; i < maxX; i++) {
                int x = (i + (int) players.get(0).getPos().getX() - (maxX / 2));
                int y = (j + (int) players.get(0).getPos().getY() - (maxY / 2));
                int finalI = i;
                int finalJ = j;
                switch (house.getBlockInside(x, y)) {
                    case HouseWallL -> {
                        Image img = allImgs.get("House" + house.getType() + "Wall");
                        int sizeHouseX = (int) house.getType().getInnerSize().getX();
                        int sizeImgY = (int) (img.getHeight() / img.getWidth() * sizeHouseX);
                        gc.drawImage(img, blockSize * (finalI), blockSize * (finalJ - sizeImgY + house.getType().getInnerWallHeight()), blockSize * (sizeHouseX), blockSize * sizeImgY + 1);
                    }
                    case HouseBigShelf -> {
                        int sizeHouseX = 2;
                        Image img = allImgs.get("HouseBigShelf");
                        double sizeImgY = (img.getHeight() / img.getWidth() * sizeHouseX);
                        gc.drawImage(img, blockSize * (finalI), blockSize * (finalJ - sizeImgY + 1), blockSize * (sizeHouseX), blockSize * (sizeImgY));
                    }
                    case HouseSmallShelf -> {
                        int sizeHouseX = 1;
                        Image img = allImgs.get("HouseSmallShelf");
                        double sizeImgY = (img.getHeight() / img.getWidth() * sizeHouseX);
                        if (house.getBlockInside(x - 1, y) == Block.HouseSmallShelf) {
                            gc.drawImage(img, blockSize * (finalI + 1), blockSize * (finalJ - sizeImgY + sizeHouseX), blockSize * -(sizeHouseX), blockSize * (sizeImgY));
                        } else {
                            gc.drawImage(img, blockSize * (finalI), blockSize * (finalJ - sizeImgY + 1), blockSize * (sizeHouseX), blockSize * (sizeImgY));
                        }
                    }
                    case HouseL -> {
                        gc.setFill(Color.BLACK);
                        gc.fillPolygon(new double[]{blockSize * (finalI) - 10, blockSize * (finalI + 1) + 10, blockSize * (finalI) - 10}, new double[]{blockSize * (finalJ) - 10, blockSize * (finalJ + 1) + 10, blockSize * (finalJ + 1) + 10}, 3);
                    }
                    case HouseR -> {
                        gc.setFill(Color.BLACK);
                        gc.fillPolygon(new double[]{blockSize * (finalI) - 10, blockSize * (finalI + 1) + 10, blockSize * (finalI + 1) + 10}, new double[]{blockSize * (finalJ + 1) + 10, blockSize * (finalJ) - 10, blockSize * (finalJ + 1) + 10}, 3);
                    }
                }
                players.stream().filter(e -> (int) e.getPos().getX() == x && (int) e.getPos().getY() == y).forEach(e -> {
                    if (e.getActivity() == Player.Activity.standing) {
                        gc.drawImage(allImgs.get("Player" + e.getSkin() + e.getDir() + "normal"), blockSize * (finalI + 0.05 - e.getPos().getX() % 1), blockSize * (finalJ - 0.35 - e.getPos().getY() % 1), blockSize * 0.9, blockSize * (0.9 / 0.7));
                    } else {
                        gc.drawImage(allImgs.get("Player" + e.getSkin() + e.getDir() + e.getHands()), blockSize * (finalI + 0.05 - e.getPos().getX() % 1 * -1), blockSize * (finalJ - 0.35 - e.getPos().getY() % 1 * -1), blockSize * 0.9, blockSize * (0.9 / 0.7));
                    }
                });
            }
        }
    }

    public boolean isFreeToWalkHouse(Vector2D pos, Player p) {
        House h = getHouse(p.getHouseEntrancePos());
        Block cur = h.getBlockInside((int) pos.getX(), (int) pos.getY());
        if (cur == Block.HouseDoor && p.getDir() == Player.Dir.down && p.getActivity() == Player.Activity.moving) {
            p.setHouseEntrancePos(null);
            h.getPlayers().remove(p);
        }
        List<Block> notWalkableBlocks = Arrays.asList(Block.none, Block.HouseWall, Block.HouseTable, Block.HouseTableL, Block.HouseWallL, Block.HouseL, Block.HouseR, Block.HouseBigShelf, Block.HouseSmallShelf);
        if (!notWalkableBlocks.contains(cur)) {
            Optional<Player> op = MyServer.getServer().getClients().entrySet().stream().filter(c -> c != null && c.getValue() != null).map(c -> c.getValue().getPlayer()).filter(c -> c != null && c != p && (c.getHouseEntrancePos() == null ? (c.getPos().equals(Vector2D.add(Vector2D.add(h.getPos(), h.getType().getDoorPos()), new Vector2D(0, 1)))) : c.getHouseEntrancePos().equals(p.getHouseEntrancePos())) && (c.getPos().equals(pos) || (c.getActivity() == Player.Activity.moving && Vector2D.add(c.getPos(), c.getDir().getVecDir()).equals(pos)))).findAny();
            op.ifPresent(a -> p.setHouseEntrancePos(Vector2D.add(h.getPos(), h.getType().getDoorPos())));
            return op.isEmpty();
        }
        return false;
    }

    public enum Block {
        Free,
        Grass,
        Tree,
        TreeL,
        Water,
        House,
        HouseL,
        HouseDoor,
        HouseWall,
        HouseWallL,
        HouseR,
        HouseTable,
        HouseTableL,
        HouseBigShelf,
        HouseSmallShelf,
        none
    }


    private boolean isTree(int x, int y) {
        x -= (Math.abs(x)) % 2;
        y -= (Math.abs(y)) % 2;
        for (int i = 0; i < 4; i++) {
            Vector2D p = new Vector2D(x + (int) (i / 2.0), y + (i % 2));
            Optional<City> op = cities.stream().filter(a -> a.isInCity(p)).findFirst();
            if (op.isPresent()) return false;
        }
        for (int i = 0; i < 4; i++) {
            if (getBlockEnvir(x + (i / 2), y + (i % 2), true) == Block.Tree) {
                return true;
            }
        }
        return false;
    }

    private boolean isTreeL(int x, int y) {
        return isTree(x, y) && x == x + ((Math.abs(x)) % 2 == 0 ? 0 : 1) && y == y + ((Math.abs(y)) % 2 != 0 ? 0 : 1);
    }

    public boolean isFreeToWalkEnvir(Vector2D pos, Player p) {
        Block cur = getSingleBlockEnvir(-1, -1, (int) pos.getX(), (int) pos.getY());
        House h = null;
        if (cur == Block.HouseDoor && p.getDir() == Player.Dir.up && p.getActivity() == Player.Activity.moving) {
            h = curHouses.get(new Vector2D(-1, -1));
            p.setHouseEntrancePos(Vector2D.add(h.getPos(), h.getType().getDoorPos()));
            h.getPlayers().add(p);
        }
        List<Block> notWalkableBlocks = Arrays.asList(Block.Water, Block.House, Block.HouseL);
        if (!notWalkableBlocks.contains(cur) && !isTree((int) pos.getX(), (int) pos.getY())) {
            House finalH = h;
            //Todoas maybe wenn man sich bewegt, und dazu geht, koennte es zu Problemen kommen, wenn man aus dem Haus kommt oder hineingeht
            Optional<Player> op = MyServer.getServer().getClients().entrySet().stream().filter(c -> c != null && c.getValue() != null).map(c -> c.getValue().getPlayer()).filter(c -> c != null && c != p && (c.getHouseEntrancePos() == null ? p.getHouseEntrancePos() == null : (finalH == null ? c.getHouseEntrancePos().equals(p.getHouseEntrancePos()) : c.getHouseEntrancePos().equals(Vector2D.add(finalH.getPos(), finalH.getType().getDoorPos())))) && (c.getPos().equals(pos) || (c.getActivity() == Player.Activity.moving && Vector2D.add(c.getPos(), c.getDir().getVecDir()).equals(pos)))).findAny();
            op.ifPresent(a -> p.setHouseEntrancePos(null));
            return op.isEmpty();
        }
        return false;
    }

    public House getHouse(Vector2D pos) {
        Optional<City> op = cities.stream().filter(a -> a.isInCity(pos)).findFirst();
        if (op.isPresent()) {
            Optional<House> hOp = op.get().getHouses().stream().filter(a -> a.getBlockEnvir((int) pos.getX(), (int) pos.getY()) != Block.none).findFirst();
            if (hOp.isPresent()) return hOp.get();
        }
        return null;
    }
}
