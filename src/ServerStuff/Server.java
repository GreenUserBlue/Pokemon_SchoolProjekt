package ServerStuff;

import Calcs.Crypto;
import ClientStuff.Keys;
import ClientStuff.Player;
import Envir.World;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Zwickelstorfer Felix
 * A Server, which can add and remove Clients
 * @version 1.3
 */
public class Server {

    /**
     * the Server
     */
    protected final ServerSocket server;

    /**
     * on which port the server runs
     */
    protected final int port;

    /**
     * the max number of clients, which can connected at the same time
     * <br><br>Negative numbers mean there is no limit
     */
    protected final int maxClients;

    /**
     * The ip of the Server
     */
    protected final String ip;

    /**
     * if all clients will be accepted, until the max is reached
     */
    private final AtomicBoolean acceptAll = new AtomicBoolean(false);

    private final ArrayList<BiConsumer<ClientHandler, Object>> onMessageForNew = new ArrayList<>();

    private final ArrayList<Consumer<ClientHandler>> onUpdateForNew = new ArrayList<>();

    private final ArrayList<Consumer<ClientHandler>> onConnectForNew = new ArrayList<>();

    /**
     * saves all the regions which are possible
     */
    private final List<World> regions = new ArrayList<>();

    /**
     * all the Clients that are currently Connected
     */
    protected HashMap<Integer, ClientHandler> clients = new HashMap<>();

    /**
     * counts how many clients had ever been connected
     */
    private int clientCount = 0;

    /**
     * to accept clients automatically
     */
    private Thread th;

    /**
     * starts a Server
     *
     * @param port       {@link Server#port}
     * @param ip         {@link Server#ip}
     * @param maxClients {@link Server#maxClients}
     */
    public Server(int port, String ip, int maxClients) throws IOException {
        this.port = port;
        this.ip = ip;
        this.maxClients = maxClients;
        server = new ServerSocket(port);
        System.out.println("Started server on Port: " + port);
    }

    /**
     * starts a Server with unlimited Clients
     *
     * @param port {@link Server#port}
     * @param ip   {@link Server#ip}
     */
    public Server(int port, String ip) throws IOException {
        this(port, ip, -1);
    }

    public HashMap<Integer, ClientHandler> getClients() {
        return clients;
    }

    public List<World> getWorlds() {
        return regions;
    }

    /**
     * @param acceptAll sets {@link Server#acceptAll}
     */
    public void setAcceptAll(boolean acceptAll) {
        if (!this.acceptAll.get()) {
            this.acceptAll.set(acceptAll);
            th = new Thread(() -> {
                while (this.acceptAll.get()) {
                    try {
                        addAndChooseClient(true);
                    } catch (IOException ignored) {
                    }
                }
            });
            th.start();
        } else {
            this.acceptAll.set(false);
            th.interrupt();
        }
    }

    /**
     * searches if there are any free ids, which means if there are clients which have disconnected
     *
     * @return if one exists, the free id-nbr, otherwise -1
     */
    private int getFreeID() {
        Optional<Map.Entry<Integer, ClientHandler>> i = clients.entrySet().stream().filter(e -> e.getValue() == null).findFirst();
        return i.isPresent() ? i.get().getKey() : -1;
    }

