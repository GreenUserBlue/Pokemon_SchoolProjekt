package ClientStuff;

import Calcs.Vector2D;
import Envir.House;
import Envir.World;
import ServerStuff.Server;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Player {

    private static final double walkingSpeed = 0.1;//0.05;//

    private String name;

    private Vector2D pos;

    private Vector2D targetPos;

    private Vector2D houseEntrancePos = null;

    public Vector2D getHouseEntrancePos() {
        return houseEntrancePos;
    }

    public void setHouseEntrancePos(Vector2D houseEntrancePos) {
        this.houseEntrancePos = houseEntrancePos;
    }

    private Vector2D curWalked = new Vector2D(0, 0);

    private String region;

    private int skin;

    private Dir dir;

    private Hands hands = Hands.normal;

    public Activity getActivity() {
        return activity;
    }

    private Activity activity = Activity.standing;

    public Player(String raw) {
        System.out.println(raw);
    }

    public Player(String name, Vector2D pos, int skin, String region) {
        this.name = name;
        this.pos = pos;
        this.skin = skin;
        this.region = region;
        dir = Dir.down;
    }

    public Vector2D getPos() {
        return pos;
    }

    public int getSkin() {
        return skin;
    }

    public Dir getDir() {
        return dir;
    }

    public void setTargetPos(Vector2D targetPos) {
        this.targetPos = targetPos;
    }

    public String getRegion() {
        return region;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
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

    public void updateShownDir(Vector2D pos, Vector2D newVec) {
        dir = Dir.getDir(Vector2D.sub(newVec, pos), dir);
    }

    public String getHands() {
        return hands.toString();
    }

    public void updateHands() {
        hands = hands.next();
    }

    public void updateNewPos(Vector2D newVec, Client c) {
        if (activity == Activity.moving || activity == Activity.standing) {
            if (getPos().equals(newVec, 0.002)) {
                setActivity(Activity.standing);
            } else {
                setActivity(Player.Activity.moving);
                updateShownDir(getPos(), newVec);
            }
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
        }

    }

    public enum Activity {
        fight,
        moving,
        menu,
        standing
    }

    public void setPos(Vector2D pos) {
        this.pos = pos;
    }

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

        public static Vector2D getDirFromKeys(List<Keys> keys) {
            Vector2D res = new Vector2D();
            keys.forEach(e -> {
                Optional<Dir> d = Arrays.stream(Dir.values()).filter(f -> f.toString().equals(e.toString())).findFirst();
                d.ifPresent(value -> res.add(value.getVecDir()));
            });
            return res;
        }

        public Vector2D getVecDir() {
            return var;
        }

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
    }

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

        public abstract Hands next();

        protected static boolean nextLeft = false;
    }
}