package dev.borsing.imagetopaint.adapter;

import jakarta.enterprise.context.ApplicationScoped;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Optional;

/**
 * The single boundary between this application and {@code javax.imageio}: decoding/encoding
 * full images, and sniffing format/dimensions from magic bytes without paying the cost of a full
 * decode (used to reject decompression-bomb inputs before they're decoded).
 */
@ApplicationScoped
public class ImageCodec {

    public BufferedImage decode(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public byte[] encode(BufferedImage image, String format) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, format, output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Empty when the file can't be opened as an image stream or its format isn't recognized.
     * Never trusts a client-supplied {@code Content-Type} or filename extension — format comes
     * only from the stream's magic bytes.
     */
    public Optional<ImageMetadata> sniff(File file) {
        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            if (input == null) {
                return Optional.empty();
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return Optional.empty();
            }

            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                reader.setInput(input);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                return Optional.of(new ImageMetadata(format, width, height));
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}