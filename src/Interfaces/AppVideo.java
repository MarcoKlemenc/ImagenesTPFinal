package Interfaces;

import Logic.Borders.ActiveContours;
import Logic.Converters.BufferedImageConverter;
import Logic.Converters.MatConverter;
import Logic.Domain.Image;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class AppVideo extends JFrame implements ActionListener {

    private static final int X_PADDING = 26;
    private static final int Y_PADDING = 72;
    private static final String[] EXTENSIONS = new String[]{"jpg", "jpeg"};
    private static final FilenameFilter IMAGE_FILTER = ((final File dir, final String name) -> {
        for (final String ext : EXTENSIONS) {
            if (name.endsWith("." + ext)) {
                return true;
            }
        }
        return false;
    });

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Image current = null;
    private double activeContoursHueLimit = 0;
    private double activeContoursSaturationLimit = 0;
    private ImageContainer container;
    private List<ActiveContours> contoursGenerator;
    private List<Image> images;
    private ListIterator<Image> imagesIt;
    private int[][] colors;
    private int currentColorIndex;

    private AppVideo() {
        this.colors = new int[][]{{255, 0, 0}, {0, 255, 0}, {0, 0, 255}};
        this.setJMenuBar(this.createMenuBar());
        this.setLayout(new FlowLayout());
        this.setSize(320, 240);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.container = new ImageContainer(this);
        this.add(this.container);
        this.setVisible(true);
    }

    public static void main(String[] args) {
        new AppVideo();
    }

    private String chooseFolderOpen() {
        FileChooser fc = new FileChooser();
        return fc.chooseFolder();
    }

    public void actionPerformed(ActionEvent e) {
        switch (((JMenuItem) e.getSource()).getText()) {
            case "Abrir carpeta":
                this.loadImages();
                this.nextFrame();
                break;
            case "Abrir video":
                this.loadVideo();
                this.nextFrame();
                break;
            case "Contornos":
                this.activateActiveContour();
                break;
            case "Avanzar":
                this.nextFrame();
                break;
        }
        this.reload();
    }

    private void nextFrame() {
        if (!this.imagesIt.hasNext()) {
            this.imagesIt = this.images.listIterator();
        }
        this.current = this.imagesIt.next();
        if (this.activeContoursHueLimit != 0) {
            this.getActiveContours();
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar barra = new JMenuBar();

        JMenu menu = this.addMenu("Abrir");
        menu.add(this.addMenuItem("Abrir carpeta"));
        menu.add(this.addMenuItem("Abrir video"));
        barra.add(menu);

        barra.add(this.addMenuItem("Contornos"));
        barra.add(this.addMenuItem("Avanzar"));

        // Lo pongo aparte porque necesita un comportamiento especial
        JMenuItem rep = new JMenuItem("Reproducir");
        barra.add(rep);
        // Cada 1000/FPS segundos se va a ejecutar actionPerformed, que va a traer y mostrar el próximo frame
        ActionListener player = ((ActionEvent e) -> {
            if (imagesIt.hasNext()) {
                nextFrame();
                reload();
            } else {
                ((Timer) e.getSource()).stop();
                imagesIt = images.listIterator();
                rep.setEnabled(true);
            }
        });
        // Cuando se haga clic en el botón reproducir se dispara un timer que actualiza la imagen cada 1000/FPS segundos
        rep.addActionListener((ActionEvent e) -> {
            int fps = IntegerEntry.getValues("Ingrese los FPS deseados", new String[]{"FPS"})[0];
            rep.setEnabled(false);
            Timer timer = new Timer(1000 / fps, player);
            timer.start();
        });
        return barra;
    }

    private void reset() {
        this.images = new LinkedList<>();
        this.imagesIt = null;
        this.current = null;
        this.contoursGenerator = new ArrayList<>();
    }

    private void loadImages() {
        this.reset();
        File folder = new File(this.chooseFolderOpen());
        if (!folder.isDirectory()) {
            return;
        }
        TreeSet<File> files = new TreeSet<>((File f1, File f2) -> {
            String number1 = f1.getName().replaceAll("\\D+", "");
            String number2 = f2.getName().replaceAll("\\D+", "");
            Integer n1 = Integer.parseInt(number1);
            Integer n2 = Integer.parseInt(number2);
            return n1.compareTo(n2);
        });
        for (final File f : folder.listFiles(IMAGE_FILTER)) {
            files.add(f);
        }
        for (final File f : files) {
            try {
                Image img = BufferedImageConverter.toImage(ImageIO.read(f));
                this.images.add(img);
            } catch (IOException e) {
            }
        }
        this.imagesIt = this.images.listIterator();
    }

    private void loadVideo() {
        this.reset();
        Mat frame = new Mat();
        FileChooser fc = new FileChooser();
        VideoCapture camera = new VideoCapture();
        camera.open(fc.chooseFile(new String[]{}));
        while (camera.read(frame)) {
            Image img = MatConverter.convertMatToImage(frame);
            this.images.add(img);
        }
        this.imagesIt = this.images.listIterator();
    }

    void addActiveContour(int x1, int y1, int x2, int y2) {
        ActiveContours newContour = new ActiveContours(this.activeContoursHueLimit, this.activeContoursSaturationLimit, x1, y1, x2, y2);
        this.contoursGenerator.add(newContour);
    }

    void getActiveContours() {
        for (ActiveContours c : this.contoursGenerator) {
            c.setImage(this.current);
        }
        int[][] contours = this.getContours();
        this.currentColorIndex++;
        if (this.currentColorIndex == this.colors.length){
            this.currentColorIndex = 0;
        }
        this.container.setContourColor(this.colors[currentColorIndex]);
        this.container.setContours(contours);
    }

    private int[][] getContours() {
        int[][] combinedContours = new int[this.current.getWidth()][this.current.getHeight()];
        for (int x = 0; x < combinedContours.length; x++) {
            for (int y = 0; y < combinedContours[0].length; y++) {
                combinedContours[x][y] = 3;
            }
        }
        for (ActiveContours contour : this.contoursGenerator) {
            this.combineTwoContourMatrixes(combinedContours, contour.getContours());
        }
        return combinedContours;
    }

    private void combineTwoContourMatrixes(int[][] srcMatrix, int[][] matrix) {
        for (int x = 0; x < srcMatrix.length; x++) {
            for (int y = 0; y < srcMatrix[0].length; y++) {
                if (srcMatrix[x][y] == 3 && matrix[x][y] != 3) {
                    srcMatrix[x][y] = matrix[x][y];
                }
            }
        }
    }

    // Buscar contornos activos
    private void activateActiveContour() {
        double[] parameters = DoubleEntry.getValues("Ingrese los factores", new String[]{"Matiz", "Saturación"});
        this.activeContoursHueLimit = parameters[0];
        this.activeContoursSaturationLimit = parameters[1];
        this.container.setMode("activeContours");
    }

    private JMenu addMenu(String texto) {
        return new JMenu(texto);
    }

    private JMenuItem addMenuItem(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(this);
        return item;
    }

    private void reload() {
        if (this.current != null) {
            this.container.setIcon(new ImageIcon(this.current));
            this.setSize(this.current.getWidth() + X_PADDING, this.current.getHeight() + Y_PADDING);
        }
    }
}
