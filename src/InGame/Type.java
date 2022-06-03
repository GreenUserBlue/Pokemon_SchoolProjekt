package InGame;

import javafx.scene.paint.Color;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public enum Type {
    normal(Color.GRAY),
    fighting,
    flying,
    poison,
    ground,
    rock,
    bug,
    ghost,
    steel,
    fire,
    water,
    grass,
    electric,
    psychic,
    ice,
    dragon,
    dark,
    fairy;

    private double[] mult;

    private Color c;

    Type(Color c) {
        this.c = c;
    }

    Type() {

    }

    public static void init() {
        Arrays.asList(Type.values()).forEach(type -> {
            type.mult = new double[Type.values().length];
            Arrays.fill(type.mult, 1);
        });
        try {
            List<String> file = Files.readAllLines(Path.of("./res/DataSets/AttacksMult.txt"));
            file.forEach(a -> {
                List<String> all = new ArrayList<>(Arrays.stream(a.split("\s|\t")).map(String::trim).toList());
                Type target = Type.valueOf(all.get(0).trim().toLowerCase());
                all.removeIf(b -> !Pattern.matches("[01½2]", b.trim()));
                for (int i = 0; i < all.size(); i++)
                    target.mult[i] = all.get(i).matches("½") ? 0.5 : Integer.parseInt(all.get(i));
            });
        } catch (IOException ignored) {
        }
    }

    public double getAttackMult(Type defender) {
        return mult[defender.ordinal()];
    }

    public Color getColor() {
        return c;
    }

    public static void main(String[] args) {
        init();
        System.out.println("water->fire(2): " + Type.water.getAttackMult(Type.fire));
        System.out.println("normal->ghost(0): " + Type.normal.getAttackMult(Type.ghost));
        System.out.println("fighting->normal(2): " + Type.fighting.getAttackMult(Type.normal));
        System.out.println("normal->fighting(1): " + Type.normal.getAttackMult(Type.fighting));
    }
}
// ANG 2, BER 2, BOE 3, 2, (3), patrick: 3, 2, 1, 3, HA (weiß nicht), 1, HOD 1, JAN 2, JOR 1, KUR 1, LIE 4, MAREK (noch nix), MARINE 3, MYR (nix), OLA 3, ÖZB 3, PAM 2, ROS (nix), SCHUH 1 mit Bauchweh, SOF 3, Marcel: 1 mit bissi Bauchweh, Tra 4, VAS 3, WID 2, WOL 2, ZACH 2, ZIM 3, ZWI 2
