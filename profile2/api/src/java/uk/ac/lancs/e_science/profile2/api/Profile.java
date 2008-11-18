package uk.ac.lancs.e_science.profile2.api;

import java.util.Date;



public interface Profile {
	
	
	public String getUserStatus(String userId);
	public String getUserStatusLastUpdated(String userId);
	
	public boolean checkContentTypeForProfileImage(String contentType);

	public byte[] scaleImage (byte[] imageData, int maxSize);
	
	public Date convertStringToDate(String dateStr);
	
	public String convertDateToString(Date date);

	
}
