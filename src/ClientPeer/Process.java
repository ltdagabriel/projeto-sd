package ClientPeer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class Process {
    private boolean verbose;

    public Process(boolean verbose) {
        this.verbose = verbose;
    }

    byte[] search(String regex, int tipo) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0);
        baos.write(tipo);
        baos.write(regex.length());
        for (char c : regex.toCharArray()) {
            baos.write(c);
        }

        return baos.toByteArray();
    }

    byte[] search(String regex, int tipo, byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte b : search(regex, tipo))
            baos.write(b);

        for (byte b : data)
            baos.write(b);

        return baos.toByteArray();
    }

    List<String> search(byte[] data) {
        List<String> list = new ArrayList<>();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        bais.read();
        bais.read();
        try {
            do {
                StringBuilder name = new StringBuilder();
                for (byte c : bais.readNBytes(bais.read())) name.append((char)c);
                list.add(name.toString());
            } while (bais.available() > 0);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }


    byte[] upload(String file, int udp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Arquivo arquivo = Arquivos.info(file);
        baos.write(0);
        baos.write(10);
        baos.write(arquivo.getFile_name().length());
        for (char c : arquivo.getFile_name().toCharArray()) {
            baos.write(c);
        }

        byte[] tamanho_arquivo = new BigInteger(String.valueOf(arquivo.size())).toByteArray();
        baos.write(tamanho_arquivo.length);
        for (byte b : tamanho_arquivo) {
            baos.write(b);
        }

        byte[] qtd_parts = new BigInteger(String.valueOf(arquivo.getN_parts())).toByteArray();
        baos.write(qtd_parts.length);
        for (byte b : qtd_parts) {
            baos.write(b);
        }
        byte[] md5 = arquivo.getMd5();
        baos.write(md5.length);
        for (byte b : md5) {
            baos.write(b);
        }

        byte[] udp_port = new BigInteger(String.valueOf(udp)).toByteArray();
        baos.write(udp_port.length);
        for (byte b : udp_port) {
            baos.write(b);
        }

        return baos.toByteArray();
    }

    byte[] download(String file, int part, int byte1, int byte2) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(byte1);
        baos.write(byte2);
        baos.write(file.length());
        for (char c : file.toCharArray()) {
            baos.write(c);
        }
        byte[] port = new BigInteger(String.valueOf(part)).toByteArray();
        baos.write(port.length);
        for (byte b : port) {
            baos.write(b);
        }
        return baos.toByteArray();
    }

    byte[] download(String file, int part, byte[] data, int byte1, int byte2) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte b : download(file, part, byte1, byte2))
            baos.write(b);

        for (byte b : data)
            baos.write(b);

        return baos.toByteArray();
    }

    Arquivo download(byte[] data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        bais.read();
        bais.read();

        int len = bais.read();

        StringBuilder name = new StringBuilder();
        while (len > 0) {
            name.append((char) bais.read());
            len--;
        }
        int part = 0;
        try {
            part = new BigInteger(bais.readNBytes(bais.read())).intValue();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Arquivo(bais.readAllBytes(), part, name.toString());
    }


    public FileObject downloadCases(byte[] data) throws IOException {
        List<Peer> peers = new ArrayList<>();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        bais.read();
        bais.read();

        StringBuilder name = new StringBuilder();
        for (byte c : bais.readNBytes(bais.read())) name.append((char) c);


        int tamanho_arquivo = new BigInteger(bais.readNBytes(bais.read())).intValue();
        int qtd_parts = new BigInteger(bais.readNBytes(bais.read())).intValue();

        do {
            byte[] ip = bais.readNBytes(bais.read());

            int port = new BigInteger(bais.readNBytes(bais.read())).intValue();

            peers.add(new Peer(InetAddress.getByAddress(ip), port));
        } while (bais.available() > 0);

        return new FileObject(name.toString(), tamanho_arquivo, qtd_parts, peers);
    }
}
