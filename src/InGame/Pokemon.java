package InGame;

import Calcs.Vector2D;
import Envir.World;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    private int growthRate;

    //block auf dem das Pokemon spawnen kann
    private World.Block block;

    public static List<Pokemon> template = new ArrayList<>();

    //liest File aus und gibts in Liste
    //und gibts in die tatsächliche Liste
    public static void init() {
        //File file = new File(String.valueOf(Path.of("res/DataSets/pokeFile.txt")));
        BufferedReader in = null;
        String[] lines = new String[151];
        try {
            in = new BufferedReader(new FileReader("res/DataSets/pokeFile.txt"));
            String row = null;
            for (int i = 0; (row = in.readLine()) != null; i++) {
                lines[i] = row;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] oneRow = new String[6];//bis jz nur 6 attribute
        int maxXp = 0;
        World.Block block;
        for (int i = 0; i < 151; i++) {
            oneRow = lines[i].split(";");
            maxXp = getXpNeeded(oneRow[5], 1);
            if (oneRow[4].contains("water")){
                block = World.Block.Water;
            }else{
                block = World.Block.Grass;
            }
            template.add(new Pokemon(oneRow[1], Integer.parseInt(oneRow[0]), null, null, null, null, 1, 0, maxXp, Integer.parseInt(oneRow[2]), block));
        }


    }

    /**
     *
     *
     * @param levelType
     * @param curLevel das is das tatsächlich derzeitige Level
     * @return
     */
    private static int getXpNeeded(String levelType, int curLevel) {
        int maxXp = 0;
        curLevel++;
        if (levelType.equals("fast")){
            maxXp = (int) ((4 * Math.pow(curLevel,3))/5);
        }else if (levelType.equals("medium")){
            maxXp = (int) Math.pow(curLevel,3);
        }else if (levelType.equals("medium-slow")){
            maxXp = (int) (((6 * Math.pow(curLevel,3))/5) - (15 * Math.pow(curLevel,2)) + (100 * curLevel) - 140);
        }else {//if (levelType.equals("slow"))
            maxXp = (int) ((5 * Math.pow(curLevel,3))/4);
        }
        return maxXp;
    }

    public static void main(String[] args) {
        Pokemon.init();
        System.out.println(template);


        /*Attack[] a = new Attack[4];
        Type[] b = new Type[]{Type.grass};
        Pokemon bisasam = new Pokemon("Bisasam", 1, EvolveType.Level, a, Nature.hardy, b,  1, 0, 20);

        Pokemon p = createPokemon(new Vector2D(200, 300), World.Block.Grass);
        p.addExp(23);
        p.addExp(60);
        System.out.println(createPokemon(new Vector2D(0, 1), World.Block.Water));
        System.out.println(p.toMsg());
        System.out.println(Pokemon.getFromMsg(""));
        //Taubsi 1-200 0.4 200-300 0.1

         */
    }




    public Pokemon(String name, int id, EvolveType evolveType, Attack[] attacks, Nature nature, Type[] type, int level, int xp, int maxXP, int growthRate, World.Block block) {
        this.name = name;
        this.id = id;
        this.evolveType = evolveType;
        this.attacks = attacks;
        this.nature = nature;
        this.type = type;
        this.level = level;
        this.xp = xp;
        this.maxXP = maxXP;
        this.growthRate = growthRate;
        this.block = block;
    }

    //TODO eine Methode getPokemon die mit id und level genau das Pokemon übergibt(mit den stats des levels)

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

    //pos für wie weit vom spawnt entfernt
    private static Pokemon createPokemon(Vector2D pos, World.Block block) {
        if (block.equals(World.Block.Grass)) {
            return new Pokemon();

        } else if (block.equals(World.Block.Water)) {
            return new Pokemon();
        }
        //math.sqrt(pos.magnitude) --> das is das level des pokemons pokemons
        //pos.magnitude --> entfernung vom spawn
        //ein legendäres ist jedoch NICHT fangbar
        //pos.magnitude 10000 keine legänderen
        //sonst ranges festlegen(aber die sollen mathematisch berechnet werden)
        //vor allem innerhalb der
        //in range von 0-150 alles zwischen 150-255 und das random
        //150-500 alles von 100-255
        //500-3000 alles von 50-150
        //3000-10000 alles nicht legänderen von 4-100

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

    @Override
    public String toString() {
        return "Pokemon{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", evolveType=" + evolveType +
                ", attacks=" + Arrays.toString(attacks) +
                ", nature=" + nature +
                ", type=" + Arrays.toString(type) +
                ", level=" + level +
                ", xp=" + xp +
                ", maxXP=" + maxXP +
                ", block=" + block +
                '}';
    }
}
