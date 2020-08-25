import java.nio.ByteBuffer;

public class ByteBufferTest {
    public static void main(String[] args) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.put("aaa".getBytes());
        byte[] b = new byte[2];
        buffer.get(b,1,2);
        System.out.println(new String(b));
    }
}
