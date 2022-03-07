package ClientStuff;


import Calcs.Crypto;
import Envir.World;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Zwickelstorfer Felix
 * a basic Client for a Server
 * @version 1.1
 */
public class Client extends Thread {

    /**
     * the port which the server will communicate about
     */
    private final int port;

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public List<Player> getPlayers() {
        return playerPos;
    }

    /**
     * Pos of all Players which can be seen from the client
     */
    private final List<Player> playerPos = new ArrayList<>();

    /**
     * the ip of the server
     */
    private final String ip;

    /**
     * saves all Consumer which will be accepted if the OldMain connects to the Server
     */
    ArrayList<Consumer<Client>> allOnConnects = new ArrayList<>();

    /**
     * saves all Consumer which will be accepted if the OldMain receives a message
     */
    final ArrayList<BiConsumer<Client, Object>> allOnMessage = new ArrayList<>();

    /**
     * the socket with whom the server is connected to the server
     */
    private Socket socket;

    /**
     * to send data to the server
     */
    private ObjectOutputStream out;

    /**
     * to receive data from the server
     */
    private ObjectInputStream in;

    /**
     * the world in which the player currently is
     */
    private World world;

    /**
     * if the client has disconnected from the server
     */
    private boolean isDisconnected = false;

    public Client(int port, String ip, boolean waitTillConnected, Consumer<Client> onConnect) {
        this(port, ip, waitTillConnected, onConnect, null);
    }

    public Client(int port, String ip, boolean waitTillConnected, BiConsumer<Client, Object> onMessage) {
        this(port, ip, waitTillConnected, null, onMessage);
    }

    public Client(int port, String ip, boolean waitTillConnected) {
        this(port, ip, waitTillConnected, null, null);
    }

    public Client(int port, String ip, boolean waitTillConnected, Consumer<Client> onConnect, BiConsumer<Client, Object> onMessage) {
        if (onConnect != null) onConnect(onConnect);
        if (onMessage != null) onMessage(onMessage);
        this.port = port;
        this.ip = ip;
        if (waitTillConnected) {
            try {
                this.socket = new Socket(ip, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                allOnConnects.forEach(e -> e.accept(this));
            } catch (Exception ignored) {
                System.out.println("Couldn't connect to Server");
                return;
            }
        }
        start();
    }

    @Override
    public void run() {
        if (socket == null) {
            try {
                this.socket = new Socket(ip, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                allOnConnects.forEach(e -> e.accept(this));
            } catch (IOException ignored) {
                System.out.println("Couldn't connect to Server");
                return;
            }
        }
        try {
            while (!isDisconnected) {
                Object s = in.readObject();
                synchronized (allOnMessage) {
                    allOnMessage.forEach(e -> e.accept(this, s));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!isDisconnected) System.out.println("Connection from Server lost");
        }
    }

    /**
     * what should happen when a message is sent
     */
    public void onMessage(BiConsumer<Client, Object> s) {
        synchronized (allOnMessage){
            allOnMessage.add(s);
        }
    }

    /**
     * when a OldMain connects to the Server
     */
    public void onConnect(Consumer<Client> s) {
        allOnConnects.add(s);
    }

    /**
     * sends a message to the server
     *
     * @param msg the message that will be send
     */
    public void send(Object msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException ignored) {
            if (!isDisconnected) System.out.println("Connection from Server lost");
        }
    }

    /**
     * disconnects from the Server
     */
    public void disconnect() {
        try {
            isDisconnected = true;
            out.close();
            in.close();
            socket.close();
            System.out.println("Connection to Server closed");
        } catch (Exception ignored) {
        }
    }

    /**
     * Crypto encryption for passwords
     */
    private Crypto crypto;

    /**
     * The current loginName
     */
    private String name = null;

    /**
     * errorMessages to show while Login
     */
    private Text errorMsg = new Text();


    public void setErrorTxt(Text errorMsg) {
        this.errorMsg = errorMsg;
    }

    public void setUsername(String name) {
        this.name = name;
    }

    public String getUsername() {
        return name;
    }

    public Text getErrorTxt() {
        return errorMsg;
    }

    public void setCrypto(Crypto crypto) {
        this.crypto = crypto;
    }

    public Crypto getCrypto() {
        return crypto;
    }
}