    /**
     * adds a {@link BiConsumer} when a message is sent
     *
     * @param c   what should happen when a message is sent
     * @param ids the ids of the client which sent it <br>
     *            if none, then everybody gets it
     */
    public void setOnMessage(boolean addNew, BiConsumer<ClientHandler, Object> c, int... ids) {
        if (ids.length == 0) {
            clients.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> e.getValue().allOnMessage.add(c));
        } else {
            clients.entrySet().stream().filter(e -> Arrays.stream(ids).filter(val -> val == e.getKey() && e.getValue() != null).findFirst().isPresent()).forEach(e -> e.getValue().allOnMessage.add(c));
        }
        if (addNew) {
            onMessageForNew.add(c);
        }
    }

    /**
     * adds a {@link BiConsumer} when an update happens
     *
     * @param c   what should happen when a update happens
     * @param ids the ids of the client which sent it <br>
     *            if none, then everybody gets it
     */
    public void setOnUpdate(boolean addNew, Consumer<ClientHandler> c, int... ids) {
        if (ids.length == 0) {
            clients.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> e.getValue().allOnUpdate.add(c));
        } else {
            clients.entrySet().stream().filter(e -> Arrays.stream(ids).filter(val -> val == e.getKey() && e.getValue() != null).findFirst().isPresent()).forEach(e -> e.getValue().allOnUpdate.add(c));
        }
        if (addNew) {
            onUpdateForNew.add(c);
        }
    }

    /**
     * removes {@link Server#setOnMessage}
     */
    public void remOnMessage(boolean addNew, BiConsumer<ClientHandler, Object> c, int... ids) {
        if (ids.length == 0) {
            clients.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> e.getValue().allOnMessage.remove(c));
        } else {
            clients.entrySet().stream().filter(e -> Arrays.stream(ids).filter(val -> val == e.getKey() && e.getValue() != null).findFirst().isPresent()).forEach(e -> e.getValue().allOnMessage.remove(c));
        }
        if (addNew) {
            onMessageForNew.remove(c);
        }
    }

    /**
     * removes {@link Server#setOnUpdate}
     */
    public void remOnUpdate(boolean addNew, Consumer<ClientHandler> c, int... ids) {
        if (ids.length == 0) {
            clients.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> e.getValue().allOnMessage.remove(c));
        } else {
            clients.entrySet().stream().filter(e -> Arrays.stream(ids).filter(val -> val == e.getKey() && e.getValue() != null).findFirst().isPresent()).forEach(e -> e.getValue().allOnUpdate.remove(c));
        }
        if (addNew) {
            onUpdateForNew.remove(c);
        }
    }

    /**
     * when a Client connects to the Server
     *
     * @param addNew if all new Clients should execute this
     * @param ids    IDs of the OldMain which should execute it (not very useful)
     */
    public void setOnConnect(boolean addNew, Consumer<ClientHandler> c, int... ids) {
        if (addNew) {
            onConnectForNew.add(c);
        }
        if (ids.length == 0) {
            clients.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> e.getValue().allOnConnects.add(c));
        } else {
            clients.entrySet().stream().filter(e -> Arrays.stream(ids).filter(val -> val == e.getKey() && e.getValue() != null).findFirst().isPresent()).forEach(e -> e.getValue().allOnConnects.add(c));
        }
    }

    /**
     * @return all IDs from the current connected clients
     */
    public ArrayList<Integer> getAllIDs() {
        ArrayList<Integer> i = new ArrayList<>();
        clients.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> i.add(e.getKey()));
        return i;
    }

    /**
     * a Multicast or Unicast
     *
     * @param msg the message that will be send
     * @param ids the id of the clients that will receive it
     */
    public void send(Object msg, int... ids) {
        clients.entrySet().stream().filter(e -> Arrays.stream(ids).filter(val -> val == e.getKey() && e.getValue() != null).findFirst().isPresent()).forEach(e -> e.getValue().send(msg));
    }

    /**
     * sends a message to all Clients
     *
     * @param msg the message that will be send
     */
    public void sendAll(Object msg) {
        clients.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> e.getValue().send(msg));
    }

    /**
     * adds a new OldMain to the Server
     *
     * @param waitReturnTillConnected if the method should wait till a client is there to return to the "normal" code
     */
    public void addAndChooseClient(boolean waitReturnTillConnected) throws IOException {
        if (maxClients < 0 || clientCount < maxClients) addClient(waitReturnTillConnected, clientCount++);
        int id = getFreeID();
        if (id != -1) addClient(waitReturnTillConnected, id);
    }

    /**
     * {@link Server#addAndChooseClient}
     */
    private void addClient(boolean waitReturnTillConnected, int id) throws IOException {
        ClientHandler c = new ClientHandler(id, waitReturnTillConnected);
        c.allOnMessage.addAll(onMessageForNew);
        c.allOnConnects.addAll(onConnectForNew);
        c.allOnUpdate.addAll(onUpdateForNew);
        clients.put(id, c);
        c.allOnConnects.forEach(e -> e.accept(c));
    }

    /**
     * disconnects all Clients with the ids from "ids"
     *
     * @param ids the ids of the clients which will be disconnected
     */
    public void disconnect(int... ids) {
        clients.entrySet().stream().filter(e -> Arrays.stream(ids).filter(val -> val == e.getKey() && e.getValue() != null).findFirst().isPresent()).forEach(e -> e.getValue().disconnect());
    }

    /**
     * disconnects all Clients from the Server
     */
    public void disconnectAll() {
        clients.entrySet().stream().filter(e -> e.getValue() != null).forEach(e -> e.getValue().disconnect());
    }

    /**
     * disconnects all Clients from the Server and resets the clientStorage
     */
    public void clear() {
        disconnectAll();
        clientCount = 0;
        clients = new HashMap<>();
    }

    /**
     * executes all updates for all clients
     */
    public void startUpdates() {
        Thread th = new Thread(() -> {
            while (true) {
                getClients().values().stream().filter(Objects::nonNull).forEach(ClientHandler::update);
                try {
                    Thread.sleep(30);
                } catch (InterruptedException ignored) {
                }
            }
        });
        th.start();
    }

    /**
     * @author Zwickelstorfer Felix
     * to handle a single OldMain
     * @see Thread
     */
    public class ClientHandler extends Thread {

        /**
         * the id of the client
         */
        private final int id;

        private final ArrayList<Consumer<ClientHandler>> allOnUpdate = new ArrayList<>();

        /**
         * saves all Consumer which will be accepted if the OldMain connects to the Server
         */
        private final ArrayList<Consumer<ClientHandler>> allOnConnects = new ArrayList<>();

        /**
         * saves all Consumer which will be accepted if the OldMain receives a message
         */
        private final ArrayList<BiConsumer<ClientHandler, Object>> allOnMessage = new ArrayList<>();

        /**
         * Crypto encryption for passwords
         */
        private final Crypto crypto;

        /**
         * counts the number of updates which where send
         */
        private long updateCount = 0;

        public List<Keys> getKeysPressed() {
            return keysPressed;
        }

        /**
         * which keys the client currently presses
         */
        private final List<Keys> keysPressed = new ArrayList<>();

        public long getUpdateCount() {
            return updateCount;
        }

        /**
         * the socket with whom the server is connected to the client
         */
        private Socket socket;

        /**
         * to send data to the client
         */
        private ObjectOutputStream out;

        /**
         * to receive data from the client
         */
        private ObjectInputStream in;

        /**
         * if the client has disconnected from the server
         */
        private boolean isDisconnected = false;

        /**
         * The username of the current logged in user
         */
        private String username = null;

        /**
         * The playerdata for the current clientHandler
         */
        private Player player;

        public ClientHandler(int id, boolean waitTillConnected) throws IOException {
            this.id = id;
            if (waitTillConnected) {
                synchronized (server) {
                    this.socket = server.accept();
                }
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
            }
            crypto = new Crypto();
            start();
        }

        /**
         * executes all updates
         */
        private void update() {
            updateCount++;
            allOnUpdate.forEach(e -> e.accept(this));
        }

        public Player getPlayer() {
            return player;
        }

        public void setPlayer(Player player) {
            this.player = player;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            ResultSet rs = Database.get("select * from User where name='" + username + "' or email='" + username + "';");
            try {
                assert rs != null;
                if (rs.first())
                    this.username = (String) rs.getObject("name");
            } catch (SQLException ignored) {
            }
        }

        public long getId() {
            return id;
        }

        @Override
        public void run() {
            if (socket == null) {
                try {
                    synchronized (server) {
                        socket = server.accept();
                    }
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());
                } catch (IOException ignored) {
                    if (!isDisconnected) System.out.println("Connection lost: " + id);
                    clients.put(id, null);
                }
            }
            try {
                while (!isDisconnected) {
                    Object s = in.readObject();
                    allOnMessage.forEach(e -> e.accept(this, s));
                }
            } catch (IOException | ClassNotFoundException e) {
                if (!isDisconnected) System.out.println("Connection lost: " + id);
                clients.get(id).setPlayer(null);
                clients.put(id, null);
            }
        }

        /**
         * sends a message to the client
         *
         * @param msg the message that will be send
         */
        public void send(Object msg) {
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException ignored) {
                if (!isDisconnected) System.out.println("Connection lost: " + id);
                clients.put(id, null);
            } catch (NullPointerException ignored) {
            }
        }

        public Crypto getCrypto() {
            return crypto;
        }

        /**
         * disconnects OldMain from the Server
         */
        private void disconnect() {
            try {
                out.close();
                in.close();
                socket.close();
                System.out.println("Connection closed: " + id);
                isDisconnected = true;
                clients.put(id, null);
            } catch (Exception ignored) {
            }
        }
    }
}
