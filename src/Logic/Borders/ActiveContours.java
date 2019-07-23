package Logic.Borders;

import Logic.Domain.Image;
import Logic.Domain.Pixel;

import java.awt.*;

public class ActiveContours {

    private Image image;
    private double hueLimit;
    private double saturationLimit;
    private int[] averages;
    private double averageHue;
    private double averageSaturation;
    private int[][] contours;
    private int x1, y1, x2, y2;

    public ActiveContours(double hueLimit, int x1, int y1, int x2, int y2) {
        this.hueLimit = hueLimit;
        this.saturationLimit = hueLimit / 360;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    private double getDistance(double angle1, double angle2) {
        double phi = Math.abs(angle2 - angle1) % 360;
        return phi > 180 ? 360 - phi : phi;
    }

    private boolean isPartOfRegion(double hue, double saturation) {
        return this.getDistance(this.averageHue, hue) <= hueLimit && Math.abs(this.averageSaturation - saturation) <= saturationLimit;
    }

    private boolean updateContours() {
        boolean modifiedValue = false;
        int[][] newContours = new int[this.image.getWidth()][this.image.getHeight()];
        // Copio el array de contornos para trabajar sin ir pisando los datos
        for (int x = 0; x < this.contours.length; x++) {
            for (int y = 0; y < this.contours[0].length; y++) {
                newContours[x][y] = this.contours[x][y];
            }
        }
        for (int x = 1; x < this.contours.length - 1; x++) {
            for (int y = 1; y < this.contours[0].length - 1; y++) {
                if (Math.abs(this.contours[x][y]) == 3) {
                    continue;
                }
                Pixel pixel = this.image.getPixel(x, y);
                float[] hsv = new float[3];
                Color.RGBtoHSB(pixel.getRedChannel(), pixel.getGreenChannel(), pixel.getBlueChannel(), hsv);
                double hue = hsv[0] * 360;
                double saturation = hsv[1];
                // Si es Lin y el pixel está lejos del promedio, lo marco como Lout
                if (this.contours[x][y] == -1 && !this.isPartOfRegion(hue, saturation)) {
                    newContours[x][y] = 1;
                    modifiedValue = true;
                    // Reviso los pixeles lindantes, objeto pasa a Lin
                    for (int i = x - 1; i <= x + 1; i++) {
                        for (int j = y - 1; j <= y + 1; j++) {
                            if (this.contours[i][j] == -3) {
                                newContours[i][j] = -1;
                            }
                        }
                    }
                }
                // Si es Lout y el pixel está cerca del promedio, lo marco como Lin
                else if (this.contours[x][y] == 1 && this.isPartOfRegion(hue, saturation)) {
                    newContours[x][y] = -1;
                    modifiedValue = true;
                    // Reviso todos los pixeles lindantes y marco todos los que sean fondo como Lout
                    for (int i = x - 1; i <= x + 1; i++) {
                        for (int j = y - 1; j <= y + 1; j++) {
                            if (this.contours[i][j] == 3) {
                                newContours[i][j] = 1;
                            }
                            if (this.contours[i][j] == 1) {
                                newContours[i][j] = -1;
                            }
                        }
                    }
                }
                // Si es Lout, me fijo si debe seguir siéndolo
                else if (this.contours[x][y] == 1) {
                    boolean linFound = false;
                    for (int i = x - 1; i <= x + 1; i++) {
                        for (int j = y - 1; j <= y + 1; j++) {
                            if (this.contours[i][j] == -1) {
                                linFound = true;
                                break;
                            }
                        }
                    }
                    if (!linFound) {
                        newContours[x][y] = 3;
                    }
                }
            }
        }
        for (int x = 1; x < this.contours.length - 1; x++) {
            for (int y = 1; y < this.contours[0].length - 1; y++) {
                if (newContours[x][y] == -1) {
                    boolean nextToLout = false;
                    for (int i = x - 1; i <= x + 1; i++) {
                        for (int j = y - 1; j <= y + 1; j++) {
                            if (newContours[i][j] == 1) {
                                nextToLout = true;
                                break;
                            }
                        }
                    }
                    if (!nextToLout) {
                        newContours[x][y] = -3;
                    }
                }
            }
        }
        if (modifiedValue) {
            this.contours = newContours;
        }

        return modifiedValue;
    }

    public int[][] getContours() {
        if (this.contours == null) {
            this.contours = new int[this.image.getWidth()][this.image.getHeight()];
            if (this.averages == null) {
                this.averages = this.image.getPixelAverages(this.x1, this.y1, this.x2, this.y2);
                float[] hsv = new float[3];
                Color.RGBtoHSB(averages[0], averages[1], averages[2], hsv);
                this.averageHue = (int) (hsv[0] * 360);
                this.averageSaturation = hsv[1];
            }
            for (int x = 0; x < this.contours.length; x++) {
                for (int y = 0; y < this.contours[0].length; y++) {
                    int val = 3;
                    // Si está dentro del rectángulo
                    if (x > this.x1 && x < this.x2 && y > this.y1 && y < this.y2) {
                        val = -3;
                    }
                    // Si está en el borde izquierdo o derecho
                    else if ((x == this.x1 || x == this.x2) && y >= this.y1 && y <= this.y2) {
                        val = -1;
                    }
                    // Si está en el borde superior o inferior
                    else if ((y == this.y1 || y == this.y2) && x >= this.x1 && x <= this.x2) {
                        val = -1;
                    }
                    // Si está justo fuera del borde izquierdo o derecho
                    else if ((this.x1 - x == 1 || x - this.x2 == 1) && y >= this.y1 && y <= this.y2) {
                        val = 1;
                    }
                    // Si está justo fuera del borde superior o inferior
                    else if ((this.y1 - y == 1 || y - this.y2 == 1) && x >= this.x1 && x <= this.x2) {
                        val = 1;
                    }
                    this.contours[x][y] = val;
                }
            }
        }
        boolean updated;
        do {
            //Tiempo de inicio
            /*SimpleDateFormat formatterInit = new SimpleDateFormat("HH:mm:ss.SSS");
            Date dateInit = new Date();
            System.out.println("Inicio frame: " + formatterInit.format(dateInit));*/

            updated = this.updateContours();

            //Tiempo de fin
            /*SimpleDateFormat formatterFinish = new SimpleDateFormat("HH:mm:ss.SSS");
            Date dateFinish = new Date();
            System.out.println("Final frame: " + formatterFinish.format(dateFinish));

            System.out.println(" ");*/
        } while (updated);
        return this.contours;
    }
}
