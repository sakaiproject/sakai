package org.sakaiproject.springframework.orm.hibernate;

import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: Jul 25, 2007
 * Time: 10:35:45 AM
 * To change this template use File | Settings | File Templates.
 */
public interface VendorHbmTransformer {
    /**
     * Applies the vendor specific xsl transformation to the given mappingDoc.  If
     * no xsl file can be found for the current vendor, return the mapping unchanged.
     *
     * @param mappingDoc - mappingDoc
     * @return the new hbm
     */
    public InputStream getTransformedMapping(InputStream mappingDoc);

}
