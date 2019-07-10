package Tracker;

import ClientPeer.FileObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FileManager {
    ObjectInputStream in;
    ObjectOutputStream out;
    String path = "./files/";

    private ObjectOutputStream openFileOutput(String filename) throws FileNotFoundException, IOException {
        FileOutputStream fout = new FileOutputStream(path + filename);

        return new ObjectOutputStream(fout);

    }

    private ObjectInputStream openFileInput(String filename) throws FileNotFoundException, IOException {
        FileInputStream fin = new FileInputStream(path + filename);

        return new ObjectInputStream(fin);
    }

    public void addFile(FileObject file) {
        try {
            out = this.openFileOutput(file.getName());
            out.writeObject(file);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    public void getFile(FileObject file) {
        try {
            in = this.openFileInput(file.getName());
            out.writeObject(file);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

}