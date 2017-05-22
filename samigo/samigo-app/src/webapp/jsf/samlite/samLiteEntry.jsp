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
      	<script type="text/javascript">
      	function toggleLayer(layerId)
        {
        	var layerStyle;
        	
            if (document.getElementById)
            {
            	layerStyle = document.getElementById(layerId).style;
            }
            else if (document.all)
            {
            	layerStyle = document.all[layerId].style;
            }
            else if (document.layers)
            {
            	layerStyle = document.layers[layerId].style;
            }
 
 			if (layerStyle) {
	           	if (layerStyle.display == "block") {
	           		layerStyle.display = "none";
	           	} else {
	           		layerStyle.display = "block";
	           	}
           	}
        }
      	</script>      	
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
	     <p/>
	     <h4><h:outputText value="#{samLiteMessages.samlite_step_1_of_2}"/></h4>
	     <p/>
	     <div class="instructions"><h:outputText value="#{samLiteMessages.samlite_instructions}"/></div>
  		 <p/>
		 <div><h4><h:outputText value="#{samLiteMessages.samlite_infogroup}"/></h4></div>	
		 <br/>
		 <div class="form-group row">
		 	<h:outputLabel for="name" value="#{samLiteMessages.samlite_name}" styleClass="form-control-label col-md-2"/>
		 	<div class="col-md-8">
		 		<h:inputText id="name" value="#{samLiteBean.name}" size="50" styleClass="form-control"/>
		 	</div>
		 </div>
		 
		 <div class="form-group row">
		 	<h:outputLabel for="description" value="#{samLiteMessages.samlite_description}" styleClass="form-control-label col-md-2"/>
		 	<div class="col-md-8">
		 		<h:inputTextarea id="description" value="#{samLiteBean.description}" rows="2" cols="40" styleClass="form-control"/>
		 	</div>
		 </div>

		 <p/>
		 <div class="navIntraTool"><strong><h:outputText value="#{samLiteMessages.samlite_textgroup}"/></strong></div>
		 <br/>
		 <div class="instructions"><h:outputText value="#{samLiteMessages.samlite_textgroup_instructions}"/></div>
		 <p/>
		 <div class="row">
		 	<div class="col-md-6">
		 		<h:inputTextarea id="data" value="#{samLiteBean.data}" rows="15" cols="75"/>
		 		<div>
					<%-- immediate=true bypasses the valueChangeListener --%>
					<h:commandButton value="#{samLiteMessages.samlite_cancel}" type="submit" action="author" immediate="true"/>
					     	
					<%-- activates the valueChangeListener --%>
					<h:commandButton value="#{samLiteMessages.samlite_validate}" type="submit" styleClass="active" action="#{samLiteBean.getOutcome}">
						<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.samlite.ParserListener" />
		 			</h:commandButton>
		 		</div>
		 	</div>
		 	<div class="col-md-6">
		 		<div class="row">
		 			<div class="col-md-12">
		 				<h4><h:outputText value="#{samLiteMessages.samlite_formatgroup}"/></h4>
						<a href="javascript:toggleLayer('general_instructions');"><h:outputText value="#{samLiteMessages.general_instructions_label}"/></a>
					    	<div id="general_instructions" class="inopPanel" style="display:none">
					   			<h:outputText value="#{samLiteMessages.general_instructions_prelude}"/><p/>
					   			<ul>
					   				<li><h:outputText value="#{samLiteMessages.general_instructions_format1}"/></li>
					   				<li><h:outputText value="#{samLiteMessages.general_instructions_format2}"/></li>
					   				<li><h:outputText value="#{samLiteMessages.general_instructions_format3}"/></li>
					   				<li><h:outputText value="#{samLiteMessages.general_instructions_format4}"/></li>
					   				<li><h:outputText value="#{samLiteMessages.general_instructions_format5}"/></li>
					   			</ul>
					   			<h:outputText value="#{samLiteMessages.general_instructions_conclude}"/><p/>
								<h:outputText value="#{samLiteMessages.general_instructions_feedback}"/>
				    	</div>
		 			</div>
		 			<div class="col-md-12">
					    <a href="javascript:toggleLayer('example_mc_question');"><h:outputText value="#{samLiteMessages.example_mc_label}"/></a>
					    <div id="example_mc_question" class="inopPanel" style="display:none">
					    	<strong><h:outputText value="#{samLiteMessages.example_instruction_label}"/></strong>
					    	<h:outputText value="#{samLiteMessages.example_mc_instructions}"/><br/>
					    	<h:outputText value="#{samLiteMessages.example_mc_instructions_1}"/><br/>
					    	<h:outputText value="#{samLiteMessages.example_mc_instructions_2}"/><p/>
				   			<strong><h:outputText value="#{samLiteMessages.example_example_label}"/></strong><br/>
				   			<h:outputText value="#{samLiteMessages.example_mc_question_format4}"/><br/>
				   			<h:outputText value="#{samLiteMessages.example_mc_question_text}"/><p/>
				   			<h:outputText value="#{samLiteMessages.example_mc_answer_a}"/><br/>
				   			<h:outputText value="#{samLiteMessages.example_mc_answer_b}"/><br/>
				   			<h:outputText value="#{samLiteMessages.example_mc_answer_c}"/><br/>
				   			<h:outputText value="#{samLiteMessages.example_mc_answer_d}"/><br/>
				   			<h:outputText value="#{samLiteMessages.example_mc_question_random}"/><br/>
				   			<h:outputText value="#{samLiteMessages.example_mc_question_rationale}"/>
				   		</div>
		 			</div>
		 			<div class="col-md-12">
						    <a href="javascript:toggleLayer('example_mcmc_question');"><h:outputText value="#{samLiteMessages.example_mcmc_label}"/></a>
						    <div id="example_mcmc_question" class="inopPanel" style="display:none">
				    			<strong><h:outputText value="#{samLiteMessages.example_instruction_label}"/></strong>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_instructions}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_instructions_1}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_instructions_2}"/><p/>
				    			<strong><h:outputText value="#{samLiteMessages.example_example_label}"/></strong><br/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_question_format4}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_question_text}"/><p/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_answer_a}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_answer_b}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_answer_c}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_question_random}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_question_rationale}"/><br/>
				    		</div>
		 			</div>
		 			<div class="col-md-12">
						    <a href="javascript:toggleLayer('example_fib_question');"><h:outputText value="#{samLiteMessages.example_fib_label}"/></a>
						    <div id="example_fib_question" class="inopPanel" style="display:none">
				    			<strong><h:outputText value="#{samLiteMessages.example_instruction_label}"/></strong><h:outputText value="#{samLiteMessages.example_fib_instructions}"/><p/>
				    			<strong><h:outputText value="#{samLiteMessages.example_example_label}"/></strong><br/>
				    			<h:outputText value="#{samLiteMessages.example_fib_question_format4}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_fib_question_text}"/><p/>
				    			<h:outputText value="#{samLiteMessages.example_fib_answer}"/>
				    		</div>
		 			</div>
		 			<div class="col-md-12">
						    <a href="javascript:toggleLayer('example_se_question');"><h:outputText value="#{samLiteMessages.example_se_label}"/></a>
						    <div id="example_se_question" class="inopPanel" style="display:none">
				    			<strong><h:outputText value="#{samLiteMessages.example_instruction_label}"/></strong><h:outputText value="#{samLiteMessages.example_se_instructions}"/><p/>
				    			<strong><h:outputText value="#{samLiteMessages.example_example_label}"/></strong><br/>
				    			<h:outputText value="#{samLiteMessages.example_se_question_format4}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_se_question_text}"/>
				    		</div>
		 			</div>
		 			<div class="col-md-12">
						    <a href="javascript:toggleLayer('example_tf_question');"><h:outputText value="#{samLiteMessages.example_tf_label}"/></a>
						    <div id="example_tf_question" class="inopPanel" style="display:none">
				    			<strong><h:outputText value="#{samLiteMessages.example_instruction_label}"/>
				    			</strong><h:outputText value="#{samLiteMessages.example_tf_instructions}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_instructions_2}"/><p/>
				    			<strong><h:outputText value="#{samLiteMessages.example_example_label}"/></strong><br/>
				    			<h:outputText value="#{samLiteMessages.example_tf_question_format4}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_tf_question_text}"/><p/>
				    			<h:outputText value="#{samLiteMessages.example_tf_answer_a}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_tf_answer_b}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_mcmc_question_rationale}"/><br/>
				    		</div>
		 			</div>
		 			<div class="col-md-12">
						    <a href="javascript:toggleLayer('example_fn_question');"><h:outputText value="#{samLiteMessages.example_fn_label}"/></a>
						    <div id="example_fn_question" class="inopPanel" style="display:none">
				    			<strong><h:outputText value="#{samLiteMessages.example_instruction_label}"/></strong><h:outputText value="#{samLiteMessages.example_fn_instructions}"/><p/>
				    			<strong><h:outputText value="#{samLiteMessages.example_example_label}"/></strong><br/>
				    			<h:outputText value="#{samLiteMessages.example_fn_question_format4}"/><br/>
				    			<h:outputText value="#{samLiteMessages.example_fn_question_text}"/><p/>
				    			<h:outputText value="#{samLiteMessages.example_fn_answer}"/>
				    		</div>
		 			</div>
		 		</div>
		 	</div>
		 </div>
		</h:form>
		</div>
 		<!-- end content -->
      </body>
    </html>
  </f:view>
