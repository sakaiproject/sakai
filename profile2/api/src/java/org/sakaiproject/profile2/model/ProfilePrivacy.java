/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.model;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Hibernate and EntityProvider model
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */

@Data
@NoArgsConstructor
public class ProfilePrivacy implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String userUuid;
	private int profileImage;
	private int basicInfo;
	private int contactInfo;
	private int businessInfo;
	private int personalInfo;
	private boolean showBirthYear;
	private int myFriends;
	private int myStatus;
	private int myPictures;
	private int messages;
	private int studentInfo;
	private int staffInfo;
	private int socialNetworkingInfo;
	private int myKudos;
	private int myWall;
	private int onlineStatus;
	
}
