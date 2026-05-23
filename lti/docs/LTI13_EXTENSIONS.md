# Sakai LTI 1.3 / Advantage Extensions

This document describes Sakai-specific extensions to IMS LTI Advantage services. These are not part of the IMS Global specifications; tools should treat them as optional capabilities and inspect responses (or tool configuration) before relying on them.

---

## Gradebook read-only view (entire gradebook)

### Summary

By default, an LTI tool using the Assignment and Grade Services (AGS) line item API only sees gradebook columns that belong to that tool (IMS-AGS columns and linked assignment columns owned by the tool’s `tool_id`).

When **gradebook read-only view** is enabled for a tool, the tool may **read** every gradebook column in the site through the standard AGS line item and result endpoints. Columns the tool does not own are marked **read-only**. The tool cannot update, delete, or post scores to those columns.

Typical uses include analytics dashboards, advising views, or cross-activity reporting inside a single LTI tool without granting write access to the full gradebook.

### Administrator configuration

1. Open **External Tools** (LTI tool admin): `sakai.basiclti.admin`.
2. Edit the tool (or create one).
3. Under **Services**, enable:
   - **Allow External Tool to create grade columns** (`allowlineitems`) — required.
   - **Allow External Tool read-only access to entire gradebook** (`allowgradebookreadonly`).

Both must be enabled. If read-only view is checked but line items are not allowed, Sakai ignores read-only view at runtime.

The setting is stored on `lti_tools.allowgradebookreadonly` (boolean). Foorm adds the column automatically on startup when the LTI service initializes.

### OAuth scopes

No new OAuth scopes are defined. Use the same AGS scopes as for normal line item access:

| Scope | IMS URI |
|-------|---------|
| Line items (read) | `https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly` |
| Line items (read/write) | `https://purl.imsglobal.org/spec/lti-ags/scope/lineitem` |
| Results (read) | `https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly` |
| Scores (write) | `https://purl.imsglobal.org/spec/lti-ags/scope/score` |

Token issuance still requires `allowlineitems` (and `allowoutcomes` for result/score scopes), same as before.

### AGS behavior

Base URL pattern (context-scoped APIs use the site id as the placement segment; see SAK-47261):

```text
GET  /imsblis/lti13/lineitems/{context_id}
GET  /imsblis/lti13/lineitems/{context_id}/{lineitem_id}
GET  /imsblis/lti13/lineitems/{context_id}/{lineitem_id}/results
GET  /imsblis/lti13/lineitems/{context_id}/{lineitem_id}/results?user_id={lti_user_id}
GET  /imsblis/lti13/lineitems/{context_id}/{lineitem_id}/results/{user_id}
```

#### Result service: single-user queries

