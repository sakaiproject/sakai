(function () {
    const launchers = [];
    // Blocking fetch via Atomics.wait is not permitted on the main thread; always fall back to synchronous XHR.

    async function bootstrap() {
        const nodes = document.querySelectorAll('.scorm-rest-launcher[data-content-package-id]');
        for (const node of nodes) {
            if (node.dataset.initialized === 'true') {
                continue;
            }
            try {
                await initLauncher(node);
            } catch (err) {
                console.error('Failed to initialise SCORM launcher', err);
            }
        }
    }

    async function initLauncher(root) {
        const contentPackageId = Number.parseInt(root.dataset.contentPackageId, 10);
        if (!Number.isFinite(contentPackageId)) {
            return;
        }

        const completionUrl = root.dataset.completionUrl || '';
        const contextPath = (root.dataset.contextPath || '').replace(/\/$/, '');
        const apiBase = normaliseApiBase(root.dataset.apiBase, contextPath);
        const navRequest = root.dataset.navRequest ? Number.parseInt(root.dataset.navRequest, 10) : null;

        const frame = root.querySelector('.scorm-rest-launcher__frame');
        const message = root.querySelector('.scorm-rest-launcher__message');
        const tocContainer = root.querySelector('.scorm-rest-launcher__toc');
        const tocList = tocContainer ? tocContainer.querySelector('.scorm-rest-launcher__toc-list') : null;

        const state = {
            sessionId: null,
            completionUrl,
            apiBase,
            contextPath,
            contentPackageId,
            navigation: null,
            showToc: false,
            toc: [],
            currentActivityId: null,
            currentScoId: null,
            activeScoId: null,
            pendingScoId: null,
            runtimeInstalled: false,
        };

        root.dataset.initialized = 'true';

        function showMessage(text) {
            if (!message) {
                return;
            }
            if (text) {
                message.textContent = text;
                message.hidden = false;
            } else {
                message.textContent = '';
                message.hidden = true;
            }
        }

        function normaliseApiBase(baseValue, ctxPath) {
            const raw = (baseValue || '/api/scorm').trim();
            if (/^https?:\/\//i.test(raw)) {
                return raw.replace(/\/+$/, '');
            }
            const withoutLeading = raw.replace(/^\/+/, '');
            if (raw.startsWith('/')) {
                return `/${withoutLeading}`.replace(/\/+$/, '');
            }
            const prefix = (ctxPath || '').replace(/\/$/, '');
            if (prefix) {
                return `${prefix}/${withoutLeading}`.replace(/\/+$/, '');
            }
            return `/${withoutLeading}`.replace(/\/+$/, '');
        }

        function resolveUrl(path) {
            const normalised = path.startsWith('/') ? path.substring(1) : path;
            return `${state.contextPath}/${normalised}`;
        }

        function apiUrl(path) {
            const target = path.startsWith('/') ? path : `/${path}`;
            if (/^https?:\/\//i.test(state.apiBase)) {
                return `${state.apiBase}${target}`;
            }
            return `${state.apiBase}${target}`;
        }

        function safeParse(text) {
            try {
                return JSON.parse(text);
            } catch (e) {
                return null;
            }
        }

        function getCsrfToken() {
            const meta = document.querySelector('meta[name="csrf-token"], meta[name="_csrf"]');
            if (meta && meta.content) {
                return meta.content;
            }
            const bodyToken = document.body && document.body.dataset ? (document.body.dataset.csrfToken || document.body.dataset.csrf) : null;
            if (bodyToken) {
                return bodyToken;
            }
            if (window.portal && window.portal.csrfToken) {
                return window.portal.csrfToken;
            }
            if (window._portal && window._portal.csrfToken) {
                return window._portal.csrfToken;
            }
            const hiddenInput = document.querySelector('input[name="sakai_csrf_token"]');
            if (hiddenInput && hiddenInput.value) {
                return hiddenInput.value;
            }
            return null;
        }

        async function requestJson(url, payload) {
            const headers = { 'Content-Type': 'application/json' };
            const csrfToken = getCsrfToken();
            if (csrfToken) {
                headers['X-CSRF-Token'] = csrfToken;
                headers['sakai_csrf_token'] = csrfToken;
            }

            const response = await fetch(url, {
                method: 'POST',
                headers,
                credentials: 'same-origin',
                body: JSON.stringify(payload),
            });

            const text = await response.text();
            const data = text ? safeParse(text) : {};

            if (!response.ok) {
                const detail = data && data.message ? data.message : `SCORM service error (${response.status})`;
                throw new Error(detail);
            }

            if (data === null) {
                throw new Error('Invalid response from SCORM service.');
            }

            return data;
        }

        function renderToc() {
            if (!tocContainer || !tocList) {
                return;
            }
            if (!state.showToc || !Array.isArray(state.toc) || state.toc.length === 0) {
                tocContainer.hidden = true;
                while (tocList.firstChild) {
                    tocList.removeChild(tocList.firstChild);
                }
                return;
            }

            const fragment = document.createDocumentFragment();
            state.toc.forEach((entry) => {
                fragment.appendChild(createTocNode(entry));
            });
            tocContainer.hidden = false;
            while (tocList.firstChild) {
                tocList.removeChild(tocList.firstChild);
            }
            tocList.appendChild(fragment);
        }

        function createTocNode(entry) {
            const li = document.createElement('li');
            li.className = 'scorm-rest-launcher__toc-node';

            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'scorm-rest-launcher__toc-button';
            button.textContent = entry.title || entry.activityId || 'Untitled activity';

            if (!entry.activityId) {
                button.disabled = true;
            } else {
                button.addEventListener('click', () => handleTocSelect(entry.activityId));
            }

            const isCurrent = Boolean(entry.current) || (entry.activityId && entry.activityId === state.currentActivityId);
            if (isCurrent) {
                button.classList.add('scorm-rest-launcher__toc-button--current');
            }

            li.appendChild(button);

            if (Array.isArray(entry.children) && entry.children.length > 0) {
                const childList = document.createElement('ul');
                childList.className = 'scorm-rest-launcher__toc-children';
                entry.children.forEach((child) => {
                    childList.appendChild(createTocNode(child));
                });
                li.appendChild(childList);
            }

            return li;
        }

        async function handleTocSelect(activityId) {
            if (!activityId || activityId === state.currentActivityId || !state.sessionId) {
                return;
            }

            try {
                const response = await requestJson(apiUrl(`/sessions/${state.sessionId}/nav`), { choiceActivityId: activityId });
                applySessionResponse(response, { keepMessage: true });
            } catch (err) {
                console.error('[SCORM REST] navigation error', err);
                showMessage(err.message || 'Unable to navigate to the selected activity.');
            }
        }

        async function refreshSessionState() {
            if (!state.sessionId) {
                return;
            }
            try {
                const response = await fetch(apiUrl(`/sessions/${state.sessionId}`), {
                    method: 'GET',
                    headers: { Accept: 'application/json' },
                    credentials: 'same-origin',
                });
                if (!response.ok) {
                    return;
                }
                const text = await response.text();
                const data = text ? safeParse(text) : null;
                if (data) {
                    applySessionResponse(data, { keepMessage: true });
                }
            } catch (err) {
                console.debug('[SCORM REST] session refresh failed', err);
            }
        }

        function applySessionResponse(response, options = {}) {
            if (!response) {
                return false;
            }

            const responseState = response.state || 'READY';
            if (response.sessionId) {
                state.sessionId = response.sessionId;
            }

            const previousScoId = state.currentScoId;
            const nextScoId = response.currentScoId || null;

            state.navigation = response.navigation || null;
            state.showToc = Boolean(response.showToc);
            state.toc = Array.isArray(response.toc) ? response.toc : [];
            state.currentActivityId = response.currentActivityId || null;
            state.currentScoId = nextScoId;
            let launchUpdated = false;
            if (response.launchPath) {
                launchUpdated = updateLaunchFrame(response.launchPath);
                if (launchUpdated) {
                    refreshSessionState();
                }
            }

            if (!state.runtimeInstalled && nextScoId && !state.activeScoId) {
                state.activeScoId = nextScoId;
            }

            if (launchUpdated) {
                if (previousScoId && nextScoId && previousScoId !== nextScoId) {
                    state.pendingScoId = nextScoId;
                } else if (!previousScoId && nextScoId) {
                    state.activeScoId = nextScoId;
                    state.pendingScoId = null;
                }
            } else if (nextScoId && nextScoId !== state.activeScoId && !state.pendingScoId) {
                state.activeScoId = nextScoId;
            }

            if (!nextScoId) {
                state.pendingScoId = null;
                if (!launchUpdated) {
                    state.activeScoId = null;
                }
            }

            renderToc();

            if (responseState === 'DENIED' || responseState === 'ERROR') {
                showMessage(response.message || (responseState === 'DENIED' ? 'Unable to launch this SCORM package.' : 'Unable to continue this SCORM session.'));
                return false;
            }

            if (responseState === 'CHOICE_REQUIRED') {
                showMessage(response.message || 'Select an activity to continue.');
            } else if (response.message) {
                showMessage(response.message);
            } else if (!options.keepMessage) {
                showMessage('');
            }

            return true;
        }

        async function openSession() {
            const payload = {
                contentPackageId: state.contentPackageId,
            };
            if (completionUrl) {
                payload.completionUrl = completionUrl;
            }
            if (Number.isFinite(navRequest)) {
                payload.navigationRequest = navRequest;
            }

            try {
                const response = await requestJson(apiUrl('/sessions'), payload);
                console.debug('[SCORM REST] openSession response', response);
                const ok = applySessionResponse(response);
                if (ok && !state.runtimeInstalled) {
                    installRuntimeApi();
                    state.runtimeInstalled = true;
                }
                return ok;
            } catch (err) {
                console.error('[SCORM REST] openSession error', err);
                showMessage(err.message || 'Unable to start SCORM session.');
                return false;
            }
        }

        function updateLaunchFrame(launchPath) {
            if (!frame || !launchPath) {
                return false;
            }
            const target = resolveUrl(launchPath);
            const current = frame.dataset.currentSrc || frame.src;
            if (current === target) {
                return false;
            }
            frame.dataset.currentSrc = target;
            frame.src = target;
            adjustFrameHeight();
            return true;
        }

        function ensureSession() {
            return !!state.sessionId;
        }

        function buildRuntimeHeaders() {
            const headers = {
                'Content-Type': 'application/json',
                Accept: 'application/json',
                'X-Requested-With': 'XMLHttpRequest',
            };
            const csrfToken = getCsrfToken();
            if (csrfToken) {
                headers['X-CSRF-Token'] = csrfToken;
                headers.sakai_csrf_token = csrfToken;
            }
            return headers;
        }

        function createRuntimeRequest(payload) {
            return {
                url: apiUrl(`/sessions/${state.sessionId}/runtime`),
                headers: buildRuntimeHeaders(),
                body: JSON.stringify(payload),
                payload,
            };
        }

        function sendRuntimeRequestSync(request) {

            const xhr = new XMLHttpRequest();
            xhr.open('POST', request.url, false);
            Object.entries(request.headers).forEach(([name, value]) => {
                if (typeof value === 'string') {
                    xhr.setRequestHeader(name, value);
                }
            });
            xhr.withCredentials = true;

            try {
                xhr.send(request.body);
            } catch (err) {
                // Sync XHR blocked during page dismissal (e.g. iframe torn down by Lessons)
                return { aborted: false, data: {}, request, bestEffort: true };
            }

            if (xhr.status === 0) {
                console.warn(`[SCORM REST] runtime request aborted by browser (${request.payload.method})`);
                return { aborted: true, data: null, request };
            }

            if (xhr.status >= 200 && xhr.status < 300) {
                const data = xhr.responseText ? safeParse(xhr.responseText) : {};
                if (data === null) {
                    showMessage('Invalid response from SCORM service.');
                    return null;
                }
                console.debug('[SCORM REST] runtime response (XHR)', data);
                return { aborted: false, data, request };
            }

            const detail = xhr.responseText ? safeParse(xhr.responseText) : null;
            console.error('[SCORM REST] runtime error (XHR)', xhr.status, detail);
            showMessage(detail && detail.message ? detail.message : `SCORM service error (${xhr.status})`);
            return null;
        }

        function runtimeCall(method, args) {
            if (!ensureSession()) {
                return 'false';
            }

            const scoId = resolveRuntimeScoId(method);
            const payload = { method, arguments: args };
            if (scoId) {
                payload.scoId = scoId;
            }

            const request = createRuntimeRequest(payload);
            console.debug('[SCORM REST] runtime request', payload);

            if (method === 'Terminate') {
                const dispatched = dispatchTerminateRequest(request, scoId);
                if (dispatched) {
                    console.debug('[SCORM REST] runtime terminate dispatched asynchronously');
                    markTerminateComplete(scoId, { refreshAfter: 750 });
                    return 'true';
                }

                const fallback = sendRuntimeRequestSync(request);
                if (!fallback) {
                    console.warn('[SCORM REST] runtime terminate sync fallback failed');
                    markTerminateComplete(scoId, { refreshAfter: 1000 });
                    return 'true';
                }

                if (fallback.aborted) {
                    console.warn('[SCORM REST] runtime terminate sync aborted; attempting keepalive');
                    const keepalive = dispatchTerminateKeepalive(request, scoId);
                    markTerminateComplete(scoId, { refreshAfter: keepalive ? 750 : 1200 });
                    return 'true';
                }

                const terminateResponse = fallback.data || {};
                processRuntimeResponse(method, terminateResponse, scoId);
                return 'true';
            }

            const result = sendRuntimeRequestSync(request);
            if (!result) {
                return 'false';
            }

            if (result.aborted) {
                showMessage('SCORM service did not respond. Please check your connection.');
                return 'false';
            }

            if (result.bestEffort) {
                return method === 'GetLastError' ? '0'
                    : (method === 'SetValue' || method === 'Commit' || method === 'Initialize') ? 'true' : '';
            }

            const response = result.data || {};
            processRuntimeResponse(method, response, scoId);
            return typeof response.value === 'string' ? response.value : '';
        }

        function resolveRuntimeScoId(method) {
            if (method === 'Initialize') {
                return state.pendingScoId || state.currentScoId || state.activeScoId || null;
            }
            return state.activeScoId || state.currentScoId || null;
        }

        function dispatchTerminateKeepalive(request, scoId) {
            try {
                return dispatchTerminateRequest(request, scoId);
            } catch (err) {
                console.warn('[SCORM REST] terminate keepalive dispatch failed', err);
                return false;
            }
        }

        function dispatchTerminateRequest(request, scoId) {
            if (!request) {
                return false;
            }

            if (typeof fetch !== 'function') {
                console.warn('[SCORM REST] terminate keepalive not supported (fetch unavailable)');
                return false;
            }

            try {
                const headers = new Headers();
                Object.entries(request.headers).forEach(([name, value]) => {
                    if (typeof value === 'string') {
                        headers.append(name, value);
                    }
                });

                fetch(request.url, {
                    method: 'POST',
                    headers,
                    credentials: 'same-origin',
                    body: request.body,
                    keepalive: true,
                })
                    .then(async (response) => {
                        const text = await response.text().catch(() => '');
                        const data = text ? safeParse(text) : {};
                        if (!response.ok || data === null) {
                            console.warn('[SCORM REST] terminate keepalive response not OK', response.status);
                            afterRuntimeCall('Terminate', { value: 'true' }, scoId, { refreshAfter: 750 });
                            return;
                        }
                        processRuntimeResponse('Terminate', data || {}, scoId);
                    })
                    .catch((err) => {
                        console.warn('[SCORM REST] terminate keepalive rejected', err);
                        window.setTimeout(() => {
                            if (ensureSession()) {
                                refreshSessionState();
                            }
                        }, 750);
                    });

                console.debug('[SCORM REST] terminate keepalive dispatched (fetch)');

                return true;
            } catch (err) {
                console.warn('[SCORM REST] terminate keepalive fetch error', err);
                return false;
            }
        }

        function processRuntimeResponse(method, response, scoId) {
            console.debug('[SCORM REST] runtime response', response);

            let launchChanged = false;
            if (response.launchPath) {
                launchChanged = updateLaunchFrame(response.launchPath);
            }

            if (response.sessionEnded && completionUrl) {
                window.location.href = completionUrl;
                return;
            } else if (response.sessionEnded) {
                refreshSessionState();
            }

            if (launchChanged) {
                refreshSessionState();
            }

            afterRuntimeCall(method, response, scoId);
        }

        function markTerminateComplete(scoId, options = {}) {
            afterRuntimeCall('Terminate', { value: 'true' }, scoId, options);
        }

        function afterRuntimeCall(method, response, scoId, options = {}) {
            const value = response && typeof response.value === 'string' ? response.value : '';

            if (method === 'Initialize' && value === 'true') {
                const resolvedId = scoId || state.currentScoId;
                if (resolvedId) {
                    state.activeScoId = resolvedId;
                }
                state.pendingScoId = null;
            }

            if (method === 'Terminate' && value === 'true') {
                state.activeScoId = null;
            }

            if (options.refreshAfter) {
                window.setTimeout(() => {
                    if (ensureSession()) {
                        refreshSessionState();
                    }
                }, options.refreshAfter);
            }
        }

        function installRuntimeApi() {
            const api = {
                Initialize: (parameter) => runtimeCall('Initialize', [parameter]),
                Terminate: (parameter) => runtimeCall('Terminate', [parameter]),
                GetValue: (element) => runtimeCall('GetValue', [element]),
                SetValue: (element, value) => runtimeCall('SetValue', [element, value]),
                Commit: (parameter) => runtimeCall('Commit', [parameter]),
                GetLastError: () => runtimeCall('GetLastError', []),
                GetErrorString: (code) => runtimeCall('GetErrorString', [code]),
                GetDiagnostic: (code) => runtimeCall('GetDiagnostic', [code]),
            };

            if (!window.API_1484_11) window.API_1484_11 = api;
            if (!window.APIAdapter) window.APIAdapter = api;
        }

        const started = await openSession();
        if (!started && (!message || message.hidden || message.textContent === '')) {
            showMessage('Failed to start SCORM session.');
        }

        function adjustFrameHeight() {
            if (!frame) {
                return;
            }
            const rect = frame.getBoundingClientRect();
            const available = window.innerHeight - rect.top - 16;
            if (available > 0) {
                frame.style.height = `${Math.max(available, 400)}px`;
            }
        }

        adjustFrameHeight();
        window.addEventListener('resize', adjustFrameHeight, { passive: true });

        launchers.push({ root, state, adjustFrameHeight });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            bootstrap().catch((err) => console.error('SCORM launcher bootstrap failed', err));
        });
    } else {
        bootstrap().catch((err) => console.error('SCORM launcher bootstrap failed', err));
    }

    window.scormRestLauncher = {
        bootstrap,
        active: launchers,
    };

    // Refresh parent window when this popup closes (handles both normal exit and force-close)
    // Using pagehide event with persisted check to avoid triggering on bfcache navigations
    window.addEventListener('pagehide', function(event) {
        if (!event.persisted && window.opener && !window.opener.closed) {
            window.opener.location.reload();
        }
    });
})();
