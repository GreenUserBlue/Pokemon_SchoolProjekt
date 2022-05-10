package ClientStuff;

import Calcs.Vector2D;
import Envir.House;
import Envir.World;
import ServerStuff.MessageType;
import ServerStuff.Server;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * a player inside the game
 *
 * @author Zwickelstorfer Felix
 */
public class Player {

    /**
     * the unique id of the player
     */
    private int id;

    /**
     * how fast a player walks normally (not fast)
     */
    private static final double walkingSpeed = 0.1;

    /**
     * the name of the player
     */
    private String name;

    /**
     * the current position of the player
     */
    private Vector2D pos;

    /**
     * where the player wants to go (which Block)
     */
    private Vector2D targetPos;

    /**
     * the door pos of the house where the player is inside
     */
    private Vector2D houseEntrancePos = null;

    /**
     * how much the player has walked in the current block
     */
    private Vector2D curWalked = new Vector2D(0, 0);

    /**
     * the Name of the region the player is inside
     */
    private String world;

    /**
     * the id of the skin
     */
    private int skin;

    /**
     * the direction in which the player is looking
     */
    private Dir dir;

    /**
     * the position of the hands
     */
    private Hands hands = Hands.normal;

    /**
     * what the player is currently doing
     */
    private Activity activity = Activity.standing;

    public Player(String raw) {
        System.out.println(raw);
    }

