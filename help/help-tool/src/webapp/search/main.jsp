<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %> 
<%-- Custom tag library just for this tool --%>
<%@ taglib uri="http://sakaiproject.org/jsf/help" prefix="help" %>
<%
		response.setContentType("text/html; charset=UTF-8");
		response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		response.addDateHeader("Last-Modified", System.currentTimeMillis());
		response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		response.addHeader("Pragma", "no-cache");
%>
<f:view>
<sakai:view_container title="#{msgs.title_edit}">
<script type="text/javascript">
 function clearText(thefield) { if (thefield.defaultValue == thefield.value) thefield.value = ""; else thefield.value = thefield.defaultValue }
</script>

<div id="message" style="padding: 2px; background:#000000 none repeat scroll 0%; position: absolute; z-index: 3; 
-moz-background-clip: initial; -moz-background-origin: initial; -moz-background-inline-policy: initial; color: white; font-size: 
90%; top: 1px; left: 1px; display: none;">
<h:outputText value="#{msgs.searching}" />
</div>  

<h:form id="helpSearchForm">
      <%--<h:commandButton value="#{msgs.back}" onclick="history.back()" />
      <h:commandButton value="#{msgs.forward}" onclick="history.forward()" />      
      --%>
      <f:verbatim><br/></f:verbatim>
      <%--
      <h:commandButton value="#{msgs.closeHelp}" onclick="javascript:top.close();" />
      --%>
	<%-- The h:panelGroup is so that the label/for/id doesn't generate an error and 
	     the outputting of the h1 in verbatim is so that the title is put inbetween them. --%>
	<h:panelGroup>
		<f:verbatim><h1 style="font-size:1em;padding:0;margin:0"></f:verbatim>
			<h:outputText value="#{msgs.search}" />
		<f:verbatim></h1></f:verbatim>
		<h:outputLabel value="#{msgs.search}" for="searchField" styleClass="skip"/>
		<h:inputText value="#{SearchTool.searchString}" onclick="clearText(this)" id="searchField"/>
		<h:commandButton action="#{SearchTool.processActionSearch}" id="searchButton" value="#{msgs.search_button}"  onclick="document.getElementById('message').style.display = 'block';"> 
			<help:defaultAction/>
		</h:commandButton>
    </h:panelGroup>
</h:form>
<sakai:group_box title="">
    <h:outputText value="#{SearchTool.numberOfResult}" />
    <h:dataTable border="0" styleClass="listHier" value="#{SearchTool.searchResults}" var="result" summary="#{msgs.search_result_summary}">
	    <h:column>
	    	<h:outputLink value="../content.hlp?docId=#{result.docId}" target="content" style="font:8pt" rendered="#{SearchTool.isRestEnabled}">
  	      <h:outputText value="#{result.name}"/>
	    	</h:outputLink>
	  		<h:outputLink value="../TOCDisplay/main?help=#{result.docId}" target="toc" style="font:8pt"  rendered="#{not SearchTool.isRestEnabled}">
			    <h:outputText value="#{result.name}"/>
			  </h:outputLink>
		</h:column>
		<h:column>
			<h:outputText value="#{result.formattedScore}" style="font:8pt"/>
		</h:column>
    </h:dataTable>
</sakai:group_box>
</sakai:view_container>
</f:view>

