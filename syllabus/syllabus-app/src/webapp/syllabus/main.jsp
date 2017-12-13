<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/syllabus" prefix="syllabus" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.syllabus.bundle.Messages"/>
</jsp:useBean>

	<sakai:view_container title="#{msgs.title_list}">
	<sakai:view_content>
	
	<%
	  	String thisId = request.getParameter("panel");
  		if (thisId == null) 
  		{
    		thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  		}
	%>

<script>includeLatestJQuery('main.jsp');</script>
<script type="text/javascript" src="/library/webjars/momentjs/2.11.1/min/moment.min.js"></script>
<script type="text/javascript" src="js/syllabus.js"></script>
<script type="text/javascript" src="js/jquery-ui-timepicker-addon.js"></script>
<link rel="stylesheet" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" type="text/css" />

<script type="text/javascript">
	var msgs;
	var mainframeId = '<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>';
	// if redirected, just open in another window else
	// open with size approx what actual print out will look like
	function printFriendly(url) {
		if (url.indexOf("printFriendly") === -1) {
			window.open(url,"mywindow");
		}
		else {
			window.open(url,"mywindow","width=960,height=1100,scrollbars=yes"); 
		}
	}
	
	$(function() {
		msgs = {
				syllabus_title: $("#messages #syllabus_title").html(),
				syllabus_content: $("#messages #syllabus_content").html(),
				clickToAddTitle: $("#messages #clickToAddTitle").html(),
				startdatetitle: $("#messages #startdatetitle").html(),
				enddatetitle: $("#messages #enddatetitle").html(),
				clickToAddStartDate: $("#messages #clickToAddStartDate").html(),
				clickToAddEndDate: $("#messages #clickToAddEndDate").html(),
				clickToAddBody: $("#messages #clickToAddBody").html(),
				saved: $("#messages #saved").html(),
				error: $("#messages #error").html(),
				required: $("#messages #required").html(),
				startBeforeEndDate: $("#messages #startBeforeEndDate").html(),
				calendarDatesNeeded: $("#messages #calendarDatesNeeded").html(),
				clickToExpandAndCollapse: $("#messages #clickToExpandAndCollapse").html(),
				bar_delete: $("#messages #bar_delete").html(),
				bar_cancel: $("#messages #bar_cancel").html(),
				confirmDelete: $("#messages #confirmDelete").html(),
				deleteItemTitle: $("#messages #deleteItemTitle").html(),
				deleteAttachmentTitle: $("#messages #deleteAttachmentTitle").html(),
				bar_new: $("#messages #bar_new").html(),
				bar_publish: $("#messages #bar_publish").html(),
				addItemTitle: $("#messages #addItemTitle").html(),
				draftTitlePrefix: $("#messages #draftTitlePrefix").html(),
				noUndoWarning: $("#messages #noUndoWarning").html()
			};
		setupAccordion('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>',<h:outputText value="#{SyllabusTool.editAble == 'true' ? true : false}"/>, msgs, 
							'<h:outputText value="#{SyllabusTool.openDataId}"/>');
					if(<h:outputText value="#{SyllabusTool.editAble == 'true'}"/>){
						//draft/publish toggle:
						setupToggleImages("publish", "publish", "publishOn", "publishOff", msgs);
						//Calendar Toggle
						setupToggleImages("linkCalendar", "linkCal", "linkCalOn", "linkCalOff", msgs);
						//Public/Private to the world toggle
						setupToggleImages("view", "linkWorld", "linkWorldOn", "linkWorldOff", msgs);
						}else{
						//remove CSS classes (otherwise you get those hover over "pencil edit" images
						$(".editItem").removeClass("editItem");
						}
	});
	
	function showConfirmDeleteHelper(deleteButton, event){
		showConfirmDelete(deleteButton, msgs, event);
	}
	
	function showConfirmDeleteAttachmentHelper(deleteButton, event){
		showConfirmDeleteAttachment(deleteButton, msgs, event);
	}
	
	function showConfirmAddHelper(){
		showConfirmAdd(msgs,'<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
	}
	
	// in case user includes the URL of a site that replaces top,
	// give them a way out. Handler is set up in the html file.
	// Unload it once the page is fully loaded.
	$(window).load(function () {
        window.onbeforeunload = null;
	});

	// in case an iframe tries to replace the top, we have to give the author a way
	// to get to the page to remove it. This will be cancelled in the javascript
	// after all content has loaded.
	window.onbeforeunload = function()
	{ return ""; };// default message is OK
	
</script>

<%-- gsilver: global things about syllabus tool:
1 ) what happens to empty lists - still generate a table?
2 ) Ids generated by jsf start with _  not optimal keeps us from validating fully.
 --%>
	<h:form id="syllabus">
		<%--gsilver: would be best if used all sakai tags, or none, 2 blocks
		following just gets tries to get around the mix --%>		
		<f:verbatim><ul class="navIntraTool actionToolbar"></f:verbatim>
				<c:if test="${SyllabusTool.addItem}">
				<f:verbatim>
				<li class="firstToolBarItem">
					<span>
							<a href="javascript:void(0)" onclick="showConfirmAddHelper();">
								</f:verbatim><h:outputText value="#{msgs.addItemTitle}"/><f:verbatim>
							</a>
							<input type="hidden" id="siteId" value="</f:verbatim><h:outputText value="#{SyllabusTool.siteId}"/><f:verbatim>">
					</span>
				</li></f:verbatim>
				</c:if>
				<c:if test="${SyllabusTool.bulkAddItem}">
				<f:verbatim>
				<li>
					<span>
					</f:verbatim>
						<h:commandLink action="#{SyllabusTool.processListNewBulkMain}">
							<h:outputText value="#{msgs.bar_new_bulk}"/>
						</h:commandLink>
					<f:verbatim>
					</span>
				</li>
				</f:verbatim>
				</c:if>
				<c:if test="${SyllabusTool.bulkEdit}">
				<f:verbatim>
				<li>
					<span>
					</f:verbatim>
						<h:commandLink action="#{SyllabusTool.processCreateAndEdit}">
							<h:outputText value="#{msgs.bar_create_edit}"/>
						</h:commandLink>
					<f:verbatim>
					</span>
				</li>
				</f:verbatim>
				</c:if>
				<c:if test="${SyllabusTool.redirect}">
				<f:verbatim>
				<li>
					<span>
					</f:verbatim>
						<h:commandLink action="#{SyllabusTool.processRedirect}">
							<h:outputText value="#{msgs.bar_redirect}"/>
						</h:commandLink>
					<f:verbatim>
					</span>
				</li>
				</f:verbatim>
				</c:if>
				<f:verbatim>
				<li>
					<span>
							<a href="javascript:void(0)" id="expandLink" onclick="expandAccordion('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>')">
					   <span class="fa fa-expand"></span>&nbsp;&nbsp;
								</f:verbatim>
									<h:outputText value="#{msgs.expandAll}"/>
								<f:verbatim>
							</a>
							<a href="javascript:void(0)" id="collapseLink" style="display:none" onclick="collapseAccordion('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>')">
					      <span class="fa fa-compress"></span>&nbsp;&nbsp;
								</f:verbatim>
									<h:outputText value="#{msgs.collapseAll}"/>
								<f:verbatim>
							</a>
					</span>
				</li>
				<li>
					<span>
					</f:verbatim>
						<h:outputLink id="print" value="javascript:printFriendly('#{SyllabusTool.printFriendlyUrl}');">
							<h:outputText value="#{msgs.printView}"/>
						</h:outputLink>
					<f:verbatim>
					</span>
				</li>
				</ul></f:verbatim>
			<syllabus:syllabus_if test="#{SyllabusTool.syllabusItem.redirectURL}">
					<f:verbatim>
						<div>
							<span id="successInfo" class="success popupMessage" style="display:none; float: left;"></span>
							<span id="warningInfo" class="alertMessage popupMessage" style="display:none; float: left;"></span>
						</div>
						<div id="accordion">
					</f:verbatim>
					<t:dataList value="#{SyllabusTool.entries}" var="eachEntry" layout="simple" styleClass="accordion-items-container">
						<f:verbatim><div><div class="group" syllabusItem="</f:verbatim>
						<h:outputText value="#{eachEntry.entry.syllabusId}"/>
						<f:verbatim>"><h3></f:verbatim>
						<f:subview id="actionIcons" rendered="#{SyllabusTool.editAble == 'true'}">
						    <f:verbatim>
							<span class="fa fa-arrows handleIcon" alt="</f:verbatim><h:outputText value="#{msgs.dragToReorder}"/><f:verbatim>" title="</f:verbatim><h:outputText value="#{msgs.dragToReorder}"/><f:verbatim>"></span>
							<span class="edit-actions">
							  <span class="fa fa-eye actionIcon publish publishOn" alt="</f:verbatim><h:outputText value="#{msgs.clickToUnpublish}"/><f:verbatim>" title="</f:verbatim><h:outputText value="#{msgs.clickToUnpublish}"/><f:verbatim>" style="</f:verbatim><h:outputText value="#{eachEntry.status == eachEntry.draftStatus ? 'display:none' : ''}"/><f:verbatim>"></span>
							  <span class="fa fa-eye-slash actionIcon publish publishOff" alt="</f:verbatim><h:outputText value="#{msgs.clickToPublish}"/><f:verbatim>" title="</f:verbatim><h:outputText value="#{msgs.clickToPublish}"/><f:verbatim>" style="</f:verbatim><h:outputText value="#{eachEntry.status == eachEntry.draftStatus ? '' : 'display:none'}"/><f:verbatim>"></span>
							  <span class="fa fa-calendar-check-o actionIcon linkCal linkCalOn" alt="</f:verbatim><h:outputText value="#{msgs.clickToRemoveCal}"/><f:verbatim>" title="</f:verbatim><h:outputText value="#{msgs.clickToRemoveCal}"/><f:verbatim>" style="</f:verbatim><h:outputText value="#{(eachEntry.entry.linkCalendar && SyllabusTool.calendarExistsForSite) ? '' : 'display:none'}"/><f:verbatim>"></span>
							  <span class="fa fa-calendar-times-o actionIcon linkCal linkCalOff" alt="</f:verbatim><h:outputText value="#{msgs.clickToAddCal}"/><f:verbatim>" title="</f:verbatim><h:outputText value="#{msgs.clickToAddCal}"/><f:verbatim>" style="</f:verbatim><h:outputText value="#{(eachEntry.entry.linkCalendar  && SyllabusTool.calendarExistsForSite) ? 'display:none' : ''}"/><f:verbatim>"></span>
							  <span class="fa fa-globe actionIcon linkWorld linkWorldOn" alt="</f:verbatim><h:outputText value="#{msgs.clickToHideWorld}"/><f:verbatim>" title="</f:verbatim><h:outputText value="#{msgs.clickToHideWorld}"/><f:verbatim>" style="</f:verbatim><h:outputText value="#{eachEntry.entry.view == 'yes' ? '' : 'display:none'}"/><f:verbatim>"></span>
							  <span class="fa fa-lock actionIcon linkWorld linkWorldOff" alt="</f:verbatim><h:outputText value="#{msgs.clickToAddWorld}"/><f:verbatim>" title="</f:verbatim><h:outputText value="#{msgs.clickToAddWorld}"/><f:verbatim>" style="</f:verbatim><h:outputText value="#{eachEntry.entry.view == 'no' ? '' : 'display:none'}"/><f:verbatim>"></span>
							  <span class="fa fa-trash actionImage delete" onclick="showConfirmDeleteHelper(this, event);" title="</f:verbatim><h:outputText value="#{msgs.clickToDelete}"/><f:verbatim>"></span>
							</span>
						    </f:verbatim>
						</f:subview>
						<f:verbatim><a href="javascript:void(0)" </f:verbatim>
							<f:subview id="draftclass" rendered="#{eachEntry.status == eachEntry.draftStatus}">
								<f:verbatim>class="draft"</f:verbatim>
							</f:subview>
							<f:verbatim>></f:verbatim>
							<h:outputText styleClass="draftTitlePrefix" rendered="#{eachEntry.status == eachEntry.draftStatus}" value="#{msgs.draftTitlePrefix}" />
							<h:outputText styleClass="" value="#{eachEntry.entry.title}" />
							<f:subview id="dateStudent" rendered="#{!SyllabusTool.editAble && (eachEntry.entry.startDate != null || eachEntry.entry.endDate != null)}">
								<f:verbatim><span style="float: right; padding-right: 1em; padding-left: 1em"></f:verbatim>
									<h:outputText value="#{eachEntry.entry.startDate}">
										<f:convertDateTime type="date" pattern="EEE MMM dd, yyyy hh:mm a"/>
									</h:outputText>
									<h:outputText value=" - " rendered="#{eachEntry.entry.startDate != null && eachEntry.entry.endDate != null}"/>
									<h:outputText value="#{eachEntry.entry.endDate}" rendered="#{!eachEntry.startAndEndDatesSameDay}">
								  		<f:convertDateTime type="date" pattern="EEE MMM dd, yyyy hh:mm a"/>
									</h:outputText>
									&nbsp;|&nbsp;<h:outputText value="#{eachEntry.entry.endDate}" rendered="#{eachEntry.startAndEndDatesSameDay}">
								  		<f:convertDateTime type="date" pattern="hh:mm a"/>
									</h:outputText>
								<f:verbatim></span></f:verbatim>
							</f:subview>
							<f:subview id="dateInstructor" rendered="#{SyllabusTool.editAble == 'true'}">
								<f:verbatim><span style="float: right; padding-right:1em; padding-left:1em"></f:verbatim>
									<h:outputText styleClass="" value="#{eachEntry.entry.startDate}">
										<f:convertDateTime type="date" pattern="yyyy/MM/dd h:mm a"/>
									</h:outputText>
									&nbsp;|&nbsp;<h:outputText styleClass="" value="#{eachEntry.entry.endDate}">
								  		<f:convertDateTime type="date" pattern="yyyy/MM/dd h:mm a"/>
									</h:outputText>
								<f:verbatim></span></f:verbatim>
							</f:subview>
						<f:verbatim>
							</a>
							</h3>
						</f:verbatim>
						<f:verbatim><div></f:verbatim>
							<f:verbatim><div class="" data-tpl='<textarea cols="120" id="textAreaWysiwyg" style="display:none"></textarea><img id="loading" style="margin: 2em;" src="images/loading.gif"/>'></f:verbatim>
							<h:outputText value="#{eachEntry.entry.asset}" escape="false"/>
							<f:verbatim></div></f:verbatim>
							
							<%/* view/add attachments */%>
							<h:dataTable value="#{eachEntry.attachmentList}" var="eachAttach" styleClass="indnt1">
								<h:column>
									<f:facet name="header">
										<h:outputText value="" />
									</f:facet>
									<sakai:contentTypeMap fileType="#{eachAttach.type}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
									<h:graphicImage id="exampleFileIcon" value="#{imagePath}" />	
									<h:outputLink styleClass="attachment" value="#{eachAttach.url}" target="_blank" title="#{msgs.openLinkNewWindow}">
										<h:outputText value=" "/><h:outputText value="#{eachAttach.name}"/>
									</h:outputLink>
									<f:subview id="removeItem" rendered="#{SyllabusTool.editAble == 'true'}">
										<f:verbatim>
										&nbsp;
											<a attachmentId='</f:verbatim><h:outputText value="#{eachAttach.syllabusAttachId}"/><f:verbatim>' 
												href="javascript:void(0)" onclick="showConfirmDeleteAttachmentHelper(this, event);" title="</f:verbatim><h:outputText value="#{msgs.clickToRemoveAttachment}"/><f:verbatim>">
												<span class="fa fa-trash" alt="</f:verbatim><h:outputText value="#{msgs.clickToRemoveAttachment}"/><f:verbatim>"></span>
											</a>
										</f:verbatim>
									</f:subview>
								</h:column>
							</h:dataTable>
							<f:subview id="instructorAddAttach" rendered="#{SyllabusTool.editAble == 'true'}">
								<f:verbatim>
									<br/>
								</f:verbatim>
								<h:commandLink action="#{SyllabusTool.processAddAttachRedirect}">
									<f:verbatim>
										<span class="fa fa-plus" alt=""></span>
									</f:verbatim>
									<h:outputText value="#{msgs.add_attach}"/>
									<f:param name="itemId" value="#{eachEntry.entry.syllabusId}"></f:param>
								</h:commandLink>
								<f:verbatim>
									<br/>
								</f:verbatim>
							</f:subview>
						<f:verbatim></div></div></div></f:verbatim>
					</t:dataList>
				<h:outputText value="#{msgs.syllabus_noEntry}" styleClass="instruction" rendered="#{SyllabusTool.displayNoEntryMsg}"/>
			</syllabus:syllabus_if>				
			<syllabus:syllabus_ifnot test="#{SyllabusTool.syllabusItem.redirectURL}">
				<br/>
				<h:outputText escape="false" value="#{msgs.redirect_explanation} " />
				<h:outputLink target="_blank" rel="noopener" title="#{msgs.openLinkNewWindow}" value="#{SyllabusTool.syllabusItem.redirectURL}">
					<h:outputText escape="false" value="#{SyllabusTool.syllabusItem.redirectURL}" />
				</h:outputLink>
			</syllabus:syllabus_ifnot>
			<f:verbatim>
				<div id="confirmDelete" style="display:none">
			</f:verbatim>
			<h:outputText value="#{msgs.confirmDelete}"/>
			<f:verbatim>
					<span id="deleteTitle"></span>
					<span id="deleteId" sylte="display:none"></span>
					<p class="act">
						<input type="button" name="deleteItem" value="</f:verbatim><h:outputText value="#{msgs.delConfRemoved}"/><f:verbatim>" title="</f:verbatim><h:outputText value="#{msgs.delConfRemoved}"/><f:verbatim>" onlclick="">
						<input type="button" name="cancelDelete" value="</f:verbatim><h:outputText value="#{msgs.cancel}"/><f:verbatim>" title="</f:verbatim><h:outputText value="#{msgs.cancel}"/><f:verbatim>" onlclick="">
					</p>
				</div>
		</f:verbatim>
        <f:verbatim><div style="padding-top: 600px" frameborder="0"></div></f:verbatim>
		</h:form>
		
		<!-- This section is used for internationalization for JS files
			This method is b/c of SAK-25424.  The original method was to use 
			saved: "<h:outputText value="#{msgs.saved}"/>"
			but that doesn't work for accent characters, hence this messages table below
		 -->
	
		<f:verbatim>
			<span id="messages" style="display:none">
				<span id="syllabus_title"></f:verbatim><h:outputText value="#{msgs.syllabus_title}"/><f:verbatim></span>
                <span id="syllabus_content"></f:verbatim><h:outputText value="#{msgs.syllabus_content}"/><f:verbatim></span>
				<span id="clickToAddTitle"></f:verbatim><h:outputText value="#{msgs.clickToAddTitle}"/><f:verbatim></span>
				<span id="startdatetitle"></f:verbatim><h:outputText value="#{msgs.startdatetitle}"/><f:verbatim></span>
				<span id="enddatetitle"></f:verbatim><h:outputText value="#{msgs.enddatetitle}"/><f:verbatim></span>
				<span id="clickToAddStartDate"></f:verbatim><h:outputText value="#{msgs.clickToAddStartDate}"/><f:verbatim></span>
				<span id="clickToAddEndDate"></f:verbatim><h:outputText value="#{msgs.clickToAddEndDate}"/><f:verbatim></span>
				<span id="clickToAddBody"></f:verbatim><h:outputText value="#{msgs.clickToAddBody}"/><f:verbatim></span>
				<span id="saved"></f:verbatim><h:outputText value="#{msgs.saved}"/><f:verbatim></span>
				<span id="error"></f:verbatim><h:outputText value="#{msgs.error}"/><f:verbatim></span>
				<span id="required"></f:verbatim><h:outputText value="#{msgs.required}"/><f:verbatim></span>
				<span id="startBeforeEndDate"></f:verbatim><h:outputText value="#{msgs.startBeforeEndDate}"/><f:verbatim></span>
				<span id="calendarDatesNeeded"></f:verbatim><h:outputText value="#{msgs.calendarDatesNeeded}"/><f:verbatim></span>
				<span id="clickToExpandAndCollapse"></f:verbatim><h:outputText value="#{msgs.clickToExpandAndCollapse}"/><f:verbatim></span>
				<span id="bar_delete"></f:verbatim><h:outputText value="#{msgs.bar_delete}"/><f:verbatim></span>
				<span id="bar_cancel"></f:verbatim><h:outputText value="#{msgs.bar_cancel}"/><f:verbatim></span>
				<span id="confirmDelete"></f:verbatim><h:outputText value="#{msgs.confirmDelete}"/><f:verbatim></span>
				<span id="deleteItemTitle"></f:verbatim><h:outputText value="#{msgs.deleteItemTitle}"/><f:verbatim></span>
				<span id="deleteAttachmentTitle"></f:verbatim><h:outputText value="#{msgs.deleteAttachmentTitle}"/><f:verbatim></span>
				<span id="bar_new"></f:verbatim><h:outputText value="#{msgs.bar_new}"/><f:verbatim></span>
				<span id="bar_publish"></f:verbatim><h:outputText value="#{msgs.bar_publish}" /><f:verbatim></span>
				<span id="addItemTitle"></f:verbatim><h:outputText value="#{msgs.addItemTitle}"/><f:verbatim></span>
				<span id="draftTitlePrefix"></f:verbatim><h:outputText value="#{msgs.draftTitlePrefix}"/><f:verbatim></span>
				<span id="noUndoWarning"></f:verbatim><h:outputText value="#{msgs.noUndoWarning}"/><f:verbatim></span>
			</span>
		</f:verbatim>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
