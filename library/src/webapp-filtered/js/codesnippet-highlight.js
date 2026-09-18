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

    const HLJS_BASE = '/library/webjars/ckeditor4/${ckeditor.version}/plugins/codesnippet/lib/highlight/';
    const SNIPPET_SELECTOR = 'pre code[class*="language-"]';
    let loading = null;

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

    function isHighlightableSnippet(element) {
        return !!(element && element.classList && !isInsideEditor(element) && !element.classList.contains('hljs'));
    }

    function snippetFromAddedNode(node) {
        if (!node) {
            return null;
        }
        if (node.nodeType === Node.TEXT_NODE) {
            node = node.parentElement;
        }
        if (!node || !node.closest) {
            return null;
        }
        const match = node.closest(SNIPPET_SELECTOR);
        return isHighlightableSnippet(match) ? match : null;
    }

    function snippetBlocks(root) {
        const blocks = [];
        if (!root) {
            return blocks;
        }
        if (!root.querySelectorAll) {
            const containing = snippetFromAddedNode(root);
            if (containing) {
                blocks.push(containing);
            }
            return blocks;
        }
        if (root.matches && root.matches(SNIPPET_SELECTOR) && isHighlightableSnippet(root)) {
            blocks.push(root);
        }
        const nodes = root.querySelectorAll(SNIPPET_SELECTOR);
        for (let i = 0; i < nodes.length; i++) {
            if (isHighlightableSnippet(nodes[i])) {
                blocks.push(nodes[i]);
            }
        }
        return blocks;
    }

    function ensureStylesheet() {
        const href = HLJS_BASE + 'styles/default.css' + cdnQuery();
        if (document.querySelector('link[data-sakai-codesnippet-theme]')) {
            return;
        }
        const link = document.createElement('link');
        link.rel = 'stylesheet';
        link.href = href;
        link.setAttribute('data-sakai-codesnippet-theme', 'default');
        document.head.appendChild(link);
    }

    function loadScript(src) {
        return new Promise(function (resolve, reject) {
            const existing = document.querySelector('script[src="' + src + '"]');
            if (existing) {
                if (existing.getAttribute('data-sakai-loaded') === 'true' || (src.indexOf('highlight.pack.js') !== -1 && window.hljs)) {
                    resolve();
                    return;
                }
                existing.addEventListener('load', function () { resolve(); });
                existing.addEventListener('error', reject);
                return;
            }
            const script = document.createElement('script');
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
        for (let i = 0; i < blocks.length; i++) {
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
        const packUrl = HLJS_BASE + 'highlight.pack.js' + cdnQuery();
        loading = loadScript(packUrl);
        return loading;
    }

    function highlightNow(root) {
        const blocks = snippetBlocks(root);
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
        let scheduled = false;
        let pendingNodes = [];
        const observer = new MutationObserver(function (mutations) {
            for (let i = 0; i < mutations.length; i++) {
                const added = mutations[i].addedNodes;
                for (let j = 0; j < added.length; j++) {
                    pendingNodes.push(added[j]);
                }
            }
            if (scheduled) {
                return;
            }
            scheduled = true;
            window.requestAnimationFrame(function () {
                scheduled = false;
                const roots = pendingNodes;
                pendingNodes = [];
                const seen = [];
                for (let r = 0; r < roots.length; r++) {
                    const blocks = snippetBlocks(roots[r]);
                    for (let b = 0; b < blocks.length; b++) {
                        if (seen.indexOf(blocks[b]) === -1) {
                            seen.push(blocks[b]);
                        }
                    }
                }
                for (let s = 0; s < seen.length; s++) {
                    highlightNow(seen[s]);
                }
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
