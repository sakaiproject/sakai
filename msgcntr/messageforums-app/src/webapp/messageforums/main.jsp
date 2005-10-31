<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %> 

<f:view>

  <f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
  <sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>

  <sakai:view_container title="Messge Forums">
    <sakai:view_content>
  
      <h:form onsubmit="return false;">
  
<%--        <sakai:hideDivision title="Private Message Area">
          private messages here 
        </sakai:hideDivision>

        <sakai:hideDivision title="Discussion Forums">
          discussion forums here
        </sakai:hideDivision>

        <sakai:hideDivision title="Open Forum">
          open forum topics here
        </sakai:hideDivision>
            
        <mf:hideDivisionButtonBar title="Open Forum 2">
          <mf:hideDivisionContent>
            some stuff...
          </mf:hideDivisionContent>
        </mf:hideDivisionButtonBar>--%>
			<mf:forumHideDivision title="Test Division" id="_test_div">
					<mf:forum_bar_link value="TestLink" action="#{MessageForumsTool.processTestLinkCompose}"/>
					<h:outputText value="Testing this division with links above."/>
			</mf:forumHideDivision>
        

<h4 style="" onclick="javascript:showHideDiv('_id7', '/sakai-jsf-resource');" class="">  
<table border="0" cellspacing="0" cellpadding="0"> 
<tr>
<td nowrap="nowrap" align="left">
<img id="_id7__img_hide_division_" alt="Open Forum"    src="/sakai-jsf-resource/hideDivision/images/right_arrow.gif" style="cursor:pointer;" />
Another Open Forum
</td>
<td width="100%">&nbsp;</td>
<td nowrap="nowrap" align="right">
<a href="http://www.google.com">link 1</a>
<a href="http://www.google.com">link 2</a>
<a href="http://www.google.com">link 3</a>
<a href="http://www.google.com">link 4</a>
&nbsp;
</td>
</tr>
</table>
</h4>
<div " style="display:none"  id="_id7__hide_division_">
   all dat stuff
</div><script type="text/javascript">  showHideDiv('_id7', '/sakai-jsf-resource');</script>
        
        
      </h:form>

    </sakai:view_content>	
  </sakai:view_container>
</f:view> 
