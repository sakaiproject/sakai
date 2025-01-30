<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
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
<script src="js/syllabus.js"></script>
<script src="/library/js/lang-datepicker/lang-datepicker.js"></script>
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
				deleted: $("#messages #deleted").html(),
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

		var menuLink = $('#syllabusMenuMainLink');
		menuLink.addClass('current');
		menuLink.find('a').removeAttr('href');
		$('#expandLink').on('click', function () {
			expandAccordion(mainframeId);
		});
		$('#collapseLink').on('click', function () {
			collapseAccordion(mainframeId);
		});

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

	<%--
	in case user includes the URL of a site that replaces top,
	give them a way out. Handler is set up in the html file.
	Unload it once the page is fully loaded.
	--%>
	$(window).load(function () {
        window.onbeforeunload = null;
	});

	<%--
	in case an iframe tries to replace the top, we have to give the author a way
	to get to the page to remove it. This will be cancelled in the javascript
	after all content has loaded.
	--%>
	window.onbeforeunload = function()
	{ return ""; };// default message is OK

</script>

	<%--
	gsilver: global things about syllabus tool:
	1 ) what happens to empty lists - still generate a table?
	2 ) Ids generated by jsf start with _  not optimal keeps us from validating fully.
	--%>
	<h:form id="syllabus">
		<input type="hidden" id="siteId" value="<h:outputText value="#{SyllabusTool.siteId}"/>">
		<%@ include file="mainMenu.jsp" %>
		<br/>
		<%@ include file="mainControls.jsp" %>
			<syllabus:syllabus_if test="#{SyllabusTool.syllabusItem.redirectURL}">
					<h:panelGroup layout="block" styleClass="instruction" rendered="#{SyllabusTool.editAble == 'true'}">
						<h:outputText value="#{msgs.draftInstruction}" />
					</h:panelGroup>
						<div>
							<span id="successInfo" class="sak-banner-success popupMessage" style="display:none; float: left;"></span>
							<span id="warningInfo" class="sak-banner-warn popupMessage" style="display:none; float: left;"></span>
						</div>

						<span id="lastMoveArray" class="d-none"></span>
						<span id="lastMoveArrayInit" class="d-none"></span>
						<span id="lastItemMoved" class="d-none"></span>

						<div id="accordion">
					<t:dataList value="#{SyllabusTool.entries}" var="eachEntry" layout="simple" styleClass="accordion-items-container" id="reorder-list">
						<div class="reorder-element">
						<f:verbatim><div class="group" syllabusItem="</f:verbatim> <h:outputText value="#{eachEntry.entry.syllabusId}"/> <f:verbatim>"></f:verbatim>
						<h3>

							<h:panelGroup rendered="#{eachEntry.status == eachEntry.draftStatus}">
								<a href="#" class="draft" onclick="event.preventDefault();">
							</h:panelGroup>
							<h:panelGroup rendered="#{eachEntry.status != eachEntry.draftStatus}">
								<a href="#" onclick="event.preventDefault();">
							</h:panelGroup>

							<h:outputText styleClass="draftTitlePrefix" rendered="#{eachEntry.status == eachEntry.draftStatus}" value="#{msgs.draftTitlePrefix}" />
							<h:outputText styleClass="syllabusItemTitle" value="#{eachEntry.entry.title}" />

							<f:subview id="dateStudent" rendered="#{!SyllabusTool.editAble && (eachEntry.entry.startDate != null || eachEntry.entry.endDate != null)}">
								<span style="float: right; padding-right: 1em; padding-left: 1em">
									<h:outputText value="#{eachEntry.entry.startDate}">
										<f:convertDateTime dateStyle="medium" timeStyle="short" />
									</h:outputText>
									<h:outputText value=" - " rendered="#{eachEntry.entry.startDate != null && eachEntry.entry.endDate != null}"/>
									<h:outputText value="#{eachEntry.entry.endDate}" rendered="#{!eachEntry.startAndEndDatesSameDay}">
										<f:convertDateTime dateStyle="medium" timeStyle="short" />
									</h:outputText>
									<h:outputText value="#{eachEntry.entry.endDate}" rendered="#{eachEntry.startAndEndDatesSameDay}">
								  		<f:convertDateTime type="date" pattern="hh:mm a"/>
									</h:outputText>
								</span>
							</f:subview>
							<f:subview id="dateInstructor" rendered="#{SyllabusTool.editAble == 'true'}">
								<span style="float: right; padding-right:1em; padding-left:1em">
									<h:outputText styleClass="" value="#{eachEntry.entry.startDate}">
										<f:convertDateTime dateStyle="medium" timeStyle="short" />
									</h:outputText>
									<h:outputText value=" - " rendered="#{eachEntry.entry.startDate != null && eachEntry.entry.endDate != null}"/>
									<h:outputText value="#{eachEntry.entry.endDate}" rendered="#{!eachEntry.startAndEndDatesSameDay}">
										<f:convertDateTime dateStyle="medium" timeStyle="short" />
									</h:outputText>
									<h:outputText value="#{eachEntry.entry.endDate}" rendered="#{eachEntry.startAndEndDatesSameDay}">
								  		<f:convertDateTime type="date" pattern="hh:mm a"/>
									</h:outputText>
								</span>
							</f:subview>
							</a>
							</h3>
						<div>
						<f:verbatim><div id="textAreaWysiwyg-</f:verbatim><h:outputText value="#{eachEntry.entry.syllabusId}"/><f:verbatim>"></f:verbatim>
							<h:outputText value="#{eachEntry.entry.asset}" escape="false"/>
						</div>
							<%-- /* view/add attachments */ --%>
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
								</h:column>
							</h:dataTable>
						</div>
					</div>
					</div>
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
        <div style="padding-top: 600px" frameborder="0"></div>
		</h:form>

		<%--
		This section is used for internationalization for JS files, this method is b/c of SAK-25424.
		The original method was to use saved: "<h:outputText value="#{msgs.saved}"/>" but that doesn't work for accent characters, hence this messages table below
		--%>
	
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
				<span id="deleted"></f:verbatim><h:outputText value="#{msgs.deleted}"/><f:verbatim></span>
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
