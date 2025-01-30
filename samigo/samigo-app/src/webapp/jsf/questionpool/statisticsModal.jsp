<script src="/samigo-app/js/questionPoolStatistics.js"></script>
<div id="stat-modal" class="modal fade" role="dialog" tabindex="-1" aria-labelledby="stat-modal-title">
  <div class="modal-dialog">
    <div class="modal-content">
      <div class="modal-header">
        <h4 id="stat-modal-title" class="modal-title">
          <h:outputText value='#{questionPoolMessages.statistics_modal_title}'/>
          <span data-qp-title></span>
        </h4>
        <button type="button" class="btn btn-close" data-bs-dismiss="modal" title="<h:outputText value='#{authorMessages.button_close}'/>"></button>
      </div>
      <div class="modal-body">
        <div id="stat-modal-spinner" class="b5 d-flex flex-column align-items-center" role="status">
          <div id="stat-modal-spinner-info" class="sak-banner-info" role="status">
            <h:outputText value='#{questionPoolMessages.loading_statistics_info}'/>
          </div>
          <div class="b5 spinner-border" aria-hidden="true"></div>
        </div>
        <div id="stat-table-wrapper">
          <table class="table table-bordered table-striped">
            <tr>
              <th scope="row">
                <h:outputText value="#{questionPoolMessages.qs}" />
              </th>
              <td data-cell-questions></td>
            </tr>
            <tr>
              <th scope="row">
                <h:outputText value="#{questionPoolMessages.subps}" />
              </th>
              <td data-cell-subPools></td>
            </tr>
            <tr>
              <th scope="row">
                <h:outputText value="#{questionPoolMessages.times_used}" />
                <span tabindex="0" data-bs-toggle="tooltip" title="<h:outputText value='#{questionPoolMessages.times_used_help}'/>">
                  <span class="fa fa-info-circle" aria-hidden="true"></span>
                </span>
              </th>
              <td data-cell-useCount></td>
            </tr>
            <tr>
              <th scope="row">
                <h:outputText value="#{questionPoolMessages.questions_attempted}" />
                <span tabindex="0" data-bs-toggle="tooltip" title="<h:outputText value='#{questionPoolMessages.questions_attempted_help}'/>">
                  <span class="fa fa-info-circle" aria-hidden="true"></span>
                </span>
              </th>
              <td data-cell-attempts></td>
            </tr>
            <tr>
              <th scope="row">
                <h:outputText value="#{questionPoolMessages.questions_correct}" />
              </th>
              <td data-cell-correct></td>
            </tr>
            <tr>
              <th scope="row">
                <h:outputText value="#{questionPoolMessages.questions_incorrect}" />
                <span tabindex="0" data-bs-toggle="tooltip" title="<h:outputText value='#{questionPoolMessages.questions_incorrect_help}'/>">
                  <span class="fa fa-info-circle" aria-hidden="true"></span>
                </span>
              </th>
              <td data-cell-incorrect></td>
            </tr>
            <tr>
              <th scope="row">
                <h:outputText value="#{questionPoolMessages.questions_unanswered}" />
              </th>
              <td data-cell-blank></td>
            </tr>
            <tr>
              <th scope="row">
                <h:outputText value="#{questionPoolMessages.difficulty}" />
              </th>
              <td data-cell-difficulty></td>
            </tr>
          </table>
          <div id="excluded-types-info" class="sak-banner-info">
            <h:outputText value='#{questionPoolMessages.excluded_types}'/>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
