package InGame;

public class Ball extends Item{
    private int ballId;

    private double catchRate;

    public Ball(String name, int prize, boolean isBuyable, int ballId, double catchRate) {
        super(name, prize, isBuyable);
        this.ballId = ballId;
        this.catchRate = catchRate;
    }
}
