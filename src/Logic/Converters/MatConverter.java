package Logic.Converters;

import Logic.Domain.Image;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;

import java.util.ArrayList;
import java.util.List;

public class MatConverter {

    public static Image convertMatToImage(Mat mat) {
        // El formato de la imagen es BGR y el de Mat es RGB, así que intercambio esos dos canales
        Mat imgMix = mat.clone();
        List<Mat> imgSrc = new ArrayList<>();
        imgSrc.add(imgMix);
        List<Mat> imgDest = new ArrayList<>();
        imgDest.add(mat);
        int[] fromTo = {0, 2, 2, 0};
        Core.mixChannels(imgSrc, imgDest, new MatOfInt(fromTo));

        // Cargo los datos del Mat a una imagen
        byte[] pixels = new byte[mat.width() * mat.height() * (int) mat.elemSize()];
        mat.get(0, 0, pixels);
        Image image = new Image(mat.width(), mat.height());
        image.getRaster().setDataElements(0, 0, mat.width(), mat.height(), pixels);
        return image;
    }
}
