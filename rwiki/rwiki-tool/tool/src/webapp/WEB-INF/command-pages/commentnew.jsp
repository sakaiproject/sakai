<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
-->
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:fn="http://java.sun.com/jsp/jstl/functions"
  ><jsp:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
    /><jsp:text
    ><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  
  <c:set var="currentRWikiObject" value="${requestScope.rsacMap.currentRWikiObject}"/>
  <c:set var="rlb" value="${requestScope.rsacMap.resourceLoaderBean}"/>
  
  <div class="modal fade" id="rwiki-newcomment-modal" tabindex="-1" aria-labelledby="rwiki-newcomment-label" aria-hidden="true">
    <form action="?#" method="post" >
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="rwiki-newcomment-label">${rlb.jsp_new_comment}</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="${rlb.jsp_close}"></button>
          </div>
          <div class="modal-body">
            <nobr><label for="wiki-textarea-content"><c:out value="${rlb.jsp_new_comment}"/></label></nobr><br/>
            <textarea cols="40" rows="10" name="content" id="wiki-textarea-content" >&#160;</textarea>
            <input type="hidden" name="action" value="commentnewsave"/>
            <input type="hidden" name="panel" value="Main"/>
            <input type="hidden" name="version" value="${currentRWikiObject.version.time}"/>
            <jsp:element name="input">
              <jsp:attribute name="type">hidden</jsp:attribute>
              <jsp:attribute name="name">pageName</jsp:attribute>
              <jsp:attribute name="value"><c:out value="${currentRWikiObject.name}" escapeXml="true"/></jsp:attribute>
            </jsp:element>
            <jsp:element name="input">
              <jsp:attribute name="type">hidden</jsp:attribute>
              <jsp:attribute name="name">realm</jsp:attribute>
              <jsp:attribute name="value"><c:out value="${currentRWikiObject.realm}" escapeXml="true"/></jsp:attribute>
            </jsp:element>
          </div>
          <div class="modal-footer">
            <button type="submit" name="save" class="btn btn-primary">${rlb.jsp_button_save}</button>
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">${rlb.jsp_button_cancel}</button>
          </div>
        </div>
      </div>
    </form>
  </div>
</jsp:root>
