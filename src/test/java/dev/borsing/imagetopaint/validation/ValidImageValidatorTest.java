package dev.borsing.imagetopaint.validation;

import dev.borsing.imagetopaint.adapter.ImageCodec;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.ws.rs.core.MultivaluedMap;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.CRC32;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Plain unit tests for {@link ValidImageValidator} - no Quarkus/REST bootstrap, {@link FileUpload}
 * is faked with a small record backed by real files on disk.
 */
class ValidImageValidatorTest {

    private final ValidImageValidator validator =
            new ValidImageValidator(new ImageCodec(), List.of("png", "jpeg", "gif"), 40_000_000L);

    @Test
    void acceptsPng(@TempDir Path tempDir) throws IOException {
        assertTrue(validator.isValid(imageFile(tempDir, "sample.png", "png"), null));
    }

    @Test
    void acceptsJpeg(@TempDir Path tempDir) throws IOException {
        assertTrue(validator.isValid(imageFile(tempDir, "sample.jpg", "jpeg"), null));
    }

    @Test
    void acceptsGif(@TempDir Path tempDir) throws IOException {
        assertTrue(validator.isValid(imageFile(tempDir, "sample.gif", "gif"), null));
    }

    @Test
    void rejectsNonImageContent(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("not-an-image.txt");
        Files.writeString(file, "this is definitely not an image");

        assertRejectedWithMessageContaining(new FakeFileUpload(file), "not a recognized image format");
    }

    @Test
    void rejectsFormatOutsideTheAllowList(@TempDir Path tempDir) throws IOException {
        // BMP is decodable by ImageIO but is not in the allow-list (png/jpeg/gif).
        assertRejectedWithMessageContaining(imageFile(tempDir, "sample.bmp", "bmp"), "format 'bmp' is not allowed");
    }

    @Test
    void rejectsImageDeclaringDimensionsAboveThePixelLimit(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("bomb.png");
        Files.write(file, pngWithDeclaredDimensions(20_000, 20_000));

        assertRejectedWithMessageContaining(new FakeFileUpload(file), "20000x20000");
    }

    @Test
    void rejectsNullFile() {
        assertRejectedWithMessageContaining(null, "no file was provided");
    }

    private void assertRejectedWithMessageContaining(FileUpload file, String expectedMessageFragment) {
        ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder =
                mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);

        assertFalse(validator.isValid(file, context));

        verify(context).disableDefaultConstraintViolation();
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(messageCaptor.capture());
        assertTrue(messageCaptor.getValue().contains(expectedMessageFragment),
                () -> "expected message to contain '" + expectedMessageFragment + "' but was: " + messageCaptor.getValue());
    }

    private static FakeFileUpload imageFile(Path dir, String fileName, String formatName) throws IOException {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        Path file = dir.resolve(fileName);
        ImageIO.write(image, formatName, file.toFile());
        return new FakeFileUpload(file);
    }

    /** Builds a PNG with a structurally valid IHDR chunk (correct CRC) but no pixel data. */
    private static byte[] pngWithDeclaredDimensions(int width, int height) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});

        ByteArrayOutputStream ihdrData = new ByteArrayOutputStream();
        writeInt(ihdrData, width);
        writeInt(ihdrData, height);
        ihdrData.write(new byte[] {8, 2, 0, 0, 0}); // bit depth, color type, compression, filter, interlace
        writeChunk(out, "IHDR", ihdrData.toByteArray());

        writeChunk(out, "IEND", new byte[0]);
        return out.toByteArray();
    }

    private static void writeChunk(ByteArrayOutputStream out, String type, byte[] data) throws IOException {
        writeInt(out, data.length);

        ByteArrayOutputStream typeAndData = new ByteArrayOutputStream();
        typeAndData.write(type.getBytes(StandardCharsets.US_ASCII));
        typeAndData.write(data);
        byte[] typeAndDataBytes = typeAndData.toByteArray();
        out.write(typeAndDataBytes);

        CRC32 crc = new CRC32();
        crc.update(typeAndDataBytes);
        writeInt(out, (int) crc.getValue());
    }

    private static void writeInt(ByteArrayOutputStream out, int value) {
        out.write((value >>> 24) & 0xFF);
        out.write((value >>> 16) & 0xFF);
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    private record FakeFileUpload(Path filePath) implements FileUpload {
        @Override
        public String name() {
            return "file";
        }

        @Override
        public String fileName() {
            return filePath.getFileName().toString();
        }

        @Override
        public long size() {
            try {
                return Files.size(filePath);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public String contentType() {
            return "application/octet-stream";
        }

        @Override
        public String charSet() {
            return null;
        }

        @Override
        public MultivaluedMap<String, String> getHeaders() {
            return null;
        }
    }
}
