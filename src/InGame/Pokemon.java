package InGame;

import Calcs.Vector2D;
import Envir.World;
import javafx.scene.image.Image;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Clemens Hodina
 */
public class Pokemon {
    //TODO Entwicklungen mit Steinen oder trades
    //Evoli is muell (einfach nur flamara und fertig)

    private String name;

    private int id;

    private EvolveType evolveType;

    private int evolvesIntoId;

    private int evolvesAtLevel;

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

    private double curHP;

    private State state;

    //individual values für stats
    private int[] iv;

    //noch nicht im Konstruktor oder so
    //da steht level bei dem das Pokemon die jeweilige attacke bekommt
    //private final TreeMap<Integer, Attack> attackAtLevel = new TreeMap<Integer, Attack>();

    public static List<Pokemon> template = new ArrayList<>();

    private static final Random rnd = new Random(696969);
    private static final Random attackRnd = new Random(420420);
    private static final Random hitProbRnd = new Random(9824756);

    //attacke mit id x hat type an der stelle x
    public static Type[] attackTypes;


    public static void main(String[] args) throws IOException {
        Attack.init();
        Pokemon.init();
        Type.init();
        /*for (Pokemon pokemon : template) {
            System.out.println(pokemon);
        }

         */

        Pokemon a = createPokemon(new Vector2D(200, 600), World.Block.Grass);
        System.out.println(a);

        /*
            getAttacks(0,40);
        } catch (IOException e) {
            e.printStackTrace();
        }

         */
        Pokemon b = createPokemon(new Vector2D(4000, 7000), World.Block.Grass);
        a.getsAttacked(b, 4, false);

    }


