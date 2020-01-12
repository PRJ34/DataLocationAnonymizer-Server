import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
                        System.out.println("Clients connectés : "+socketsChannelClient.size());
                    }
                    iter.remove();
                }
                selector.select(1);
        }
        return this.socketsChannelClient;
    }

    // Nécessite l'execution de "ecouterClients" pour fonctionner
    public void sendBeginSignal(String interval) {
        String beginSignal = "start:"+interval;

        for (SocketChannel s : this.socketsChannelClient) {
            sendString(s, beginSignal);
        }
    }

    public void waitClients() throws IOException {
        socketClients = new HashMap<>();
        ByteBuffer buffer = ByteBuffer.allocate(1000);
        selector.select();
        int i = 0;
        while(true) {
            if(i >= socketsChannelClient.size())
                break;
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    client.read(buffer);
                    buffer.flip();
                    int length = buffer.getInt();
                    byte[] string_buffer = new byte[length];
                    buffer.get(string_buffer);
                    ByteBuffer string_bb = ByteBuffer.wrap(string_buffer);
                    CharBuffer s = StandardCharsets.UTF_8.decode(string_bb);
                    byte[] byte_buffer = new byte[buffer.remaining()];
                    buffer.get(byte_buffer);
                    buffer.clear();
                    String[] strSplit = s.toString().split(":");
                    Client c = new Client(Integer.valueOf(strSplit[0]), byte_buffer, "localhost", Integer.valueOf(strSplit[1]));
                    socketClients.put(client, c);
                    i++;
                }
                iter.remove();
            }
            selector.select(100);
        }
    }

    public void waitHeatmaps() throws IOException {
        selector.select();

        int nbClients = 0;
        while (nbClients < socketsChannelClient.size()) { // TODO
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iter = selectedKeys.iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                if (key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();

                    ObjectInputStream ois =
                            new ObjectInputStream(client.socket().getInputStream());

                    try {
                        int[][] s = (int[][])ois.readObject();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

//                    List<ByteBuffer[]> buffers = new ArrayList<>();
//
//                    for (int i = 0; i < Heatmap.HEATMAP_ROW; i++) {
//                        ByteBuffer buffer[] = new ByteBuffer[Heatmap.HEATMAP_COL];
//                        for (int j = 0; j < Heatmap.HEATMAP_COL; j++) {
//                            buffer[j] = ByteBuffer.allocate(2048);
//                            client.read(buffer[j]);
//                            //buffer[j].flip();
//                            System.out.print(buffer[j].getDouble());
//                        }
//                        System.out.println();
//                        buffers.add(buffer);
//                    }
                    nbClients++;
                }
            }
        }
    }

    private ByteBuffer buildClientsData() {
        ArrayList<ByteBuffer> buffers = new ArrayList<>();
        int capacity = 0;
        for (SocketChannel s : socketClients.keySet()) {
            Client c = socketClients.get(s);
            ByteBuffer clientBB = c.toByteBuffer();
            buffers.add(clientBB);
            capacity += clientBB.capacity()+4;
        }
        ByteBuffer payload = ByteBuffer.allocate(capacity);
        for(ByteBuffer bb : buffers){
            bb.flip();
            payload.putInt(bb.limit()).put(bb);
        }
        return payload;
    }

    public void sendClients() throws IOException {
        ByteBuffer clientsData = this.buildClientsData();
        for (SocketChannel s : socketClients.keySet()) {
            clientsData.rewind();
            s.write(clientsData);
        }
    }

    public void sendString(SocketChannel socket, String text) {
        try {
            socket.write(StandardCharsets.UTF_8.encode(text));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String parseStringDatesToInterval(String stringDate1, String stringDate2) throws ParseException {
        SimpleDateFormat formater = new SimpleDateFormat("yyyy:MM:dd:HH:mm:ss");
        Date date1 = formater.parse(stringDate1);
        Date date2 = formater.parse(stringDate2);
        return date1.getTime()+" "+date2.getTime();
    }

    private static void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    private static String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len-1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }
}
