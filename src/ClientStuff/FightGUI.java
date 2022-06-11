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
import javafx.scene.text.Font;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public void startNewFight(String s) {
        String[] str = s.split("N");
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
        children.get(2).setOnAction((e) -> showPokeSelect());
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

    private void showPokeSelect() {
        HashMap<String, String> keys = new HashMap<>();
        List<Pokemon> pokeToShow;
        synchronized (thisPlayer) {
            pokeToShow = thisPlayer.getPoke().stream().skip(1).filter((a) -> a.getCurHP() > 0).toList();
        }
        AtomicInteger atI = new AtomicInteger();
        pokeToShow.forEach(a -> {
            keys.put("name" + atI.get(), a.getName());
            keys.put("curHP" + atI.get(), (int) a.getCurHP() + "");
            keys.put("HP" + atI.get(), (int) a.getMaxHP() + "");
            atI.incrementAndGet();
        });
        textEvent.startNewText(TextEventIDsTranslator.FightPokeSwitchInfo.getId(), keys, true);
        List<Button> children = textEvent.getOptionsNode().getChildren().stream().filter(Button.class::isInstance).map(Button.class::cast).toList();

        for (int i = 0; i < children.size(); i++) {
            if (pokeToShow.size() < i) {
                textEvent.getOptionsNode().getChildren().remove(pokeToShow.size());
            } else {
                int finalI = i;
                children.get(i).setOnAction(e -> {
                    sendChoice(FightChoice.Switch, finalI + "");
                });
            }
        }
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
        //        itemsToShow.keySet().stream().map(Item::getItem);
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
        System.out.println("FightGUI.updateAll: " + msg);
        String[] split = msg.split("\\._\\.");
        updateSingle(split[0], split.length > 1 ? split[1] : split[0]);
    }

    private void updateSingle(String msg, String msgForOther) {
        String[] s = msg.split("-\\|-");
        FightGUI.FightChoice choice = FightGUI.FightChoice.valueOf(s[0]);
        HashMap<String, String> keys = new HashMap<>();
        switch (choice) {
            case Surrender -> {
                keys.put("situation", s[1].equals("0") ? "lost" : "won");
                if (!isAgainstPlayer) {
                    keys.put("amount", "0");
                }
                Platform.runLater(() -> {
                    textEvent.startNewText(TextEventIDsTranslator.FightEnd.getId(), keys);
                    synchronized (thisPlayer) {
                        thisPlayer.setActivity(Player.Activity.textEvent);
                    }
                });
                return;
            }
            case Switch -> {
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
                        enemy = Pokemon.getFromMsg(s[2]);
                        keys.put("pokeNew", enemy.getName());
                    }
                }
                Platform.runLater(() -> {
                    textEvent.startNewText(TextEventIDsTranslator.FightSwitchPoke.getId(), keys);
                });

            }

            case Item -> {
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
                    // sToSend.append("t");// stands for "too many pokemon already"
                    //                            } else if (c.getOtherPoke().getsCaptured(b)) {
                    //                                player.setActivity(Player.Activity.standing);
                    //                                c.setOtherPoke(null);
                    //                                sendPosUpdate(c);
                    //                                sToSend.append("c"); // stands for "captured"
                    //                            } else {
                    //                                sToSend.append("f"); // stands for "failed to capture"
                    if (s[3].startsWith("c")) {
                        keys.put("poke", enemy.getName());
                        Platform.runLater(() -> {
                            textEvent.startNewText(TextEventIDsTranslator.FightEndCapture.getId(), keys);
                            synchronized (thisPlayer) {
                                thisPlayer.setActivity(Player.Activity.textEvent);
                            }
                        });
                        return;
                    }
                }
            }
        }
//        if (msgForOther != null) {
//            textEvent.addOnFin(() -> updateSingle(msgForOther, null));
//        } else {
        textEvent.addOnFin(this::doSingleMove);
//        }
    }


    public void draw(Canvas canvas, Vector2D size, Map<String, Image> allImgs) {
        canvas.setLayoutX(0);
        canvas.setLayoutY(0);
        canvas.setWidth(size.getX());
        canvas.setHeight(size.getY());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, size.getX(), size.getY());
        if (thisPlayer == null || enemy == null/* || client == null*/) {//TODO später wieder reinhauen, aber daweil hab ich nix
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, size.getX(), size.getY());
        } else {
            gc.setFill(Color.DARKMAGENTA);
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
                drawOnCenterWithPercentage(gc, enemy.getImage(), new Vector2D(70, 16.5), new Vector2D(20, 20), oneP);

                drawOnCenterWithPercentage(gc, imgForFightBottomGrass, new Vector2D(25, 43), sizeForFightBottomGrass.mult(1.2), oneP);
                drawOnCenterWithPercentage(gc, pokePlayer.getBackImage(), new Vector2D(25, 37), new Vector2D(30, 30), oneP);

                gc.fillRoundRect(oneP * 50, oneP * 38, oneP * 25, oneP * 8, oneP * 2, oneP * 2);

                drawPokemonInfo(gc, oneP, enemy, 5, 2);
                drawPokemonInfo(gc, oneP, pokePlayer, 50, 38);

//                gc.fillText("HalloWelt", 100, 100);
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
        Surrender(2);

        public int getPriority() {
            return priority;
        }

        private final int priority;

        FightChoice(int priority) {
            this.priority = priority;
        }
    }
}
