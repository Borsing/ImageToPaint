package org.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

public class ValidImageValidator implements ConstraintValidator<ValidImage, FileUpload> {

    /** Formats accepted after magic-byte sniffing; anything else (TIFF, BMP, ICO...) is rejected. */
    private static final Set<String> ALLOWED_FORMATS = Set.of("png", "jpeg", "gif");

    /** Guards against decompression-bomb images (tiny file, huge decoded bitmap). */
    private static final long MAX_PIXELS = 40_000_000L;

    @Override
    public boolean isValid(FileUpload file, ConstraintValidatorContext context) {
        if (file == null) {
            return false;
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(file.uploadedFile().toFile())) {
            if (input == null) {
                return false;
            }

            ImageReader reader = findAllowedReader(input);
            if (reader == null) {
                return false;
            }

            try {
                reader.setInput(input);
                long pixels = (long) reader.getWidth(0) * reader.getHeight(0);
                return pixels <= MAX_PIXELS;
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Picks a reader based on the format ImageIO sniffs from the stream's magic bytes -
     * never trust the client-supplied Content-Type or filename extension.
     */
    private ImageReader findAllowedReader(ImageInputStream input) throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
        while (readers.hasNext()) {
            ImageReader reader = readers.next();
            if (ALLOWED_FORMATS.contains(reader.getFormatName().toLowerCase(Locale.ROOT))) {
                return reader;
            }
            reader.dispose();
        }
        return null;
    }
}
