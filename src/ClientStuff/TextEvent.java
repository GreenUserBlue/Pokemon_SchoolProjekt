package ClientStuff;

import JsonParser.JSONValue;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TextEvent {

    private String[] text;

    private List<String> optionsAfterText;

    private int curLine = 0;

    private TextField field;

    public TextEvent(JSONValue jsonValue) {
        List<JSONValue> h = jsonValue.getList();
        text = splitToLines(h.get(0).getStr().replaceAll("   ", System.lineSeparator()), 20).toArray(new String[0]);
        h.stream().skip(1).forEach(a -> optionsAfterText.add(a.getStr()));

    }

    public TextEvent() {
    }

    public void nextLines() {
        if (text.length >= curLine + 2) {
            curLine += 2;
            field.setText(text[curLine - 1] + System.lineSeparator() + (text.length > curLine ? text[curLine] : ""));
        }
    }

    /**
     * zerlegt den String bei einem whitespace, sodass es maximal maxLen zeichen hat
     * falls eine zu lange Zeichenkette ohne Whitespace kommt, bleibt diese zusammen
     */
    public static List<String> splitToLines(String s, int maxLen) {
        if (maxLen < 1 || s == null) return null;
        List<String> list = new ArrayList<>();
        if (!Pattern.matches("\\s", "" + s.charAt(s.length() - 1))) s += " ";
        for (int i = maxLen; i < s.length() + maxLen; ) {
            for (int j = 0; j < maxLen; j++) {
                if (i - j < s.length() && Pattern.matches("\\s", "" + s.charAt(i - j))) {
                    String str = s.substring((i - maxLen), i - j).trim();
                    if (!str.isBlank()) list.add(str);
                    i += maxLen - j;
                    break;
                } else if (j == maxLen - 1) {
                    String str = s.substring((i - maxLen + 1), Math.min(i + 1, s.length())).trim();
                    if (!str.isBlank()) list.add(str);
                    i += maxLen + 1;
                }
            }
        }
        return list;
    }
}
