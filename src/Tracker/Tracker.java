package Tracker;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Tracker {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5555);

            List<FileObject> fileList = new ArrayList<>();

            while (true) {
                System.out.println("Waiting Connections");

                System.out.println("Client Connected");
                new ConnectionHandlerThread(serverSocket.accept(), fileList).start();
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}