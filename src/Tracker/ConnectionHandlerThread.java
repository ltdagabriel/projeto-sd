package Tracker;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConnectionHandlerThread extends Thread {
    Socket communicationSocket;
    InputStream in;
    OutputStream out;
    List<FileObject> fileList;
    private static final int packet_length = 32 * 1024;
    private static final int part_size = 16 * 1024 * 1024;


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

    private void handleAddFile(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        bais.read();
        bais.read();
        StringBuilder name = new StringBuilder();
        int n = bais.read();
        for (byte c : bais.readNBytes(n)) name.append((char)c);

        int tamanho_arquivo = new BigInteger(bais.readNBytes(bais.read())).intValue();
        int qtd_parts = new BigInteger(bais.readNBytes(bais.read())).intValue();
        byte[] md5 = bais.readNBytes(bais.read());

        int udp_port = new BigInteger(bais.readNBytes(bais.read())).intValue();
        FileObject fileObject = new FileObject(name.toString(), tamanho_arquivo, qtd_parts, md5);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(1);
        baos.write(10);
        fileObject.addPeer(communicationSocket.getInetAddress(), udp_port);
        this.fileList.add(fileObject);
        baos.write(1);

        this.out.write(baos.toByteArray());
    }

    private void handleListFiles(byte[] b) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(b);
        bais.read();
        bais.read();
        StringBuilder regex = new StringBuilder();
        for (byte c : bais.readNBytes(bais.read())) regex.append((char)c);

        String query = regex.toString();
        List<FileObject> resultList = new ArrayList<>();

        for (FileObject file : this.fileList) if (file.getName().contains(query)) resultList.add(file);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(1);
        baos.write(11);

        for (FileObject file : resultList) {
            baos.write(file.getName().length());
            for (char c : file.getName().toCharArray()) {
                baos.write(c);
            }
        }
        this.out.write(baos.toByteArray());
    }

    private void handleGetFile(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        bais.read();
        bais.read();
        StringBuilder regex = new StringBuilder();
        for (byte c : bais.readNBytes(bais.read())) regex.append((char)c);

        String query = regex.toString();
        int udp_port = new BigInteger(bais.readNBytes(bais.read())).intValue();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(1);
        baos.write(12);


        for (FileObject file : this.fileList) {
            if (file.getName().contains(query)) {
                file.addPeer(this.communicationSocket.getInetAddress(), udp_port);
                System.out.println(file.getName());
                baos.write(file.getName().length());
                for (char c : file.getName().toCharArray()) {
                    baos.write(c);
                }

                byte[] tamanho_arquivo = new BigInteger(String.valueOf(file.getSize())).toByteArray();
                baos.write(tamanho_arquivo.length);
                for (byte b : tamanho_arquivo) {
                    baos.write(b);
                }

                byte[] qtd_parts = new BigInteger(String.valueOf(file.getPartCount())).toByteArray();
                baos.write(qtd_parts.length);
                for (byte b : qtd_parts) {
                    baos.write(b);
                }

                for (Peer p : file.getPeers()) {
                    byte[] ip = p.ip.getAddress();

                    baos.write(ip.length);
                    for (byte c : ip) {
                        baos.write(c);
                    }

                    byte[] port = new BigInteger(String.valueOf(p.port)).toByteArray();
                    baos.write(port.length);
                    for (byte b : port) {
                        baos.write(b);
                    }
                }

                break;
            }
        }
        out.write(baos.toByteArray());
    }

    private void handleRequest(byte[] b) throws IOException {
        int opCode = b[1];

        switch (opCode) {
            case 10:
                this.handleAddFile(b);
                break;

            case 11:
                this.handleListFiles(b);
                break;

            case 12:
                this.handleGetFile(b);
                break;
        }
    }

    @Override
    public void run() {
        try {
            byte[] buffer = new byte[packet_length + 256];

            int readCount;
            while (true) {
                readCount = in.read(buffer, 0, buffer.length);
                if (readCount > 0) {
                    this.handleRequest(Arrays.copyOfRange(buffer, 0, readCount));
                }
            }
        } catch (Exception e) {
            System.out.println(e.getClass());
        }

        System.out.println("Client Closed");
    }
}