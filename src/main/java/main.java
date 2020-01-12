import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class main {
    public static void main(String[] args){
        int tempsEcoute = Integer.valueOf(args[0]); // Seconde
        int portEcoute = Integer.valueOf(args[1]);

        System.out.println("Lancement du serveur sur le port " + portEcoute + ", temps ecoute :" + tempsEcoute);
        Server srv = new Server(portEcoute);
        try {
            System.out.println("LISTEN");
            srv.ecouterClients(tempsEcoute);
            System.out.println("SEND BEGIN");
            srv.sendBeginSignal();
            System.out.println("WAIT CLIENTS");
            srv.waitClients();
            System.out.println("SEND TABLE");
            srv.sendClients();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
