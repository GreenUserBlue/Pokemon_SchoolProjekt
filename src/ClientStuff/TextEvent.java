package ClientStuff;

import JsonParser.JSONValue;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class TextEvent {

    private String[] text;

    private List<String> optionsAfterText;

    private int curLine = 0;

    private TextField field;

    private long timeTillNextNextLine;

    public TextEvent(JSONValue jsonValue, TextField field) {
        if(field!=null){
            this.field = field;
            List<JSONValue> h = jsonValue.getList();
            text = Objects.requireNonNull(splitToLines(h.get(0).getStr().replaceAll(" {3}", System.lineSeparator()), 20)).toArray(new String[0]);

            h.stream().skip(1).forEach(a -> optionsAfterText.add(a.getStr()));
            timeTillNextNextLine = System.currentTimeMillis() + 400;
            //TODO error, weil server nie aktualisiert, dass man wieder laufen kann. (und weiter fertig machen)
        }
    }

    public TextEvent() {
    }

    @Override
    public String toString() {
        return "TextEvent{" +
                "text=" + Arrays.toString(text) +
                ", optionsAfterText=" + optionsAfterText +
                ", curLine=" + curLine +
                ", field=" + field +
                '}';
    }

    public void nextLines() {
        if (System.currentTimeMillis() > timeTillNextNextLine) {
            timeTillNextNextLine = System.currentTimeMillis() + 400;
            if (text.length >= curLine + 2) {
                curLine += 2;
                field.setText(text[curLine - 1] + System.lineSeparator() + (text.length > curLine ? text[curLine] : ""));
            } else {
            }
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
