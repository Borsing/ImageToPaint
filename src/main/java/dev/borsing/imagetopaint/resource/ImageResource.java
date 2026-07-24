package dev.borsing.imagetopaint.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.core.Response;
import dev.borsing.imagetopaint.adapter.ImageCodec;
import dev.borsing.imagetopaint.usecase.ImageFilteringFacade;
import dev.borsing.imagetopaint.domain.filter.PaintingFilterParams;
import dev.borsing.imagetopaint.validation.ValidImage;
import jakarta.validation.constraints.Min;
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
    @Path("/grayscale")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/png")
    public Response grayScale(@RestForm @ValidImage FileUpload file) {
        BufferedImage bitmap = imageCodec.decode(file.uploadedFile().toFile());
        BufferedImage result = imageFilteringFacade.filterToGrayScale(bitmap);
        byte[] output = imageCodec.encode(result, "png");
        return Response.ok(output).build();
    }

    @POST
    @Path("/paint")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/png")
    public Response paint(@RestForm @ValidImage FileUpload file,
                          @RestForm @DefaultValue("6") @Min(1) int numberOfColors,
                          @RestForm @DefaultValue("42") @Min(1) long seed,
                          @RestForm @DefaultValue("100") @Min(1) int maxIterations) {

        BufferedImage bitmap = imageCodec.decode(file.uploadedFile().toFile());
        BufferedImage result = imageFilteringFacade.filterToPaint(bitmap, new PaintingFilterParams(numberOfColors, seed, maxIterations));
        byte[] output = imageCodec.encode(result, "png");

        return Response.ok(output).build();
    }
}
