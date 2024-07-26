package io.zact.zops.dto;

public class RequestProcessMapper {
    public static RequestProcessDTO toRequestProcessDTO(RequestTurbonomicApiDTO requestTurbonomicApiDTO) {
        return setRequestProcessFromDTO(requestTurbonomicApiDTO);
    }

    private static RequestProcessDTO setRequestProcessFromDTO(RequestTurbonomicApiDTO requestTurbonomicApiDTO){
        RequestProcessDTO requestProcessDTO = new RequestProcessDTO();
        requestProcessDTO.setUrl(requestTurbonomicApiDTO.getUrl());
        requestProcessDTO.setMethod(requestTurbonomicApiDTO.getMethod());
        requestProcessDTO.setBody(requestTurbonomicApiDTO.getBody());
        requestProcessDTO.setKeyword(requestTurbonomicApiDTO.getKeyRequestApi());
        requestProcessDTO.setSoftware(requestTurbonomicApiDTO.getSoftware());

        return requestProcessDTO;
    }
}
