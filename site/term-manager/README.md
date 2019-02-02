# AcadTermManage 
An UI to manage Academic Terms, so it doesn't need to happen via SQL or WebServices.

It supports creation of new terms and updates of existing terms.

It will also update the references in a the "term\_eid" and "term\_title" site properties if the term's EID or title has been changed.

It currently does NOT support the deletion of terms (in the UI), because it's unclear what should happen to site properties and/or sites which reference a deleted term (Do nothing? Delete the now outdated site properties? Delete the sites?)  

![screenshot](/screenshot_tool_w640.png?raw=true "acadtermmanage-tool screenshot")


Editing terms (and access to the tool) should be limited to sakai admins.
