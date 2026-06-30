package com.primesys.adminserviceserver.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpApiResponse<T> implements Serializable {
    private boolean success;
    private Result<T> data;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private ErrorResponse error;

    public HttpApiResponse(T data) {
        this.success = true;
        this.data = new Result<>(data);
        this.error = null;
    }

    public HttpApiResponse(T data, boolean success) {
        this.success = success;
        this.data = new Result<>(data);
        this.error = null;
    }

    public HttpApiResponse(ErrorResponse data) {
        this.success = false;
        this.data = null;
        this.error = data;
    }

}
