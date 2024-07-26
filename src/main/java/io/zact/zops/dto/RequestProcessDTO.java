package io.zact.zops.dto;

import java.util.UUID;

public class RequestProcessDTO {
    private UUID requestUUID;
    private String url;
    private String method;
    private String body;
    private String keyword;
    private String software;
    private String tenantUUID;

    public UUID getRequestUUID() {
        return requestUUID;
    }

    public void setRequestUUID(UUID requestUUID) {
        this.requestUUID = requestUUID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getTenantUUID() {
        return tenantUUID;
    }

    public void setTenantUUID(String tenantUUID) {
        this.tenantUUID = tenantUUID;
    }
}
