package Calcs;

import InGame.Attack;

import java.util.List;

public class Utils {

    public static <K extends Attack> K[] deepClone(K[] iv) {
        if (iv == null) {
            return null;
        }
        K[] res = iv.clone();
        for (int i = 0; i < iv.length; i++) {
            if (iv[i] != null) {
                //noinspection unchecked
                res[i] = (K) iv[i].clone();
            }
        }
        return res;
    }


    public static <K> void switchObjects(List<K> items, int id) {
        K old = items.get(0);
        items.remove(old);
        K newZero = items.get(id);
        items.remove(newZero);
        items.add(id, old);
        items.add(0, newZero);
    }

    /**
     * rounds a single value to a single number of points
     *
     * @param value         the value which needs to be rounded
     * @param decimalPoints the number of decimal-points
     * @return the new rounded value
     */
    public static double round(double value, int decimalPoints) {
        double d = Math.pow(10, decimalPoints);
        return Math.round(value * d) / d;
    }

    public static int toInt(String s) {
        return Integer.parseInt(s.trim());
    }
}
