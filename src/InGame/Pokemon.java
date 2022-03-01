package InGame;

import Calcs.Vector2D;
import Envir.World;

public class Pokemon {

    public static void main(String[] args) {
        Pokemon p = createPokemon(new Vector2D(200, 300), World.Block.Grass);
        p.addExp(23);
        p.addExp(60);
        System.out.println(createPokemon(new Vector2D(0, 1), World.Block.Water));
    }

    private void addExp(int newExP) {
        if (newExP > 50) {
            levelUp();
        }
    }


    private void levelUp() {

    }

    private static Pokemon createPokemon(Vector2D pos, World.Block block) {
        return new Pokemon();
    }
}
