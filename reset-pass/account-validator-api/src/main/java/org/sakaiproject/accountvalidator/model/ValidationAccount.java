/**
 * $Id$
 * $URL$
 * 
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.accountvalidator.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lombok.Data;
import org.sakaiproject.springframework.data.PersistableEntity;

/**
 * Represents a validation account record used in Sakai's account validation workflow.
 * This entity stores information about account validation requests, including tokens,
 * user details, and validation status.
 *
 * <p>ValidationAccount records are created when:</p>
 * <ul>
 *   <li>New users need to validate their accounts</li>
 *   <li>Existing users need to reset their passwords</li>
 *   <li>Legacy accounts need to be migrated</li>
 *   <li>Users request account updates</li>
 * </ul>
 *
 * <p>The class tracks various account statuses through static constants:</p>
 * <ul>
 *   <li>Token status: SENT, RESENT, CONFIRMED, EXPIRED</li>
 *   <li>Account status: NEW, EXISTING, LEGACY, LEGACY_NOPASS, PASSWORD_RESET, REQUEST_ACCOUNT, USERID_UPDATE</li>
 * </ul>
 *
 * <p>This entity is persisted in the VALIDATIONACCOUNT_ITEM table and includes
 * both persistent fields (stored in database) and transient fields (used during
 * validation workflow but not persisted).</p>
 *
 * @see org.sakaiproject.accountvalidator.dto.ValidationClaim
 * @see org.sakaiproject.accountvalidator.entity.ValidationEntityProvider
 */
@Data
@Entity
@Table(name = "VALIDATIONACCOUNT_ITEM")
public class ValidationAccount implements PersistableEntity<Long> {

	public static final Integer STATUS_SENT = 0;
	public static final Integer STATUS_RESENT = 1;
	public static final Integer STATUS_CONFIRMED = 2;
	public static final Integer STATUS_EXPIRED = 3;

    public static final int ACCOUNT_STATUS_NEW = 1;  // Token for new account
    public static final int ACCOUNT_STATUS_EXISTING = 2;  // Token for existing account
    public static final int ACCOUNT_STATUS_LEGACY = 3;  // Token for pre-deployment account
    public static final int ACCOUNT_STATUS_LEGACY_NOPASS = 4;  // Legacy account without password
    public static final int ACCOUNT_STATUS_PASSWORD_RESET = 5;  // For password reset
    public static final int ACCOUNT_STATUS_REQUEST_ACCOUNT = 6;  // For requested accounts
    public static final int ACCOUNT_STATUS_USERID_UPDATE = 7;  // For userId updates

    @Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "validation_account_seq")
	@SequenceGenerator(name = "validation_account_seq", sequenceName = "VALIDATIONACCOUNT_ITEM_ID_SEQ")
	@JsonIgnore
	private Long id;

    @Column(name = "USER_ID", nullable = false)
	@JsonIgnore
	private String userId;

    @Column(name = "EID")
	@JsonIgnore
	private String eid;

    @Column(name = "VALIDATION_TOKEN", nullable = false)
    @JsonIgnore
    private String validationToken;

    @Column(name = "VALIDATION_SENT")
	@Temporal(TemporalType.TIMESTAMP)
	private Date validationSent;

    @Column(name = "VALIDATION_RECEIVED")
	@Temporal(TemporalType.TIMESTAMP)
	private Date validationReceived;

    @Column(name = "VALIDATIONS_SENT")
	private Integer validationsSent;

    @Column(name = "STATUS")
	private Integer status;

    @Column(name = "FIRST_NAME")
	private String firstName;

    @Column(name = "SURNAME")
	private String surname;

    @Column(name = "ACCOUNT_STATUS")
	private Integer accountStatus;

    @Transient
	private String password;

    @Transient
	private String password2;

    @Transient
    private boolean terms = false;

	public void setTerms(Boolean terms) {
		// RSF likes to set things to null
        this.terms = terms != null && terms;
	}

 }
