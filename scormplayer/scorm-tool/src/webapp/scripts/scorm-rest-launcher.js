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
        const apiBase = root.dataset.apiBase || '/api/scorm';
        const navRequest = root.dataset.navRequest ? Number.parseInt(root.dataset.navRequest, 10) : null;

        const frame = root.querySelector('.scorm-rest-launcher__frame');
        const message = root.querySelector('.scorm-rest-launcher__message');

        const state = {
            sessionId: null,
            completionUrl,
            apiBase,
            contextPath,
            contentPackageId,
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

        function resolveUrl(path) {
            const normalised = path.startsWith('/') ? path.substring(1) : path;
            return `${state.contextPath}/${normalised}`;
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
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
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
                const response = await requestJson(`${state.apiBase}/sessions`, payload);
                console.debug('[SCORM REST] openSession response', response);

                if (response.state && response.state !== 'READY') {
                    showMessage(response.message || 'Unable to launch this SCORM package.');
                    return false;
                }

                state.sessionId = response.sessionId;
                updateLaunchFrame(response.launchPath);
                showMessage('');
                installRuntimeApi();
                return true;
            } catch (err) {
                console.error('[SCORM REST] openSession error', err);
                showMessage(err.message || 'Unable to start SCORM session.');
                return false;
            }
        }

        function updateLaunchFrame(launchPath) {
            if (!frame || !launchPath) {
                return;
            }
            const target = resolveUrl(launchPath);
            const current = frame.dataset.currentSrc || frame.src;
            if (current === target) {
                return;
            }
            frame.dataset.currentSrc = target;
            frame.src = target;
            adjustFrameHeight();
        }

        function ensureSession() {
            return !!state.sessionId;
        }

        function sendRuntimeRequestSync(payload) {
            const url = `${state.apiBase}/sessions/${state.sessionId}/runtime`;
            console.debug('[SCORM REST] runtime request', payload);

            const xhr = new XMLHttpRequest();
            xhr.open('POST', url, false);
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.setRequestHeader('Accept', 'application/json');
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');
            const csrfToken = getCsrfToken();
            if (csrfToken) {
                xhr.setRequestHeader('X-CSRF-Token', csrfToken);
                xhr.setRequestHeader('sakai_csrf_token', csrfToken);
            }
            xhr.withCredentials = true;
            try {
                xhr.send(JSON.stringify(payload));
            } catch (e) {
                showMessage('Unable to reach SCORM service.');
                return null;
            }

            if (xhr.status === 0) {
                showMessage('SCORM service did not respond. Please check your connection.');
                console.error('[SCORM REST] runtime network error (XHR)', xhr);
                return null;
            }

            if (xhr.status >= 200 && xhr.status < 300) {
                const data = xhr.responseText ? safeParse(xhr.responseText) : {};
                if (data === null) {
                    showMessage('Invalid response from SCORM service.');
                    return null;
                }
                console.debug('[SCORM REST] runtime response (XHR)', data);
                return data;
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

            const response = sendRuntimeRequestSync({
                method,
                arguments: args,
            });

            if (!response) {
                return 'false';
            }

            if (response.launchPath) {
                updateLaunchFrame(response.launchPath);
            }

            if (response.sessionEnded && completionUrl) {
                window.location.href = completionUrl;
            }

            return typeof response.value === 'string' ? response.value : '';
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
        if (!started) {
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
})();
