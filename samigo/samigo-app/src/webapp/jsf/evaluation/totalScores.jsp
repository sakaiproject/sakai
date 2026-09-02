<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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
      <title><h:outputText
        value="#{commonMessages.total_scores}" /></title>
      <script src='/library/js/spinner.js<h:outputText value="#{totalScores.CDNQuery}" />'></script>
<%@ include file="/js/delivery.js" %>

      <script>includeWebjarLibrary('awesomplete')</script>
      <script src='/library/js/sakai-reminder.js<h:outputText value="#{totalScores.CDNQuery}" />'></script>

<script>
function toPoint(id)
{
  var x=document.getElementById(id).value
  document.getElementById(id).value=x.replace(',','.')
}

function pause(numberMillis)
{
var now = new Date();
var exitTime = now.getTime() + numberMillis;
while (true)
{
now = new Date();
if (now.getTime() > exitTime)
return;
}
}

function inIt()
{
  var inputs= document.getElementsByTagName("INPUT");
  for (var i = 0; i < inputs.length; i++) {
    if (inputs[i].name.indexOf("applyScoreButton") >=0) {
      inputs[i].disabled=false;
    }
  }
}

function disableIt()
{
  var inputs= document.getElementsByTagName("INPUT");
  for (var i = 0; i < inputs.length; i++) {
    if (inputs[i].name.indexOf("applyScoreButton") >=0) {
      inputs[i].disabled=true;
    }
  }
}

