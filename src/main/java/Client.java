import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Client {
    private int id;
    private byte[] clePublic;
    private String adresse;
    private int port;

    public Client(int id, byte[] clePublic, String adresse, int port) {
        this.id = id;
        this.clePublic = clePublic;
        this.adresse = adresse;
        this.port = port;
    }

    public ByteBuffer toByteBuffer(){
        String payload_string = this.id+":"+this.port+":";
        //System.out.println(this.toHexString(this.kPair.getPublic().getEncoded()));
        //System.out.println(this.kPair.getPublic().getEncoded().toString());
        //System.out.println(StandardCharsets.UTF_8.decode(StandardCharsets.UTF_8.encode(this.kPair.getPublic().getEncoded().toString())));
        ByteBuffer string_buffer = StandardCharsets.UTF_8.encode(payload_string);
        ByteBuffer byte_buffer = ByteBuffer.wrap(this.clePublic);
        ByteBuffer payload_buffer = ByteBuffer.allocate(4+string_buffer.capacity()+byte_buffer.capacity()).putInt(string_buffer.limit()).put(string_buffer).put(byte_buffer);
        return payload_buffer;
    }

    @Override
    public String toString() {
        return this.id + ":" + this.port + ":" + this.clePublic;
    }
}
