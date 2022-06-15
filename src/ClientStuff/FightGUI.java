package ClientStuff;

import Calcs.Utils;
import Calcs.Vector2D;
import ClientStuff.TextEvent.TextEventIDsTranslator;
import Envir.World;
import InGame.*;
import ServerStuff.MessageType;
import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.text.Font;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("SynchronizeOnNonFinalField")
public class FightGUI {

    public FightGUI(TextEvent txt) {
        this.textEvent = txt;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    private Client client;

    private final TextEvent textEvent;

    private Player thisPlayer;

    private Pokemon enemy;

    private Pokemon thisPoke;

    private boolean isAgainstPlayer = false;

    private Ball caughtWithBall = null;

    private static ImagePattern background;

    static {
        try {
            background = new ImagePattern(new Image(String.valueOf(Path.of("./res/Fight/Background.png").toUri().toURL())));
        } catch (MalformedURLException ignored) {
        }
    }

    public void startNewFight(String s) {
        String[] st = s.split("&");
        isAgainstPlayer = Boolean.parseBoolean(st[1]);
        String[] str = st[0].split("N");
        caughtWithBall = null;
        synchronized (thisPlayer) {
            thisPlayer.getPoke().clear();
            Arrays.stream(str).skip(1).filter(a -> !a.isBlank()).forEach(a -> thisPlayer.getPoke().add(Pokemon.getFromMsg(a.trim())));
        }
        thisPoke = thisPlayer.getPoke().get(0);
        enemy = Pokemon.getFromMsg(str[0]);
        HashMap<String, String> keys = new HashMap<>();
        keys.put("nameEn", enemy.getName());
        Platform.runLater(() -> {
            textEvent.startNewText(TextEventIDsTranslator.FightFirstTextStart.getId(), keys);
        });
        textEvent.addOnFin(this::doSingleMove);
    }

    private void doSingleMove() {
        HashMap<String, String> keys = new HashMap<>();
        synchronized (thisPoke) {
            keys.put("nameTh", thisPoke.getName());
        }
        textEvent.startNewText(TextEventIDsTranslator.FightWhatDo.getId(), keys);
        List<Button> children = textEvent.getOptionsNode().getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
        children.get(0).setOnAction((e) -> showAttacksSelect());
        children.get(1).setOnAction((e) -> showItemTypeSelect());
        children.get(2).setOnAction((e) -> showSwitchPokeSelect());
        children.get(3).setOnAction((e) -> showRunSelect());
    }

    private void showAttacksSelect() {
        HashMap<String, String> keys = new HashMap<>();
        List<Button> children;
        synchronized (thisPoke) {
            keys.put("nameTh", thisPoke.getName());
            Attack[] atts = thisPoke.getAttacks();
            for (int i = 0; i < atts.length; i++) {
                if (atts[i] != null) {
                    keys.put("name" + i, atts[i].getName());
                    keys.put("curAP" + i, atts[i].getCurAP() + "");
                    keys.put("AP" + i, atts[i].getAP() + "");
                }
            }
            textEvent.startNewText(TextEventIDsTranslator.FightAttackSel.getId(), keys, true);
            children = textEvent.getOptionsNode().getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
            for (int i = 0; i < Math.min(atts.length, children.size() - 1); i++) {
                if (atts[atts.length - i - 1] == null) {
                    textEvent.getOptionsNode().getChildren().remove(atts.length - i - 1);
                }
            }
        }
        children = textEvent.getOptionsNode().getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
        for (int i = 0; i < children.size() - 1; i++) {
            int finalI = i;
            children.get(i).setOnAction(e -> sendChoice(FightChoice.Attack, finalI + ""));
        }
        children.get(children.size() - 1).setOnAction((e) -> doSingleMove());
    }

    private void showRunSelect() {
        textEvent.startNewText(TextEventIDsTranslator.FightRunQues.getId(), null);
        List<Button> children = textEvent.getOptionsNode().getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();

        children.get(0).setOnAction((e) -> sendChoice(FightChoice.Surrender, null));
        children.get(children.size() - 1).setOnAction((e) -> doSingleMove());
    }

    private void showSwitchPokeSelect() {
        HashMap<String, String> keys = new HashMap<>();
        List<Pokemon> pokeToShow;
        synchronized (thisPlayer) {
            pokeToShow = thisPlayer.getPoke().stream().skip(1).toList();
        }
        AtomicInteger atI = new AtomicInteger();
        pokeToShow.forEach(a -> {
            keys.put("name" + atI.get(), a.getName());
            keys.put("curHP" + atI.get(), a.getCurHP() + "");
            keys.put("HP" + atI.get(), a.getMaxHP() + "");
            atI.incrementAndGet();
        });
        textEvent.startNewText(TextEventIDsTranslator.FightPokeSwitchInfo.getId(), keys, true);
        List<Button> children = new ArrayList<>(textEvent.getOptionsNode().getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast).toList());
        List<Button> toRem = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            if (pokeToShow.size() < i) {
                textEvent.getOptionsNode().getChildren().remove(pokeToShow.size());
            } else {
                int finalI = i;
                children.get(i).setOnAction(e -> sendChoice(FightChoice.Switch, finalI + ""));
                if (i >= thisPlayer.getPoke().size() - 1 || thisPlayer.getPoke().get(i + 1).getCurHP() == 0) {
                    toRem.add(children.get(i));
                }
            }

        }
        textEvent.getOptionsNode().getChildren().removeIf(toRem::contains);
        children.get(children.size() - 1).setOnAction((e) -> doSingleMove());
    }

    private void showItemTypeSelect() {
        if (!isAgainstPlayer) {
            textEvent.startNewText(TextEventIDsTranslator.FightItemTypeSel.getId(), null);
            List<Button> children = textEvent.getOptionsNode().getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
            children.get(0).setOnAction((e) -> showItemSingleSelect(Ball.class));
            children.get(1).setOnAction((e) -> showItemSingleSelect(Potion.class));
            children.get(children.size() - 1).setOnAction((e) -> doSingleMove());
        } else {
            showItemSingleSelect(Potion.class);
        }
//                keys.put("nameTh", thisPoke.getName());
//            textEvent.startNewText(TextEventIDsTranslator.FightAttackSel.getId(), keys, true);
//            List<Button> children = textEvent.getOptionsNode().getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
//            for (int i = 0; i < Math.min(atts.length, children.size() - 1); i++) {
//                if (atts[atts.length - i - 1] == null) {
//                    textEvent.getOptionsNode().getChildren().remove(atts.length - i - 1);
//                }
//            }
//            children.get(children.size() - 1).setOnAction((e) -> doSingleMove());
//    }
    }

    private void showItemSingleSelect(Class<? extends Item> myClass) {
        Map<Integer, Integer> itemsToShow;
        HashMap<String, String> keys = new HashMap<>();
        synchronized (thisPlayer) {
            itemsToShow = thisPlayer.getItems().entrySet().stream().filter((a) -> myClass.isInstance(Item.getItem(a.getKey()))).collect(Collectors.toMap((Map.Entry::getKey), (Map.Entry::getValue)));
            AtomicInteger i = new AtomicInteger();
            itemsToShow.forEach((a, b) -> {
                keys.put("item" + i.get(), Item.getItem(a).getName());
                keys.put("amount" + i.getAndIncrement(), b + "");
            });
            textEvent.startNewText(TextEventIDsTranslator.FightItemSingleSel.getId(), keys, true);
        }
        List<Button> children = textEvent.getOptionsNode().getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();
        for (int i = 0; i < children.size(); i++) {
            if (itemsToShow.size() < i) {
                textEvent.getOptionsNode().getChildren().remove(itemsToShow.size());
            } else {
                int finalI = i;
                children.get(i).setOnAction(e -> {
                    sendChoice(FightChoice.Item, itemsToShow.keySet().stream().toList().get(finalI) + "");
                });
            }
        }
        if (isAgainstPlayer) {
            children.get(children.size() - 1).setOnAction((e) -> doSingleMove());
        } else {
            children.get(children.size() - 1).setOnAction((e) -> showItemTypeSelect());
        }
    }

    private void sendChoice(FightChoice choice, String s) {
//        System.out.println(choice + "-value: " + s);
        client.send(MessageType.toStr(MessageType.inFightChoice) + choice + (s == null ? "" : "," + s));
        textEvent.startNewText(TextEventIDsTranslator.FightWaitingOpponent.getId(), null);
    }


    public void updateAll(String msg) {
        String[] split = msg.split("\\._\\.");
        updateSingle(split[0], split.length > 1 ? split[1] : /*split[0]*/null);
    }

    private void updateSingle(String msg, String msgForOther) {
        String[] s = msg.split("-\\|-");
        FightGUI.FightChoice choice = FightGUI.FightChoice.valueOf(s[0]);
        HashMap<String, String> keys = new HashMap<>();
        keys.put("start", s[1].startsWith("0") ? "You have" : "The enemy has");
        switch (choice) {
            case Surrender -> {
                doSurrender(s, keys);
                return;
            }
            case Switch -> doSwitchPokemon(s, keys);

            case Item -> {
                if (doItemsAndIsFightEnd(s, keys)) return;
            }

            case Attack -> {
                if (doAttackAndIsFightEnd(s, keys)) return;
            }
            case ChooseAfterDeath -> {
                doSwitchPokemon(s, keys);
            }
        }

        if (msgForOther != null) {
            textEvent.addOnFin(() -> updateSingle(msgForOther, null));
        } else {
            if (thisPlayer.getPoke().get(0).getCurHP() > 0 && enemy.getCurHP() > 0) {
                textEvent.addOnFin(this::doSingleMove);
            }
        }
    }

    private boolean doAttackAndIsFightEnd(String[] s, HashMap<String, String> keys) {
        if (s[1].startsWith("0")) showAttackLog(s, keys, thisPoke, enemy);
        else showAttackLog(s, keys, enemy, thisPoke);
        if (Utils.toInt(s[3]) < 1) {
            if (s[1].startsWith("0")) showPokeDefeated(s, keys, thisPoke, enemy, true);
            else showPokeDefeated(s, keys, enemy, thisPoke, false);
            return true;
        }
        return false;
    }

    private void showPokeDefeated(String[] s, HashMap<String, String> keys, Pokemon thisPoke, Pokemon enemy, boolean isMyThis) {
        Pokemon pokeNew = Pokemon.getFromMsg(s[5]);
        int xp = Utils.toInt(s[4]);
        synchronized (keys) {
            keys.put("xp", xp + "");
            keys.put("pokeEn", enemy.getName());
        }
        int oldLvl = thisPoke.getLevel();
        int oldId = thisPoke.getId();
        synchronized (keys) {
            keys.put("pokeThis", thisPoke.getName());
            thisPoke.addExp(xp);
            if (pokeNew.getLevel() != oldLvl) {
                keys.put("lvl", pokeNew.getLevel() + "");
                if (pokeNew.getId() != oldId) {
                    keys.put("pokeNew", pokeNew.getName());
                }
            }
        }
        System.out.println("FightGUI.showPokeDefeated: " + thisPoke.getXp());
        System.out.println("FightGUI.showPokeDefeated: " + thisPoke.getMaxXPNeeded());
        textEvent.addOnFin(() -> {
            textEvent.startNewText(TextEventIDsTranslator.FightEnemyKilled.getId(), keys);
            textEvent.addOnFin(() -> {
                if (keys.get("lvl") != null) {
                    textEvent.startNewText(TextEventIDsTranslator.FightLevelUp.getId(), keys);
                    textEvent.addOnFin(() -> {
                        if (keys.get("pokeNew") != null) {
                            textEvent.startNewText(TextEventIDsTranslator.FightPokeEvolves.getId(), keys);
                            textEvent.addOnFin(() -> {
                                showTextAfterPokeDeafeated(s, keys, isMyThis, pokeNew);
                            });
                        } else {
                            showTextAfterPokeDeafeated(s, keys, isMyThis, pokeNew);
                        }
                    });
                } else {
                    showTextAfterPokeDeafeated(s, keys, isMyThis, pokeNew);
                }
            });
        });
    }

    private void showTextAfterPokeDeafeated(String[] s, HashMap<String, String> keys, boolean isMyThis, Pokemon pokeNew) {
        if (isMyThis) {
            this.thisPoke = pokeNew;
            if (s[6].startsWith("c")) {
                textEvent.startNewText(TextEventIDsTranslator.FightWaitingOpponent.getId(), null);
            } else {
                keys.put("situation", "won");
                keys.put("amount", Utils.toInt(s[7]) + "");
                endFight(keys);
            }
        } else {
            this.enemy = pokeNew;
            if (s[6].startsWith("c")) {
                showSelectionAfterDefeat();
            } else {
                keys.put("situation", "lost");
                keys.put("amount", Utils.toInt(s[7]) + "");
                endFight(keys);
            }
        }
    }

    private void showSelectionAfterDefeat() {
        HashMap<String, String> keys = new HashMap<>();
        List<Pokemon> pokeToShow;
        synchronized (thisPlayer) {
            pokeToShow = thisPlayer.getPoke().stream().skip(1).toList();
        }
        AtomicInteger atI = new AtomicInteger();
        pokeToShow.forEach(a -> {
            keys.put("name" + atI.get(), a.getName());
            keys.put("curHP" + atI.get(), a.getCurHP() + "");
            keys.put("HP" + atI.get(), a.getMaxHP() + "");
            atI.incrementAndGet();
        });
        textEvent.startNewText(TextEventIDsTranslator.FightPokeSwitchInfo.getId(), keys, true);
        List<Button> children = new ArrayList<>(textEvent.getOptionsNode().getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast).toList());
        List<Button> toRem = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            if (pokeToShow.size() < i) {
                textEvent.getOptionsNode().getChildren().remove(pokeToShow.size());
            } else {
                int finalI = i;
                children.get(i).setOnAction(e -> sendChoice(FightChoice.ChooseAfterDeath, finalI + ""));
                if (i >= thisPlayer.getPoke().size() - 1 || thisPlayer.getPoke().get(i + 1).getCurHP() == 0) {
                    toRem.add(children.get(i));
                }
            }

        }
        textEvent.getOptionsNode().getChildren().removeIf(toRem::contains);

        children.get(children.size() - 1).setText("Give Up");
        children.get(children.size() - 1).setOnAction((e) -> sendChoice(FightChoice.ChooseAfterDeath, "s"));
    }

    private void showAttackLog(String[] s, HashMap<String, String> keys, Pokemon thisPoke, Pokemon enemy) {
        keys.put("pokeThis", thisPoke.getName());
        Attack att = thisPoke.getAttacks()[Utils.toInt(s[2])];
        synchronized (keys) {
            keys.put("effect", enemy.getEffectness(att.getType()));
            keys.put("att", att.getName());
        }
        att.use();
        Platform.runLater(() -> textEvent.startNewText(TextEventIDsTranslator.FightAttacks.getId(), keys));
        enemy.setCurHP(Utils.toInt(s[3]));
    }

    private boolean doItemsAndIsFightEnd(String[] s, HashMap<String, String> keys) {
        Item it = Item.getItem(Utils.toInt(s[2]));
        if (it instanceof Potion) {
            if (s[1].startsWith("0")) {
                keys.put("start", "You have");
                keys.put("poke", thisPoke.getName());
                keys.put("oldHP", thisPoke.getCurHP() + "");
                thisPoke.setCurHP(Utils.toInt(s[3]));
                keys.put("newHP", thisPoke.getCurHP() + "");
            } else {
                keys.put("start", "The enemy has");
                keys.put("poke", enemy.getName());
                keys.put("oldHP", enemy.getCurHP() + "");
                enemy.setCurHP(Utils.toInt(s[3]));
                keys.put("newHP", enemy.getCurHP() + "");
            }
            Platform.runLater(() -> {
                textEvent.startNewText(TextEventIDsTranslator.FightUseItem.getId(), keys);
            });
        } else if (it instanceof Ball b) {
            if (s[3].startsWith("c")) {
                caughtWithBall = b;
                keys.put("poke", enemy.getName());
                Platform.runLater(() -> {
                    textEvent.startNewText(TextEventIDsTranslator.FightEndCapture.getId(), keys);
                    textEvent.addOnFin(() -> {
                        synchronized (thisPlayer) {
                            thisPlayer.setActivity(Player.Activity.standing);
                        }
                    });
                });
                return true;
            } else if (s[3].startsWith("f")) {
                System.out.println("kein Pokemon gefangen (noch kein TextEvent)");
            }
        }
        return false;
    }

    private void doSwitchPokemon(String[] s, HashMap<String, String> keys) {
        if (s[1].equals("0")) {
            keys.put("start", "You have");
            synchronized (thisPoke) {
                keys.put("pokeOld", thisPoke.getName());
                Utils.switchObjects(thisPlayer.getPoke(), Utils.toInt(s[2]));
                thisPoke = thisPlayer.getPoke().get(0);
                keys.put("pokeNew", thisPoke.getName());
            }
        } else {
            keys.put("start", "The enemy has");
            synchronized (thisPoke) {
                keys.put("pokeOld", enemy.getName());
                enemy = Pokemon.getFromMsg(s[4]);
                keys.put("pokeNew", enemy.getName());
            }
        }
        Platform.runLater(() -> {
            textEvent.startNewText(TextEventIDsTranslator.FightSwitchPoke.getId(), keys);
        });
    }

    private void doSurrender(String[] s, HashMap<String, String> keys) {
        keys.put("situation", s[1].equals("0") ? "lost" : "won");
        keys.put("amount", Utils.toInt(s[2]) + "");
        endFight(keys);
    }

    private void endFight(HashMap<String, String> keys) {
        Platform.runLater(() -> {
            textEvent.startNewText(TextEventIDsTranslator.FightEnd.getId(), keys);
            textEvent.addOnFin(null);
            synchronized (thisPlayer) {
                thisPlayer.setActivity(Player.Activity.textEvent);
            }
        });
    }


    public void draw(Canvas canvas, Vector2D size, Map<String, Image> allImgs) {
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);
        canvas.setWidth(size.getX());
        canvas.setHeight(size.getY());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, size.getX(), size.getY());
        if (thisPlayer == null || enemy == null || client == null) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, size.getX(), size.getY());
        } else {
            gc.setFill(background != null ? background : Color.GREEN);
//                gc.setFill(Color.GREEN);
//        }
            gc.fillRect(0, 0, size.getX(), size.getY());
            Pokemon pokePlayer;
            synchronized (thisPlayer) {
                pokePlayer = thisPlayer.getPoke().get(0);
            }
            synchronized (enemy) {
                double oneP = size.clone().mult(0.01).getX();
                Image imgForFightBottomGrass = allImgs.get("FightBottomGrass");
                Vector2D sizeForFightBottomGrass = new Vector2D(30, 0);
                sizeForFightBottomGrass.setY(sizeForFightBottomGrass.getX() * imgForFightBottomGrass.getHeight() / imgForFightBottomGrass.getWidth());

                drawOnCenterWithPercentage(gc, imgForFightBottomGrass, new Vector2D(70, 20), sizeForFightBottomGrass, oneP);
                if (caughtWithBall == null) {
                    drawOnCenterWithPercentage(gc, enemy.getImage(), new Vector2D(70, 16.5), new Vector2D(20, 20), oneP);
                } else {
                    drawOnCenterWithPercentage(gc, allImgs.get("Ball" + caughtWithBall.getId()), new Vector2D(70, 20), new Vector2D(4, 4), oneP);
                }

                drawOnCenterWithPercentage(gc, imgForFightBottomGrass, new Vector2D(25, 43), sizeForFightBottomGrass.mult(1.2), oneP);
                drawOnCenterWithPercentage(gc, pokePlayer.getBackImage(), new Vector2D(25, 37), new Vector2D(30, 30), oneP);

                gc.fillRoundRect(oneP * 50, oneP * 38, oneP * 25, oneP * 8, oneP * 2, oneP * 2);

                drawPokemonInfo(gc, oneP, enemy, 5, 2);
                drawPokemonInfo(gc, oneP, pokePlayer, 50, 38);
            }
        }
    }

    private void drawPokemonInfo(GraphicsContext gc, double oneP, Pokemon poke, int startX, int startY) {

        gc.setFill(Color.BLACK);
        gc.fillRoundRect(oneP * startX, oneP * startY, oneP * 25, oneP * 8, oneP * 2, oneP * 2);

        gc.setFont(new Font(oneP * 2));
        gc.setFill(Color.WHITE);
        gc.fillText(poke.getName(), oneP * (startX + 1), oneP * (startY + 3));

        gc.fillText((int) poke.getCurHP() + "/" + (int) poke.getMaxHP(), oneP * (startX + 17), oneP * (startY + 3));

        gc.setFill(Color.GRAY);
        gc.fillRoundRect(oneP * (startX + 1), oneP * (startY + 5), oneP * 23, oneP * 1.2, oneP, oneP);
        double x;
        // the double and int cast are important for graphics to get rid of the decimal values, but still a decimal result
        gc.setFill((x = (int) poke.getCurHP() / (double) (int) poke.getMaxHP()) > 0.5 ? Color.GREEN : x > 0.2 ? Color.YELLOW : Color.RED);
        gc.fillRoundRect(oneP * (startX + 1), oneP * (startY + 5), oneP * 23 * x, oneP * 1.2, oneP, oneP);

        gc.setFill(Color.GRAY);
        gc.fillRoundRect(oneP * (startX + .5), oneP * (startY + 7.3), oneP * 24, oneP * .5, oneP, oneP);
        gc.setFill(Color.GREENYELLOW);
        gc.fillRoundRect(oneP * (startX + .5), oneP * (startY + 7.3), oneP * 24 * poke.getXp() / poke.getMaxXPNeeded(), oneP * .5, oneP, oneP);
    }

    /**
     * draws a picturce with the center given and percentages as values
     *
     * @param gc         graphics context to draw with
     * @param img        image to draw
     * @param pos        centerpos
     * @param size       size of img
     * @param onePercent one percent of where to draw
     */
    public void drawOnCenterWithPercentage(GraphicsContext gc, Image img, Vector2D pos, Vector2D size, double onePercent) {
        Vector2D posit = new Vector2D(onePercent * (pos.getX() - size.getX() / 2), onePercent * (pos.getY() - size.getY() / 2));
        gc.drawImage(img, posit.getX(), posit.getY(), onePercent * (size.getX()), onePercent * (size.getY()));//0, 0, img.getWidth(), img.getHeight()
    }

    public void startTests() {
        isAgainstPlayer = false;
        Pokemon thisP = Pokemon.createPokemon(new Vector2D(6969, 420), World.Block.Grass);
//        enemy = Pokemon.createPokemon(new Vector2D(10, 10), World.Block.Grass);
        thisPlayer = new Player("name", new Vector2D(12, 23), 2, "Name");
        thisPlayer.getPoke().add(thisP);
//        enemy.setCurHP(enemy.getCurHP() * 0.21);
//        thisPlayer.getPoke().get(0).setCurHP(thisPlayer.getPoke().get(0).getCurHP() * 1 / 2 + 1);
//        System.out.println(thisPlayer.getPoke().get(0).getMaxXPNeeded());
//        System.out.println(.getCurHP());
        thisPlayer.getPoke().get(0).addExp(40000);
        enemy = Pokemon.getFromMsg(thisP.toMsg());
    }

    public void setPlayer(Player player) {
        this.thisPlayer = player;
    }

    public enum FightChoice {
        Attack(0),
        Switch(1),
        Item(1),
        Surrender(2),
        ChooseAfterDeath(3);

        public int getPriority() {
            return priority;
        }

        private final int priority;

        FightChoice(int priority) {
            this.priority = priority;
        }
    }
}
