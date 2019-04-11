package org.sakaiproject.onedrive.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ONEDRIVE_USER")
@Data @NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OneDriveUser {

    @Id
    @JsonProperty("id")
    private String oneDriveUserId;

    private String sakaiUserId;

    @Lob
    private String token;

    @Lob
    private String refreshToken;
    
    @JsonProperty("userPrincipalName")
    private String oneDriveName;

}
