package Logic.Domain;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Image extends BufferedImage {

    public Image(int width, int height) {
        super(width, height, TYPE_3BYTE_BGR);
    }

    public Pixel getPixel(int x, int y) {
        Color color = new Color(super.getRGB(x, y));
        Pixel pixel = new Pixel(color.getRed(), color.getGreen(), color.getBlue());

        return pixel;
    }

    public int[] getPixelAverages(int x1, int y1, int x2, int y2) {
        int red = 0;
        int green = 0;
        int blue = 0;
        int pixelQty = 0;
        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                Pixel pixel = this.getPixel(x, y);
                red += pixel.getRedChannel();
                green += pixel.getGreenChannel();
                blue += pixel.getBlueChannel();
                pixelQty++;
            }
        }
        red /= pixelQty;
        green /= pixelQty;
        blue /= pixelQty;
        return new int[]{red, green, blue};
    }
}