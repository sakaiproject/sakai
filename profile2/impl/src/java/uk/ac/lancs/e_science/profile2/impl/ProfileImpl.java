package uk.ac.lancs.e_science.profile2.impl;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import uk.ac.lancs.e_science.profile2.api.Profile;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;



public class ProfileImpl implements Profile {

	private transient Logger log = Logger.getLogger(ProfileImpl.class);

	public ProfileImpl() {
	}

	public String getUserStatus(String userId) {
		return "this is my status";
	}
	
	public String getUserStatusLastUpdated(String userId) {
		return "on Monday";
	}
	
	
	public boolean checkContentTypeForProfileImage(String contentType) {
		
		ArrayList<String> allowedTypes = new ArrayList<String>();
		allowedTypes.add("image/jpeg");
		allowedTypes.add("image/gif");
		allowedTypes.add("image/png");

		if(allowedTypes.contains(contentType)) {
			return true;
		}
		
		return false;
	}
	
	
	
	public byte[] scaleImage (byte[] imageData, int maxSize) {
	
	    if (log.isDebugEnabled()) {
	    	log.debug("Scaling image...");
	    }
	    // Get the image from a file.
	    Image inImage = new ImageIcon(imageData).getImage();
	
	    // Determine the scale.
	    double scale = (double) maxSize / (double) inImage.getHeight(null);
	    if (inImage.getWidth(null) > inImage.getHeight(null)) {
	        scale = (double) maxSize / (double) inImage.getWidth(null);
	    }
	
	    // Determine size of new image.
	    // One of the dimensions should equal maxSize.
	    int scaledW = (int) (scale * inImage.getWidth(null));
	    int scaledH = (int) (scale * inImage.getHeight(null));
	
	    // Create an image buffer in which to paint on.
	    BufferedImage outImage = new BufferedImage(
	            scaledW, scaledH, BufferedImage.TYPE_INT_RGB
	        );
	
	    // Set the scale.
	    AffineTransform tx = new AffineTransform();
	
	    // If the image is smaller than the desired image size,
	    // don't bother scaling.
	    if (scale < 1.0d) {
	        tx.scale(scale, scale);
	    }
	
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
	    	if (log.isDebugEnabled()) {
	    		log.debug("Scaling done.");
	    	}
	    } catch (IOException e) {
	    	log.error("failed");
	    }
	    return os.toByteArray();
	}
	

	
	/*	
	
	
	public boolean setUserStatus(String userId, String status) {
		return true;
	}
	*/
	
}
