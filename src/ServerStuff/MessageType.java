package ServerStuff;

public enum MessageType {
    hellman,             //Client + Server
    register,            //Client
    login,               //Client
    logout,              //Client
    delete,              //Client
    profile,             //Server + Client
    worldSelect,         //Client
    keysPres,            //Client
    updatePos,           //Server
    textEvent,           //Server
    houseRequest,        //Client
    pokAppear,           //Server
    playerInteraction,   //Client + Server
    itemBuy,             //Client
    itemData,            //Server
    itemRequest,         //Client
    inFight,             //Client + Server
    pokRequest,          //Client
    levelUp,             //Server
    badgeRequest,        //Client
    error,               //Client + Server
    ;

    public static MessageType getType(int val) {
        for (MessageType m : MessageType.values()) {
            if (val == m.ordinal()) return m;
        }
        return error;
    }

    public static String toStr(MessageType m) {
        return String.format("%03d", m.ordinal());
    }
}

