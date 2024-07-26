package io.zact.zops.resource;

import io.quarkus.security.Authenticated;
import io.zact.zops.dto.RequestProcessDTO;
import io.zact.zops.dto.RequestSendDTO;
import io.zact.zops.exception.PermissionFailedException;
import io.zact.zops.exception.RequestApiException;
import io.zact.zops.service.RequestService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * ProducerResource
 * <p>
 * This class is responsible for handling all HTTP requests related to producer operations.
 * It is a RESTful resource that can be accessed through the path "/api/v1/request".
 * It is responsible for handling all CRUD operations related to Request entity.
 * It is also responsible for handling all exceptions related to Request entity.
 * It is also responsible for handling all security related to Request entity.
 * <p>
 */
@Path("/api/v1/request")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Request", description = "Request operations")
@Authenticated
public class ProducerResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerResource.class);
    RequestService requestService;

    @Inject
    @Channel("send-request")
    Emitter<RequestProcessDTO> requestProcessDTOEmitter;

    @Inject
    public ProducerResource(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * This method is used to send a new request to queue
     *
     * @param requestSendDTO - A JSON object with all necessary fields to send a new request to queue
     * @return Response with the confirmation send new request to queue
     */
    @POST
    @Path("send/turbonomic")
    @Operation(summary = "send a new request to queue", description = "send a new request to queue")
    @APIResponse(responseCode = "200", description = "Send a new request to queue successfully")
    @APIResponse(responseCode = "400", description = "Request API not found")
    @APIResponse(responseCode = "401", description = "Send a new request unauthorized")
    @APIResponse(responseCode = "500", description = "Send a new request to queue Failed")
    @RolesAllowed("request-send")
    public Response requestSubmissionToTurbonomic(@Parameter(description = "A JSON object with all necessary fields to send a new request to queue") RequestSendDTO requestSendDTO, @Parameter(description = "Token permission") @HeaderParam("Authorization") String accessToken){
        try{
            RequestProcessDTO requestProcessDTO = requestService.requestSubmissionToTurbonomic(accessToken, UUID.fromString(requestSendDTO.getUserKeycloakUUID()), requestSendDTO.getKeyword(), requestSendDTO.getTenantUUID());
            LOGGER.info("Adicionando a requisição na fila do Kafka");
            requestProcessDTOEmitter.send(requestProcessDTO);
            return Response.ok().entity("Solicitação colocada na fila para execução: " + requestProcessDTO.getRequestUUID().toString()).build();
        } catch (PermissionFailedException permissionFailedException){
            LOGGER.error("Failed to send email to queue", permissionFailedException);
            return Response.status(Response.Status.UNAUTHORIZED).entity(permissionFailedException.getMessage()).build();
        } catch (IllegalArgumentException illegalArgumentException){
            LOGGER.error("Failed to send email to queue", illegalArgumentException);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(illegalArgumentException.getMessage()).build();
        } catch (RequestApiException requestApiException){
            LOGGER.error("Failed to send email to queue", requestApiException);
            return Response.status(Response.Status.NOT_FOUND).entity(requestApiException.getMessage()).build();
        }
    }
}
