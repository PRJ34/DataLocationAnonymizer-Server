import java.io.IOException;
import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Server {
    private ServerSocket serverSocket;

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Client> ecouterClients(final int tempsEcoute) {
        List<Client> clients = new ArrayList<>();
        List<Socket> socketsClient = new ArrayList<>();
        final long debutTimer = System.currentTimeMillis();
        Selector selector = null;
        ServerSocketChannel serverSocketChannel = null;
        SelectionKey key = null;
        try {
            InetAddress host = InetAddress.getByName("localhost");
            selector = Selector.open();
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(host, 1234));
            serverSocketChannel.register(selector, SelectionKey.
                    OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(System.currentTimeMillis() < debutTimer + tempsEcoute * 1000) {
            try {
                if (selector.select() <= 0)
                    continue;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                key = (SelectionKey) iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = null;
                    try {
                        sc = serverSocketChannel.accept();
                        sc.configureBlocking(false);
                        sc.register(selector, SelectionKey.
                                OP_READ);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return clients;
    }
}
