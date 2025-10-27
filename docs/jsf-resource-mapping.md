# JSF Resource Mapping Notes

## Current Situation
- Sakai portal rewrites tool URLs to include a placement-specific prefix such as `*/`. For tests & quizzes, resource requests arrive as `/*/javax.faces.resource/...`.
- `SamigoJsfTool` normalises `target` in `dispatch` by stripping the `/*/` prefix before delegating to the FacesServlet. This allows JSF to locate views and resources under `/jsf/...` correctly.
- A static copy of Mojarra's `jsf.js` now lives under `/samigo-app/js/jsf.js`; the Facelets head includes it explicitly so the browser pulls the script without hitting JSF resource mappings.

## Resulting Behaviour
- The page loads `jsf.js` from the static path, avoiding portal URL quirks while still retaining legacy behaviour.

## Remaining Considerations
- If other tools depend on the JSF runtime script, they can either adopt the same static copy or ensure their dispatch logic normalises the portal prefix before delegating to the FacesServlet.
- When stripping the `/*/` prefix, perform it before resource detection so JSF doesn't treat the request as a view navigation.

