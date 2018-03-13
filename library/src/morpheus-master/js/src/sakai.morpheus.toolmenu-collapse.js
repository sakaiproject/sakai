jQuery(function () {
    var collapse = $PBJQ('.Mrphs-collapseTools');
    if (collapse.length > 0) {
        var floatIt = function () {
            var width = $PBJQ('#toolMenuWrap').outerWidth();

            collapse.css('position', 'fixed')
                    .css('bottom', 0)
                    .css('left', 0)
                    .css('display', 'flex')
                    .css('width', width);

            collapse.addClass('floatingToolMenu');
        };

        floatIt();

        collapse.on('click', floatIt);
    }
});
