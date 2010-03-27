package org.sakaiproject.springframework.orm.hibernate.impl;

import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.springframework.orm.hibernate.VendorHbmTransformer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jul 25, 2007
 * Time: 10:26:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class VendorHbmTransformerImpl implements VendorHbmTransformer {
    protected final transient Log logger = LogFactory.getLog(getClass());
 
    private Map vendorTransforms = new HashMap();

    /**
     * Applies the vendor specific xsl transformation to the given mappingDoc.  If
     * no xsl file can be found for the current vendor, return the mapping unchanged.
     *
     * @param mappingDoc - mappingDoc
     * @return
     */
    public InputStream getTransformedMapping(InputStream mappingDoc) {
        InputStream xsl = null;
        try {
            try {
                xsl = loadTransformStream();
            } catch (IOException e) {
               logger.error("problem loading xsl file, leaving hbm unchanged", e);
                return mappingDoc;
            }

            // no xsl for this vendor, so nothing to transform
          if (xsl == null) {
              logger.info("no vendor specific xsl, returning hbm unchanged");
              return mappingDoc;
          }

          try {
             ByteArrayOutputStream transformedMapping = new ByteArrayOutputStream();
             Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(xsl));
             transformer.transform(new StreamSource(mappingDoc), new StreamResult(transformedMapping));
             logger.debug(new String(transformedMapping.toByteArray()));
             return new ByteArrayInputStream(transformedMapping.toByteArray());
          } catch (Exception e) {
                logger.error("Problem applying transformation to hibernate mapping file, processing hbm unchanged", e);
                return mappingDoc;
          }
        } finally {
            
                if (xsl != null)
					try {
						xsl.close();
					} catch (IOException e) {
					}
            

        }
    }

    protected InputStream loadTransformStream() throws IOException {
        String resourcePath = (String) getVendorTransforms().get(SqlService.getVendor());
        if (resourcePath == null) {
            return null;
        }

        Resource resource = new ClassPathResource(resourcePath.trim());

        logger.info("Transforming hibernate mapping file using: " + resource.getFilename());

        return resource.getInputStream();
    }

   
    public Map getVendorTransforms() {
        return vendorTransforms;
    }

    public void setVendorTransforms(Map vendorTransforms) {
        this.vendorTransforms = vendorTransforms;
    }
}
