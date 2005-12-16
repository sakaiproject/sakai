<%--********************* Private Message Area*********************--%>
<mf:forumHideDivision title="#{msgs.pvtarea_name}" id="_test_div">
<%--
  <mf:forum_bar_link value="Organize " action="#{PrivateMessagesTool.processPvtMsgOrganize}"/> &nbsp;
  <mf:forum_bar_link value=" Statistics " action="#{PrivateMessagesTool.processPvtMsgStatistics}"/> &nbsp;
 --%>
  <mf:forum_bar_link value=" Settings " action="#{PrivateMessagesTool.processPvtMsgSettings}" rendered="#{PrivateMessagesTool.instructor}"/> &nbsp;

  <h:outputText rendered="#{PrivateMessagesTool.pvtArea}"/>

  <h:dataTable width="100%" value="#{PrivateMessagesTool.decoratedForum}" var="forum" rendered="#{PrivateMessagesTool.pvtAreaEnabled}">
    <h:column >
    <f:verbatim><div class="forumsRow"><div class="forumsRowLeft"></f:verbatim>
    	
    	<h:commandLink action="#{PrivateMessagesTool.processHpView}" immediate="true">
  			<h:outputText value="#{PrivateMessagesTool.decoratedForum.forum.title}" />   
  		</h:commandLink>
  		
  		<f:verbatim></div></f:verbatim>
  		<f:verbatim><div class="rightAlign"></f:verbatim>
  			<h:commandLink action="#{PrivateMessagesTool.processPvtMsgCompose}"  value="Compose Private Message">
			</h:commandLink>
			<%-- currently new folder disabled
			<f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
  			<h:commandLink action="#{PrivateMessagesTool.processPvtMsgFolderSettingAdd}"  value="New Folder">
			</h:commandLink>	
			--%>		
			<f:verbatim></div></f:verbatim>
			<f:verbatim> </br></f:verbatim>
			<f:verbatim></div></f:verbatim>
			
		  <h:dataTable id="privateForums" width="90%" value="#{forum.topics}" var="topic"  >
		    <h:column rendered="#{!topic.topic.mutable}">
				<f:verbatim><div class="topicRows"></f:verbatim>
    		<h:commandLink action="#{PrivateMessagesTool.processPvtMsgTopic}" immediate="true"> 
    			<h:graphicImage url="/images/fldclosed.gif" />
    			<h:outputText value="  " />    
    			<h:outputText value="#{topic.topic.title}" /> 
    			<f:param value="#{topic.topic.title}" name="pvtMsgTopicTitle"/>
    			<f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId"/>
    		</h:commandLink>
    		<h:outputText id="topic_msg_count" value=" (#{topic.totalNoMessages} messages - #{topic.unreadNoMessages} unread)"/>
				</h:column>
					
				<h:column rendered="#{topic.topic.mutable}">
					 <f:verbatim><div class="topicRows"></f:verbatim>
					 <h:commandLink action="#{PrivateMessagesTool.processPvtMsgTopic}" immediate="true"> 
					 <h:outputText value="#{topic.topic.title}" /> 
	    			 <f:param value="#{topic.topic.title}" name="pvtMsgTopicTitle"/>
	    			 <f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId"/>
	    				</h:commandLink>
	    				<h:outputText id="topic_msg_count1" value=" (#{topic.totalNoMessages} messages - #{topic.unreadNoMessages} unread)"/>

					 <f:verbatim></div></f:verbatim>
					 <f:verbatim><div class="rightAlign"></f:verbatim>
					 <h:commandLink action="#{PrivateMessagesTool.processPvtMsgFolderSettings}"  value="Folder Settings">
					   <f:param value="#{topic.topic.title}" name="pvtMsgTopicTitle"/>
	    			 <f:param value="#{topic.topic.uuid}" name="pvtMsgTopicId"/>
					 </h:commandLink>
					 <f:verbatim></div></f:verbatim>
				</h:column>
				
		</h:dataTable>
	</h:column>
  </h:dataTable>       

</mf:forumHideDivision>  

  