import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Tracker {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(6666);
            Socket communicationSocket;

            List<FileObject> fileList = new ArrayList<FileObject>();

            while (true) {
                System.out.println("Waiting Connections");
                communicationSocket = serverSocket.accept();
                System.out.println("Client Connected");
                new ConnectionHandlerThread(communicationSocket, fileList);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}