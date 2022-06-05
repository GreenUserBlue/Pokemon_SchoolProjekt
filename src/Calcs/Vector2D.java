package Calcs;

import java.util.Objects;

/**
 * @author Zwickelstorfer Felix
 * erstellt einen Vektor
 */
public class Vector2D {

    /**
     * Die X Position
     */
    private double x;

    /**
     * Die Y Position
     */
    private double y;

    /**
     * inizialisiert alle Daten
     */
    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * inizialisiert alle Daten
     */
    public Vector2D() {
        this(0, 0);
    }

    /**
     * addiert 2 Vektoren
     *
     * @param v1 Der erste Vektor
     * @param v2 Der zweite Vektor
     */
    public static Vector2D add(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.x + v2.x, v1.y + v2.y);
    }

    /**
     * subtrahiert 2 Vektoren
     *
     * @param v1 Der erste Vektor
     * @param v2 Der zweite Vektor
     */
    public static Vector2D sub(Vector2D v1, Vector2D v2) {
        return new Vector2D(v1.x - v2.x, v1.y - v2.y);
    }

    /**
     * berechnet den Winkel vom Ausgansvektor zum anderen Vektoren
     *
     * @param pMiddle Der erste Vektor
     * @param pSec    Der zweite Vektor
     */
    public static double getAngle(Vector2D pMiddle, Vector2D pSec) {
        Vector2D p = sub(pMiddle, pSec);
        double rad = Math.sqrt((p.x) * (p.x) + (p.y) * (p.y));
        if (rad == 0) {
            return Double.MIN_VALUE;
        }

        double lng = Math.sqrt((p.x - rad) * (p.x - rad) + (p.y) * (p.y));
        double height = rad - Math.sqrt(rad * rad - lng * lng / 4);
        double angle = Math.toDegrees(2 * Math.acos(1 - height / rad));

        if (p.y < 0) {
            angle = 360 - angle;
        }
        return 180 + angle;
    }

    /**
     * berechnet den Winkel relativ zum 0 Punkt
     *
     * @param v der Vektor zum berechnen
     */
    public static double getAngleRel(Vector2D v) {
        return getAngle(new Vector2D(0, 0), v);
    }

    /**
     * berechnet den Winkel relativ zu dem Object
     *
     * @param p der andere Vector
     */
    public double getAngle(Vector2D p) {
        return getAngle(this, p);
    }

    /**
     * @return die X-Coordinates
     */
    public double getX() {
        return x;
    }

    /**
     * setzt die X-Coordinates
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return die Y-Coordinates
     */
    public double getY() {
        return y;
    }

    /**
     * setzt die Y-Coordinates
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * addiert zum dem Objekt einen anderen Vector
     *
     * @param v der Vector der addiert wird
     */
    public void add(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
    }

    /**
     * subtrahiert zum dem Objekt einen anderen Vector
     *
     * @param v der Vector der subtrahiert wird
     */
    public void sub(Vector2D v) {
        this.x -= v.x;
        this.y -= v.y;
    }

    /**
     * Die Entfernung zur Mitte der Map
     *
     * @return double entfernung zum Spawn
     */
    public double magnitude(){
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2D vector2D = (Vector2D) o;
        return vector2D.x - x == 0 &&
                vector2D.y - y == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Vector2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public Vector2D clone() {
        return new Vector2D(x, y);
    }

    /**
     * if two vector objects are equal
     *
     * @param o the other vector
     * @param e the max Inaccuracy
     */
    public boolean equals(Object o, double e) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2D vector2D = (Vector2D) o;
        return Math.abs(x - vector2D.x) < e &&
                Math.abs(y - vector2D.y) < e;
    }

    /**
     * checks if either x or y absolute value is bigger than d
     *
     * @param d the min for either value
     */
    public boolean anyBigger(double d) {
        return Math.abs(x) >= d || Math.abs(y) >= d;
    }

    /**
     * rounds the value to specific number of decimal-points
     *
     * @param decimalPoints the number of decimal-points
     */
    public void round(int decimalPoints) {
        x = round(x, decimalPoints);
        y = round(y, decimalPoints);
    }

    /**
     * rounds a single value to a single number of points
     *
     * @param value         the value which needs to be rounded
     * @param decimalPoints the number of decimal-points
     * @return tthe new rounded value
     */
    private static double round(double value, int decimalPoints) {
        double d = Math.pow(10, decimalPoints);
        return Math.round(value * d) / d;
    }
}
