<%--
Headings for item edit pages, needs to have msg=AuthorMessages.properties.
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
<h:form id="itemFormHeading">
<p class="navIntraTool">
    <h:commandLink action="author" immediate="true">
      <h:outputText value="#{msg.global_nav_assessmt}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="template" immediate="true">
      <h:outputText value="#{msg.global_nav_template}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="poolList" immediate="true">
      <h:outputText value="#{msg.global_nav_pools}" />
    </h:commandLink>
</p>

<!-- breadcrumb-->
<div>
    <h:commandLink rendered="#{itemauthor.target == 'assessment'}" action="author" immediate="true">
      <h:outputText value="#{msg.global_nav_assessmt}" />
    </h:commandLink>
    <h:outputText rendered="#{itemauthor.target == 'assessment'}" value=" > " />
    <h:commandLink action="editAssessment" immediate="true" rendered="#{itemauthor.target == 'assessment'}">
      <h:outputText value="#{msg.qs}: #{assessmentBean.title}" />
    </h:commandLink>
    <h:outputText value=" > " rendered="#{itemauthor.target == 'assessment'}" />
    <h:outputText value="#{msg.q} #{itemauthor.itemNo}" rendered="#{itemauthor.target == 'assessment'}"/>

</div>
<div>
<h:outputText rendered="#{itemauthor.target == 'questionpool'}" value="#{msg.global_nav_pools}> "/>

<samigo:dataLine rendered="#{itemauthor.target == 'questionpool'}" value="#{questionpool.currentPool.parentPoolsArray}" var="parent"
   separator=" > " first="0" rows="100" >
  <h:column>
    <h:commandLink action="#{questionpool.editPool}"  immediate="true">
      <h:outputText value="#{parent.displayName}" />
      <f:param name="qpid" value="#{parent.questionPoolId}"/>
    </h:commandLink>
  </h:column>
</samigo:dataLine>
<h:outputText rendered="#{questionpool.currentPool.showParentPools && itemauthor.target == 'questionpool'}" value=" > " />
<h:commandLink rendered="#{itemauthor.target == 'questionpool'}" action="#{questionpool.editPool}"  immediate="true">
  <h:outputText value="#{questionpool.currentPool.displayName}"/>
  <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
</h:commandLink>
</div>

<h3>
   <h:outputText value="#{msg.modify_q}:"/>
   <h:outputText value="#{assessmentBean.title}" rendered="#{itemauthor.target == 'assessment'}"/>
</h3>
<!-- CHANGE TYPE -->
<div class="indnt1">
<div class=" shorttext"><h:outputLabel value="#{msg.change_q_type}"/>

<h:selectOneMenu rendered="#{questionpool.importToAuthoring == 'true'}" onchange="document.links[5].onclick();"
  value="#{itemauthor.currentItem.itemType}" required="true" id="changeQType1">
  <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />

  <f:selectItem itemLabel="#{msg.select_qtype}" itemValue=""/>
  <f:selectItem itemLabel="#{msg.multiple_choice_type}" itemValue="1"/>
  <f:selectItem itemLabel="#{msg.multiple_choice_surv}" itemValue="3"/>
  <f:selectItem itemLabel="#{msg.short_answer_essay}" itemValue="5"/>
  <f:selectItem itemLabel="#{msg.fill_in_the_blank}" itemValue="8"/>
  <f:selectItem itemLabel="#{msg.matching}" itemValue="9"/>
  <f:selectItem itemLabel="#{msg.true_false}" itemValue="4"/>
  <f:selectItem itemLabel="#{msg.audio_recording}" itemValue="7"/>
  <f:selectItem itemLabel="#{msg.file_upload}" itemValue="6"/>
</h:selectOneMenu>


<h:selectOneMenu onchange="document.links[5].onclick();" rendered="#{questionpool.importToAuthoring == 'false'}"
  value="#{itemauthor.currentItem.itemType}" required="true" id="changeQType2">
  <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />

  <f:selectItem itemLabel="#{msg.select_qtype}" itemValue=""/>
  <f:selectItem itemLabel="#{msg.multiple_choice_type}" itemValue="1"/>
  <f:selectItem itemLabel="#{msg.multiple_choice_surv}" itemValue="3"/>
  <f:selectItem itemLabel="#{msg.short_answer_essay}" itemValue="5"/>
  <f:selectItem itemLabel="#{msg.fill_in_the_blank}" itemValue="8"/>
  <f:selectItem itemLabel="#{msg.matching}" itemValue="9"/>
  <f:selectItem itemLabel="#{msg.true_false}" itemValue="4"/>
<%--
  <f:selectItem itemLabel="#{msg.audio_recording}" itemValue="7"/>
--%>
  <f:selectItem itemLabel="#{msg.file_upload}" itemValue="6"/>
  <f:selectItem itemLabel="#{msg.import_from_q}" itemValue="10"/>
</h:selectOneMenu>

<h:commandLink id="hiddenlink" action="#{itemauthor.doit}" value="">
</h:commandLink>

<h:message rendered="#{questionpool.importToAuthoring == 'true'}" for="changeQType1" styleClass="validate"/>
<h:message rendered="#{questionpool.importToAuthoring == 'false'}" for="changeQType2" styleClass="validate"/>
</div>
<!-- SUBHEADING -->
<p class="navModeAction">
  <span class="leftNav">
   <b>
     <h:outputText value="#{msg.q}"/>
     <h:outputText rendered="#{itemauthor.target == 'assessment'}" value="#{itemauthor.itemNo}"/>
     <h:outputText value=" - "/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType == 1}" value="#{msg.multiple_choice_type}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 2}" value="#{msg.multiple_choice_type}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 3}" value="#{msg.multiple_choice_surv}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 4}" value="#{msg.true_false}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 5}" value="#{msg.short_answer_essay}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 8}" value="#{msg.fill_in_the_blank}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 9}" value="#{msg.matching}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 7}" value="#{msg.audio_recording}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 6}" value="#{msg.file_upload}"/>
     <h:outputText rendered="#{itemauthor.currentItem.itemType== 10}" value="#{msg.import_from_q}"/>
   </b>
 </span>
 <span class="rightNav">

 <%--
  temporily comment put Preview link for a specific question in Author. It will not be the feature in Sam 1.5.
  <h:commandLink id="preview" immediate="true" action="preview">
          <h:outputText value="#{msg.preview}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemModifyListener" />
  </h:commandLink>


  <h:outputText rendered="#{itemauthor.currentItem.itemId != null}" value=" | " />
--%>
  <h:commandLink rendered="#{itemauthor.currentItem.itemId != null}" styleClass="alignRight" immediate="true" id="deleteitem" action="#{itemauthor.confirmDeleteItem}">
                <h:outputText value="#{msg.button_remove}" />
                <f:param name="itemid" value="#{itemauthor.currentItem.itemId}"/>
              </h:commandLink>


<%-- removed MyQuestion link per new mockup
  <h:outputText rendered="#{itemauthor.target == 'assessment' && itemauthor.currentItem.itemId != null}" value=" | " />
  <h:commandLink immediate="true" rendered="#{itemauthor.target == 'assessment'}" action="editAssessment">
    <h:outputText value="#{msg.my_qs}" />
  </h:commandLink>
--%>
 </span>
 <br />
   <h:messages layout="table" style="color:red"/>

 <br />

</p>
</div>
 <br />
</h:form>
