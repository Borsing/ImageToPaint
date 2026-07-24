package dev.borsing.imagetopaint.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ValidImageValidator implements ConstraintValidator<ValidImage, FileUpload> {

    /** Formats accepted after magic-byte sniffing; anything else (TIFF, BMP, ICO...) is rejected. */
    private static final Set<String> ALLOWED_FORMATS = new LinkedHashSet<>(List.of("png", "jpeg", "gif"));

    /** Guards against decompression-bomb images (tiny file, huge decoded bitmap). */
    private static final long MAX_PIXELS = 40_000_000L;

    @Override
    public boolean isValid(FileUpload file, ConstraintValidatorContext context) {
        if (file == null) {
            return fail(context, "no file was provided");
        }

        try (ImageInputStream input = ImageIO.createImageInputStream(file.uploadedFile().toFile())) {
            if (input == null) {
                return fail(context, "the file could not be read");
            }

            // Format is sniffed from the stream's magic bytes - never trust the client-supplied
            // Content-Type or filename extension.
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return fail(context, "the file content is not a recognized image format");
            }

            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!ALLOWED_FORMATS.contains(format)) {
                    return fail(context, "format '" + format + "' is not allowed, only " + ALLOWED_FORMATS
                            + " are accepted");
                }

                reader.setInput(input);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                long pixels = (long) width * height;
                if (pixels > MAX_PIXELS) {
                    return fail(context, "image is " + width + "x" + height + " (" + pixels
                            + " pixels), which exceeds the " + MAX_PIXELS + " pixel limit");
                }

                return true;
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            return fail(context, "the file could not be decoded as an image");
        }
    }

    private boolean fail(ConstraintValidatorContext context, String detail) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(detail).addConstraintViolation();
        return false;
    }
}
