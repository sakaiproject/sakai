/**********************************************************************************
 *
 * Copyright (c) 2015 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.util;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.assessment.data.dao.grading.MediaData;
import org.sakaiproject.tool.assessment.facade.AssessmentGradingFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.services.PersistenceService;

/**
 * A utility service to convert media blobs from database storage to using
 * Content Hosting.
 *
 * This conversion is run at startup if the samigo.mediaConvert property is set
 * to true. It uses batching and marking to be rather safe in its operation. If
 * there are errors, they are reported in the usual log (catalina.out unless
 * configured otherwise).
 */
@Slf4j
public class MediaContentConverter {

	private static final String CONVERT_MEDIA_PROP = "samigo.convertMedia";

	private PersistenceService persistenceService;
	private AssessmentGradingFacadeQueriesAPI gq;
	private int recordsConverted = 0;
	private int recordsNotMarked = 0;
	private int recordsInError = 0;

	public void init() {
		boolean convertMedia = ServerConfigurationService.getBoolean(CONVERT_MEDIA_PROP, false);

		if (convertMedia) {
			gq = persistenceService.getAssessmentGradingFacadeQueries();
			convert();
		}
	}

	/**
	 * Convert MediaData objects with blobs in the database to use Content
	 * Hosting.
	 */
	public void convert() {
		log.info("Starting Samigo Media Conversion...");
		List<Long> ids = gq.getMediaConversionBatch();
		if (ids.isEmpty()) {
			String summary = outstandingSummary();
			if ("".equals(summary)) {
				log.info("No remaining Media to convert.");
			} else {
				log.info("No Media can be converted, but there are outstanding errors:\n" + outstandingSummary());
			}
		} else {
			while (!ids.isEmpty()) {
				gq.markMediaForConversion(ids);
				for (Long mediaId : ids) {
					convertMedia(mediaId);
				}
				log.info("Samigo Media Conversion in progress... " + summary());
				ids = gq.getMediaConversionBatch();
			}
			log.info("Samigo Media Conversion finished... " + summary());
			log.info(outstandingSummary());
		}
	}

	private void convertMedia(Long mediaId) {
		try {
			MediaData mediaData = gq.getMedia(mediaId);
			if ("CONVERTING".equals(mediaData.getLocation())) {
				mediaData.setLocation(null);
				gq.saveMedia(mediaData);
				log.debug("MediaData converted with ID: " + mediaId);
				recordsConverted++;
			} else {
				log.debug("MediaData could not be marked as in progress, ID: " + mediaId);
				recordsNotMarked++;
			}
		} catch (Exception e) {
			recordsInError++;
			log.warn("Error converting MediaData with ID: " + mediaId, e);
		}
	}

	private String summary() {
		return String.format("%d records converted, %d records unsuccessfully processed", recordsConverted,
				recordsInError + recordsNotMarked);
	}

	private String outstandingSummary() {
		List<Long> withBoth = gq.getMediaWithDataAndLocation();
		List<Long> inProgress = gq.getMediaInConversion();
		if (withBoth.size() + inProgress.size() > 0) {
			String message = "%d records remaining with data and location, %d records remaining marked as in progress\n"
					+ "\tIDs with both fields: %s\n"
					+ "\tIDs marked in progress: %s";
			return String.format(message, withBoth.size(), inProgress.size(), withBoth, inProgress);
		}
		return "";
	}

	public PersistenceService getPersistenceService() {
		return persistenceService;
	}

	public void setPersistenceService(PersistenceService persistenceService) {
		this.persistenceService = persistenceService;
	}

}