    /**
     * initializes the player
     *
     * @param name  {@link Player#name}
     * @param pos   {@link Player#pos}
     * @param skin  {@link Player#skin}
     * @param world {@link Player#world}
     */
    public Player(String name, Vector2D pos, int skin, String world) {
        this.name = name;
        this.pos = pos;
        this.skin = skin;
        this.world = world;
        dir = Dir.down;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    /**
     * initializes the player
     *
     * @param name {@link Player#name}
     * @param pos  {@link Player#pos}
     * @param skin {@link Player#skin}
     * @param id   {@link Player#id}
     */
    public Player(String name, Vector2D pos, int skin, int id) {
        this.name = name;
        this.pos = pos;
        this.skin = skin;
        this.id = id;
        dir = Dir.down;
    }

    public Vector2D getHouseEntrancePos() {
        return houseEntrancePos;
    }

    public void setHouseEntrancePos(Vector2D houseEntrancePos) {
        this.houseEntrancePos = houseEntrancePos;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Vector2D getPos() {
        return pos;
    }

    public void setPos(Vector2D pos) {
        this.pos = pos;
    }

    public int getSkin() {
        return skin;
    }

    public Dir getDir() {
        return dir;
    }

    public void setDir(Dir dir) {
        this.dir = dir;
    }

    public void setTargetPos(Vector2D targetPos) {
        this.targetPos = targetPos;
    }

    public String getWorld() {
        return world;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", pos=" + Vector2D.add(pos, curWalked) +
                ", skin=" + skin +
                '}';
    }

    public String getName() {
        return name;
    }

    public Vector2D getCurWalked() {
        return curWalked;
    }

    /**
     * moves the player to the next location and updates the position and direction
     *
     * @param client        the client from the server
     * @param isDoubleSpeed if the player is walking fast
     * @param w             the world the player is in
     */
    public void updatePos(Server.ClientHandler client, boolean isDoubleSpeed, World w) {
        curWalked.round(4);
        Vector2D targetWay = Vector2D.sub(targetPos, pos);
        double maxInaccuracy = 0.002;
        if (targetWay.equals(new Vector2D(), maxInaccuracy) && curWalked.equals(new Vector2D(), maxInaccuracy)) {
            activity = Activity.standing;
        } else {
            activity = Activity.moving;
            if (curWalked.equals(new Vector2D(), maxInaccuracy)) {
                dir = Dir.getDir(targetWay, dir);
                if (houseEntrancePos == null) {
                    if (!w.isFreeToWalkEnvir(Vector2D.add(pos, dir.getVecDir()), this)) {
                        Dir curDir = Dir.getDir(targetWay, dir);
                        if (curDir == dir) {
                            activity = Activity.standing;
                        } else if (w.isFreeToWalkEnvir(Vector2D.add(pos, curDir.getVecDir()), this)) {
                            dir = curDir;
                        } else {
                            activity = Activity.standing;
                        }
                    }
                } else {
                    if (!w.isFreeToWalkHouse(Vector2D.add(pos, dir.getVecDir()), this)) {
                        Dir curDir = Dir.getDir(targetWay, dir);
                        if (curDir == dir) {
                            activity = Activity.standing;
                        } else if (w.isFreeToWalkHouse(Vector2D.add(pos, curDir.getVecDir()), this)) {
                            dir = curDir;
                        } else {
                            activity = Activity.standing;
                        }
                    }
                }
                client.getKeysPressed().remove(Keys.valueOf(dir.toString()));
            }
            if (activity == Activity.moving) {
                Vector2D add = new Vector2D(dir.getVecDir().getX() * walkingSpeed * (isDoubleSpeed ? 2 : 1), dir.getVecDir().getY() * walkingSpeed * (isDoubleSpeed ? 2 : 1));
                curWalked.add(add);
                curWalked.round(4);
                if (curWalked.anyBigger(1.0 - maxInaccuracy)) {
                    pos.add(dir.getVecDir());
                    curWalked = new Vector2D();
                }
            }
        }
    }

    /**
     * updates the direction the player where he is looking
     *
     * @param pos    the old position of the player
     * @param newVec the new position of the player
     */
    public void updateShownDir(Vector2D pos, Vector2D newVec) {
        dir = Dir.getDir(Vector2D.sub(newVec, pos), dir);
    }

    public String getHands() {
        return hands.toString();
    }

    public void updateHands() {
        hands = hands.next();
    }

    /**
     * updates the position for the client
     *
     * @param newVec the new position of the player
     * @param c      the client because of the world data for houses
     */
    public void updateNewPos(Vector2D newVec, Client c) {
        if (activity == Activity.moving || activity == Activity.standing) {
            if (getPos().equals(newVec, 0.002)) {
                setActivity(Activity.standing);
            } else {
                setActivity(Player.Activity.moving);
                updateShownDir(getPos(), newVec);
            }
        } else if (!getPos().equals(newVec, 0.002)) {
            updateShownDir(getPos(), newVec);
        }
        setPos(newVec);
        House h = c.getWorld().getHouse(newVec);
        if (houseEntrancePos == null) {
            if (h != null && dir == Dir.up) {
                houseEntrancePos = Vector2D.add(h.getPos(), h.getType().getDoorPos());
            }
        } else if (h != null && Vector2D.add(h.getPos(), h.getType().getDoorPos()).equals(houseEntrancePos)) {
            if (newVec.getY() % 1 > 0.2 && h.getBlockInside((int) newVec.getX(), (int) newVec.getY() + 1) == World.Block.HouseDoor && dir == Dir.down) {
                houseEntrancePos = null;
            }
        } else if (h == null && houseEntrancePos.getY() < newVec.getY()) {
            houseEntrancePos = null;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    /**
     * updates the text events for the player (server)
     *
     * @param client      the clienthandler
     * @param keysPressed the current keys which the client has pressed
     * @param w           the world the is inside
     */
    public void updateTextEvents(Server.ClientHandler client, List<Keys> keysPressed, World w) {
        if (activity.equals(Activity.standing)) {
            if (keysPressed.contains(Keys.confirm)) {
                if (client.getTimeTillNextTextField() <= System.currentTimeMillis()) {
                    if (houseEntrancePos == null) {
                        World.Block b = w.getBlockEnvir((int) (getPos().getX() + dir.getVecDir().getX()), (int) (getPos().getY() + dir.getVecDir().getY()), false);
                        if (b.getVal() != -1) {
                            String s = MessageType.toStr(MessageType.textEvent) + 0 + b.getVal();
                            client.send(s);
                            activity = Activity.textEvent;
                        }
                    } else {
                        House h = w.getHouse(houseEntrancePos);
                        World.Block b = h.getBlockInside((int) (getPos().getX() + dir.getVecDir().getX()), (int) (getPos().getY() + dir.getVecDir().getY()));
                        if (b.getVal() != -1) {
                            String s = MessageType.toStr(MessageType.textEvent) + 0 + b.getVal();
                            client.send(s);
                            activity = Activity.textEvent;
                        }
                    }
                }
            }
        }
    }

    /**
     * what the player is doing
     */
    public enum Activity {
        fight,
        moving,
        menu,
        standing,
        textEvent,
    }


    /**
     * the direction in which a player is able to look
     */
    public enum Dir {
        up(new Vector2D(0, -1)),
        down(new Vector2D(0, 1)),
        left(new Vector2D(-1, 0)),
        right(new Vector2D(1, 0)),
        none(new Vector2D());

        private final Vector2D var;

        Dir(Vector2D var) {
            this.var = var;
        }

        /**
         * makes the keys to directions
         *
         * @param keys the keys which are pressed
         */
        public static Vector2D getDirFromKeys(List<Keys> keys) {
            Vector2D res = new Vector2D();
            keys.forEach(e -> {
                Optional<Dir> d = Arrays.stream(Dir.values()).filter(f -> f.toString().equals(e.toString())).findFirst();
                d.ifPresent(value -> res.add(value.getVecDir()));
            });
            return res;
        }

        /**
         * calculates the new direction which depends on the old direction and the target looking position
         *
         * @param target the position where the player should be
         * @param cur    the current direction
         * @return the new calculated direction
         */
        public static Dir getDir(Vector2D target, Dir cur) {
            if (Math.abs(target.getX()) > Math.abs(target.getY())) {
                return target.getX() < 0 ? left : right;
            } else if (Math.abs(target.getX()) < Math.abs(target.getY())) {
                return target.getY() < 0 ? up : down;
            } else {
                if (target.getX() == 0) return cur;
                if (target.getX() < 0 && target.getY() < 0) {
                    if (cur == left) return up;
                    else return left;
                }
                if (target.getX() > 0 && target.getY() < 0) {
                    if (cur == right) return up;
                    else return right;
                }
                if (target.getX() > 0 && target.getY() > 0) {
                    if (cur == down) return right;
                    else return down;
                }
                if (cur == left) return down;
                else return left;
            }
        }

        public Vector2D getVecDir() {
            return var;
        }
    }

    /**
     * which hands are shown for a player if he is walking
     */
    public enum Hands {
        left {
            @Override
            public Hands next() {
                nextLeft = false;
                return normal;
            }
        },
        right {
            @Override
            public Hands next() {
                nextLeft = true;
                return normal;
            }
        },
        normal {
            @Override
            public Hands next() {
                return nextLeft ? left : right;
            }
        };

        /**
         * if the next shown hand is left
         */
        protected static boolean nextLeft = false;

        public abstract Hands next();
    }
}