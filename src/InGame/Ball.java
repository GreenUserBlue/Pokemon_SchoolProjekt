package InGame;

public class Ball extends Item {

    private double catchRate;

    public Ball(int id,String name, int prize, boolean isBuyable, int badgesNeeded, double catchRate) {
        super(id,name, prize, isBuyable,  badgesNeeded);
        this.catchRate = catchRate;
    }
}
