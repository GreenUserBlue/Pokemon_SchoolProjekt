package ClientStuff;

import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zwickelstorfer Felix
 * @version 2.32
 * all Keys which are pressed
 */
public enum Keys {
    confirm(KeyCode.SPACE, KeyCode.ENTER,KeyCode.Q),
    decline(KeyCode.SHIFT),
    menu(KeyCode.X, KeyCode.E),
    up(KeyCode.W, KeyCode.UP),
    down(KeyCode.S, KeyCode.DOWN),
    left(KeyCode.A, KeyCode.LEFT),
    right(KeyCode.D, KeyCode.RIGHT);

    /**
     * saves the keys which are needed for the keyEvents
     */
    private final KeyCode[] triggerKeys;

    Keys(KeyCode... keyCodes) {
        triggerKeys = keyCodes;
    }

    public static ArrayList<Keys> getKeys(List<KeyCode> c) {
        ArrayList<Keys> res = new ArrayList<>();
        for (Keys d : Keys.values()) {
            for (KeyCode cur : d.triggerKeys) {
                if (c.contains(cur) && !res.contains(d)) res.add(d);
            }
        }
        return res;
    }

    /**
     * returns a list from keys from all which are pressed
     * @param strings the keys
     */
    public static ArrayList<Keys> getKeysFromString(String strings) {
        ArrayList<Keys> res = new ArrayList<>();
        for (Keys d : Keys.values()) {
            if (strings.contains("" + d.ordinal()) && !res.contains(d)) res.add(d);
        }
        return res;
    }

    /**
     * removes all keys which would cancel each other out
     */
    public static List<Keys> getSmartKeys(List<KeyCode> keysPressed) {
        ArrayList<Keys> res = getKeys(keysPressed);
        if (res.contains(left) || res.contains(up) || res.contains(right) || res.contains(down)) {
            res.remove(confirm);
            if (res.contains(up) && res.contains(down)) {
                res.remove(up);
                res.remove(down);
            }
            if (res.contains(left) && res.contains(right)) {
                res.remove(right);
                res.remove(left);
            }
        }
        return res;
    }
}
