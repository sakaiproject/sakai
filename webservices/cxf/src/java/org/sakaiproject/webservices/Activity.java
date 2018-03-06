/**
 * Copyright (c) 2005-2016 The Apereo Foundation
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
package org.sakaiproject.webservices;

import java.util.Date;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.event.api.EventQueryService;
import org.sakaiproject.tool.api.Session;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 9/19/11
 * Time: 10:21 AM
 * To change this template use File | Settings | File Templates.
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
@Slf4j
public class Activity extends AbstractWebService {

    /**
     * Returns a list of events of a user between 2 dates. As this one requires 2 Date objects,
     * it is the one to be called as a normal webservice.
     * @param eid	is the user that we want to query
     * @param startDate limit the query ti these dates
     * @param endDate limit the query ti these dates
     * @return	String as the result of the Query in xml
     */
    @WebMethod
    @Path("/getUserActivity")
    @Produces("text/plain")
    @GET
    public String getUserActivity(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "startDate", partName = "startDate") @QueryParam("startDate") Date startDate,
            @WebParam(name = "endDate", partName = "endDate") @QueryParam("endDate") Date endDate) {

        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser()) {
            log.warn("WS getUserActivity(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS getUserActivity(): Permission denied. Restricted to super users.");
        }

        return eventQueryService.getUserActivity(eid, startDate, endDate);
    }

    /**
     * Returns a list of events of a user between 2 dates. This one allows to pass strings, so
     * this can be called as a Rest Service
     * @param eid	is the user that we want to query
     * @param startDateString limit the query ti these dates. In this case as String if we call it as a rest
     * @param endDateString limit the query ti these dates. In this case as String if we call it as a rest
     * @return	String as the result of the Query in xml
     */
    @WebMethod
    @Path("/getUserActivityRestVersion")
    @Produces("text/plain")
    @GET
    public String getUserActivityRestVersion(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "startDate", partName = "startDate") @QueryParam("startDate") String startDateString,
            @WebParam(name = "endDate", partName = "endDate") @QueryParam("endDate") String endDateString) {

        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser()) {
            log.warn("WS getUserActivityStringDates(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS getUserActivityStringDates(): Permission denied. Restricted to super users.");
        }

        return eventQueryService.getUserActivityRestVersion(eid, startDateString, endDateString);
    }

    /**
     * Returns the User's logon activity.
     * @param eid	is the user that we want to query
     * @return	String as the result of the Query in xml
     */

    @WebMethod
    @Path("/getUserLogonActivity")
    @Produces("text/plain")
    @GET
    public String getUserLogonActivity(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid) {

        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser()) {
            log.warn("WS getUserLogonActivity(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS getUserLogonActivity(): Permission denied. Restricted to super users.");
        }


        return eventQueryService.getUserLogonActivity(eid);
    }


    /**
     * Returns the User's activity filtered by one event type.
     * @param eid	is the user that we want to query
     * @param eventType the event type to filter
     * @return	String as the result of the Query in xml
     */

    @WebMethod
    @Path("/getUserActivityByType")
    @Produces("text/plain")
    @GET
    public String getUserActivityByType(
            @WebParam(name = "sessionid", partName = "sessionid") @QueryParam("sessionid") String sessionid,
            @WebParam(name = "eid", partName = "eid") @QueryParam("eid") String eid,
            @WebParam(name = "eventType", partName = "eventType") @QueryParam("eventType") String eventType) {

        Session session = establishSession(sessionid);

        if (!securityService.isSuperUser()) {
            log.warn("WS getUserActivityByType(): Permission denied. Restricted to super users.");
            throw new RuntimeException("WS getUserActivityByType(): Permission denied. Restricted to super users.");
        }

        return eventQueryService.getUserActivityByType(eid,eventType);
    }



    /** Dependency: EventQueryService. */
    /**
     * @return the EventQueryService collaborator.
     */
    protected EventQueryService eventQueryService;

    public void setEventQueryService(EventQueryService eventQueryService) {
        this.eventQueryService = eventQueryService;
    }
}
