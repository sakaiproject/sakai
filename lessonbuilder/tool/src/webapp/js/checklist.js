(function (checklist, $, undefined) {
    var oldloc;
    var checklistItemName = "checklist-item-name";
    var checklistItemLink = "checklist-item-link";
    var dialogRadios = $("input[name=external-link-radios]");
    var externalLinkDialog = $('#externalLink-dialog');
    var externalLinkItemsAvailable = [];


    checklist.init = function () {
        $("input[name=external-link-radios]").each(function() {
            externalLinkItemsAvailable.push($(this).val());
        });

        if(externalLinkItemsAvailable.length == 0) {
            $(".external-link").hide();
        }

        loadPreviouslySavedChecklistItems();

        var confirmMsg = $('#deleteButtonLangSupp').html();
        $('.deleteButton').click(function () {
            return confirm(confirmMsg);
        });

        $('#additionalSettings').accordion({
            collapsible: true,
            active: false,
            heightStyle: "content"
        });

        // Hide group header if no group div
        if ($("#grouplist").length === 0) {
            $("#groupHeader").hide();
        }

        externalLinkDialog.dialog({
            autoOpen: false,
            width: modalDialogWidth(),
            modal: true,
            resizable: false,
            draggable: false
        }).parent('.ui-dialog').css('zIndex', 150000);

        $("#externalLink-dialog-close").click(function(e){
            e.preventDefault();
            externalLinkDialog.dialog('close');
            oldloc.focus();
            clearDialogRadios();
        });

        // "submit" the external link dialog
        $("#externalLink-dialog-submit").click(function (e) {
            e.preventDefault();
            var selected = $("input[name=external-link-radios]:checked").val();
            var target = $("#item-id").val();
            $("input[name=checklist-item-link" + target + "]").val(selected);
            $("#external-link-unlink-" + target).show();

            externalLinkDialog.dialog('close');
            updateUsedExternalLinks(selected);
            clearDialogRadios();
        });

        //add new checklist item
        $("#addChecklistItemButton").click(function(e){
          e.preventDefault();
          checklist.addChecklistItem();
        });

        // save all checklist items
        $("#save").click(function(){
           return checklist.validateChecklistForm();
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
        clonedAnswer.find("[name='checklist-item-link']").attr("name", "checklist-item-link" + num);

        clonedAnswer.find(".checklist-item-link").attr("id", "checklist-item-link-" + num);
        clonedAnswer.find("#external-link-").attr("id", "external-link-" + num);
        clonedAnswer.find("#external-link-unlink-").attr("id", "external-link-unlink-" + num);

        clonedAnswer.appendTo("#createdChecklistItems");

        $('#createdChecklistItems').sortable({
            handle: ".handle"
        });

        // external link button (opens dialog box)
        $("#external-link-" + num).click({id: num}, function(e) {
            e.preventDefault();

            oldloc = $(this);
            $("#item-id").val(e.data.id);
            var selected = $("input[name='" + checklistItemLink + e.data.id +"']").val();
            var array = getUsedExternalLinks();

           dialogRadios.each(function() {
               if($(this).val() === selected) {
                   $(this).prop('checked', true);
               }
               var dialogRadio = $(this);
               $.each(array, function(index, item) {
                   if(dialogRadio.val() !== selected && dialogRadio.val() === item.toString()) {
                       dialogRadio.prop('disabled', true);
                   }
               })
            });

            externalLinkDialog.dialog('open');
        });

        // external link unlinking (hide button and set up click event)
        clonedAnswer.find(".external-unlink").hide();
        $("#external-link-unlink-" + num).click({id: num}, function(e) {
            e.preventDefault();
            var cacheChecklistItemLink = $("#checklist-item-link-" + num);
            removeUsedExternalLinks(cacheChecklistItemLink.val());
            cacheChecklistItemLink.val("");
            $(this).hide();
        });

        // delete checklist Item.
        clonedAnswer.find(".deleteItemLink").click({id: num}, function(e){
            e.preventDefault();
            var linkedVal = $("#checklist-item-link-" + e.data.id).val();
            if(linkedVal !== "") {
                removeUsedExternalLinks(linkedVal);
            }
            $("#checklistItemDiv" + e.data.id).remove();
        });

        return clonedAnswer;
    };

    checklist.validateChecklistForm = function () {
        if ($.trim($('#name').val()) === '') {
            $('#checklist-error').text(msg("simplepage.checklist-name-required"));
            $('#checklist-error-container').show();
            return false;
        } else {
            $('#checklist-error-container').hide();
            return updateChecklistItems();
        }
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
        if (attributeString === null || attributeString === '') {
            return;
        }
        var obj = $.parseJSON(attributeString);
        if (obj) {
            $.each(obj.checklistItems, function (index, el) {
                var id = el.id;
                var name = el.name;
                var link = el.link;
                var linkPresent = false;
                if(link === undefined || link ===  -1) {
                    link = "";
                } else {
                    linkPresent = true;
                    if($.inArray(link.toString(), externalLinkItemsAvailable) === -1) {
                        linkPresent = false;
                        link = "";
                    }
                }

                var itemSlot = checklist.addChecklistItem();

                itemSlot.find(".checklist-item-id").val(id);
                itemSlot.find("." + checklistItemName).val(name);
                itemSlot.find("." + checklistItemLink).val(link);
                if(linkPresent) {
                    updateUsedExternalLinks(link);
                    itemSlot.find(".external-unlink").show();
                }
            });
        }
    }

    function updateChecklistItems() {
        $(".checklist-item-complete").each(function (index, el) {
            var id = $(el).parent().find(".checklist-item-id").val();
            var text = $.trim($(el).parent().find("." + checklistItemName).val());
            var link = $(el).parent().find("." + checklistItemLink).val();

            $(el).val(JSON.stringify({"index": index, "id": id, "text": text, "link": link}));
        });

        return true;
    }

    function getUsedExternalLinks() {
        var data = externalLinkDialog.data("externalLinkItemsUsed");
        if(data === undefined) {
           return [];
        } else {
            return data;
        }
    }

    function updateUsedExternalLinks(val) {
        var array = getUsedExternalLinks();
        if(val !== undefined) {
            array.push(val);
            externalLinkDialog.data("externalLinkItemsUsed", array);
        }
    }

    function removeUsedExternalLinks(removeItem) {
        var array = getUsedExternalLinks();

        array = $.grep(array, function(value) {
            return value.toString() !== removeItem;
        });

        externalLinkDialog.data("externalLinkItemsUsed", array);
    }

    function clearDialogRadios() {
        dialogRadios.each(function() {
            var dialogRadio = $(this);
            dialogRadio.prop('checked', false);
            dialogRadio.prop('disabled', false);
        })
    }

}(window.checklist = window.checklist || {}, jQuery));
