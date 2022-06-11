package InGame;

public class Potion extends Item {

    public double getHealQuantity() {
        return healQuantity;
    }

    private double healQuantity;

    public Potion(int id, String name, int price, boolean isBuyable, int badgesNeeded, double healQuantity) {
        super(id, name, price, isBuyable, badgesNeeded);
        this.healQuantity = healQuantity;
    }
}
