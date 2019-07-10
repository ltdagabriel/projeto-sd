package ClientPeer;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Arquivos {
    private Semaphore lock = new Semaphore(1);

    /**
     * @param file_path : caminho do arquivo
     * @param data      : Dados as serem gravados no arquivo
     * @param position  : Posição do ponteiro em Byte
     * @throws IOException: ClientPeer.Arquivo ocupado/ inexistente
     */
    void escreve_no_arquivo(String file_path, byte[] data, int position) throws IOException, InterruptedException {
        lock.acquire();
        System.out.println(file_path);
        RandomAccessFile file = new RandomAccessFile(file_path, "rw");
        file.seek(position);
        file.write(data);
        file.close();
        lock.release();
    }

    /**
     * @param file_path : caminho do arquivo
     * @param position  : Posição do ponteiro em Byte
     * @param size      : Quantidade de Bytes a serem lidos
     * @return Bytes lidos
     * @throws IOException ClientPeer.Arquivo ocupado/ inexistente
     */

    byte[] ler_do_arquivo(String file_path, int position, int size) throws IOException, InterruptedException {
        lock.acquire();
        RandomAccessFile file = new RandomAccessFile(file_path, "r");
        file.seek(position);
        byte[] data = new byte[size];
        int len = file.read(data);
        file.close();
        lock.release();
        if (len > 0)
            return Arrays.copyOfRange(data, 0, len);

        return new byte[0];
    }


    static int exist(String file, int part) {
        Arquivos arquivos = new Arquivos();
        try {
            byte[] data = arquivos.ler_do_arquivo(file, part, 3);
            return data.length;

        } catch (InterruptedException | IOException e) {
            return 0;
        }
    }

    static Arquivo info(String file_name) {
        return new Arquivo(file_name);
    }

}
