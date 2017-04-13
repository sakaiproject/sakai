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

    // ESC key also closes it
    $PBJQ(document).on('keyup',quickLinksNavEscHandler);

  } else {
    $PBJQ('.quicklinks-dropdown-overlay').remove();
    $PBJQ(document).off('keyup',quickLinksNavEscHandler);
  }
}

$PBJQ('#quickLinks-close').on('click', toggleQuickLinksNav);
$PBJQ(".js-toggle-quick-links-nav").on("click", toggleQuickLinksNav);