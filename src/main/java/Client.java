public class Client {
    private int id;
    private String clePublic;
    private String adresse;
    private int port;

    public Client(int id, String clePublic, String adresse, int port) {
        this.id = id;
        this.clePublic = clePublic;
        this.adresse = adresse;
        this.port = port;
    }

    @Override
    public String toString() {
        return this.id + ":" + this.port + ":" + this.clePublic;
    }
}
