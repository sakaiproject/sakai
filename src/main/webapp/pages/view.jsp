<%@ page contentType="text/html" isELIgnored="false" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>

<portlet:defineObjects />

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
	      			<a target="_blank" href="${SyndEntry.link}" class="news-item-title">${SyndEntry.title}</a>
	      			<span class="news-item-excerpt">${SyndEntry.description.value}</span>
	    		</li>
    		</c:forEach>
			
		</ul>
	</div>
</div>
		