package Tracker;

import java.io.Serializable;
import java.net.InetAddress;

public class Peer implements Serializable {
    InetAddress ip;
    int port;

    Peer(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
    }
}