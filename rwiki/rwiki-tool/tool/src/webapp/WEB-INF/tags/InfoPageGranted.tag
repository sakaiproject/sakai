<jsp:directive.tag/>
<jsp:directive.attribute name="granted" required="true" type="java.lang.Boolean" />
<jsp:directive.attribute name="span" type="java.lang.Boolean"/>
<jsp:directive.attribute name="resourceLoaderBean" type="uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean" />
<jsp:scriptlet>
if (span == null || span.booleanValue()) {
   if (granted.booleanValue()) {
</jsp:scriptlet>
	<span class="rwiki_info_page_granted" ><jsp:expression>resourceLoaderBean.getString("jsp_yes")</jsp:expression></span>
<jsp:scriptlet>
   } else {
</jsp:scriptlet>
	<span class="rwiki_info_page_denied" ><jsp:expression>resourceLoaderBean.getString("jsp_no")</jsp:expression></span>
<jsp:scriptlet>
   }
} else {
	if (granted.booleanValue()) {
		out.print(resourceLoaderBean.getString("jsp_yes"));
	} else {
		out.print(resourceLoaderBean.getString("jsp_no"));
	}
}
</jsp:scriptlet>
