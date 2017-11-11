<jsp:directive.tag/>
<jsp:directive.attribute name="useHomeLink" type="java.lang.Boolean"/>
<jsp:directive.attribute name="usePrinterLink" type="java.lang.Boolean"/>
<jsp:directive.attribute name="useViewLink" type="java.lang.Boolean"/>
<jsp:directive.attribute name="viewLinkName" type="java.lang.String"/>
<jsp:directive.attribute name="useEditLink" type="java.lang.Boolean"/>
<jsp:directive.attribute name="useInfoLink" type="java.lang.Boolean"/>
<jsp:directive.attribute name="useHistoryLink" type="java.lang.Boolean"/>
<jsp:directive.attribute name="useWatchLink" type="java.lang.Boolean"/>
<jsp:directive.attribute name="withNotification" type="java.lang.Boolean"/>
<jsp:directive.attribute name="homeBean" type="uk.ac.cam.caret.sakai.rwiki.tool.bean.HomeBean"/>
<jsp:directive.attribute name="viewBean" type="uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean"/>
<jsp:directive.attribute name="resourceLoaderBean" type="uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean" />
<span class="rwiki_pageLinks"><jsp:scriptlet>
if (usePrinterLink != null && usePrinterLink.booleanValue()) {
</jsp:scriptlet>
<jsp:element name="a">
    <jsp:attribute name="href"><jsp:expression>org.sakaiproject.util.Web.escapeHtml(viewBean.getRssAccessUrl())</jsp:expression></jsp:attribute>
	<jsp:attribute name="target">rssfeed</jsp:attribute>
	<jsp:attribute name="id">rssLink</jsp:attribute>
	<jsp:attribute name="title"><jsp:expression>resourceLoaderBean.getString("jsp_rss_feed_changes")</jsp:expression></jsp:attribute> 
	<jsp:body>&#160;</jsp:body>
</jsp:element>
<jsp:scriptlet>
}
if ( viewLinkName == null || viewLinkName.length() == 0 ) {
	viewLinkName = resourceLoaderBean.getString("jsp_view");
}
if (useHomeLink == null || useHomeLink.booleanValue()) {
</jsp:scriptlet>
<jsp:element name="a">
	<jsp:attribute name="id">homeLink</jsp:attribute>
	<jsp:attribute name="href"><jsp:expression>org.sakaiproject.util.Web.escapeHtml(homeBean.getHomeLinkUrl())</jsp:expression></jsp:attribute>
	<jsp:attribute name="title"><jsp:expression>resourceLoaderBean.getString("jsp_wiki_home")</jsp:expression></jsp:attribute>
	<jsp:body><jsp:expression>homeBean.getHomeLinkValue()</jsp:expression></jsp:body>
</jsp:element>
<jsp:scriptlet>
}
if (useViewLink == null || useViewLink.booleanValue()) {
</jsp:scriptlet>
<jsp:element name="a">
	<jsp:attribute name="id">viewLink</jsp:attribute>
	<jsp:attribute name="href"><jsp:expression>org.sakaiproject.util.Web.escapeHtml(viewBean.getViewUrl())</jsp:expression></jsp:attribute>
	<jsp:attribute name="title"><jsp:expression>resourceLoaderBean.getString("jsp_view")</jsp:expression></jsp:attribute>
	<jsp:body><jsp:expression>viewLinkName</jsp:expression></jsp:body>
</jsp:element>
<jsp:scriptlet>
}
if (useEditLink == null || useEditLink.booleanValue()) {
</jsp:scriptlet>
<jsp:element name="a">
	<jsp:attribute name="id">editLink</jsp:attribute>
	<jsp:attribute name="href"><jsp:expression>org.sakaiproject.util.Web.escapeHtml(viewBean.getEditUrl())</jsp:expression></jsp:attribute>
			<jsp:attribute name="title"><jsp:expression>resourceLoaderBean.getString("jsp_edit")</jsp:expression></jsp:attribute>
	<jsp:body><jsp:expression>resourceLoaderBean.getString("jsp_edit")</jsp:expression></jsp:body>
</jsp:element>
<jsp:scriptlet>
}
if (useInfoLink == null || useInfoLink.booleanValue()) {
</jsp:scriptlet>
<jsp:element name="a">
	<jsp:attribute name="id">infoLink</jsp:attribute>
	<jsp:attribute name="href"><jsp:expression>org.sakaiproject.util.Web.escapeHtml(viewBean.getInfoUrl())</jsp:expression></jsp:attribute>
	<jsp:attribute name="title"><jsp:expression>resourceLoaderBean.getString("jsp_info")</jsp:expression></jsp:attribute>
	<jsp:attribute name="class">rwiki_currentPage</jsp:attribute>
	<jsp:body><jsp:expression>resourceLoaderBean.getString("jsp_info")</jsp:expression></jsp:body>
</jsp:element>
<jsp:scriptlet>
}
if (useHistoryLink == null || useHistoryLink.booleanValue()) {
</jsp:scriptlet>
<jsp:element name="a">
	<jsp:attribute name="id">historyLink</jsp:attribute>
	<jsp:attribute name="href"><jsp:expression>org.sakaiproject.util.Web.escapeHtml(viewBean.getHistoryUrl())</jsp:expression></jsp:attribute>
	<jsp:attribute name="title"><jsp:expression>resourceLoaderBean.getString("jsp_history")</jsp:expression></jsp:attribute>
	<jsp:body><jsp:expression>resourceLoaderBean.getString("jsp_history")</jsp:expression></jsp:body>
</jsp:element>
<jsp:scriptlet>
}
if (useWatchLink == null || useWatchLink.booleanValue()) {
	   if ( withNotification != null && withNotification.booleanValue() ) {
</jsp:scriptlet>
<jsp:element name="a">
	<jsp:attribute name="id">watchLink</jsp:attribute>
	<jsp:attribute name="href"><jsp:expression>org.sakaiproject.util.Web.escapeHtml(viewBean.getPreferencesUrl())</jsp:expression></jsp:attribute>
	<jsp:attribute name="title"><jsp:expression>resourceLoaderBean.getString("jsp_watch_for_changes")</jsp:expression></jsp:attribute>
	<jsp:body><jsp:expression>resourceLoaderBean.getString("jsp_watch")</jsp:expression></jsp:body>
</jsp:element>
<jsp:scriptlet>
	   }
}
</jsp:scriptlet>
</span>
