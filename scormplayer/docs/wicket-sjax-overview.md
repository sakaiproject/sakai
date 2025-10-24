# Wicket SJAX Flow in the SCORM Player

This note summarizes how the legacy Wicket “SJAX” bridge in Sakai’s SCORM Player shuttles runtime API calls between an in-browser SCO and the server, and sketches a path toward a REST-based replacement.

## Runtime Call Flow
- **SCORM API shim**: The SCO invokes `API.SetValue`, `API.GetValue`, `API.Commit`, or `API.Terminate`. Our injected `scorm-sjax.js` implementation forwards each call to `ScormSjax.sjaxCall(...)`.
- **AJAX dispatch**: `sjaxCall` builds a POST to the current `ScormPlayerPage` instance. Each API verb has its own Wicket callback URL (e.g., `...-setvaluecall`), so the request is routed to `SjaxCall` on the server.
- **Server processing**: `SjaxCall` hands the message to `ScormApplicationService`. `SetValue` and friends immediately update the Hibernate-managed `SCODataManager`, so values persist before the response returns.

## Response Structure
Wicket replies with its standard XML envelope:

1. `<component>` – fresh markup for the `sjaxContainer` div, including hidden inputs with the next round of callback URLs.
2. `<header-contribution>` – script tags that re-bootstrap Wicket Ajax and re-register the SCORM API adapter.
3. `<evaluate>` – inline JavaScript that runs immediately (e.g., queuing follow-up Ajax calls, publishing `scormresult='true'`).

The client replaces the entire `sjaxContainer`, then executes any inline or external scripts listed in the response.

## Client Execution Details
- **DOM replacement** relies on `Wicket.DOM.replace` (present in Wicket 9+) to swap out `sjaxContainer`.
- **Header script execution** pulls inline code from `<script>` tags and evaluates it. The current implementation filters out malformed payloads to avoid throwing `SyntaxError`.
- **Result handling** reads `scormresult` (or an error token) from the evaluated script environment and resolves the original `ScormSjax.sjaxCall` promise back to the SCO.

## Error Behaviour
- Any exception while evaluating `<header-contribution>` or `<evaluate>` scripts is logged via `Wicket.Log.error` and returned to the SCO as the standard “Could not retrieve/store a value from the LMS” alert.
- The SCO runtime toggles `terminateCalled` after `API.Terminate("")`. Subsequent SCORM API calls short‑circuit on the client, but the server has already persisted the attempt and sequencing state.

## Why SJAX Feels Fragile
- **XML + inline JS**: Responses HTML‑encode snippets, so client parsing must decode them before `eval`, making the handler brittle.
- **Full container refresh**: Every round-trip rebuilds the hidden form, reinitializes Wicket Ajax, and forces redundant script execution.
- **Opaque errors**: Failures surface as generic alerts inside the SCO, making diagnostics hard without browser dev tools and server logs.

## Evolving Toward REST
1. **Expose REST endpoints** – Map `SetValue`, `GetValue`, `Commit`, and `Terminate` onto `/api/scorm/attempts/{id}` routes that speak JSON while delegating to the existing `ScormApplicationService`.
2. **New client adapter** – Replace `ScormSjax.sjaxCall` with a `fetch()` wrapper that returns `{ success, data }` payloads but preserves the SCORM 2004 API surface the SCO expects.
3. **Session handling** – Use Sakai’s session cookies or tokens instead of hidden fields for state. Include attempt IDs in the URL and enforce permissions server-side.
4. **Progressive rollout** – Keep the SJAX bridge as a fallback (feature flag per tool instance) while onboarding content to the REST adapter, then retire the Wicket behavior once confidence is high.

Moving the SCORM runtime onto a conventional REST layer eliminates the XML parsing and DOM-replacement gymnastics, clarifies error handling, and opens the door to reusing the API for new web components or mobile clients.
