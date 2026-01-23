document.addEventListener('DOMContentLoaded', () => {
    const adjustPdfIframeHeight = (iframe) => {
        const height = iframe.offsetWidth * 0.75;
        iframe.style.height = height + 'px';
    };

    document.querySelectorAll('iframe[src*="pdf-js"]').forEach((iframe) => {
        const section = iframe.closest('.section');

        // Set initial height
        adjustPdfIframeHeight(iframe);

        if (section) {
            const ro = new ResizeObserver(() => {
                adjustPdfIframeHeight(iframe);
            });
            ro.observe(section);
        }
    });
});
