package org.example.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.validation.ValidImage;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

@Path("/images")
public class ImageResource {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response upload(@RestForm @ValidImage FileUpload file) {
        BufferedImage bitmap;
        try {
            bitmap = ImageIO.read(file.uploadedFile().toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return Response.ok(Map.of(
                "filename", file.fileName(),
                "width", bitmap.getWidth(),
                "height", bitmap.getHeight())).build();
    }
}
