<%--********************* Private Message Area*********************--%>

<mf:forumHideDivision title="#{PrivateMessagesTool.area.name}" id="_test_div">
  <mf:forum_bar_link value="Organize " action="#{PrivateMessagesTool.processPvtMsgOrganize}"/> &nbsp;
  <mf:forum_bar_link value=" Statistics " action="#{PrivateMessagesTool.processPvtMsgStatistics}"/> &nbsp;
  <mf:forum_bar_link value=" Settings " action="#{PrivateMessagesTool.processPvtMsgSettings}"/> &nbsp;

<h:outputText style="font-weight:bold" value="#{PrivateMessagesTool.decoratedForum.forum.title}" />                  
<h:dataTable id="privateForums" value="#{PrivateMessagesTool.decoratedForum.topics}" var="topic"> 
  <h:column>  
    <h:commandLink action="#{PrivateMessagesTool.processPvtMsgTopic}" immediate="true">   
    <h:outputText value="#{topic.topic.title}" /> 
    <f:param value="#{topic.topic.title}" name="pvtMsgTopicTitle"/>
    </h:commandLink>
    <h:outputText id="topic_msg_count" value="(#{topic.totalNoMessages} messages - #{topic.unreadNoMessages} unread)"/>
		
  </h:column>     
</h:dataTable>             

</mf:forumHideDivision>  