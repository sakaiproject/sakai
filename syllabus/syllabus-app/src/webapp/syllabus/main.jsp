<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>

<f:view>
<f:loadBundle basename="org.sakaiproject.tool.syllabus.bundle.Messages" var="msgs"/>
	<sakai:view_container title="#{msgs.title_list}">
	<sakai:view_content>
		<h:form>
		  <sakai:tool_bar>
				<syllabus:syllabus_ifnot test="#{SyllabusTool.editAble}">
			  	<sakai:tool_bar_item
			    	action="#{SyllabusTool.processCreateAndEdit}"
						value="#{msgs.bar_create_edit}" />
				</syllabus:syllabus_ifnot>
   	  </sakai:tool_bar>

   	  <table width="100%">
  			<tr>
		  	  <td width="0%" />
   	  	  <td width="100%">
					</td>
				</tr>
			</table>

			<syllabus:syllabus_if test="#{SyllabusTool.syllabusItem.redirectURL}">
				<h:dataTable value="#{SyllabusTool.entries}" var="eachEntry" rendered="#{! SyllabusTool.syllabusItem.redirectURL}">
					<h:column>
						<sakai:panel_edit>
							<sakai:doc_section>
								<h:outputText/>
							</sakai:doc_section>
							<h:outputText value="#{eachEntry.entry.title}" 
								style="font-size:14px;font-weight:bold"/>
						
							<sakai:doc_section>
								<h:outputText/>
							</sakai:doc_section>
							<syllabus:syllabus_htmlShowArea value="#{eachEntry.entry.asset}" />
							
							<sakai:doc_section>
								<h:outputText/>
							</sakai:doc_section>
							<sakai:group_box>
								<h:dataTable value="#{eachEntry.attachmentList}" var="eachAttach">
								  <h:column>
										<f:facet name="header">
											<h:outputText value="" />
										</f:facet>
										<h:graphicImage url="/syllabus/excel.gif" rendered="#{eachAttach.type == 'application/vnd.ms-excel'}"/>
										<h:graphicImage url="/syllabus/html.gif" rendered="#{eachAttach.type == 'text/html'}"/>
										<h:graphicImage url="/syllabus/pdf.gif" rendered="#{eachAttach.type == 'application/pdf'}"/>
										<h:graphicImage url="/syllabus/ppt.gif" rendered="#{eachAttach.type == 'application/vnd.ms-powerpoint'}"/>
										<h:graphicImage url="/syllabus/text.gif" rendered="#{eachAttach.type == 'text/plain'}"/>
										<h:graphicImage url="/syllabus/word.gif" rendered="#{eachAttach.type == 'application/msword'}"/>

										<h:outputLink value="#{eachAttach.url}" target="_new_window">
											<h:outputText value="#{eachAttach.name}"  style="text-decoration:underline;"/>
										</h:outputLink>
									</h:column>
								</h:dataTable>
							</sakai:group_box>
						</sakai:panel_edit>
					</h:column>
				</h:dataTable>
				<h:outputText value="#{msgs.syllabus_noEntry}" style="font-size:10px;font-weight:bold" rendered="#{SyllabusTool.displayNoEntryMsg}"/>
			</syllabus:syllabus_if>				
			<syllabus:syllabus_ifnot test="#{SyllabusTool.syllabusItem.redirectURL}">
  			<syllabus:syllabus_iframe redirectUrl="#{SyllabusTool.syllabusItem.redirectURL}" width="750" height="500" />
			</syllabus:syllabus_ifnot>

		</h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
