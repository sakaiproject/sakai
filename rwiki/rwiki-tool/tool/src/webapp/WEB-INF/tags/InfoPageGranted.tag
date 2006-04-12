<jsp:directive.tag/>
<jsp:directive.attribute name="granted" required="true" type="java.lang.Boolean" />
<jsp:directive.attribute name="span" type="java.lang.Boolean"/>
<jsp:scriptlet>
// FIXME internationalize
if (span == null || span.booleanValue()) {
   if (granted.booleanValue()) {
</jsp:scriptlet>
	<span class="rwiki_info_page_granted" >yes</span>
<jsp:scriptlet>
   } else {
</jsp:scriptlet>
	<span class="rwiki_info_page_denied" >no</span>
<jsp:scriptlet>
   }
} else {

	if (granted.booleanValue()) {
		out.print("yes");
	} else {
		out.print("no");
	}
}
</jsp:scriptlet>
