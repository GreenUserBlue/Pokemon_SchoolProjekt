package InGame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Clemens Hodina
 */
public class Item {

    private final int id;

    private final String name;

    public static Item getItem(int itemNbr) {
        return allItems.stream().filter(a -> a.id == itemNbr).findFirst().orElse(null);
    }

    public String getName() {
        return name;
    }

    public int getPrize() {
        return prize;
    }

    private final int prize;

    private final boolean isBuyable;

    protected final int badgesNeeded;

    private final static List<Item> allItems = new ArrayList<>();

    public Item(int id, String name, int prize, boolean isBuyable, int badgesNeeded) {
        this.id = id;
        this.name = name;
        this.prize = prize;
        this.isBuyable = isBuyable;
        this.badgesNeeded = badgesNeeded;
    }

    public static void init(Path p) {
        try {
            List<String> file = Files.readAllLines(p);
            file.forEach(a -> {
                String[] list = a.split(",");
                allItems.add(getItem(list[0].trim(), Integer.parseInt(list[1].trim()), list[2].trim(), Integer.parseInt(list[3].trim()), Boolean.parseBoolean(list[4].trim()), Integer.parseInt(list[5].trim()), Double.parseDouble(list[6].trim())));
            });
        } catch (IOException ignored) {
        }
    }

    public static void init() {
        init(Path.of("./res/DataSets/Items.csv"));
    }

    private static Item getItem(String type, int id, String name, int price, boolean isBuyable, int badgesNeeded, double extraValue) {
        return switch (type) {
            case "Ball" -> new Ball(id, name, price, isBuyable, badgesNeeded, extraValue);
            case "Potion" -> new Potion(id, name, price, isBuyable, badgesNeeded, extraValue);
            default -> new WaterItem(id, name, price, isBuyable, badgesNeeded);
        };
    }

    public static List<Item> getShop(int badges) {
        return new ArrayList<>(allItems.stream().filter(i -> i.isBuyable).filter(i -> i.badgesNeeded <= badges).toList());
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", prize=" + prize +
                ", isBuyable=" + isBuyable +
                ", badgesNeeded=" + badgesNeeded +
                '}';
    }

    public Integer getId() {
        return id;
    }
}
