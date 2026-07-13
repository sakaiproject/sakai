/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.lti.api.model;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "lti_tools")
@Data
@NoArgsConstructor
public class LtiTool implements PersistableEntity<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "lti_tools_sequence")
    @SequenceGenerator(name = "lti_tools_sequence", sequenceName = "lti_tools_S")
    private Long id;

    @Column(name = "SITE_ID", length = 99)
    private String siteId;

    @Column(name = "title", length = 1024)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "status")
    @ColumnDefault("0")
    private Integer status = 0;

    @Column(name = "visible")
    @ColumnDefault("0")
    private Integer visible = 0;

    @Column(name = "deployment_id")
    private Integer deploymentId;

    @Column(name = "launch", length = 1024)
    private String launch;

    @Column(name = "newpage")
    @ColumnDefault("0")
    private Integer newPage = 0;

    @Column(name = "frameheight")
    private Integer frameHeight;

    @Column(name = "fa_icon", length = 1024)
    private String faIcon;

    @Column(name = "pl_launch")
    @ColumnDefault("0")
    private Integer plLaunch = 0;

    @Column(name = "pl_linkselection")
    @ColumnDefault("0")
    private Integer plLinkSelection = 0;

    @Column(name = "pl_contextlaunch")
    @ColumnDefault("0")
    private Integer plContextlaunch = 0;

    @Column(name = "pl_lessonsselection")
    @ColumnDefault("0")
    private Integer plLessonsSelection = 0;

    @Column(name = "pl_contenteditor")
    @ColumnDefault("0")
    private Integer plContentEditor = 0;

    @Column(name = "pl_assessmentselection")
    @ColumnDefault("0")
    private Integer plAssessmentSelection = 0;

    @Column(name = "pl_coursenav")
    @ColumnDefault("0")
    private Integer plCourseNav = 0;

    @Column(name = "pl_importitem")
    @ColumnDefault("0")
    private Integer plImportItem = 0;

    @Column(name = "pl_fileitem")
    @ColumnDefault("0")
    private Integer plFileItem = 0;

    @Column(name = "sendname")
    @ColumnDefault("0")
    private Integer sendName = 0;

    @Column(name = "sendemailaddr")
    @ColumnDefault("0")
    private Integer sendEmailAddr = 0;

    @Column(name = "pl_privacy")
    @ColumnDefault("0")
    private Integer plPrivacy = 0;

    @Column(name = "allowoutcomes")
    @ColumnDefault("0")
    private Integer allowOutcomes = 0;

    @Column(name = "allowlineitems")
    @ColumnDefault("0")
    private Integer allowLineItems = 0;

    @Column(name = "allowroster")
    @ColumnDefault("0")
    private Integer allowRoster = 0;

    @Column(name = "debug")
    @ColumnDefault("0")
    private Integer debug = 0;

    @Column(name = "siteinfoconfig")
    @ColumnDefault("0")
    private Integer siteinfoConfig = 0;

    @Lob
    @Column(name = "splash")
    private String splash;

    @Lob
    @Column(name = "custom")
    private String custom;

    @Lob
    @Column(name = "rolemap")
    private String rolemap;

    @Column(name = "lti13")
    @ColumnDefault("0")
    private Integer lti13 = 0;

    @Column(name = "lti13_tool_keyset", length = 1024)
    private String lti13ToolKeyset;

    @Column(name = "lti13_oidc_endpoint", length = 1024)
    private String lti13OidcEndpoint;

    @Column(name = "lti13_oidc_redirect", length = 1024)
    private String lti13OidcRedirect;

    @Column(name = "lti13_client_id", length = 1024)
    private String lti13ClientId;

    @Column(name = "lti13_lms_deployment_id", length = 1024)
    private String lti13LmsDeploymentId;

    @Column(name = "consumerkey", length = 1024)
    private String consumerKey;

    @Column(name = "secret", length = 1024)
    private String secret;

    @Lob
    @Column(name = "xmlimport")
    private String xmlImport;

    @Column(name = "lti13_auto_token", length = 1024)
    private String lti13AutoToken;

    @Column(name = "lti13_auto_state")
    private Integer lti13AutoState;

    @Lob
    @Column(name = "lti13_auto_registration")
    private String lti13AutoRegistration;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "allowgradebookreadonly")
    @ColumnDefault("0")
    private Integer allowGradebookReadOnly = 0;
}
