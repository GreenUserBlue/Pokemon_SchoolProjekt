package InGame;

public class Attack {
    private String name;

    private Type type;

    private int damage;

    //z. 0.85 is 85% oder 1 = 100%
    private double hitProbability;

    private boolean attacksAlwaysFirst;

    public Attack(String name, Type type, int damage, double hitProbability, boolean attacksAlwaysFirst) {
        this.name = name;
        this.type = type;
        this.damage = damage;
        this.hitProbability = hitProbability;
        this.attacksAlwaysFirst = attacksAlwaysFirst;
    }
}
