package org.example.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.awt.image.BufferedImage;

@ApplicationScoped
public class ImageTransformationService {

    // Placeholder passthrough until the actual paint transformation pipeline is implemented.
    public BufferedImage transform(BufferedImage image) {
        return image;
    }
}
