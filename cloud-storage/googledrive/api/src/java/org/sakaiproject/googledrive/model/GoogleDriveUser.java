package org.sakaiproject.googledrive.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "GOOGLEDRIVE_USER")
@Data @NoArgsConstructor
public class GoogleDriveUser {

    @Id
    private String sakaiUserId;
    @Column(unique=true)
    private String googleDriveUserId;
    @Lob
    private String token;
    @Lob
    private String refreshToken;    
    private String googleDriveName;

}
