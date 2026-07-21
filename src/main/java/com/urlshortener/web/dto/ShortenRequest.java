package com.urlshortener.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ShortenRequest {

    @NotBlank(message = "url must not be blank")
    @Size(max = 2048, message = "url must be at most 2048 characters")
    private String url;

    @Size(min = 3, max = 32, message = "alias must be 3-32 characters when provided")
    private String alias;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
