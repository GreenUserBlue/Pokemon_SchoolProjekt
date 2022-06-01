package InGame;

public class Potion extends Item {

    private static int curIDs = 0;

    private double healQuantity;

    public Potion(String name, int price, boolean isBuyable, int badgesNeeded, double healQuantity) {
        super(name, price, isBuyable, curIDs++, badgesNeeded);
        this.healQuantity = healQuantity;
    }
}
