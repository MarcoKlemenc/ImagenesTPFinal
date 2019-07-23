package Interfaces;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class FileChooser {

    private static final String EXTENSION_PREFIX = "Archivos ";
    private JFileChooser fileC;

    public FileChooser() {
        this.fileC = new JFileChooser();
    }

    public String chooseFolder() {
        initialize(new String[0]);
        this.fileC.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int a = this.fileC.showOpenDialog(null);
        saveDirectory();
        return a == JFileChooser.APPROVE_OPTION ? this.fileC.getSelectedFile().toString() : null;
    }

    public String chooseFile(String[] extensions) {
        initialize(extensions);
        int a = this.fileC.showOpenDialog(this.fileC);
        saveDirectory();
        return a == JFileChooser.APPROVE_OPTION ? this.fileC.getSelectedFile().toString() : null;
    }

    private void saveDirectory() {
        try {
            Files.write(Paths.get("dir.ini"), this.fileC.getCurrentDirectory().toString().getBytes());
        } catch (Exception e) {
        }
    }

    private void initialize(String[] extensions) {
        for (String extension : extensions) {
            String label = EXTENSION_PREFIX + extension.toLowerCase();
            this.fileC.addChoosableFileFilter(new FileNameExtensionFilter(label, extension.substring(1)));
        }
        try {
            Scanner s = new Scanner(new File("dir.ini"));
            this.fileC.setCurrentDirectory(new File(s.nextLine()));
            s.close();
        } catch (Exception e) {
        }
    }
}
