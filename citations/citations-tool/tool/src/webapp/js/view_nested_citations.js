(function () {

    function addAccordionFunctionality() {

        // h1 level collapse
        $('.h1NestedLevel ol').each(function () {
            $(this).hide();
        });

        // expand all on click
        $('.h1NestedLevel li[data-sectiontype="HEADING1"]  > div[id^=linkClick]').click(function() {
            $(this).parent().find('ol').slideToggle();
            var image =  $('#' + this.id.replace('linkClick', 'toggleImg')).get(0);

            if( image.src.indexOf("/library/image/sakai/white-arrow-right.gif")!=-1 ) {
                image.src = "/library/image/sakai/white-arrow-down.gif";
            } else {
                image.src = "/library/image/sakai/white-arrow-right.gif";
            }
        });
    }

    $(document).ready(function(){
        addAccordionFunctionality();
    });
}());
