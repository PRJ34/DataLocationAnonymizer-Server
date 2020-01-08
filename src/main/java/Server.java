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
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private SelectionKey key;

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

    public List<Client> ecouterClients(final int tempsEcoute)
        throws IOException{
        List<Client> clients = new ArrayList<>();
        List<SocketChannel> socketsChannelClient = new ArrayList<>();
        final long debutTimer = System.currentTimeMillis();

        while(System.currentTimeMillis() < debutTimer + tempsEcoute * 1000) {
            System.out.println("before");
            selector.select();
            System.out.println("after");
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    System.out.println("after");
                    SelectionKey key = iter.next();

                    if (key.isAcceptable()) {
                        SocketChannel client = serverSocketChannel.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_READ);
                        System.out.println(selector.selectNow());
                    }
                    iter.remove();
                }
        }
        return clients;
    }
}
