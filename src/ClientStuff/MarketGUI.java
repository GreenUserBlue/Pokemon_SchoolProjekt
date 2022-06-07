package ClientStuff;

import InGame.Item;
import ServerStuff.MessageType;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;

/**
 * @author Zwickelstorfer Felix
 * @version 1.0
 *
 */
public class MarketGUI {

    /**
     * the current top item which will be displayed at the select screen
     */
    private int page = 0;

    /**
     * the client to send data
     */
    private Client client;

    /**
     * the textevent to show everything
     */
    private final TextEvent txtEvent;

    /**
     * the items which are currently displayed
     */
    private List<Item> curShopItems;

    /**
     * the player of the shop
     */
    private Player curPlayer;

    /**
     * how much money the player currently has (if there is any delay between server and client, so that the correct sum is always shown)
     */
    private long curMoney = 0;

    public void setClient(Client client) {
        this.client = client;
    }

    public MarketGUI(TextEvent t) {
        this.txtEvent = t;
    }

    /**
     * starts a new market
     * @param p the player
     * @param money the money the player possesses
     */
    public void startNewMarket(Player p, long money) {
        this.curMoney = money;
        curPlayer = p;
        curShopItems = Item.getShop(8);
        if (curShopItems.size() > 100) System.out.println(p);
        page = 0;
        txtEvent.startNewText(TextEvent.TextEventIDsTranslator.MarketShopMeet.getId(), null);
        VBox v = txtEvent.getOptionsNode();
        ((Button) v.getChildren().get(0)).setOnAction(e -> showItemChooser());
        ((Button) v.getChildren().get(v.getChildren().size() - 1)).setOnAction(e -> txtEvent.startNewText(TextEvent.TextEventIDsTranslator.MarketShopGoodBye.getId(), null));
    }

    /**
     * displays the selection for the items
     */
    private void showItemChooser() {
        HashMap<String, String> items = new HashMap<>();
        for (int i = 0; i < 3; i++) {
            Item it = curShopItems.get((page + i) % curShopItems.size());
            items.put("Item" + i, it.getName() + " " + it.getPrize() + "¥");
        }
        items.put("money", curMoney + "¥");
        txtEvent.startNewText(TextEvent.TextEventIDsTranslator.MarketShopItems.getId(), items, true);

        ObservableList<Node> children = txtEvent.getOptionsNode().getChildren();
        for (int i = 1; i < children.size() - 2; i++) {
            if (children.get(i) instanceof Button b) {
                int item = (((page + i) % curShopItems.size()) == 0 ? curShopItems.size() : ((page + i) % curShopItems.size()));
                b.setOnAction(a -> showItemBuyer(item));
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

    /**
     * displays the opportunity to buy an item
     * @param itemNbr the id of the item which will be bought
     */
    private void showItemBuyer(int itemNbr) {
        HashMap<String, String> vals = new HashMap<>();
        Item it = Item.getItem(itemNbr);
        curMoney = curPlayer.getMoney();
        vals.put("money", curMoney + "¥");
        vals.put("item", it.getName());
        int[] nbrs = new int[]{1, 5, 10};
        for (int i = 0; i < 3; i++) {
            vals.put("cost" + i, it.getPrize() * nbrs[i] + "¥");
        }
        Integer amount;
        synchronized (curPlayer.getItems()) {
            amount = curPlayer.getItems().get(itemNbr);
        }
        vals.put("amount", amount == null ? "0 times" : amount == 1 ? "1 time" : amount + " times");
        txtEvent.startNewText(TextEvent.TextEventIDsTranslator.MarketShopItemsBuy.getId(), vals, true);

        ObservableList<Node> children = txtEvent.getOptionsNode().getChildren();
        for (int i = 0; i < children.size() - 1; i++) {
            if (children.get(i) instanceof Button b) {
                int finalI = i;
                b.setOnAction(a -> {
                    int nbr = nbrs[finalI];
                    if (curMoney >= (long) it.getPrize() * nbr) {
                        client.send(MessageType.toStr(MessageType.itemBuy) + "," + itemNbr + "," + nbrs[finalI]);
                        synchronized (curPlayer.getItems()) {
                            curPlayer.getItems().putIfAbsent(itemNbr, 0);
                            curPlayer.getItems().put(itemNbr, curPlayer.getItems().get(itemNbr) + nbrs[finalI]);
                        }
                        curMoney -= (long) it.getPrize() * nbr;

                        HashMap<String, String> valsBuy = new HashMap<>();
                        valsBuy.put("amount", nbr + "");
                        valsBuy.put("item", it.getName() + (nbr > 1 ? "s" : ""));
                        txtEvent.startNewText(TextEvent.TextEventIDsTranslator.MarketShopEnoughMoney.getId(), valsBuy);
                    } else {
                        txtEvent.startNewText(TextEvent.TextEventIDsTranslator.MarketShopNoMoney.getId(), null);
                    }
                    txtEvent.addOnFin(() -> showItemBuyer(itemNbr));
                });
            }
        }
        ((Button) children.get(children.size() - 1)).setOnAction(a -> showItemChooser());
    }
}
