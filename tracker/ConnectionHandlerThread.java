import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConnectionHandlerThread extends Thread {
    Socket communicationSocket;
    InputStream in;
    OutputStream out;
    List<FileObject> fileList;

    ConnectionHandlerThread(Socket communicationSocket, List<FileObject> fileList) {
        this.communicationSocket = communicationSocket;
        try {
            this.in = communicationSocket.getInputStream();
            this.out = communicationSocket.getOutputStream();
        } catch (Exception e) {
            //TODO: handle exception
        }
        this.fileList = fileList;
    }

    private void handleAddFile(byte[] b, int readCount) throws IOException {
        byte fileNameSize = b[2];
        String fileName = new String(b, 3, fileNameSize);

        int fileSize = ByteBuffer.wrap(b, 3 + fileNameSize, 4).getInt();

        int partCount = ByteBuffer.wrap(b, 3 + fileNameSize + 4, 3).getInt();

        byte[] checksum = Arrays.copyOfRange(b, 3 + fileNameSize + 3 + 16, readCount);

        this.fileList.add(new FileObject(fileName, fileSize, partCount, checksum));

        byte[] response = new byte[3];

        response[0] = 1;
        response[1] = 10;
        response[2] = 0;

        this.out.write(response);
    }

    private void handleListFiles(byte[] b, int readCount) throws IOException {
        byte fileNameSize = b[2];
        String fileName = new String(b, 3, fileNameSize);
        
        List<FileObject> resultList = new ArrayList<FileObject>();

        for (FileObject file : this.fileList) {
            if (file.getName().contains(fileName)) {
                resultList.add(file);
            }
        }

        byte[] response = new byte[4096];

        response[0] = 1;
        response[1] = 11;

        byte[] fileNameBytes;
        for (FileObject file : resultList) {
            fileNameBytes = file.getName().getBytes();
            response[0] = (byte)fileNameBytes.length;
            System.arraycopy(fileNameBytes, 0, response, 2, fileNameBytes.length);
            System.arraycopy(file.getChecksum(), 0, response, 2 + fileNameBytes.length, 16);
            this.out.write(response, 0, 2 + fileNameBytes.length + 16);
        }
    }

    private void handleGetFile(byte[] b, int readCount) {
        byte[] checksum = Arrays.copyOfRange(b, 2, 16); 
        
        byte[] response = new byte[4096];
        response[0] = 1;
        response[1] = 12;
        
        for (FileObject file : this.fileList) {
            if (Arrays.equals(file.getChecksum(), checksum)) {
                
            }
        }


    }

    private void handleRequest(byte[] b, int readCount) throws IOException {
        byte opCode = b[1];

        switch (opCode) {
            case 10:
                this.handleAddFile(b, readCount);
                break;

            case 11:
                this.handleListFiles(b, readCount);
                break;

            case 12:
                this.handleGetFile(b, readCount);
                break;
        }
    }

    @Override
    public void run() {
        try {
            byte[] b = new byte[4096];
            int readCount;
            while (true) {
                readCount = in.read(b);
                if (readCount > 0) {
                    this.handleRequest(b, readCount);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getClass());
        }
    }
}