package InGame;

public class WaterItem extends Item {

    private static int curIDs = 0;

    public WaterItem(String name, int price, boolean isBuyable, int badgesNeeded) {
        super(name, price, isBuyable, curIDs++, badgesNeeded);
    }
}
