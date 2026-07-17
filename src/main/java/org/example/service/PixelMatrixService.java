package org.example.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.awt.image.BufferedImage;

@ApplicationScoped
public class PixelMatrixService {

    /**
     * Converts an image to a [height][width][3] matrix of RGB components (0-255 each, alpha dropped).
     * Reads all pixels in a single {@code getRGB} call rather than one call per pixel.
     */
    public int[][][] toRgbMatrix(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] argbPixels = image.getRGB(0, 0, width, height, null, 0, width);

        int[][][] rgb = new int[height][width][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = argbPixels[y * width + x];
                rgb[y][x][0] = (argb >> 16) & 0xFF;
                rgb[y][x][1] = (argb >> 8) & 0xFF;
                rgb[y][x][2] = argb & 0xFF;
            }
        }
        return rgb;
    }
}
