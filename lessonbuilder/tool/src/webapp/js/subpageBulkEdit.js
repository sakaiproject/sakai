(function (subpagebulkedit, $, undefined) {

    subpagebulkedit.init = function () {
        $("#bulk-edit-form").on("submit", function(){
            return subpagebulkedit.validateBulkEditForm();
        });
    };

    subpagebulkedit.validateBulkEditForm = function () {
        let jsonarr = [];
        $(".subpage-item").each(function () {
            let id = $(this).find(".subpage-item-id").val();
            let title = $.trim($(this).find(".subpage-item-title").val());
            jsonarr.push(JSON.stringify({"itemId": id, "title": title}));
        });
        $("#subpage-bulk-edit-json").val(JSON.stringify(jsonarr));
    };
}(window.subpagebulkedit = window.subpagebulkedit || {}, jQuery));