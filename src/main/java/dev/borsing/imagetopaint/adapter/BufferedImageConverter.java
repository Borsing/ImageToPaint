package dev.borsing.imagetopaint.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import dev.borsing.imagetopaint.domain.Image;
import dev.borsing.imagetopaint.domain.color.Rgb;

import java.awt.image.BufferedImage;

@ApplicationScoped
public class BufferedImageConverter {

    /**
     * Converts an image to a [height][width] matrix of {@link Rgb} pixels (alpha dropped).
     * Reads all pixels in a single {@code getRGB} call rather than one call per pixel.
     */
    public Image toDomainImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] argbPixels = image.getRGB(0, 0, width, height, null, 0, width);

        Rgb[][] pixels = new Rgb[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = argbPixels[y * width + x];
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                pixels[y][x] = new Rgb(r, g, b);
            }
        }

        return new Image(pixels);
    }

    public BufferedImage fromDomainImage(Image image) {
        int height = image.height();
        int width = image.width();

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] packedPixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Rgb pixel = image.pixels()[y][x];
                packedPixels[y * width + x] = (pixel.red() << 16) | (pixel.green() << 8) | pixel.blue();
            }
        }

        bufferedImage.setRGB(0, 0, width, height, packedPixels, 0, width);

        return bufferedImage;
    }
}
