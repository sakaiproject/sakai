# Sakai LTI Integration in the Assignments Tool

The Sakai Assignments tool supports an **External Tool** / LTI assignment type. This assignment type complements the placement types that have been in Sakai for a long time:

- **Lessons** — Learning App and External Tool
- **Rich Text Editor** placement
- **Left navigation bar** — Site Manage / Manage External Tools

The Assignments placement is a useful addition because none of these other placements fully support coordinated **open**, **due**, and **accept-until (close)** dates. LTI Advantage Deep Linking can carry availability and submission date ranges as well as maximum points. Maximum points appear in many placements, but only Assignments provides a complete date workflow aligned with how instructors expect assignments to behave.

## Creating an LTI Assignment in Sakai

1. Install or edit an LTI tool and indicate that it supports the **Assignment** placement.
2. Open **Assignments** and add a new assignment.
3. Set **Submission Type** to **External Tool (LTI)**.
4. Use **Select External Tool (LTI)** to launch the resource picker.
5. Choose a tool and complete the selection process.

Some tools (for example Tsugi Trophy or LMSTest) may include a configuration step (for example “Configure the LineItem”) as part of the Deep Linking response.

See the IMS Deep Linking specification: [LTI Deep Linking v2.0](http://www.imsglobal.org/spec/lti-dl/v2p0) — search for `lineitem`, `available`, and `submission`.

Tools may suggest:

- `scoreMaximum`
- available date range
- submission date range

These values are returned to Sakai and used to **pre-populate** the Assignments UI. Sakai treats them as **defaults**; the instructor can review and change all values before saving.

After save, a tool can read the final values using LTI Advantage services or via substitution variables at launch (see below).

Once the assignment is saved, it appears in the gradebook with the correct maximum score.

## LTI Date Handling in Sakai Assignments

LTI 1.3 defines two independent date ranges:

- **`ResourceLink.available.*`** — when the activity is accessible
- **`ResourceLink.submission.*`** — when submissions are accepted

Sakai Assignments uses a richer internal model:

| Sakai field | Typical meaning |
|-------------|-----------------|
| **visible date** | When the assignment becomes visible. **Not shown in the Assignments UI by default** (`assignment.visible.date.enabled` is false unless your site turns it on). |
| **open date** | When students may begin work |
| **due date** | Soft deadline / expected completion |
| **close date** | Accept-until (hard cutoff for submission) |
| **resubmission accept-until** | Late resubmission window end (`allowResubmitCloseTime` property) |

Date flow is **bidirectional**:

1. **Inbound (Deep Link → instructor UI)** — tool suggestions pre-fill the assignment editor.
2. **Outbound (assignment → LTI launch)** — current assignment dates are written into substitution variables so tools see the values Sakai is actually using.

### Inbound: Deep Link pre-populates the assignment editor

When the instructor picks an activity via Deep Linking, `returnContentItem` in JavaScript in the Assignment UI maps the tool’s `available` and `submission` blocks onto Sakai’s open, due, and close pickers:

- `available.startDateTime` and `submission.startDateTime` both target **open** (submission is applied second and wins if both are present).
- `available.endDateTime` and `submission.endDateTime` both set **due** and **close** to the same epoch (LTI has no separate soft deadline in the payload).

The instructor can change any value before save. Sakai treats Deep Link dates as **defaults**, not the final source of truth.

### Outbound: four `ResourceLink` substitution parameters at launch

On each external-tool launch, `AssignmentAction` copies the assignment’s current dates into the LTI content item’s `LTI_SETTINGS` JSON. At launch, `SakaiLTIUtil` loads those keys into the substitution map and `LTI13Util.substituteCustom()` resolves tool custom parameters such as `$ResourceLink.submission.endDateTime`.

Implementation: `assignment/tool/.../AssignmentAction.java` (build `content_json`) and `lti/lti-common/.../SakaiLTIUtil.java` (`jsonSubst` loop).

All date values are ISO-8601 strings from `Instant.toString()`.

| Substitution variable | How Sakai sets it |
|----------------------|-------------------|
| `ResourceLink.available.startDateTime` | **Earliest** non-null of **visible date** and **open date** (omitted if both are unset). Visible date is rarely set in practice because the Assignments UI does not expose it by default. |
| `ResourceLink.submission.startDateTime` | Assignment **open date** |
| `ResourceLink.submission.endDateTime` | Assignment **due date** |
| `ResourceLink.available.endDateTime` | **Latest** non-null of **close date**, **due date**, and **resubmission accept-until** (omitted if all are unset) |

Note the **available** dates are generally outside the range of the **submission** dates.

**Why aggregate available dates?** LTI only exposes one start and one end for “availability,” while Sakai can have separate visible, open, due, close, and resubmit-cutoff times. The earliest start and latest end give tools a single window that covers every Sakai constraint.

**Submission vs available:** Submission start/end map directly to open and due so tools that only read the submission range still get the primary student workflow dates. Available start/end add the wider Sakai window (visibility and accept-until / resubmit limits).

Example tool custom configuration:

```text
windowStart=$ResourceLink.available.startDateTime
open=$ResourceLink.submission.startDateTime
due=$ResourceLink.submission.endDateTime
windowEnd=$ResourceLink.available.endDateTime
```

### Sakai-specific substitution parameters

In addition to the four `ResourceLink.*` variables, Sakai exposes each internal date under `Sakai.assignment.*` (constants in `SakaiLTIUtil`). Each is stored only when that assignment field is set:

| Substitution variable | Assignment field |
|----------------------|------------------|
| `Sakai.assignment.visibleDate` | Visible date (omitted when unset; UI field hidden unless `assignment.visible.date.enabled` is true) |
| `Sakai.assignment.openDate` | Open date |
| `Sakai.assignment.dueDate` | Due date |
| `Sakai.assignment.closeDate` | Close (accept-until) date |
| `Sakai.assignment.resubmissionAcceptUntil` | Resubmission accept-until |

Use these when a tool needs Sakai’s native fields separately instead of the aggregated `ResourceLink.available.*` values.

### AGS line item PUT (date sync from tool)

When a tool updates a line item with `PUT` parses the body as a `SakaiLineItem` and calls `LineItemUtil.updateLineItem()`.

For a gradebook row that is the **primary LTI line item** for a linked Sakai external-tool assignment, `applyLineItemToSakaiAssignment()` applies:

| Line item field | Sakai assignment field |
|-----------------|------------------------|
| `startDateTime` | **open date** |
| `endDateTime` | **due date** only |
| `label`, `scoreMaximum` | title, max points (when applicable) |

LTI AGS exposes only one end timestamp. Sakai maps it to **due date** and does **not** change **close date** (accept-until), **visible date** (not editable in the default Assignments UI), or **resubmission accept-until** on PUT. Instructors can therefore keep a later accept-until in Assignments than the tool’s advertised deadline.

After `assignmentService.updateAssignment()`, `syncGradebookColumnTitleAndDueFromSakaiAssignment()` copies the assignment title and due date onto the gradebook column.

If there is no linked assignment row, `endDateTime` updates only the gradebook column’s due date.

**GET / line item responses:** When Sakai builds a line item for a linked assignment (`getLineItem()`), `endDateTime` is the assignment **due date**, or **close date** if due is unset—so tools reading the line item see a single end time that reflects Sakai’s effective deadline, even though PUT writes due only.

Launch substitution variables (above) are refreshed from the full assignment model on the next student launch, not from this PUT path.

See `lti/lti-blis/.../LTI13Servlet.java` and `lti/lti-common/.../LineItemUtil.java`.

## LTI Activity State and Submission Lifecycle

Sakai treats `activityProgress` as a state machine for assignment submission status:

0. **Tool omits `activityProgress`**
   - Sakai defaults missing `activityProgress` to completed/submitted behavior
   - This is intentional compatibility behavior for grade pushes without lifecycle state
   - Tools that need/implement precise lifecycle control must send `activityProgress` on every update

1. **Student submits (submitted/completed state)**
   - Example states: `ACTIVITY_SUBMITTED`, `ACTIVITY_COMPLETED`
   - Sakai sets `submitted = true`
   - If `dateSubmitted` is empty, Sakai sets it to the current time

2. **Instructor requests resubmission (restart/in-progress state)**
   - Example states: `ACTIVITY_INITIALIZED`, `ACTIVITY_STARTED`, `ACTIVITY_INPROGRESS`
   - Sakai clears submission state:
     - `submitted = false`
     - `dateSubmitted = null`

3. **Student submits again after restart**
   - State returns to submitted/completed
   - Because step 2 cleared `dateSubmitted`, Sakai sets a fresh `dateSubmitted` timestamp

4. **Duplicate submitted callbacks without restart**
   - If a submitted/completed callback is repeated without an intervening restart/in-progress state, Sakai preserves the existing `dateSubmitted`
   - This avoids timestamp drift from retries

### Practical implication

- Restart/in-progress transitions intentionally clear the submission timestamp.
- The next real submit records a new submit timestamp.
- Retries of the same submitted state do not keep moving the timestamp.

## Sakai LTI Assignments and Gradebook Integration

LTI assignments integrate with the Sakai gradebook **differently** from other assignment types.

**Non-LTI assignments:** Choosing **Send Grades to Gradebook** creates an externally managed column that is **not** exposed through the LTI grade APIs in the same way.

**LTI assignments:**

- Gradebook columns are created in a form compatible with **LTI Advantage** (Assignments and Grades Service, line items).
- Grades can be updated via LTI.
- Grades can still be overridden directly in the gradebook.

That lets tools retrieve and manage line items in a consistent way.

**Example:** A tool placed in both Assignments and Lessons can retrieve line items for both contexts; Assignments and Grades Service can return the relevant columns for the tool. This avoids introducing another “externally managed only” grade type that would be hard to reconcile with LTI Advantage expectations. Which line items appear together still depends on tool registration and deployment; in common test setups (for example Trophy in Assignments and LMSTest in Lessons on the same Tsugi host), both placements target the same tool server so AGS can list related line items as expected.

**Side effect:** Deleting an assignment **does not** delete the gradebook column. That preserves historical grades but may require manual column cleanup when an assignment is removed on purpose.
