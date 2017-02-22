/**
 * ESC handler to dismiss user nav
 */

function userNavEscHandler(e){
  if (e.keyCode === 27) { // esc keycode
    toggleUserNav(e);
  }
}

/**
 * Toggle user nav in header: 
 */

function toggleUserNav(event){
  event.preventDefault();
  $PBJQ('.Mrphs-userNav__subnav').toggleClass('is-hidden');

  if (!$PBJQ('.Mrphs-userNav__subnav').hasClass('is-hidden')) {
    // Add an invisible overlay to allow clicks to close the dropdown

    var overlay = $PBJQ('<div class="user-dropdown-overlay" />');
    overlay.on('click', function (e) {toggleUserNav(e)});

    $PBJQ('body').prepend(overlay);

    // ESC key also closes it
    $PBJQ(document).on('keyup',userNavEscHandler);

  } else {
    $PBJQ('.user-dropdown-overlay').remove();
    $PBJQ(document).off('keyup',userNavEscHandler);    
  }
}

 // Logout Confirm
  $PBJQ('#loginLink1').click(function(e){
    if ($PBJQ(this).attr("data-warning") !== "" && !confirm($PBJQ(this).attr("data-warning"))){
	e.preventDefault();
    }
  });


$PBJQ(".js-toggle-user-nav a#loginUser > .Mrphs-userNav__drop-btn", "#loginLinks").on("click", toggleUserNav);
$PBJQ(".js-toggle-user-nav .Mrphs-userNav__drop-btn", "#loginLinks").on("click", toggleUserNav);

$PBJQ('.Mrphs-userNav__pic-changer').on("click", function (event) {

    var $profileLink = $PBJQ(this);

    if (!window.FileReader) {
        // we need FileReader support to load the image for croppie
        // when browser doesn't support it, then fallback to old upload method
        $PBJQ($profileLink.data("profileLink")).trigger("click");
        return true;
    }

    if (!$PBJQ.fn.modal) {
        // we need Bootstrap
        // when not loaded fallback to the old upload method
        $PBJQ($profileLink.data("profileLink")).trigger("click");
        return true;
    }

    event.preventDefault();
    event.stopImmediatePropagation();

    function resetProfileImage() {

        $modal.find(".modal-body .alert").hide();

        $PBJQ.ajax("/direct/profile-image/remove", {
            data: {
                sakai_csrf_token: sakai_csrf_token
            },
            type: 'POST',
            dataType: 'json',
            success: function (data, textStatus, jqXHR) {

                if (data.status == "SUCCESS") {
                    refreshProfileImageTagsAndHideDialog();
                    $('#cropToolbar').hide();
                } else {
                    $PBJQ('#remove-error').show();
                }
            }
        });
    }

    function initCropWidget() {

        $cropWidget.find("> img").cropper({
            aspectRatio: 1 / 1,
            checkCrossOrigin: false,
            guides: true,
            minContainerWidth: 300,
            minContainerHeight: 300,
            autoCropArea: 1,
            viewMode: 1,
            dragMode: 'move'
        });
    }

    function initCropWidgetToolBar() {

        var $toolbar = $('#cropToolbar').show();

        if (!portal.pictureToolbarSetup) {
            $('.profile-image-zoom-in', $toolbar).on('click', function() {
                $cropWidget.find('> img').cropper('zoom', 0.1);
            });
            $('.profile-image-zoom-out', $toolbar).on('click', function() {
                $cropWidget.find('> img').cropper('zoom', -0.1);
            });
            $('.profile-image-pan-up', $toolbar).on('click', function() {
                $cropWidget.find('> img').cropper('move', 0, -10);
            });
            $('.profile-image-pan-down', $toolbar).on('click', function() {
                $cropWidget.find('> img').cropper('move', 0, 10);
            });
            $('.profile-image-pan-left', $toolbar).on('click', function() {
                $cropWidget.find('> img').cropper('move', 10, 0);
            });
            $('.profile-image-pan-right', $toolbar).on('click', function() {
                $cropWidget.find('> img').cropper('move', -10, 0);
            });
            $('.profile-image-rotate', $toolbar).on('click', function() {
                $cropWidget.find('> img').cropper('clear');
                $cropWidget.find('> img').cropper('rotate', 90);
                $cropWidget.find('> img').cropper('crop');
            });
            portal.pictureToolbarSetup = true;
        }
    }

    function setCropWidgetURL(url) {

        $cropWidget.show();
        $cropWidget.find("> img").cropper('clear');
        $cropWidget.find("> img").cropper('replace', url);
        $save.removeProp("disabled");

        initCropWidgetToolBar();
    }

    function loadExistingProfileImage() {

        $PBJQ.getJSON("/direct/profile-image/details?_=" + new Date().getTime(), function (json) {

            if (json.status == "SUCCESS") {
                if (!json.isDefault) {
                    setCropWidgetURL(json.url + "?_=" + new Date().getTime());

                    $PBJQ('.remove-profile-image').on("click", function () {
                        resetProfileImage();
                    });
                }
            }
            sakai_csrf_token = json.csrf_token;
        });
    };

    function getCropWidgetResultAsPromise() {

        return new Promise(function(resolve) {
            resolve($cropWidget.find("> img").cropper('getCroppedCanvas', {width: 200, height: 200}).toDataURL());
        });
    }

    function refreshProfileImageTagsAndHideDialog() {

        var picLink = $PBJQ('#loginUser > .Mrphs-userNav__submenuitem--profilepicture');
        var parent = picLink.parent();
        picLink.detach();
        var d = new Date();
        var style = 'background-image: url(/direct/profile/' + portal.user.id + '/image/thumb?' + d.getTime() + ')';
        picLink.attr('style', style);
        parent.prepend(picLink);

        picLink = $PBJQ('.Mrphs-userNav__submenuitem--profilelink > .Mrphs-userNav__submenuitem--profilepicture');
        parent = picLink.parent();
        picLink.detach();
        picLink.attr('style', style);
        parent.prepend(picLink);

        $('#profileImageUpload').modal('hide');
    }

    function uploadProfileImage(imageByteSrc) {

        $modal.find(".modal-body .alert").hide();

        $PBJQ.ajax("/direct/profile-image/upload", {
            data: {
                sakai_csrf_token: sakai_csrf_token,
                base64: imageByteSrc
            },
            type: 'POST',
            dataType: 'json',
            success: function(data, textStatus, jqXHR) {

                if (data.status == "SUCCESS") {
                    refreshProfileImageTagsAndHideDialog();
                } else {
                    $PBJQ('#upload-error').show();
                }
            }
        });
    }

    // show popup!
    var $modal = $PBJQ('#profileImageUpload');
    var modalVisible = false;
    $modal.on("shown.bs.modal", function() {
        loadExistingProfileImage();
    });
    $modal.modal({
        width: 320
    });

    $PBJQ('#remove-error').hide();
    $PBJQ('#upload-error').hide();

    var $save = $modal.find("#save");

    var $fileUpload = $PBJQ('#file');

    var $cropWidget = $PBJQ('#cropme').hide();

    initCropWidget();

    $fileUpload.on("change", function () {

        var $this = $PBJQ(this);
        if (this.files && this.files[0]) {
            var reader = new FileReader();
            reader.onload = function (e) {
                setCropWidgetURL(e.target.result);
            };

            reader.readAsDataURL(this.files[0]);
        } else {
            throw "Browser does not support FileReader";
        }
    });

    $save.on('click', function (ev) {

        getCropWidgetResultAsPromise().then(function (src) {
            uploadProfileImage(src.replace(/^data:image\/(png|jpg);base64,/, ''));
        });
    });

    return false;
});

