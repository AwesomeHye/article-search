package dev.hyein.article.app.response;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter @Setter
public class ErrorResponse {
    private Map<String, Object> error;

    public ErrorResponse(String msg) {
        this.error = new HashMap<>();
        error.put("message", msg);
    }
}
