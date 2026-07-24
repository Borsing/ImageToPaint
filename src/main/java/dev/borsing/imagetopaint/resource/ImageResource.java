package dev.borsing.imagetopaint.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import dev.borsing.imagetopaint.adapter.ImageCodec;
import dev.borsing.imagetopaint.application.ImageFilteringFacade;
import dev.borsing.imagetopaint.validation.ValidImage;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.awt.image.BufferedImage;

@Path("/images")
public class ImageResource {

    @Inject
    ImageCodec imageCodec;

    @Inject
    ImageFilteringFacade imageFilteringFacade;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/png")
    public Response upload(@RestForm @ValidImage FileUpload file) {
        BufferedImage bitmap = imageCodec.decode(file.uploadedFile().toFile());
        BufferedImage result = imageFilteringFacade.filterToGrayScale(bitmap);
        byte[] output = imageCodec.encode(result, "png");
        return Response.ok(output).build();
    }
}
