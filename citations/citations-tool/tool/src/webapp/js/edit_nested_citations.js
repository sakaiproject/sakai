(function () {

    $(document).ready(function(){

        var isEditingEnabled,
            toggle = $("input[id^='toggle']"),
            addDescriptionButtons = $("input[id^='addDescription']"),
            removeButton = $("input[id^='removeSection']"),
            addH1SubsectionButton = $("input[id^='addH1SubsectionButton']"),
            addH2SubsectionButton = $("input[id^='addH2SubsectionButton']"),
            SECTION_INLINE_EDITOR = 'sectionInlineEditor',
            TOGGLE = 'toggle',
            DESCRIPTION = 'descriptionToggle';

        CKEDITOR.disableAutoInline = true; // don't show ckeditor on page load

        // handler for adding a section
        $('#addSectionButton').on('click', function() {

            // determine location on page
            var locationId = 1;
            $('#addSectionDiv').prevAll('li[id^="link"]').each(function() {
                locationId = locationId + 1 + $(this).find('li[id^="link"]').size();
            });

            // create html
            var divId = 'sectionInlineEditor' + locationId;
            var toggleId = 'toggle' + locationId;
            var removeDivId = 'removeSection' + locationId;
            var addSubsectionButtonId = 'addH1SubsectionButton' + locationId;
            var addDescriptionH1ButtonId = 'addDescriptionH1Button' + locationId;
            var sectionTitle = $('#sectionTitleText').val();
            var startEditingText = $('#startEditingText').val();
            var deleteSectionText = $('#deleteSectionText').val();
            var addSubsectionButtonText = $('#addSubsectionButtonText').val();
            var html = "<li id='link" + locationId + "' class='h1Editor' data-value='<h1>" + sectionTitle + "</h1>' data-location='" + locationId + "' data-sectiontype='HEADING1'><div id='"
                + divId + "' contenteditable='true' class='editor h1Editor sectionEditor'>" +
                "<h1>" + sectionTitle + "</h1></div>" +
                " <div id='buttonsDiv" + locationId + "' class='sectionButtons'><input type='button' id='" + toggleId + "' class='active' value='" + startEditingText + "'/>" +
                "<input type='button' id='" + removeDivId + "' class='active' value='" + deleteSectionText + "'/></div>" +
                "<div id='h1AddDescriptionDiv" + locationId + "'><div id='addDescriptionH1Editor" + locationId + "' class='editor'></div><div class='h1Editor' data-citation-action='add_subsection' " +
                "data-sectiontype='DESCRIPTION' class='h1Editor' style='margin-bottom:0px; padding-bottom:0px; padding-top:10px;'>" +
                "<input type='button' value='Add Description' style='padding-right:3px;' class='active' id='"+ addDescriptionH1ButtonId + "'></div></div>" +
                "<ol id='addSubsection" + locationId + "' class='h2NestedLevel holdCitations' style='display: block;'></ol>" +
                "<div style='padding:5px;'><input type='button' id='" + addSubsectionButtonId + "' class='active' value='" + addSubsectionButtonText + "'/></div></li>";
            $( "#addSectionDiv" ).before(html);

            // save to the db
            var actionUrl = $('#newCitationListForm').attr('action');
            $('#citation_action').val('add_section');
            var params = $('#newCitationListForm').serializeArray();
            params.push({name:'sectionType', value:'HEADING1'});
            params.push({name:'locationId', value:locationId});
            ajaxPost(actionUrl, params, true);

            // set up click handlers on buttons
            onClick( $('#' + toggleId).get(0), toggleEditor );
            onClick( $('#' + removeDivId).get(0), removeSection );
            onClick( $('#' + addSubsectionButtonId).get(0), addSubsection );
            onClick( $('#' + addDescriptionH1ButtonId).get(0), addSubsection );

            // refresh drag and drop list
            $("ol.serialization").sortable("destroy");
            createDragAndDropList();

            increasePageHeight(150);
        });

        // handler for adding a subsection
        function addSubsection() {

            // is h1 or h2
            var isH1 = $(this).attr('id').indexOf('addH1SubsectionButton')!=-1;
            var sectionType;
            if (isH1){
                sectionType = 'HEADING1';
            }
            else {
                sectionType = 'HEADING2';
            }

            var isAddDescriptionButton = $(this).attr('id').indexOf('addDescription')!=-1;
            if(isAddDescriptionButton){
                sectionType = 'DESCRIPTION';
            }

            // determine location on page
            var locationId = 0;
            var h1;
            if (sectionType === 'HEADING1') {
                h1 = $(this).parent().parent();

                // add up all the previous h1's on the page and their subsections
                h1.prevAll('li[id^="link"]').each(function() {
                    locationId = locationId + 1 + $(this).find('li[id^="link"]').size();
                });

                // add in the subsections of this h1
                locationId = locationId + 1 + h1.find('li[id^="link"]').size();

                // new location
                locationId = parseInt(locationId)+1;
            }
            else if (sectionType === 'HEADING2') {
                h1 = $(this).parent().parent().parent().parent();

                // add up all the previous h1's on the page and their subsections
                h1.prevAll('li[id^="link"]').each(function() {
                    locationId = locationId + 1 + $(this).find('li[id^="link"]').size(); // add 1 for the previous h1 and then all the subsections and citations
                });

                // add up all the previous h2's in this h1 and its subsections
                var h2 = $(this).parent().parent();
                h2.prevAll('li.h2Section').each(function() {
                    locationId = locationId + 1 + $(this).find('li[id^="link"]').size(); // this is the outer shell around the link so it includes the h2 so no need to +1
                });

                // add in the subsections of this h2
                locationId = locationId + 1 + h2.find('li[id^="link"]').size();

                // add this h1 in
                locationId = locationId + 1;

                // new location
                locationId = parseInt(locationId)+1;
            }
            else if (sectionType === 'DESCRIPTION') {
                h1 = $(this).parent().parent().parent();

                // new location
                locationId = parseInt(h1.data('location'))+1;
            }


            // create html
            var divId = 'sectionInlineEditor' + locationId;
            var toggleId = 'toggle' + locationId;
            var removeDivId = 'removeSection' + locationId;
            var sectionTitle = $('#sectionTitleText').val();
            var sectionDescription = $('#sectionDescriptionText').val();
            var addDescriptionH2ButtonId = 'addDescriptionH2Button' + locationId;
            var addDescriptionH3ButtonId = 'addDescriptionH3Button' + locationId;
            var startEditingText = $('#startEditingText').val();
            var addSubsectionButtonId = 'addH2SubsectionButton' + locationId;
            var addSubsectionButtonText = $('#addSubsectionButtonText').val();
            var deleteSectionText = $('#deleteSectionText').val();
            var html;
            if (sectionType === 'HEADING1'){
                html =
                    "<li id='link" + locationId + "' data-value='<h2>" + sectionTitle + "</h2>' class='h2Section' data-location='" + locationId + "' data-sectiontype='HEADING2'>" +
                    "<div id='" + divId + "' class='editor h2Editor sectionEditor' contenteditable='true'>" + "<h2>" + sectionTitle + "</h2></div>" +
                    "<div id='buttonsDiv" + locationId + "' style='margin-left: 5px;'><input type='button' id='" + toggleId + "' class='active' value='" + startEditingText + "'/>" +
                    "<input type='button' id='" + removeDivId + "' class='active' value='" + deleteSectionText + "'/></div>" +
                    "<div id='h2AddDescriptionDiv" + locationId + "'><div id='addDescriptionH2Editor" + locationId + "' class='editor'></div><div class='h2Editor' data-citation-action='add_subsection' " +
                    "data-sectiontype='DESCRIPTION' class='h2Editor' style='margin-bottom:0px; padding-bottom:0px; padding-top:10px;'>" +
                    "<input type='button' value='Add Description' style='padding-right:3px;' class='active' id='"+ addDescriptionH2ButtonId + "'></div></div>" +
                    "<ol id='addSubsection" + locationId + "' class='h3NestedLevel holdCitations' style='padding: 5px; display: block;'></ol>" +
                    "<div style='padding:5px;'><input type='button' id='" + addSubsectionButtonId + "' class='active' value='" + addSubsectionButtonText + "'/></div></li>";
                $(this).parent().prevAll('ol.h2NestedLevel').show();
                $(this).parent().prevAll('ol.h2NestedLevel').append(html);
            }
            else if (sectionType === 'HEADING2'){
                html =
                    "<li id='link" + locationId + "' data-value='<h3>" + sectionTitle + "</h3>' class='h3Section' data-location='" + locationId + "' data-sectiontype='HEADING3'>" +
                    "<div id='" + divId + "' class='editor h3Editor sectionEditor' contenteditable='true'>" + "<h3>" + sectionTitle + "</h3></div>" +
                    "<div id='buttonsDiv" + locationId + "' style='padding:5px;'><input type='button' id='" + toggleId + "' class='active' value='" + startEditingText + "'/>" +
                    "<input type='button' id='" + removeDivId + "' class='active' value='" + deleteSectionText + "'/></div>" +
                    "<div id='h3AddDescriptionDiv" + locationId + "'><div id='addDescriptionH3Editor" + locationId + "' class='editor'></div><div class='h3Editor' data-citation-action='add_subsection' " +
                    "data-sectiontype='DESCRIPTION' class='h3Editor' style='margin-bottom:0px; padding-bottom:0px; padding-top:10px;'>" +
                    "<input type='button' value='Add Description' style='padding-right:3px;' class='active' id='"+ addDescriptionH3ButtonId + "'></div></div>" +
                    "<ol class='h4NestedLevel holdCitations' style='padding: 5px; display: block;'></ol>" +
                    "</li>";
                $(this).parent().prevAll('ol.h3NestedLevel').show();
                $(this).parent().prevAll('ol.h3NestedLevel').append(html);
            }
            else if (sectionType === 'DESCRIPTION'){

                var li_class;
                if ($(this).attr('id').indexOf('addDescriptionH1Button')!=-1){
                    li_class = 'h2Section';
                }
                else if ($(this).attr('id').indexOf('addDescriptionH2Button')!=-1 ||
                    $(this).attr('id').indexOf('addDescriptionH3Button')!=-1){
                    li_class = 'h3Section';
                }

                html =
                    "<li id='link" + locationId + "' data-value='<p>" + sectionDescription + "</p>' class='" + li_class + " description' data-location='" + locationId + "' data-sectiontype='DESCRIPTION'>" +
                    "<div id='" + divId + "' class='editor h2Editor description' contenteditable='true'>" + "<p>" + sectionDescription + "</p></div>" +
                    "<div id='buttonsDiv" + locationId + "' class='descriptionButtons'><input type='button' id='" + toggleId + "' class='active' value='" + startEditingText + "'/>" +
                    "<input type='button' id='" + removeDivId + "' class='active' value='" + deleteSectionText + "'/>" +
                    "</div><ol class='h4NestedLevel holdCitations' style='padding: 5px; display: block;'></ol></li>";

                var liAbove = $(this).parent().parent();
                if ($(this).attr('id').indexOf('addDescriptionH1Button')!=-1){
                    liAbove.next('ol.h2NestedLevel').show().prepend(html);
                    liAbove.hide();
                }
                else if ($(this).attr('id').indexOf('addDescriptionH2Button')!=-1){
                    liAbove.next('ol.h3NestedLevel').show().prepend(html);
                    liAbove.hide();
                }
                else if ($(this).attr('id').indexOf('addDescriptionH3Button')!=-1){
                    liAbove.next('ol.h4NestedLevel').show().prepend(html);
                    liAbove.hide();
                }
            }

            
            // set up click handlers on buttons
            onClick( $('#' + toggleId).get(0), toggleEditor );
            onClick( $('#' + removeDivId).get(0), removeSection );
            if (sectionType === 'HEADING1'){
                onClick( $('#' + addSubsectionButtonId).get(0), addSubsection );
                onClick( $('#' + addDescriptionH2ButtonId).get(0), addSubsection );
            }
            else if (sectionType === 'HEADING2'){
                onClick( $('#' + addDescriptionH3ButtonId).get(0), addSubsection );
            }

            refreshIdsOnPage();


            // save to the db
            var actionUrl = $('#newCitationListForm').attr('action');
            $('#citation_action').val('add_subsection');
            var params = $('#newCitationListForm').serializeArray();
            if (sectionType === 'HEADING1'){
                params.push({name:'addSectionHTML', value:'<h2>' + sectionTitle + '</h2>'});
                params.push({name:'sectionType', value:'HEADING2'});
            }
            else if (sectionType === 'HEADING2'){
                params.push({name:'addSectionHTML', value:'<h3>' + sectionTitle + '</h3>'});
                params.push({name:'sectionType', value:'HEADING3'});
            }
            else if (sectionType === 'DESCRIPTION'){
                params.push({name:'addSectionHTML', value:'<p>' + 'Section Description' + '</p>'});
                params.push({name:'sectionType', value:'DESCRIPTION'});
            }
            params.push({name:'locationId', value:locationId});
            ajaxPost(actionUrl, params, true);

            $("ol.serialization").sortable("destroy");
            createDragAndDropList();

            addAccordionFunctionality(false);

            if (sectionType === 'HEADING1' || sectionType === 'DESCRIPTION'){
                increasePageHeight(110);
            }
            else if (sectionType === 'HEADING2'){
                increasePageHeight(90);
            }
        }

        function increasePageHeight(heightToAdd) {
            var iFrame = $(parent.document.getElementById(window.name));
            iFrame.height(iFrame.height() + heightToAdd);
        }

        function refreshIdsOnPage() {
            var count = 1;
            $('.h1NestedLevel li[id^="link"]').each(function() {
                $(this).attr('id', 'link' + count);
                $(this).attr('data-location', count);

                $(this).find("> div").each(function() {
                    var id = $(this).attr('id');
                    if (id!=null){
                        if (id.indexOf('sectionInlineEditor')!=-1){
                            $(this).attr('id', 'sectionInlineEditor' + count);
                        }
                        else if (id.indexOf('buttonsDiv')!=-1){
                            $(this).attr('id', 'buttonsDiv' + count);
                            $(this).find("> input").each(function() {
                                var id = $(this).attr('id');
                                if (id.indexOf('toggle')!=-1){
                                    $(this).attr('id', 'toggle' + count);
                                }
                                else if (id.indexOf('removeSection')!=-1){
                                    $(this).attr('id', 'removeSection' + count);
                                }
                                else if (id.indexOf('addH1SubsectionButton')!=-1){
                                    $(this).attr('id', 'addH1SubsectionButton' + count);
                                }
                                else if (id.indexOf('addH2SubsectionButton')!=-1){
                                    $(this).attr('id', 'addH2SubsectionButton' + count);
                                }
                            });
                        }
                    }
                });
                count++;
            });
        }

        function ajaxPost(actionUrl, params, async) {
            $.ajax({
                type: 'POST',
                url: actionUrl,
                cache: false,
                async: async,
                data: params,
                dataType: 'json',
                success: function (jsObj) {
                    $.each(jsObj, function (key, value) {
                        if (key === 'message' && value && 'null' !== value && '' !== $.trim(value)) {
                            reportSuccess(value);
                        } else if(key === 'sectionToRemove') {
                            // remove section from page
                            $(value).closest("li[id*='link']").remove();
                        } else if (key === 'secondsBetweenSaveciteRefreshes') {
                            citations_new_resource.secondsBetweenSaveciteRefreshes = value;
                        } else if ($.isArray(value)) {
                            reportError(key + value);
                        } else {
                            $('input[name=' + key + ']').val(value);
                        }
                    });
                },
                error: function (jqXHR, textStatus, errorThrown) {
                    reportError("failed: " + textStatus + " :: " + errorThrown);
                }
            });
        }

        function enableEditing(sectionInlineEditor, showBasicEditor) {
            if ( !CKEDITOR.instances[sectionInlineEditor] ) {
                if (showBasicEditor){

                    CKEDITOR.inline( sectionInlineEditor, {
                        startupFocus: true,
                        forcePasteAsPlainText : true,
                        on:
                        {
                            instanceReady:function(event)
                            {
                                var editorText = event.editor.getData();
                                if (editorText=='<h1>Section Title</h1>\n' ||
                                    editorText=='<h2>Section Title</h2>\n' ||
                                    editorText=='<h3>Section Title</h3>\n' ||
                                    editorText=='<p>Reading List Introduction</p>\n' ||
                                    editorText=='<p>Section Description</p>\n'){
                                    $('#' + event.editor.name).children().first().text(' ');
                                }
                            }
                        },
                        toolbar :
                            [
                                { name: 'basicstyles', items : [ 'Bold','Italic', 'Underline', 'Strike' ] },
                                { name: 'styles', items : [ 'Format' ] }
                            ]
                    } );

                }
                else {

                    CKEDITOR.inline( sectionInlineEditor, {
                        startupFocus: true,
                        forcePasteAsPlainText : true,
                        on:
                        {
                            instanceReady:function(event)
                            {
                                var editorText = event.editor.getData();
                                if (editorText=='<h1>Section Title</h1>\n' ||
                                    editorText=='<h2>Section Title</h2>\n' ||
                                    editorText=='<h3>Section Title</h3>\n' ||
                                    editorText=='<p>Reading List Introduction</p>\n' ||
                                    editorText=='<p>Section Description</p>\n'){
                                    $('#' + event.editor.name).children().first().text(' ');
                                }
                            }
                        },
                        toolbar :
                            [
                                { name: 'editing', items : [ 'Find','Replace' ] },
                                { name: 'basicstyles', items : [ 'Bold','Italic', 'Underline', 'Strike', 'Subscript','Superscript' ] },
                                { name: 'paragraph', items : [ 'NumberedList','BulletedList','BidiLtr','BidiRtl' ] },
                                { name: 'links', items : [ 'Link' ] },
                                { name: 'styles', items : [ 'Format' ] },
                                { name: 'colors', items : [ 'TextColor','BGColor' ] },
                                { name: 'tools', items : [ 'Maximize'] }
                            ]
                    } );


                }

            }
        }

        function disableEditing(sectionInlineEditor) {
            if ( CKEDITOR.instances[sectionInlineEditor])
                CKEDITOR.instances[sectionInlineEditor].destroy();
        }

        function toggleEditor() {
            if ( isEditingEnabled ) {  // clicked 'Finish Editing'

                // re-enable editing of 'Search Library' and nested list
                $('#Search').closest('li').show();
                $(".act *").removeAttr("disabled");

                disableEditing(this.id.replace(TOGGLE, SECTION_INLINE_EDITOR));
                $('#' + this.id.replace(TOGGLE, SECTION_INLINE_EDITOR)).attr( 'contenteditable', false );
                this.value = $('#startEditingText').val();
                isEditingEnabled = false;

                // save to db
                var actionUrl = $('#newCitationListForm').attr('action');
                if (this.id === 'toggleDescription') {
                    $('#citation_action').val('update_introduction');
                }
                else {
                    $('#citation_action').val('update_section');
                }
                var params = $('#newCitationListForm').serializeArray();
                params.push({name:'addSectionHTML', value:$('#' + this.id.replace(TOGGLE, SECTION_INLINE_EDITOR)).get(0).innerHTML});
                params.push({name:'sectionType', value:$(this).parent().parent().attr('data-sectiontype')});
                params.push({name:'locationId', value:this.id.replace(TOGGLE, "")});

                ajaxPost(actionUrl, params, true);

                // re enable drag and drop
                $("ol.serialization").sortable("enable"); //call widget-function enable
            }
            else { // clicked 'Edit'

                // disable editing of 'Search Library' and nested list functionality (except 'Finish 'Editing' button)
                $('#Search').closest('li').hide();
                $(".act *").not("#" + this.id).attr("disabled", "disabled");

                $('#' + this.id.replace(TOGGLE, SECTION_INLINE_EDITOR)).attr( 'contenteditable', true );
                var container = $(this).parent().parent();
                var showBasicEditor = !container.hasClass('serialization') && container.data('sectiontype')!='DESCRIPTION';
                enableEditing(this.id.replace(TOGGLE, SECTION_INLINE_EDITOR), showBasicEditor);
                this.value = $('#finishEditingText').val();
                isEditingEnabled = true;

                // disable drag and drop while editing
                $("ol.serialization").sortable("disable"); //call widget-function disable
            }
        }

        function removeSection() {
            var section = $(this).parent().parent();
            var confirmMessage;
            if (section != null && section.data('sectiontype')=='DESCRIPTION'){
                confirmMessage = $('#deleteButtonDescConfirmText').val();
            }
            else {
               confirmMessage = $('#deleteButtonConfirmText').val();
            }
            if(confirm(confirmMessage)) {

                var addButton;
                if (section != null && section.data('sectiontype')=='DESCRIPTION'){
                    addButton = $(this).parent().parent().parent().prev();
                }

                var actionUrl = $('#newCitationListForm').attr('action');
                $('#citation_action').val('remove_section');
                var params = $('#newCitationListForm').serializeArray();
                params.push({name: 'locationId', value: this.id.replace('removeSection', '')});
                ajaxPost(actionUrl, params, false);

                refreshIdsOnPage();

                addButton.show();
            }
        }


        function onClick( element, callback ) {
            if ( window.addEventListener ) {
                element.addEventListener( 'click', callback, false );
            }
            else if ( window.attachEvent ) {
                element.attachEvent( 'onclick', callback );
            }
        }

        function createDragAndDropList() {

            //create drag and drop list
            var group = $("ol.serialization").sortable({
                group: 'serialization',
                revert: true,
                delay: 100,
                isValidTarget: function (item, container) {
                    var sectiontype = item.data('sectiontype');
                    if (sectiontype=='CITATION'){
                        if (container.target.hasClass("holdCitations")){
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                    else if (sectiontype=='HEADING3'){
                        if (container.target.hasClass("h3NestedLevel")){
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                    else if (sectiontype=='HEADING2'){
                        if (container.target.hasClass("h2NestedLevel")){
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                    else if (sectiontype=='HEADING1'){
                        if (container.target.hasClass("h1NestedLevel")){
                            return true;
                        }
                        else {
                            return false;
                        }
                    }
                    else if (sectiontype=='DESCRIPTION'){
                        return false;
                    }

                    return true;
                },
                onDrop: function (item, container, _super) {

                    // put the editor's value in the link data-value
                    $('li[id^="link"]').each(function(index) {
                        var editorId = $(this).attr('id').replace('link', 'sectionInlineEditor');
                        var outerHTML = '';
                        if ($('#' + editorId).html()!=null){
                            outerHTML = $('#' + editorId).html().trim();
                        }
                        $(this).data('value', outerHTML);

                        var sectiontype = $(this).data('sectiontype');
                        if (sectiontype=='CITATION'){
                            $(this).find('a').each(function(){
                                var href = $(this).attr('href');
                                if (href.indexOf("location=0")!=-1){
                                    $(this).attr('href', href.replace("location=0", "location=" + index));
                                }
                            });
                        }
                    });

                    // if it's a citation dropped into nest then increase page height
                    var sectiontype = item.data('sectiontype');
                    if (sectiontype=='CITATION'){
                        increasePageHeight(70);
                    }

                    // if it's a citation being moved upwards then show the div and buttons
                    item.children().each(function( ) {
                        $(this).show();
                    });

                    // save to the db
                    var actionUrl = $('#newCitationListForm').attr('action');
                    $('#citation_action').val('drag_and_drop');
                    var params = $('#newCitationListForm').serializeArray();
                    params.push({name:'sectionType', value:item.data('sectiontype')});
                    var data = group.sortable("serialize").get()[0];
                    var jsonString = JSON.stringify(data, null, ' ')
                    jsonString  = jsonString.replace(/\\\"/g,'&quot;');
                    params.push({name:'data', value:jsonString});

                    ajaxPost(actionUrl, params, true);

                    refreshIdsOnPage();

                    _super(item, container);
                }
            });
        }

        function addAccordionFunctionality(collapseAllSections) {

            // remove any bound click events
            $('.h1NestedLevel li[data-sectiontype="HEADING1"] > div > div[id^=sectionInlineEditor]').unbind("click");
            $('.h2NestedLevel li[data-sectiontype="HEADING2"] > div > div[id^=sectionInlineEditor]').unbind("click");
            $('.h3NestedLevel li[data-sectiontype="HEADING3"] > div > div[id^=sectionInlineEditor]').unbind("click");


            // h1 level collapse expand
            if (collapseAllSections) {
                $('.h1NestedLevel ol').each(function () {
                    $(this).hide();
                });
            }

            $('.h1NestedLevel li[data-sectiontype="HEADING1"] > div > div[id^=sectionInlineEditor]').click(function() {
                $(this).parent().parent().find('ol').slideToggle();
                var image =  $('#' + this.id.replace('linkClick', 'toggleImg').replace('sectionInlineEditor', 'toggleImg')).get(0);

                if( image.src.indexOf("/library/image/sakai/white-arrow-right.gif")!=-1 ) {
                    image.src = "/library/image/sakai/white-arrow-down.gif";
                } else {
                    image.src = "/library/image/sakai/white-arrow-right.gif";
                }
            });

            // h2 level collapse expand
            if (collapseAllSections) {
                $('.h2NestedLevel ol').each(function () {
                    $(this).hide();
                });
            }

            $('.h2NestedLevel li[data-sectiontype="HEADING2"] > div > div[id^=sectionInlineEditor], .h3NestedLevel li[data-sectiontype="HEADING3"] > div > div[id^=sectionInlineEditor]').click(function() {
                $(this).parent().parent().find('ol').slideToggle();

                var image =  $('#' + this.id.replace('linkClick', 'toggleImg').replace('sectionInlineEditor', 'toggleImg')).get(0);

                if( image.src.indexOf("/library/image/sakai/expand.gif")!=-1 ) {
                    image.src = "/library/image/sakai/collapse.gif";
                } else {
                    image.src = "/library/image/sakai/expand.gif";
                }
            });

            // h3 level collapse expand
            if (collapseAllSections) {
                $('.h3NestedLevel ol').each(function () {
                $(this).hide();
                });
            }
        }


        createDragAndDropList();

        toggle.each(function( ) {
            onClick( this, toggleEditor );
        });
        addDescriptionButtons.each(function( ) {
            onClick( this, addSubsection);
        });
        removeButton.each(function( ) {
            onClick( this, removeSection);
        });
        addH1SubsectionButton.each(function( ) {
            onClick( this, addSubsection);
        });
        addH2SubsectionButton.each(function( ) {
            onClick( this, addSubsection);
        });

        addAccordionFunctionality(true);

        $('.unnestedList').each(function( ) {
            $(this).show();
        });

        // hide Add Description button if there's description for the section
        $('ol.serialization li[data-sectiontype="DESCRIPTION"]').each(function() {
            $(this).parent().prevAll('div[id^="h1AddDescriptionDiv"]').hide();// dont change these to nextall.. instead change the js if it isnt working there
            $(this).parent().prevAll('div[id^="h2AddDescriptionDiv"]').hide();
            $(this).parent().prevAll('div[id^="h3AddDescriptionDiv"]').hide();
        });

    });
}());

function addSectionHeightsToPageHeight(numSections) {
    var height = (numSections-1) * 100;
    window.onload = function() {
        var iFrame = $(parent.document.getElementById(window.name));
        iFrame.height(iFrame.height() + height);
    }
}

function selectManagingLibrary(value) {
    if (value!='-1'){
        $('#managingLibrary option[value=' + value + ']').attr('selected','selected');
    }
}
