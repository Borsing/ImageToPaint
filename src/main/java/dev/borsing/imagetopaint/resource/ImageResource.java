package dev.borsing.imagetopaint.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import dev.borsing.imagetopaint.service.ImageFilteringService;
import dev.borsing.imagetopaint.validation.ValidImage;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

@Path("/images")
public class ImageResource {

    @Inject
    ImageFilteringService imageFilteringService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/png")
    public Response upload(@RestForm @ValidImage FileUpload file) {
        try {
            BufferedImage bitmap = ImageIO.read(file.uploadedFile().toFile());
            BufferedImage result = imageFilteringService.filterToBlackAndWhite(bitmap);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(result, "png", output);

            return Response.ok(output.toByteArray()).build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
