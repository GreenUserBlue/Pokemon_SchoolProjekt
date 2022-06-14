package InGame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Clemens Hodina
 */
public class Attack {

    /**
     * list of all attacks
     */
    private static List<Attack> template = new ArrayList<>();

    /**
     * id of the attack
     */
    private int id;

    public String getName() {
        return name;
    }

    /**
     * name of the attack
     */
    private final String name;

    /**
     * type of the attack
     */
    private Type type;

    /**
     * the damage the attack deals
     */
    private int damage;

    /**
     * how often you can use an attack
     */
    private int AP;

    /**
     * how often you can use an attack
     */
    private int curAP;

    /**
     * how probable it is that the attack hits
     */
    private double hitProbability;

    public boolean attacksAlwaysFirst() {
        return attacksAlwaysFirst;
    }

    /**
     * if the attack always attacks first
     */
    private boolean attacksAlwaysFirst;

    //z. 0.85 is 85% oder 1 = 100%

    /**
     * the type of the attack
     */
    private AttackType attackType;

    public Attack(String name) {
        this.name = name;
    }

    public Attack(int id, String name, Type type, int damage, int AP, double hitProbability, boolean attacksAlwaysFirst, AttackType attackType) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.damage = damage;
        this.AP = AP;
        this.hitProbability = hitProbability;
        this.attacksAlwaysFirst = attacksAlwaysFirst;
        this.attackType = attackType;
        this.curAP = AP;
    }

    public Attack(int id, String name, Type type, int damage, int ap, double hitProbability, boolean attacksAlwaysFirst, AttackType attackType, int curAP) {
        this(id, name, type, damage, ap, hitProbability, attacksAlwaysFirst, attackType);
        this.curAP = curAP;
    }

    //maybe effects (paralysieren und brennen)

    public static void main(String[] args) throws IOException {
        Attack.init();
        for (Attack attack : template) {
            System.out.println(attack);
        }
        //System.out.println(template);
    }

    /**
     * creates the template for every attack with the necessary data
     */
    public static void init() throws IOException {
        BufferedReader in;
        String[] lines = new String[165];
        in = new BufferedReader(new FileReader("res/DataSets/movesList"));
        String row;
        for (int i = 0; (row = in.readLine()) != null; i++) {
            lines[i] = row;
        }
        String[] oneRow;
        for (int i = 0; i < 165; i++) {
            oneRow = lines[i].split(";");
            int a = 0;
            double b;
            if (oneRow[4].equals("null")) {
                b = -1;//trifft immer
            } else {
                a = Integer.parseInt(oneRow[4]);
                b = (double) (a / 100);
            }
            if (oneRow[1].equals("null")) {
                template.add(new Attack(Integer.parseInt(oneRow[0]), oneRow[3], Type.valueOf(oneRow[5].toLowerCase()), 0, Integer.parseInt(oneRow[2]), a, false, AttackType.Status));
            } else {
                template.add(new Attack(Integer.parseInt(oneRow[0]), oneRow[3], Type.valueOf(oneRow[5].toLowerCase()), Integer.parseInt(oneRow[1]), Integer.parseInt(oneRow[2]), a, false, AttackType.Attack));
            }
        }
    }

    public static Attack getFromTemp(int id) {
        return template.get(id - 1).clone();
    }

    public int getId() {
        return id;
    }

    public int getAP() {
        return AP;
    }

    public int getCurAP() {
        return curAP;
    }

    @Override
    public String toString() {
        return "Attack{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", damage=" + damage +
                ", AP=" + AP +
                ", curAP=" + curAP +
                ", hitProbability=" + hitProbability +
                ", attacksAlwaysFirst=" + attacksAlwaysFirst +
                ", attackType=" + attackType +
                '}';
    }

    public Type getType() {
        return type;
    }

    public int getDamage() {
        return damage;
    }

    public double getHitProbability() {
        return hitProbability;
    }

    @SuppressWarnings({"MethodDoesntCallSuperMethod", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    public Attack clone() {
        return new Attack(id, name, type, damage, AP, hitProbability, attacksAlwaysFirst, attackType, curAP);
    }

    public void setCurAP(int curAP) {
        this.curAP = curAP;
    }

    public boolean use() {
        return --curAP > 0;
    }

    enum AttackType {
        Attack,
        Special,
        Status
    }
}


