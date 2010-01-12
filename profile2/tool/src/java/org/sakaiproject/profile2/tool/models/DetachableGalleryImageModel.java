package org.sakaiproject.profile2.tool.models;

import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.profile2.model.GalleryImage;

/**
 * IModel implementation for GalleryImage.
 */
public class DetachableGalleryImageModel extends LoadableDetachableModel {

	private static final long serialVersionUID = 1L;

	private final GalleryImage image;

	public DetachableGalleryImageModel(GalleryImage image) {
		this.image = image;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(image.getId()).hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (obj instanceof DetachableGalleryImageModel) {
			DetachableGalleryImageModel other = (DetachableGalleryImageModel) obj;
			return other.image.getId() == image.getId();
		}
		return false;
	}

	@Override
	protected GalleryImage load() {
		return image;
	}
}
