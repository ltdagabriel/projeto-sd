package ClientPeer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Arquivo {
    File file;
    private String file_name;
    private double bytes;
    private byte[] data;
    private int part;
    private long n_parts;

    Arquivo(byte[] data, int part, String file_name) {
        this.data = data;
        this.part = part;
        this.file_name = file_name;
    }

    Arquivo(String path) {
        file = new File(path);
        file_name = file.getName();

        bytes = file.length();
        double kilobytes = (bytes / 1024);
        double megabytes = (kilobytes / 1024);
        n_parts = (long) Math.ceil(megabytes / 16);
    }

    long size() {
        return (long) bytes;
    }

    public int getPart() {
        return part;
    }

    public byte[] getData() {
        return data;
    }

    public String getFile_name() {
        return file_name;
    }

    public long getN_parts() {
        return n_parts;
    }

    public byte[] getMd5() {
        InputStream is = null;
        try {
            is = Files.newInputStream(this.file.toPath());
            MessageDigest m = MessageDigest.getInstance("MD5");
            DigestInputStream dis = new DigestInputStream(is, m);
            return m.digest();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }
}