package Calcs;

import InGame.Attack;

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
}
