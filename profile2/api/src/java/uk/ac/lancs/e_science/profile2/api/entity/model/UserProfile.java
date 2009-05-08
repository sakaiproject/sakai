package uk.ac.lancs.e_science.profile2.api.entity.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.annotations.EntityId;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityOwner;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityTitle;

/**
 * This is the model for a user's profile, used by the ProfileEntityProvider
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class UserProfile {

	@EntityId
	private String userUuid;
	
	@EntityTitle @EntityOwner
	private String displayName;
	private String nickname;
	private Date dateOfBirth;
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
	private String statusMessage;
	private Date statusDate;
	private String statusDateFormatted;
	
	/* 
	 * This is an EntityBroker URL that can be used to get directly to a user's profile image. URL is open, but privacy is still checked.
	 */
	private String imageUrl; 
	private String imageThumbUrl; 
	
	private Map<String, String> props;

	

	/**
	 * Basic constructor
	 */
	public UserProfile() {
	}
	
	public void setUserUuid(String userUuid) {
		this.userUuid = userUuid;
	}

	public String getUserUuid() {
		return userUuid;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
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

	public String getOtherInformation() {
		return otherInformation;
	}

	public void setOtherInformation(String otherInformation) {
		this.otherInformation = otherInformation;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}

	
	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void setImageThumbUrl(String imageThumbUrl) {
		this.imageThumbUrl = imageThumbUrl;
	}

	public String getImageThumbUrl() {
		return imageThumbUrl;
	}

	public void setStatusDateFormatted(String statusDateFormatted) {
		this.statusDateFormatted = statusDateFormatted;
	}

	public String getStatusDateFormatted() {
		return statusDateFormatted;
	}

	public void setProps(Map<String, String> props) {
		this.props = props;
	}

	public Map<String, String> getProps() {
		return props;
	}
	
	/* for setting properties into the props Map */
	public void setProperty(String key, String value) {
        if (props == null) {
            props = new HashMap<String, String>();
        }
        props.put(key, value);
    }

    public String getProperty(String key) {
        if (props == null) {
            return null;
        }
        return props.get(key);
    }
	
	
	
	
	
}
