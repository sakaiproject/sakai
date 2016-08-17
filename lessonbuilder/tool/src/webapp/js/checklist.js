(function (checklist, $, undefined) {

    checklist.init = function () {
        loadPreviouslySavedChecklistItems();

        var confirmMsg = $('#confirmDeleteLangSupp').html();
        $('.deleteButton').click(function () {
            return confirm(confirmMsg);
        });

        $('#additionalSettings').accordion({
            collapsible: true,
            active: false,
            heightStyle: "content"
        });
    };

    // Clones one of the checklist items and appends it to the end of the list
    checklist.addChecklistItem = function () {
        var clonedAnswer = $("#copyableChecklistItemDiv").clone(true);
        clonedAnswer.show();
        var num = $("#createdChecklistItems").find("li").length + 1; // Should be currentNumberOfChecklistItems + 1

        clonedAnswer.find(".checklist-item-id").val("-1");
        clonedAnswer.find(".checklist-item-name").val("");

        clonedAnswer.attr("id", "checklistItemDiv" + num);

        // Each input has to be renamed so that RSF will recognize them as distinct
        clonedAnswer.find("[name='checklist-item-complete']")
            .attr("name", "checklist-item-complete" + num)
            .addClass("checklist-item-complete");
        clonedAnswer.find("[name='checklist-item-complete-fossil']")
            .attr("name", "checklist-item-complete" + num + "-fossil");
        clonedAnswer.find("[name='checklist-item-id']")
            .attr("name", "checklist-item-id" + num);
        clonedAnswer.find("[for='checklist-item-name']")
            .attr("for", "checklist-item-name" + num);
        clonedAnswer.find("[name='checklist-item-name']")
            .attr("name", "checklist-item-name" + num);

        clonedAnswer.appendTo("#createdChecklistItems");

        $('#createdChecklistItems').sortable();

        return clonedAnswer;
    };

    checklist.validateChecklistForm = function () {
        if ($('#name').val() === '') {
            $('#checklist-error').text(msg("simplepage.checklist-name-required"));
            $('#checklist-error-container').show();
            return false;
        } else {
            $('#checklist-error-container').hide();
            return updateChecklistItems();
        }
    };

    checklist.deleteChecklistItem = function (el) {
        el.parent('li').remove();
    };

    function msg(s) {
        var m = document.getElementById(s);
        if (m === null) {
            return s;
        } else
            return m.innerHTML;
    }

    function loadPreviouslySavedChecklistItems() {

        var attributeString = $('#attributeString').val();
	if (attributeString === null || attributeString === '')
	    return;
        var obj = $.parseJSON(attributeString);
        if (obj) {
            $.each(obj.checklistItems, function (index, el) {
                var id = el.id;
                var name = el.name;

                var itemSlot = checklist.addChecklistItem();

                itemSlot.find(".checklist-item-id").val(id);
                itemSlot.find(".checklist-item-name").val(name);
            });
        }
    }

    function updateChecklistItems() {
        $(".checklist-item-complete").each(function (index, el) {
            var id = $(el).parent().find(".checklist-item-id").val();
            var text = $(el).parent().find(".checklist-item-name").val();

            $(el).val(index + ":" + id + ":" + text);
        });

        return true;
    }

}(window.checklist = window.checklist || {}, jQuery));