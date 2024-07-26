package io.zact.zops.dto;

public class RequestSendDTO {
    private String keyword;
    private String userKeycloakUUID;
    private String tenantUUID;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getUserKeycloakUUID() {
        return userKeycloakUUID;
    }

    public void setUserKeycloakUUID(String userKeycloakUUID) {
        this.userKeycloakUUID = userKeycloakUUID;
    }

    public String getTenantUUID() {
        return tenantUUID;
    }

    public void setTenantUUID(String tenantUUID) {
        this.tenantUUID = tenantUUID;
    }
}
