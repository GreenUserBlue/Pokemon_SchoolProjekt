package InGame;

public class State {
    //Klasse Status{
    // int HP, Attack, Defense,	Sp. Atk, 	Sp. Def, 	Speed
    // void add(int[]);
    // int[] mult(double[](zB von Wesen))
    //}
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


    public void add(int id, int level, Pokemon.Nature nat) {
        double[] nature = nat.getEffect();
        HP = ((getaDouble(id, level, 0, 0)) )+ level + 10;
        attack = (getaDouble(id, level, 1, 1) + 5) * nature[0];
        defense = (getaDouble(id, level, 2, 2) + 5) * nature[1];
        spAttack = (getaDouble(id, level, 3, 3) + 5) * nature[2];
        spDefense = (getaDouble(id, level, 3, 4) + 5) * nature[3];
        speed = (getaDouble(id, level, 4, 5) + 5) * nature[4];
    }

    private double getaDouble(int id, int level, int idIV, int baseID) {
        return (((2 * Pokemon.template.get(id).getState().get(baseID)) + Pokemon.template.get(id).getIv()[idIV]) * level) / 100;
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


}
