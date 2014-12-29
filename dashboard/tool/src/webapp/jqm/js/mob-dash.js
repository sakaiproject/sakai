$(document).ready(function(){

    $('meta[name=viewport]').attr('content','width=device-width, user-scalable=no,  initial-scale=1.0, maximum-scale=1.0');

    $('.siteLink').click(function(e){
        e.preventDefault();
        return null;
    }) 
        

    $(document).bind('pagechange', function(){
        $('.ui-page-active .ui-listview').listview('refresh');
        $('.ui-page-active :jqmData(role=content)').trigger('create');
    });
    
    $('a[href=#activ]').click(function(){
        if ($('#activ .pagerCell span.pager a[title="Go to next page"]').length) {
            $('#activ #newsMoreDummyButton').show();
        }
        else {
            $('#activ #newsMoreDummyButton').hide();
        }
    });
    
    $('a[href=#events]').click(function(){
        if ($('#events .pagerCell span.pager a[title="Go to next page"]').length) {
            $('#events #calendarMoreDummyButton').show();
        }
        else {
            $('#events #calendarMoreDummyButton').hide();
        }
    });
    
    /*
     * TODO: tab click callback needs to examine the existence of the next button on the pager of the target view
     *
     */
    if ($('#activ .pagerCell span.pager a[title="Go to next page"]').length) {
        $('#activ #newsMoreDummyButton').show();
    }
    else {
        $('#activ #newsMoreDummyButton').hide();
    }
    $('#newsMoreDummyButton').live('click', function(e){
        e.preventDefault();
        $('#activ .pagerCell span.pager a[title="Go to next page"]').trigger('click');
    });
    
    $('#activ .pagerCell .pager a[title="Go to next page"]').live('click', function(e){
        // loading items this way works, but starring still being handled via wicket
        //options: 1) hide stars; 2) show stars for information purposes; 3) spend some time
        //working around this limitation
        e.preventDefault();
        var href = $(this).attr('href');
        $.ajax({
            url: href,
            cache: false,
            dataType: 'html'
        }).done(function(data){
            //extract from payload only the new items
            var newItems = $(data).find('#activ ul.itemList').find('li');
            //and also extract the new pager
            var newPager = $(data).find('#activ .pagerCell').find('span.pager');
            //insert logic here to see if there are any new items to retrieve
            //by examining length of $('#activ .pagerCell .pager a[title="Go to next page"]');
            // if no length, hide dummy button, if there is, do not hide
            //replace old pager with the new one
            $('#activ .pagerCell span.pager').replaceWith(newPager);
            //show or not the dummy "More" button
            if ($('#activ .pagerCell span.pager a[title="Go to next page"]').length) {
                $('#newsMoreDummyButton').show();
            }
            else {
                $('#activ #newsMoreDummyButton').hide();
            }
            //append new items to the list
            $(newItems).appendTo('#activ .itemList');
            //need something to flag which are the new items inserted - maybe an animation (start off
            //light yellow background and fade to white) 
        });
    });
    
    if ($('#events .pagerCell span.pager a[title="Go to next page"]').length) {
        $('#events #calendarMoreDummyButton').show();
    }
    else {
        $('#events #newsMoreDummyButton').hide();
    }
    $('#calendarMoreDummyButton').live('click', function(e){
        e.preventDefault();
        $('#events .pagerCell span.pager a[title="Go to next page"]').trigger('click');
    });
    
    $('#events .pagerCell .pager a[title="Go to next page"]').live('click', function(e){
        e.preventDefault();
        var href = $(this).attr('href');
        $.ajax({
            url: href,
            cache: false,
            dataType: 'html'
        }).done(function(data){
            //extract from payload only the new items
            var newItems = $(data).find('#events ul.itemList').find('li');
            //and also extract the new pager
            var newPager = $(data).find('#events .pagerCell').find('span.pager');
            //insert logic here to see if there are any new items to retrieve
            //by examining length of $('#activ .pagerCell .pager a[title="Go to next page"]');
            // if no length, hide dummy button, if there is, do not hide
            //replace old pager with the new one
            $('#events .pagerCell span.pager').replaceWith(newPager);
            //show or not the dummy "More" button
            if ($('#events .pagerCell span.pager a[title="Go to next page"]').length) {
                $('#events #calendarMoreDummyButton').show();
            }
            else {
                $('#events #calendarMoreDummyButton').hide();
            }
            //append new items to the list
            $(newItems).appendTo('#events .itemList');
            //need something to flag which are the new items inserted - maybe an animation (start off
            //light yellow background and fade to white) 
        });
    });
    
    // Set text and href of jQuery Mobile back buttons using
    // values from Sakai navigation buttons, which are hidden.
    var currentSiteLink = $('ul#pda-portlet-menu li.currentSiteLink span a');

    // Some site names are too long for the button on mobile devices, so skip this for now,
    // until we find a way to truncate jQM button names.
    //    var currentSiteLinkText = currentSiteLink.text();
    //    $('.backToSiteButtonLabel').text(currentSiteLinkText);

    var currentSiteLinkHref = currentSiteLink.attr('href');
    $('.backToSiteButton')
    	.attr('href', currentSiteLinkHref);
    
    // Set titles using value from Sakai navigation.
    // Supports future tool localization and name changes.
    var currentToolTitle = $('ul#pda-portlet-menu li.currentToolTitle span').text();
    $('h1.navtopTitleHeader')
    	.text(currentToolTitle);
    document.title = currentToolTitle;  // Fix title that was set by jQuery Mobile
});
