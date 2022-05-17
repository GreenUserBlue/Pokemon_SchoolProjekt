package ClientStuff;

import InGame.Item;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;

public class MarketGUI {

    private int page = 0;

    private Client client;

    private final TextEvent txtEvent;

    private List<Item> curShopItems;

    public void setClient(Client client) {
        this.client = client;
    }

    public MarketGUI(TextEvent t) {
        this.txtEvent = t;
    }

    public void startNewMarket(Player p) {
        curShopItems = Item.getShop(8);
        if (curShopItems.size() > 100) System.out.println(p);
        page = 0;
        txtEvent.startNewText(TextEvent.TextEventIDsTranslater.MarketShopMeet.getId(), null);
        VBox v = txtEvent.getOptionsNode();
        ((Button) v.getChildren().get(0)).setOnAction(e -> showItemChooser());
    }

    private void showItemChooser() {
        HashMap<String, String> items = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            Item it = curShopItems.get((page + i) % curShopItems.size());
            items.put("Item" + i, it.getName() + " " + it.getPrize() + "€");
        }
        txtEvent.startNewText(TextEvent.TextEventIDsTranslater.MarketShopItems.getId(), items,true);

        ObservableList<Node> childs = txtEvent.getOptionsNode().getChildren();
        for (int i = 1; i < childs.size() - 2; i++) {
            if (childs.get(i) instanceof Button b) {
                b.setOnAction(a -> {

                });
            }
        }
        ((Button) childs.get(0)).setOnAction(a -> {
            page += childs.size() - 3;
            showItemChooser();
        });
    }

}
