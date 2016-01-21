/**
* For toggling the Minimize and Maximize tools menu in Morpheus: Adds classes to the <body> and changes the label text for accessibility
*/

function toggleMinimizeNav(){

  $PBJQ('body').toggleClass('Mrphs-toolMenu-collapsed');

  var el = $PBJQ(this);
  var label = $PBJQ('.accessibility-btn-label' , el);

  el.toggleClass('min max');

  if (label.text() == el.data("title-expand")) {
    label.text(el.data("text-original"));
    el.attr('title', (el.data("text-original")));
    el.attr('aria-pressed', true);
    document.cookie = "sakai_nav_minimized=false; path=/";

  } else {

    el.data("text-original", label.text());
    label.text(el.data("title-expand"));
    el.attr('title', (el.data("title-expand")));
    el.attr('aria-pressed', false);
    document.cookie = "sakai_nav_minimized=true; path=/";

  }
}

$PBJQ(".js-toggle-nav").on("click", toggleMinimizeNav);