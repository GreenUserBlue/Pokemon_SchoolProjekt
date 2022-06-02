package InGame;

public class Potion extends Item {

    private double healQuantity;

    public Potion(int id, String name, int price, boolean isBuyable, int badgesNeeded, double healQuantity) {
        super(id, name, price, isBuyable, badgesNeeded);
        this.healQuantity = healQuantity;
    }
}
