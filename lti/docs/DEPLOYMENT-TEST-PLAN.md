# Manual test plan: LTI `deployment_id` and deployments

This plan exercises the behavior described in [DEPLOYMENT.md](./DEPLOYMENT.md): normalization, server and tool defaults, site properties, `lti_tool_site.deployment_group` (Tool in site / bulk deploy), and launch-time precedence.

Style note: each part lists **roles**, **steps**, and **Expected** outcomes. Adjust site property names (`School`, `colDiv`, `unit`, etc.) to match your institution’s course-creation templates.

---

## Global prerequisites (any part that asserts `deployment_id`)

- An **LTI 1.3** external tool that exposes launch claims (JWT / message debugger), e.g. a Tsugi “LMS Test”–style tool, IMS reference consumer, or vendor “show claims” placement. Without this, many checks are indirect.
- Ability to set **site properties** (Site Info → Manage Access → Edit Properties, or admin equivalent).
- Ability to change **`sakai.properties`** and restart for keys configured there (including the **server default** `lti13.deployment_id` and `lti13.deployment_id.site.properties`). Set **per-site** `lti13.deployment_id` via site properties, not `sakai.properties`. Prefer a **non-production** node. Parts whose title includes **(dev test)** require that kind of server config change and restart before the steps apply.
- **Admin** access to **LTI Admin** for tool registration, visibility, and **Tool in site** / bulk deploy flows.
- At least **two course sites** (Site A, Site B) and **two LTI 1.3 tools** (Tool 1, Tool 2) where the product allows manual deployment and deployment groups.

### How to record results

- Capture the tool-reported **`https://purl.imsglobal.org/spec/lti/claim/deployment_id`** (or equivalent) for each launch.
- Note **role** used to launch if paths differ (admin vs instructor vs student).

---

## Part 1: Baseline defaults (server and tool)

**Goal:** Default `deployment_id` behavior and tool-level override (**precedence Step 4–5** in DEPLOYMENT.md).

### Setup

- `lti13.deployment_id` unset or explicitly `1` in `sakai.properties` (documented default).
- An LTI 1.3 tool that can be launched from a course without extra site configuration.

### Steps

1. As **Sakai admin**, open an LTI 1.3 tool in LTI Admin and confirm the creation / registration path exposes **Deployment ID** as required on first setup (including Dynamic Registration, if used).
2. Leave tool-level deployment id **blank** - the UI should allow this, save.
3. As **instructor**, add the tool to a course and **launch** it; record `deployment_id`.
4. Set tool-level **Deployment ID** to a distinct allowed value (e.g. `tooldefault42`), save, launch again.

### Expected

- With blank tool field and no higher-precedence sources, resolved `deployment_id` matches **server default** (typically **`1`**).
- With `tooldefault42` set and no Step 1–3 sources, launch shows **`tooldefault42`** (after normalization if applicable).
- Changing tool-level value changes the claim on the next launch (note any caching oddities).

---

## Part 2: Normalization (trim + character stripping)

**Goal:** Only ASCII letters, digits, hyphen, underscore survive after trim; other characters removed (see DEPLOYMENT.md example).

### Steps

1. As **admin**, set tool-level **Deployment ID** to `Hello 🌴 123` (spaces + non-ASCII).
2. As **instructor**, launch and read resolved `deployment_id`.
3. Repeat with: leading/trailing spaces; strings containing only disallowed characters so normalization yields **blank**—confirm resolver **falls through** to the next chain step (e.g. server default), not a broken launch—unless the tool rejects the outcome (tool-specific).

### Expected

- `Hello 🌴 123` normalizes to **`Hello123`** (emoji and spaces removed per product rules).
- Hyphens and underscores preserved when allowed.
- Blank after normalization falls through per precedence chain; launch still completes or fails only per tool policy.

---

## Part 3: Server default override (`lti13.deployment_id` in `sakai.properties`) (dev test)

**Goal:** **Step 5** — last-resort server default when Steps 1–4 do not supply a value.

### Setup

- On test site: no `lti13.deployment_id` site property (or empty).
- No `lti_tool_site.deployment_group` for this tool/site (or use a fresh tool/site link).
- `lti13.deployment_id.site.properties` unset or mapped props empty on the site.

### Steps

1. Set `lti13.deployment_id=server77` in `sakai.properties`, restart Sakai.
2. Launch the tool with no tool-level id (or blank), no site property, no deployment group, no mapped props.

### Expected

- Launch `deployment_id` resolves to **`server77`** (normalized if needed).

### Cleanup

- Restore or remove `lti13.deployment_id` override and restart.

---

## Part 4: Per-site explicit `lti13.deployment_id` (site property)

**Goal:** **Step 1** wins over deployment group, mapped properties, tool default, and server default.

### Setup

- Tool-level deployment id set to something identifiable (e.g. `toolZZ`).
- **Tool in site** deployment group set for the same tool/site (e.g. `groupYY`) — see Part 5 if not already present.
- Configure `lti13.deployment_id.site.properties` so a mapped property would win if Step 1 were absent (optional, for stronger proof) **(dev test)** — requires `sakai.properties` edit and restart.

### Steps

1. On **Site A**, add site property **`lti13.deployment_id`** = `siteexplicit42`.
2. Launch the tool from **Site A**.

### Expected

- Resolved `deployment_id` is **`siteexplicit42`** (normalized), not `groupYY`, not mapped org value, not `toolZZ`, not server default alone.

---

## Part 5: `lti_tool_site.deployment_group` (manual “Tool in site” bulk deploy)

**Goal:** **Step 2** — per tool and launch site; applies when non-blank after normalization.

### Setup

