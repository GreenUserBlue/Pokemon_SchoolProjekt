package InGame;

public class Potion extends Item{
    private int potionId;

    private int healQuantity;

    public Potion(String name, int prize, boolean isBuyable, int potionId, int healQuantity) {
        super(name, prize, isBuyable);
        this.potionId = potionId;
        this.healQuantity = healQuantity;
    }
}
