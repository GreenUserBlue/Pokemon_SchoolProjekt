package Envir;

import Calcs.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Zwickelstorfer Felix
 * a city which breaks the random environment and has houses instead
 */
public class City {

    /**
     * the start pos of the city
     */
    private final Vector2D pos;

    /**
     * the size of the city
     */
    private final Vector2D size;

    public List<House> getHouses() {
        return houses;
    }

    /**
     * all houses inside a city
     */
    private final List<House> houses = new ArrayList<>();

//    public static void main(String[] args) {
//        City c = new City(new Vector2D(-19, 6), new Random(), new Vector2D(20, 30), false, true);
//    }

    public City(Vector2D startPos, Random rnd, Vector2D maxSize, boolean hasArena, boolean hasMarket, int worldID, int houseIDs) {
        this.pos = startPos;
        this.size = maxSize;
        int housesQuantity = rnd.nextInt(4) + 2;
        if (hasArena) {
            houses.add(createNewHouse(rnd, House.Type.arena));
        }
        House hHeal;
        do {
            hHeal = createNewHouse(rnd, House.Type.poke);
        } while (hHeal.isNotFreeForHouse(houses, 2));
        houses.add(hHeal);

        if (hasMarket) {
            House hMarket;
            do {
                hMarket = createNewHouse(rnd, House.Type.market);
            } while (hMarket.isNotFreeForHouse(houses, 2));
            houses.add(hMarket);
        }

        for (int i = 0; i < housesQuantity; i++) {
            House hNormal;
            do {
                hNormal = createNewHouse(rnd, House.Type.normal);
            } while (hNormal.isNotFreeForHouse(houses, 2));
            houses.add(hNormal);
        }

        for (House h : houses) h.setId(worldID, houseIDs++);
    }

    /**
     * creates a new house
     *
     * @param rnd  the rnd value
     * @param type which type of house
     */
    private House createNewHouse(Random rnd, House.Type type) {
        return new House(new Vector2D(pos.getX() + rnd.nextInt((int) (size.getX() - type.getSize().getX() - 1)), pos.getY() + rnd.nextInt((int) (size.getY() - type.getSize().getY() - 1))), type);
    }

    /**
     * to check if a block is inside the city
     *
     * @param v the block to check
     */
    public boolean isInCity(Vector2D v) {
        return pos.getX() <= v.getX() && pos.getX() + size.getX() >= v.getX() && pos.getY() <= v.getY() && pos.getY() + size.getY() >= v.getY();
    }

    @Override
    public String toString() {
        return "City{" +
                "pos=" + pos +
                ", size=" + size +
                ", houses=" + houses +
                '}';
    }

    public Vector2D getPos() {
        return pos;
    }
}
