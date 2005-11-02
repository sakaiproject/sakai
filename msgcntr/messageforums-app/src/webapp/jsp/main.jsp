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
        <mf:forumHideDivision title="Private Message" id="_test_div">
          <mf:forum_bar_link value="Organize " action="#{PrivateMessagesTool.processPvtMsgOrganize}"/> &nbsp;
          <mf:forum_bar_link value=" Statistics " action="#{PrivateMessagesTool.processPvtMsgStatistics}"/> &nbsp;
          <mf:forum_bar_link value=" Settings " action="#{PrivateMessagesTool.processPvtMsgSettings}"/> &nbsp;
          
          <h:outputText value="Private Messages"/>
          <h:commandLink value="Received" styleClass="line-break:  strict" action="#{PrivateMessagesTool.processPvtMsgReceived}"/>  
          <h:commandLink value="Sent" styleClass="line-break:  strict" action="#{PrivateMessagesTool.processPvtMsgSent}"/> 
          <h:commandLink value="Delete" styleClass="line-break:  strict" action="#{PrivateMessagesTool.processPvtMsgDelete}"/>  
          <h:commandLink value="Drafts" styleClass="line-break:  strict" action="#{PrivateMessagesTool.processPvtMsgDrafts}"/>    
          <h:commandLink value="Case" styleClass="line-break:  strict" action="#{PrivateMessagesTool.processPvtMsgCase}"/>    
          <h:commandLink value="Group 2 Correspondance" styleClass="line-break:  strict" action="#{PrivateMessagesTool.processPvtMsgGrpCorres}"/>    
          
		  
        <sakai:button_bar>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processCDFMPostMessage}" value="#{msgs.cdfm_button_bar_post_message}" /> &nbsp;
          <sakai:button_bar_item action="#{PrivateMessagesTool.processCDFMSaveDraft}" value="#{msgs.cdfm_button_bar_save_draft}" />&nbsp;
          <sakai:button_bar_item action="#{PrivateMessagesTool.processCDFMCancel}" value="#{msgs.cdfm_button_bar_cancel}" />&nbsp;
        </sakai:button_bar>     
        </mf:forumHideDivision>
        
        <mf:forumHideDivision title="Discussion Forums" id="_test_div">
          <mf:forum_bar_link value="Organize " action="#{PrivateMessagesTool.processPvtMsgOrganize}"/> &nbsp;
          <mf:forum_bar_link value=" Statistics " action="#{PrivateMessagesTool.processPvtMsgStatistics}"/> &nbsp;
          <mf:forum_bar_link value=" Settings " action="#{PrivateMessagesTool.processPvtMsgSettings}"/> &nbsp;                       
        </mf:forumHideDivision>

        <mf:forumHideDivision title="Open Forums" id="_test_div">
          <mf:forum_bar_link value="Organize " action="#{PrivateMessagesTool.processPvtMsgOrganize}"/> &nbsp;
          <mf:forum_bar_link value=" Statistics " action="#{PrivateMessagesTool.processPvtMsgStatistics}"/> &nbsp;
          <mf:forum_bar_link value=" Settings " action="#{PrivateMessagesTool.processPvtMsgSettings}"/> &nbsp;                       
        </mf:forumHideDivision>
                
        <sakai:button_bar>
          <sakai:button_bar_item action="#{PrivateMessagesTool.processCDFMPostMessage}" value="#{msgs.cdfm_button_bar_post_message}" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processCDFMSaveDraft}" value="#{msgs.cdfm_button_bar_save_draft}" />
          <sakai:button_bar_item action="#{PrivateMessagesTool.processCDFMCancel}" value="#{msgs.cdfm_button_bar_cancel}" />
        </sakai:button_bar>     
          
      </h:form>

    </sakai:view_content>	
  </sakai:view_container>
</f:view> 
