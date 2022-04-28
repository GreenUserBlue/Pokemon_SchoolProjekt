package InGame;

import Calcs.Vector2D;
import Envir.World;

public class Pokemon {



    private String name;

    private int id;

    private EvolveType evolveType;

    private Attack[] attacks;

    private Nature nature;

    private Type[] type;

    private int level;

    //wie viele xp hat man grade
    private int xp;

    //wie viele xp kann man auf dem Level erreichen maximal
    private int maxXP;

    //block auf dem das Pokemon spawnen kann
    private World.Block block;

    public static void main(String[] args) {
        Attack[] a = new Attack[4];
        Type[] b = new Type[]{Type.grass};
        Pokemon bisasam = new Pokemon("Bisasam", 1, EvolveType.Level, a, Nature.hardy, b, 1, 0, 20);

        Pokemon p = createPokemon(new Vector2D(200, 300), World.Block.Grass);
        p.addExp(23);
        p.addExp(60);
        System.out.println(createPokemon(new Vector2D(0, 1), World.Block.Water));
        System.out.println(p.toMsg());
        System.out.println(Pokemon.getFromMsg(""));
        //Taubsi 1-200 0.4 200-300 0.1
    }


    public Pokemon(String name, int id, EvolveType evolveType, Attack[] attacks, Nature nature, Type[] type, int level, int xp, int maxXP) {
        this.name = name;
        this.id = id;
        this.evolveType = evolveType;
        this.attacks = attacks;
        this.nature = nature;
        this.type = type;
        this.level = level;
        this.xp = xp;
        this.maxXP = maxXP;
    }

    public Pokemon() {
    }

    /**
     * makes a Pokemon from the String which was sent from the server
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
        if (newExP + xp >= maxXP) {
            levelUp(maxXP - xp - newExP);
        } else {
            xp = newExP + xp;
        }
    }

    //xpoverride is wie viele xp man ins nächste level mitnimmt
    private void levelUp(int xpOverride) {
        level++;
        xp = xpOverride;
    }

    private static Pokemon createPokemon(Vector2D pos, World.Block block) {
        if (block.equals(World.Block.Grass)) {
            return new Pokemon();

        } else if (block.equals(World.Block.Water)) {
            return new Pokemon();
        }

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
