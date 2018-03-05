/**
 * For Responsive Menus in Morpheus: Adds classes to the <body>
 */

function toggleToolsNav(event){
  if (event) {
    event.preventDefault();
  }
    
  $PBJQ('body').toggleClass('toolsNav--displayed');
  if ($PBJQ('body').hasClass('toolsNav--displayed')) {
    /* Add the mask to grey out the top headers - re-use code in more.sites.js */
    createDHTMLMask(toggleToolsNav)
  }else{
    removeDHTMLMask();
  }
}

$PBJQ(document).ready(function(){


  function setupRoleSwitcherAsMenu() {
    function closeTheRoleSwitchToggle() {
        if ($PBJQ('#roleSwitchDropDown').is('.open')) {
            $PBJQ('#roleSwitchDropDownToggle').trigger('click');
        }
    };

    function handleKeyUp(event) {
        if (event.keyCode == 27) {
            closeTheRoleSwitchToggle();
            return false;
        }
        return true;
    };

    // Setup the initial ARIA attributes
    $PBJQ('#roleSwitchDropDownToggle').attr('aria-hidden', 'false');
    $PBJQ('#roleSwitchDropDown').attr('aria-hidden', 'true').attr('aria-label', 'submenu');

    $PBJQ('#roleSwitchDropDownToggle').click( function(){
      $PBJQ('#roleSwitchDropDown').css('right', $PBJQ(window).width() - 20 - ($PBJQ('#roleSwitchDropDownToggle').offset().left + $PBJQ('#roleSwitchDropDownToggle').width()));
      $PBJQ('#roleSwitchDropDown').toggleClass('open');
      if ($PBJQ('#roleSwitchDropDown').is('.open')) {
          $PBJQ('#roleSwitchDropDown').attr('aria-hidden', 'false');
          $PBJQ(document.body).prepend('<div class="user-dropdown-overlay"></div>');
          $PBJQ('.user-dropdown-overlay').on('click', function() {
              closeTheRoleSwitchToggle();
          });
          $PBJQ(document.body).on('keyup', handleKeyUp);
          setTimeout(function() {
              $PBJQ('#roleSwitchDropDown').find('a, :input').focus();
          });
      } else {
         $PBJQ('#roleSwitchDropDown').attr('aria-hidden', 'true');
         $PBJQ('.user-dropdown-overlay').remove();
         $PBJQ(document.body).off('keyup', handleKeyUp);
         $PBJQ('#roleSwitchDropDownToggle').focus();
      }
    });
  };

  $PBJQ('#roleSwitchSelect').on("change", function(){
    if( $PBJQ('option:selected', this ).text() !== '' ){
      document.location = $PBJQ('option:selected', this ).val();
    }else{
      $PBJQ(this)[0].selectedIndex = 0;
    }
  });

  if(MorpheusViewportHelper.isPhone()) {
    setupRoleSwitcherAsMenu();
  } else {
    // if the menu has not be setup, then don't show the toggle if the
    // page is resized to the mobile viewport size
    $PBJQ('#roleSwitch').addClass('menu-not-setup');
  }

});

$PBJQ(".js-toggle-tools-nav", "#skipNav").on("click", toggleToolsNav);
