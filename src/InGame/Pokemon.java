package InGame;

import Calcs.Utils;
import Calcs.Vector2D;
import Envir.World;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
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

    public String getName() {
        return name;
    }

    public Attack[] getAttacks() {
        return attacks;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    /**
     * name of the pokemon
     */
    private String name;

    /**
     * id of the pokemon
     */
    private int id;

    /**
     * how the pokemon evolves
     */
    private EvolveType evolveType;

    /**
     * in which pokemon it evolves
     */
    private int evolvesIntoId;

    /**
     * level at which the pokemon evolves
     */
    private int evolvesAtLevel;

    /**
     * the attacks of the pokemon
     */
    private Attack[] attacks = new Attack[4];

    /**
     * the nature of the pokemon, which adds or subs stats
     */
    private Nature nature;

    /**
     * the types of the pokemon
     */
    private Type[] type;

    /**
     * the current level of the pokemon
     */
    private int level;

    //wie viele xp hat man grade

    /**
     * how many xp the pokemon holds at this moment
     */
    private int xp;

    //wie viele xp kann man auf dem Level erreichen maximal

    public int getMaxXPNeeded() {
        return maxXPNeeded;
    }

    /**
     * the xp the pokemon needs to go to the next level
     */
    private int maxXPNeeded;

    /**
     * how hard it is to find and capture the pokemon
     */
    private int captureRate;

    //block auf dem das Pokemon spawnen kann

    /**
     * type of block on which the pokemon is abled to spawn
     */
    private World.Block block;

    /**
     * how fast the pokemon gains xp
     */
    private String growthRate;

    /**
     * The HP the Pokemon has at this moment
     */
    private double curHP;

    /**
     * the set of stats one Pokemon has
     */
    private State state;

    //individual values für stats

    /**
     * individual values for the stats for a single pokemon
     */
    private int[] iv;

    //noch nicht im Konstruktor oder so
    //da steht level bei dem das Pokemon die jeweilige attacke bekommt
    //private final TreeMap<Integer, Attack> attackAtLevel = new TreeMap<Integer, Attack>();

    /**
     * A List with every Pokemon
     */
    public static List<Pokemon> template = new ArrayList<>();

    private static final Random rnd = new Random(696969);

    private static final Random attackRnd = new Random(420420);

    private static final Random hitProbRnd = new Random(9824756);


    public static void main(String[] args) throws IOException {
        Attack.init();
        Pokemon.init(false);
        Type.init();
        Item.init();
        /*for (Pokemon pokemon : template) {
            System.out.println(pokemon);
        }
         */

        Pokemon a = createPokemon(new Vector2D(200, 610), World.Block.Grass);
        System.out.println(a);
        /*
            getAttacks(0,40);
        } catch (IOException e) {
            e.printStackTrace();
        }
         */
        Pokemon b = createPokemon(new Vector2D(4000, 7000), World.Block.Grass);
//        a.getsAttacked(b, 4, false);

        System.out.println(a.getsCaptured((Ball) Item.getItem(2)));

    }


    //liest Files aus und gibts in Liste
    //und gibts in die tatsächliche Liste

    /**
     * creates the template for every pokemon with the necessary data
     */
    public static void init(boolean withImgs) {
        //File file = new File(String.valueOf(Path.of("res/DataSets/pokeFile.txt")));
        Type.init();
        try {
            Attack.init();
        } catch (IOException ignored) {
        }
        if (withImgs) {
            initPokeImgs();
        }
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
                } catch (NumberFormatException ignored) {
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

    public Pokemon(String name, int id, EvolveType evolveType, int evolvesIntoId, int evolvesAtLevel, Attack[] attacks, Nature nature, Type[] type, int level, int xp, int maxXPNeeded, int captureRate, World.Block block, String growthRate, double curHP, State state, int[] iv) {
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
        this.maxXPNeeded = maxXPNeeded;
        this.captureRate = captureRate;
        this.block = block;
        this.growthRate = growthRate;
        this.curHP = curHP;
        this.state = state;
        this.iv = iv;
    }

    public Pokemon() {
    }

    public Pokemon(int id, int level) {
        this.id = id;
        this.level = level;
    }

    /**
     * gives the pokemon with the exact id and level and changed stats, attacks and so on
     *
     * @param id    the needed pokemon
     * @param level the needed level
     * @return the pokemon at the id with the right stats and attacks
     */
    private static Pokemon getPokemon(int id, int level) {
        //da id mit 1 beginnt
        id--;
        Pokemon a = new Pokemon();
        a = template.get(id).clone();
        a.level = level;
        a.xp = 0;//getXpNeeded(a.growthRate, level - 1);//is die xp wenn man gerade auf das level gekommen ist
        a.maxXPNeeded = getXpNeeded(a.growthRate, level);
        a.nature = Nature.values()[(int) ((Math.random()) * Nature.values().length)];
        for (int i = 0; i < 6; i++) {
            a.iv[i] = rnd.nextInt(16);
        }
        a.state.updateVals(a, level, a.nature);
        try {
            a.attacks = getAttacks(id, level);
        } catch (IOException e) {
            e.printStackTrace();
        }
        a.curHP = a.state.getHP();
        return a;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Pokemon clone() {
        //Pokemon clone = (Pokemon) super.clone();
        return new Pokemon(name, id, evolveType, evolvesIntoId, evolvesAtLevel, Utils.deepClone(attacks), nature, type.clone(), level, xp, maxXPNeeded, captureRate, block, growthRate, curHP, state.clone(), iv.clone());
    }

    public static Pokemon getFromMsg(String msg) {
        String[] s = msg.split(";");
        Pokemon poke = new Pokemon();
        poke.id = toInt(s[0]);
        poke.level = toInt(s[1]);

        String[] str = s[2].split("§");
        for (int i = 0; i < str.length; i++) {
            String[] at = str[i].split(",");
            Attack a = Attack.getFromTemp(toInt(at[0]));
            a.setCurAP(toInt(at[1]));
            poke.attacks[i] = a;
        }

        poke.xp = toInt(s[3]);

        str = s[4].split(",");
        int[] ivs = new int[str.length];
        for (int i = 0; i < str.length; i++) {
            ivs[i] = toInt(str[i]);
        }
        poke.setIv(ivs);

        poke.nature = Nature.values()[toInt(s[5])];

        poke.curHP = toInt(s[6]);

        Pokemon temp = template.get(poke.id - 1).clone();
        poke.state = temp.getState();
        poke.state.updateVals(poke, poke.level, poke.nature);
        poke.growthRate = temp.growthRate;
        poke.maxXPNeeded = getXpNeeded(poke.growthRate, poke.level);
        poke.name = temp.name;
        return poke;
    }

    /**
     * for toMsg
     */
    private String toString3() {
        StringBuilder b = new StringBuilder();
        b.append(id).append(";");
        b.append(level).append(";");
        Arrays.stream(attacks).filter(Objects::nonNull).forEach(a -> {
            b.append(a.getId()).append(",").append(a.getCurAP()).append("§");
        });
        b.append(";");
        b.append(xp).append(";");

        for (int i = 0; i < iv.length; i++) {
            b.append(iv[i]);
            if (i != iv.length - 1) b.append(",");
        }
        b.append(";").append(nature.ordinal()).append(";");
        b.append((int) curHP);
        return b.toString();
    }

    private static int toInt(String s) {
        return Integer.parseInt(s);
    }

    /**
     * makes a Pokemon from the String which was sent from the server
     *
     * @param msg the message from the server
     * @return pokemon from server
     */
    private static Pokemon getFromMsg2(String msg) {
        String[] messg = msg.split(";");
        int id = Integer.parseInt(messg[1]);
        Pokemon a = null;
        a = template.get(id).clone();
        a.setLevel(Integer.parseInt(messg[4]));//hierdurch auch iwie attacken und maxXp uns sowas
        a.setXp(Integer.parseInt(messg[5]));
        a.setCurHP(Double.parseDouble(messg[7]));
        String wholeState = messg[8];
//        System.out.println(wholeState);
        Pattern getStates = Pattern.compile("HP=([0-9]+.[0-9]+), attack=([0-9]+.[0-9]+), defense=([0-9]+.[0-9]+), spAttack=([0-9]+.[0-9]+), spDefense=([0-9]+.[0-9]+), speed=([0-9]+.[0-9]+)");
        Matcher m = getStates.matcher(wholeState);
        while (m.find()) {
            a.setState(new State(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)), Double.parseDouble(m.group(3)), Double.parseDouble(m.group(4)), Double.parseDouble(m.group(5)), Double.parseDouble(m.group(6))));
        }
        a.setIv(new int[]{rnd.nextInt(16)});
        //in msg steht die id mit den attacken und den ap und dem level und allem
        return a;
    }

    public static Pokemon createStarter(int starterID) {
        return getPokemon(starterID == 0 ? 1 : starterID == 1 ? 4 : 7, 5);
    }

    //return  name + ";" + id + ";" + Arrays.toString(attacks) + ";" + nature + ";" + level + ";" + xp + ";" + maxXP + ";" +curHP + ";" +state.toString() + ";" + Arrays.toString(iv);

    //Pokemon{name='venomoth', id=49, evolveType=null, evolvesIntoId=-1, evolvesAtLevel=-1, attacks=[Attack{id=13, name='razor-wind', type=normal, damage=80, AP=10, hitProbability=100.0, attacksAlwaysFirst=false, attackType=Attack}, Attack{id=36, name='take-down', type=normal, damage=90, AP=20, hitProbability=85.0, attacksAlwaysFirst=false, attackType=Attack}, Attack{id=38, name='double-edge', type=normal, damage=120, AP=15, hitProbability=100.0, attacksAlwaysFirst=false, attackType=Attack}, Attack{id=63, name='hyper-beam', type=normal, damage=150, AP=5, hitProbability=90.0, attacksAlwaysFirst=false, attackType=Attack}], nature=hasty, type=[bug, poison], level=25, xp=15625, maxXP=17576, captureRate=75, block=Grass, growthRate='medium', curHP=70.0, state=State{HP=73.0, attack=41.0, defense=35.22727272727273, spAttack=51.75, spDefense=44.25, speed=55.825}, iv=[12, 14, 15, 7, 3, 4]}

    /*
    "Pokemon{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", attacks=" + Arrays.toString(attacks) +
                ", nature=" + nature +
                ", level=" + level +
                ", xp=" + xp +
                ", maxXP=" + maxXP +
                ", curHP=" + curHP +
                ", state=" + state.toString() +
                ", iv=" + Arrays.toString(iv) +
                '}';
     */

    /**
     * makes a String to send from the server to the client
     *
     * @return string to server
     */
    public String toMsg() {
        return this.toString3();
    }


    /* *//**
     * for toMsg
     *//*
    private String toString3() {
        StringBuilder b = new StringBuilder();
        b.append(id).append(";");
        b.append(level).append(";");
        Arrays.stream(attacks).filter(Objects::nonNull).forEach(a -> {
            b.append(a.getId()).append(",").append(a.getCurAP()).append("%");
        });
        b.append(";");
        b.append(xp).append(";");

        for (int i = 0; i < iv.length; i++) {
            b.append(iv[i]);
            if (i != iv.length - 1) b.append(",");
        }
        b.append(";").append(nature.ordinal());
        return b.toString();
    }*/

    /**
     * adds xp to a pokemon after a fight
     */
    public int xpAfterFight(boolean isFightWild) {//TODO aufrufen
        if (isFightWild) {
            return (200 * level) / 7;
        } else {
            return (int) (1.5 * 200 * level) / 7;
        }
    }


    /**
     * says how much money you can get from this pokemon from a fight
     */
    public int getMaxMoney() {
        return level * 3;
    }


    /**
     * adds xp to a pokemon, for example after a fight
     *
     * @param newExP the xp the pokemon gets from the fight
     */
    public void addExp(int newExP) {
        if (newExP + xp >= maxXPNeeded) {
            levelUp(maxXPNeeded - xp - newExP);
        } else {
            xp = newExP + xp;
        }
    }

    //xpoverride is wie viele xp man ins nächste level mitnimmt

    /**
     * takes the pokemon to the nect level
     *
     * @param xpOverride how much xp the pokemon takes with it to the next level
     */
    private void levelUp(int xpOverride) {
        level++;
        double oldHP = state.getHP();
        state.updateVals(this, level, nature);
        curHP += (state.getHP() - oldHP);
        curHP = Math.min(curHP, state.getHP());
        xp = xpOverride;
        if (level == evolvesAtLevel) {
            evolve();
        }
    }

    /**
     * lets the pokemon evolve
     */
    private void evolve() {
        id = evolvesIntoId;
        state.updateVals(this, level, nature);
        setType(template.get(id - 1).type);
    }

    //pos für wie weit vom spawnt entfernt
    //vl schöner machen und sachen in Methoden auslagern

    /**
     * creates a Pokemon from the place the player is, and block the pokemon should spawn
     *
     * @param pos   the position the player is
     * @param block the block the pokemon should spawn
     * @return a pokemon for the given data
     */
    public static Pokemon createPokemon(Vector2D pos, World.Block block) {
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
            int number = (int) (Math.random() * (possibilities.size()) /*+ 1*/);
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
     * subs the current HP and takes an AP from the attack
     * the methods needs to know "i attack with the pokemon attacker the pokemon this with the attack attackID"
     *
     * @param attacker the attacking pokemon
     * @param attackId the attack with which it attacks
     */
    public void getsAttacked(Pokemon attacker, int attackId, boolean isCrit) {
//        Attack at = Attack.template.get(attackId);
        Attack at = attacker.attacks[attackId];
        if (at.use()) {
            double crit = 1;//mimimi leben dürfen nd negativ sein
            double random = (attackRnd.nextInt(15) + 85) / 100D;
            int hitRandom = hitProbRnd.nextInt(100);
            double stab = 1;
            if (at.getType().equals(this.type[0]) || at.getType().equals(this.type[1])) {
                stab = 1.5;
            }
            if (isCrit) {
                crit = 1.5;
            }
            boolean[] isHitting = new boolean[100];
            double hitProb = at.getHitProbability();
            for (int i = 0; i < 100; i++) {
                if (i < hitProb) {
                    isHitting[i] = true;
                }
            }
            if (isHitting[hitRandom]) {
                if (this.type[1] == null) {
                    curHP = (curHP - (((((2 * attacker.level / 5d) + 2) * at.getDamage() * (attacker.state.attack / this.state.defense) / 50) + 2) * random * stab * at.getType().getAttackMult(this.type[0]) * crit));
                } else {
                    curHP = (curHP - (((((2 * attacker.level / 5d) + 2) * at.getDamage() * (attacker.state.attack / this.state.defense) / 50) + 2) * random * stab * at.getType().getAttackMult(this.type[0]) * at.getType().getAttackMult(this.type[1]) * crit));
                }
            }
        }
    }


    /**
     * gets the attacks for the pokemon id at the level level
     *
     * @param id    the pokemon from which you need the attacks
     * @param level the level of the pokemon you need
     * @return an attack array with the attacks of the pokemon at this level
     * @throws IOException lol
     */
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
                    attackList.add(Attack.getFromTemp(Integer.parseInt(oneAttack[0])));
                }
            } else {
                counter++;
            }
            //System.out.println(Integer.parseInt(oneAttack[0]));
        }
        for (int i = 0; i < Math.min(erg.length, attackList.size()); i++) erg[i] = attackList.get(i);

        if (erg[0] == null) {
            erg[0] = Attack.getFromTemp(33);
        }
        return erg;
    }

    public int getMaxHP() {
        return (int) state.getHP();
    }

    public int getId() {
        return id;
    }

    public void heal(double healQuantity) {
        curHP = Math.min(curHP + healQuantity, getMaxHP());
    }


    public boolean getsCaptured(Ball b) {//captuerRateMuell //TODO Formel einfuegen usw.
//        f = \left\lfloor \dfrac {HP_{max} \times 255 \times 4}{HP_{current} \times Ball} \right\rfloor}
       /*a int n = rnd.nextInt(b.getNCatch());
        int f = (int) ((getMaxHP() * 255 * 4) / (getCurHP() * b.getCatchRate()));
        double d = (((3 * getMaxHP() - 2 * curHP) * captureRate * b.getCatchRate()) / (3 * getMaxHP())) * 1;
        System.out.println(d);*/
        return true;
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
                ", evolveType=" + evolveType +//
                ", evolvesIntoId=" + evolvesIntoId +//
                ", evolvesAtLevel=" + evolvesAtLevel +//
                ", attacks=" + Arrays.toString(attacks) +
                ", nature=" + nature +
                ", type=" + Arrays.toString(type) +//
                ", level=" + level +
                ", xp=" + xp +
                ", maxXP=" + maxXPNeeded +
                ", captureRate=" + captureRate +//
                ", block=" + block +//
                ", growthRate='" + growthRate + '\'' +//
                ", curHP=" + curHP +
                ", state=" + state.toString() +
                ", iv=" + Arrays.toString(iv) +
                '}';
    }

    private String toString2() {
        return name + ";" + id + ";" + Arrays.toString(attacks) + ";" + nature + ";" + level + ";" + xp + ";" + maxXPNeeded + ";" + curHP + ";" + state.toString() + ";" + Arrays.toString(iv);
                /*"Pokemon{" +
                "name='" + name + '\'' +
                ", id=" + id +
                ", attacks=" + Arrays.toString(attacks) +
                ", nature=" + nature +
                ", level=" + level +
                ", xp=" + xp +
                ", maxXP=" + maxXP +
                ", curHP=" + curHP +
                ", state=" + state.toString() +
                ", iv=" + Arrays.toString(iv) +
                '}';

                 */
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

    /**
     * method to load the images of the pokemon
     */
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

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEvolveType(EvolveType evolveType) {
        this.evolveType = evolveType;
    }

    public void setEvolvesIntoId(int evolvesIntoId) {
        this.evolvesIntoId = evolvesIntoId;
    }

    public void setEvolvesAtLevel(int evolvesAtLevel) {
        this.evolvesAtLevel = evolvesAtLevel;
    }

    public void setAttacks(Attack[] attacks) {
        this.attacks = attacks;
    }

    public void setNature(Nature nature) {
        this.nature = nature;
    }

    public void setType(Type[] type) {
        this.type = type;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setXp(int xp) {
        this.xp = xp;
    }

    public void setMaxXPNeeded(int maxXPNeeded) {
        this.maxXPNeeded = maxXPNeeded;
    }

    public void setCaptureRate(int captureRate) {
        this.captureRate = captureRate;
    }

    public void setBlock(World.Block block) {
        this.block = block;
    }

    public void setGrowthRate(String growthRate) {
        this.growthRate = growthRate;
    }

    public void setCurHP(double curHP) {
        this.curHP = Math.min(getMaxHP(), curHP);
    }

    public int getCurHP() {
        return (int) curHP;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setIv(int[] iv) {
        this.iv = iv;
    }

    public static void setTemplate(List<Pokemon> template) {
        Pokemon.template = template;
    }

    public static void setAllImgs(Map<Integer, Image> allImgs) {
        Pokemon.allImgs = allImgs;
    }
}