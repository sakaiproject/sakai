Checklist for adding code to Lessons:

* All strings must be internationalized
* All code must be accessible. Includes any libraries used
* All added code must check for necessary permissions
* Posts must use CSRF token
* Any new items must implement restrictions: this item is required
  and this item must no be shown until prerequisites have been met.
  In a few cases, e.g. inline content, there's no way to check
  whether someone has done the item. In that case "required" won't
  be implemented, but it still won't be shown until prerequisites 
  have been met.
* It must be possible to restrict access to an item by group
* The author's UI must show if an item is restricted to groups or
  if it is unavailable, e.g. because the underlying object hasn't
  been released, etc.
* Make sure you verify that you can add the item using the add content
  icon at the top (which adds it at the end of the page) and also
  the + icons in the middle of the page (which adds it above a 
  current item)
* make sure the item shows properly in the reorder screen and in
  the index of pages if display of individual items is enabled
* make sure the style in which the item is shown matches the
  rest of Lessons. Where possible inherit styles from Morpheus,
  particularly for colors.
* Check markup for all screen widths. Check also when a Lessons
  tool is the second tool on a page. (An Admin will have to edit
  the page to do that.) Note that layout at the top is slightly
  different when it's the second tool, because there's no way 
  lessons can change the header for the tool.

