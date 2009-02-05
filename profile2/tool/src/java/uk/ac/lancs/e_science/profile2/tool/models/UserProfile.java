package uk.ac.lancs.e_science.profile2.tool.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Model for profile information to be used ONLY by the tool.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public class UserProfile implements Serializable {

	private static final long serialVersionUID = 1L;
	private String userId;
	private String nickname;
	private String birthday;
	private String birthdayDisplay;
	private Date dateOfBirth;
	private String displayName;
	private String email;
	private String position;
	private String department;
	private String school;
	private String room;
	private String homepage;
	private String workphone;
	private String homephone;
	private String mobilephone;
	private String favouriteBooks;
	private String favouriteTvShows;
	private String favouriteMovies;
	private String favouriteQuotes;
	private String otherInformation;
	
	
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
		
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getBirthdayDisplay() {
		return birthdayDisplay;
	}
	public void setBirthdayDisplay(String birthdayDisplay) {
		this.birthdayDisplay = birthdayDisplay;
	}
	
	public Date getDateOfBirth() {
		return dateOfBirth;
	}
	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPosition() {
		return position;
	}
	public void setPosition(String position) {
		this.position = position;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}
	public String getSchool() {
		return school;
	}
	public void setSchool(String school) {
		this.school = school;
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	
	public String getHomepage() {
		return homepage;
	}
	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}
	public String getWorkphone() {
		return workphone;
	}
	public void setWorkphone(String workphone) {
		this.workphone = workphone;
	}
	public String getHomephone() {
		return homephone;
	}
	public void setHomephone(String homephone) {
		this.homephone = homephone;
	}
	public String getMobilephone() {
		return mobilephone;
	}
	public void setMobilephone(String mobilephone) {
		this.mobilephone = mobilephone;
	}
	
	
	public String getFavouriteBooks() {
		return favouriteBooks;
	}
	public void setFavouriteBooks(String favouriteBooks) {
		this.favouriteBooks = favouriteBooks;
	}
	public String getFavouriteTvShows() {
		return favouriteTvShows;
	}
	public void setFavouriteTvShows(String favouriteTvShows) {
		this.favouriteTvShows = favouriteTvShows;
	}
	public String getFavouriteMovies() {
		return favouriteMovies;
	}
	public void setFavouriteMovies(String favouriteMovies) {
		this.favouriteMovies = favouriteMovies;
	}
	public String getFavouriteQuotes() {
		return favouriteQuotes;
	}
	public void setFavouriteQuotes(String favouriteQuotes) {
		this.favouriteQuotes = favouriteQuotes;
	}





	public void setOtherInformation(String otherInformation) {
		this.otherInformation = otherInformation;
	}
	public String getOtherInformation() {
		return otherInformation;
	}





	/* for the form feedback, to get around a weird thing in Wicket where it needs a backing model for the FeedbackLabel component */
	private String emailFeedback;
	private String fileFeedback;

	public String getEmailFeedback() {
		return emailFeedback;
	}
	public void setEmailFeedback(String emailFeedback) {
		this.emailFeedback = emailFeedback;
	}
	
	public String getFileFeedback() {
		return fileFeedback;
	}
	public void setFileFeedback(String fileFeedback) {
		this.fileFeedback = fileFeedback;
	}
	
	/**
	 * Default constructor
	 */
	public UserProfile() {
	
	}
	
	
}
