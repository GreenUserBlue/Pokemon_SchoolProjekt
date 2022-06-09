package ClientStuff;

import Calcs.Vector2D;
import Envir.World;
import InGame.Pokemon;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Map;

public class FightGUI {

    public FightGUI(TextEvent txt) {
        this.textEvent = txt;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private Client client;

    private TextEvent textEvent;

    private Player thisPlayer;

    private Pokemon enemy;

    private boolean isAgainstPlayer = false;

    public void startNewFight(String s) {

    }

    public void update(String msg) {

    }


    @SuppressWarnings("SynchronizeOnNonFinalField")
    public void draw(Canvas canvas, Vector2D size, Map<String, Image> allImgs) {
        canvas.setWidth(size.getX());
        canvas.setHeight(size.getY());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, size.getX(), size.getY());
        if (thisPlayer == null || enemy == null/* || client == null*/) {//TODO später wieder reinhauen, aber daweil hab ich nix
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, size.getX(), size.getY());
        } else {
            gc.setFill(Color.DARKMAGENTA);
            gc.fillRect(0, 0, size.getX(), size.getY());
            Pokemon pokePlayer;
            synchronized (thisPlayer) {
                pokePlayer = thisPlayer.getPoke().get(0);
            }
            synchronized (enemy) {
                double oneP = size.clone().mult(0.01).getX();
                Image imgForFightBottomGrass = allImgs.get("FightBottomGrass");
                Vector2D sizeForFightBottomGrass = new Vector2D(30, 0);
                sizeForFightBottomGrass.setY(sizeForFightBottomGrass.getX() * imgForFightBottomGrass.getHeight() / imgForFightBottomGrass.getWidth());

                drawOnCenterWithPercentage(gc, imgForFightBottomGrass, new Vector2D(70, 20), sizeForFightBottomGrass, oneP);
                drawOnCenterWithPercentage(gc, enemy.getImage(), new Vector2D(70, 16.5), new Vector2D(20, 20), oneP);

                drawOnCenterWithPercentage(gc, imgForFightBottomGrass, new Vector2D(25, 43), sizeForFightBottomGrass.mult(1.2), oneP);
                drawOnCenterWithPercentage(gc, pokePlayer.getBackImage(), new Vector2D(25, 37), new Vector2D(30, 30), oneP);


                gc.fillRoundRect(oneP * 50, oneP * 38, oneP * 25, oneP * 8, oneP * 2, oneP * 2);

                drawPokemonInfo(gc, oneP, enemy, 5, 2);
                drawPokemonInfo(gc, oneP, pokePlayer, 50, 38);

//                gc.fillText("HalloWelt", 100, 100);
            }
        }
    }

    private void drawPokemonInfo(GraphicsContext gc, double oneP, Pokemon poke, int startX, int startY) {

        gc.setFill(Color.BLACK);
        gc.fillRoundRect(oneP * startX, oneP * startY, oneP * 25, oneP * 8, oneP * 2, oneP * 2);

        gc.setFont(new Font(oneP * 2));
        gc.setFill(Color.WHITE);
        gc.fillText(poke.getName(), oneP * (startX + 1), oneP * (startY + 3));

        gc.fillText((int) poke.getCurHP() + "/" + (int) poke.getMaxHP(), oneP * (startX + 17), oneP * (startY + 3));

        gc.setFill(Color.GRAY);
        gc.fillRoundRect(oneP * (startX + 1), oneP * (startY + 5), oneP * 23, oneP * 1.2, oneP, oneP);
        double x;
        gc.setFill((x = poke.getCurHP() / poke.getMaxHP()) > 0.5 ? Color.GREEN : x > 0.2 ? Color.YELLOW : Color.RED);
        gc.fillRoundRect(oneP * (startX + 1), oneP * (startY + 5), oneP * 23 * x, oneP * 1.2, oneP, oneP);

        gc.setFill(Color.GRAY);
        gc.fillRoundRect(oneP * (startX + .5), oneP * (startY + 7.3), oneP * 24, oneP * .5, oneP, oneP);
        gc.setFill(Color.GREENYELLOW);
        gc.fillRoundRect(oneP * (startX + .5), oneP * (startY + 7.3), oneP * 24 * poke.getXp() / poke.getMaxXPNeeded(), oneP * .5, oneP, oneP);
    }

    public void drawOnCenterWithPercentage(GraphicsContext gc, Image img, Vector2D pos, Vector2D size, double onePercent) {
        Vector2D posit = new Vector2D(onePercent * (pos.getX() - size.getX() / 2), onePercent * (pos.getY() - size.getY() / 2));
        gc.drawImage(img, posit.getX(), posit.getY(), onePercent * (size.getX()), onePercent * (size.getY()));//0, 0, img.getWidth(), img.getHeight()
//        gc.drawImage(img, 100, 100);
//        gc.drawImage(img, posit.getX(), posit.getY());
    }

    public void startTests() {
        isAgainstPlayer = false;
        thisPlayer = new Player("name", new Vector2D(12, 23), 2, "Name");
        thisPlayer.getPoke().add(Pokemon.createPokemon(new Vector2D(6969, 420), World.Block.Grass));
        enemy = Pokemon.createPokemon(new Vector2D(10, 10), World.Block.Grass);
        enemy.setCurHP(enemy.getCurHP() * 2 / 3);
        thisPlayer.getPoke().get(0).setCurHP(thisPlayer.getPoke().get(0).getCurHP() * 1 / 2);
//        System.out.println("myPoke:");
//        System.out.println(thisPlayer.getPoke().get(0).getCurHP() + "/" + thisPlayer.getPoke().get(0).getMaxHP());
    }
}
