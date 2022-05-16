package InGame;

import Calcs.Vector2D;

import java.util.ArrayList;
import java.util.List;

public class Item {
    private String name;

    private int prize;

    private boolean isBuyable;

    public Item(String name, int prize, boolean isBuyable) {
        this.name = name;
        this.prize = prize;
        this.isBuyable = isBuyable;
    }

    public List<Item> getStandardShop(){
        List<Item> erg = new ArrayList<>();
        erg.add(new Ball("Pokeball", 100, true, 1, 1));
        erg.add(new Ball("Superball", 200, true, 2, 1.5));
        erg.add(new Ball("Hyperball", 500, true, 3, 2));
        erg.add(new Potion("Potion", 250, true, 1, 20));
        erg.add(new Potion("Superpotion", 500, true, 1, 50));
        erg.add(new Potion("Hyperpotion", 700, true, 1, 120));
        return erg;
    }

    public List<Item> getShop(int badges){
        List<Item> erg = new ArrayList<>();
        if (badges < 2){
            erg.add(new Ball("Pokeball", 100, true, 1, 1));
            erg.add(new Potion("Potion", 250, true, 1, 20));
        }
        if (badges < 4 && badges >= 2){
            erg.add(new Ball("Superball", 200, true, 2, 1.5));
            erg.add(new Potion("Superpotion", 500, true, 1, 50));
        }
        if (badges < 6 && badges >= 4){
            erg.add(new Ball("Hyperball", 500, true, 3, 2));
            erg.add(new Potion("Hyperpotion", 700, true, 1, 120));
        }
        return erg;
    }


}
