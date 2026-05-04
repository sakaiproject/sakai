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

After save, a tool can read the final values using LTI Advantage services or via substitution variables:

```text
ResourceLink.available.startDateTime
ResourceLink.available.endDateTime
ResourceLink.submission.startDateTime
ResourceLink.submission.endDateTime
```

Once the assignment is saved, it appears in the gradebook with the correct maximum score.

## LTI Date Handling in Sakai Assignments

LTI 1.3 defines two independent date ranges:

- **`ResourceLink.available.*`** — when the activity is accessible
- **`ResourceLink.submission.*`** — when submissions are accepted

Sakai Assignments uses a three-part model:

**open date → due date → accept-until (close) date**

LTI does not explicitly define a separate “due date” (soft deadline). In practice, many LTI tools (including Turnitin) treat **`ResourceLink.submission.endDateTime`** as the assignment due date.

To keep behavior predictable and interoperable, Sakai applies this mapping when processing LTI date values:

**If `ResourceLink.submission.endDateTime` is present:**

- due date = `submission.endDateTime`
- close (accept-until) date = `submission.endDateTime`

**Else if `ResourceLink.available.endDateTime` is present:**

- due date = `available.endDateTime`
- close (accept-until) date = `available.endDateTime`

### Design rules

| Rule | Meaning |
|------|--------|
| **`submission.endDateTime` is authoritative** | When present, it defines the assignment deadline and is used for **both** due date and accept-until date. |
| **`available.endDateTime` is fallback only** | Used only when `submission.endDateTime` is not provided; it must **never** override `submission.endDateTime`. |
| **`submission.startDateTime` overrides `available.startDateTime` for open** | When both are present, the assignment **open** date follows `ResourceLink.submission.startDateTime`; `ResourceLink.available.startDateTime` does not win for open in that case (same precedence idea as end dates). |
| **Due date and close date may be identical** | LTI does not distinguish a soft due date from a hard cutoff; Sakai may collapse these to one value. |

### Rationale

- Many tools interpret `submission.endDateTime` as the due date.
- LTI does not standardize a late submission window.
- Letting `available.endDateTime` override `submission.endDateTime` would produce incorrect deadlines for those tools.
- A single authoritative rule reduces long-term confusion and support load.

### Practical effect

- `submission.endDateTime` → Sakai **due** date (authoritative)
- `available.endDateTime` → fallback **due** / **close** date
- `available.startDateTime` → Sakai **open** date (fallback when `submission.startDateTime` is absent)
- `submission.startDateTime` → Sakai **open** date when present; **`submission.startDateTime` overrides `available.startDateTime`** when both are present

**Where this is applied in the UI:** In `assignment/tool/src/webapp/vm/assignment/chef_assignments_instructor_new_edit_assignment.vm`, the External Tool deep-link callback `returnContentItem` assigns both `ResourceLink.available.startDateTime` and `ResourceLink.submission.startDateTime` to the assignment open-date input **`#opendate`** in that order, so the submission value wins if both are returned.

This favors consistency with real-world LTI tool behavior over a strict, spec-only reading that would disagree with common implementations.

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
