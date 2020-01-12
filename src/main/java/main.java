import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.Scanner;

public class main {
    public static void main(String[] args){
        int tempsEcoute = Integer.valueOf(args[0]); // Seconde
        int portEcoute = Integer.valueOf(args[1]);

        System.out.println("Lancement du serveur sur le port " + portEcoute + ", temps ecoute :" + tempsEcoute);
        Server srv = new Server(portEcoute);
        Scanner keyboard = new Scanner(System.in);
        //2020:01:01:12:00:00 2020:01:01:23:00:00
        System.out.println("enter a timeframe : yyyy:MM:dd:HH:mm:ss yyyy:MM:dd:HH:mm:ss");
        String time1 = keyboard.next();
        String time2 = keyboard.next();
        try {
            System.out.println("LISTEN");
            srv.ecouterClients(tempsEcoute);
            System.out.println("SEND BEGIN");
            srv.sendBeginSignal(srv.parseStringDatesToInterval(time1, time2));
            System.out.println("WAIT CLIENTS");
            srv.waitClients();
            System.out.println("SEND TABLE");
            srv.sendClients();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


}
