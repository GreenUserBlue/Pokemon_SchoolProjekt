package InGame;

public class Ball extends Item {

    public double getCatchRate() {
        return catchRate;
    }

    private double catchRate;

    public Ball(int id,String name, int prize, boolean isBuyable, int badgesNeeded, double catchRate) {
        super(id,name, prize, isBuyable,  badgesNeeded);
        this.catchRate = catchRate;
    }

    public int getNCatch() {
        return 255;
    }
}
