<%@ page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		import="uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean,uk.ac.cam.caret.sakai.rwiki.tool.bean.MultiRealmEditBean,uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean"
		%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%

	 RequestScopeSuperBean rsac = (RequestScopeSuperBean)request.getAttribute(RequestScopeSuperBean.REQUEST_ATTRIBUTE);
	 if ( rsac == null ) 
	 {
	 	throw new ServletException("Unable to locate RequestScopeBean");
	 }
	 MultiRealmEditBean multiRealmEditBean = rsac.getMultiRealmEditBean();
	 ResourceLoaderBean rlb = rsac.getResourceLoaderBean();
	 if ( multiRealmEditBean == null ) 
	 {
	 	throw new ServletException("Unable to locate MultiRealmEditBean from RequestScopeBean");
	 }
	 System.err.println("Got MREB "+multiRealmEditBean);
	 
	 String errorFormatWrapper = "<p class=\"validation\" style=\"clear: none;\">{0}</p>";
	 String errorFormatItem = "{0}";
	 
	 // this wrapps the realm, there are multiple realms
	 String permissionsRealmWrapper = 
	 "<tr> "+
	 "<th colspan=\"7\" >" +
	 "<label>"+rlb.getString("jsp_realm")+": {0}</label>"+
	 "<input type=\"submit\" name=\"save\" value=\""+rlb.getString("jsp_realm")+"Remove Realm\"/>"+
	 "</th>"+
	 "</tr><tr>"+
	 "<th>"+rlb.getString("jsp_realm")+"Roles</th>"+
	 "{1}"+
	 "</tr>{2}<tr>"+
	 "<td colspan=\"7\" >"+
	 "<input type=\"test\" name=\"{3}\" /><input type=\"submit\" name=\"save\" value=\""+rlb.getString("jsp_add_role")+"\"/>"+
	 "</td>"+
	 "</tr>";
	 
	 // this is used in {1} in permissionRealmWrapper above
	 String permissionNames="<td><label>{0}</label></td>";
	 // this is used in {2} in permissionRealm
	 String permissionsGroupRoleWrapper="<tr class=\"permissionsGroupRole\"><th><label>{0}</label></th>{1}</tr>";
	 String permissionEditOn="<td><input type=\"checkbox\" name=\"{0}\" checked=\"checked\"/>";
	 String permissionEditOff="<td><input type=\"checkbox\" name=\"{0}\" />";
	 String addRealmOptionFormat="<option value=\"{0}\" >{0}</option>";
%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="<%= rlb.getString("jsp_lang") %>" xml:lang="<%= rlb.getString("jsp_xml_lang") %>"> 
  <head> 
    <title>Edit Section: 
      <%= multiRealmEditBean.getLocalSpace() %>
    </title>
    <%= multiRealmEditBean.getSakaiHTMLHead() %>
  </head>
  <body <%= multiRealmEditBean.getBodyOnLoad() %> > 
    <div id="rwiki_container"> 
      <div class="portletBody"> 
          <h3><%= rlb.getString("jsp_edit_acl_title") %>Edit ACL's Controlling Page Permissions: 
            <%= multiRealmEditBean.getPageName() %>
          </h3>
          <%= multiRealmEditBean.getErrors(errorFormatWrapper,errorFormatItem) %>
          <form action="?#" method="post"> 
              <table cellspacing="0"> 
                <!-- a list of roles, based on the templates -->
                <tr>      
                  <td colspan="7"> 
                        <select name="add_realm" >
                        	<%= multiRealmEditBean.getAvailableRealms(addRealmOptionFormat) %>
                        </select>
                        <input type="submit" name="command_add_realm" value="<%= rlb.getString("jsp_add_realm") %>"/>   
                  </td>
                </tr>
                <%= multiRealmEditBean.getPermissionsGroupRoles(permissionsRealmWrapper,permissionNames,permissionsGroupRoleWrapper,permissionEditOn,permissionEditOff) %>
                <tr> 
                  <td colspan="7"> 
                        <input type="hidden" name="pageName" value="<%= multiRealmEditBean.getPageName() %>" />
                        <input type="hidden" name="panel" value="Main"/>
                        <input type="hidden" name="action" value="editRealmMany"/>
                        <input type="submit" name="command_save" value="<%= rlb.getString("jsp_button_save") %>"/>
                        <input type="submit" name="command_cancel" value="<%= rlb.getString("jsp_button_cancel") %>"/>
                        <input type="hidden" name="realm" value="<%= multiRealmEditBean.getLocalSpace() %>"/>
                  </td>
                </tr>
              </table>
          </form>
        </div>
      </div>
    <%= multiRealmEditBean.getFooterScript() %>
</html>