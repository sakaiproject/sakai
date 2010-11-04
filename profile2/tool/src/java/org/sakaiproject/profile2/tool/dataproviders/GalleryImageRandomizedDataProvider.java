/**
 * 
 */
package org.sakaiproject.profile2.tool.dataproviders;

import java.util.Iterator;

import org.sakaiproject.profile2.model.GalleryImage;

/**
 * Extension of <code>GalleryImageDataProvider</code> that retrieves gallery
 * images in randomized order.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class GalleryImageRandomizedDataProvider extends
		GalleryImageDataProvider {

	private static final long serialVersionUID = 1L;

	public GalleryImageRandomizedDataProvider(String userUuid) {
		super(userUuid);
	}

	public Iterator<GalleryImage> iterator(int first, int count) {
		return imageLogic.getGalleryImagesRandomized(userUuid).subList(first,
				first + count).iterator();
	}
}
