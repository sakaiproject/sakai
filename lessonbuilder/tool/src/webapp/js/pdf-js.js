$(function() {
    function adjustPdfIframeHeight($iframe) {
        var width = $iframe.width();
        var height = width * 0.75;
        $iframe.css('height', height + 'px');
    }

    $('iframe[src*="pdf-js"]').each(function() {
        var $iframe = $(this);
        var $section = $iframe.closest('.section');
        if ($section.length) {
            var ro = new ResizeObserver(function() {
                adjustPdfIframeHeight($iframe);
            });
            ro.observe($section[0]);
        }
    });
});
