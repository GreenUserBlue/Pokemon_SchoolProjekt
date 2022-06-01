package InGame;

public class Ball extends Item {

    private double catchRate;

    private static int curIDs=0;

    public Ball(String name, int prize, boolean isBuyable, int badgesNeeded, double catchRate) {
        super(name, prize, isBuyable, curIDs++, badgesNeeded);
        this.catchRate = catchRate;
    }
}
