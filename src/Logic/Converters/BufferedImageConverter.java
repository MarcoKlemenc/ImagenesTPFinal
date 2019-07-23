package Logic.Converters;

import Logic.Domain.Image;

import java.awt.image.BufferedImage;

public class BufferedImageConverter {

    public static Image toImage(BufferedImage image) {
        Image res = new Image(image.getWidth(), image.getHeight());
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                res.setRGB(x, y, image.getRGB(x, y));
            }
        }
        return res;
    }
}
