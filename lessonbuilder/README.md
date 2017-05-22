Checklist for adding code to Lessons:

* All strings must be internationalized
* All code must be accessible. Includes any libraries used
* All added code must check for necessary permissions
* POSTs must send and check the CSRF token
* Any new items must implement restrictions: "this item is required"
  and "this item must not be shown until prerequisites have been met".
  In a few cases, e.g. inline content, there's no way to check
  whether someone has done the item. In that case "required" won't
  be implemented, but it still won't be shown until prerequisites 
  have been met.
* It must be possible to restrict access to an item by group
* The author's UI must show if an item is restricted to groups or
  if it is unavailable, e.g. because the underlying object hasn't
  been released, etc. That's normally done by a message in brackets
  after the title.
* Make sure you verify that you can add the item using the add content
  icon at the top (which adds it at the end of the page) and also
  the + icons in the middle of the page (which adds it above a 
  current item)
* Make sure the item shows properly in the reorder screen and in
  the index of pages if display of individual items is enabled
* Make sure the style in which the item is shown matches the
  rest of Lessons. Where possible inherit styles from Morpheus,
  particularly for colors.
* Check markup for all screen widths. Check also when a Lessons
  tool is the second tool on a page. (An Admin will have to edit
  the page to do that.) Note that layout at the top is slightly
  different when it's the second tool, because there's no way 
  lessons can change the header for the tool.
* Make sure that the backend code that adds or changes items 
  does a single hibernate operation: save, update or saveOrUpdate.
  See LSNBLDR-722 for a full explanation. Old code often created
  an item and then fixed up fields. That can cause trouble.
* Code that plays with sequence numbers will need to call setRefreshMode
  at the beginning. It must be called before any items on the page 
  have been loaded into the Session.

For new code I encourage doing something that isn't in the original code 
but should have been:

* The popups used to create an item and to edit an existing item should
be the same. That way all options can be set when creating an item.

If anyone has time, I'd love to see the existing popups fixed to be that way.
