package InGame;

import Calcs.Vector2D;
import Envir.World;

public class Pokemon {

    private String name;

    private int id;

    private EvolveType evolveType;

    private Attack[] attacks;

    private Nature nature;

    public static void main(String[] args) {
        Pokemon p = createPokemon(new Vector2D(200, 300), World.Block.Grass);
        p.addExp(23);
        p.addExp(60);
        System.out.println(createPokemon(new Vector2D(0, 1), World.Block.Water));
        System.out.println(p.toMsg());
        System.out.println(Pokemon.getFromMsg(""));
    }

    /**
     * makes a Pokemon from the String which was send to the server
     *
     * @param msg the message from the server
     * @return
     */
    private static Pokemon getFromMsg(String msg) {

        return new Pokemon();
    }

    /**
     * makes a String to send from the server to the client
     *
     * @return
     */
    private String toMsg() {
        return "Hier ist ein Pokemon namens Ditto mit den Attacken {Tackle,AP4,MAXAP17}{Platcher,AP30,MAXAP30} und dem Wesen usw.";
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

    enum EvolveType {
        //https://www.pokewiki.de/Entwicklung#Entwicklungsmethoden (nicht alle, erste gen reichen die 3)
        Level,
        Trade,
        Item
    }

    enum Nature {
        hardy(1, 1, 1, 1, 1),
        bold(1 / 1.1, 1 * 1.1, 1, 1, 1),
        modest(1 / 1.1, 1, 1 * 1.1, 1, 1),
        calm(1 / 1.1, 1, 1, 1 * 1.1, 1),
        timid(1 / 1.1, 1, 1, 1, 1 * 1.1),
        lonely(1 * 1.1, 1 / 1.1, 1, 1, 1),
        docile(1, 1, 1, 1, 1),
        mild(1, 1 / 1.1, 1 * 1.1, 1, 1),
        gentle(1, 1 / 1.1, 1, 1 * 1.1, 1),
        hasty(1, 1 / 1.1, 1, 1, 1 * 1.1),
        adamant(1 * 1.1, 1, 1 / 1.1, 1, 1),
        impish(1, 1 * 1.1, 1 / 1.1, 1, 1),
        bashful(1, 1, 1, 1, 1),
        careful(1, 1, 1 / 1.1, 1 * 1.1, 1),
        jolly(1, 1, 1 / 1.1, 1, 1 * 1.1),
        naughty(1 * 1.1, 1, 1, 1 / 1.1, 1),
        lax(1, 1 * 1.1, 1, 1 / 1.1, 1),
        rash(1, 1, 1 * 1.1, 1 / 1.1, 1),
        quirky(1, 1, 1, 1, 1),
        naive(1, 1, 1, 1 / 1.1, 1 * 1.1),
        brave(1 * 1.1, 1, 1, 1, 1 / 1.1),
        relaxed(1, 1 * 1.1, 1, 1, 1 / 1.1),
        quiet(1, 1, 1 * 1.1, 1, 1 / 1.1),
        sassy(1, 1, 1, 1 * 1.1, 1 / 1.1),
        serious(1, 1, 1, 1, 1);


        private final double[] values;

        Nature(double att, double def, double spAtt, double spDef, double init) {
            values = new double[]{att, def, spAtt, spDef, init};
        }

        public double[] getEffect() {
            return values;
        }
    }
}
