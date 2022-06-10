package InGame;

/**
 * @author Clemens Hodina
 */

public class State {

    double HP;

    double attack;

    double defense;

    double spAttack;

    double spDefense;

    double speed;


    public State(double HP, double attack, double defense, double spAttack, double spDefense, double speed) {
        this.HP = HP;
        this.attack = attack;
        this.defense = defense;
        this.spAttack = spAttack;
        this.spDefense = spDefense;
        this.speed = speed;
    }

    //gibt den Status eines Pokemons mit level und id aus
    private State getState(int id, int level) {
        return null;
    }


    public void updateVals(Pokemon poke, int level, Pokemon.Nature nat) {
        double[] nature = nat.getEffect();//poke.getId() - 1
        HP = ((getaDouble(poke, level, 0, 0))) + level + 10;
        attack = (getaDouble(poke, level, 1, 1) + 5) * nature[0];
        defense = (getaDouble(poke, level, 2, 2) + 5) * nature[1];
        spAttack = (getaDouble(poke, level, 3, 3) + 5) * nature[2];
        spDefense = (getaDouble(poke, level, 3, 4) + 5) * nature[3];
        speed = (getaDouble(poke, level, 4, 5) + 5) * nature[4];
    }

    private double getaDouble(Pokemon poke, int level, int idIV, int baseID) {
        return (((2 * Pokemon.template.get(poke.getId() - 1).getState().get(baseID)) + poke.getIv()[idIV]) * level) / 100;
    }


    private double get(int baseID) {
        return switch (baseID) {
            case 0 -> HP;
            case 1 -> attack;
            case 2 -> defense;
            case 3 -> spAttack;
            case 4 -> spDefense;
            default -> speed;
        };
    }

    private int[] mult(double[] a) { //(zB von Wesen)
        return null;
    }


    @Override
    public String toString() {
        return "State{" +
                "HP=" + HP +
                ", attack=" + attack +
                ", defense=" + defense +
                ", spAttack=" + spAttack +
                ", spDefense=" + spDefense +
                ", speed=" + speed +
                '}';
    }

    public double getHP() {
        return HP;
    }

    public void setHP(double HP) {
        this.HP = HP;
    }

    public void setAttack(double attack) {
        this.attack = attack;
    }

    public void setDefense(double defense) {
        this.defense = defense;
    }

    public void setSpAttack(double spAttack) {
        this.spAttack = spAttack;
    }

    public void setSpDefense(double spDefense) {
        this.spDefense = spDefense;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }


    @Override
    protected State clone() {
        return new State(HP, attack, defense, spAttack, spDefense, speed);
    }
}
