package org.sakaiproject.profile2.tool.dataproviders;

import java.util.Iterator;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.sakaiproject.profile2.model.GalleryImage;
import org.sakaiproject.profile2.tool.Locator;
import org.sakaiproject.profile2.tool.models.DetachableGalleryImageModel;

/**
 * IDataProvider implementation for retrieving gallery images.
 */
public class GalleryImageDataProvider implements IDataProvider {

	private static final long serialVersionUID = 1L;

	private String userId;

	public GalleryImageDataProvider(String userId) {
		if (userId == null || userId.equals("")) {
			throw new IllegalArgumentException("userId must be specified");
		}

		this.userId = userId;
	}

	public Iterator<GalleryImage> iterator(int first, int count) {	
		return Locator.getProfileImageService().getProfileGalleryImages(userId)
				.subList(first, first + count).iterator();
	}

	public IModel model(Object object) {
		if (!(object instanceof GalleryImage)) {
			throw new IllegalArgumentException(
					"object not an instance of GalleryImage");
		}

		return new DetachableGalleryImageModel((GalleryImage) object);
	}

	public int size() {
		return Locator.getProfileImageService().getProfileGalleryImages(userId)
				.size();
	}

	public void detach() {

	}

}
