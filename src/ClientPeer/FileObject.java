package ClientPeer;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;



public class FileObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private long size;
    private int partCount;
    private byte[] checksum;
    private List<Peer> peers;

    FileObject(String name, long size, int partCount, byte[] checksum) {
        this.name = name;
        this.size = size;
        this.partCount = partCount;
        this.checksum = checksum;
        this.peers = new LinkedList<>();
    }

    FileObject(String name, long size, int partCount, List<Peer> peers) {
        this.name = name;
        this.size = size;
        this.partCount = partCount;
        this.peers = peers;
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

    public List<Peer> getPeers() {
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

    public void setPeers(List<Peer> peers) {
        this.peers.addAll(peers);
    }

    public boolean addPeer(InetAddress inetAddress, int udp_port) {
        return this.peers.add(new Peer(inetAddress, udp_port));
    }

    public long getSize() {
        return size;
    }
}