/**
* For Toggling the Minimize and Maximize tools menu in Morpheus: Adds classes to the <body>
*/

function toggleMinimizeNav(){

  var el = $(this);
  var label = $('.accessibility-btn-label' , el);

  el.toggleClass('min max');
    
  if (label.text() == el.data("title-expand")) {
    label.text(el.data("text-original"));
    el.attr('title', (el.data("text-original")));
    el.attr('aria-pressed', true);
  
  } else {
    el.data("text-original", label.text());
    label.text(el.data("title-expand"));
    el.attr('title', (el.data("title-expand")));
    el.attr('aria-pressed', false);
  }

}

$(".js-toggle-nav").on("click", toggleMinimizeNav);



