import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private SelectionKey key;
    private List<SocketChannel> socketsChannelClient;
    Map<SocketChannel, Client>  socketClients;

    public Server(int port) {
        selector = null;
        serverSocketChannel = null;
        key = null;
        try {
            InetAddress host = InetAddress.getByName("localhost");
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(host, port));
            serverSocketChannel.register(selector, SelectionKey.
                    OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<SocketChannel> ecouterClients(final int tempsEcoute)
        throws IOException{
        this.socketsChannelClient = new ArrayList<>();
        selector.select();
        final long debutTimer = System.currentTimeMillis();
        while(System.currentTimeMillis() < debutTimer + tempsEcoute * 1000) {
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        this.socketsChannelClient.add(client);
                        System.out.println(socketsChannelClient.size());
                    }
                    iter.remove();
                }
                selector.select(1);
        }
        return this.socketsChannelClient;
    }

    // Nécessite l'execution de "ecouterClients" pour fonctionner
    public void sendBeginSignal() {
        String beginSignal = "ok";

        for (SocketChannel s : this.socketsChannelClient) {
            sendString(s, beginSignal);
        }
    }

    public void waitClients() throws IOException {
        socketClients = new HashMap<>();
        ByteBuffer buffer = ByteBuffer.allocate(250);
        selector.select();
        int i = 0;
        while(true) {
            if(i == 3)
                break;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    client.read(buffer);
                    buffer.flip();
                    CharBuffer s = StandardCharsets.UTF_8.decode(buffer);
                    buffer.clear();
                    System.out.println(s.toString());
                    String[] strSplit = s.toString().split(":");
                    Client c = new Client(Integer.valueOf(strSplit[0]), strSplit[2], "localhost", Integer.valueOf(strSplit[1]));
                    socketClients.put(client, c);
                    i++;
                }
                iter.remove();
            }
            selector.select(100);
        }
    }

    private String buildClientsData() {
        String allClientsData = "";

        for (SocketChannel s : socketClients.keySet()) {
            Client c = socketClients.get(s);
            allClientsData += c.toString() + ";";
        }

        return allClientsData;
    }

    public void sendClients() {
        String clientsData = this.buildClientsData();

        for (SocketChannel s : socketClients.keySet()) {
            sendString(s, clientsData);
        }
    }

    public void sendString(SocketChannel socket, String text) {
        try {
            socket.write(StandardCharsets.UTF_8.encode(text));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
