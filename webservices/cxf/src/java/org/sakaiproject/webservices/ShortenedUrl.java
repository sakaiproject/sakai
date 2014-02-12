package org.sakaiproject.webservices;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.api.Session;

/**
 * A set of AXis web services for ShortenUrlService
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 */

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class ShortenedUrl extends AbstractWebService {

    private static final Log LOG = LogFactory.getLog(ShortenedUrl.class);

    /**
     * Shorten a URL. Optionally specify the secure property to get a longer key, 22 chars vs 6.
     *
     * @param sessionid        id of a valid session
     * @param url            the url to shorten
     * @param secure        whether or not use 'secure' urls. Secure urls are much longer than normal shortened URLs (22 chars vs 6 chars).
     * Must be a value of "true" if you want this, any other value will be false.
     * @return the shortened url, or an empty string if errors. Note that if you have not configured the ShortenedUrlService in sakai.properties, then you will get the original url. Ie it is a no-op.
     * To configure ShortenedUrlService, see: https://confluence.sakaiproject.org/display/SHRTURL/Home
     */
    @WebMethod
    @Path("/shortenWithSecurity")
    @Produces("text/plain")
    @GET
    public String shortenWithSecurity(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "url", partName = "url") @QueryParam("url") String url,
            @WebParam(name = "secure", partName = "secure") @QueryParam("secure") String secure) {

        Session session = establishSession(sessionid);

        boolean isSecure = Boolean.parseBoolean(secure);

        try

        {
            return shortenedUrlService.shorten(url, isSecure);
        } catch (
                Exception e
                )

        {
            LOG.warn("WS shorten(): " + e.getClass().getName() + " : " + e.getMessage());
            return "";
        }

    }

    /**
     * Shorten a URL.
     *
     * @param sessionid id of a valid session
     * @param url       the url to shorten
     * @return the shortened url, or an empty string if errors. Note that if you have not configured the ShortenedUrlService in sakai.properties, then you will get the original url. Ie it is a no-op.
     * To configure ShortenedUrlService, see: https://confluence.sakaiproject.org/display/SHRTURL/Home
     */
    @WebMethod
    @Path("/shorten")
    @Produces("text/plain")
    @GET
    public String shorten(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "url", partName = "url") @QueryParam("url") String url) {
        return shortenWithSecurity(sessionid, url, "false");
    }


}
