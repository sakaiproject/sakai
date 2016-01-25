<%--********************* Private Message Area*********************--%>
  
<%--
  <mf:forum_bar_link value="#{msgs.pvt_organize}" action="#{PrivateMessagesTool.processPvtMsgOrganize}"/> &nbsp;
  <mf:forum_bar_link value="#{msgs.pvt_statistics}" action="#{PrivateMessagesTool.processPvtMsgStatistics}"/> &nbsp;
 --%>
        		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
  <mf:forum_bar_link value="#{msgs.pvt_settings}" action="#{PrivateMessagesTool.processPvtMsgSettings}"
                     rendered="#{PrivateMessagesTool.atMain}"/> &nbsp;
<!--jsp/privateMsg/pvtArea.jsp-->
	<h:panelGroup rendered="#{PrivateMessagesTool.pvtAreaEnabled}"><f:verbatim><div class="specialLink hierItemBlockWrapper" ></f:verbatim>
  <h:dataTable value="#{PrivateMessagesTool.decoratedForum}" var="forum" rendered="#{PrivateMessagesTool.pvtAreaEnabled}" width="100%" cellpadding="0" cellspacing="0">
    <h:column>
    	
    	  <h:panelGrid columns="2"  styleClass="hierItemBlock" columnClasses="bogus,itemAction">
    	    <h:panelGroup >
			<f:verbatim><h4></f:verbatim>		
    	      <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" immediate="true" title=" #{PrivateMessagesTool.decoratedForum.forum.title}">
  			      <h:outputText value="#{PrivateMessagesTool.decoratedForum.forum.title}" />   
  		      </h:commandLink>
			  <f:verbatim></h4></f:verbatim>
    	    </h:panelGroup>
    	    <h:panelGroup style="float: right;text-align: right;white-space: nowrap;">
  			    <h:commandLink action="#{PrivateMessagesTool.processPvtMsgCompose}" title=" #{msgs.pvt_compose}">
  			      <h:outputText value="#{msgs.pvt_compose}" />
			    </h:commandLink>
			    <f:verbatim>&nbsp;|&nbsp;</f:verbatim>
     	    <h:commandLink action="#{PrivateMessagesTool.processPvtMsgFolderSettingAdd}" title=" #{msgs.pvt_newfolder}">
			      <h:outputText value="#{msgs.pvt_newfolder}" />
			    </h:commandLink>
    	    </h:panelGroup>
    	  </h:panelGrid>
			
		  <h:dataTable id="privateForums" value="#{forum.topics}" var="topic" width="100%" cellpadding="0" cellspacing="0" style="margin:0;">
		    <h:column>
          
          <h:panelGrid columns="2" width="100%"  cellpadding="0" cellspacing="0"  styleClass="listHier" style="margin:0">
    	        <h:panelGroup   rendered="#{!topic.topic.mutable}">
   			       <h:graphicImage url="/images/dir_closed.gif" alt="" />
					<h:commandLink action="#{PrivateMessagesTool.processPvtMsgTopic}" immediate="true" title=" #{msgs[topic.topic.title]}"> 
    			        <h:outputText value="  " />    
    			        <h:outputText value="#{msgs[topic.topic.title]}" /> 
    			        <%-- <f:param value="#{topic.topic.title}" name="pvtMsgTopicTitle"/> --%>
    			        <f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId"/>
    		        </h:commandLink>
    		        <h:outputText value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.pvt_lowercase_msg}" rendered="#{topic.totalNoMessages < 2}" styleClass="textPanelFooter"/>
	    		      <h:outputText value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.pvt_lowercase_msgs}" rendered="#{topic.totalNoMessages > 1}" styleClass="textPanelFooter"/>
    		        <h:outputText value=" - #{topic.unreadNoMessages} #{msgs.pvt_unread}" rendered="#{topic.topic.title == 'pvt_received'}" styleClass="textPanelFooter"/>
    		        <h:outputText value=" #{msgs.cdfm_closeb}" styleClass="textPanelFooter"/>
    	        </h:panelGroup>
    	        <h:panelGroup rendered="#{!topic.topic.mutable}"><h:outputText value=" " /></h:panelGroup>
    	        <h:panelGroup  rendered="#{topic.topic.mutable}">
				    <h:graphicImage url="/images/dir_closed.gif" alt="" />
    	          <h:commandLink action="#{PrivateMessagesTool.processPvtMsgTopic}" immediate="true" title=" #{topic.topic.title}"> 
					  
    			        <h:outputText value="  " />     
					      <h:outputText value="#{topic.topic.title}" /> 
	    			      <%-- <f:param value="#{topic.topic.title}" name="pvtMsgTopicTitle"/> --%>
	    			      <f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId"/>
	    			    </h:commandLink>
	    		      <h:outputText value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.pvt_lowercase_msg}" styleClass="textPanelFooter" />
    		        <h:outputText value=" - #{topic.unreadNoMessages} #{msgs.pvt_unread}" rendered="#{topic.topic.title == 'pvt_received' || topic.topic.title == 'pvt_deleted'}" styleClass="textPanelFooter"/>
    		        <h:outputText value=" #{msgs.cdfm_closeb}" styleClass="textPanelFooter"/>
    	        </h:panelGroup>
    	        <h:panelGroup styleClass="itemAction msgNav" rendered="#{topic.topic.mutable}">
  			        <h:commandLink action="#{PrivateMessagesTool.processPvtMsgFolderSettings}" title=" #{msgs.pvt_foldersettings}">
  			          <h:outputText value="#{msgs.pvt_foldersettings}" />
					      <%-- <f:param value="#{topic.topic.title}" name="pvtMsgTopicTitle"/> --%>
					      <f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId"/>
					    </h:commandLink>
		    	    </h:panelGroup>
    	      </h:panelGrid>
 	      
				</h:column>
		  </h:dataTable>
	  </h:column>
  </h:dataTable>     
<f:verbatim></div></f:verbatim></h:panelGroup>  
  <h:panelGroup>	
  <h:outputText rendered="#{PrivateMessagesTool.dispError}" value="#{msgs.pvt_mainpgerror}" styleClass="instruction" />
  </h:panelGroup>  
