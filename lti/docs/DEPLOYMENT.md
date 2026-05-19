
Sakai Support for LTI Deployments
=================================

IMS LTI 1.3 includes a concept called `deployment_id` for servers that serve multiple tenants
from the same URL, or for SaaS-style hosts that use a single platform signing key pair
for JWTs across all hosted instances (for example Canvas or Blackboard). In Sakai we use
one key pair per server. We still need to support multiple tenants for LTI tools—law,
engineering, or even a single site when a tool wants different billing for different areas
of the university.

Most tools sell to the "whole university" and in effect ignore the `deployment_id` from Sakai
for authorization decisions. The tools must still receive the `deployment_id` and send it
on certain API calls; in those situations the tool typically accepts whatever value the LMS
sends and echoes it as required.

Treat `deployment_id` values as alphanumeric slugs: ASCII letters, ASCII digits, hyphens, and underscores. If Sakai
sees other characters, it removes them at launch time so the protocol does not fail. For
example, `"Hello 🌴 123"` becomes `"Hello123"` because the emoji and spaces are not allowed.

Multiple Deployments in a Sakai Instance
----------------------------------------

Some tools license by seat count; different parts of the university pay separately and do
not want to share a seat pool. Those cases are addressed by assigning different
`deployment_id` values to different sites or (more commonly) groups of sites.

**Operational note:** If two unrelated orgs end up with the same `deployment_id` after mapping
(for example the same `colDiv` value, or the same string after stripping), their seat pools can
merge from the tool's perspective. Uniqueness across the intended partition is an operational
responsibility unless the product later adds detection or warnings.

When a tool is installed (registered), `deployment_id` is generally set to `"1"` because the
field is required from the first step of tool creation, including LTI Dynamic Registration.
By convention (Moodle uses a similar approach) the default for new registrations is
`deployment_id` `1`. The server default can be overridden in `sakai.properties`:

    # DEFAULT: "1"
    # lti13.deployment_id=1

Overriding this default is uncommon. Per LTI 1.3, the LMS is allowed and expected to mint this value.

When a tool is created or edited, the default `deployment_id` used for launches from that tool
can be changed. Often it is left blank or null so the tool inherits the default `"1"`.

Per-Site Deployment Id
----------------------

System and tool defaults for `deployment_id` are coarse-grained. The finest grain is to give
every site its own `deployment_id` by setting the `lti13.deployment_id` property on the site:

    lti13.deployment_id=42

That is somewhat inconvenient to set by hand. Usually it is set by the process that creates
sites (semesters, schools, departments, courses). It might also be populated as part of
Delegated Access setup, or set manually on a site for testing.

Per-tool deployment when manually deployed to sites
-----------------------------------------------------

Some tools are registered with **manual deployment**: they are not visible everywhere; an
administrator deploys them only to selected sites. In the LTI admin tool, **Tool in site**
deployment screens let you add many sites at once and, for each tool–site link, optionally set
a **deployment group** (letters and digits). That value is stored on the `lti_tool_site` row and,
at launch time, contributes to the LTI 1.3 `deployment_id` sent to the tool when it wins in the
precedence chain described below (after an explicit site `lti13.deployment_id` and before mapped
site properties). Leaving it blank is fine; launches then fall through to the next sources in
that chain.

Typical use: bulk-deploy one registered tool to a long list of sites and give each site (or batch
of sites) its own deployment identifier so the external tool can separate tenants or billing.

Launch-time resolution (first match wins)
-----------------------------------------

At each launch, Sakai resolves `deployment_id` in this order:

| Step | Source |
|------|--------|
| 1 | Site property `lti13.deployment_id`, if set and non-blank after normalization |
| 2 | `lti_tool_site.deployment_group` for this LTI tool and launch site, when a matching tool-site row exists and the value is non-blank after normalization (see *Tool in site* / deployment UI in the LTI admin tool) |
| 3 | First site property named in `lti13.deployment_id.site.properties` (comma-separated list), in order, that exists and is non-blank after normalization |
| 4 | Tool-level default (`lti_tools.deployment_id`), if configured and non-blank after normalization |
| 5 | Server default from `lti13.deployment_id` in `sakai.properties` (typically `"1"`) |

Step 2 applies only when the launch has a site id and tool id so Sakai can look up `lti_tool_site`.
It ranks **below** the explicit site property in step 1 and **above** the generic site-property
list in step 3, so a course-wide `lti13.deployment_id` on the site still wins over a per-tool
deployment group, while the deployment group still wins over mapped org properties such as
`School` or `colDiv`.

Normalization is applied before comparing or sending values: only ASCII letters, digits,
hyphen (`-`), and underscore (`_`) are kept after trim; other characters are removed. The first
non-blank normalized value in the chain is used.

Automatic Unit-Based Deployment Id Generation
---------------------------------------------

Many course-site processes already set site properties from the org structure where the
course lives. For example, the default course provider on Sakai's nightly servers sets:

    School      MUSIC
    Department  Blues

Another common pattern:

    colDiv      ENGR
    school      CompSci

These strings are suitable as `deployment_id` values once any disallowed characters are
stripped. Such properties often partition course sites naturally, so they are good candidates
to drive `deployment_id` for launches from a site.  Case matters in these property names and
values.

To use one or more site properties this way, configure a comma-separated list in
`sakai.properties`:

    lti13.deployment_id.site.properties=School

Or, for a different local convention:

    lti13.deployment_id.site.properties=colDiv

Multiple names are allowed; order is the priority order among those properties (after the
explicit site `lti13.deployment_id` and after `lti_tool_site.deployment_group`, per the table above):

    lti13.deployment_id.site.properties=colDiv,school

Each institution knows its local convention for organizational site properties and can tune
`sakai.properties` to get the desired result.

**Scope:** Steps 1, 3, and 5 are shared by every tool launched from a site. Step 2 is **per tool
and site** (`lti_tool_site`), so two tools in the same course can still send different
`deployment_id` values when each has its own tool-site deployment group. Step 4 is the
tool’s own default from `lti_tools`.

Summary
-------

While this might sound a bit complex, one simplifying factor is that most tools are installed
instance-wide (or linked broadly across sites), so the tool default of `"1"` is perfectly
sufficient because those tools do not gate launches on `deployment_id`.

Tools that do gate launches (and billing) on `deployment_id` generally read the value from
the launch and check whether it is already known. If there is an existing mapping from
`deployment_id` to a billing tenant, the launch proceeds. If the `deployment_id` is new, the
tool usually runs a flow to register that `deployment_id` and associate it with a tenant or
client. Until that flow completes, the first launch from a new `deployment_id` may be
blocked, limited, or redirected by the tool; behavior is tool-specific, so admins should not
assume a brand-new deployment always results in a fully working first click.

So while some situations need very tight control, as long as the partition reflects the
organizational structure reasonably well, things should work without too much manual
configuration.

For manual QA aligned with this document, see [DEPLOYMENT-TEST-PLAN.md](./DEPLOYMENT-TEST-PLAN.md).
