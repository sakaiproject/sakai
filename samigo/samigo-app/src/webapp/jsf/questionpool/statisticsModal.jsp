<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:corejsf="http://java.sun.com/upload"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:sakai="http://sakaiproject.org/jsf2/sakai"
    xmlns:samigo="http://www.sakaiproject.org/samigo"
    xmlns:t="http://myfaces.apache.org/tomahawk">
  <jsp:element name="script">
    <jsp:attribute name="src" trim="false">/samigo-app/js/questionPoolStatistics.js</jsp:attribute>
    <jsp:body>
    </jsp:body>
  </jsp:element>
  <div id="stat-modal" class="modal fade" role="dialog" tabindex="-1" aria-labelledby="stat-modal-title">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h4 id="stat-modal-title" class="modal-title">
            <h:outputText value="#{questionPoolMessages.statistics_modal_title}"/>
            <jsp:element name="span">
              <jsp:attribute name="data-qp-title" trim="false"></jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
          </h4>
          <jsp:text><![CDATA[
        <button type="button" class="btn btn-close" data-bs-dismiss="modal" title="]]></jsp:text>
          <h:outputText value="#{authorMessages.button_close}"/>
          <jsp:text><![CDATA["></button>
      ]]></jsp:text>
        </div>
        <div class="modal-body">
          <div id="stat-modal-spinner" class="b5 d-flex flex-column align-items-center" role="status">
            <div id="stat-modal-spinner-info" class="sak-banner-info" role="status">
              <h:outputText value="#{questionPoolMessages.loading_statistics_info}"/>
            </div>
            <jsp:element name="div">
              <jsp:attribute name="class" trim="false">b5 spinner-border</jsp:attribute>
              <jsp:attribute name="aria-hidden" trim="false">true</jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
          </div>
          <div id="stat-table-wrapper">
            <table class="table table-bordered table-striped">
              <tr>
                <th scope="row">
                  <h:outputText value="#{questionPoolMessages.qs}"/>
                </th>
                <jsp:element name="td">
                  <jsp:attribute name="data-cell-questions" trim="false"></jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </tr>
              <tr>
                <th scope="row">
                  <h:outputText value="#{questionPoolMessages.subps}"/>
                </th>
                <jsp:element name="td">
                  <jsp:attribute name="data-cell-subPools" trim="false"></jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </tr>
              <tr>
                <th scope="row">
                  <h:outputText value="#{questionPoolMessages.times_used}"/>
                  <jsp:text><![CDATA[
                <span tabindex="0" data-bs-toggle="tooltip" title="]]></jsp:text>
                  <h:outputText value="#{questionPoolMessages.times_used_help}"/>
                  <jsp:text><![CDATA[">
                  ]]></jsp:text>
                  <jsp:element name="span">
                    <jsp:attribute name="class" trim="false">fa fa-info-circle</jsp:attribute>
                    <jsp:attribute name="aria-hidden" trim="false">true</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                  <jsp:text><![CDATA[
                </span>
              ]]></jsp:text>
                </th>
                <jsp:element name="td">
                  <jsp:attribute name="data-cell-useCount" trim="false"></jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </tr>
              <tr>
                <th scope="row">
                  <h:outputText value="#{questionPoolMessages.questions_attempted}"/>
                  <jsp:text><![CDATA[
                <span tabindex="0" data-bs-toggle="tooltip" title="]]></jsp:text>
                  <h:outputText value="#{questionPoolMessages.questions_attempted_help}"/>
                  <jsp:text><![CDATA[">
                  ]]></jsp:text>
                  <jsp:element name="span">
                    <jsp:attribute name="class" trim="false">fa fa-info-circle</jsp:attribute>
                    <jsp:attribute name="aria-hidden" trim="false">true</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                  <jsp:text><![CDATA[
                </span>
              ]]></jsp:text>
                </th>
                <jsp:element name="td">
                  <jsp:attribute name="data-cell-attempts" trim="false"></jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </tr>
              <tr>
                <th scope="row">
                  <h:outputText value="#{questionPoolMessages.questions_correct}"/>
                </th>
                <jsp:element name="td">
                  <jsp:attribute name="data-cell-correct" trim="false"></jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </tr>
              <tr>
                <th scope="row">
                  <h:outputText value="#{questionPoolMessages.questions_incorrect}"/>
                  <jsp:text><![CDATA[
                <span tabindex="0" data-bs-toggle="tooltip" title="]]></jsp:text>
                  <h:outputText value="#{questionPoolMessages.questions_incorrect_help}"/>
                  <jsp:text><![CDATA[">
                  ]]></jsp:text>
                  <jsp:element name="span">
                    <jsp:attribute name="class" trim="false">fa fa-info-circle</jsp:attribute>
                    <jsp:attribute name="aria-hidden" trim="false">true</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                  <jsp:text><![CDATA[
                </span>
              ]]></jsp:text>
                </th>
                <jsp:element name="td">
                  <jsp:attribute name="data-cell-incorrect" trim="false"></jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </tr>
              <tr>
                <th scope="row">
                  <h:outputText value="#{questionPoolMessages.questions_unanswered}"/>
                </th>
                <jsp:element name="td">
                  <jsp:attribute name="data-cell-blank" trim="false"></jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </tr>
              <tr>
                <th scope="row">
                  <h:outputText value="#{questionPoolMessages.difficulty}"/>
                </th>
                <jsp:element name="td">
                  <jsp:attribute name="data-cell-difficulty" trim="false"></jsp:attribute>
                  <jsp:body>
                  </jsp:body>
                </jsp:element>
              </tr>
            </table>
            <div id="excluded-types-info" class="sak-banner-info">
              <h:outputText value="#{questionPoolMessages.excluded_types}"/>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</jsp:root>