    //liest File aus und gibts in Liste
    //und gibts in die tatsächliche Liste
    public static void init() {
        //File file = new File(String.valueOf(Path.of("res/DataSets/pokeFile.txt")));
        BufferedReader in;
        BufferedReader in2;
        BufferedReader in3;
        String[] lines = new String[151];
        String[] lines2 = new String[151];
        String[] lines3 = new String[152];
        State s;
        try {
            in = new BufferedReader(new FileReader("res/DataSets/pokeFile.txt"));
            in2 = new BufferedReader(new FileReader("res/DataSets/dataList.txt"));
            in3 = new BufferedReader(new FileReader("res/DataSets/Evolutions.csv"));
            String row;
            String row2;
            String row3;
            for (int i = 0; (row = in.readLine()) != null; i++) {
                lines[i] = row;
            }
            for (int i = 0; (row2 = in2.readLine()) != null && i < 151; i++) {
                lines2[i] = row2;
            }
            for (int i = 0; (row3 = in3.readLine()) != null; i++) {
                //System.out.println("i: " + i + "   " + row3);
                if (!row3.isBlank()) {
                    lines3[i] = row3;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] oneRow;
        String[] oneRow2;
        String[] oneRow3;

        int maxXp;
        World.Block block;
        for (int i = 0; i < 151; i++) {
            Type[] types = new Type[2];
            oneRow = lines[i].split(";");
            oneRow2 = lines2[i].split(",");
            oneRow3 = lines3[i].split(";");
            maxXp = getXpNeeded(oneRow[5], 1);
            if (oneRow[4].contains("water") || oneRow[4].contains("sea")) {
                block = World.Block.Water;
            } else if (oneRow[4].equals("rare")) {
                block = null;
            } else {
                block = World.Block.Grass;
            }
            s = new State(Integer.parseInt(oneRow2[5]), Integer.parseInt(oneRow2[6]), Integer.parseInt(oneRow2[7]), Integer.parseInt(oneRow2[8]), Integer.parseInt(oneRow2[9]), Integer.parseInt(oneRow2[10]));
            if (oneRow2[3].equals("")) {
                //oneRow2[3] = null;
                types[0] = Type.valueOf(oneRow2[2].toLowerCase());
                types[1] = null;
            } else {
                types[0] = Type.valueOf(oneRow2[2].toLowerCase());
                //System.out.println(types[1]);
                types[1] = Type.valueOf(oneRow2[3].toLowerCase());
            }
            int evolvesIntoID;
            int evolvesAtLevel = -1;
            if (oneRow3[5] != null && !oneRow3[5].equals("null")) {
                evolvesIntoID = Integer.parseInt(oneRow3[5]);
                try {
                    evolvesAtLevel = Integer.parseInt(oneRow3[3]);
                }catch (NumberFormatException e){
                }

            } else {
                evolvesIntoID = -1;
            }
            template.add(new Pokemon(oneRow[1], Integer.parseInt(oneRow[0]), null, evolvesIntoID, evolvesAtLevel, null, null, types, 1, 0, maxXp, Integer.parseInt(oneRow[2]), block, oneRow[5], s.getHP(), s, new int[6]));
        }


    }

    /**
     * @param levelType how fast a pokemons gains xp
     * @param curLevel  the current level
     * @return how much exp needed for next level
     */
    private static int getXpNeeded(String levelType, int curLevel) {
        int maxXp;
        curLevel++;
        maxXp = switch (levelType) {// wenn hier was nd geht is der switch schuld
            case "fast" -> (int) ((4 * Math.pow(curLevel, 3)) / 5);
            case "medium" -> (int) Math.pow(curLevel, 3);
            case "medium-slow" -> (int) (((6 * Math.pow(curLevel, 3)) / 5) - (15 * Math.pow(curLevel, 2)) + (100 * curLevel) - 140);
            default -> //if (levelType.equals("slow"))
                    (int) ((5 * Math.pow(curLevel, 3)) / 4);
        };
        return maxXp;
    }

    public Pokemon(String name, int id, EvolveType evolveType, int evolvesIntoId, int evolvesAtLevel, Attack[] attacks, Nature nature, Type[] type, int level, int xp, int maxXP, int captureRate, World.Block block, String growthRate, double curHP, State state, int[] iv) {
        this.name = name;
        this.id = id;
        this.evolveType = evolveType;
        this.evolvesIntoId = evolvesIntoId;
        this.evolvesAtLevel = evolvesAtLevel;
        this.attacks = attacks;
        this.nature = nature;
        this.type = type;
        this.level = level;
        this.xp = xp;
        this.maxXP = maxXP;
        this.captureRate = captureRate;
        this.block = block;
        this.growthRate = growthRate;
        this.curHP = curHP;
        this.state = state;
        this.iv = iv;
    }

    private static Pokemon getPokemon(int id, int level) {
        //da id mit 1 beginnt
        id--;
        Pokemon a = new Pokemon();
        try {
            a = template.get(id).clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        a.level = level;
        a.xp = getXpNeeded(a.growthRate, level - 1);//is die xp wenn man gerade auf das level gekommen ist
        a.maxXP = getXpNeeded(a.growthRate, level);
        a.nature = Nature.values()[(int) ((Math.random()) * Nature.values().length)];
        for (int i = 0; i < 6; i++) {
            a.iv[i] = rnd.nextInt(16);
        }
        a.state.add(id, level, a.nature);
        try {
            a.attacks = getAttacks(id, level);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return a;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    protected Pokemon clone() throws CloneNotSupportedException {
        //Pokemon clone = (Pokemon) super.clone();
        return new Pokemon(name, id, evolveType, evolvesIntoId, evolvesAtLevel, attacks, nature, type, level, xp, maxXP, captureRate, block, growthRate, curHP, state, iv);
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
     * @return pokemon from server
     */
    private static Pokemon getFromMsg(String msg) {

        return new Pokemon();
    }

    /**
     * makes a String to send from the server to the client
     *
     * @return string to server
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
        state.add(id, level, nature);
        xp = xpOverride;
        if (level == evolvesAtLevel){
            evolve();
        }
    }

    private void evolve(){
        id = evolvesIntoId;
    }


    //pos für wie weit vom spawnt entfernt
    //vl schöner machen und sachen in Methoden auslagern
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
            int number = (int) (Math.random() * (possibilities.size()));
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


    /**
     * zieht die aktuellen HP ab und verbraucht bei der attacke ein AP
     * man übergibt der Methode ich greife mit Pokemon attacker das pokemon this mit der Attacke attackId an
     *
     * @param attacker das attackierende pokemon
     * @param attackId die attacke mit der es angreift wobei das keinen sinn macht lol
     */
    public void getsAttacked(Pokemon attacker, int attackId, boolean isCrit) {
        Attack at = Attack.template.get(attackId);
        double crit = 1.5;//mimimi leben dürfen nd negativ sein
        double random = (attackRnd.nextInt(15) + 85) / 100D;
        int hitRandom = hitProbRnd.nextInt(100);
        double stab = 1;
        if (at.getType().equals(this.type[0]) || at.getType().equals(this.type[1])) {
            stab = 1.5;
        }
        boolean[] isHitting = new boolean[100];
        double hitProb = at.getHitProbability();
        for (int i = 0; i < 100; i++) {
            if (i < hitProb) {
                isHitting[i] = true;
            }
        }
        if (isHitting[hitRandom]) {
            if (isCrit) {
                if (this.type[1] == null) {
                    curHP = curHP - ((((2 * this.level / 5d) + 2) * at.getDamage() * (this.state.attack / attacker.state.defense) / 50) + 2) * crit * random * stab * at.getType().getAttackMult(this.type[0]);
                } else {
                    curHP = curHP - ((((2 * this.level / 5d) + 2) * at.getDamage() * (this.state.attack / attacker.state.defense) / 50) + 2) * crit * random * stab * at.getType().getAttackMult(this.type[0]) * at.getType().getAttackMult(this.type[1]);
                }
            } else {
                if (this.type[1] == null) {
                    curHP = curHP - ((((2 * this.level / 5d) + 2) * at.getDamage() * (this.state.attack / attacker.state.defense) / 50) + 2) * random * stab * at.getType().getAttackMult(this.type[0]);
                } else {
                    curHP = curHP - ((((2 * this.level / 5d) + 2) * at.getDamage() * (this.state.attack / attacker.state.defense) / 50) + 2) * random * stab * at.getType().getAttackMult(this.type[0]) * at.getType().getAttackMult(this.type[1]);
                }
            }
        } else {
            System.out.println("Attacke hat nicht getroffen");
        }
        System.out.println(curHP);
    }

    public static Attack[] getAttacks(int id, int level) throws IOException {
        Attack[] erg;
        int counter = 0;
        List<Attack> attackList = new ArrayList<>();
        if (level <= 10) {
            erg = new Attack[3];
        } else {
            erg = new Attack[4];
        }
        BufferedReader in;
        List<String> allLines = new ArrayList<>();
        in = new BufferedReader(new FileReader("res/DataSets/movesForPokemon"));
        String row;
        for (int i = 0; (row = in.readLine()) != null; i++) {
            allLines.add(row);
        }
        //System.out.println(allLines.getItem(id));
        String[] oneRow;
        String[] oneAttack;
        oneRow = allLines.get(id).split(";");
        //System.out.println(Arrays.toString(oneRow));//bis da gehts
        for (String s : oneRow) {
            if (counter > 0) {
                oneAttack = s.split("/");
                //System.out.println(Arrays.toString(oneAttack));
                if (Integer.parseInt(oneAttack[2]) <= level) {
                    attackList.add(Attack.template.get(Integer.parseInt(oneAttack[0]) - 1));
                }
            } else {
                counter++;
            }
            //System.out.println(Integer.parseInt(oneAttack[0]));
        }
        for (int i = 0; i < Math.min(erg.length, attackList.size()); i++) erg[i] = attackList.get(i);
        return erg;
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
                ", evolvesIntoId=" + evolvesIntoId +
                ", evolvesAtLevel=" + evolvesAtLevel +
                ", attacks=" + Arrays.toString(attacks) +
                ", nature=" + nature +
                ", type=" + Arrays.toString(type) +
                ", level=" + level +
                ", xp=" + xp +
                ", maxXP=" + maxXP +
                ", captureRate=" + captureRate +
                ", block=" + block +
                ", growthRate='" + growthRate + '\'' +
                ", curHP=" + curHP +
                ", state=" + state.toString() +
                ", iv=" + Arrays.toString(iv) +
                '}';
    }


    public State getState() {
        return state;
    }

    public int[] getIv() {
        return iv;
    }

    public Nature getNature() {
        return nature;
    }

    private static Map<Integer, Image> allImgs = new HashMap<>();

    public Image getImage() {
        return allImgs.get(id);
    }

    public Image getBackImage() {
        return allImgs.get(-id);
    }

    private static void initPokeImgs() {
        try {
            int maxPoke = 151;
            if (!Files.exists(Path.of("./res/PokemonImgs/1.png"))) {
                Files.deleteIfExists(Path.of("./res/PokemonImgs/"));
                Files.createDirectory(Path.of("./res/PokemonImgs/"));
                for (int i = 1; i <= maxPoke; i++) {
                    try (InputStream in = new URL("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + i + ".png").openStream()) {
                        Files.copy(in, Paths.get("./res/PokemonImgs/" + i + ".png"));
                    } catch (IOException ignored) {
                    }
                    try (InputStream in = new URL("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/back/" + i + ".png").openStream()) {
                        Files.copy(in, Paths.get("./res/PokemonImgs/" + i + "_back.png"));
                    } catch (IOException ignored) {
                    }
                }
            }
            Files.walk(Path.of("./res/PokemonImgs/")).forEach(f -> {
                Matcher m = Pattern.compile("([0-9]+)(_back)?.[pP][nN][gG]").matcher(f.getFileName().toString());
                if (m.find()) {
                    try {
                        allImgs.put(Integer.parseInt(m.group(1)) * (m.group(2) != null ? -1 : 1), new Image(String.valueOf(f.toFile().toURI().toURL())));
                    } catch (MalformedURLException ignored) {
                    }
                }
            });

            for (int i = 1; i <= maxPoke; i++) {
                if (allImgs.get(i) == null) {
                    allImgs.put(i, new Image(String.valueOf(Path.of("./res/LogScreen/ImgNotFound.png").toUri().toURL())));
                }

                if (allImgs.get(-i) == null) {
                    allImgs.put(-i, new Image(String.valueOf(Path.of("./res/LogScreen/ImgNotFound.png").toUri().toURL())));
                }
            }

        } catch (IOException ignored) {
        }
    }


}
