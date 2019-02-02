# AcadTermManage 
An UI to manage Academic Terms, so it doesn't need to happen via SQL or WebServices.

It supports creation of new terms and updates of existing terms.

It will also update the references in a the "term\_eid" and "term\_title" site properties if the term's EID or title has been changed.

It currently does NOT support the deletion of terms (in the UI), because it's unclear what should happen to site properties and/or sites which reference a deleted term (Do nothing? Delete the now outdated site properties? Delete the sites?)  

![screenshot](/screenshot_tool_w640.png?raw=true "acadtermmanage-tool screenshot")


In theory, editing terms (and access to the tool) should be limited to sakai admins AND users who have the permission/function "sakai.acadtermmanage.is\_manager" in the site where the tool is installed.
 
In practice, the course management API, which is used by this tool, requires users to be sakai admins (instead of checking the user's permissions), so adding that permission and checking it is currently disabled via the boolean 
org.sakaiproject.acadtermmanage.logic.impl.AcademicSessionLogicImpl.USE_PERMISSION.
With that switch set to "false" (its current default), the tool will do the same as the course management API and require its users to be sakai admins.    


