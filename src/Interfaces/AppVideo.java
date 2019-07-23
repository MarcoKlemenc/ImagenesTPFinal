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

    protected static final int X_PADDING = 26;
    protected static final int Y_PADDING = 72;
    private static final String[] EXTENSIONS = new String[]{"jpg", "jpeg"};
    static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return true;
                }
            }
            return false;
        }
    };

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private Image current = null;
    private double activeContoursHueLimit = 0;
    private ImageContainer container;
    private ImageIcon icon = null;
    private List<ActiveContours> contoursGenerator = new ArrayList<>();
    private List<Image> images;
    private ListIterator<Image> imagesIt;

    public AppVideo() {
        this.setJMenuBar(this.createMenuBar());
        setLayout(new FlowLayout());
        setSize(320, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.container = new ImageContainer(this);
        this.add(this.container);
        setVisible(true);
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

    public void nextFrame() {
        if (!this.imagesIt.hasNext()) {
            this.imagesIt = this.images.listIterator();
        }
        Image image = this.imagesIt.next();
        this.current = image;
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
        ActionListener player = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (imagesIt.hasNext()) {
                    nextFrame();
                    reload();
                } else {
                    ((Timer) e.getSource()).stop();
                    imagesIt = images.listIterator();
                    rep.setEnabled(true);
                }
            }
        };
        // Cuando se haga clic en el botón reproducir se dispara un timer que actualiza la imagen cada 1000/FPS segundos
        rep.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int fps = IntegerEntry.getValues("Ingrese los FPS deseados", new String[]{"FPS"})[0];
                rep.setEnabled(false);
                Timer timer = new Timer(1000 / fps, player);
                timer.start();
            }
        });
        return barra;
    }

    public void loadImages() {
        File folder = new File(this.chooseFolderOpen());
        if (!folder.isDirectory()) {
            return;
        }
        TreeSet<File> files = new TreeSet<>(new Comparator<File>() {
            public int compare(File f1, File f2) {
                String number1 = f1.getName().replaceAll("\\D+", "");
                String number2 = f2.getName().replaceAll("\\D+", "");
                Integer n1 = Integer.parseInt(number1);
                Integer n2 = Integer.parseInt(number2);
                return n1.compareTo(n2);
            }
        });
        for (final File f : folder.listFiles(IMAGE_FILTER)) {
            files.add(f);
        }
        this.images = new LinkedList<>();
        for (final File f : files) {
            try {
                Image img = BufferedImageConverter.toImage(ImageIO.read(f));
                this.images.add(img);
            } catch (IOException e) {
            }
        }
        this.imagesIt = this.images.listIterator();
    }

    public void loadVideo() {
        Mat frame = new Mat();
        FileChooser fc = new FileChooser();
        VideoCapture camera = new VideoCapture();
        camera.open(fc.chooseFile(new String[]{}));
        this.images = new LinkedList<>();
        while (camera.read(frame)) {
            Image img = MatConverter.convertMatToImage(frame);
            this.images.add(img);
        }
        this.imagesIt = this.images.listIterator();
    }

    public void addActiveContour(int x1, int y1, int x2, int y2) {
        ActiveContours newContour = new ActiveContours(this.activeContoursHueLimit, x1, y1, x2, y2);
        this.contoursGenerator.add(newContour);
    }

    public void getActiveContours() {
        for (ActiveContours c : this.contoursGenerator) {
            c.setImage(this.current);
        }
        int[][] contours = this.getContours();
        this.container.setContourColor(new int[]{255, 255, 255});
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
            combinedContours = this.combineTwoContourMatrixes(combinedContours, contour.getContours());
        }
        return combinedContours;
    }

    private int[][] combineTwoContourMatrixes(int[][] srcMatrix, int[][] matrix) {
        for (int x = 0; x < srcMatrix.length; x++) {
            for (int y = 0; y < srcMatrix[0].length; y++) {
                if (srcMatrix[x][y] == 3 && matrix[x][y] != 3) {
                    srcMatrix[x][y] = matrix[x][y];
                }
            }
        }
        return srcMatrix;
    }

    // Buscar contornos activos
    public void activateActiveContour() {
        double[] parameters = DoubleEntry.getValues("Ingrese el factor de matiz", new String[]{"Factor de matiz"});
        this.activeContoursHueLimit = parameters[0];
        this.container.setMode("activeContours");
    }

    public JMenu addMenu(String texto) {
        return new JMenu(texto);
    }

    public JMenuItem addMenuItem(String texto) {
        JMenuItem item = new JMenuItem(texto);
        item.addActionListener(this);
        return item;
    }

    public void reload() {
        if (this.current != null) {
            this.icon = new ImageIcon(this.current);
            this.container.setIcon(icon);
            this.setSize(this.current.getWidth() + X_PADDING, this.current.getHeight() + Y_PADDING);
        }
    }
}