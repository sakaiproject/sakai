<%-- $Id$
include file for delivering audio questions
should be included in file importing DeliveryMessages
--%>
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
<%--
 "javascript: window.open('{$base}/{.}'
         +'?filename='+audioFileName+'_{$itemIdentRef}.au'
         +'&amp;seconds='+audioSeconds
         +'&amp;limit='+audioLimit
         +'&amp;app='+audioAppName
         +'&amp;dir='+audioDir
         +'&amp;imageUrl='+audioImageURL
         +'&amp;item_ident_ref='+'{$itemIdentRef}',
         '__ha_dialog',
         'toolbar=no,menubar=no,personalbar=no,width=430,height=330,scrollbars=no,
resizable=no');">
--%>
<h:outputText value="#{question.text} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:outputLink rendered="#{delivery.previewMode ne 'true'}"
 value="javascript:window.open('/samigo/jsp/aam/applet/soundRecorder.jsp'
         +'?filename='+'lydiattest'+'_1.au'
         +'&amp;seconds='+'#{question.duration}'
         +'&amp;limit='+'#{question.triesAllowed}'
         +'&amp;app='+'#{audioAppName}'
         +'&amp;dir='+'#{audioDir}'
         +'&amp;imageUrl='+'#{audioImageURL}'
         +'&amp;item_ident_ref='+'#{itemIdentRef}',
       	'ha_fullscreen',
	'toolbar=no,location=no,directories=no,status=no,menubar=yes, scrollbars=yes,resizable=yes,width=640,height=480');">
  <h:graphicImage id="image" alt="#{msg.audio_recording}."
    url="/images/recordresponse.gif" />
</h:outputLink>

<f:verbatim><br /></f:verbatim>
<h:outputText value="#{msg.time_limit}: "  escape="false"/>
<h:outputText value="#{question.duration} "  escape="false"/>
<f:verbatim><br /></f:verbatim>
<h:outputText value="#{msg.NoOfTries}: "  escape="false"/>
<h:outputText value="#{question.triesAllowed} "  escape="false"/>


<f:verbatim><br /></f:verbatim>
<h:selectBooleanCheckbox value="#{question.review}" rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1' }" id="mark_for_review" />
<h:outputLabel for="mark_for_review" value="#{msg.mark}"
  rendered="#{delivery.previewMode ne 'true' && delivery.navigation ne '1'}"/>

<h:panelGroup rendered="#{delivery.feedback eq 'true' && question.feedback != null}">
  <h:panelGroup rendered="#{delivery.feedbackComponent.showItemLevel}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="feedSC" value="#{msg.feedback}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="feedSC" value="#{question.feedback}" escape="false"/>
  </h:panelGroup>
  <h:panelGroup rendered="#{delivery.feedbackComponent.showGraderComment && question.gradingComment != null}">
    <f:verbatim><br /></f:verbatim>
    <f:verbatim><b></f:verbatim>
    <h:outputLabel for="commentSC" value="#{msg.comment}: " />
    <f:verbatim></b></f:verbatim>
    <h:outputText id="commentSC" value="#{question.gradingComment}" escape="false" />
  </h:panelGroup>
</h:panelGroup>

