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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.imgscalr.Scalr;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.api.FormattedText;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProfileUtils {

	private static Map<String, String> formats = Map.of(
		"image/jpeg", "jpg",
		"image/gif", "gif",
		"image/png", "png",
		"image/x-png", "png",
		"image/pjpeg", "jpg",
		"image/jpg", "jpg"
	);

	private static String getInformalFormatForMimeType(String mimeType) {

		String format = ProfileUtils.formats.get(mimeType);
		return format != null ? format : "jpg";
	}

	public static byte[] scaleImage(byte[] imageData, int maxSize, String mimeType) {

		try (InputStream in = new ByteArrayInputStream(imageData)) {
			return scaleImage(in, maxSize, mimeType);
		} catch (IOException e) {
			log.error("Error scaling image: ", e.toString());
		}
		return null;
	}

	/**
	 * Scale an image so it is fit within a give width and height, whilst maintaining its original proportions 
	 *
	 * @param imageData		bytes of the original image
	 * @param maxSize		maximum dimension in px
	 */
	private static byte[] scaleImage(InputStream in, int maxSize, String mimeType) {
		
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
	 * is set to null, use DateFormat.MEDIUM.
	 * Do not use locale to preserve old behavior
	 *
	 * @param date			date to convert
	 * @param format		format in SimpleDateFormat syntax. Set to null to force DateFormat.MEDIUM.
	 */
	public static String convertDateToString(Date date, String format) {
		return convertDateToString(date, format, false);
	}
	
	/**
	 * Convert a Date into a String according to format, or, if format
	 * is set to null, use DateFormat.MEDIUM and also force using locale.
	 *
	 * @param date			date to convert
	 * @param format		format in SimpleDateFormat syntax. Set to null to force DateFormat.MEDIUM and locale.
	 * @param useLocale		Use the users locale, added a default to reduce chance of regression for code that was expecting the previous format when format 
     *                      was not set.
	 */
	public static String convertDateToString(Date date, String format, boolean useLocale) {
		
		if(date == null || "".equals(format)) { 
			throw new IllegalArgumentException("Null Argument in Profile.convertDateToString()");	 
		}
		
        String dateStr = null;
		DateFormat formatter;
        
        Locale userLocale = (new ResourceLoader()).getLocale();
        if(format != null) {
            if (useLocale == false) {
                formatter = new SimpleDateFormat(format);
            }
            else {
                formatter = new SimpleDateFormat(format, userLocale);
            }
        } else {
        	formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, userLocale);
        }
        dateStr = formatter.format(date);
        
        if(log.isDebugEnabled()) {
        	log.debug("Profile.convertDateToString(): Input date: " + date.toString()); 
        	log.debug("Profile.convertDateToString(): Converted date string: " + dateStr); 
        }

		return dateStr;
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
		return ComponentManager.get(FormattedText.class).processFormattedText(s, null, true, false);
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
			ComponentManager.get(FormattedText.class).trimFormattedText(s, maxNumOfChars, trimmedHtml);
			return trimmedHtml.toString();
		} 
		
		//plain text
		return StringUtils.substring(s, 0, maxNumOfChars);
		
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
