package ClientStuff;

import JsonParser.JSONValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class TextEvent {

    private String[] text;

    private final List<String> optionsAfterText = new ArrayList<>();

    private int curLine = 0;

    private TextArea field;

    private long timeTillNextNextLine;

    private final VBox optionsNode = new VBox();

    private GridPane grid=new GridPane();

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
        optionsNode.getChildren().clear();
        h.stream().skip(1).forEach(a -> {
            optionsAfterText.add(a.getStr());
//            c.setPercentWidth(optionsAfterText.size() % 2 == 0 ? optionsAfterText.size() / 2 : optionsAfterText.size() / 2 + 1);
            Button t = new Button(optionsAfterText.get(optionsAfterText.size() - 1));
            t.setMaxWidth(Double.MAX_VALUE);
            t.setMaxHeight(Double.MAX_VALUE);
            optionsNode.getChildren().add(t);
        });
        optionsNode.setAlignment(Pos.BOTTOM_RIGHT);
        GridPane.setHalignment(optionsNode, HPos.RIGHT); //TODO ein gridpane mit allen antworten, welches im gridpane mit txt und diesem gridpane ist
        nextLine();
        createGrid();


    }

    private void createGrid() {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        ColumnConstraints c3 = new ColumnConstraints();
        c3.setPercentWidth(100);
        grid.getColumnConstraints().add(c3);

        RowConstraints r = new RowConstraints();
        RowConstraints r2 = new RowConstraints();
        r.setPercentHeight(85);
        r2.setPercentHeight(15);
        grid.getRowConstraints().addAll(r, r2);

        grid.add(field, 0, 1);
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
        return optionsNode;
    }

    public GridPane getGrid() {
        return grid;
    }

    void updateSize() {
        optionsNode.setPrefWidth(((GridPane) optionsNode.getParent()).getWidth() * 0.2);
        optionsNode.getChildren().forEach(a -> ((Button) a).setMaxWidth(optionsNode.getPrefWidth()));
        System.out.println(((Button) optionsNode.getChildren().get(0)).getMaxWidth());
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
                }
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
