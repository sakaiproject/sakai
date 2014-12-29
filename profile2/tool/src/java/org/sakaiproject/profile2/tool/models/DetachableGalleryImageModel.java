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
package org.sakaiproject.profile2.tool.models;

import org.apache.wicket.model.LoadableDetachableModel;
import org.sakaiproject.profile2.model.GalleryImage;

/**
 * IModel implementation for GalleryImage.
 */
public class DetachableGalleryImageModel extends LoadableDetachableModel<GalleryImage> {

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
