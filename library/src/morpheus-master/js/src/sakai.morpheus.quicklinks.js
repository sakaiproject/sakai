// * For Quick Links in Morpheus
// */

function quickLinksNavEscHandler(e){
  if (e.keyCode === 27) { // esc keycode
    toggleQuickLinksNav(e);
  }
}

function toggleQuickLinksNav(event){
  event.preventDefault();

  // Hide the user nav panel as necessary for mobile screen display
  if (!$PBJQ('.Mrphs-userNav__subnav').hasClass('is-hidden')) {
    toggleUserNav(event);
  }

  $PBJQ('.Mrphs-quickLinksNav__subnav').toggleClass('is-hidden');

  if (!$PBJQ('.Mrphs-quickLinksNav__subnav').hasClass('is-hidden')) {

    // Add an invisible overlay to allow clicks to close the dropdown
    var overlay = $PBJQ('<div class="quicklinks-dropdown-overlay" />');
    overlay.on('click', function (e) {toggleQuickLinksNav(e)});
    $PBJQ('body').prepend(overlay);

    // Prevent scrolling of the background.
    $PBJQ('body').css('overflow-y', 'hidden');

    // ESC key also closes it
    $PBJQ(document).on('keyup',quickLinksNavEscHandler);

    // Set max height so that scroll bar appears if necessary.
    $PBJQ('.tab-box').css('max-height', window.innerHeight - $PBJQ('#selectQuickLink').offset().top - 14);

  } else {
    $PBJQ('.quicklinks-dropdown-overlay').remove();
    $PBJQ('body').css('overflow-y', 'visible');
    $PBJQ(document).off('keyup',quickLinksNavEscHandler);
  }
}

$PBJQ('#quickLinks-close').on('click', toggleQuickLinksNav);
$PBJQ(".js-toggle-quick-links-nav").on("click", toggleQuickLinksNav);