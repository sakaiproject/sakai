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
  
<%--        
        <sakai:hideDivision title="Private Message Area">
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
        </mf:hideDivisionButtonBar>
--%>
        <mf:forumHideDivision title="Test Division" id="_test_div">
          <mf:forum_bar_link value="TestLink" action="#{MessageForumsTool.processTestLinkCompose}"/>
          <h:outputText value="Testing this division with links above."/>
        </mf:forumHideDivision>
               
      </h:form>

    </sakai:view_content>	
  </sakai:view_container>
</f:view> 
