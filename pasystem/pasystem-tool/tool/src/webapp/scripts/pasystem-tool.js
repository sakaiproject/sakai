$(function () {

    var initDatePickers = function () {
        $('.pasystem-body .datepicker').each(function() {
            var $datepicker = $(this);

            var initDate = $(this).data('initDate');

            // setup date-time picker
            localDatePicker({
                input: '#' + $datepicker.attr('id'),
                useTime: 1,
                icon: 0,
                val: (initDate && initDate > 0) ? new Date(initDate) : undefined,
                ashidden: { iso8601: $datepicker.attr('id') + '_selected_datetime' },
            });

            // setup input button to trigger date-time picker
            $datepicker.siblings().find(".invoke-datepicker-btn").click(function() {
                $datepicker.focus();
            });

            // add clear action if present
            $datepicker.siblings().find(".clear-datepicker-btn").click(function() {
              $datepicker.val("");
            });
        });
    };


    var initDeleteConfirmation = function() {

      document.querySelectorAll(".pasystem-delete-btn").forEach(b => {

        b.addEventListener("click", function (event) {

          event.preventDefault();
          event.stopPropagation();

          const template = document.getElementById("pasystemDeleteConfirmationModalTemplate")?.innerHTML.trim().toString();
          const trimPathTemplate = TrimPath.parseTemplate(template, "pasystemDeleteConfirmationModalTemplate");

          const modal = trimPathTemplate.process({
                        recordType: this.dataset.recordType,
                        deleteURL: this.href
                        });

          this.closest(".portletBody").insertAdjacentHTML('beforeend', modal);

          const modalId = document.getElementById("pasystemDeleteConfirmationModal");
          (new bootstrap.Modal(modalId)).show();

        });
      });
    };


    var addPreviewHandlers = function () {
        $(document).on('click', '.preview-btn', function () {
            var url = $(this).prop('href')

            $.ajax({
                url: url,
                success: function (data) {
                    $('#popup-container-content').html(data);
                    new PASystemPopup('preview', 'preview');
                }
            });

            return false;
        });
    }


    var addFormHandlers = function () {
        var openCampaignRadio = $('#open-campaign-radio');

        if (openCampaignRadio.length > 0) {

          var distribution = $('#distribution');

          $('.campaign-visibility').on('change', function () {
            if ($(this).attr('id') == openCampaignRadio.attr('id')) {
              distribution.prop('disabled', true);
            } else {
              distribution.prop('disabled', false);
            }
          });
        }

        $('.pasystem-cancel-btn').on('click', function () {
            window.location.replace($(this).data('target'));
        });
    };


    initDatePickers();
    initDeleteConfirmation();
    addFormHandlers();
    addPreviewHandlers();
});
