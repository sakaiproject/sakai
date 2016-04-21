<%--

    Copyright 2008 Sakaiproject Licensed under the
    Educational Community License, Version 2.0 (the "License"); you may
    not use this file except in compliance with the License. You may
    obtain a copy of the License at

    http://www.osedu.org/licenses/ECL-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an "AS IS"
    BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
    or implied. See the License for the specific language governing
    permissions and limitations under the License.

--%>
<%@ page contentType="text/html" isELIgnored="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<jsp:directive.include file="/WEB-INF/jsp/header.jsp" />

<div class="portletBody">

<h2><spring:message code="siterole.title" /></h2>

<div class="permsMessages">
  <c:if test="${not empty(messages)}">
  <div class="messageInformation">
      <c:forEach var="message" items="${messages}" varStatus="counter">
      <ul style="margin:0px;">
          <li>${message}</li>
      </ul>
      </c:forEach>
  </div>
  </c:if>
  <c:if test="${not empty(errors)}">
  <div class="messageError">
      <c:forEach var="message" items="${errors}" varStatus="counter">
      <ul style="margin:0px;">
          <li>${message}</li>
      </ul>
      </c:forEach>
  </div>
  </c:if>
</div>

<div class="instructions clear"><spring:message code="siterole.instructions" /></div>

<div>
    <form id="actionForm" method="post" style="margin: 0px;">
        <div style="padding: 5px;">
            <input name="addPerms" type="submit" value="<spring:message code="siterole.add.command" />" />
            <input name="removePerms" type="submit" value="<spring:message code="siterole.remove.command" />" />
        </div>

        <div style="width:99%" class="permsControls">
            <div style="float:left;width:50%;">
              <fieldset>
              <legend><span><spring:message code="siterole.sitetype.title" /></span></legend>
              <div class="permsInstruction"><em><spring:message code="siterole.sitetype.header" /></em></div>
              <ul style="margin: 0px;">
                  <c:forEach var="siteType" items="${siteTypes}" varStatus="counter">
                  <li class="checkbox">
                      <input id="siteTypeSelect-${siteType}" type="checkbox" name="site-type" value="${siteType}" />
                      <label for="siteTypeSelect-${siteType}">${siteType}</label>
                  </li>
                  </c:forEach>
              </ul>
              </fieldset>

              <fieldset>
              <legend><span><spring:message code="siterole.role.title" /></span></legend>
              <div class="permsInstruction"><em><spring:message code="siterole.role.header" /></em></div>
              <ul style="margin: 0px;">
                  <c:forEach var="role" items="${roles}" varStatus="counter">
                  <li class="checkbox">
                      <input id="roleSelect-${role}" type="checkbox" name="site-role" value="${role}" />
                      <label for="roleSelect-${role}">${role}</label>
                  </li>
                  </c:forEach>
                  <c:if test="${not empty additionalRoles}">
					<c:set var="lastGroupRole" value="-none-"/>
					<c:forEach var="additionalrole" items="${additionalRoles}" varStatus="counter">
						<c:if test="${lastGroupRole != additionalrole.groupId}">
							<li><hr/></li>
						</c:if>
						<li class="checkbox">
							<input id="roleSelect-${additionalrole.id}" type="checkbox" name="site-role" value="${additionalrole.id}" />
							<label for="roleSelect-${additionalrole.id}">${additionalrole.name}</label>
						</li>
						<c:set var="lastGroupRole" value="${additionalrole.groupId}"/>
					</c:forEach>
                  </c:if>
              </ul>
              </fieldset>
            </div>

            <div style="float:right;width:50%;">
              <fieldset>
              <legend><span><spring:message code="siterole.perm.title" /></span></legend>
              <div class="permsInstruction"><em><spring:message code="siterole.perm.header" /></em></div>
              <ul style="margin: 0px;">
                  <c:forEach var="permission" items="${permissions}" varStatus="counter">
                  <li class="checkbox">
                      <input id="permSelect-${permission}" type="checkbox" name="site-perm"value="${permission}" />
                      <label for="permSelect-${permission}">${permission}</label>
                  </li>
                  </c:forEach>
              </ul>
              </fieldset>
            </div>
        </div>
        <div style="clear: both;"></div>

        <div style="padding: 5px;">
            <input name="addPerms" type="submit" value="<spring:message code="siterole.add.command" />" />
            <input name="removePerms" type="submit" value="<spring:message code="siterole.remove.command" />" />
        </div>

        <input name="action" type="hidden" value="perms" />
    </form>
    <br/>
</div>

</div>

<!-- Hidden block to declare resource bundle variables for use in JS -->
<div style="display: none;">
  <!-- By convention, the id of the span must be "i18n_"+{message code}, the container div should be set to display:none;, use kaltura.i18n(key) in JS to lookup the message -->
  <%-- <span id="i18n_listCollections.delete.collection.confirmation"><spring:message code="listCollections.delete.collection.confirmation" /></span> --%>
</div>

<!-- script type="text/javascript"></script -->

<jsp:directive.include file="/WEB-INF/jsp/footer.jsp" />
