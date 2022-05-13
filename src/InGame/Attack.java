package InGame;

public class Attack {
    //TODO aus den files die zwicki mir geschickt hat und des infos von pokeapi wo daas schön augelistet ist kann man sich die Attacken iwie zusammenbauen
    //iwie move namen holen und dann level learned at
    //maybe ne map machen mit <pokemon, Map<Atacke, Level> --> is halt schwer zum auslesen
    private String name;

    private Type type;

    private int damage;

    //z. 0.85 is 85% oder 1 = 100%
    private double hitProbability;

    private boolean attacksAlwaysFirst;

    //speed is bei Pokemon nd be Attacke

    //iwie muss der damage mit den werten verbunden sein --> wird aber erst in der Pokemon Klasse passieren


    public Attack(String name) {
        this.name = name;
    }

    public Attack(String name, Type type, int damage, double hitProbability, boolean attacksAlwaysFirst) {
        this.name = name;
        this.type = type;
        this.damage = damage;
        this.hitProbability = hitProbability;
        this.attacksAlwaysFirst = attacksAlwaysFirst;
    }
}
