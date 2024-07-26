package io.zact.zops.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.zact.zops.dto.RequestProcessDTO;
import io.zact.zops.dto.RequestProcessMapper;
import io.zact.zops.dto.RequestTurbonomicApiDTO;
import io.zact.zops.exception.PermissionFailedException;
import io.zact.zops.exception.RequestApiException;
import io.zact.zops.resource.ProducerResource;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@ApplicationScoped
public class RequestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProducerResource.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    String urlCompanyAPI = ConfigProvider.getConfig().getValue("quarkus.company.api.server-url", String.class);
    String urlTurbonomicAPI = ConfigProvider.getConfig().getValue("quarkus.turbonomic.api.server-url", String.class);


    public RequestProcessDTO requestSubmissionToTurbonomic(String accessToken, UUID userKeycloakUUID, String keyword, String tenantUUID) {
        LOGGER.info("Verification Permission");
        Boolean permission = verifyPermissionFromUser(accessToken, userKeycloakUUID);
        if (!permission) {
            LOGGER.error("Unauthorized exception in requestSubmissionToTurbonomic");
            throw new PermissionFailedException("Unauthorized");
        }
        LOGGER.info("Verification Request API");
        RequestTurbonomicApiDTO requestTurbonomicApiDTO = requestAPIToTurbonomicAPI(keyword);
        RequestProcessDTO requestProcessDTO = RequestProcessMapper.toRequestProcessDTO(requestTurbonomicApiDTO);
        requestProcessDTO.setRequestUUID(UUID.randomUUID());
        requestProcessDTO.setTenantUUID(tenantUUID);

        return requestProcessDTO;
    }

    @CircuitBreaker(
            requestVolumeThreshold = 4,
            failureRatio = 0.50,
            delay = 1000,
            successThreshold = 2
    )
    @Fallback(fallbackMethod = "fallbackVerifyPermissionFromUser")
    @Retry(maxRetries = 3, delay = 5000)
    public Boolean verifyPermissionFromUser(String accessToken, UUID userKeycloakUUID){
        if (accessToken.isBlank() || userKeycloakUUID == null) {
            LOGGER.error("Access token or userKeycloakUUI invalid or is blank");
            throw new IllegalArgumentException("Access token or userKeycloakUUI invalid or is blank");
        }
        try{
            String URL = urlCompanyAPI + "/api/v1/keycloak/getUser/groups/" + userKeycloakUUID.toString();
            LOGGER.debug("URL: {}", URL);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.error("Failed to fetch user groups: HTTP {}", response.statusCode());
                throw new PermissionFailedException("Failed to fetch user groups: HTTP " + response.statusCode());
            }

            String requiredGroupName = "Request";

            JsonNode responseBody = objectMapper.readTree(response.body());
            for (JsonNode group : responseBody) {
                LOGGER.info("Found group: {}", group.toString());
                if (group.get("name").asText().equals(requiredGroupName)) {
                    return true;
                }
            }
            throw new PermissionFailedException("Failed to find required group: " + requiredGroupName);
        }  catch (IOException | InterruptedException e) {
            LOGGER.error("Erro de comunicação: {}", e.getMessage());
            throw new PermissionFailedException("Erro de comunicação: {}" + e.getMessage());
        }
    }

    public Boolean fallbackVerifyPermissionFromUser(String accessToken, UUID userKeycloakUUID) {
        LOGGER.warn("Fallback method called due to failure in verifyPermissionFromUser");
        return false;
    }

    @CircuitBreaker(
            requestVolumeThreshold = 4,
            failureRatio = 0.50,
            delay = 1000,
            successThreshold = 2
    )
    @Fallback(fallbackMethod = "fallbackrequestAPIToTurbonomicAPI")
    @Retry(maxRetries = 3, delay = 5000)
    public RequestTurbonomicApiDTO requestAPIToTurbonomicAPI(String keyword){
        if (keyword.isBlank() || keyword.isEmpty()) {
            LOGGER.error("keyword invalid or is blank");
            throw new IllegalArgumentException("keyword invalid or is blank");
        }
        try{
            String URL = urlTurbonomicAPI + "/api/v1/processRequest/keyRequestApi/" + keyword;
            LOGGER.debug("URL: {}", URL);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LOGGER.error("Failed to request api");
                throw new RequestApiException("Failed to request api");
            }

            String responseBody = response.body();
            LOGGER.debug("Response Body: {}", responseBody);

            ObjectMapper objectMapper = new ObjectMapper();
            RequestTurbonomicApiDTO apiResponse = objectMapper.readValue(responseBody, RequestTurbonomicApiDTO.class);

            return apiResponse;
        }  catch (IOException | InterruptedException e) {
            LOGGER.error("Erro de comunicação: {}", e.getMessage());
            throw new PermissionFailedException("Erro de comunicação: {}" + e.getMessage());
        }
    }

    public RequestTurbonomicApiDTO fallbackrequestAPIToTurbonomicAPI(String keyword) {
        LOGGER.warn("Failed to request api from turbonomic API");
        return new RequestTurbonomicApiDTO();
    }
}
