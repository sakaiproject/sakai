# Sakai tag selector

A Lit replacement for the shared Vue selector used by Assignments, Messages and
Samigo question pools. It uses Sakai's `tag-selector` translations and is included
in the standard `base.js` bundle.

```html
<sakai-tag-selector site-id="site" tool="samigo" collection-id="owner"
    selected-temp="existing-tag-id" input-id="pool-tags" add-new="true">
</sakai-tag-selector>
<input type="hidden" id="pool-tags" name="tags" value="existing-tag-id">
```

- `site-id`, `tool`, and `collection-id` select the existing Tags API endpoint.
- `selected-temp` restores comma-separated IDs or unsaved labels. If empty,
  `item-id` loads existing associations when `add-new` is enabled.
- `extra-options` adds comma-separated filter choices, such as assignment groups.
- `add-new="false"` limits selection to available choices. New labels exclude
  commas because the existing form contract uses comma-separated values.
- `input-id` identifies the host form's existing hidden field. Initialization and
  selection changes update its value directly; failed requests leave it intact.
- `tags-changed` emits `{ value: [{ name, code }] }`. `selectedTags` returns the
  current selection; `clear()` clears it and updates the form field.

The picker supports arrow-key navigation, Enter selection/creation, Escape to
close the options, and named buttons to remove selected tags. Labels are rendered
as text. There is no global initialization helper or Vue dependency.
