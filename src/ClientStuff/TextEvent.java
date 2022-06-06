package ClientStuff;

import JsonParser.JSONParser;
import JsonParser.JSONValue;
import ServerStuff.MessageType;
import javafx.animation.AnimationTimer;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * @author Zwickelstorfer Felix
 * @version 2.1
 * <p>
 * presents a graphic grid-pane with a textarea, which always presents two lines of a text
 */
public class TextEvent {

    // 0-999 send to server result
    // 0-99 single: afterwards close field
    // 100-199 multiple: show and if last then close field
    // 200-299

    // 1000-1999 do not send to server
    // 1000-1099 single: afterwards let field open
    // 1100-1199 multiple: afterwards let field open

    // 2000-2099 multiple: send only if not last

    private final List<String> optionsAfterText = new ArrayList<>();

    public TextArea getField() {
        return field;
    }

    private final TextArea field;

    private final VBox optionsNode = new VBox();

    private final GridPane grid = new GridPane();

    private String[] text;

    private boolean isCurFin = true;

    private String curTextToWrite = "";

    private int curLine = 0;

    private int curCharsShown = 0;

    private TextEventState state = TextEventState.nothing;

    private int curTextNbr = -1;

    private Client client;

    /**
     * what happens if
     */
    private List<Runnable> onFin = new ArrayList<>();

    public void setClient(Client client) {
        this.client = client;
    }


    public void addOnFin(Runnable c) {
        onFin.add(c);
    }

    private static final Map<Integer, JSONValue> eventTexts = new HashMap<>();

    private boolean isInstantFin = true;

