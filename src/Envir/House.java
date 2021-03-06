package Envir;

import Calcs.Vector2D;
import ClientStuff.Player;
import ServerStuff.Database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zwickelstorfer Felix
 * a house where a player can walk inside
 */
public class House {

    /**
     * the id of the house
     */
    private int id;

    /**
     * the id of the world
     */
    private int worldID;

    public void setId(int worldID, int id) {
        this.worldID = worldID;
        this.id = id;
        if (Database.isConnected()) {
            ResultSet s = Database.get("select * from World inner join House H on World.PK_World_ID = H.FK_World_ID where PK_World_ID = " + worldID + " && H.houseIDInWorld = " + id + ";");
            try {
                assert s != null;
                if (s.first()) {
                    s = Database.get("select name\n" +
                            " from User inner join Player on Player.FK_User_ID = PK_User_ID where PK_Player_ID=" + s.getObject("FK_Owner_ID") + ";");
                    assert s != null;
                    if (s.first()) {
                        this.playerName = s.getString("name");
                    }
                }
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * the pos of the house
     */
    private final Vector2D pos;

    /**
     * the size of the house
     */
    private final Vector2D size;

    /**
     * the housetype
     */
    private final Type type;

    /**
     * all players inside the house
     */
    private final List<Player> players = new ArrayList<>();

    /**
     * the name of the owner of the house (not implemented)
     */
    private String playerName = null;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        if (this.playerName != null && this.playerName.equals(playerName)) {
            return;
        }
        if (Database.isConnected()) {
            if (this.playerName == null) {
                if (playerName != null)
                    Database.execute("insert into house (houseIDInWorld, FK_World_ID, FK_Owner_ID) VALUE (" + id + "," + worldID + ", (select PK_Player_ID from Player inner join User U on Player.FK_User_ID = U.PK_User_ID where U.name='" + playerName + "'));");
            } else {
                if (playerName != null) {
                    Database.execute("update House\n" +
                            "set FK_Owner_ID=(Select PK_Player_ID\n" +
                            "                 From User\n" +
                            "                          inner join Player P on User.PK_User_ID = P.FK_User_ID\n" +
                            "                 where name = '" + playerName + "')\n" +
                            "where FK_World_ID = " + worldID + " && houseIDInWorld = " + id + ";");
                } else {
                    Database.execute("delete\n" +
                            "from House\n" +
                            "where houseIDInWorld = " + id + " && House.FK_World_ID = " + worldID + ";");
                }

            }
        }
        this.playerName = playerName;
    }

    public Type getType() {
        return type;
    }

    public Vector2D getPos() {
        return pos;
    }

    public House(Vector2D pos, Type type) {
        this.pos = pos;
        this.size = type.getSize();
        this.type = type;
    }

    /**
     * if this house can be placed where it wants in relation to he other houses
     * @param houses the other houses
     * @param distance the min distance to other houses
     */
    public boolean isNotFreeForHouse(List<House> houses, int distance) {
        for (House h : houses) {
            if (h.pos.getX() < pos.getX() + size.getX() && pos.getX() < h.pos.getX() + h.size.getX() && h.pos.getY() < pos.getY() + size.getY() + distance && pos.getY() < h.pos.getY() + h.size.getY() + distance) {
                return true;
            }
        }
        return pos.getX() <= 0 && pos.getX() + size.getX() >= 0 && pos.getY() <= 0 && pos.getY() + size.getY() >= 0;
    }

    @Override
    public String toString() {
        return "House{" +
                "id=" + id +
                ", pos=" + pos +
                ", size=" + size +
                ", type=" + type +
                '}';
    }

    public Vector2D getSize() {
        return size;
    }

    /**
     * get the block from an enviromental POV
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public World.Block getBlockEnvir(int x, int y) {
        if (pos.getX() <= x && pos.getX() + size.getX() > x && pos.getY() <= y && pos.getY() + size.getY() > y && (type != Type.arena || (!pos.equals(new Vector2D(x, y)) && !pos.equals(new Vector2D(x - size.getX() + 1, y))))) {
            Vector2D last = Vector2D.add(pos, new Vector2D(0, size.getY() - 1));
            if (x == last.getX() && y == last.getY()) return World.Block.HouseL;
            Vector2D door = Vector2D.add(pos, type.getDoorPos());
            if (x == door.getX() && y == door.getY()) return World.Block.HouseDoor;
            else return World.Block.House;
        }
        return World.Block.none;
    }

    /**
     * get the block from an "inside the House" POV
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public World.Block getBlockInside(int x, int y) {
        Vector2D pos = new Vector2D();
        pos.add(this.pos);
        pos.add(type.getDoorPos());
        pos.sub(type.getInnerDoorPos());
        Vector2D size = type.getInnerSize();
        if (pos.getX() <= x && pos.getX() + size.getX() > x && pos.getY() <= y && pos.getY() + size.getY() > y && (type != Type.arena || (!pos.equals(new Vector2D(x, y)) && !pos.equals(new Vector2D(x - size.getX() + 1, y))))) {
            pos = Vector2D.sub(new Vector2D(x, y), pos);
            World.Block[][] inside = type.getInside();
            if (pos.equals(new Vector2D(0, inside[0].length - 1))) return World.Block.HouseL;
            if (pos.equals(new Vector2D(inside.length - 1, inside[0].length - 1))) return World.Block.HouseR;
            return inside[(int) pos.getX()][(int) pos.getY()];
        }
        Vector2D door = Vector2D.add(pos, type.getInnerDoorPos());
        if (x == door.getX() && y == door.getY() + 1) return World.Block.HouseDoor;
        return World.Block.none;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public enum Type {
        poke {
            @Override
            public Vector2D getSize() {
                return new Vector2D(5, 3);
            }

            @Override
            public Vector2D getInnerSize() {
                return new Vector2D(15, 7);
            }

            @Override
            public Vector2D getDoorPos() {
                return new Vector2D(2, getSize().getY() - 1);
            }

            @Override
            public Vector2D getInnerDoorPos() {
                return new Vector2D(7, getInnerSize().getY() - 1);
            }

            @Override
            public World.Block[][] getInside() {
                World.Block[][] res = new World.Block[(int) getInnerSize().getX()][(int) getInnerSize().getY()];
                for (World.Block[] re : res) Arrays.fill(re, World.Block.Free);
                for (int i = 0; i < res.length; i++) {
                    res[i][0] = World.Block.HouseWall;
                }
                res[0][0] = World.Block.HouseWallL;
                for (int i = 0; i < 7; i++) {
                    for (int j = 0; j < 3; j++) {
                        res[i + 4][j] = World.Block.HouseTable;
                    }
                }
                res[res.length / 2][2] = World.Block.HousePokeHealTalk;
                res[res.length - 2][0] = World.Block.Free;
                res[res.length - 3][0] = World.Block.Free;
                return res;
            }

            @Override
            public double getInnerWallHeight() {
                return 4.55;
            }
        },
        market {
            @Override
            public Vector2D getSize() {
                return new Vector2D(4, 2);
            }

            @Override
            public Vector2D getDoorPos() {
                return new Vector2D(1, getSize().getY() - 1);
            }

            @Override
            public Vector2D getInnerSize() {
                return new Vector2D(10, 9);
            }

            @Override
            public Vector2D getInnerDoorPos() {
                return new Vector2D(2, getInnerSize().getY() - 1);
            }

            @Override
            public World.Block[][] getInside() {
                World.Block[][] res = new World.Block[(int) getInnerSize().getX()][(int) getInnerSize().getY()];
                for (World.Block[] re : res) Arrays.fill(re, World.Block.Free);
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        res[i][j] = World.Block.HouseTable;
                    }
                }

                res[1][3] = World.Block.HouseMarketTalk;

                res[0][0] = World.Block.HouseWallL;
                res[res.length - 1][0] = World.Block.HouseWall;
                res[4][0] = World.Block.HouseWall;

                res[6][2] = World.Block.HouseBigShelfOther;
                res[7][2] = World.Block.HouseBigShelfOther;
                res[7][3] = World.Block.HouseBigShelfOther;
                res[6][3] = World.Block.HouseBigShelf;

                res[9][2] = World.Block.HouseSmallShelfOther;
                res[9][3] = World.Block.HouseSmallShelf;

                res[6][6] = World.Block.HouseSmallShelfOther;
                res[6][7] = World.Block.HouseSmallShelf;

                res[7][6] = World.Block.HouseSmallShelfOther;
                res[7][7] = World.Block.HouseSmallShelf;

                res[9][6] = World.Block.HouseSmallShelfOther;
                res[9][7] = World.Block.HouseSmallShelf;
                return res;
            }

            @Override
            public double getInnerWallHeight() {
                return 7.55;
            }
        },
        arena {
            @Override
            public Vector2D getSize() {
                return new Vector2D(7, 4);
            }

            @Override
            public Vector2D getDoorPos() {
                return new Vector2D(3, getSize().getY() - 1);
            }

            @Override
            public Vector2D getInnerSize() {
                return new Vector2D(7, 10);
            }

            @Override
            public Vector2D getInnerDoorPos() {
                return new Vector2D(3, getInnerSize().getY() - 1);
            }

            @Override
            public World.Block[][] getInside() {
                World.Block[][] res = new World.Block[(int) getInnerSize().getX()][(int) getInnerSize().getY()];
                for (World.Block[] re : res) Arrays.fill(re, World.Block.Free);
//                for (int i = 0; i < 4; i++) {
//                    for (int j = 0; j < 4; j++) {
//                        res[i][j] = World.Block.HouseTable;
//                    }
//                }
                res[0][0] = World.Block.HouseWallL;
                return res;
            }

            @Override
            public double getInnerWallHeight() {
                return 6;
            }
        },
        normal {
            @Override
            public Vector2D getSize() {
                return new Vector2D(4, 3);
            }

            @Override
            public Vector2D getInnerSize() {
                return new Vector2D(9, 5);
            }

            @Override
            public Vector2D getDoorPos() {
                return new Vector2D(1, getSize().getY() - 2);
            }

            @Override
            public Vector2D getInnerDoorPos() {
                return new Vector2D(1, getInnerSize().getY() - 1);
            }

            @Override
            public World.Block[][] getInside() {
                World.Block[][] res = new World.Block[(int) getInnerSize().getX()][(int) getInnerSize().getY()];
                for (World.Block[] re : res) Arrays.fill(re, World.Block.Free);
                return res;
            }

            @Override
            public double getInnerWallHeight() {
                return 3;
            }
        };

        /**
         * @return the size of the house
         */
        public abstract Vector2D getSize();

        /**
         * @return the size inside the house
         */
        public abstract Vector2D getInnerSize();

        /**
         * @return the position of the door outside
         */
        public abstract Vector2D getDoorPos();

        /**
         * @return the position of the door inside
         */
        public abstract Vector2D getInnerDoorPos();

        /**
         * @return all Blocks inside the house
         */
        public abstract World.Block[][] getInside();

        /**
         * @return how many blocks the image of the wall is
         */
        public abstract double getInnerWallHeight();
    }
}
