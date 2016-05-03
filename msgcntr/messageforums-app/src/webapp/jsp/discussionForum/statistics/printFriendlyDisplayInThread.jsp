<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view  toolCssHref="/messageforums-tool/css/msgcntr.css">
	<h:form id="msgForum" rendered="#{ForumTool.instructor}">
		<!-- discussionForum/statistics/printFriendlyDisplayInThread.jsp-->	
		<ul class="navIntraTool actionToolBar">
			<li class="firstToolBarItem">
				<span>
					<h:graphicImage url="/../../library/image/silk/printer.png" alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
					<a id="printIcon" href="javascript:" onClick="javascript:window.print();">
						<h:outputText value="#{msgs.send_to_printer}" />
					</a>
				</span>
			</li>
			<li>
				<span>		
					<a value="" href="" onClick="window.close();" >
						<h:outputText value="#{msgs.close_window}" />
					</a>
				</span>
			</li>	
		</ul>
		 <f:verbatim><div class="breadCrumb idnt2"><h3></f:verbatim>
			<h:outputText value="#{msgs.cdfm_discussion_forums}"/>
			<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			<h:outputText value="#{msgs.stat_list}"/>
			<f:verbatim><h:outputText value="" /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			<h:outputText value="#{mfStatisticsBean.selectedSiteUser}"/>
			<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			<h:outputText value="#{msgs.stat_authored}"/>
			<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			<h:outputText value="#{ForumTool.selectedForum.forum.title}" />
			<f:verbatim><h:outputText value=" " /><h:outputText value=" / " /><h:outputText value=" " /></f:verbatim>
			<h:outputText value="#{ForumTool.selectedTopic.topic.title}" />
	    <f:verbatim></h3></div></f:verbatim>
		<div class="table-responsive">      
  		<mf:hierDataTable id="allMessagesForOneTopic" value="#{ForumTool.messages}" var="msgDecorateBean" noarrows="true" styleClass="table table-hover table-striped table-bordered" cellpadding="0" cellspacing="0" width="100%" columnClasses="bogus">	
   			<h:column id="_msg_subject">
	   			<h:panelGroup rendered="#{ForumTool.selectedMsgId!=msgDecorateBean.message.id}">
					<f:verbatim><div class="printBlock" style="margin:0"></f:verbatim>
						<f:verbatim><p style="border-bottom:1px solid #ccc;padding-bottom:5px;margin:0;font-size:110%;color:#000;font-weight:bold"></f:verbatim>
							<h:panelGroup rendered="#{!msgDecorateBean.message.deleted}">
								<h:outputText value="#{msgDecorateBean.message.title}" />
								<h:outputText  value= " - #{msgDecorateBean.anonAwareAuthor}" />
								<%-- TODO: include an option to display "(me)" in anonymous contexts.
									The inclusion / exclusion of "(me)" depends on the user's intention with the printout, so the user must be able to toggle this --%>
								<%-- h:outputText rendered="#{msgDecorateBean.currentUserAndAnonymous}" value=" #{msgs.cdfm_me}" / --%>
								<h:outputText value=" #{msgDecorateBean.message.created}">
									<f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
								</h:outputText>	
							</h:panelGroup>
							<h:panelGroup styleClass="inactive" rendered="#{msgDecorateBean.message.deleted}">
							 	<f:verbatim><span></f:verbatim>
								<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
								<f:verbatim></span></f:verbatim>
								<f:verbatim></div ></f:verbatim>
							</h:panelGroup>
						<f:verbatim></p></f:verbatim>
						<mf:htmlShowArea value="#{msgDecorateBean.message.body}" hideBorder="true" rendered="#{!msgDecorateBean.message.deleted}"/>
						<mf:htmlShowArea value="" hideBorder="true" rendered="#{msgDecorateBean.message.deleted}"/>	
					<f:verbatim></div></f:verbatim>
				</h:panelGroup>
				
				<h:panelGroup rendered="#{ForumTool.selectedMsgId==msgDecorateBean.message.id}">
					<f:verbatim><div class="printBlock" style="border:1px solid #aaa;margin:0"></f:verbatim>
						<f:verbatim><p style="border-bottom:1px solid #ccc;padding-bottom:5px;margin:0;font-size:110%;color:#000;font-weight:bold"></f:verbatim>
			
							<h:panelGroup rendered="#{!msgDecorateBean.message.deleted}">
								<h:outputText value="#{msgDecorateBean.message.title}" />
								<h:outputText  value= " - #{msgDecorateBean.anonAwareAuthor}" />
								<%-- TODO: include an option to display "(me)" in anonymous contexts.
									The inclusion / exclusion of "(me)" depends on the user's intention with the printout, so the user must be able to toggle this --%>
								<%-- h:outputText rendered="#{msgDecorateBean.currentUserAndAnonymous}" value=" #{msgs.cdfm_me}" / --%>
								<h:outputText value=" #{msgDecorateBean.message.created}">
									<f:convertDateTime pattern="#{msgs.date_format_paren}" timeZone="#{ForumTool.userTimeZone}" locale="#{ForumTool.userLocale}"/>
								</h:outputText>
							</h:panelGroup>
				
							<h:panelGroup styleClass="inactive" rendered="#{msgDecorateBean.message.deleted}">
								<f:verbatim><span></f:verbatim>
									<h:outputText value="#{msgs.cdfm_msg_deleted_label}" />
								<f:verbatim></span></f:verbatim>
								<f:verbatim></div ></f:verbatim>
							</h:panelGroup>
				
						<f:verbatim></p></f:verbatim>
						<mf:htmlShowArea value="#{msgDecorateBean.message.body}" hideBorder="true" rendered="#{!msgDecorateBean.message.deleted}"/>
						<mf:htmlShowArea value="" hideBorder="true" rendered="#{msgDecorateBean.message.deleted}"/>
					<f:verbatim></div></f:verbatim>
				</h:panelGroup>
	 		</h:column>
		</mf:hierDataTable>
		</div>
  	</h:form>
  </sakai:view>
 </f:view>
