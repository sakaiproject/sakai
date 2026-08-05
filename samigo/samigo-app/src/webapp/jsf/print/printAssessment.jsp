<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

<f:view>
  <f:verbatim>
    <!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
  </f:verbatim>

  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <%= request.getAttribute("html.head") %>
      <title>
        <h:outputText value="Quiz: #{pdfAssessment.title}" />
      </title>

      <script>
        function refreshPrintPdfPreview() {
          const iframe = document.getElementById('print-pdf-preview');
          const urlEl = document.getElementById('pdf-preview-url');
          const titleEl = document.getElementById('pdf-preview-title');
          if (iframe && urlEl) {
            iframe.src = urlEl.textContent.trim();
          }
          if (iframe && titleEl) {
            iframe.title = titleEl.textContent.trim();
          }
          if (iframe) {
            iframe.style.height = Math.max(600, iframe.offsetWidth * 0.75) + 'px';
          }
        }

        function applyPrintSettingsFromControl(control) {
          const form = control.closest('form');
          const applyBtn = form && form.querySelector('.auto-apply-settings');
          if (!form || !applyBtn) {
            return;
          }
          if (typeof form.requestSubmit === 'function') {
            form.requestSubmit(applyBtn);
          } else {
            applyBtn.click();
          }
        }

        document.addEventListener('DOMContentLoaded', function() {
          refreshPrintPdfPreview();
          window.addEventListener('resize', refreshPrintPdfPreview);

          document.querySelectorAll('form').forEach(form => {
            form.addEventListener('change', function(event) {
              const control = event.target;
              if (control.matches('.form-check-input, .form-select, input[type="radio"]')) {
                applyPrintSettingsFromControl(control);
              }
            });
          });
        });
      </script>
    </head>

    <body id="qb_print" class="view_student">
      <h:form id="assessmentForm">
        <h:commandLink styleClass="printReturnLink" action="#{pdfAssessment.getActionString}">
          <h:outputText value="#{printMessages.back_to_assessmt}" rendered="#{pdfAssessment.actionString == 'editAssessment'}" escape="false" />
          <h:outputText value="#{printMessages.back_to_landingpage}" rendered="#{pdfAssessment.actionString != 'editAssessment'}" escape="false" />
        </h:commandLink>

        <div class="container-fluid mt-3">
          <div class="navModeAction">
            <div class="vstack gap-2">
              <div class="form-check">
                <h:selectBooleanCheckbox id="showKeys" value="#{printSettings.showKeys}" styleClass="form-check-input" />
                <h:outputLabel for="showKeys" styleClass="form-check-label">
                  <h:outputText value="#{printMessages.show_answer_key}" />
                </h:outputLabel>
              </div>

              <div class="ms-4 mb-2">
                <h:selectOneRadio id="answerKeyFeedbackMode" value="#{printSettings.showKeysFeedback}" layout="pageDirection" styleClass="form-check" disabled="#{not printSettings.showKeys}">
                  <f:selectItem itemValue="#{false}" itemLabel="#{printMessages.show_answer_key_only}" />
                  <f:selectItem itemValue="#{true}" itemLabel="#{printMessages.show_answer_key_with_feedback}" />
                </h:selectOneRadio>
              </div>

              <div class="form-check">
                <h:selectBooleanCheckbox id="showPartIntros" value="#{printSettings.showPartIntros}" styleClass="form-check-input" />
                <h:outputLabel for="showPartIntros" styleClass="form-check-label">
                  <h:outputText value="#{printMessages.show_intros_titles}" />
                </h:outputLabel>
              </div>

              <div class="form-check">
                <h:selectBooleanCheckbox id="showSamePage" value="#{printSettings.showSamePage}" styleClass="form-check-input" />
                <h:outputLabel for="showSamePage" styleClass="form-check-label">
                  <h:outputText value="#{printMessages.show_same_page}" />
                </h:outputLabel>
              </div>

              <div class="mb-3 col-md-3">
                <h:outputLabel for="fontSize" styleClass="form-label">
                  <h:outputText value="#{printMessages.font_size}:" />
                </h:outputLabel>
                <h:selectOneMenu id="fontSize" value="#{printSettings.fontSize}" styleClass="form-select">
                  <f:selectItem itemLabel="#{printMessages.size_xsmall}" itemValue="1" />
                  <f:selectItem itemLabel="#{printMessages.size_small}" itemValue="2" />
                  <f:selectItem itemLabel="#{printMessages.size_medium}" itemValue="3" />
                  <f:selectItem itemLabel="#{printMessages.size_large}" itemValue="4" />
                  <f:selectItem itemLabel="#{printMessages.size_xlarge}" itemValue="5" />
                </h:selectOneMenu>
              </div>

              <div class="d-flex gap-2">
                <h:commandButton action="#{pdfAssessment.applyPrintSettings}" value="#{printMessages.apply_settings}" styleClass="btn btn-primary d-none auto-apply-settings" />
                <h:commandButton action="#{pdfAssessment.getPDFAttachment}" value="#{printMessages.print_pdf}" styleClass="btn btn-secondary" />
              </div>
            </div>
          </div>
        </div>

        <div class="container-fluid my-4">
          <span id="pdf-preview-url" class="visually-hidden" aria-hidden="true">
            <h:outputText value="#{pdfAssessment.pdfJsViewerUrl}" />
          </span>
          <span id="pdf-preview-title" class="visually-hidden" aria-hidden="true">
            <h:outputText value="#{printMessages.print_pdf}" />
          </span>
          <iframe id="print-pdf-preview"
            width="100%"
            style="min-height: 600px;">
          </iframe>
        </div>
      </h:form>
    </body>
  </html>
</f:view>
