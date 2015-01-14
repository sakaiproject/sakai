/**
 * Alter links so that we can handler dispatching ourselves
 */
function spiffyUp() {
  $('#page').find('a').attr('onclick', 'portalClick(this);');
}

/**
 * Handle link dispatching such that links just change the url of the current iframe
 * insted of opening in a popup like things cleaned with FormattedText want to do
 */
function portalClick(link) {
  if ($(link).attr('target') === '_blank') {
    $(link).removeAttr('target');
  }
}
