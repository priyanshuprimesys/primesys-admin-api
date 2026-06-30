package com.primesys.adminserviceserver.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpChatBotApiResponse<T> implements Serializable {
    private boolean success;
    private List<T> data;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private ErrorResponse error;

    public HttpChatBotApiResponse(List data) {
        this.success = true;
        this.data = data;
        this.error = null;
    }

    public HttpChatBotApiResponse(List data, boolean success) {
        this.success = success;
        this.data = data;
        this.error = null;
    }

    public HttpChatBotApiResponse(ErrorResponse data) {
        this.success = false;
        this.data = null;
        this.error = data;
    }

}
