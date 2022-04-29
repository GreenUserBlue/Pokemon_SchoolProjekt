package InGame;

import Calcs.Vector2D;
import Envir.World;

import java.io.*;
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

    private int captureRate;

    //block auf dem das Pokemon spawnen kann
    private World.Block block;

    private String growthRate;

    public static List<Pokemon> template = new ArrayList<>();



    public static void main(String[] args) {
        Pokemon.init();

        Pokemon a = createPokemon(new Vector2D(900,900), World.Block.Water);
        System.out.println(a);
    }



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
            if (oneRow[4].contains("water") || oneRow[4].contains("sea")) {
                block = World.Block.Water;
            } else if (oneRow[4].equals("rare")) {
                block = null;
            } else {
                block = World.Block.Grass;
            }
            template.add(new Pokemon(oneRow[1], Integer.parseInt(oneRow[0]), null, null, null, null, 1, 0, maxXp, Integer.parseInt(oneRow[2]), block, oneRow[5]));
        }


    }

    /**
     * @param levelType
     * @param curLevel  das is das tatsächlich derzeitige Level
     * @return
     */
    private static int getXpNeeded(String levelType, int curLevel) {
        int maxXp = 0;
        curLevel++;
        if (levelType.equals("fast")) {
            maxXp = (int) ((4 * Math.pow(curLevel, 3)) / 5);
        } else if (levelType.equals("medium")) {
            maxXp = (int) Math.pow(curLevel, 3);
        } else if (levelType.equals("medium-slow")) {
            maxXp = (int) (((6 * Math.pow(curLevel, 3)) / 5) - (15 * Math.pow(curLevel, 2)) + (100 * curLevel) - 140);
        } else {//if (levelType.equals("slow"))
            maxXp = (int) ((5 * Math.pow(curLevel, 3)) / 4);
        }
        return maxXp;
    }




    public Pokemon(String name, int id, EvolveType evolveType, Attack[] attacks, Nature nature, Type[] type, int level, int xp, int maxXP, int captureRate, World.Block block, String growthRate) {
        this.name = name;
        this.id = id;
        this.evolveType = evolveType;
        this.attacks = attacks;
        this.nature = nature;
        this.type = type;
        this.level = level;
        this.xp = xp;
        this.maxXP = maxXP;
        this.captureRate = captureRate;
        this.block = block;
        this.growthRate = growthRate;
    }


    //TODO neues Bild





    //TODO die Stats an das Level anpassen
    private static Pokemon getPokemon(int id, int level) {
        Pokemon a = new Pokemon();
        try {
            a = template.get(id-1).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        a.level = level;
        a.xp = getXpNeeded(a.growthRate, level-1);//TODO is die xp wenn man gerade auf das level gekommen ist
        a.maxXP=getXpNeeded(a.growthRate, level);
        return a;
    }

    @Override
    protected Pokemon clone() throws CloneNotSupportedException {
        return new Pokemon(name, id, evolveType, attacks, nature, type, level, xp, maxXP, captureRate, block, growthRate);
    }

    public Pokemon() {
    }

    public Pokemon(int id, int level) {
        this.id = id;
        this.level = level;
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
    //TODO es is nicht beachtet wegen Grass und Wasser
    //TODO vl schöner machen und sachen in Methoden auslagern
    private static Pokemon createPokemon(Vector2D pos, World.Block block) {
        int distance = (int) pos.magnitude();
        int level = (int) Math.sqrt(distance);
        List<Pokemon> possibilities = new ArrayList<>();
        if (level == 0) {
            level = 1;
        }
        if (block.equals(World.Block.Grass)) {
            if (distance >= 0 && distance <= 150) {
                for (Pokemon pokemon : template) {
                    if (pokemon.captureRate >= 150 && pokemon.captureRate <= 255 && pokemon.block == World.Block.Grass) {
                        possibilities.add(pokemon);
                    }
                }
            } else if (distance > 150 && distance <= 500) {
                for (Pokemon pokemon : template) {
                    if (pokemon.captureRate >= 100 && pokemon.captureRate <= 255 && pokemon.block == World.Block.Grass) {
                        possibilities.add(pokemon);
                    }
                }
            } else if (distance > 500 && distance <= 3000) {
                for (Pokemon pokemon : template) {
                    if (pokemon.captureRate >= 50 && pokemon.captureRate <= 150 && pokemon.block == World.Block.Grass) {
                        possibilities.add(pokemon);
                    }
                }
            } else if (distance > 3000 && distance <= 10000) {
                for (Pokemon pokemon : template) {
                    if (pokemon.captureRate >= 4 && pokemon.captureRate <= 100 && pokemon.block == World.Block.Grass) {
                        possibilities.add(pokemon);
                    }
                }
            }
            int number = (int) (Math.random() * (possibilities.size() - 1 + 1) + 1);
            return getPokemon(possibilities.get(number).id, level);
        } else if (block.equals(World.Block.Water)) {
            if (distance >= 0 && distance <= 150) {
                for (Pokemon pokemon : template) {
                    if (pokemon.captureRate >= 150 && pokemon.captureRate <= 255 && pokemon.block == World.Block.Water) {
                        possibilities.add(pokemon);
                    }
                }
            } else if (distance > 150 && distance <= 500) {
                for (Pokemon pokemon : template) {
                    if (pokemon.captureRate >= 100 && pokemon.captureRate <= 255 && pokemon.block == World.Block.Water) {
                        possibilities.add(pokemon);
                    }
                }
            } else if (distance > 500 && distance <= 3000) {
                for (Pokemon pokemon : template) {
                    if (pokemon.captureRate >= 50 && pokemon.captureRate <= 150 && pokemon.block == World.Block.Water) {
                        possibilities.add(pokemon);
                    }
                }
            } else if (distance > 3000 && distance <= 10000) {
                for (Pokemon pokemon : template) {
                    if (pokemon.captureRate >= 4 && pokemon.captureRate <= 100 && pokemon.block == World.Block.Water) {
                        possibilities.add(pokemon);
                    }
                }
            }
            int number = (int) (Math.random() * (possibilities.size() - 1 + 1) + 1);
            return getPokemon(possibilities.get(number).id, level);
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
                ", captureRate=" + captureRate +
                ", block=" + block +
                '}';
    }
}
