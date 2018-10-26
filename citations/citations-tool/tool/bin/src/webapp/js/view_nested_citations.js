(function () {

    function addAccordionFunctionality() {

        // collapse h1 level if not on print view
        if(window.location.href.indexOf("printView") == -1) {
            $('.h1NestedLevel ol').each(function () {
                $(this).hide();
            });

            // expand all h1 on click
            $('.h1NestedLevel li[data-sectiontype="HEADING1"]  > div[id^=linkClick]').click(function() {
                $(this).parent().find('ol').slideToggle();
                var image =  $('#' + this.id.replace('linkClick', 'toggleImg')).get(0);

                if( image.src.indexOf("/library/image/sakai/white-arrow-right.gif")!=-1 ) {
                    image.src = "/library/image/sakai/white-arrow-down.gif";
                } else {
                    image.src = "/library/image/sakai/white-arrow-right.gif";
                }
            });

            // expand all h2 or h3 on click
            $('.h2NestedLevel li[data-sectiontype="HEADING2"]  > div[id^=linkClick], .h3NestedLevel li[data-sectiontype="HEADING3"]  > div[id^=linkClick]').click(function() {
                $(this).parent().find('ol').slideToggle();
                var image =  $('#' + this.id.replace('linkClick', 'toggleImg')).get(0);

                if( image.src.indexOf("/library/image/sakai/collapse.gif")!=-1 ) {
                    image.src = "/library/image/sakai/expand.gif";
                } else {
                    image.src = "/library/image/sakai/collapse.gif";
                }
            });
        }
    }

    $(document).ready(function(){
        addAccordionFunctionality();
    });
}());
