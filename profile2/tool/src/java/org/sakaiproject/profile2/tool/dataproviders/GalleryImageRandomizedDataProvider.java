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
