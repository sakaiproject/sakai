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
package org.sakaiproject.profile2.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.time.DateUtils;
import org.imgscalr.Scalr;

import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class ProfileUtils {

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
		
		//add more here as required, BUT also add them below. 
		//You will need to check ImageIO for the informal names.

		if(allowedTypes.contains(contentType)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Helper to get the informal format name that is used by ImageIO.
	 * We have access to the mimetype so we can map them.
	 * 
	 * <p>If no valid mapping is found, it will default to "jpg".
	 * 
	 * @param mimeType the mimetype of the original image, eg image/jpeg
	 */
	public static String getInformalFormatForMimeType(String mimeType){
		Map<String,String> formats = new HashMap<String,String>();
		formats.put("image/jpeg", "jpg");
		formats.put("image/gif", "gif");
		formats.put("image/png", "png");
		formats.put("image/x-png", "png");
		formats.put("image/pjpeg", "jpg");
		formats.put("image/jpg", "jpg");
		
		String format = formats.get(mimeType);
		
		if(format != null) {
			return format;
		}
		return "jpg";
	}
	
	
	public static byte[] scaleImage(byte[] imageData, int maxSize, String mimeType) {
		InputStream in = null;
		try {
			in = new ByteArrayInputStream(imageData);
			return scaleImage(in, maxSize, mimeType);
			
		} finally {
			if (in != null) {
				try {
					in.close();
					log.debug("Image stream closed."); 
				}
				catch (IOException e) {
					log.error("Error closing image stream: ", e); 
				}
			}
		}
	}
	/**
	 * Scale an image so it is fit within a give width and height, whilst maintaining its original proportions 
	 *
	 * @param imageData		bytes of the original image
	 * @param maxSize		maximum dimension in px
	 */
	public static byte[] scaleImage(InputStream in, int maxSize, String mimeType) {
		
		byte[] scaledImageBytes = null;
		try {
			//convert original image to inputstream
			
			//original buffered image
			BufferedImage originalImage = ImageIO.read(in);
			
			//scale the image using the imgscalr library
			BufferedImage scaledImage = Scalr.resize(originalImage, maxSize);
			
			//convert BufferedImage to byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(scaledImage, getInformalFormatForMimeType(mimeType), baos);
			baos.flush();
			scaledImageBytes = baos.toByteArray();
			baos.close();
			
		} catch (Exception e) {
			log.error("Scaling image failed.", e);
		}
		
		return scaledImageBytes;
	}
	
	/**
	 * Convert a Date into a String according to format, or, if format
	 * is set to null, do a current locale based conversion.
	 *
	 * @param date			date to convert
	 * @param format		format in SimpleDateFormat syntax. Set to null to force as locale based conversion.
	 */
	public static String convertDateToString(Date date, String format) {
		
		if(date == null || "".equals(format)) { 
			throw new IllegalArgumentException("Null Argument in Profile.convertDateToString()");	 
		}
		
        String dateStr = null;
        
        if(format != null) {
        	SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        	dateStr = dateFormat.format(date);
        } else {
        	// Since no specific format has been specced, we use the user's locale.
        	Locale userLocale = (new ResourceLoader()).getLocale();
        	DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, userLocale);
        	dateStr = formatter.format(date);
        }
        
        if(log.isDebugEnabled()) {
        	log.debug("Profile.convertDateToString(): Input date: " + date.toString()); 
        	log.debug("Profile.convertDateToString(): Converted date string: " + dateStr); 
        }

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
		} catch (ParseException e) {
			log.error("Profile.convertStringToDate() failed. " + e.getClass() + ": " + e.getMessage());  
			return null;
		}       
	}
	
	/**
	 * Strip the year from a given date (actually just sets it to 1)
	 * 
	 * @param date	original date
	 * @return
	 */
	public static Date stripYear(Date date){
		return DateUtils.setYears(date, 1);
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
		return WordUtils.capitalizeFully(input);
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
		
		//log.info("currentDate:" + currentTimeMillis);
		//log.info("postingDate:" + postingTimeMillis);
		//log.info("diff:" + diff);
		
		int MILLIS_IN_SECOND = 1000;
		int MILLIS_IN_MINUTE = 1000 * 60;
		int MILLIS_IN_HOUR = 1000 * 60 * 60;
		int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
		int MILLIS_IN_WEEK = 1000 * 60 * 60 * 24 * 7;
				
		if(diff < MILLIS_IN_SECOND) {
			//less than a second
			return Messages.getString("Label.just_then"); 
		} else if (diff < MILLIS_IN_MINUTE) {
			//less than a minute, calc seconds
			int numSeconds = diff/MILLIS_IN_SECOND;
			if(numSeconds == 1) {
				//one sec
				return Messages.getString("Label.second_ago", new Object[] {numSeconds}); 
			} else {
				//more than one sec
				return Messages.getString("Label.seconds_ago", new Object[] {numSeconds}); 
			}
		} else if (diff < MILLIS_IN_HOUR) {
			//less than an hour, calc minutes
			int numMinutes = diff/MILLIS_IN_MINUTE;
			if(numMinutes == 1) {
				//one minute
				return Messages.getString("Label.minute_ago", new Object[] {numMinutes}); 
			} else {
				//more than one minute
				return Messages.getString("Label.minutes_ago", new Object[] {numMinutes}); 
			}
		} else if (diff < MILLIS_IN_DAY) {
			//less than a day, calc hours
			int numHours = diff/MILLIS_IN_HOUR;
			if(numHours == 1) {
				//one hour
				return Messages.getString("Label.hour_ago", new Object[] {numHours}); 
			} else {
				//more than one hour
				return Messages.getString("Label.hours_ago", new Object[] {numHours}); 
			}
		} else if (diff < MILLIS_IN_WEEK) {
			//less than a week, calculate days
			int numDays = diff/MILLIS_IN_DAY;
			
			//now calculate which day it was
			if(numDays == 1) {
				return Messages.getString("Label.yesterday"); 
			} else {
				//set calendar and get day of week
				Calendar postingCal = Calendar.getInstance();
				postingCal.setTimeInMillis(postingTimeMillis);
				
				int postingDay = postingCal.get(Calendar.DAY_OF_WEEK);

				//set to localised value: 'on Wednesday' for example
				String dayName = getDayName(postingDay,locale);
				if(dayName != null) {
					return Messages.getString("Label.on", new Object[] {toProperCase(dayName)});
				}
			}
			
		} else {
			//over a week ago, we want it blank though.
		}

		return null;
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
	 * Gets the users preferred orientation, either from the user's session or Sakai preferences and returns it
	 * This depends on Sakai's ResourceLoader.
	 * 
	 * @return
	 */
	public static String getUserPreferredOrientation() {
		ResourceLoader rl = new ResourceLoader();
		return rl.getOrientation(rl.getLocale());
	}
	
	/**
	 * Creates a full profile event reference for a given reference
	 * @param ref
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
	 * @param str 		the String to split
	 * @param separator	separator character
	 * @return
	 */
	public static List<String> getListFromString(String str, char separator) {
		String[] items = StringUtils.split(str, separator);
		return Arrays.asList(items);
	}
	
	/**
	 * Processes HTML and escapes evils tags like &lt;script&gt;, also converts newlines to proper HTML breaks.
	 * @param s
	 * @return
	 */
	public static String processHtml(String s){
		return FormattedText.processFormattedText(s, new StringBuilder(), true, false);
	}
	
	/**
	 * Strips string of HTML and returns plain text.
	 * 
	 * @param s
	 * @return
	 */
	public static String stripHtml(String s) {
		return FormattedText.convertFormattedTextToPlaintext(s);
	}
	
	/**
	 * Strips string of HTML, escaping anything that is left to return plain text.
	 * 
	 * <p>Deals better with poorly formed HTML than just stripHtml and is best for XSS protection, not for storing actual data.
	 * 
	 * @param s The string to process
	 * @return
	 */
	public static String stripAndCleanHtml(String s) {
		//Attempt to strip HTML. This doesn't work on poorly formatted HTML though
		String stripped = FormattedText.convertFormattedTextToPlaintext(s);
		
		//so we escape anything that is left
		return StringEscapeUtils.escapeHtml(stripped);
	}
	
	/**
	 * Trims text to the given maximum number of displayed characters.
	 * Supports HTML and preserves formatting. 
	 * 
	 * @param s				 the string
	 * @param maxNumOfChars	 num chars to keep. If HTML, it's the number of content chars, ignoring tags.
	 * @param isHtml		 is the string HTML?
	 * @return
	 */
	public static String truncate(String s, int maxNumOfChars, boolean isHtml) {
		
		if (StringUtils.isBlank(s)) {
			return "";
		}
		
		//html
		if(isHtml) {
			StringBuilder trimmedHtml = new StringBuilder();
			FormattedText.trimFormattedText(s, maxNumOfChars, trimmedHtml);
			return trimmedHtml.toString();
		} 
		
		//plain text
		return StringUtils.substring(s, 0, maxNumOfChars);
		
	}
	
	/**
	 * Trims and abbreviates text to the given maximum number of displayed
	 * characters (less 3 characters, in case "..." must be appended).
	 * Supports HTML and preserves formatting.
	 * 
	 * @param s				 the string
	 * @param maxNumOfChars	 num chars to keep. If HTML, it's the number of content chars, ignoring tags.
	 * @param isHtml		 is the string HTML?
	 * @return
	 */
	public static String truncateAndAbbreviate(String s, int maxNumOfChars, boolean isHtml) {
		
		if (StringUtils.isBlank(s)) {
			return "";
		}
		
		//html
		if(isHtml) {
			StringBuilder trimmedHtml = new StringBuilder();
		
			boolean trimmed = FormattedText.trimFormattedText(s, maxNumOfChars - 3, trimmedHtml);
		
			if (trimmed) {
				int index = trimmedHtml.lastIndexOf("</");
				if (-1 != index) {
					trimmedHtml.insert(index, "...");
				} else {
					trimmedHtml.append("...");
				}
			}
			return trimmedHtml.toString();
		}
		
		//plain text
		return StringUtils.abbreviate(s, maxNumOfChars);
		
	}
	
	
	
	/**
	 * Generate a UUID
	 * @return
	 */
	public static String generateUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
			
	/**
	 * Returns the SkypeMe URL for the specified Skype username.
	 * 
	 * @param skypeUsername
	 * @return the SkypeMe URL for the specified Skype username.
	 */
	public static String getSkypeMeURL(String skypeUsername) {
		return "skype:" + skypeUsername + "?call";
	}
		
	/**
	 * Remove duplicates from a list, order is not retained.
	 * 
	 * @param list	list of objects to clean
	 */
	public static <T> void removeDuplicates(List<T> list){
		Set<T> set = new HashSet<T>();
		set.addAll(list);
		list.clear();
		list.addAll(set);
	}
	
	/**
	 * Remove duplicates from a list, order is retained.
	 *
	 * @param list	list of objects to clean
	 */
	public static <T> void removeDuplicatesWithOrder(List<T> list) {
		Set<T> set = new HashSet<T> ();
		List<T> newList = new ArrayList<T>();
		for(T e: list) {
			if (set.add(e)) {
				newList.add(e);
	    	}
		}
	    list.clear();
	    list.addAll(newList);
	}
	
	/**
	 * Calculate an MD5 hash of a string
	 * @param s	String to hash
	 * @return	MD5 hash as a String
	 */
	public static String calculateMD5(String s){
		return DigestUtils.md5Hex(s);
	}
	
	/**
	 * Creates a square avatar image by taking a segment out of the centre of the original image and resizing to the appropriate dimensions
	 * 
	 * @param imageData		original bytes of the image
	 * @param mimeType		mimetype of image
	 * @return
	 */
	public static byte[] createAvatar(byte[] imageData, String mimeType) {
		
		InputStream in = null;
		byte[] outputBytes = null;
		try {
			//convert original image to inputstream
			in = new ByteArrayInputStream(imageData);
			
			//original buffered image
			BufferedImage originalImage = ImageIO.read(in);
			
			//OPTION 1
			//determine the smaller side of the image and use that as the size of the cropped square
			//to be taken out of the centre
			//then resize to the avatar size =80 square.
			
			int smallestSide = originalImage.getWidth();
			if(originalImage.getHeight() < originalImage.getWidth()) {
				smallestSide = originalImage.getHeight();
			}
			
			if(log.isDebugEnabled()){
				log.debug("smallestSide:" + smallestSide);
			}
			
			int startX = (originalImage.getWidth() / 2) - (smallestSide/2);
			int startY = (originalImage.getHeight() / 2) - (smallestSide/2);
			
			//OPTION 2 (unused)
			//determine a percentage of the original image which we want to keep, say 90%.
			//then figure out the dimensions of the box and crop to that.
			//then resize to the avatar size =80 square.
					
			//int percentWidth = (originalImage.getWidth() / 100) * 90;
			//int startX = (originalImage.getWidth() / 2) - (percentWidth/2);
			//int percentHeight = (originalImage.getHeight() / 100) * 90;
			//int startY = (originalImage.getHeight() / 2) - (percentHeight/2);
			//log.debug("percentWidth:" + percentWidth);
			//log.debug("percentHeight:" + percentHeight);
			//so it is square, we can only use one dimension for both side, so choose the smaller one
			//int croppedSize = percentWidth;
			//if(percentHeight < percentWidth) {
			//	croppedSize = percentHeight;
			//}
			//log.debug("croppedSize:" + croppedSize);
		
			if(log.isDebugEnabled()){
				log.debug("originalImage.getWidth():" + originalImage.getWidth());
				log.debug("originalImage.getHeight():" + originalImage.getHeight());
				log.debug("startX:" + startX);
				log.debug("startY:" + startY);	
			}
			
			//crop to these bounds and starting positions
			BufferedImage croppedImage = Scalr.crop(originalImage, startX, startY, smallestSide, smallestSide);

			//now resize it to the desired avatar size
			BufferedImage scaledImage = Scalr.resize(croppedImage, 80);
			
			//convert BufferedImage to byte array
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(scaledImage, getInformalFormatForMimeType(mimeType), baos);
			baos.flush();
			outputBytes = baos.toByteArray();
			baos.close();
			
		} catch (Exception e) {
			log.error("Cropping and scaling image failed.", e);
		}
		
		finally {
			if (in != null) {
				try {
					in.close();
					log.debug("Image stream closed."); 
				}
				catch (IOException e) {
					log.error("Error closing image stream: ", e); 
				}
			}
		}
		
		return outputBytes;
	}
	
}
