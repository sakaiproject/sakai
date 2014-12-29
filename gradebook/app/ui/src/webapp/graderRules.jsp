<script src="js/gradebook.js" type="text/javascript"></script>
<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">

			<t:aliasBean alias="#{bean}" value="#{graderRulesBean}">
				<%@ include file="/inc/appMenu.jspf"%>
			</t:aliasBean>
	
			<sakai:flowState bean="#{graderRulesBean}" />
	
			<h2>
					<h:outputText value="#{msgs.grader_rules_title}" />
			</h2>
			
			<%@ include file="/inc/globalMessages.jspf"%>
			
			<h:panelGroup rendered="#{!graderRulesBean.assistantsDefined || (!graderRulesBean.categoriesDefined && !graderRulesBean.sectionsDefined)}">
				<f:verbatim><div class="indnt1 instruction"></f:verbatim>
					<h:outputText value="#{msgs.grader_rules_no_assistants}" rendered="#{!graderRulesBean.assistantsDefined}" />
					<h:outputText value="#{msgs.grader_rules_no_sections_categories}" rendered="#{graderRulesBean.assistantsDefined && (!graderRulesBean.categoriesDefined && !graderRulesBean.sectionsDefined)}" />
				<f:verbatim></div></f:verbatim>
			</h:panelGroup>
			
			<h:panelGroup rendered="#{graderRulesBean.assistantsDefined && (graderRulesBean.categoriesDefined || graderRulesBean.sectionsDefined)}">
			
				<f:verbatim><div class="instruction"></f:verbatim>
					<h:outputText value="#{msgs.grader_rules_instruction}" escape="false"/>
				<f:verbatim></div></f:verbatim>
				
				
				<f:verbatim><table class="graderRulesWrap"></f:verbatim>
					<f:verbatim><tr><td></f:verbatim>
						<h:outputLabel for="selectedGraderId" value="#{msgs.grader_rules_select_a_grader}" />
					<f:verbatim></td><td>&nbsp;</td></f:verbatim>
					<f:verbatim><tr><td valign="top"></f:verbatim>
						<h:selectOneMenu styleClass="graderRuleMenu" id="selectedGraderId" value="#{graderRulesBean.selectedGraderId}" valueChangeListener="#{graderRulesBean.processSelectGrader}" onchange="this.form.submit();">
							<f:selectItems value="#{graderRulesBean.graderNameSelectMenu}" />
						</h:selectOneMenu>
						<f:verbatim><br /><br /></f:verbatim>
						<h:commandLink action="#{graderRulesBean.processAddRule}" title="#{msgs.grader_rules_add_rule}" styleClass="graderRulesAdd"
								rendered="#{graderRulesBean.selectedGrader != null}">
							<h:graphicImage value="/../../library/image/silk/add.png" alt="#{msgs.grader_rules_add_rule}"/>
							<h:outputText value="#{msgs.grader_rules_add_rule} " />
						</h:commandLink>
					<f:verbatim></td></f:verbatim>
					<f:verbatim><td valign="top"></f:verbatim>
						<h:panelGroup rendered="#{graderRulesBean.selectedGrader != null}">
		
							<h:outputText value="#{msgs.grader_rules_no_rules}" rendered="#{!graderRulesBean.userHasRules}" styleClass="instruction"/>
										
							<t:dataTable
								value="#{graderRulesBean.selectedGrader.graderRules}"
								var="graderRule"
								rowIndexVar="rowIndex"
								rendered="#{graderRulesBean.userHasRules}">
								<h:column>
									<h:outputText value="#{msgs.grader_rules_can}"/>
								</h:column>
								<h:column>
									<h:selectOneMenu id="selectGradeOrView" value="#{graderRule.gradeOrViewValue}">
										<f:selectItems value="#{graderRulesBean.gradeOrViewMenu}" />
									</h:selectOneMenu>
								</h:column>
								<h:column rendered="#{graderRulesBean.categoriesDefined}">
									<h:selectOneMenu id="selectedCategoryId" value="#{graderRule.selectedCategoryId}" 
										 styleClass="graderRuleMenu">
										<f:selectItems value="#{graderRulesBean.categorySelectMenu}" />
									</h:selectOneMenu>
								</h:column>
								<h:column rendered="#{graderRulesBean.categoriesDefined && graderRulesBean.sectionsDefined}">
									<h:outputText value="#{msgs.grader_rules_in}" />
								</h:column>
								<h:column rendered="#{graderRulesBean.sectionsDefined}">
									<h:selectOneMenu id="selectedSectionUuid" value="#{graderRule.selectedSectionUuid}" 
										styleClass="graderRuleMenu">
										<f:selectItems value="#{graderRulesBean.sectionSelectMenu}" />
									</h:selectOneMenu>
								</h:column>
								<h:column>
									<h:commandLink actionListener="#{graderRulesBean.processRemoveRule}" title="#{msgs.grader_rules_remove_rule}">
										<h:graphicImage value="/../../library/image/silk/delete.png" alt="#{msgs.grader_rules_remove_rule}"/>
										<f:param name="rowIndex" value="#{rowIndex}"/>
									</h:commandLink>
								</h:column>
							</t:dataTable>
								
						</h:panelGroup>
					<f:verbatim></td></tr></f:verbatim>
				
				<f:verbatim></table></f:verbatim>
				
				<f:verbatim><div class="act calendarPadding"></f:verbatim>
				<h:commandButton
					id="saveButton"
					styleClass="active"
					value="#{msgs.grader_rules_save}"
					action="#{graderRulesBean.processSaveGraderRules}"
					rendered="#{graderRulesBean.selectedGrader != null}"/>
				<h:commandButton
					value="#{msgs.grader_rules_cancel}"
					action="#{graderRulesBean.processCancelChanges}" immediate="true"
					rendered="#{graderRulesBean.selectedGrader != null}"/>
			<f:verbatim></div></f:verbatim>
			
			</h:panelGroup>
			 
			<%
			  String thisId = request.getParameter("panel");
			  if (thisId == null) 
			  {
			    thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
			  }
			%>
			<script type="text/javascript">
				function resize(){
	  				mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
	  		}
			</script> 
			 
	  </h:form>
	</div>
</f:view>