import java.io.Serializable;
import java.net.InetAddress;
import java.util.List;

public class FileObject implements Serializable {
    private static final long serialVersionUID = 1L;
    String name;
    int size;
    int partCount;
    byte[] checksum;
    List<InetAddress> peers;

    FileObject(String name, int size, int partCount, byte[] checksum) {
        this.name = name;
        this.size = size;
        this.partCount = partCount;
        this.checksum = checksum;
    }

    public byte[] getChecksum() {
        return checksum;
    }
    public String getName() {
        return name;
    }
    public int getPartCount() {
        return partCount;
    }
    public List getPeers() {
        return peers;
    }
    public void setChecksum(byte[] checksum) {
        this.checksum = checksum;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPartCount(int partCount) {
        this.partCount = partCount;
    }
    public void setPeers(List<InetAddress> peers) {
        this.peers = peers;
    }
}