var header = $(".Mrphs-topHeader");
var currentHeaderWidth = -1;
var mainHeaderSize  = $(header).height();

$PBJQ(document).ready( function(){

  $(header).data("sticked",false);

  if( $PBJQ('.Mrphs-hierarchy--parent-sites').length > 0 && $PBJQ(window).width() <= 800 ){
    $PBJQ('#content').css( 'margin-top', ( parseInt( $PBJQ('#content').css('margin-top').replace('px', '') ) +  $PBJQ('.Mrphs-hierarchy--parent-sites').outerHeight(true) ) + 'px' );
  }
 
  $PBJQ(window).resize(function() {
	  currentHeaderWidth = $(".Mrphs-mainHeader").width();
	  $(header).css('height', 'auto');
	  mainHeaderSize = $(header).height();
  });
 
  $PBJQ(window).scroll(function(){
	if(currentHeaderWidth > 799) {
		var size = 0;
		var stick = (($(document).height() - $(window).height()) > $(header).height()) === true;
		if($(window).scrollTop() > 0) {
		  if($(header).data("sticked") === false && stick === true) {
		    $(header).data("sticked",true);
			$(".Mrphs-mainHeader").addClass("is-fixed");
			$(header).stop().animate({
				height: $('.is-fixed').css('height')	
			}, 200);
		  }
		} else {
		  $(".Mrphs-mainHeader").removeClass("is-fixed");
		  $(header).data("sticked",false);
		  $(header).css('height', null);
		  $(header).stop().animate({
			 height: mainHeaderSize 
		  }, 200);
		}
		animateToolBar();
	} else $(".Mrphs-mainHeader").removeClass("is-fixed");
  });
  
  currentHeaderWidth = $(".Mrphs-mainHeader").width();
  
});