IMS [AGS 2.0](https://www.imsglobal.org/spec/lti-ags/v2p0) defines filtering to one learner with the **`user_id` query parameter** on the results collection:

```text
GET …/lineitems/{context_id}/{lineitem_id}/results?user_id={lti_user_id}
```

The response is a JSON **array** with at most one result (an empty array `[]` if the user has no score).

Sakai also accepts a **path segment** on the same collection URL (from older LIS v2 / LTI 2.0 outcomes REST and from Sakai `result.id` values):

```text
GET …/lineitems/{context_id}/{lineitem_id}/results/{user_id}
```

Both forms accept either the LTI 1.3 **subject** (`https://{platform}/user/{sakai_user_id}`) or the bare Sakai user id. Path segments in `result.id` URLs emitted by Sakai use the bare Sakai user id so tools can follow those links without conversion.

Prefer the query-parameter form for new tool code; it matches the AGS 2.0 specification and certification expectations.

#### When read-only view is **disabled** (default)

| Operation | Tool-owned columns | Other columns |
|-----------|-------------------|---------------|
| List line items | Included | Not listed |
| Get line item | Allowed | 404 |
| Get results | Allowed | 404 |
| PUT line item | Allowed | 404 |
| DELETE line item | Allowed | 404 |
| POST score | Allowed | 404 |

#### When read-only view is **enabled**

| Operation | Tool-owned columns | Other columns |
|-----------|-------------------|---------------|
| List line items | Included; `…/readOnly` omitted (writable) | Included; `…/readOnly`: `true` |
| Get line item | Allowed | Allowed |
| Get results | Allowed (all students in site) | Allowed (all students in site) |
| PUT line item | Allowed | **403** — `Line item is read-only` |
| DELETE line item | Allowed | **403** |
| POST score | Allowed | **403** — `Line item is read-only` |

**Ownership** is determined from the gradebook column’s `tool_id|content_id` key in `EXTERNAL_ID` (or legacy `EXTERNAL_DATA`), consistent with existing LTI line item authorization.

### Line item JSON: `readOnly` extension

When gradebook read-only view is enabled, the **list** (`GET …/lineitems/{context_id}`) and **detail** (`GET …/lineitems/{context_id}/{id}`) use the Sakai extension below. The property is **omitted unless the line item is read-only for this tool**; omission means writable (update, delete, post scores allowed).

| Property | Type | When present |
|----------|------|----------------|
| `https://www.sakailms.org/spec/lti-ags/v2p0/readOnly` | boolean | Only when `true` (column not owned by this tool). Absent when this tool may write to the column. |

When gradebook read-only view is **disabled**, the property is never sent (only tool-owned line items are listed; all are writable).

Detail example:

```json
{
  "id": "https://lms.example.edu/imsblis/lti13/lineitems/site-id/12345",
  "scoreMaximum": 100,
  "label": "Midterm",
  "https://www.sakailms.org/spec/lti-ags/v2p0/readOnly": true
}
```

List example:

```json
[
  {
    "id": "https://lms.example.edu/imsblis/lti13/lineitems/abc123/100",
    "label": "My tool quiz",
    "scoreMaximum": 10
  },
  {
    "id": "https://lms.example.edu/imsblis/lti13/lineitems/abc123/200",
    "label": "Manual gradebook column",
    "scoreMaximum": 100,
    "https://www.sakailms.org/spec/lti-ags/v2p0/readOnly": true
  }
]
```

Tools should:

1. Request line items with a `lineitem.readonly` or `lineitem` access token.
2. In the line item **list**, treat presence of `https://www.sakailms.org/spec/lti-ags/v2p0/readOnly: true` as read-only before calling write APIs.
3. If the property is absent, the line item is writable by this tool (when gradebook read-only view is enabled).

Query parameters for listing (`tag`, `resource_id`, `lti_link_id`) still apply when filtering the expanded list.

### Tool developer checklist

1. Confirm the Sakai administrator enabled both **line items** and **gradebook read-only view** for your tool.
2. Obtain a token with `lineitem.readonly` (read-only gradebook) or `lineitem` (read/write on owned columns only).
3. `GET` the line item container for the site `context_id`.
4. For each line item in the list, if `https://www.sakailms.org/spec/lti-ags/v2p0/readOnly` is `true`, only call GET on the item and its `/results` collection (optionally with `?user_id=` to read one learner).
5. Continue to use `lineitem` + `score` scopes only for columns your tool owns.

---

## Other Sakai AGS line item extensions

Sakai also exposes these line item fields (see `SakaiLineItem`):

| Property | Type | Purpose |
|----------|------|---------|
| `https://www.sakailms.org/spec/lti-ags/v2p0/releaseToStudent` | boolean | Maps to gradebook “release to students”. |
| `https://www.sakailms.org/spec/lti-ags/v2p0/includeInComputation` | boolean | Maps to gradebook “include in course grade”. |

These apply to line items the tool owns and can be sent on create/update when the tool has write scope.

---

## Related documentation

- [Using LTI with the Assignments Tool](ASSIGNMENTS.md) — assignment-backed line items and date mapping
- [IMS LTI Assignment and Grade Services 2.0](https://www.imsglobal.org/spec/lti-ags/v2p0)
- [LTI Advantage in Sakai](https://www.tsugi.org/md/ADVANTAGE.md) (Tsugi testing guide)
