/*global portal*/
/**
 * SAK-42483 Jump-to-Top Behavior
 */
(function($) {

  function JumpToTop() {
    
    // Don't run this if i18n is not yet available
    if (typeof portal.i18n.loadProperties !== 'function') {
      return;
    }

    this.$link = $("<a>");
    this.$link.attr("id", "jumptotop");
    this.$link.addClass("jumptotop");
    this.$link.addClass("hidden");

    portal.i18n.loadProperties({
      resourceClass: "org.sakaiproject.portal.api.PortalService",
      resourceBundle: "jumptotop",
      namespace: "jumptotop",
      callback: () => this.$link.attr("title", portal.i18n.tr("jumptotop", "jumptotop_title"))
    });

    this.hide();

    $(document.body).append(this.$link);

    this.bindEvents();

    // just in case we"re scrolled, assume it is
    this.onScroll();
  };

  JumpToTop.prototype.bindEvents = function () {

    var self = this;

    $(window).on("scroll", $.proxy(self.onScroll, self));
    self.$link.on("click", $.proxy(self.onClick, self));
  };

  JumpToTop.prototype.show = function (opacity) {

    if (opacity > 0) {
      this.$link.removeClass("hidden");
      this.$link.addClass("visible");
    }
    this.$link.css("opacity", opacity);
  };

  JumpToTop.prototype.hide = function () {

    this.$link.css("opacity", "0");
    this.$link.addClass("hidden");
    this.$link.removeClass("visible");
  };

  JumpToTop.prototype.onClick = function (event) {

    event.preventDefault();
    $("html, body").animate({scrollTop : 0}, 500);
  };

  JumpToTop.prototype.onScroll = function (event) {

    var self = this;
    var magicOffset = $(window).height(); // only show jumpto after a whole window is scrolled
    var containerHeight = magicOffset;
    var scrollHeight = $(document.body).height();
    var scrollOffset = Math.max(0, window.scrollY - magicOffset);

    if (scrollOffset === 0) {
      self.hide();
    } else {
      var opacity = Math.min(1, scrollOffset / containerHeight);
      self.show(opacity);
    }
  };

  new JumpToTop();
})($PBJQ);