- Use a tool configured for **manual / stealthed** deployment (not visible everywhere without deploy).
- As **admin**, in LTI Admin use **Tool in site** (or equivalent) to deploy the tool to **Site A** with **deployment group** `batch01` (letters and digits per UI).

### Steps

1. Ensure Site A has **no** site property `lti13.deployment_id` (or remove it).
2. Ensure Step 3 mapped properties do not supply a value **or** temporarily clear them so Step 2 is visible.
3. Launch from **Site A**.

### Expected

- Launch shows **`batch01`** (normalized) as `deployment_id`.

### Negative / UI

- If the UI restricts deployment group to alphanumeric, attempt invalid input and confirm validation (no silent persistence of invalid values).

---

## Part 6: Precedence — deployment group vs mapped site properties (Step 2 vs Step 3) (dev test)

**Goal:** **Step 2** ranks **above** the comma-separated site property list (**Step 3**).

### Setup

- On **Site A**: set `School=EDUCATION` or `colDiv=ENGR` (match your environment).
- In `sakai.properties`: `lti13.deployment_id.site.properties=School` (or `colDiv`).
- **Do not** set site property `lti13.deployment_id`.
- Set **deployment group** for this tool/site to `groupAA`.

### Steps

1. Launch the tool from Site A.

### Expected

- Resolved value is **`groupAA`**, not `EDUCATION` / `ENGR`.

---

## Part 7: Mapped site properties — list order (Step 3) (dev test)

**Goal:** First named property in `lti13.deployment_id.site.properties` that exists and normalizes non-blank wins among Step 3 candidates.

### Setup

- Remove site `lti13.deployment_id`.
- Remove or blank **deployment group** for this tool/site so Step 2 does not apply.
- On site: set `colDiv=ENGR` and `unit=SCI` (or two properties you control).
- `sakai.properties`: `lti13.deployment_id.site.properties=colDiv,unit`.

### Steps

1. Launch; record `deployment_id`.
2. Remove or blank `colDiv` on the site, keep `unit`, launch again.

### Expected

- First launch: **`ENGR`** (or normalized).
- After `colDiv` cleared: **`SCI`**.

### Variant

- Change list to `unit,colDiv` and repeat; **priority follows list order**, not alphabetical property names.

---

## Part 8: Two tools, same site, different deployment groups

**Goal:** Scope note in DEPLOYMENT.md — Step 2 is **per tool and site**; two tools can send different `deployment_id` values from the same course.

### Setup

- Manually deploy **Tool 1** and **Tool 2** to **Site A** with deployment groups `tg111` and `tg222` respectively.
- No site `lti13.deployment_id`; Step 3 sources cleared or subordinate as needed.

### Steps

1. Launch **Tool 1** from Site A; record claim.
2. Launch **Tool 2** from Site A; record claim.

### Expected

- **`tg111`** vs **`tg222`** — two different values from the same site.

---

## Part 9: Dynamic registration and tool configuration `deployment_id`

**Goal:** Registration path sets required `deployment_id` (convention **`1`**); Sakai persists tool configuration JSON consistently.

### Setup

- LTI 1.3 **Dynamic Registration** available to a test tool.

### Steps

1. Complete dynamic registration for a new tool.
2. In LTI Admin, open the tool’s stored registration / auto-registration JSON (where the product surfaces IMS tool configuration).

### Expected

- Tool configuration includes **`deployment_id`** consistent with product default (typically **`1`**) unless overrides apply at registration time.

---

## Part 10: End-to-end “partition” smoke (site props + pilot override) (dev test)

**Goal:** Typical rollout: most courses from org props; one site with explicit override.

### Steps

1. In `sakai.properties`, set `lti13.deployment_id.site.properties=School` (or the **site property name** your course templates populate, e.g. `colDiv`). This names **per-site** properties Sakai reads for Step 3—it does not inject a deployment id server-wide; template sites must carry that named property on each site.
2. Ensure several sites created from your template have a non-blank **`School`** **site property** (matching the name from step 1).
3. Set the site property `lti13.deployment_id=pilot99` on the pilot site (do not change server-wide sakai.properties).
4. Launch the same tool from a “template” site and from the pilot site.

### Expected

- Template sites: value from the mapped **site** property (e.g. **`School`**, steps 1–2).
- Pilot site: **`pilot99`** from the explicit site property `lti13.deployment_id` (step 3).

---

## Part 11: Regression — LTI with Assignments / Lessons / Gradebook

**Goal:** Rudimentary regression after changes to deployment resolution or LTI line-item paths.

### Suggestions

- **Assignments**: external tool submission type; create, launch, return grade or line item update; **Edit** assignment and confirm gradebook association remains (similar rigor to integration smoke tests elsewhere).
- **Lessons**: LTI link launch if used in your deployment.
- **Advantage (AGS/NRPS)** if enabled: confirm tokens/launches remain consistent for the same site after changing deployment sources (tool-dependent logging).

### Expected

- No loss of placement, launch, or gradebook linkage attributable to deployment configuration changes under test.

---

## Traceability (doc → parts)

| DEPLOYMENT.md topic | Parts |
|---------------------|-------|
| Normalization | 2 |
| Server default `lti13.deployment_id` | 1, 3 |
| Site `lti13.deployment_id` | 4 |
| `lti_tool_site.deployment_group` | 5, 6, 8 |
| `lti13.deployment_id.site.properties` | 7 |
| Precedence table (Steps 1–5) | 4–7 |
| Per-tool scope (Step 2) | 8 |
| Dynamic registration default | 9 |
| End-to-end partition smoke | 10 |
| Assignments / Lessons / Gradebook regression | 11 |
| Typical “most tools use 1” | 1 (smoke) |


