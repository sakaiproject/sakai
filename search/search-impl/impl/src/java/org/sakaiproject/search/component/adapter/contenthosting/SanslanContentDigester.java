/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata.GpsInfo;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfo;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SanslanContentDigester extends BaseContentDigester {

    public String getContent(ContentResource contentResource) {
        log.debug("digesting: {}", contentResource.getId());
        
        InputStream contentStream = null;
        ImageMetadata metadata = null;

        try {
            contentStream = contentResource.streamContent();
            ResourceProperties resourceProperties = contentResource.getProperties();
            String fileName = resourceProperties.getProperty(resourceProperties.getNamePropDisplayName());
            
            // Start with a StringBuffer that includes the filename for searching
            StringBuffer sb = new StringBuffer();
            sb.append(fileName).append("\n");
            
            try {
                metadata = Imaging.getMetadata(contentStream, fileName);
                if (metadata instanceof JpegImageMetadata) {
                    JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
                    sb.append(getFieldValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MAKE));
                    sb.append(getFieldValue(jpegMetadata, TiffTagConstants.TIFF_TAG_MODEL));
                    sb.append(getFieldValue(jpegMetadata, TiffTagConstants.TIFF_TAG_ARTIST));
                    sb.append(getFieldValue(jpegMetadata, ExifTagConstants.EXIF_TAG_USER_COMMENT));
                    //get the GPS info 
                    //TODO this should go in its own field in the index
                    TiffImageMetadata exifMetadata = jpegMetadata.getExif();
                    if (exifMetadata != null) {
                        try {
                            GpsInfo gpsInfo = exifMetadata.getGpsInfo();
                            if (null != gpsInfo) {
                                double longitude = gpsInfo.getLongitudeAsDegreesEast();
                                double latitude = gpsInfo.getLatitudeAsDegreesNorth();
                                sb.append("GPS Description: " + gpsInfo + "\n");
                                sb.append("GPS Longitude: " + longitude + "\n");
                                sb.append("GPS Latitude: " + latitude + "\n");
                            }
                        } catch (ImagingException e) {
                            log.error(e.getMessage(), e);
                        }
                    }    
                }
                log.debug("got metadata: {}", sb.toString());
                return sb.toString();
            } catch (IOException e) {
                log.error("Failed to extract metadata from image file {}, returning only filename for searchability", fileName, e);
                // Even if we can't get metadata, return the filename for searchability
                return sb.toString();
            }
        } catch (ServerOverloadException e) {
            log.error(e.getMessage(), e);
        }
        
        return null;
    }

    private String getFieldValue(JpegImageMetadata metadata,
            TagInfo tagInfo) {
            TiffField field = metadata.findExifValue(tagInfo);
            if (field == null) {
               return "";
            } else {
                return(tagInfo.name + ": " +
                    field.getValueDescription() + "\n");
            }
    }

    public Reader getContentReader(ContentResource contentResource) {
        return new StringReader(this.getContent(contentResource));
    }

}
