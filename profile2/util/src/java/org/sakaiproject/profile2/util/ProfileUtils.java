package org.sakaiproject.profile2.util;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jasypt.util.text.BasicTextEncryptor;
import org.sakaiproject.util.ResourceLoader;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ProfileUtils {

	private static final Logger log = Logger.getLogger(ProfileUtils.class);


	/**
	 * Check content type against allowed types. only JPEG,GIF and PNG are support at the moment
	 *
	 * @param contentType		string of the content type determined by some image parser
	 */
	public static boolean checkContentTypeForProfileImage(String contentType) {
		
		ArrayList<String> allowedTypes = new ArrayList<String>();
		allowedTypes.add("image/jpeg");
		allowedTypes.add("image/gif");
		allowedTypes.add("image/png");
		//Adding MIME types that Internet Explorer returns PRFL-98
		allowedTypes.add("image/x-png");
		allowedTypes.add("image/pjpeg");
		allowedTypes.add("image/jpg");

		if(allowedTypes.contains(contentType)) {
			return true;
		}
		
		return false;
	}
	
	

	/**
	 * Scale an image so one side is a maximum of maxSize in pixels.
	 *
	 * @param imageData		bytes of the original image
	 * @param maxSize		maximum dimension in px that the image should have on any one side
	 */
	public static byte[] scaleImage(byte[] imageData, int maxSize) {
	
	    log.debug("Scaling image..."); 
	
	    // Get the image 
	    Image inImage = new ImageIcon(imageData).getImage();
	
	    // Determine the scale (we could change this to only determine scale from one dimension, ie the width only?)
	    double scale = (double) maxSize / (double) inImage.getHeight(null);
	    if (inImage.getWidth(null) > inImage.getHeight(null)) {
	        scale = (double) maxSize / (double) inImage.getWidth(null);
	    }
	    
	    /*
	    log.debug("===========Image scaling============");
	    log.debug("WIDTH: " + inImage.getWidth(null));
	    log.debug("HEIGHT: " + inImage.getHeight(null));
	    log.debug("SCALE: " + scale);
	    log.debug("========End of image scaling========");
	    */
	
	    //if image is smaller than desired image size (ie scale is larger) just return the original image bytes
	    if (scale >= 1.0d) {
	    	return imageData;
	    }
	    
	    
	
	    // Determine size of new image.
	    // One of the dimensions should equal maxSize.
	    int scaledW = (int) (scale * inImage.getWidth(null));
	    int scaledH = (int) (scale * inImage.getHeight(null));
	
	    // Create an image buffer in which to paint on.
	    BufferedImage outImage = new BufferedImage(scaledW, scaledH, BufferedImage.TYPE_INT_RGB);
	
	    // Set the scale.
	    AffineTransform tx = new AffineTransform();
	
	    //scale
	    tx.scale(scale, scale);
	
	    // Paint image.
	    Graphics2D g2d = outImage.createGraphics();
	    g2d.setRenderingHint(
	            RenderingHints.KEY_ANTIALIASING,
	            RenderingHints.VALUE_ANTIALIAS_ON
	        );
	    g2d.drawImage(inImage, tx, null);
	    g2d.dispose();
	
	    // JPEG-encode the image
	    // and write to file.
	    ByteArrayOutputStream os = new ByteArrayOutputStream();
	    try { 
	    	JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
	    	encoder.encode(outImage);
	    	os.close();
	    	log.debug("Scaling done."); 
	    } catch (IOException e) {
	    	log.error("Scaling image failed."); 
	    }
	    return os.toByteArray();
	}
	
	/**
	 * Convert a Date into a String according to format
	 *
	 * @param date			date to convert
	 * @param format		format in SimpleDateFormat syntax
	 */
	public static String convertDateToString(Date date, String format) {
		
		if(date == null || "".equals(format)) { 
			throw new IllegalArgumentException("Null Argument in Profile.convertDateToString()");	 
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        String dateStr = dateFormat.format(date);
        
        log.debug("Profile.convertDateToString(): Input date: " + date.toString()); 
        log.debug("Profile.convertDateToString(): Converted date string: " + dateStr); 

		return dateStr;
	}
	
	
	/**
	 * Convert a string into a Date object (reverse of above
	 *
	 * @param dateStr		date string to convert
	 * @param format		format of the input date in SimpleDateFormat syntax
	 */
	public static Date convertStringToDate(String dateStr, String format) {
		if("".equals(dateStr) || "".equals(format)) {  
			throw new IllegalArgumentException("Null Argument in Profile.convertStringToDate()");	 
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		 
		try {
			Date date = dateFormat.parse(dateStr);
			
	        log.debug("Profile.convertStringToDate(): Input date string: " + dateStr); 
	        log.debug("Profile.convertStringToDate(): Converted date: " + date.toString()); 
			return date;
		} catch (Exception e) {
			log.error("Profile.convertStringToDate() failed. " + e.getClass() + ": " + e.getMessage());  
			return null;
		}       
	}
	
	/**
	 * Get the localised name of the day (ie Monday for en, Maandag for nl)
	 * @param day		int according to Calendar.DAY_OF_WEEK
	 * @param locale	locale to render dayname in
	 * @return
	 */
	public static String getDayName(int day, Locale locale) {
		
		//localised daynames
		String dayNames[] = new DateFormatSymbols(locale).getWeekdays();
		String dayName = null;
		
		try {
			dayName = dayNames[day];
		} catch (Exception e) {
			log.error("Profile.getDayName() failed. " + e.getClass() + ": " + e.getMessage());
		}
		return dayName;
	}
	
	
	/**
	 * Convert a string to propercase. ie This Is Proper Text
	 * @param input		string to be formatted
	 * @return
	 */
	public static String toProperCase(String input) {
		if (input == null || input.trim().length() == 0) {
			return input;
		}
		String output = input.toLowerCase();
		return output.substring(0, 1).toUpperCase() + output.substring(1);
	}
	
	
	/**
	 * Convert a date into a field like "just then, 2 minutes ago, 4 hours ago, yesterday, on sunday, etc"
	 *
	 * @param date		date to convert
	 */
	public static String convertDateForStatus(Date date) {

		//current time
		Calendar currentCal = Calendar.getInstance();
		long currentTimeMillis = currentCal.getTimeInMillis();
		
		//posting time
		long postingTimeMillis = date.getTime();
		
		//difference
		int diff = (int)(currentTimeMillis - postingTimeMillis);
		
		Locale locale = getUserPreferredLocale();
		
		//System.out.println("currentDate:" + currentTimeMillis);
		//System.out.println("postingDate:" + postingTimeMillis);
		//System.out.println("diff:" + diff);
		
		int MILLIS_IN_SECOND = 1000;
		int MILLIS_IN_MINUTE = 1000 * 60;
		int MILLIS_IN_HOUR = 1000 * 60 * 60;
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
		int MILLIS_IN_WEEK = 1000 * 60 * 60 * 24 * 7;

		String message=""; 
				
		if(diff < MILLIS_IN_SECOND) {
			//less than a second
			message = Messages.getString("Label.just_then"); 
		} else if (diff < MILLIS_IN_MINUTE) {
			//less than a minute, calc seconds
			int numSeconds = diff/MILLIS_IN_SECOND;
			if(numSeconds == 1) {
				//one sec
				message = numSeconds + Messages.getString("Label.second_ago"); 
			} else {
				//more than one sec
				message = numSeconds + Messages.getString("Label.seconds_ago"); 
			}
		} else if (diff < MILLIS_IN_HOUR) {
			//less than an hour, calc minutes
			int numMinutes = diff/MILLIS_IN_MINUTE;
			if(numMinutes == 1) {
				//one minute
				message = numMinutes + Messages.getString("Label.minute_ago"); 
			} else {
				//more than one minute
				message = numMinutes + Messages.getString("Label.minutes_ago"); 
			}
		} else if (diff < MILLIS_IN_DAY) {
			//less than a day, calc hours
			int numHours = diff/MILLIS_IN_HOUR;
			if(numHours == 1) {
				//one hour
				message = numHours + Messages.getString("Label.hour_ago"); 
			} else {
				//more than one hour
				message = numHours + Messages.getString("Label.hours_ago"); 
			}
		} else if (diff < MILLIS_IN_WEEK) {
			//less than a week, calculate days
			int numDays = diff/MILLIS_IN_DAY;
			
			//now calculate which day it was
			if(numDays == 1) {
				message = Messages.getString("Label.yesterday"); 
			} else {
				//set calendar and get day of week
				Calendar postingCal = Calendar.getInstance();
				postingCal.setTimeInMillis(postingTimeMillis);
				
				int postingDay = postingCal.get(Calendar.DAY_OF_WEEK);

				//set to localised value: 'on Wednesday' for example
				String dayName = getDayName(postingDay,locale);
				if(dayName != null) {
					message = Messages.getString("Label.on") + toProperCase(dayName); 
				}
			}
			
		} else {
			//over a week ago, we want it blank though.
		}

		return message;
	}
	
	/**
	 * Gets the users preferred locale, either from the user's session or Sakai preferences and returns it
	 * This depends on Sakai's ResourceLoader.
	 * 
	 * @return
	 */
	public static Locale getUserPreferredLocale() {
		ResourceLoader rl = new ResourceLoader();
		return rl.getLocale();
	}
	
	/**
	 * Creates a full profile event reference for a give reference
	 * @param eventId
	 * @return
	 */
	public static String createEventRef(String ref) {
		return "/profile/"+ref;
	}
	
	/**
	 * Method for getting a value from a map based on the given key, but if it does not exist, use the given default
	 * @param map
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static Object getValueFromMapOrDefault(Map<?,?> map, Object key, Object defaultValue) {
		return (map.containsKey(key) ? map.get(key) : defaultValue);
	}
	
	/**
	 * Method to chop a String into it's parts based on the separator and return as a List. Useful for multi valued Sakai properties
	 * @param s 	the String to split
	 * @param separator	separator character
	 * @return
	 */
	public static List<String> getListFromString(String str, char separator) {
		String[] items = StringUtils.split(str, separator);
		return Arrays.asList(items);
	}
	
	/**
	 * Decrypt the string
	 * @param encryptedText
	 * @return
	 */
	public static String decrypt(final String encryptedText) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(BASIC_ENCRYPTION_KEY);
		return(textEncryptor.decrypt(encryptedText));
	}
	
	/**
	 * Encrypt the string
	 * @param plainText
	 * @return
	 */
	public static String encrypt(final String plainText) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(BASIC_ENCRYPTION_KEY);
		return(textEncryptor.encrypt(plainText));
	}
	
	
	/**
	 * The user's password needs to be decrypted and sent to Twitter for updates
	 * so we can't just one-way encrypt it. 
	 * 
	 * Note to casual observers:
	 * Having just this key won't allow you to decrypt a password. 
	 * No two encryptions are the same using the encryption method that Profile2 uses 
	 * but they decrypt to the same value which is why we can use it.
	 */
	private static final String BASIC_ENCRYPTION_KEY = "AbrA_ca-DabRa.123"; 
	
}
