/* ============================================================================
 * jquery.clearsearch.js v1.0.3
 * https://github.com/waslos/jquery-clearsearch
 * ============================================================================
 * Copyright (c) 2012, Was los.de GmbH & Co. KG
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the "Was los.de GmbH & Co. KG" nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ========================================================================= */
(function($) {
	$.fn.clearSearch = function(options) {
		var settings = $.extend({
			'clearClass' : 'clear_input',
			'focusAfterClear' : true,
			'linkText' : '&times;'
		}, options);
		return this.each(function() {
					var $this = $(this), btn,
						divClass = settings.clearClass + '_div';

					if (!$this.parent().hasClass(divClass)) {
						$this.wrap('<div style="position: relative;" class="'
							+ divClass + '">' + $this.html() + '</div>');
						$this.after('<a style="position: absolute; cursor: pointer;" class="'
							+ settings.clearClass + '">' + settings.linkText + '</a>');
					}
					btn = $this.next();

					function clearField() {
						$this.val('').change();
						triggerBtn();
						if (settings.focusAfterClear) {
							$this.focus();
						}
						if (typeof (settings.callback) === "function") {
							settings.callback();
						}
					}

					function triggerBtn() {
						if (hasText()) {
							btn.show();
						} else {
							btn.hide();
						}
						update();
					}

					function hasText() {
						return $this.val().replace(/^\s+|\s+$/g, '').length > 0;
					}

					function update() {
						var width = $this.outerWidth(), height = $this
								.outerHeight();
						btn.css({
							top : height / 2 - btn.height() / 2,
							left : width - height / 2 - btn.height() / 2
						});
					}

					btn.on('click', clearField);
					$this.on('keyup keydown change focus', triggerBtn);
					triggerBtn();
				});
	};
})(jQuery);
