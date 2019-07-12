package ClientPeer;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.text.MessageFormat;


class ClientAwait {
    private ServerSocket socket_TCP;
    private DatagramSocket socket_UDP;
    private Process process;
    private boolean verbose;

    private static final String shared_folder = "download2/";
    private static final int packet_length = 32*1024;
    private static final int part_size = 16 * 1024 * 1024;

    ClientAwait(int port_tcp, int port_udp, boolean verbose) {
        try {
            this.socket_UDP = new DatagramSocket(port_udp);
            this.verbose = verbose;
            this.socket_TCP = new ServerSocket(port_tcp);

            if (verbose) System.out.println(MessageFormat.format("S: Servidor TCP inicializado em {0}:{1}",
                    this.socket_TCP.getInetAddress(), String.valueOf(this.socket_TCP.getLocalPort())));


            if (verbose) System.out.println(MessageFormat.format("S: Servidor UDP inicializado em {0}:{1}",
                    this.socket_UDP.getLocalAddress(), String.valueOf(this.socket_UDP.getLocalPort())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        process = new Process(verbose);
    }

    void listen_TCP() {

        // TCP Listen
        new Thread(() -> {
            while (true) {
                try {
                    // aguardando requisições
                    accept_request_TCP(socket_TCP.accept());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    void listen_UDP() {
        // UDP Listen

        new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1000]; // cria um buffer para receber requisições

                    /* cria um pacote vazio */
                    DatagramPacket dgramPacket = new DatagramPacket(buffer, buffer.length);
                    socket_UDP.receive(dgramPacket);  // aguarda a chegada de datagramas

                    // aguardando requisições
                    accept_request_UDP(dgramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();

    }

    private void accept_request_UDP(DatagramPacket datagramPacket) {
        if (verbose) System.out.println(MessageFormat.format("S: Thread aceita: Pacote UDP recebido <--> {0}:{1}",
                datagramPacket.getAddress(), String.valueOf(datagramPacket.getPort())));


        Thread t = new Thread(() -> {
            Arquivo arquivo = process.download(datagramPacket.getData());

            int len = Arquivos.exist(shared_folder + arquivo.getFile_name(), arquivo.getPart());
            // add confirmação de existencia e porta para download
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(len);
            byte[] port = new BigInteger(String.valueOf(socket_TCP.getLocalPort())).toByteArray();
            baos.write(port.length);
            for (byte b : port) {
                baos.write(b);
            }

            byte[] data = process.download(arquivo.getFile_name(), arquivo.getPart(), baos.toByteArray(), 1, 100);
            DatagramPacket reply = new DatagramPacket(data, data.length, datagramPacket.getAddress(), datagramPacket.getPort()); // cria um pacote com os dados

            try {
                socket_UDP.send(reply); // envia o pacote
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        t.start();

        try {
            t.join();
            if (verbose) System.out.println(MessageFormat.format("S: Thread finalizada: pacote UDP <--> {0}:{1}",
                    datagramPacket.getAddress(), String.valueOf(datagramPacket.getPort())));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void accept_request_TCP(Socket socket) {
        if (verbose) System.out.println(MessageFormat.format("S: Criando Thread <--> {0}",
                socket.getRemoteSocketAddress()));

        //
        Thread t = new Thread(() -> {

            try {
                DataInputStream receive = new DataInputStream(socket.getInputStream());
                DataOutputStream send = new DataOutputStream(socket.getOutputStream());
                int i = 0;
                Arquivos arquivos = new Arquivos();
                boolean continua = true;
                do {
                    byte[] buffer = new byte[packet_length+256];
                    receive.read(buffer, 0, buffer.length);
                    Arquivo arquivo = process.download(buffer);
                    // processa e responde a mensagem

                    try {
                        send.write(process.download(arquivo.getFile_name(), arquivo.getPart(),
                                arquivos.ler_do_arquivo(shared_folder + arquivo.getFile_name(), arquivo.getPart() + i, packet_length),
                                1, 101));
                        send.flush();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Arquivo arquivo1 = Arquivos.info(shared_folder + arquivo.getFile_name());
                    i += packet_length;
                    if (i + arquivo.getPart() >= arquivo1.size()) {
                        continua = false;
                    }
                } while (continua && i < part_size);
                receive.close();
                send.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();

        try {
            t.join();
            if (verbose) System.out.println(MessageFormat.format("S: Finalizando Thread <--> {0}",
                    socket.getRemoteSocketAddress()));
            socket.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

    }

    public void close() {
        try {
            socket_TCP.close();
            socket_UDP.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Download {
    private InetAddress address;
    private int port;
    private final String file_name;
    private int part;
    private double file_size;

    Download(InetAddress address, int port, String file_name, int part) {

        this.address = address;
        this.port = port;
        this.file_name = file_name;
        this.part = part;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public String getFile_name() {
        return file_name;
    }

    public int getPart() {
        return part;
    }


    public double getFile_size() {
        return file_size;
    }

    public void setFile_size(double file_size) {
        this.file_size = file_size;
    }
}