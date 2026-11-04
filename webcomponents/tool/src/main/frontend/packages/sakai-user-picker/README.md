# Sakai user picker

`<sakai-user-picker>` searches a tool-provided list of people and other choices.
Sakai pages can load it from `/webcomponents/bundles/user-picker.js`. It uses the
`user-picker` translation bundle.

## Component properties

- `options` accepts an array of choices. Each choice has an `id` and `name`,
  with optional `username`, `group`, and `disabled` fields. Replace the array
  when the choices change. Search matches words in names and usernames in
  either order, ignoring case and accents.
- `values` contains the selected IDs. `multiple` enables multi-selection,
  and `disabled` prevents changes. Set `label` to give the field an
  accessible name and use `placeholder` for an optional prompt.
- User changes emit a bubbling `picker-change` event with `detail.values`.
  Programmatic changes to `values` do not emit the event.

Use the arrow keys to highlight a result, Enter to choose it, and Escape to
close the results. With a nonempty query and no highlight, Enter chooses the
first enabled match. Multiple selection keeps the query and results open.
Remove an individual selection with its named removal button.

## Existing HTML forms

Call `picker.bindSelect(select)` once on a new picker. It inserts the picker
after the native select and reads the choices and disabled state at bind time.
Keep the select in the form with its original name. The picker updates its
submitted values and responds to native change and reset events. The select
remains usable until the picker is ready.
