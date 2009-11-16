<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
  <sakai:view_container title="#{msgs.cdfm_revise_forum_msg}">
    <sakai:view_content>
<!--jsp/dfMsgRevise.jsp-->
	<h:form id="dfCompose">
			<style type="text/css">
				@import url("/sakai-messageforums-tool/css/msgcntr.css");
			</style>
	       		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
			<script type="text/javascript">
				$(document).ready(function() {
					$('#countme').click(function(e){
						$('#counttotal').text ((countStuff()));
						msgupdatecounts = $('.msg-updatecount').text();
						$('#countmetitle').text(msgupdatecounts);
						return null
					});					
				});
			</script>

     <h3><h:outputText value="#{msgs.cdfm_revise_forum_msg}" /></h3>

			<table class="topicBloc topicBlocLone">
				<tr>
					<td>
						<span class ="title">
							<h:outputText value="#{ForumTool.selectedForum.forum.title}-#{ForumTool.selectedTopic.topic.title}" />
						</span>
						<p class="textPanel">
	 <h:outputText value="#{ForumTool.selectedTopic.topic.shortDescription}"/>
						</p>
					</td>
				</tr>
			</table>	

	<p class="instruction">		
              <h:outputText value="#{msgs.cdfm_required}"/>
              <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStarInline" />
	  </p>
		<h:messages styleClass="alertMessage" id="errorMessages" />
		<h:panelGrid styleClass="jsfFormTable" columns="1" style="width: 100%;">
			<h:panelGroup>
				<h:outputLabel for="df_compose_title" style="display:block;float:none;clear:both;padding-bottom:.3em">
							     <h:outputText value="#{msgs.cdfm_info_required_sign}" styleClass="reqStar"/>
					<h:outputText value="#{msgs.cdfm_reply_title}" />
				</h:outputLabel>
					   <h:inputText value="#{ForumTool.composeTitle}" size="40" required="true" id="df_compose_title">
						 <f:validateLength minimum="1" maximum="255"/>
					   </h:inputText>
				   </h:panelGroup>
		</h:panelGrid>



	            <h:outputText value="#{msgs.cdfm_message}" />
			<a  id="countme" href="#" style="margin-left:3em"><img src="/library/image/silk/table_add.png" /> <span id="countmetitle"><h:outputText value="#{msgs.cdfm_message_count}" /></span></a>
			<span  id="counttotal" class="highlight"> </span>
			<h:outputText value="#{msgs.cdfm_message_count_update}" styleClass="msg-updatecount skip"/>		

	            <sakai:inputRichText value="#{ForumTool.composeBody}" id="df_compose_body" rows="22" cols="120">
					<f:validateLength maximum="65000"/>
				</sakai:inputRichText>
			<script language="javascript" type="text/javascript">
			 function countStuff() 
			 {
				var textInfo
					var textareas = document.getElementsByTagName("textarea");
					var rteId = textareas.item(0).id;
					var oEditor = FCKeditorAPI.GetInstance(rteId) ;
					var oDOM = oEditor.EditorDocument ;
					if ( document.all ) // If Internet Explorer.
					{
						 wordCount=oDOM.body.innerText.split(" ").length;
					}
					else // If Gecko.
					{
						var r = oDOM.createRange();	
						r.selectNodeContents(oDOM.body);
						wordCount = r.toString().split(" ").length;
					}
					msgupdatecounts = $('.msg-updatecount').text();
					textInfo = "(" + wordCount + ")"; 
					return textInfo;
				}
			</script>		
<%--********************* Attachment *********************--%>	
	        <h4>
	          <h:outputText value="#{msgs.cdfm_att}"/>
	        </h4>

			<p class="instruction">
	        <h:outputText value="#{msgs.cdfm_no_attachments}" rendered="#{empty ForumTool.attachments}"/>
			</p>
			
	
			<%-- designNote: moved rendered attribute from h:colun to dataTable - we do not want empty tables--%>
			<h:dataTable styleClass="attachPanel" id="attmsg" width="100%" value="#{ForumTool.attachments}" var="eachAttach"
					columnClasses="att,bogus,itemAction specialLink,bogus,bogus" rendered="#{!empty ForumTool.attachments}" style="width:auto">
					  <h:column>
							<f:facet name="header">
							</f:facet>
							<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
							<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />								
							</h:column>	
							<h:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.cdfm_title}"/>
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentName}"/>
							</h:column>
							<h:column>
								<h:commandLink action="#{ForumTool.processDeleteAttachRevise}" 
									immediate="true"
									onfocus="document.forms[0].onsubmit();"
									title="#{msgs.cdfm_remove}">
									<h:outputText value="#{msgs.cdfm_remove}"/>
<%--									<f:param value="#{eachAttach.attachmentId}" name="dfmsg_current_attach"/>--%>
									<f:param value="#{eachAttach.attachment.attachmentId}" name="dfmsg_current_attach"/>
								</h:commandLink>
						</h:column>
					  <h:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.cdfm_attsize}" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentSize}"/>
						</h:column>
					  <h:column>
							<f:facet name="header">
		  			    <h:outputText value="#{msgs.cdfm_atttype}" />
							</f:facet>
							<h:outputText value="#{eachAttach.attachment.attachmentType}"/>
						</h:column>
						</h:dataTable>   

			<p style="padding:0" class="act">
				<sakai:button_bar_item 
					rendered="#{empty ForumTool.attachments}"
						action="#{ForumTool.processAddAttachmentRedirect}" 
						value="#{msgs.cdfm_button_bar_add_attachment_redirect}" 
						immediate="true" 
						accesskey="a" 
						style="font-size:95%"/>
				<sakai:button_bar_item
					rendered="#{!empty ForumTool.attachments}"
						action="#{ForumTool.processAddAttachmentRedirect}"
						value="#{msgs.cdfm_button_bar_add_attachment_more_redirect}"
						immediate="true"
						accesskey="a"
						style="font-size:95%"/>
			 </p>
        		
			
<%--********************* Label *********************
				<sakai:panel_titled>
          <table width="80%" align="left">
            <tr>
              <td align="left" width="20%">
                <h:outputText value="Label"/>
              </td>
              <td align="left">
 							  <h:selectOneListbox size="1" id="viewlist">
            		  <f:selectItem itemLabel="Normal" itemValue="none"/>
          			</h:selectOneListbox>  
              </td>                           
            </tr>                                
          </table>
        </sakai:panel_titled>
--%>		        
			<p style="padding:0" class="act">
        <sakai:button_bar_item action="#{ForumTool.processDfMsgRevisedPost}" value="#{msgs.cdfm_button_bar_post_revised_msg}" accesskey="s" styleClass="active" />
       <%-- <sakai:button_bar_item action="#{ForumTool.processDfMsgSaveRevisedDraft}" value="#{msgs.cdfm_button_bar_save_draft}" /> --%>
        <sakai:button_bar_item action="#{ForumTool.processDfMsgRevisedCancel}" value="#{msgs.cdfm_button_bar_cancel}" immediate="true" accesskey="x" />
			</p>	
    </h:form>
     
    </sakai:view_content>
  </sakai:view_container>
</f:view> 

