/**
 * Apply Highlight.js to CKEditor Code Snippet markup on view pages.
 *
 * CKEditor only highlights inside the editing iframe. On save it downcasts to
 * <pre><code class="language-xxx"> with plain text, so published content needs
 * this pass or snippets render as unstyled monospace.
 */
(function () {
    if (window.sakaiCodeSnippetHighlight && window.sakaiCodeSnippetHighlight.started) {
        return;
    }

    window.sakaiCodeSnippetHighlight = { started: true };

    var HLJS_BASE = '/library/webjars/ckeditor4/${ckeditor.version}/plugins/codesnippet/lib/highlight/';
    var SNIPPET_SELECTOR = 'pre code[class*="language-"]';
    var loading = null;

    function cdnQuery() {
        if (typeof portal !== 'undefined' && portal.portalCDNQuery) {
            return portal.portalCDNQuery;
        }
        try {
            if (window.parent && window.parent.portal && window.parent.portal.portalCDNQuery) {
                return window.parent.portal.portalCDNQuery;
            }
        } catch (e) {
            // Cross-origin parent.
        }
        return '';
    }

    function isInsideEditor(element) {
        return !!(element.closest && element.closest('.cke, .cke_inner, .cke_editable, .cke_wysiwyg_frame'));
    }

    function snippetBlocks(root) {
        var scope = root && root.querySelectorAll ? root : document;
        var nodes = scope.querySelectorAll(SNIPPET_SELECTOR);
        var blocks = [];
        for (var i = 0; i < nodes.length; i++) {
            if (!isInsideEditor(nodes[i]) && !nodes[i].classList.contains('hljs')) {
                blocks.push(nodes[i]);
            }
        }
        return blocks;
    }

    function ensureStylesheet() {
        var href = HLJS_BASE + 'styles/default.css' + cdnQuery();
        if (document.querySelector('link[data-sakai-codesnippet-theme]')) {
            return;
        }
        var link = document.createElement('link');
        link.rel = 'stylesheet';
        link.href = href;
        link.setAttribute('data-sakai-codesnippet-theme', 'default');
        document.head.appendChild(link);
    }

    function loadScript(src) {
        return new Promise(function (resolve, reject) {
            var existing = document.querySelector('script[src="' + src + '"]');
            if (existing) {
                if (existing.getAttribute('data-sakai-loaded') === 'true' || (src.indexOf('highlight.pack.js') !== -1 && window.hljs)) {
                    resolve();
                    return;
                }
                existing.addEventListener('load', function () { resolve(); });
                existing.addEventListener('error', reject);
                return;
            }
            var script = document.createElement('script');
            script.src = src;
            script.async = false;
            script.onload = function () {
                script.setAttribute('data-sakai-loaded', 'true');
                resolve();
            };
            script.onerror = reject;
            document.head.appendChild(script);
        });
    }

    function highlightBlocks(blocks) {
        if (!window.hljs || !blocks.length) {
            return;
        }
        for (var i = 0; i < blocks.length; i++) {
            window.hljs.highlightBlock(blocks[i]);
        }
    }

    function ensureHighlighter() {
        if (loading) {
            return loading;
        }
        ensureStylesheet();
        if (window.hljs) {
            loading = Promise.resolve();
            return loading;
        }
        var packUrl = HLJS_BASE + 'highlight.pack.js' + cdnQuery();
        loading = loadScript(packUrl);
        return loading;
    }

    function highlightNow(root) {
        var blocks = snippetBlocks(root);
        if (!blocks.length) {
            return;
        }
        ensureHighlighter().then(function () {
            highlightBlocks(snippetBlocks(root));
        }).catch(function (error) {
            console.error('Could not highlight CKEditor code snippets', error);
        });
    }

    function start() {
        highlightNow(document);

        if (!window.MutationObserver || !document.body) {
            return;
        }
        var scheduled = false;
        var observer = new MutationObserver(function () {
            if (scheduled) {
                return;
            }
            scheduled = true;
            window.requestAnimationFrame(function () {
                scheduled = false;
                highlightNow(document);
            });
        });
        observer.observe(document.body, { childList: true, subtree: true });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', start);
    } else {
        start();
    }
})();
