package code;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

/**
 * Created by Andreas on 11.08.2016.
 *
 * Klasse, die die Vorbereitung der Bilder auf das Hashing vornimmt.
 */
public class ImageCalculator {

    private static ColorConvertOp colorConvert = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

    public static BufferedImage resize(BufferedImage image, int width, int height){
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    public static BufferedImage grayscale(BufferedImage img){
        colorConvert.filter(img, img);
        return img;
    }

    public static int getBlue(BufferedImage img, int x, int y){
        return (img.getRGB(x, y)) & 0xff;
    }

}
