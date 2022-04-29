package ClientStuff;

import JsonParser.JSONValue;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class TextEvent {

    private final List<String> optionsAfterText = new ArrayList<>();

    private final TextArea field;

    private final VBox optionsNode = new VBox();

    private final GridPane grid = new GridPane();

    private String[] text;

    private int curLine = 0;

    private long timeTillNextNextLine;

    private boolean showAfterwards = false;

    private TextEventState state = TextEventState.nothing;

    public TextEvent() {
        field = new TextArea();
        field.setMinHeight(field.getFont().getSize() * 3);
        field.setEditable(false);
        field.setFocusTraversable(false);
        //TO DO error, weil server nie updates, so that you can run again

        field.setWrapText(true);
        field.setMouseTransparent(true);
        field.heightProperty().addListener((a, oldVal, newVal) -> {
            field.setFont(new Font((field.getHeight()) / 2.55 - 7.5));
            ScrollBar sc = (ScrollBar) field.lookup(".scroll-bar:vertical");
            if (sc != null) {
                sc.setDisable(true);
                sc.setOpacity(0);
            }
        });
    }

    /**
     * splits the string at a whitespace, so that it has at most maxLen characters
     * if a too long string comes without whitespace, it stays together
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

    public TextEventState getState() {
        return state;
    }

    public void startNewText(JSONValue jsonValue, Map<String, String> keys, boolean showAfterwards) {
        this.showAfterwards = showAfterwards;
        List<JSONValue> h = jsonValue.getList();
        if (keys != null) {
            AtomicReference<String> s = new AtomicReference<>(h.get(0).getStr());
            keys.forEach((a, b) -> s.set(s.get().replaceAll(a, b)));
        }

        text = Objects.requireNonNull(splitToLines(h.get(0).getStr().replaceAll(" {3}", System.lineSeparator()), 80)).toArray(new String[0]);
        optionsNode.getChildren().clear();
        h.stream().skip(1).forEach(a -> {
            optionsAfterText.add(a.getStr());
            Button t = new Button(optionsAfterText.get(optionsAfterText.size() - 1));
            optionsNode.getChildren().add(t);
        });
        optionsNode.setAlignment(Pos.BOTTOM_RIGHT);
        GridPane.setHalignment(optionsNode, HPos.RIGHT);
        nextLine();
        createGrid();
    }

    public void startNewText(JSONValue jsonValue, Map<String, String> keys) {
        startNewText(jsonValue, keys, false);
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
        grid.add(getOptionsNode(), 0, 0);
        getOptionsNode().setVisible(false);
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
     */
    public void nextLine() {
        if (System.currentTimeMillis() > timeTillNextNextLine) {
            timeTillNextNextLine = System.currentTimeMillis() + 100;
            if (text.length > curLine + 1) {
                curLine++;
                field.setText(text[curLine - 1] + System.lineSeparator() + (text.length > curLine ? text[curLine] : ""));
                state = TextEventState.reading;
                System.out.println(Arrays.toString(text));
            } else {
                if (hasOptions()) {
                    state = TextEventState.selection;
                    optionsNode.setVisible(true);
                    updateSize();
                } else {
                    state = TextEventState.nothing;
                    if (!showAfterwards) {
                        field.setVisible(false);
                    }
                }
            }
        }
    }

    public boolean hasOptions() {
        return optionsAfterText.size() > 0;
    }

    public VBox getOptionsNode() {
        return optionsNode;
    }

    public GridPane getGrid() {
        return grid;
    }

    void updateSize() {
        optionsNode.setPrefWidth(((GridPane) optionsNode.getParent()).getWidth() * 0.2);
        Optional<Button> b = optionsNode.getChildren().stream().map(Button.class::cast).max(Comparator.comparingDouble(Region::getWidth));
        if (b.isPresent()) {
            optionsNode.getChildren().forEach(a -> ((Button) a).setMaxWidth(b.get().getWidth()));
        } else {
            optionsNode.getChildren().forEach(a -> ((Button) a).setMaxWidth(optionsNode.getPrefWidth()));
        }
    }

    public enum TextEventState {
        nothing,
        reading,
        selection
    }
}
