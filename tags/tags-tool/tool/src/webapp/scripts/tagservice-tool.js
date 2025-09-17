$(function () {

    const initDeleteConfirmation = function() {
      $(".tagservice-delete-btn").on("click", function(event) {
        event.preventDefault();
        event.stopPropagation();

        const template = $("#tagserviceDeleteConfirmationModalTemplate").html().trim().toString();
        const trimPathTemplate = TrimPath.parseTemplate(template, "tagserviceDeleteConfirmationModalTemplate");

        const $modal = $(trimPathTemplate.process({
                      recordType: $(this).data("record-type"),
                      deleteURL: $(this).prop("href")
                     }));

        $(this).closest(".portletBody").append($modal);

        new bootstrap.Modal($modal[0]).show();
      });
    };



    const addFormHandlers = function () {

        $('.tagservice-cancel-btn').on('click', function () {
            window.location.replace($(this).data('target'));
        });
    };

    initDeleteConfirmation();
    addFormHandlers();
});
