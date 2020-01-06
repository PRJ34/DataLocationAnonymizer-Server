public class main {
    public static void main(String[] args){
        int tempsEcoute = Integer.valueOf(args[0]); // Seconde
        int portEcoute = Integer.valueOf(args[1]);

        System.out.println("Lancement du serveur sur le port " + portEcoute + ", temps ecoute :" + tempsEcoute);
        Server srv = new Server(portEcoute);
        srv.ecouterClients(tempsEcoute);
    }


}
