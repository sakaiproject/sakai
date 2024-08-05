/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.service.api;

import java.util.List;

import org.sakaiproject.scorm.exceptions.ResourceNotDeletedException;
import org.sakaiproject.scorm.exceptions.ResourceStorageException;
import org.sakaiproject.scorm.model.api.ContentPackage;

import org.w3c.dom.Document;

public interface ScormContentService
{
	/**
	 * Gets the content package by PK
	 * @param contentPackageId
	 * @return
	 * @throws ResourceStorageException
	 */
	public ContentPackage getContentPackage(long contentPackageId) throws ResourceStorageException;

	/**
	 * Gets the content package by the resource its represents
	 * @param resourceId
	 * @return
	 * @throws ResourceStorageException
	 */
	public ContentPackage getContentPackageByResourceId(String resourceId) throws ResourceStorageException;

	/**
	 * Get all content packages in the current context
	 * @return
	 * @throws ResourceStorageException
	 */
	public List<ContentPackage> getContentPackages() throws ResourceStorageException;

	/**
	 * Get all content packages for the given site
	 * @param siteID the ID of the site for which all content packages are being requested
	 * @return
	 * @throws ResourceStorageException
	 */
	public List<ContentPackage> getContentPackages( String siteID ) throws ResourceStorageException;

	/**
	 * Gets the status of a package, one of ScormConstants#CONTENT_PACKAGE_STATUS_*
	 * @param contentPackage
	 * @return one of ScormConstants#CONTENT_PACKAGE_STATUS_*
	 */
	public int getContentPackageStatus(ContentPackage contentPackage);

	/**
	 * Extracts the title from the content package.
	 * @param document
	 * @return
	 */
	public String getContentPackageTitle(Document document);

	/**
	 * Flags content package to be deleted.
	 * @param contentPackageId
	 * @throws ResourceNotDeletedException
	 */
	public void removeContentPackage(long contentPackageId) throws ResourceNotDeletedException;

	/**
	 * Updates the content package in the persistent storage
	 * @param contentPackage
	 * @throws ResourceStorageException
	 */
	public void updateContentPackage(ContentPackage contentPackage) throws ResourceStorageException;

	/**
	 * Validates AND STORES the content package.
	 * @param resourceId
	 * @param encoding
	 * @param createContentPackage TODO
	 * @return
	 * @throws ResourceStorageException
	 */
	public int validate(String resourceId, String encoding) throws ResourceStorageException;

	/**
	 * Validates AND STORES the content package.
	 * @param resourceId
	 * @param isValidateToSchema
	 * @param encoding
	 * @param createContentPackage TODO
	 * @return
	 * @throws ResourceStorageException
	 */
	public int storeAndValidate(String resourceId, boolean isValidateToSchema, String encoding) throws ResourceStorageException;
}
