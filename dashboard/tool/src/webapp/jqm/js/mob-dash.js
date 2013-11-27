$(document).ready(function(){

    if($('.pagerCell span.pager a[title="Go to next page"]').length){
        $('#newsMoreDummyButton').show();
    }
    else {
        $('#newsMoreDummyButton').hide();
    }
    $('#newsMoreDummyButton').click(function(e){
        e.preventDefault();
        $('.pagerCell span.pager a[title="Go to next page"]').trigger('click');
    })
    //function to examine initial length of $('#activ .pagerCell .pager a[title="Go to next page"]') to show or
    //not the dummy button (1 and 0)

    //Note - we will need to do this once for each view - Events, Activity, Starred

    //function to forward clicks on dummy button to "next" in hidden pager
    
    $('#activ .pagerCell .pager a[title="Go to next page"]').live('click', function(e){
        // loading items this way works, but starring still being handled via wicket
        //options: 1) hide stars; 2) show stars for information purposes; 3) spend some time
        //working around this limitation
        e.preventDefault();
        var href=$(this).attr('href');
        $.ajax({
            url: href,
            cache:false,
            dataType:'html'
        }).done(function(data){
            //extract from payload only the new items
            var newItems = $(data).find('ul.itemList').find('li');
            //and also extract the new pager
            var newPager = $(data).find('.pagerCell').find('span.pager');
            //insert logic here to see if there are any new items to retrieve
            //by examining length of $('#activ .pagerCell .pager a[title="Go to next page"]');
            // if no length, hide dummy button, if there is, do not hide
            //replace old pager with the new one
            $('.pagerCell span.pager').replaceWith(newPager);
            //show or not the dummy "More" button
            if($('.pagerCell span.pager a[title="Go to next page"]').length){
                console.log($('.pagerCell span.pager a[title="Go to next page"]').length)
                $('#newsMoreDummyButton').show();
            }
            else {
                $('#newsMoreDummyButton').hide();
            }
            //append new items to the list
            $(newItems).appendTo('#newsPanel .itemList');
            //need something to flag which are the new items inserted - maybe an animation (start off
            //light yellow background and fade to white) 
        });
    });
});
