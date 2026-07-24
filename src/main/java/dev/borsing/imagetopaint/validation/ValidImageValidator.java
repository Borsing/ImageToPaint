package dev.borsing.imagetopaint.validation;

import dev.borsing.imagetopaint.adapter.ImageCodec;
import dev.borsing.imagetopaint.adapter.ImageMetadata;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ValidImageValidator implements ConstraintValidator<ValidImage, FileUpload> {

    private final ImageCodec imageCodec;

    /** Formats accepted after magic-byte sniffing; anything else (TIFF, BMP, ICO...) is rejected. */
    private final Set<String> allowedFormats;

    /** Guards against decompression-bomb images (tiny file, huge decoded bitmap). */
    private final long maxPixels;

    @Inject
    public ValidImageValidator(
            ImageCodec imageCodec,
            @ConfigProperty(name = "imagetopaint.image.allowed-formats") List<String> allowedFormats,
            @ConfigProperty(name = "imagetopaint.image.max-pixels") long maxPixels) {
        this.imageCodec = imageCodec;
        this.allowedFormats = new LinkedHashSet<>(allowedFormats);
        this.maxPixels = maxPixels;
    }

    @Override
    public boolean isValid(FileUpload file, ConstraintValidatorContext context) {
        if (file == null) {
            return fail(context, "no file was provided");
        }

        Optional<ImageMetadata> metadata = imageCodec.sniff(file.uploadedFile().toFile());
        if (metadata.isEmpty()) {
            return fail(context, "the file content is not a recognized image format");
        }

        ImageMetadata image = metadata.get();
        if (!allowedFormats.contains(image.format())) {
            return fail(context, "format '" + image.format() + "' is not allowed, only " + allowedFormats
                    + " are accepted");
        }

        long pixels = (long) image.width() * image.height();
        if (pixels > maxPixels) {
            return fail(context, "image is " + image.width() + "x" + image.height() + " (" + pixels
                    + " pixels), which exceeds the " + maxPixels + " pixel limit");
        }

        return true;
    }

    private boolean fail(ConstraintValidatorContext context, String detail) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(detail).addConstraintViolation();
        return false;
    }
}
