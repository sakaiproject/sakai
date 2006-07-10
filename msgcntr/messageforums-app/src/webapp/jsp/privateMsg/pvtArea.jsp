<%--********************* Private Message Area*********************--%>
  
<%--
  <mf:forum_bar_link value="#{msgs.pvt_organize}" action="#{PrivateMessagesTool.processPvtMsgOrganize}"/> &nbsp;
  <mf:forum_bar_link value="#{msgs.pvt_statistics}" action="#{PrivateMessagesTool.processPvtMsgStatistics}"/> &nbsp;
 --%>
  <mf:forum_bar_link value="#{msgs.pvt_settings}" action="#{PrivateMessagesTool.processPvtMsgSettings}"
                     rendered="#{PrivateMessagesTool.atMain}"/> &nbsp;
  <h:dataTable value="#{PrivateMessagesTool.decoratedForum}" var="forum" rendered="#{PrivateMessagesTool.pvtAreaEnabled}" width="100%">
    <h:column>
    	
    	  <h:panelGrid columns="2" styleClass="msgHeadings" summary="">
    	    <h:panelGroup>
    	      <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" immediate="true" title=" #{PrivateMessagesTool.decoratedForum.forum.title}">
  			      <h:outputText value="#{PrivateMessagesTool.decoratedForum.forum.title}" />   
  		      </h:commandLink>
    	    </h:panelGroup>
    	    <h:panelGroup styleClass="msgNav">
  			    <h:commandLink action="#{PrivateMessagesTool.processPvtMsgCompose}" title=" #{msgs.pvt_compose}">
  			      <h:outputText value="#{msgs.pvt_compose}" />
			    </h:commandLink>
			    <f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
     	    <h:commandLink action="#{PrivateMessagesTool.processPvtMsgFolderSettingAdd}" title=" #{msgs.pvt_newfolder}">
			      <h:outputText value="#{msgs.pvt_newfolder}" />
			    </h:commandLink>
    	    </h:panelGroup>
    	  </h:panelGrid>
			
		  <h:dataTable id="privateForums" value="#{forum.topics}" var="topic" width="100%" >
		    <h:column>
          
          <h:panelGrid columns="2" summary="" width="100%">
    	        <h:panelGroup styleClass="indnt2"  rendered="#{!topic.topic.mutable}">
    	          <h:commandLink action="#{PrivateMessagesTool.processPvtMsgTopic}" immediate="true" title=" #{topic.topic.title}"> 
    			        <h:graphicImage url="/images/dir_closed.gif" alt="" />
    			        <h:outputText value="  " />    
    			        <h:outputText value="#{topic.topic.title}" /> 
    			        <%-- <f:param value="#{topic.topic.title}" name="pvtMsgTopicTitle"/> --%>
    			        <f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId"/>
    		        </h:commandLink>
    		        <h:outputText value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.pvt_lowercase_msg}"/>
    		        <h:outputText value=" - #{topic.unreadNoMessages} #{msgs.pvt_unread}" rendered="#{topic.topic.title == 'Received'}"/>
    		        <h:outputText value="#{msgs.cdfm_closeb}"/>
    	        </h:panelGroup>
    	        <h:panelGroup rendered="#{!topic.topic.mutable}"><h:outputText value=" " /></h:panelGroup>

    	        <h:panelGroup styleClass="indnt2" rendered="#{topic.topic.mutable}">
    	          <h:commandLink action="#{PrivateMessagesTool.processPvtMsgTopic}" immediate="true" title=" #{topic.topic.title}"> 
					      <h:graphicImage url="/images/dir_closed.gif" alt="" />
    			        <h:outputText value="  " /> 
					      <h:outputText value="#{topic.topic.title}" /> 
	    			      <%-- <f:param value="#{topic.topic.title}" name="pvtMsgTopicTitle"/> --%>
	    			      <f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId"/>
	    			    </h:commandLink>
	    		      <h:outputText value=" #{msgs.cdfm_openb} #{topic.totalNoMessages} #{msgs.pvt_lowercase_msg}"/>
    		        <h:outputText value=" - #{topic.unreadNoMessages} #{msgs.pvt_unread}" rendered="#{topic.topic.title == 'Received' || topic.topic.title == 'Deleted'}"/>
    		        <h:outputText value="#{msgs.cdfm_closeb}"/>
    	        </h:panelGroup>
    	        <h:panelGroup styleClass="msgNav" rendered="#{topic.topic.mutable}">
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
  <h:panelGroup>	
	  <h:outputText rendered="#{PrivateMessagesTool.dispError}" value="#{msgs.pvt_mainpgerror}" />
  </h:panelGroup>  