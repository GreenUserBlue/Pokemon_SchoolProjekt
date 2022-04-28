package ClientStuff;

import JsonParser.JSONValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class TextEvent {

    private String[] text;

    private final List<String> optionsAfterText = new ArrayList<>();

    private int curLine = 0;

    private TextArea field;

    private long timeTillNextNextLine;

    private GridPane optionsNode;

    public TextEventState getState() {
        return state;
    }

    private TextEventState state = TextEventState.nothing;

    public TextEvent(JSONValue jsonValue, TextArea field, Map<String, String> keys) {
        if (field != null) {
            this.field = field;
            field.setMinHeight(field.getFont().getSize() * 3);
            field.setEditable(false);
            field.setFocusTraversable(false);
            startNewText(jsonValue, keys);
            //TODO error, weil server nie aktualisiert, dass man wieder laufen kann. (und weiter fertig machen)
        }
    }

    public TextEvent() {

    }


    public void startNewText(JSONValue jsonValue, Map<String, String> keys) {
        List<JSONValue> h = jsonValue.getList();
        if (keys != null) {
            AtomicReference<String> s = new AtomicReference<>(h.get(0).getStr());
            keys.forEach((a, b) -> s.set(s.get().replaceAll(a, b)));
        }

        text = Objects.requireNonNull(splitToLines(h.get(0).getStr().replaceAll(" {3}", System.lineSeparator()), 20)).toArray(new String[0]);
        h.stream().skip(1).forEach(a -> {
            optionsAfterText.add(a.getStr());
          /*  optionsNode.getChildren().clear();
            ColumnConstraints c = new ColumnConstraints();

            c.setPercentWidth(optionsAfterText.size() % 2 == 0 ? optionsAfterText.size() / 2 : optionsAfterText.size() / 2 + 1);
            VBox box = new VBox();
            for (int i = 0; i < optionsAfterText.size(); i++) {
                Button t = new Button(optionsAfterText.get(i));
                t.setMaxWidth(Double.MAX_VALUE);
                t.setMaxHeight(Double.MAX_VALUE);
                box.getChildren().add(t);
                if (i % 2 == 0) {
                    optionsNode.getColumnConstraints().add(c);
                    box.setAlignment(Pos.BOTTOM_RIGHT);
                    GridPane.setHalignment(box, HPos.RIGHT);//TODO ein gridpane mit allen antworten, welches im gridpane mit txt und diesem gridpane ist
                    optionsNode.add(box, i / 2, 0);
                }
            }*/

        });
        nextLine();
    }

    public static TextField getText() {
        TextField t = new TextField();

        return t;
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

    /**
     * shows the next line on the textField
     *
     * @return if it is finished
     */
    public boolean nextLine() {
        if (System.currentTimeMillis() > timeTillNextNextLine) {
            timeTillNextNextLine = System.currentTimeMillis() + 100;
            if (text.length > curLine + 1) {
                curLine++;
                field.setText(text[curLine - 1] + System.lineSeparator() + (text.length > curLine ? text[curLine] : ""));
                state = TextEventState.reading;
                System.out.println(Arrays.toString(text));
                return false;
            } else {
                state = hasOptions() ? TextEventState.selection : TextEventState.nothing;
                return true;
            }
        }
        return false;
    }

    public boolean hasOptions() {
        return optionsAfterText.size() > 0;
    }

    public Node getOptionsNode() {
        VBox box = new VBox();

        optionsAfterText.forEach(a -> {
            Button t = new Button(a);
//            t.setMaxWidth(Double.MAX_VALUE);
            t.setMaxHeight(Double.MAX_VALUE);

            box.getChildren().add(t);
        });
        box.setAlignment(Pos.BOTTOM_RIGHT);
        GridPane.setHalignment(box, HPos.RIGHT);

        return box;
    }

    void updateSize(VBox box, GridPane pa) {
        box.setMaxWidth(((GridPane) box.getParent()).getWidth() * 0.2);
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

            boolean isLineBreak = false;
            for (int j = 0; j < maxLen; j++) {
                if (i - maxLen + j < s.length() && s.substring(i - maxLen + j).startsWith(System.lineSeparator())) {
//                    System.out.println("detected");
                    list.add(s.substring(i - maxLen, i - maxLen + j));
                    isLineBreak = true;
                    i += j + 1;
                    break;
                }
            }
            if (isLineBreak) continue;

            for (int j = 0; j < maxLen; j++) {
                if (i - j < s.length() && s.substring(i - j).startsWith(System.lineSeparator())) {
                    System.out.println("detected");
                    /*String str = s.substring((i), i + j).trim();
                    if (!str.isBlank()) list.add(str);
                    i += maxLen - j;
                    break;*/
                } /*else*/
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

    public static enum TextEventState {
        nothing,
        reading,
        selection
    }
}
