/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
 *
 **********************************************************************************/

package org.sakaiproject.api.common.edu.person;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon</a>
 * @author <a href="mailto:s.swinsburg@lancaster.ac.uk">Steve Swinsburg</a>
 */
import java.util.Date;

public interface SakaiPerson extends EduPerson
{
	
	public static final String PROFILE_SAVE_PERMISSION = "profile.save";
	
	/**
	 * Person's pictureUrl, url to user picture provided.
	 * 
	 * @return
	 */
	public String getPictureUrl();

	/**
	 * Person's pictureUrl, url to user picture provided.
	 * 
	 * @param pictureURL
	 */
	public void setPictureUrl(String pictureURL);

	/**
	 * Person's preference to display system picture, if the institution provided picture is preferred for display.
	 * 
	 * @return
	 */
	public Boolean isSystemPicturePreferred();

	/**
	 * Person's preference to display system picture, if the institution provided picture is preferred for display.
	 * 
	 * @return
	 */
	public void setSystemPicturePreferred(Boolean systemPicturePreferred);

	/**
	 * Other information provided
	 * 
	 * @return
	 */
	public String getNotes();

	/**
	 * Other information provided
	 * 
	 * @param notes :
	 *        set other information provided
	 */
	public void setNotes(String notes);

	// TODO verify method with Lance
	/**
	 * Person's campus
	 * 
	 * @return
	 */
	public String getCampus();

	// TODO verify method with Lance
	/**
	 * Person's campus
	 * 
	 * @param school
	 */
	public void setCampus(String school);

	/**
	 * Person's preference to allow read access to private information
	 * 
	 * @return
	 */
	public Boolean getHidePrivateInfo();

	/**
	 * Person's preference to allow read access to private information
	 * 
	 * @param hidePrivateInfo
	 */
	public void setHidePrivateInfo(Boolean hidePrivateInfo);

	/**
	 * Person's preference to allow read access to public information
	 * 
	 * @return
	 */
	public Boolean getHidePublicInfo();

	/**
	 * Person's preference to allow read access to public information
	 * 
	 * @param hidePublicInfo
	 */
	public void setHidePublicInfo(Boolean hidePublicInfo);

	/**
	 * Has the user invoked their FERPA rights?
	 * 
	 * @return
	 */
	public Boolean getFerpaEnabled();

	/**
	 * @param ferpaEnabled
	 *        TRUE if FERPA rights have been invoked. If FALSE or NULL no FERPA rights have been invoked.
	 */
	public void setFerpaEnabled(Boolean ferpaEnabled);

	
	/**
	 * Set the users Date of birth
	 * @param dateOfBirth Date of Birth
	 * 
	 */
	public void setDateOfBirth(Date dateOfBirth);
	
	/**
	 * get Date of Birth
	 */
	public Date getDateOfBirth();
	
	
	/**
	 * Set the lock status
	 */
	public void setLocked(Boolean locked);
	
	
	/**
	 *  Get the locked status
	 * @return
	 */
	public Boolean getLocked();
	
	
	/**
	 * Set favourite books
	 */
	public void setFavouriteBooks(String favouriteBooks);
	
	
	/**
	 *  Get favourite books
	 * @return
	 */
	public String getFavouriteBooks();
	
	
	/**
	 * Set favourite TV shows
	 */
	public void setFavouriteTvShows(String favouriteTvShows);
	
	
	/**
	 *  Get favourite TV shows
	 * @return
	 */
	public String getFavouriteTvShows();
	
	
	/**
	 * Set favourite movies
	 */
	public void setFavouriteMovies(String favouriteMovies);
	
	
	/**
	 *  Get favourite movies
	 * @return
	 */
	public String getFavouriteMovies();
	
	
	/**
	 * Set favourite quotes
	 */
	public void setFavouriteQuotes(String favouriteQuotes);
	
	
	/**
	 *  Get favourite quotes
	 * @return
	 */
	public String getFavouriteQuotes();
	
	/**
	 * Set education course
	 */
	public void setEducationCourse(String educationCourse);
	
	
	/**
	 *  Get education course
	 * @return
	 */
	public String getEducationCourse();
	
	
	
	/**
	 * Set education subjects
	 */
	public void setEducationSubjects(String educationSubjects);
	
	
	/**
	 *  Get education subjects
	 * @return
	 */
	public String getEducationSubjects();

	
	/**
	 * Get the user's normalized mobile number in international format:
	 * country code + number, no spaces or punctuation, example:
	 * local format for South African number = 083 123 4567,
	 * normalized international form = 27831234567
	 * @return
	 */
	public String getNormalizedMobile();

	
	/**
	 *  Set the user's normalized mobile number in international format.
	 *  Normalize the number before passing it to this method.
	 *  @param the number
	 */
	public void setNormalizedMobile(String Number);
	
	
	/**
	 * Set staff profile info
	 * @param staffProfile
	 */
	public void setStaffProfile(String staffProfile);
	
	/**
	 * Get staff profile info
	 * @return
	 */
	public String getStaffProfile();
	
	/**
	 * Set external profile url
	 * @param universityProfileUrl
	 */
	public void setUniversityProfileUrl(String universityProfileUrl);
	
	/**
	 * Get external profile url
	 * @return
	 */
	public String getUniversityProfileUrl();
	
	/**
	 * Set academic profile url
	 * @param academicProfileUrl
	 */
	public void setAcademicProfileUrl(String academicProfileUrl);
	
	/**
	 * Get academic profile url
	 * @return
	 */
	public String getAcademicProfileUrl();
	
	/**
	 * Set publications
	 * @param publications
	 */
	public void setPublications(String publications);
	
	/**
	 * Get publications
	 * @return
	 */
	public String getPublications();
	
	/**
	 * Set business bio
	 * @param businessBiography
	 */
	public void setBusinessBiography(String businessBiography);
	
	/**
	 * Get business bio
	 * @return
	 */
	public String getBusinessBiography();

	

}
