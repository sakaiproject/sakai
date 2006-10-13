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
<span class="rwiki_pageLinks"><jsp:scriptlet>
if (usePrinterLink != null && usePrinterLink.booleanValue()) {
</jsp:scriptlet><jsp:element name="a"><jsp:attribute name="href"><jsp:expression>viewBean.getRssAccessUrl()</jsp:expression></jsp:attribute><jsp:attribute name="target">rssfeed</jsp:attribute><jsp:attribute name="id">rssLink</jsp:attribute><jsp:body><img src="/library/image/transparent.gif" alt="RSS" border="0" /></jsp:body></jsp:element><jsp:element name="a"><jsp:attribute name="href"><jsp:expression>viewBean.getPublicViewUrl()</jsp:expression></jsp:attribute><jsp:attribute name="target">_blank</jsp:attribute><jsp:attribute name="id">printerFriendlyLink</jsp:attribute><jsp:body><img src="/library/image/transparent.gif" alt="Printer Friendly" border="0" /></jsp:body></jsp:element><jsp:scriptlet>
}
if ( viewLinkName == null || viewLinkName.length() == 0 ) {
	viewLinkName = "View";
}
if (useHomeLink == null || useHomeLink.booleanValue()) {
</jsp:scriptlet><span id="homeLink" ><img border="0" src="/library/image/transparent.gif" /></span><jsp:element name="a"><jsp:attribute name="href"><jsp:expression>homeBean.getHomeLinkUrl()</jsp:expression></jsp:attribute><jsp:body><jsp:expression>homeBean.getHomeLinkValue()</jsp:expression></jsp:body></jsp:element><jsp:scriptlet>
}
if (useViewLink == null || useViewLink.booleanValue()) {
</jsp:scriptlet><span id="viewLink" ><img border="0" src="/library/image/transparent.gif" /></span><jsp:element name="a"><jsp:attribute name="href"><jsp:expression>viewBean.getViewUrl()</jsp:expression></jsp:attribute><jsp:body><jsp:expression>viewLinkName</jsp:expression></jsp:body></jsp:element><jsp:scriptlet>
}
if (useEditLink == null || useEditLink.booleanValue()) {
</jsp:scriptlet><span id="editLink" ><img border="0" src="/library/image/transparent.gif" /></span><jsp:element name="a"><jsp:attribute name="href"><jsp:expression>viewBean.getEditUrl()</jsp:expression></jsp:attribute><jsp:body>Edit</jsp:body></jsp:element><jsp:scriptlet>
}
if (useInfoLink == null || useInfoLink.booleanValue()) {
</jsp:scriptlet><span id="infoLink" ><img border="0" src="/library/image/transparent.gif" /></span><jsp:element name="a"><jsp:attribute name="href"><jsp:expression>viewBean.getInfoUrl()</jsp:expression></jsp:attribute><jsp:attribute name="class">rwiki_currentPage</jsp:attribute><jsp:body>Info</jsp:body></jsp:element><jsp:scriptlet>
}
if (useHistoryLink == null || useHistoryLink.booleanValue()) {
</jsp:scriptlet><span id="historyLink" ><img border="0" src="/library/image/transparent.gif" /></span><jsp:element name="a"><jsp:attribute name="href"><jsp:expression>viewBean.getHistoryUrl()</jsp:expression></jsp:attribute><jsp:body>History</jsp:body></jsp:element><jsp:scriptlet>
}
if (useWatchLink == null || useWatchLink.booleanValue()) {
	   if ( withNotification != null && withNotification.booleanValue() ) {
</jsp:scriptlet><span id="watchLink" ><img border="0" src="/library/image/transparent.gif" alt="Watch for changes" /></span><jsp:element name="a"><jsp:attribute name="href"><jsp:expression>viewBean.getPreferencesUrl()</jsp:expression></jsp:attribute><jsp:body>Watch</jsp:body></jsp:element><jsp:scriptlet>
	   }
}
</jsp:scriptlet></span>
