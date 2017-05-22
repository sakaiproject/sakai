$(function () {

    var initDeleteConfirmation = function() {
      $(".tagservice-delete-btn").on("click", function(event) {
        event.preventDefault();
        event.stopPropagation();

        var template = $("#tagserviceDeleteConfirmationModalTemplate").html().trim().toString();
        var trimPathTemplate = TrimPath.parseTemplate(template, "tagserviceDeleteConfirmationModalTemplate");

        var $modal = $(trimPathTemplate.process({
                      recordType: $(this).data("record-type"),
                      deleteURL: $(this).prop("href")
                     }));

        $(this).closest(".portletBody").append($modal);

        $modal.modal();
      });
    };



    var addFormHandlers = function () {

        $('.tagservice-cancel-btn').on('click', function () {
            window.location.replace($(this).data('target'));
        });
    };

    initDeleteConfirmation();
    addFormHandlers();
});
