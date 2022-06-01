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

    private Player curPlayer;

    private long curMoney = 0;

    public void setClient(Client client) {
        this.client = client;
    }

    public MarketGUI(TextEvent t) {
        this.txtEvent = t;
    }

    //TODO ein Bug weil wenn man nicht schnell genug weggeht, dann kommt das auswahlfeld nochmals
    public void startNewMarket(Player p, long money) {
        this.curMoney = money;
        curPlayer = p;
        curShopItems = Item.getShop(8);
        System.out.println("MarketGUI.startNewMarket: " + curShopItems);
        if (curShopItems.size() > 100) System.out.println(p);
        page = 0;
        txtEvent.startNewText(TextEvent.TextEventIDsTranslater.MarketShopMeet.getId(), null);
        VBox v = txtEvent.getOptionsNode();
        ((Button) v.getChildren().get(0)).setOnAction(e -> showItemChooser());
        ((Button) v.getChildren().get(v.getChildren().size() - 1)).setOnAction(e -> {
            txtEvent.startNewText(TextEvent.TextEventIDsTranslater.MarketShopGoodBye.getId(), null);
        });
        System.out.println("preparation finished");
    }

    private void showItemChooser() {
        System.out.println("now in itemChooser");
        HashMap<String, String> items = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            Item it = curShopItems.get((page + i) % curShopItems.size());
            items.put("Item" + i, it.getName() + " " + it.getPrize() + "¥");
        }
        items.put("money", curMoney + "¥");
        txtEvent.startNewText(TextEvent.TextEventIDsTranslater.MarketShopItems.getId(), items, true);

        ObservableList<Node> children = txtEvent.getOptionsNode().getChildren();
        for (int i = 1; i < children.size() - 2; i++) {
            if (children.get(i) instanceof Button b) {
                b.setOnAction(a -> {
                    System.out.println(b.getText());
                });
            }
        }
        ((Button) children.get(0)).setOnAction(a -> {
            page += children.size() - 3;
            showItemChooser();
        });

        ((Button) children.get(children.size() - 2)).setOnAction(a -> {
            page += children.size() + 3;
            showItemChooser();
        });

        ((Button) children.get(children.size() - 1)).setOnAction(a -> {
            page = 0;
            startNewMarket(curPlayer, this.curMoney);
        });
    }

}
