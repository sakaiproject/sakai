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

package org.sakaiproject.component.common.edu.person;

import java.util.Date;

import org.sakaiproject.api.common.edu.person.InetOrgPerson;
import org.sakaiproject.api.common.edu.person.OrganizationalPerson;
import org.sakaiproject.api.common.edu.person.Person;
import org.sakaiproject.api.common.edu.person.SakaiPerson;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 */
public class SakaiPersonImpl extends EduPersonImpl implements Person, OrganizationalPerson, InetOrgPerson, SakaiPerson
{
	/**
	 * Empty constuctor for hibernate
	 */
	public SakaiPersonImpl()
	{
		super();
	}

	protected String pictureUrl;

	/*
	 * @see org.sakaiproject.service.profile.SakaiPerson#getPictureUrl()
	 */
	public String getPictureUrl()
	{
		return pictureUrl;
	}

	/*
	 * @see org.sakaiproject.service.profile.SakaiPerson#setPictureUrl(java.lang.String)
	 */
	public void setPictureUrl(String pictureURL)
	{
		this.pictureUrl = pictureURL;
	}

	protected Boolean systemPicturePreferred;

	/*
	 * @see org.sakaiproject.service.profile.SakaiPerson#isSystemPicturePreferred()
	 */
	public Boolean isSystemPicturePreferred()
	{
		return this.systemPicturePreferred;
	}

	/*
	 * @see org.sakaiproject.service.profile.SakaiPerson#setSystemPicturePreferred(java.lang.Boolean)
	 */
	public void setSystemPicturePreferred(Boolean systemPicturePreferred)
	{
		this.systemPicturePreferred = systemPicturePreferred;
	}

	protected String notes;

	/*
	 * @see org.sakaiproject.service.profile.SakaiPerson#getNotes()
	 */
	public String getNotes()
	{
		return this.notes;
	}

	/*
	 * @see org.sakaiproject.service.profile.SakaiPerson#setNotes(java.lang.String)
	 */
	public void setNotes(String notes)
	{
		this.notes = notes;
	}

	protected String campus;

	/*
	 * @see org.sakaiproject.service.profile.SakaiPerson#getCampus()
	 */
	public String getCampus()
	{
		return this.campus;
	}

	/*
	 * @see org.sakaiproject.service.profile.SakaiPerson#setCampus(java.lang.String)
	 */
	public void setCampus(String school)
	{
		this.campus = school;
	}

	/**
	 * Comment for <code>isPrivateInfoViewable</code>
	 */
	protected Boolean hidePrivateInfo;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.service.profile.SakaiPerson#getIsPrivateInfoViewable()
	 */
	public Boolean getHidePrivateInfo()
	{
		return hidePrivateInfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.service.profile.SakaiPerson#setIsPrivateInfoViewable(java.lang.Boolean)
	 */
	public void setHidePrivateInfo(Boolean hidePrivateInfo)
	{
		this.hidePrivateInfo = hidePrivateInfo;
	}

	protected Boolean hidePublicInfo;

	/**
	 * @see org.sakaiproject.service.profile.SakaiPerson#getIsPublicInfoViewable()
	 */
	public Boolean getHidePublicInfo()
	{
		return hidePublicInfo;
	}

	/**
	 * @see org.sakaiproject.service.profile.SakaiPerson#setIsPublicInfoViewable(java.lang.Boolean)
	 */
	public void setHidePublicInfo(Boolean hidePublicInfo)
	{
		this.hidePublicInfo = hidePublicInfo;
	}

	private Boolean ferpaEnabled;

	/**
	 * @see org.sakaiproject.service.profile.SakaiPerson#getFerpaEnabled()
	 * @return Returns the ferpaEnabled.
	 */
	public Boolean getFerpaEnabled()
	{
		return ferpaEnabled;
	}

	/**
	 * @see org.sakaiproject.service.profile.SakaiPerson#setFerpaEnabled(Boolean)
	 * @param ferpaEnabled
	 *        The ferpaEnabled to set.
	 */
	public void setFerpaEnabled(Boolean ferpaEnabled)
	{
		this.ferpaEnabled = ferpaEnabled;
	}

	private Date dateOfBirth; // date of birth
	
	public Date getDateOfBirth() {
		return dateOfBirth;
	}
	
	public void setDateOfBirth(Date dateOfBirth){
		this.dateOfBirth = dateOfBirth;
	}

	private Boolean locked;
	public Boolean getLocked() {
		return locked;
	}

	public void setLocked(Boolean locked) {
		this.locked = locked;
	}
	
	private String favouriteBooks;
	public String getFavouriteBooks() {
		return favouriteBooks;
	}
	
	public void setFavouriteBooks(String favouriteBooks) {
		this.favouriteBooks = favouriteBooks;
	}
	
	private String favouriteTvShows;
	public String getFavouriteTvShows() {
		return favouriteTvShows;
	}
	
	public void setFavouriteTvShows(String favouriteTvShows) {
		this.favouriteTvShows = favouriteTvShows;
	}
	
	private String favouriteMovies;
	public String getFavouriteMovies() {
		return favouriteMovies;
	}
	
	public void setFavouriteMovies(String favouriteMovies) {
		this.favouriteMovies = favouriteMovies;
	}
	
	private String favouriteQuotes;
	public String getFavouriteQuotes() {
		return favouriteQuotes;
	}
	
	public void setFavouriteQuotes(String favouriteQuotes) {
		this.favouriteQuotes = favouriteQuotes;
	}
	
	private String educationCourse;
	public String getEducationCourse() {
		return educationCourse;
	}

	public void setEducationCourse(String educationCourse) {
		this.educationCourse = educationCourse;
	}
	
	private String educationSubjects;
	public String getEducationSubjects() {
		return educationSubjects;
	}

	public void setEducationSubjects(String educationSubjects) {
		this.educationSubjects = educationSubjects;
	}

	private String normalizedMobile;
	public String getNormalizedMobile() {
		return normalizedMobile;
	}

	public void setNormalizedMobile(String number) {
		normalizedMobile = number;
	}

	private String staffProfile;
	public String getStaffProfile() {
		return staffProfile;
	}
	
	public void setStaffProfile(String staffProfile) {
		this.staffProfile = staffProfile;
	}
	
	private String universityProfileUrl;
	public String getUniversityProfileUrl() {
		return universityProfileUrl;
	}
	
	public void setUniversityProfileUrl(String universityProfileUrl) {
		this.universityProfileUrl = universityProfileUrl;
	}

	private String academicProfileUrl;
	public String getAcademicProfileUrl() {
		return academicProfileUrl;
	}
	
	public void setAcademicProfileUrl(String academicProfileUrl) {
		this.academicProfileUrl = academicProfileUrl;
	}

	private String publications;
	public String getPublications() {
		return publications;
	}

	public void setPublications(String publications) {
		this.publications = publications;
	}

	private String businessBiography;
	public String getBusinessBiography() {
		return businessBiography;
	}

	public void setBusinessBiography(String businessBiography) {
		this.businessBiography = businessBiography;
	}

	
}
