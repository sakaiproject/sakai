<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<script src="/library/js/spinner.js" type="text/javascript"></script>
<f:view>
    <div class="portletBody">
        <%--TODO: determine how to make menu option unlinked not using this hack --%>
        <h:inputHidden id="pageName" value="#{spreadsheetUploadBean.unknownSize}#{spreadsheetUploadBean.pageName}" />
        
 		<%-- This form ends after step 1 since want to bypass validation of Step 3 --%>
        <h:form>
              <t:aliasBean alias="#{bean}" value="#{spreadsheetUploadBean}">
                <%@ include file="/inc/appMenu.jspf"%>
            </t:aliasBean>

            <sakai:flowState bean="#{spreadsheetUploadBean}" />
 
           <h2><h:outputText value="#{msgs.import_entire_main_title}"/></h2>
            <div class="instruction">
                <h:outputText value="#{msgs.import_entire_instructions}" escape="false"/>
            </div>
            <p/>
            <%@ include file="/inc/globalMessages.jspf"%>
            
            <%-- To display formatted error messages that occured during import --%>
            <%@ include file="/inc/importErrorMessages.jspf" %>
            
            <h:outputText value="#{spreadsheetUploadBean.externallyMaintainedImportMsg}"
            	rendered="#{spreadsheetUploadBean.externallyMaintainedImportMsg != null}"
            	styleClass="alertMessage" escape="false"/>
 			
 			
		</h:form>            

            <%-- Step 1: Download template --%>
 	            <h4><h:outputText value="#{msgs.import_entire_template}"/></h4>
 	            <br />
		        <h:form id="gbExportForm">
              <h:graphicImage value="/../../library/image/sakai/excel.gif" alt="" />
              <h:commandLink actionListener="#{rosterBean.exportXlsNoCourseGrade}">
                <h:outputText value="#{msgs.import_entire_template_excel}"/>
              </h:commandLink>
              
              <h:outputText value="#{msgs.import_entire_template_or}" />
                
 	    	      <h:graphicImage value="images/silk/page_white.png" alt="" />
    	    	  <h:commandLink actionListener="#{rosterBean.exportCsvNoCourseGrade}">
        	      <h:outputText value="#{msgs.import_entire_template_csv}"/>
 	        		</h:commandLink>
				</h:form> <%-- End of download csv file form --%>
			
			<%-- Step 2: Edit Spreadsheet --%>
			<br />
			<h4><h:outputText value="#{msgs.import_entire_edit}" /></h4>
			<br />
			<h:outputText value="#{msgs.import_entire_edit_inst}" />
			
			<%-- Step 3: Import Spreadsheet --%>
			<br /><br />
       		<h:form id="gbForm" enctype="multipart/form-data">
 				<h4><h:outputText value="#{msgs.import_entire_import}" /></h4>
				<br />
				<h:outputText value="#{msgs.import_entire_import_inst}" />
				<f:verbatim>
					<table>
						<tr>
						<td>
				</f:verbatim>
					<%/*
           			<h:outputLabel for="fileupload">
                    	<h:outputText id="fileuploadLabel" value="#{msgs.upload_view_choose_file}"/>
            		</h:outputLabel>
            		<t:inputFileUpload id="fileupload" value="#{spreadsheetUploadBean.upFile}" storage="file" required="true" accept="application/vnd.ms-excel"/>
					*/%>
            		<h:outputLabel for="pickedFileDesc" value="#{msgs.upload_view_choose_file}"/>
                	<h:inputText id="pickedFileDesc" value="#{spreadsheetUploadBean.pickedFileDesc}" required="true" readonly="true"/>
            		<f:verbatim>
            			</td>
            			<td>
            		</f:verbatim>
                	<h:commandButton actionListener="#{spreadsheetUploadBean.launchFilePicker}" immediate="true" value="#{msgs.upload_view_choose_file_button}" onclick="SPNR.disableControlsAndSpin( this, null );">
                	</h:commandButton>
					<%/*
            		<h:message for="fileupload" styleClass="validationEmbedded" />
					*/%>
                	<h:message for="pickedFileDesc" styleClass="validationEmbedded" />
            		<f:verbatim>
            			</td>
            			</tr>
            			</table>
            		</f:verbatim>            		

           <%-- Button to do the actual import --%>
           <p>
                <h:commandButton
                     id="saveButton"
                     styleClass="active"
                     value="#{msgs.import_entire_button_title}"
                     action="#{spreadsheetUploadBean.processFileEntire}"
                     onclick="SPNR.disableControlsAndSpin( this, null );" />
            </p>
   
         </h:form> <%-- End of upload completed spreadsheet form --%>
     </div>
 </f:view>
