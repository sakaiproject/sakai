<jsp:directive.tag import="uk.ac.cam.caret.sakai.rwiki.utils.UserDisplayHelper" />
<jsp:directive.attribute name="name" required="true" />
<jsp:expression> UserDisplayHelper.formatDisplayName(name) </jsp:expression>


