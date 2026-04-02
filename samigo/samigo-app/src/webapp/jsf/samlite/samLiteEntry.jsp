<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<!--
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006, 2007 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
  <f:view>
  
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      	<title><h:outputText value="#{samLiteMessages.samlite_title}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
		<div class="portletBody container-fluid">
		<!-- content... -->
		 <h:form id="samLiteEntryForm">
		 
		 <!-- HEADINGS -->
  		 <%@ include file="/jsf/author/allHeadings.jsp" %>
  		 <h3>
   	 		<h:outputText value="#{samLiteMessages.samlite_title}"/>
  	     </h3>
  	     <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
	     <h4><h:outputText value="#{samLiteMessages.samlite_step_1_of_2}"/></h4>
	     <div class="instructions"><h:outputText value="#{samLiteMessages.samlite_instructions}"/></div>
		 <div><h4><h:outputText value="#{samLiteMessages.samlite_infogroup}"/></h4></div>	
		 <br/>
		 <div class="form-group row">
		 	<h:outputLabel for="name" value="#{samLiteMessages.samlite_name}" styleClass="form-label col-md-2"/>
		 	<div class="col-md-8">
		 		<h:inputText id="name" value="#{samLiteBean.name}" size="50" styleClass="form-control"/>
		 	</div>
		 </div>

		 <div class="form-group row">
		 	<h:outputLabel for="description" value="#{samLiteMessages.samlite_description}" styleClass="form-label col-md-2"/>
		 	<div class="col-md-8">
		 		<h:inputTextarea id="description" value="#{samLiteBean.description}" rows="2" cols="40" styleClass="form-control"/>
		 	</div>
		 </div>

		 <div class="navIntraTool"><strong><h:outputText value="#{samLiteMessages.samlite_textgroup}"/></strong></div>
		 <br/>
		 <div class="instruction samlite-instructions"><h:outputText value="#{samLiteMessages.samlite_textgroup_instructions}"/></div>
		 <div class="row">
			<div class="col-md-6">
				<h:panelGroup layout="block" styleClass="toggle_link_container_for_richtextarea" rendered="#{!samLiteBean.richTextarea}">
					<a class="toggle_link" id="samLiteEntryForm:data_toggle" href="javascript:pre_show_editor()">
						<h:outputText value="#{samLiteMessages.rich_text_label}"/>
					</a>
				</h:panelGroup>
				<h:inputHidden value="#{samLiteBean.samigoFrameId}" id="data_textinput_current_status"/>
				<h:inputHidden value="#{samLiteBean.data}" id="data_value"/>
				<h:inputTextarea id="data" value="#{samLiteBean.data}" styleClass="samlite-input-textarea" rows="15" />

				<div class="samlite-button-group-spacing">
					<%-- immediate=true bypasses the valueChangeListener --%>
					<h:commandButton value="#{samLiteMessages.samlite_cancel}" type="submit" action="author" immediate="true"/>
					<%-- activates the valueChangeListener --%>
					<h:commandButton value="#{samLiteMessages.samlite_validate}" type="submit" styleClass="active ms-1" action="#{samLiteBean.getOutcome}">
						<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.samlite.ParserListener" />
					</h:commandButton>
				</div>
			</div>
			<div class="col-md-6">
				<div class="accordion" id="samliteFormatAccordion">
					<h:panelGroup layout="block" styleClass="accordion-item samlite-accordion-item">
						<h2 class="accordion-header" id="headingGeneralInstructions">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseGeneralInstructions" aria-expanded="false" aria-controls="collapseGeneralInstructions">
								<h:outputText value="#{samLiteMessages.general_instructions_label}"/>
							</button>
						</h2>
						<div id="collapseGeneralInstructions" class="accordion-collapse collapse" aria-labelledby="headingGeneralInstructions" data-bs-parent="#samliteFormatAccordion">
							<div class="accordion-body">
								<div class="card-body">
									<p><h:outputText value="#{samLiteMessages.general_instructions_prelude}"/></p>
									<ul>
										<li><h:outputText value="#{samLiteMessages.general_instructions_format1}"/></li>
										<li><h:outputText value="#{samLiteMessages.general_instructions_format2}"/></li>
										<li><h:outputText value="#{samLiteMessages.general_instructions_format3}"/></li>
										<li><h:outputText value="#{samLiteMessages.general_instructions_format4}"/></li>
										<li><h:outputText value="#{samLiteMessages.general_instructions_format5}"/></li>
									</ul>
									<p><h:outputText value="#{samLiteMessages.general_instructions_conclude}"/></p>
									<h:outputText value="#{samLiteMessages.general_instructions_feedback}"/>
								</div>
							</div>
						</div>
					</h:panelGroup>

					<h:panelGroup layout="block" styleClass="accordion-item samlite-accordion-item" rendered="#{!samLiteBean.richTextarea}">
						<h2 class="accordion-header" id="headingRichEditorInstructions">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseRichEditorInstructions" aria-expanded="false" aria-controls="collapseRichEditorInstructions">
								<h:outputText value="#{samLiteMessages.rich_editor_instructions_label}"/>
							</button>
						</h2>
						<div id="collapseRichEditorInstructions" class="accordion-collapse collapse" aria-labelledby="headingRichEditorInstructions" data-bs-parent="#samliteFormatAccordion">
							<div class="accordion-body">
								<div class="card-body">
									<p><h:outputText value="#{samLiteMessages.rich_editor_instructions_prelude}"/></p>
									<p><h:outputText value="#{samLiteMessages.rich_editor_instructions_prelude_2}"/></p>
									<ol>
										<li><h:outputText value="#{samLiteMessages.rich_editor_instructions_recomendation_1}"/></li>
										<li><h:outputText value="#{samLiteMessages.rich_editor_instructions_recomendation_2}"/></li>
										<li><h:outputText value="#{samLiteMessages.rich_editor_instructions_recomendation_3}"/></li>
										<li><h:outputText value="#{samLiteMessages.rich_editor_instructions_recomendation_4}"/></li>
										<li><h:outputText value="#{samLiteMessages.rich_editor_instructions_recomendation_5}"/></li>
									</ol>
								</div>
							</div>
						</div>
					</h:panelGroup>

					<h:panelGroup layout="block" styleClass="accordion-item samlite-accordion-item">
						<h2 class="accordion-header" id="headingExampleMcQuestion">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseExampleMcQuestion" aria-expanded="false" aria-controls="collapseExampleMcQuestion">
								<h:outputText value="#{samLiteMessages.example_mc_label}"/>
							</button>
						</h2>
						<div id="collapseExampleMcQuestion" class="accordion-collapse collapse" aria-labelledby="headingExampleMcQuestion" data-bs-parent="#samliteFormatAccordion">
							<div class="accordion-body">
								<div class="samlite-card-separator">
									<div class="card-body">
										<p><h:outputText value="#{samLiteMessages.example_mc_instructions}"/></p>
										<p><h:outputText value="#{samLiteMessages.example_mc_instructions_1}"/></p>
										<p><h:outputText value="#{samLiteMessages.example_mc_instructions_2}"/></p>
										<h:outputText styleClass="sak-banner-info" value="#{samLiteMessages.general_instructions_important}"/>
									</div>
								</div>
								<div class="card">
									<div class="card-header">
										<h:outputText value="#{samLiteMessages.example_example_label}"/>
									</div>
									<div class="card-body">
										<pre style="white-space:normal;">
											<h:outputText value="#{samLiteMessages.example_mc_question_format4}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mc_question_text}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mc_answer_a}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mc_answer_b}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mc_answer_c}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mc_answer_d}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mc_question_random}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mc_question_rationale}"/>
										</pre>
									</div>
								</div>
							</div>
						</div>
					</h:panelGroup>

					<h:panelGroup layout="block" styleClass="accordion-item samlite-accordion-item">
						<h2 class="accordion-header" id="headingExampleMcmcQuestion">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseExampleMcmcQuestion" aria-expanded="false" aria-controls="collapseExampleMcmcQuestion">
								<h:outputText value="#{samLiteMessages.example_mcmc_label}"/>
							</button>
						</h2>
						<div id="collapseExampleMcmcQuestion" class="accordion-collapse collapse" aria-labelledby="headingExampleMcmcQuestion" data-bs-parent="#samliteFormatAccordion">
							<div class="accordion-body">
								<div class="samlite-card-separator">
									<div class="card-body">
										<p><h:outputText value="#{samLiteMessages.example_mcmc_instructions}"/></p>
										<p><h:outputText value="#{samLiteMessages.example_mcmc_instructions_1}"/></p>
										<p><h:outputText value="#{samLiteMessages.example_mcmc_instructions_2}"/></p>
										<h:outputText styleClass="sak-banner-info" value="#{samLiteMessages.general_instructions_important}"/>
									</div>
								</div>
								<div class="card">
									<div class="card-header">
										<h:outputText value="#{samLiteMessages.example_example_label}"/>
									</div>
									<div class="card-body">
										<pre style="white-space:normal;">
											<h:outputText value="#{samLiteMessages.example_mcmc_question_format4}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mcmc_question_text}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mcmc_answer_a}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mcmc_answer_b}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mcmc_answer_c}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mcmc_question_random}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mcmc_question_rationale}"/>
										</pre>
									</div>
								</div>
							</div>
						</div>
					</h:panelGroup>

					<h:panelGroup layout="block" styleClass="accordion-item samlite-accordion-item">
						<h2 class="accordion-header" id="headingExampleFibQuestion">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseExampleFibQuestion" aria-expanded="false" aria-controls="collapseExampleFibQuestion">
								<h:outputText value="#{samLiteMessages.example_fib_label}"/>
							</button>
						</h2>
						<div id="collapseExampleFibQuestion" class="accordion-collapse collapse" aria-labelledby="headingExampleFibQuestion" data-bs-parent="#samliteFormatAccordion">
							<div class="accordion-body">
								<div class="samlite-card-separator">
									<div class="card-body">
										<p><h:outputText value="#{samLiteMessages.example_fib_instructions}"/></p>
										<p><h:outputText styleClass="sak-banner-info" value="#{samLiteMessages.general_instructions_important}"/></p>
									</div>
								</div>
								<div class="card">
									<div class="card-header">
										<h:outputText value="#{samLiteMessages.example_example_label}"/>
									</div>
									<div class="card-body">
										<pre style="white-space:normal;">
											<h:outputText value="#{samLiteMessages.example_fib_question_format4}"/><br/>
											<h:outputText value="#{samLiteMessages.example_fib_question_text}"/><br/>
											<h:outputText value="#{samLiteMessages.example_fib_answer}"/>
										</pre>
									</div>
								</div>
							</div>
						</div>
					</h:panelGroup>

					<h:panelGroup layout="block" styleClass="accordion-item samlite-accordion-item">
						<h2 class="accordion-header" id="headingExampleSeQuestion">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseExampleSeQuestion" aria-expanded="false" aria-controls="collapseExampleSeQuestion">
								<h:outputText value="#{samLiteMessages.example_se_label}"/>
							</button>
						</h2>
						<div id="collapseExampleSeQuestion" class="accordion-collapse collapse" aria-labelledby="headingExampleSeQuestion" data-bs-parent="#samliteFormatAccordion">
							<div class="accordion-body">
								<div class="samlite-card-separator">
									<div class="card-body">
										<p><h:outputText value="#{samLiteMessages.example_se_instructions}"/></p>
										<p><h:outputText styleClass="sak-banner-info" value="#{samLiteMessages.general_instructions_important}"/></p>
									</div>
								</div>
								<div class="card">
									<div class="card-header">
										<h:outputText value="#{samLiteMessages.example_example_label}"/>
									</div>
									<div class="card-body">
										<pre style="white-space:normal;">
											<h:outputText value="#{samLiteMessages.example_se_question_format4}"/><br/>
											<h:outputText value="#{samLiteMessages.example_se_question_text}"/>
										</pre>
									</div>
								</div>
							</div>
						</div>
					</h:panelGroup>

					<h:panelGroup layout="block" styleClass="accordion-item samlite-accordion-item">
						<h2 class="accordion-header" id="headingExampleTfQuestion">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseExampleTfQuestion" aria-expanded="false" aria-controls="collapseExampleTfQuestion">
								<h:outputText value="#{samLiteMessages.example_tf_label}"/>
							</button>
						</h2>
						<div id="collapseExampleTfQuestion" class="accordion-collapse collapse" aria-labelledby="headingExampleTfQuestion" data-bs-parent="#samliteFormatAccordion">
							<div class="accordion-body">
								<div class="samlite-card-separator">
									<div class="card-body">
										<p><h:outputText value="#{samLiteMessages.example_tf_instructions}"/></p>
										<p><h:outputText value="#{samLiteMessages.example_mcmc_instructions_2}"/></p>
										<p><h:outputText styleClass="sak-banner-info" value="#{samLiteMessages.general_instructions_important}"/></p>
									</div>
								</div>
								<div class="card">
									<div class="card-header">
										<h:outputText value="#{samLiteMessages.example_example_label}"/>
									</div>
									<div class="card-body">
										<pre style="white-space:normal;">
											<h:outputText value="#{samLiteMessages.example_tf_question_format4}"/><br/>
											<h:outputText value="#{samLiteMessages.example_tf_question_text}"/><br/>
											<h:outputText value="#{samLiteMessages.example_tf_answer_a}"/><br/>
											<h:outputText value="#{samLiteMessages.example_tf_answer_b}"/><br/>
											<h:outputText value="#{samLiteMessages.example_mcmc_question_rationale}"/>
										</pre>
									</div>
								</div>
							</div>
						</div>
					</h:panelGroup>

					<h:panelGroup layout="block" styleClass="accordion-item samlite-accordion-item">
						<h2 class="accordion-header" id="headingExampleFnQuestion">
							<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#collapseExampleFnQuestion" aria-expanded="false" aria-controls="collapseExampleFnQuestion">
								<h:outputText value="#{samLiteMessages.example_fn_label}"/>
							</button>
						</h2>
						<div id="collapseExampleFnQuestion" class="accordion-collapse collapse" aria-labelledby="headingExampleFnQuestion" data-bs-parent="#samliteFormatAccordion">
							<div class="accordion-body">
								<div class="samlite-card-separator">
									<div class="card-body">
										<p><h:outputText value="#{samLiteMessages.example_fn_instructions}"/></p>
										<p><h:outputText value="#{samLiteMessages.example_fn_instructions1}"/></p>
										<p><h:outputText value="#{samLiteMessages.example_fn_instructions2}"/></p>
										<p><h:outputText value="#{samLiteMessages.example_fn_instructions3}"/></p>
										<p><h:outputText styleClass="sak-banner-info" value="#{samLiteMessages.general_instructions_important}"/></p>
									</div>
								</div>
								<div class="card">
									<div class="card-header">
										<h:outputText value="#{samLiteMessages.example_example_label}"/>
									</div>
									<div class="card-body">
										<pre style="white-space:normal;">
											<h:outputText value="#{samLiteMessages.example_fn_question_format4}"/><br/>
											<h:outputText value="#{samLiteMessages.example_fn_question_text}"/><br/>
											<h:outputText value="#{samLiteMessages.example_fn_answer}"/>
										</pre>
									</div>
								</div>
							</div>
						</div>
					</h:panelGroup>
				</div>
		 	</div>
		 </div>
		</h:form>
		</div>
 		<!-- end content -->
		<script>
			function pre_show_editor() {
				show_editor('samLiteEntryForm:data', document.getElementById('samLiteEntryForm:data_textinput_current_status').value, 1000000);
			}
			function chef_setupformattedtextarea(client_id, shouldToggle, frame_id, max_chars) {
				if (shouldToggle == true) {
					var input_text = document.getElementById(client_id);
					var input_text_value = input_text.value;
					var input_text_encoded = encodeHTML(input_text_value);
					input_text.value = input_text_encoded;
				}

				config = ''
				if (max_chars) {
					config = 
						{
							wordcount: {'maxCharCount' : max_chars},
							toolbarCanCollapse: true,
							toolbar: 'Basic',
							toolbar_Basic: [
								['Bold','Italic','Underline','-','TextColor','BGColor','-','Subscript','Superscript','-','Image','Link','Unlink'],
							],
							resize_dir: 'vertical',
						}
				}
				
				sakai.editor.launch(client_id, config,'450','240');
			}
		</script>
		<h:panelGroup styleClass="" rendered="#{samLiteBean.richTextarea}">
			<script>
				pre_show_editor();
				document.getElementById('samLiteEntryForm:data').value=document.getElementById('samLiteEntryForm:data_value').value;
			</script>
		</h:panelGroup>
      </body>
    </html>
  </f:view>
