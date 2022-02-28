package OldMain;

import Calcs.Vector2D;

public class OldPlayer {

    private final Vector2D curMoved = new Vector2D(0, 0);

    private Vector2D pos;

    private int skinID = 0;

    private Dir lookingDir = Dir.Down;

    private CurHands curhands = CurHands.Left;

    private Vector2D moveTo = new Vector2D(0, 0);

    public int getSkinID() {
        return skinID;
    }

    public void setSkinID(int skinID) {
        this.skinID = skinID;
    }

    public Vector2D getCurMoved() {
        return curMoved;
    }

    public Vector2D getPos() {
        return pos;
    }

    public void setPos(Vector2D pos) {
        this.pos = pos;
    }

    public Dir getLookingDir() {
        return lookingDir;
    }

    public CurHands getCurhands() {
        return curhands;
    }

    public void setCurhands(CurHands curhands) {
        this.curhands = curhands;
    }

    public void moveTo(Vector2D dir) {
        moveTo = dir.clone();
        moveTo.sub(pos);
    }

    public void updatePos() {
        double speed = 0.1;
        if ((moveTo.getX() != 0 || curMoved.getX() != 0) && curMoved.getY() == 0) {
            curMoved.add(new Vector2D((!(curMoved.getX() < 0) && (curMoved.getX() > 0 || moveTo.getX() < 0)) ? speed : -speed, 0));
            if (curMoved.getX() > 1) {
                pos.add(new Vector2D(-1, 0));
                curMoved.setX((moveTo.getY() == 0 && moveTo.getX() < 0) ? speed : 0);
                moveTo.setX(0);
            }
            if (curMoved.getX() < -1) {
                pos.add(new Vector2D(1, 0));
                curMoved.setX((moveTo.getY() == 0 && moveTo.getX() > 0) ? -speed : 0);
                moveTo.setX(0);
            }
        }
        if ((moveTo.getY() != 0 || curMoved.getY() != 0) && curMoved.getX() == 0) {
            curMoved.add(new Vector2D(0, (!(curMoved.getY() < 0) && (curMoved.getY() > 0 || moveTo.getY() < 0)) ? speed : -speed));
            if (curMoved.getY() > 1) {
                pos.add(new Vector2D(0, -1));
                curMoved.setY((moveTo.getX() == 0 && moveTo.getY() < 0) ? speed : 0);
                moveTo.setY(0);
            }
            if (curMoved.getY() < -1) {
                pos.add(new Vector2D(0, 1));
                curMoved.setY((moveTo.getX() == 0 && moveTo.getY() > 0) ? -speed : 0);
                curMoved.setX(moveTo.getX() != 0 ? ((moveTo.getX() < 0) ? speed : -speed) : 0);
                moveTo.setY(0);
            }
        }
        curhands = curhands.update();
        lookingDir = curMoved.getX() < 0 ? Dir.Right : curMoved.getX() > 0 ? Dir.Left : curMoved.getY() < 0 ? Dir.Down : curMoved.getY() > 0 ? Dir.Up : lookingDir;
    }

    enum Dir {
        Up, Down, Left, Right
    }

    public enum CurHands {
        Left,
        Right,
        Normal;

        private int count = 0;

        private boolean nextLeft = false;

        public CurHands update() {
            if (count++ > 17) {
                count = 0;
                if (this == Normal) {
                    return ((nextLeft = !nextLeft) ? Right : Left);
                } else {
                    return Normal;
                }
            }
            return this;
        }
    }
}