    public TextEvent() {
        field = new TextArea();
        field.setMinHeight(field.getFont().getSize() * 3);
        field.setEditable(false);
        field.setFocusTraversable(false);
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
                    list.add(s.substring(i - maxLen, i - maxLen + j + System.lineSeparator().length() - 1));
                    isLineBreak = true;
                    i += j + 1 + System.lineSeparator().length() - 1;
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

    public void startNewText(int jsonValue, Map<String, String> keys) {
        startNewText(jsonValue, keys, false);
    }

    public void startNewText(int jsonValue, Map<String, String> keys, boolean isInstantFin) {
        curLine = 0;
        curCharsShown = 0;
        curTextToWrite = null;
        this.isInstantFin = isInstantFin;
        isCurFin = true;
        this.curTextNbr = jsonValue;
        List<JSONValue> h = eventTexts.get(jsonValue).getList();
        AtomicReference<String> s = new AtomicReference<>(h.get(0).getStr());
        if (keys != null) {
            keys.forEach((a, b) -> s.set(s.get().replaceAll("%[$]%" + a + "%[$]%", b)));
        }
        text = Objects.requireNonNull(splitToLines(s.get().replaceAll(" {3}", System.lineSeparator()), 80)).toArray(new String[0]);
        optionsNode.getChildren().clear();
        optionsAfterText.clear();
        h.stream().skip(1).forEach(a -> {
            if (keys != null) {
                AtomicReference<String> str = new AtomicReference<>(a.getStr());
                keys.forEach((c, b) -> str.set(str.get().replaceAll("%[$]%" + c + "%[$]%", b)));
                optionsAfterText.add(str.get());
            } else {
                optionsAfterText.add(a.getStr());
            }
            Button t = new Button(optionsAfterText.get(optionsAfterText.size() - 1));
            optionsNode.getChildren().add(t);
        });
        optionsNode.setAlignment(Pos.BOTTOM_RIGHT);
        GridPane.setHalignment(optionsNode, HPos.RIGHT);
        nextLine();
        createGrid();
    }

    // My Guess is that this text will go on for now an that you will have some fun for waiting till you can press again and then you shall die because i have depression and i want to tell you about it.   I hope you won't delete this game now because this text-box is going on for now.
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
    public boolean nextLine() {
        if (text != null && isCurFin) {
            isCurFin = false;
            if (text.length > curLine + 1 || (text.length == 1 && curLine == 0)) {
                curLine++;
                curTextToWrite = text[curLine - 1] + System.lineSeparator() + (text.length > curLine ? text[curLine] : "");
                curCharsShown = curLine != 1 ? Math.max(curTextToWrite.indexOf(System.lineSeparator()), 1) : 1;
                AnimationTimer timer = new AnimationTimer() {
                    long timeTillNextUse = 0;

                    @Override
                    public void handle(long l) {
                        if (timeTillNextUse < System.currentTimeMillis()) {
                            timeTillNextUse = System.currentTimeMillis() + 15;
                            if (curCharsShown < curTextToWrite.length()) {
//                                field.setText(curTextToWrite.substring(0, (curCharsShown += (isInstantFin ? (Math.min(5, curTextToWrite.length() - curCharsShown)) : 1))));
                                field.setText(curTextToWrite.substring(0, Math.min((curCharsShown += (isInstantFin ? (Math.min(5, curTextToWrite.length() - curCharsShown)) : 1)), curTextToWrite.length())));
                                if (field.getText().equals(curTextToWrite)) {
                                    curCharsShown = curTextToWrite.length() + 1;
                                }
                            } else {
                                isCurFin = true;
                                stop();
                                if (hasOptions()) nextLine();
                            }
                        }
                    }
                };
                timer.start();
                field.setText("");
                state = TextEventState.reading;
            } else {
                if (hasOptions()) {
                    state = TextEventState.selection;
                    optionsNode.setVisible(true);
                    if (client != null) {
                        client.getKeysPressed().removeAll(Arrays.asList(KeyCode.SPACE, KeyCode.ENTER));
                    }
                    updateSize();

                } else {
                    state = TextEventState.nothing;
                    if (curTextNbr < 1000) {
                        System.out.println("");//TODO something here
                        if (curTextNbr < 100) {
                            field.setVisible(false);
                        }
                        if (client != null) {
                            client.send(MessageType.toStr(MessageType.textEvent) + 0 + curTextNbr);
                        }
                    } else {
                        if (curTextNbr < 1100) {
//                            System.out.println("TextEvent.nextLine: fieldStaysVisible");
                        }
                    }
                }
//      a          timeAtLastFin = System.currentTimeMillis();
                if (text.length <= 1) {
                    field.setText(text[0]);
                }
                onFin.forEach(a -> {
//                    Platform.runLater(() -> {
                    a.run();
                    System.out.print("");
//                    });
                });
                onFin.clear();
                return true;
            }
        }
        return false;
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

    public void updateSize(Scene scene) {
        grid.setMinSize(scene.getWidth(), scene.getHeight());
        grid.setMaxSize(scene.getWidth(), scene.getHeight());
        grid.setPrefSize(scene.getWidth(), scene.getHeight());
    }

    public boolean isFinToWalk() {
        return Arrays.stream(TextEventIDsTranslator.values()).filter(a -> a.getId() == curTextNbr).findFirst().orElse(TextEventIDsTranslator.Tree).isWalkableAfterwards;
    }

    public enum TextEventState {
        nothing,
        reading,
        selection
    }

    public static void initTexts() {
        Map<String, JSONValue> c = JSONParser.read(Path.of("./res/DataSets/texts.json"));
        c.forEach((key, value) -> value.getMap().forEach((a, b) -> eventTexts.put(Integer.parseInt(a) + Integer.parseInt(key), b)));
    }

    public enum TextEventIDsTranslator {
        Tree(0, true),
        BigShelf(1, true),
        SmallShelf(2, true),
        OnlyDeco(2, true),
        PokeHeal(3, true),
        FightEnd(4, true),
        WrongItem(5, false),
        PlayersMeetQues(6, false),
        PlayersMeetAns(100, false),
        MarketShopItems(1100, false),
        MarketShopItemsBuy(1101, false),
        MarketShopMeet(101, false),
        MarketShopNoMoney(1004, false),
        MarketShopEnoughMoney(1005, false),
        MarketShopGoodBye(7, true);

        private final int id;


        private final boolean isWalkableAfterwards;

        TextEventIDsTranslator(int val, boolean isWalkableAfterwards) {
            this.isWalkableAfterwards = isWalkableAfterwards;
            id = val;
        }

        public int getId() {
            return id;
        }

        public boolean getIsWalkableAfterwards() {
            return isWalkableAfterwards;
        }
    }
}
