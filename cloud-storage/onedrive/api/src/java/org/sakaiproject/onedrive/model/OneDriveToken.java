package org.sakaiproject.onedrive.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OneDriveToken {

    @JsonProperty("access_token")
    private String currentToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

}
