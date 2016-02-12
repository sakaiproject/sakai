<%--

    Copyright 2011-2013 The Australian National University

       Licensed under the Apache License, Version 2.0 (the "License");
       you may not use this file except in compliance with the License.
       You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing, software
       distributed under the License is distributed on an "AS IS" BASIS,
       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
       See the License for the specific language governing permissions and
       limitations under the License.

--%>
<%@ page contentType="text/html" isELIgnored="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>

<portlet:defineObjects />
<link type="text/css" rel="stylesheet"  href="<%=request.getContextPath()%>/css/simple-rss-portlet.css" />

<div class="news-feed">

	<div class="news-source">
		<c:if test="${not empty SyndFeed.image}">
			<a target="_blank" href="${SyndFeed.image.link}">
      			<img src="${SyndFeed.image.url}" alt="${SyndFeed.image.description}" class="news-feed-img"/>
    		</a>
    	</c:if>
		<p>${SyndFeed.description}</p>
	</div>
		
	<div class="news-items">
		<ul>
			<c:forEach items="${SyndFeed.entries}" var="SyndEntry" end="${maxItems}">
				<li>
					<c:if test="${not empty Media[SyndEntry.uri] && Media[SyndEntry.uri].image}">
						<img src="${Media[SyndEntry.uri].url}" class="news-item-img"/>
					</c:if>
					<h3>
					<c:choose>
						<c:when test="${not empty SyndEntry.link}">
	      					<a target="_blank" href="${SyndEntry.link}" class="news-item-title">${SyndEntry.title}</a>
	      				</c:when>
	      				<c:otherwise>
		      				${SyndEntry.title}
		      			</c:otherwise>
		      		</c:choose>
		      		</h3>
	      			<span class="news-item-excerpt">${SyndEntry.description.value}</span>
	      			
	      			<c:if test="${not empty Media[SyndEntry.uri] && !Media[SyndEntry.uri].image}">
						<div class="news-item-attachment">
							<a href="${Media[SyndEntry.uri].url}">${Media[SyndEntry.uri].displayName}</a>
							 (${Media[SyndEntry.uri].type}, ${Media[SyndEntry.uri].displayLength})
						</div>
					</c:if>
	      				      			
	    		</li>
    		</c:forEach>
			
		</ul>
	</div>
</div>
		