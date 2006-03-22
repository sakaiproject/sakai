<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->
<%-- $Id$
include file for delivering file upload questions
should be included in file importing DeliveryMessages
--%>
<h:outputText value="#{question.text} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:panelGroup>
  <h:outputText value="#{msg.file}#{msg.column} " />
  <!-- note that target represent the location where the upload medis will be temporarily stored -->
  <!-- For ItemGradingData, it is very important that target must be in this format: -->
  <!-- assessmentXXX/questionXXX/agentId -->
  <!-- please check the valueChangeListener to get the final destination -->
  <h:inputText size="50" />
  <h:outputText value="  " />
  <h:commandButton accesskey="#{msg.a_browse}" value="#{msg.browse}" type="button"/>
  <h:outputText value="  " />
  <h:commandButton accesskey="#{msg.a_upload}" value="#{msg.upload}" type="button"/>
</h:panelGroup>
<f:verbatim><br /></f:verbatim>
