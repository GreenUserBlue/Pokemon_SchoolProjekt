package ClientStuff;

import ServerStuff.MessageType;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import java.util.Collections;

public class Menu {

    private final MyClient myClient;

    public Menu(MyClient myClient) {
        this.myClient = myClient;
    }

    private static long timeTillClosedMenu = 0;

    enum MenuType {
        normal,
        poke,
        items,
        badge,
        map,
        exit,
    }

    public void updatePlayerMenuPos() {
        Pane p = (Pane) myClient.getStage().getScene().getRoot().getChildrenUnmodifiable().get(1);
        ObservableList<Node> child = p.getChildren();
        for (int i = 0; i < child.size(); i++) {
            if (child.get(i) instanceof Button b) {
                b.setLayoutX(myClient.getStage().getScene().getWidth() - 100);
                b.setLayoutY((i + 1) * 40);
                b.setPrefWidth(80);
                b.setPrefHeight(35);
            }
        }
        p.setVisible(true);
    }

    public void showMenu() {
        if (timeTillClosedMenu < System.currentTimeMillis()) {
            myClient.getClient().getPlayers().get(0).setActivity(Player.Activity.menu);
            myClient.getClient().send(MessageType.toStr(MessageType.keysPres));
            Pane p = (Pane) myClient.getStage().getScene().getRoot().getChildrenUnmodifiable().get(1);
            ObservableList<Node> child = p.getChildren();
            p.setVisible(false);
            child.clear();
            Button pokemon = new Button("Pokemon");
            Button map = new Button("Map");
            Button items = new Button("Items");
            p.getChildren().addAll(pokemon, map, items);
            p.setPrefWidth(800);
            p.setPrefHeight(800);
            p.setOnKeyPressed(e -> {
                if (Keys.getKeys(Collections.singletonList(e.getCode())).contains(Keys.menu)) {
                    synchronized (myClient.getClient().getPlayers()) {
                        myClient.getClient().getPlayers().get(0).setActivity(Player.Activity.moving);
                        p.setVisible(false);
                        timeTillClosedMenu = System.currentTimeMillis() + 400;
                    }
                }
            });
        }
    }
}
