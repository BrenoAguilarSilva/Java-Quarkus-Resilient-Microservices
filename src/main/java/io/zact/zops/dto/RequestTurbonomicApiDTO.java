package io.zact.zops.dto;

public class RequestTurbonomicApiDTO {
    private String url;
    private String method;
    private String body;
    private String keyRequestApi;
    private String software;

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

    public String getKeyRequestApi() {
        return keyRequestApi;
    }

    public void setKeyRequestApi(String keyRequestApi) {
        this.keyRequestApi = keyRequestApi;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }
}