$(document).ready(function(){

  // The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.
  var currentLink = $('#editTotalResults\\:totalScoresMenuLink');
  currentLink.addClass('current');
  // Remove the link of the current option
  currentLink.html(currentLink.find('a').text());

  $("a.sam-scoretable-deleteattempt").each(function(){
    this.existingOnclick = this.onclick;
    this.onclick = null;
    $(this).click(function(){
    	if ( confirm("<h:outputText value="#{commonMessages.confirm_delete_attempt}" escape="false"/>") ) {
        this.existingOnclick();
      } else {
        return false;
      }
    });
  });

  // unified Update: live hint, enable logic, overlap protection
  window.samBatchCountTpl = "<h:outputText value="#{evaluationMessages.batch_selected_count}" escape="false"/>";
  window.samBatchDeleteTpl = "<h:outputText value="#{evaluationMessages.batch_delete_confirm}" escape="false"/>";
  window.samHintNothing  = "<h:outputText value="#{evaluationMessages.batch_hint_nothing}" escape="false"/>";
  window.samHintConflict = "<h:outputText value="#{evaluationMessages.batch_hint_conflict}" escape="false"/>";
  window.samHintSave     = "<h:outputText value="#{evaluationMessages.batch_hint_save}" escape="false"/>";
  window.samHintEmail    = "<h:outputText value="#{evaluationMessages.batch_hint_email}" escape="false"/>";
  window.samTotSubmitted = <h:outputText value="#{totalScores.submittedAgentCount}"/>;
  window.samTotNoSub     = <h:outputText value="#{totalScores.noSubmissionAgentCount}"/>;
  window.samMaxScore     = parseFloat("<h:outputText value="#{totalScores.maxScore}"/>".replace(',', '.'));

  var samTable = 'table[id$="totalScoreTable"]';
  // snapshot each row's original inline adjustment / comment so we can tell what the instructor actually typed
  $(samTable + ' tbody tr').each(function() {
    $(this).find('input[id*="adjustTotal"]').each(function() { this.setAttribute('data-orig', this.value); });
    $(this).find('textarea:not([disabled])').each(function() { this.setAttribute('data-orig', this.value); });
  });

  function samRowTargeted($tr, target) {
    var $cb = $tr.find('input.select-checkbox');
    var submitted = $cb.length > 0;           // the select checkbox only renders for real submissions
    if (target === 'SELECTED') return $cb.is(':checked');
    if (target === 'WITH_SUBMISSIONS') return submitted;
    if (target === 'NO_SUBMISSION') return !submitted;
    return true;                              // ALL
  }

  window.samBatchCount = function() { return $("input.select-checkbox:checked").length; };
  window.samBatchSetState = function(sel, on) {
    if (on) { sel.removeClass("disabled").attr("tabindex", 0); }
    else { sel.addClass("disabled").attr("tabindex", -1); }
  };
  window.samBatchUpdate = function() {
    var n = samBatchCount();
    var total = $("input.select-checkbox").length;
    $("#batchSelectedCount").text(samBatchCountTpl.replace('{0}', n));
    $(".sam-select-all").prop("checked", total > 0 && n === total);

    var adjVal = $.trim($('input[id$="applyToSelectedScore"]').val() || "");
    var hasAdj = adjVal !== "";
    var hasCmt = $.trim($('textarea[id$="bulkComment"]').val() || "") !== "";
    var target = $('select[id$="bulkApplyTarget"]').val() || "NO_SUBMISSION";
    var notify = $('input[id$="bulkNotify"]').is(":checked");

    var bulkAdjNum = hasAdj ? parseFloat(adjVal.replace(',', '.')) : NaN;
    var bulkCmtVal = $.trim($('textarea[id$="bulkComment"]').val() || "");
    // walk the rows once: count inline edits, and where a bulk value CONFLICTS
    // with a hand-typed one. An overlap whose value is identical to the bulk
    // value is a no-op, not a conflict, so it never blocks or grays.
    var inlineEdited = 0, adjConflict = 0, cmtOverlap = 0;
    $(samTable + ' tbody tr').each(function() {
      var $tr = $(this);
      var adj = $tr.find('input[id*="adjustTotal"]')[0];
      var cmt = $tr.find('textarea:not([disabled])')[0];
      var adjEdited = adj && adj.value !== adj.getAttribute('data-orig');
      var cmtEdited = cmt && cmt.value !== cmt.getAttribute('data-orig');
      if (adjEdited || cmtEdited) inlineEdited++;
      var targeted = (hasAdj || hasCmt) && samRowTargeted($tr, target);
      var rowAdjConflict = targeted && hasAdj && adjEdited &&
                           parseFloat((adj.value || "").replace(',', '.')) !== bulkAdjNum;
      if (rowAdjConflict) adjConflict++;
      if (targeted && hasCmt && cmtEdited && $.trim(cmt.value) !== "" && $.trim(cmt.value) !== bulkCmtVal) cmtOverlap++;
      // flag the offending adjustment field, the way Date Manager flags bad dates
      if (adj) $(adj).toggleClass("border border-danger sam-batch-conflict-cell", !!rowAdjConflict);
    });

    var staged = inlineEdited > 0 || hasAdj || hasCmt;

    // Notify grays until there's a change to notify about
    var $notify = $('input[id$="bulkNotify"]');
    $notify.prop('disabled', !staged);
    $(".sam-batch-notify").toggleClass("sam-batch-off", !staged);
    if (!staged && notify) { $notify.prop('checked', false); notify = false; }

    // Replace grays only when a targeted row has a hand-typed comment it would wipe
    var $replace = $('input[name$="bulkCommentMode"][value="REPLACE"]');
    var lockReplace = hasCmt && cmtOverlap > 0;
    $replace.prop('disabled', lockReplace);
    $replace.closest('label, td, tr, span').toggleClass('sam-batch-off', lockReplace);
    if (lockReplace && $replace.is(':checked')) {
      $('input[name$="bulkCommentMode"][value="APPEND"]').prop('checked', true);
    }

    // "Selected" bulk needs at least one row checked
    var bulkNeedsSelection = (hasAdj || hasCmt) && target === 'SELECTED' && n === 0;
    var updateOk = staged && adjConflict === 0 && !bulkNeedsSelection;
    samBatchSetState($(".sam-batch-update"), updateOk);
    samBatchSetState($(".sam-batch-button"), n > 0);   // Delete

    // live "what Update will do" hint
    var hint, $hint = $("#samUpdateHint");
    if (adjConflict > 0) {
      hint = samHintConflict.replace('{0}', adjConflict);
      $hint.removeClass("text-muted").addClass("sam-batch-hint-warn text-danger fw-bold");
    } else {
      $hint.removeClass("sam-batch-hint-warn text-danger fw-bold").addClass("text-muted");
      var parts = [];
      if (inlineEdited > 0) parts.push(samHintSave.replace('{0}', inlineEdited));
      if (hasAdj || hasCmt) {
        var m = target === 'SELECTED' ? n
              : target === 'WITH_SUBMISSIONS' ? samTotSubmitted
              : target === 'NO_SUBMISSION' ? samTotNoSub
              : (samTotSubmitted + samTotNoSub);
        var verb = (hasAdj && hasCmt) ? ("set adjustment " + adjVal + " & comment")
                 : hasAdj ? ("set adjustment " + adjVal) : "add comment";
        var tgt = $('select[id$="bulkApplyTarget"] option:selected').text();
        var over = hasAdj && !isNaN(samMaxScore) && (parseFloat(adjVal.replace(',', '.')) > samMaxScore || parseFloat(adjVal.replace(',', '.')) < 0);
        parts.push(verb + " for " + m + " (" + tgt + ")" + (over ? " ⚠ out of range" : ""));
      }
      if (notify) parts.push(samHintEmail);
      hint = parts.length ? parts.join("  ·  ") : samHintNothing;
    }
    $hint.text(hint);
  };
  window.samBatchConfirmDelete = function() {
    return confirm(samBatchDeleteTpl.replace('{0}', samBatchCount()));
  };
  $(document).on("change", "input.select-checkbox", samBatchUpdate);
  $(document).on("input change",
    'input[id$="applyToSelectedScore"], textarea[id$="bulkComment"], select[id$="bulkApplyTarget"], input[id$="bulkNotify"], input[name$="bulkCommentMode"], ' +
    samTable + ' input[id*="adjustTotal"], ' + samTable + ' textarea:not([disabled])',
    samBatchUpdate);
  $(document).on("change", ".sam-select-all", function() {
    $("input.select-checkbox").prop("checked", this.checked);
    samBatchUpdate();
  });
  samBatchUpdate();

  // remember whether the bulk composer is collapsed, per user (localStorage).
  // Default (no stored value) is expanded; Bootstrap's data-api handles the toggling.
  try {
    var samComposerKey = "samigo.totalScores.bulkComposer";
    var samComposerEl = document.getElementById("samBulkComposer");
    if (samComposerEl && window.localStorage) {
      if (localStorage.getItem(samComposerKey) === "collapsed") {
        $(samComposerEl).removeClass("show");
        $(".sam-batch-toggle").attr("aria-expanded", "false").addClass("collapsed");
      }
      // Bootstrap 5 fires NATIVE events with dotted names; jQuery .on() misparses
      // the dots as namespaces (it would listen for "hidden"), so bind natively.
      samComposerEl.addEventListener("shown.bs.collapse", function() { try { localStorage.setItem(samComposerKey, "expanded"); } catch (e) {} });
      samComposerEl.addEventListener("hidden.bs.collapse", function() { try { localStorage.setItem(samComposerKey, "collapsed"); } catch (e) {} });
    }
  } catch (e) {}

  window.samScrollTo = function(id) {
    var el = document.getElementById(id) || document.querySelector(id);
    if (el) { el.scrollIntoView({ behavior: "smooth", block: "center" }); }
    return false;
  };

  var sakaiReminder = new SakaiReminder();
  $('.awesomplete').each(function() {
    new Awesomplete(this, {
      list: sakaiReminder.getAll()
    });
  });
  $('#editTotalResults').submit(function(e) {
    $('textarea.awesomplete').each(function() {
      sakaiReminder.new($(this).val());
    });
  });

});

function showLoadingMessage() {
  let loadingMessageContainer = document.getElementById('editTotalResults:loadingMessage');
  loadingMessageContainer.classList.remove('hidden');
}

</script>
</head>
<body onload="disableIt();<%= request.getAttribute("html.body.onload") %>">
 <%-- Mrphs-sakai-samigo opts this page into the samigo skin scope (see deliverAssessment.jsp) --%>
 <div class="portletBody container-fluid Mrphs-sakai-samigo">

<!-- content... -->
<h:form id="editTotalResults">
  <h:inputHidden id="publishedId" value="#{totalScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h:panelGroup layout="block" styleClass="page-header">
    <%-- Export moved to the top-right of the page header --%>
    <h:panelGroup id="export-total-scores" layout="block" styleClass="pull-right float-end mt-1">
      <h:commandButton value="#{commonMessages.export_action}" action="#{totalScores.exportExcel}"/>
    </h:panelGroup>
    <h1>
      <h:outputText value="#{commonMessages.total_scores}#{evaluationMessages.column} " escape="false"/>
      <small><h:outputText value="#{totalScores.assessmentName} " escape="false"/></small>
    </h1>
  </h:panelGroup>

  <!-- EVALUATION SUBMENU -->
  <%@ include file="/jsf/evaluation/evaluationSubmenu.jsp" %>

  <div class="hide">
    <h:outputText value="#{evaluationMessages.auto_scored_tip}" rendered="#{totalScores.isAutoScored}" />
  </div>

<div class="tier1">
  <h:messages infoClass="sak-banner-info" warnClass="sak-banner-warn" errorClass="sak-banner-error" fatalClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
  <!-- only shows Max Score Possible if this assessment does not contain random dawn parts -->

  <sakai:flowState bean="#{totalScores}" />

  <h:panelGroup styleClass="max-score-possible" layout="block" rendered="#{!totalScores.hasRandomDrawPart}">
    <h:outputText value="<h2>#{evaluationMessages.max_score_poss}<small>: #{totalScores.maxScore}</small></h2>" escape="false"/>
  </h:panelGroup>

  <%-- Average-submission view keeps its own dedicated apply control (saveTotalScoresAverage).
       In every other view the old "Apply This Score to No Submission" is folded into the
       unified bulk bar below, where "Apply to" defaults to No submission. --%>
  <h:panelGroup styleClass="apply-grades" layout="block" rendered="#{totalScores.allSubmissions=='4'}">
    <h:commandButton value="#{evaluationMessages.applyGrades} " id="applyScoreButton" styleClass="active" type="submit" onclick="SPNR.disableControlsAndSpin( this, null );">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
    </h:commandButton>
    <h:outputText value="&#160;" escape="false" />
    <h:inputText id="applyScoreUnsubmitted" value="#{totalScores.applyToUngraded}"  onkeydown="inIt()" onchange="toPoint(this.id);" size="5"/>
    <h:outputText value=" #{evaluationMessages.applyGradesDescAvg}"/>
  </h:panelGroup>
<a name="samBatchActions"></a>
<span id="samBatchActions"></span>
<h:panelGroup layout="block" styleClass="sam-batch-actions" rendered="#{totalScores.allSubmissions!='4'}">
  <%-- disclosure bar styled like the Assignments "Send Feedback to Multiple Students" toggle (theme-adaptive info-banner) --%>
  <div class="sam-batch-bar d-flex align-items-center gap-2">
    <button type="button" class="sam-batch-toggle" data-bs-toggle="collapse" data-bs-target="#samBulkComposer" aria-expanded="true" aria-controls="samBulkComposer">
      <span class="fa fa-chevron-down sam-batch-caret" aria-hidden="true"></span>
      <h:outputText value="#{evaluationMessages.batch_actions_heading}"/>
    </button>
    <span id="batchSelectedCount" class="small sam-batch-count" aria-live="polite"></span>
  </div>
  <div id="samBulkComposer" class="collapse show">
  <h:panelGroup layout="block" styleClass="sam-batch-inputs">
    <%-- grade group: the adjustment score and which participants it (and the comment) applies to --%>
    <h:panelGroup layout="block" styleClass="sam-batch-group sam-batch-grade form-inline">
      <h:outputLabel for="applyToSelectedScore" value="#{evaluationMessages.batch_adjustment_label}" styleClass="sam-batch-label"/>
      <h:inputText id="applyToSelectedScore" value="#{totalScores.applyToSelectedScore}" size="4" maxlength="8" onchange="toPoint(this.id);"/>
      <h:outputLabel for="bulkApplyTarget" value="#{evaluationMessages.batch_apply_target_label}" styleClass="sam-batch-label"/>
      <h:selectOneMenu id="bulkApplyTarget" value="#{totalScores.bulkApplyTarget}">
        <f:selectItem itemValue="NO_SUBMISSION"    itemLabel="#{evaluationMessages.batch_target_no_submission}"/>
        <f:selectItem itemValue="WITH_SUBMISSIONS" itemLabel="#{evaluationMessages.batch_target_with_submissions}"/>
        <f:selectItem itemValue="SELECTED"         itemLabel="#{evaluationMessages.batch_target_selected}"/>
        <f:selectItem itemValue="ALL"              itemLabel="#{evaluationMessages.batch_target_all}"/>
      </h:selectOneMenu>
    </h:panelGroup>
    <%-- comment group: kept in its own block, spaced away from the grade so the two don't read as one control --%>
    <h:panelGroup layout="block" styleClass="sam-batch-group sam-batch-comment form-inline">
      <h:outputLabel for="bulkComment" value="#{evaluationMessages.batch_comment_label}" styleClass="sam-batch-label"/>
      <h:inputTextarea id="bulkComment" value="#{totalScores.bulkComment}" rows="2" cols="30"/>
      <h:selectOneRadio id="bulkCommentMode" value="#{totalScores.bulkCommentMode}" layout="pageDirection" styleClass="sam-batch-commentmode">
        <f:selectItem itemValue="ONLY_EMPTY" itemLabel="#{evaluationMessages.batch_comment_only_empty}"/>
        <f:selectItem itemValue="APPEND" itemLabel="#{evaluationMessages.batch_comment_append}"/>
        <f:selectItem itemValue="REPLACE" itemLabel="#{evaluationMessages.batch_comment_replace}"/>
      </h:selectOneRadio>
    </h:panelGroup>
  </h:panelGroup>
  <%-- Notify rides with Update: tick to also email the students this Update actually changed. --%>
  <h:panelGroup layout="block" styleClass="sam-batch-notify" rendered="#{totalScores.gradingNotifyAvailable}">
    <h:selectBooleanCheckbox id="bulkNotify" value="#{totalScores.bulkNotify}" styleClass="sam-batch-notify-cb"/>
    <h:outputLabel for="bulkNotify" value=" #{evaluationMessages.batch_notify_label}" styleClass="sam-batch-label"/>
  </h:panelGroup>
  </div><%-- /#samBulkComposer --%>
  <%-- One primary Update: saves inline edits, applies the bulk composer to its target, and notifies whoever changed.
       A live hint (populated by JS) states exactly what it will do. Delete and Cancel stay separate. --%>
  <h:panelGroup layout="block" styleClass="sam-batch-buttons act">
    <h:commandButton id="updateScores" styleClass="active sam-batch-update" value="#{evaluationMessages.batch_update}" action="totalScores" type="submit"
        onclick="if (jQuery(this).hasClass('disabled')) return false;">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ApplyToSelectedListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.NotifyGradingUpdatedListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
    </h:commandButton>
    <span id="samUpdateHint" class="sam-batch-hint small" aria-live="polite"></span>
    <h:commandButton id="deleteSelected" styleClass="sam-batch-button sam-batch-danger disabled" value="#{evaluationMessages.batch_delete_selected}" action="totalScores" type="submit"
        rendered="#{person.isAdmin || !totalScores.restrictedDelete}"
        onclick="if (jQuery(this).hasClass('disabled')) return false; if (!samBatchConfirmDelete()) return false;">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.DeleteSelectedSubmissionsListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
    </h:commandButton>
    <h:commandButton value="#{commonMessages.cancel_action}" action="author">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetResultsCalculatedListener" />
    </h:commandButton>
  </h:panelGroup>
</h:panelGroup>

<h:panelGroup styleClass="row total-score-box" layout="block" rendered="#{totalScores.anonymous eq 'false'}">

  <h:panelGroup styleClass="col-md-6" layout="block">
    <h:panelGroup styleClass="all-submissions form-group" layout="block">
      <h:outputLabel styleClass="col-md-2" value="#{evaluationMessages.view}"/>
      <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsA1"
        required="true" onchange="showLoadingMessage();document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '4'}">
      <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
      <f:selectItem itemValue="4" itemLabel="#{evaluationMessages.average_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
     </h:selectOneMenu>

     <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsL1"
        required="true" onchange="showLoadingMessage();document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2'}">
      <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
      <f:selectItem itemValue="2" itemLabel="#{evaluationMessages.last_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
     </h:selectOneMenu>

     <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsH1"
        required="true" onchange="showLoadingMessage();document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1'}">
      <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
      <f:selectItem itemValue="1" itemLabel="#{evaluationMessages.highest_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
     </h:selectOneMenu>
     
     <!-- SECTION AWARE -->
     <h:outputText value="&nbsp;" escape="false" rendered="#{totalScores.multipleSubmissionsAllowed eq 'true'}"/>
     <h:outputText value="&nbsp;#{evaluationMessages.forAllSectionsGroups}" escape="false" rendered="#{totalScores.availableSectionSize < 1 && totalScores.multipleSubmissionsAllowed eq 'true'}"/>
     <h:outputText value="&nbsp;#{evaluationMessages.all_sections}" escape="false" rendered="#{totalScores.availableSectionSize < 1 && !totalScores.multipleSubmissionsAllowed eq 'true'}"/>
     <h:outputText value="&nbsp;#{evaluationMessages.for_s}&nbsp;&nbsp;" rendered="#{totalScores.availableSectionSize >= 1}" escape="false"/>

        <h:selectOneMenu value="#{totalScores.selectedSectionFilterValue}" id="sectionpicker" required="true" onchange="showLoadingMessage();document.forms[0].submit();" rendered="#{totalScores.availableSectionSize >= 1}">
          <f:selectItems value="#{totalScores.sectionFilterSelectItems}"/>
          <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener"/>
        </h:selectOneMenu>

        <h:panelGroup id="loadingMessage" styleClass="hidden">
          <h:panelGroup styleClass="spinner-border spinner-border-sm">
          </h:panelGroup>
          <h:outputText value="#{evaluationMessages.loading_submissions_message}" />
        </h:panelGroup>

      </h:panelGroup>

	  <h:panelGroup styleClass="search-student form-group" layout="block">
      <h:outputLabel styleClass="col-md-2" value="#{evaluationMessages.search}"/>
 	        <h:inputText
				id="searchString"
				value="#{totalScores.searchString}"
				onfocus="clearIfDefaultString(this, '#{evaluationMessages.search_default_student_search_string}')"
				onkeypress="return submitOnEnter(event, 'editTotalResults:searchSubmitButton');"/>
			<h:outputText value="&nbsp;" escape="false" />
			<h:commandButton actionListener="#{totalScores.search}" value="#{evaluationMessages.search_find}" id="searchSubmitButton" />
			<h:outputText value="&nbsp;" escape="false" />
			<h:commandButton actionListener="#{totalScores.clear}" value="#{evaluationMessages.search_clear}"/>
	  </h:panelGroup>
  </h:panelGroup>
   
  <h:panelGroup layout="block" styleClass="samigo-pager col-md-6" style="text-align: right">
    <sakai:pager id="pager1" totalItems="#{totalScores.dataRows}" firstItem="#{totalScores.firstRow}" pageSize="#{totalScores.maxDisplayedRows}" textStatus="#{evaluationMessages.paging_status}" />
  </h:panelGroup>
</h:panelGroup>

<h:panelGroup styleClass="total-scores-anon" layout="block" rendered="#{totalScores.anonymous eq 'true'}">
  <h:panelGroup>
	  <h:outputText value="#{evaluationMessages.view}" rendered="#{totalScores.multipleSubmissionsAllowed eq 'true' }"/>
      <h:panelGroup>
        <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsL2"
         required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2' && totalScores.multipleSubmissionsAllowed eq 'true' }">
        <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
        <f:selectItem itemValue="2" itemLabel="#{evaluationMessages.last_sub}" />
        <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        </h:selectOneMenu>

        <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsH2"
         required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1' && totalScores.multipleSubmissionsAllowed eq 'true' }">
          <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
          <f:selectItem itemValue="1" itemLabel="#{evaluationMessages.highest_sub}" />
          <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        </h:selectOneMenu>

		<h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissionsA2" required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '4' && totalScores.multipleSubmissionsAllowed eq 'true' }">
		  <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
		  <f:selectItem itemValue="4" itemLabel="#{evaluationMessages.average_sub}" />
          <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
		 </h:selectOneMenu>
      </h:panelGroup>
  </h:panelGroup>
  
  <h:panelGroup>
	<sakai:pager id="pager2" totalItems="#{totalScores.dataRows}" firstItem="#{totalScores.firstRow}" pageSize="#{totalScores.maxDisplayedRows}" textStatus="#{evaluationMessages.paging_status}" />
  </h:panelGroup>
</h:panelGroup>

  <!-- STUDENT RESPONSES AND GRADING -->
  <!-- note that we will have to hook up with the back end to get N at a time -->
<span id="samTop"></span>
<div class="table">
  <h:dataTable id="totalScoreTable" value="#{totalScores.agents}" var="description" styleClass="table table-striped table-bordered" columnClasses="textTable">

  <!-- SELECT column (drives the batch actions bar) -->
  <h:column rendered="#{totalScores.allSubmissions!='4'}">
     <f:facet name="header">
       <h:panelGroup>
         <h:selectBooleanCheckbox styleClass="sam-select-all" value="#{totalScores.selectAll}"
             title="#{evaluationMessages.batch_select_all}"/>
         <h:outputText value=" #{evaluationMessages.batch_select_column}"/>
       </h:panelGroup>
     </f:facet>
     <h:selectBooleanCheckbox id="rowSelect" styleClass="select-checkbox" value="#{description.selected}"
         title="#{evaluationMessages.batch_select_row}"
         rendered="#{description.assessmentGradingId ne '-1' && description.attemptDate != null}"/>
  </h:column>

	<!-- Add Submission Attempt Deleter-->
	<h:column rendered="#{person.isAdmin || !totalScores.restrictedDelete}">
     <f:facet name="header">
       <h:outputText value="#{commonMessages.delete}" rendered="true" />
     </f:facet>
     <h:panelGroup> <span class="tier2">
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />

       <h:commandLink styleClass="sam-scoretable-deleteattempt" title="#{commonMessages.delete_attempt}" action="totalScores" immediate="true" rendered="true" >
         <h:panelGroup rendered="#{description.assessmentGradingId ne '-1'}">
	     <span class="fa fa-trash" aria-hidden="true"></span>
	     <span class="sr-only"><h:outputText value="#{commonMessages.delete}" /></span>
         </h:panelGroup>
         <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.evaluation.GrantSubmissionListener" />
         <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
         <f:actionListener  type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
</span>
     </h:panelGroup>
    </h:column>
    
    <!-- NAME/SUBMISSION ID -->
    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType ne 'lastName'}">
     <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" immediate="true" id="lastName" action="totalScores">
          <h:outputText value="#{evaluationMessages.name}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="lastName" />
        <f:param name="sortAscending" value="true"/>        
        </h:commandLink>
     </f:facet>
      <sakai-user-photo user-id='<h:outputText value="#{description.agentId}"/>' profile-popup="on"></sakai-user-photo>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />

         <h:outputText value="#{description.lastName}" rendered="#{description.assessmentGradingId eq '-1' || description.forGrade == 'false' || totalScores.allSubmissions eq'4'}" />
         <h:outputText value=", " rendered="#{(description.assessmentGradingId eq '-1' || description.forGrade == 'false') && description.lastInitial ne 'Anonymous' || totalScores.allSubmissions eq'4'}"/>
         <h:outputText value="#{description.firstName}" rendered="#{description.assessmentGradingId eq '-1' || description.forGrade == 'false'  || totalScores.allSubmissions eq'4'}" />
         <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous' && (description.assessmentGradingId eq '-1' || description.forGrade == 'false')}" />
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true" 
          rendered="#{description.forGrade == 'true' &&  description.assessmentGradingId ne '-1' && totalScores.allSubmissions!='4'}" >
         <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" />
         <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
     <span class="itemAction">
       <h:panelGroup rendered="#{description.email != null && description.email != ''}">
         <h:outputText value="<a href=\"mailto:#{description.email}?subject=#{totalScores.assessmentName} #{commonMessages.feedback}\">" escape="false" />
         <h:outputText value="<span class=\"fa fa-envelope\" aria-hidden=\"true\"></span><span class=\"sr-only\">#{evaluationMessages.email}</span></a> " escape="false" />
       </h:panelGroup>
       <h:panelGroup rendered="#{not empty description.alternativeInstructorReviewUrl}">
         <h:outputText value="<a target=\"blank\" href=\"#{description.alternativeInstructorReviewUrl}\"><span class=\"fa fa-video-camera\" aria-hidden=\"true\"></span><span class=\"sr-only\">#{evaluationMessages.instructor_review}</span></a>" escape="false" />
       </h:panelGroup>
     </span>
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'lastName' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" action="totalScores">
          <h:outputText value="#{evaluationMessages.name}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortLastNameDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
      <sakai-user-photo user-id='<h:outputText value="#{description.agentId}"/>' profile-popup="on"></sakai-user-photo>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />

         <h:outputText value="#{description.lastName}" rendered="#{description.assessmentGradingId eq '-1' || description.forGrade == 'false' || totalScores.allSubmissions eq'4'}" />
         <h:outputText value=", " rendered="#{(description.assessmentGradingId eq '-1' || description.forGrade == 'false') && description.lastInitial ne 'Anonymous' || totalScores.allSubmissions eq'4'}"/>
         <h:outputText value="#{description.firstName}" rendered="#{description.assessmentGradingId eq '-1' || description.forGrade == 'false' ||totalScores.allSubmissions eq '4'}" />
         <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous' && (description.assessmentGradingId eq '-1' || description.forGrade == 'false')}" />
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true" 
          rendered="#{description.forGrade == 'true' && description.assessmentGradingId ne '-1' &&  totalScores.allSubmissions!='4'}" >
         <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" />
         <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>
     <span class="itemAction">
       <h:panelGroup rendered="#{description.email != null && description.email != ''}">
         <h:outputText value="<a href=\"mailto:#{description.email}?subject=#{totalScores.assessmentName} #{commonMessages.feedback}\">" escape="false" />
         <h:outputText value="<span class=\"fa fa-envelope\" aria-hidden=\"true\"></span><span class=\"sr-only\">#{evaluationMessages.email}</span></a> " escape="false" />
       </h:panelGroup>
       <h:panelGroup rendered="#{not empty description.alternativeInstructorReviewUrl}">
         <h:outputText value="<a target=\"_blank\" href=\"#{description.alternativeInstructorReviewUrl}\"><span class=\"fa fa-video-camera\" aria-hidden=\"true\"></span><span class=\"sr-only\">#{evaluationMessages.instructor_review}</span></a>" escape="false" />
       </h:panelGroup>
	 </span>

    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'lastName' && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortLastName}" action="totalScores">
        <h:outputText value="#{evaluationMessages.name}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortLastNameAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
      <sakai-user-photo user-id='<h:outputText value="#{description.agentId}"/>' profile-popup="on"></sakai-user-photo>
            <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />

         <h:outputText value="#{description.lastName}" rendered="#{description.assessmentGradingId eq '-1' || description.forGrade == 'false'}" />
         <h:outputText value=", " rendered="#{(description.assessmentGradingId eq '-1' || description.forGrade == 'false') && description.lastInitial ne 'Anonymous' || totalScores.allSubmissions eq'4'}"/>
         <h:outputText value="#{description.firstName}" rendered="#{description.assessmentGradingId eq '-1' || description.forGrade == 'false' || totalScores.allSubmissions eq'4' || totalScores.allSubmissions eq'4'}" />
         <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous' && (description.assessmentGradingId eq '-1' || description.forGrade == 'false')}" />
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true" 
          rendered="#{description.forGrade == 'true' && description.assessmentGradingId ne '-1' &&  totalScores.allSubmissions!='4'}" >
         <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
         <h:outputText value="#{description.firstName}" />
         <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
     </h:panelGroup>

     <f:verbatim><br/></f:verbatim>

	 <span class="itemAction">
	  <h:panelGroup rendered="#{description.email != null && description.email != ''}">
		 <h:outputText value="<a href=\"mailto:" escape="false" />
	     <h:outputText value="#{description.email}" />
	     <h:outputText value="?subject=" escape="false" />
		 <h:outputText value="#{totalScores.assessmentName} #{commonMessages.feedback}\">" escape="false" />
         <h:outputText value="  #{evaluationMessages.email}" escape="false"/>
         <h:outputText value="</a>" escape="false" />
	   </h:panelGroup>
	 </span>
	</h:column>
    

    <!-- ANONYMOUS and ASSESSMENTGRADINGID -->
    <h:column rendered="#{totalScores.anonymous eq 'true' && totalScores.sortType ne 'assessmentGradingId'}">
     <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmissionId}" action="totalScores" >
          <h:outputText value="#{evaluationMessages.sub_id}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="assessmentGradingId" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
     <h:panelGroup >
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" rendered="#{totalScores.allSubmissions != '4' && description.assessmentGradingId != -1}">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}"/>
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
       <h:outputText rendered="#{totalScores.allSubmissions eq '4' && description.assessmentGradingId != -1}" value="#{description.assessmentGradingId}" />
       <h:outputText rendered="#{totalScores.allSubmissions != '4' && description.assessmentGradingId == -1}" value="#{evaluationMessages.na}" />
     </h:panelGroup>
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'true' && totalScores.sortType eq 'assessmentGradingId' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmissionId}" action="totalScores">
          <h:outputText value="#{evaluationMessages.sub_id}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmissionIdDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup>
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true" rendered="#{totalScores.allSubmissions != '4' && description.assessmentGradingId != -1}">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
       <h:outputText rendered="#{totalScores.allSubmissions eq '4' && description.assessmentGradingId != -1}" value="#{description.assessmentGradingId}" />
       <h:outputText rendered="#{totalScores.allSubmissions != '4' && description.assessmentGradingId == -1}" value="#{evaluationMessages.na}" />
     </h:panelGroup>
    </h:column>
    
    <h:column rendered="#{totalScores.anonymous eq 'true' && totalScores.sortType eq 'assessmentGradingId' && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortSubmissionId}" action="totalScores">
        <h:outputText value="#{evaluationMessages.sub_id}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortSubmissionIdAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
     <h:panelGroup>
       <h:commandLink title="#{evaluationMessages.t_student}" action="studentScores" immediate="true" rendered="#{totalScores.allSubmissions != '4' && description.assessmentGradingId != -1}">
         <h:outputText value="#{description.assessmentGradingId}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.StudentScoreListener" />
         <f:param name="studentid" value="#{description.idString}" />
         <f:param name="studentName" value="#{description.assessmentGradingId}" />
         <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
         <f:param name="gradingData" value="#{description.assessmentGradingId}" />
       </h:commandLink>
       <h:outputText rendered="#{totalScores.allSubmissions eq '4' && description.assessmentGradingId != -1}" value="#{description.assessmentGradingId}" />
       <h:outputText rendered="#{totalScores.allSubmissions != '4' && description.assessmentGradingId == -1}" value="#{evaluationMessages.na}" />
     </h:panelGroup>
    </h:column>
 

   <!-- STUDENT ID -->
    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType!='agentDisplayId'}" >
     <f:facet name="header">
       <h:commandLink title="#{evaluationMessages.t_sortUserId}" id="agentDisplayId" action="totalScores" >
          <h:outputText value="#{evaluationMessages.uid}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="agentDisplayId" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.agentDisplayId}" />
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'agentDisplayId' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortUserId}" action="totalScores">
          <h:outputText value="#{evaluationMessages.uid}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortUserIdDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText value="#{description.agentDisplayId}" />
    </h:column>
    
    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'agentDisplayId' && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortUserId}" action="totalScores">
        <h:outputText value="#{evaluationMessages.uid}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortUserIdAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
        <h:outputText value="#{description.agentDisplayId}" />
    </h:column>
 

    <!-- ROLE -->
    <h:column rendered="#{totalScores.sortType ne 'role'}">
     <f:facet name="header" >
        <h:commandLink title="#{evaluationMessages.t_sortRole}" id="role" action="totalScores">
          <h:outputText value="#{evaluationMessages.role}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="role" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.role}" />
    </h:column>

    <h:column rendered="#{totalScores.sortType=='role' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortRole}" action="totalScores">
          <h:outputText value="#{evaluationMessages.role}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortRoleDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
       <h:outputText value="#{description.role}" />
    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='role'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortRole}" action="totalScores">
        <h:outputText value="#{evaluationMessages.role}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortRoleAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
       <h:outputText value="#{description.role}" />
    </h:column>
    

    <!-- DATE -->
    <h:column rendered="#{totalScores.sortType!='submittedDate' && totalScores.allSubmissions!='4'}">
     <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" id="submittedDate" action="totalScores">
          <h:outputText value="#{evaluationMessages.submit_date}" />
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="submittedDate" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.submittedDate}" rendered="#{description.attemptDate != null}" >
          <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
        </h:outputText>
        <h:panelGroup rendered="#{description.attemptDate != null}">
          <h:panelGroup rendered="#{description.isLate == 'true' && ((description.isAutoSubmitted == 'false' && !(totalScores.isTimedAssessment eq 'true' && totalScores.acceptLateSubmission eq 'false'))
                                      || (description.isAutoSubmitted == 'true' && totalScores.acceptLateSubmission eq 'true' && totalScores.isTimedAssessment ne 'true'))}">
            <f:verbatim><br/></f:verbatim>
            <h:outputText style="color:red" value="#{evaluationMessages.late}"/>
          </h:panelGroup>
          <h:panelGroup rendered="#{description.isAutoSubmitted == 'true' && totalScores.isTimedAssessment ne 'true' && (description.isLate == 'false' || totalScores.acceptLateSubmission eq 'true')}">
            <f:verbatim><br/></f:verbatim>
            <h:outputText style="color:red" value="#{evaluationMessages.auto_submit}"/>
          </h:panelGroup>
        </h:panelGroup>

      <h:outputText value="#{evaluationMessages.no_submission}" rendered="#{description.attemptDate == null}"/>
    </h:column>

    <h:column rendered="#{totalScores.sortType=='submittedDate' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" action="totalScores">
          <h:outputText value="#{evaluationMessages.submit_date}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmittedDateDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText value="#{description.submittedDate}" rendered="#{description.attemptDate != null}" >
          <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
        </h:outputText>
        <h:panelGroup rendered="#{description.attemptDate != null}">
          <h:panelGroup rendered="#{description.isLate == 'true' && ((description.isAutoSubmitted == 'false' && !(totalScores.isTimedAssessment eq 'true' && totalScores.acceptLateSubmission eq 'false'))
                                      || (description.isAutoSubmitted == 'true' && totalScores.acceptLateSubmission eq 'true' && totalScores.isTimedAssessment ne 'true'))}">
            <f:verbatim><br/></f:verbatim>
            <h:outputText style="color:red" value="#{evaluationMessages.late}"/>
          </h:panelGroup>
          <h:panelGroup rendered="#{description.isAutoSubmitted == 'true' && totalScores.isTimedAssessment ne 'true' && (description.isLate == 'false' || totalScores.acceptLateSubmission eq 'true')}">
            <f:verbatim><br/></f:verbatim>
            <h:outputText style="color:red" value="#{evaluationMessages.auto_submit}"/>
          </h:panelGroup>
        </h:panelGroup>

        <h:outputText value="#{evaluationMessages.no_submission}" rendered="#{description.attemptDate == null}"/>
    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='submittedDate'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" action="totalScores">
        <h:outputText value="#{evaluationMessages.submit_date}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortSubmittedDateAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
        <h:outputText value="#{description.submittedDate}" rendered="#{description.attemptDate != null}" >
          <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
        </h:outputText>
        <h:panelGroup rendered="#{description.attemptDate != null}">
          <h:panelGroup rendered="#{description.isLate == 'true' && ((description.isAutoSubmitted == 'false' && !(totalScores.isTimedAssessment eq 'true' && totalScores.acceptLateSubmission eq 'false'))
                                      || (description.isAutoSubmitted == 'true' && totalScores.acceptLateSubmission eq 'true' && totalScores.isTimedAssessment ne 'true'))}">
            <f:verbatim><br/></f:verbatim>
            <h:outputText style="color:red" value="#{evaluationMessages.late}"/>
          </h:panelGroup>
          <h:panelGroup rendered="#{description.isAutoSubmitted == 'true' && totalScores.isTimedAssessment ne 'true' && (description.isLate == 'false' || totalScores.acceptLateSubmission eq 'true')}">
            <f:verbatim><br/></f:verbatim>
            <h:outputText style="color:red" value="#{evaluationMessages.auto_submit}"/>
          </h:panelGroup>
        </h:panelGroup>

        <h:outputText value="#{evaluationMessages.no_submission}" rendered="#{description.attemptDate == null}"/>
    </h:column>

    <!-- ANSWERS SUMMARY FOR ONE SELECTION TYPE -->
    <h:column rendered="#{totalScores.isOneSelectionType}">
      <f:facet name="header">
        <h:outputText value="#{evaluationMessages.answers_title}" escape="false"/>
      </f:facet>
      <h:panelGroup rendered="#{description.attemptDate != null}">
        <div>
          <h:outputText value="#{totalScores.results[description.assessmentGradingId][0]}"/>&nbsp;/&nbsp;
          <h:outputText value="#{totalScores.results[description.assessmentGradingId][1]}"/>&nbsp;/&nbsp;
          <h:outputText value="#{totalScores.results[description.assessmentGradingId][2]}"/>
        </div>
      </h:panelGroup>
    </h:column>

    <!-- TIME -->
    <h:column rendered="#{totalScores.isTimedAssessment && totalScores.sortType!='timeElapsed'}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortTime}" id="time" action="totalScores">
          <h:outputText value="#{evaluationMessages.time}" />
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="timeElapsed" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
      </f:facet>
      <h:outputText value="#{description.formattedTimeElapsed}" />
    </h:column>

	<h:column rendered="#{totalScores.isTimedAssessment && totalScores.sortType=='timeElapsed' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortTime}" action="totalScores">
          <h:outputText value="#{evaluationMessages.time}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortTimeDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
      <h:outputText value="#{description.formattedTimeElapsed}" />
    </h:column>
    
    <h:column rendered="#{totalScores.isTimedAssessment && totalScores.sortType=='timeElapsed'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortTime}" action="totalScores">
        <h:outputText value="#{evaluationMessages.time}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortTimeAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
      <h:outputText value="#{description.formattedTimeElapsed}" />
    </h:column>

    <!-- TOTAL -->
    <h:column rendered="#{totalScores.sortType!='totalAutoScore' && totalScores.allSubmissions!='4'}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortScore}" id="totalAutoScore" action="totalScores">
          <h:outputText value="#{evaluationMessages.score}" />
          <f:param name="sortBy" value="totalAutoScore" />
          <f:param name="sortAscending" value="true"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        </h:commandLink>
      </f:facet>
      <h:outputText value="#{description.roundedTotalAutoScore}" />
    </h:column>

    <h:column rendered="#{totalScores.sortType=='totalAutoScore' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortScore}" action="totalScores">
          <h:outputText value="#{evaluationMessages.score}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortAdjustScoreDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
      <h:outputText value="#{description.roundedTotalAutoScore}" />
    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='totalAutoScore'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortScore}" action="totalScores">
        <h:outputText value="#{evaluationMessages.score}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortAdjustScoreAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
      <h:outputText value="#{description.roundedTotalAutoScore}" />
    </h:column>
    
    <!-- ADJUSTMENT -->
    <h:column rendered="#{totalScores.sortType!='totalOverrideScore' && totalScores.allSubmissions!='4'}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortAdjustScore}" id="totalOverrideScore" action="totalScores">
    	    <h:outputText value="#{evaluationMessages.adjustment}" />
        	<f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
	        <f:param name="sortBy" value="totalOverrideScore" />
	        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
      </f:facet>
      <h:inputText value="#{description.totalOverrideScore}" size="5" id="adjustTotal" required="false" onchange="toPoint(this.id);" />
   </h:column>


    <h:column rendered="#{totalScores.sortType=='totalOverrideScore' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortAdjustScore}" action="totalScores">
          <h:outputText value="#{evaluationMessages.adjustment}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortScoreDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
      <h:inputText value="#{description.totalOverrideScore}" size="5" id="adjustTotal2" required="false" onchange="toPoint(this.id);" />
   </h:column>
    
    <h:column rendered="#{totalScores.sortType=='totalOverrideScore'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortAdjustScore}" action="totalScores">
        <h:outputText value="#{evaluationMessages.adjustment}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortScoreAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
      <h:inputText value="#{description.totalOverrideScore}" size="5" id="adjustTotal3" required="false" onchange="toPoint(this.id);" />
    </h:column>

    <!-- SUBMISSION COUNT (AVERAGE SCORE VIEW) -->
    <h:column rendered="#{totalScores.allSubmissions eq '4' && totalScores.sortType!='submissionCount'}">
     <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortSubmissionCount}" id="submissionCount" action="totalScores" >
        <h:outputText value="#{evaluationMessages.sub_count}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="submissionCount" />
        <f:param name="sortAscending" value="true"/>
      </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.submissionCount}" />
    </h:column>

    <h:column rendered="#{totalScores.allSubmissions eq '4' && totalScores.sortType=='submissionCount' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmissionCount}" action="totalScores">
          <h:outputText value="#{evaluationMessages.sub_count}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmissionCountDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>
      </f:facet>
      <h:outputText value="#{description.submissionCount}" />
    </h:column>

    <h:column rendered="#{totalScores.allSubmissions eq '4' && totalScores.sortType=='submissionCount'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortSubmissionCount}" action="totalScores">
        <h:outputText value="#{evaluationMessages.sub_count}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortSubmissionCountAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink>
      </f:facet>
      <h:outputText value="#{description.submissionCount}" />
    </h:column>

    <!-- FINAL SCORE -->
    <h:column rendered="#{totalScores.sortType!='finalScore'}">
     <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortFinalScore}" id="finalScore" action="totalScores" >
        <h:outputText value="#{evaluationMessages.tot}" />
         <f:actionListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="finalScore" />
        <f:param name="sortAscending" value="true"/>
      </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.roundedFinalScore}" />
    </h:column>

    <h:column rendered="#{totalScores.sortType=='finalScore' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortFinalScore}" action="totalScores">
          <h:outputText value="#{evaluationMessages.tot}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortFinalScoreDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
          </h:commandLink>    
      </f:facet>
      <h:outputText value="#{description.roundedFinalScore}" />
    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='finalScore'  && !totalScores.sortAscending}">
      <f:facet name="header">
      <h:commandLink title="#{evaluationMessages.t_sortFinalScore}" action="totalScores">
        <h:outputText value="#{evaluationMessages.tot}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortFinalScoreAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      </h:commandLink> 
      </f:facet>
      <h:outputText value="#{description.roundedFinalScore}" />
    </h:column>



    <!-- NOTIFY STUDENT: GRADING UPDATED (per-row) -->
    <h:column rendered="#{totalScores.gradingNotifyAvailable && totalScores.allSubmissions!='4'}">
      <f:facet name="header">
        <h:outputText value="#{evaluationMessages.notify_grading_updated_column}"/>
      </f:facet>
      <%-- mirror the listener's eligibility gate: no notify affordance on rows it would skip --%>
      <h:panelGroup layout="block" rendered="#{description.assessmentGradingId ne '-1' && description.attemptDate != null && description.forGrade}">
        <h:commandLink id="notifyRow" action="totalScores" styleClass="sam-notify-grading-updated"
            title="#{evaluationMessages.notify_grading_updated_tooltip}"
            rendered="#{!totalScores.notifyCooldown[description.assessmentGradingIdString]}">
          <span class="fa fa-paper-plane" aria-hidden="true"></span>
          <h:outputText value=" #{evaluationMessages.notify_grading_updated}" styleClass="sr-only"/>
          <%-- save pending comment/score edits first so the email is truthful --%>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreUpdateListener" />
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.NotifyGradingUpdatedListener" />
          <f:param name="studentid" value="#{description.idString}" />
          <f:param name="publishedIdd" value="#{totalScores.publishedId}" />
          <f:param name="gradingData" value="#{description.assessmentGradingId}" />
        </h:commandLink>
        <h:panelGroup rendered="#{totalScores.notifyCooldown[description.assessmentGradingIdString]}">
          <span class="fa fa-check" aria-hidden="true"></span>
          <h:outputText value=" #{evaluationMessages.notify_grading_updated_sent}"
              title="#{evaluationMessages.notify_grading_updated_cooldown}"/>
        </h:panelGroup>
        <h:panelGroup layout="block" styleClass="small sam-notify-last-sent"
            rendered="#{totalScores.notifyLastSent[description.assessmentGradingIdString] != null}">
          <h:outputFormat value="#{evaluationMessages.notify_grading_updated_last_sent}">
            <f:param value="#{totalScores.notifyLastSent[description.assessmentGradingIdString]}"/>
          </h:outputFormat>
        </h:panelGroup>
      </h:panelGroup>
    </h:column>

    <!-- COMMENT -->
    <h:column rendered="#{totalScores.sortType!='comments' && totalScores.allSubmissions!='4'}">
     <f:facet name="header">
      <h:panelGroup>
	  <h:commandLink title="#{evaluationMessages.t_sortCommentsForStudent}" id="comments" action="totalScores">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />    
        <h:outputText value="#{evaluationMessages.comment_for_student}"/>
        <f:param name="sortBy" value="comments" />
        <f:param name="sortAscending" value="true"/>
      </h:commandLink>
	  
	  <h:outputText value="&nbsp;&nbsp;" escape="false"/>
	  
	  <h:outputLink title="#{evaluationMessages.whats_this_link}" value="#" onclick="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" >
            <h:outputText  value="#{evaluationMessages.whats_this_link}"/>
      </h:outputLink>
	  </h:panelGroup>
     </f:facet>

   <h:inputTextarea value="#{description.comments}" rows="3" cols="30" rendered="#{description.attemptDate != null}" styleClass="awesomplete" />
   <h:inputTextarea value="#{evaluationMessages.requires_student_submission}" rows="3" styleClass="awesomplete" disabled="true" cols="30" rendered="#{description.attemptDate == null}"/>
   <h:panelGroup rendered="#{description.attemptDate != null}">
   		<%@ include file="/jsf/evaluation/totalScoresAttachment.jsp" %>
   </h:panelGroup>
    </h:column>

    <h:column rendered="#{totalScores.sortType=='comments' && totalScores.sortAscending}">
      <f:facet name="header">
        <h:panelGroup>
        <h:commandLink title="#{evaluationMessages.t_sortCommentsForStudent}" action="totalScores">
          <h:outputText value="#{evaluationMessages.comment_for_student}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortCommentDescending}" rendered="#{totalScores.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
  	    </h:commandLink>   
		<h:outputText value="&nbsp;&nbsp;" escape="false"/>
	  
        <h:outputLink title="#{evaluationMessages.whats_this_link}" value="#" onclick="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" >
            <h:outputText  value="#{evaluationMessages.whats_this_link}"/>
        </h:outputLink>
	  </h:panelGroup>
      </f:facet>
   <h:inputTextarea value="#{description.comments}" rows="3" cols="30" rendered="#{description.attemptDate != null}"/>
   <h:inputTextarea value="#{evaluationMessages.requires_student_submission}" rows="3" disabled="true" cols="30" rendered="#{description.attemptDate == null}"/>
   <h:panelGroup rendered="#{description.attemptDate != null}">
   		<%@ include file="/jsf/evaluation/totalScoresAttachment.jsp" %>
   </h:panelGroup>
    </h:column>
    
    <h:column rendered="#{totalScores.sortType=='comments'  && !totalScores.sortAscending}">
      <f:facet name="header">
     <h:panelGroup>
      <h:commandLink title="#{evaluationMessages.t_sortCommentsForStudent}" action="totalScores">
        <h:outputText value="#{evaluationMessages.comment_for_student}" />
        <f:param name="sortAscending" value="true"/>
        <h:graphicImage alt="#{evaluationMessages.alt_sortCommentAscending}" rendered="#{!totalScores.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />              
      </h:commandLink> 
	  <h:outputText value="&nbsp;&nbsp;" escape="false"/>
	  
	  <h:outputLink title="#{evaluationMessages.whats_this_link}" value="#" onclick="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('../evaluation/totalScoresCommentPopUp.faces','CommentForStudent','width=510,height=515,scrollbars=yes, resizable=yes');" >
            <h:outputText  value="#{evaluationMessages.whats_this_link}"/>
      </h:outputLink>
	  </h:panelGroup>
      </f:facet>
   <h:inputTextarea value="#{description.comments}" rows="3" cols="30" rendered="#{description.attemptDate != null}"/>
   <h:inputTextarea value="#{evaluationMessages.requires_student_submission}" rows="3" disabled="true" cols="30" rendered="#{description.attemptDate == null}"/>
   <h:panelGroup rendered="#{description.attemptDate != null}" >
   		<%@ include file="/jsf/evaluation/totalScoresAttachment.jsp" %>
   </h:panelGroup>
    </h:column>
  </h:dataTable>

<h:outputText value="#{evaluationMessages.mult_sub_highest}" rendered="#{totalScores.scoringOption eq '1'&& totalScores.multipleSubmissionsAllowed eq 'true' }"/>
<h:outputText value="#{evaluationMessages.mult_sub_last}" rendered="#{totalScores.scoringOption eq '2' && totalScores.multipleSubmissionsAllowed eq 'true' }"/>
<h:outputText value="#{evaluationMessages.mult_sub_average}" rendered="#{totalScores.scoringOption eq '4' && totalScores.multipleSubmissionsAllowed eq 'true' }"/>
</div>
<!-- bottom: back-to-top only; all action buttons live at the top -->
<p class="act">
   <h:outputLink value="#" styleClass="sam-batch-totop" onclick="return samScrollTo('samTop');" rendered="#{totalScores.allSubmissions!='4'}">
      <span class="fa fa-arrow-up" aria-hidden="true"></span>
      <h:outputText value=" #{evaluationMessages.batch_back_to_top}"/>
   </h:outputLink>

</p>
</div>
</h:form>

</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
