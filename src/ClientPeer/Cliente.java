package ClientPeer;

import java.io.*;
import java.math.BigInteger;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;

public class Cliente {
    private static boolean verbose = true;
    private static Process process = new Process(verbose);
    private static final String shared_folder = "download/";
    private static final int part_size = 16 * 1024 * 1024;
    private static final int port_dst_UDP = 6667;
    private static final int port_dst_TCP = 6668;
    private static final int packet_length = 32 * 1024;

    private static Download send_UDP(InetAddress ip, int port, byte[] data) {
        if (verbose) System.out.println(MessageFormat.format("C: Enviando Pacote UDP <--> {0}:{1}",
                ip.getHostAddress(), String.valueOf(port)));

        /* envia o pacote */
        try {
            DatagramSocket dgramSocket = new DatagramSocket();
            DatagramPacket request = new DatagramPacket(data, data.length, ip, port);
            dgramSocket.send(request);

            byte[] buffer = new byte[1500];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            /* aguarda datagramas */
            dgramSocket.receive(reply);
            Arquivo arquivo = process.download(reply.getData());
            ByteArrayInputStream bais = new ByteArrayInputStream(arquivo.getData());

            int len = bais.read();
            if (len > 0) {// parte existente
                int cp = new BigInteger(bais.readNBytes(bais.read())).intValue();
                if (verbose) {
                    System.out.println("C: Adicionado a lista de downloads");
                }
                return new Download(ip, cp, arquivo.getFile_name(), arquivo.getPart());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void baixar_parte(Download download) {
        new Thread(() -> {

            /* conecta com o servidor */
            Socket socket = null;
            try {
                socket = new Socket(download.getAddress(), download.getPort());
                if (verbose) System.out.println(MessageFormat.format("C: Criando Thread <--> {0}",
                        socket.getRemoteSocketAddress()));


                DataInputStream in = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                int i = 0;
                Arquivos arquivos = new Arquivos();

                do {

                    byte[] request = process.download(download.getFile_name(), download.getPart(), 0, 100);
                    out.write(request);
                    out.flush();


                    byte[] data = new byte[packet_length + 256];
                    int len = in.read(data, 0, data.length);

                    Arquivo arquivo = process.download(Arrays.copyOfRange(data, 0, len));

                    try {
                        arquivos.escreve_no_arquivo(shared_folder + arquivo.getFile_name(),
                                arquivo.getData(), arquivo.getPart() + i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i += packet_length;
                    if (arquivo.getPart() + i > download.getFile_size()) {
                        break;
                    }
                } while (i < part_size);

                if (verbose) System.out.println(MessageFormat.format("C: Finalizando Thread UDP<--> {0}",
                        socket.getRemoteSocketAddress()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    private static void baixar_arquivo(FileObject file) {
        new Thread(() -> {
            if (verbose)
                System.out.println(MessageFormat.format("C: Preparando para baixar arquivo {0}", file.getName()));
            for (int i = 0; i < file.getPartCount(); i++) {
                List<Peer> peers = file.getPeers();
                Collections.shuffle(peers);
                for (Peer address : peers) {
                    System.out.println(address.ip);
                    System.out.println(address.port);
                    byte[] data = process.download(file.getName(), i * part_size, 0, 100);

                    Download download = send_UDP(address.ip, address.port, data);
                    if (download != null) {
                        download.setFile_size(file.getSize());
                        baixar_parte(download);
                        break;
                    }
                }
            }
            if (verbose)
                System.out.println(MessageFormat.format("C: Finalizado {0}", file.getName()));

        }).start();
    }

    public static byte[] send(byte[] data, InetAddress ip, int port) {
        byte[] buffer = new byte[packet_length + 256];
        Socket socket = null;
        int len = 0;
        try {
            socket = new Socket(ip, port);
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.write(data);
            out.flush();

            len = in.read(buffer, 0, buffer.length);
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (len == 0) return new byte[0];
        return Arrays.copyOfRange(buffer, 0, len);
    }

    public static void help() {
        System.out.println("----------------------");
        System.out.println("Lista de Comandos");
        System.out.println("add [nome do arquivo]: cria um arquivo compartilhavel");
        System.out.println("ex: add test.txt");
        System.out.println("download [nome do arquivo]: inicia o download do arquivo");
        System.out.println("ex: download test.txt");
        System.out.println("search [query]: busca arquivos aprtir de uma string");
        System.out.println("ex: search test");
        System.out.println("help: exibe a lista de comandos");
        System.out.println("----------------------");
        System.out.print("$>");
    }

    public static void main(String[] args) throws UnknownHostException {

        // ClientPeer.ClientAwait
        ClientAwait p2p = new ClientAwait(port_dst_TCP, port_dst_UDP, verbose);
        p2p.listen_TCP();
        p2p.listen_UDP();

        // Client
        String raw_message;

        // ClientPeer.ClientAwait address
        InetAddress server_address = InetAddress.getByName("127.0.0.1");
        int server_port = 5555;
        List<String> download_available = new ArrayList<>();

        Scanner reader = new Scanner(System.in); // ler mensagens via teclado
        help();
        do {
            raw_message = reader.nextLine();
            String[] message = raw_message.split(" ");

            String file_name = raw_message.substring(message[0].length() + 1);
            Arquivo arquivo = Arquivos.info(shared_folder + file_name);
            System.out.println(arquivo.getFile_name());
            switch (message[0]) {
                case "help":
                    help();
                    break;
                case "add":
                    byte[] data = process.upload(shared_folder + file_name, port_dst_UDP);
                    byte[] reply = send(data, server_address, server_port);
                    if (verbose)
                        System.out.printf("C: A adição de %s ao tracker %s!!%n", file_name, reply[2] > 0 ? "sucesso" : "falha");
                    break;
                case "search":
                    data = process.search(file_name, 11);
                    reply = send(data, server_address, server_port);
                    List<String> list = process.search(reply);
                    if (verbose && list.size() > 0) {
                        download_available.addAll(list);
                        if (verbose)
                            System.out.printf("C: Lista de downloads possiveis atualizada!!%n");
                        for (String a : download_available) {
                            System.out.println(a);
                        }
                    } else
                        System.out.printf("C: Não foi encontrado arquivos!!%n");
                    break;
                case "download":
                    byte[] port = new BigInteger(String.valueOf(port_dst_UDP)).toByteArray();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    baos.write(port.length);
                    for (byte b : port) {
                        baos.write(b);
                    }

                    data = process.search(file_name, 12, baos.toByteArray());
                    reply = send(data, server_address, server_port);

                    try {
                        FileObject prepare = process.downloadCases(reply);
                        int len = Arquivos.exist(shared_folder + prepare.getName(), 0);
                        if (len > 0) {
                            Arquivo arquivo1 = Arquivos.info(shared_folder + prepare.getName());
                            if (arquivo1.size() == prepare.getSize()) {
                                System.out.printf("Arquivo %s ja está na pasta compartilhada!!%n", arquivo1.getFile_name());
                                break;
                            }
                        }
                        baixar_arquivo(prepare);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + message[0]);
            }
        } while (!raw_message.contains("exit"));
        p2p.close();
    }

}
