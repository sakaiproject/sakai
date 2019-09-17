/**
 * SAK-42483 Jump-to-Top Behavior
 */
(function($) {

    function JumpToTop() {
        this.init();
    };

    JumpToTop.prototype.init = function() {
        this.$link = $("<a>");
        this.$link.attr("id", "jumptotop");
        this.$link.addClass("jumptotop");
        this.$link.addClass("hidden");
        this.$link.attr("title", "Jump to top");

        this.hide();

        $(document.body).append(this.$link);

        this.bindEvents();

        // just in case we"re scrolled, assume it is
        this.onScroll();
    };

    JumpToTop.prototype.bindEvents = function() {
        var self = this;

        $(window).on("scroll", $.proxy(self.onScroll, self));
        self.$link.on("click", $.proxy(self.onClick, self));
    };

    JumpToTop.prototype.show = function(opacity) {
        if (opacity > 0) {
            this.$link.removeClass("hidden");
            this.$link.addClass("visible");
        }
        this.$link.css("opacity", opacity);
    };

    JumpToTop.prototype.hide = function() {
        this.$link.css("opacity", "0");
        this.$link.addClass("hidden");
        this.$link.removeClass("visible");
    };

    JumpToTop.prototype.onClick = function(event) {
        event.preventDefault();
        $("html, body").animate({scrollTop : 0}, 500);
    };

    JumpToTop.prototype.onScroll = function(event) {
        var self = this;
        var magicOffset = $(window).height(); // only show jumpto after a whole window is scrolled
        var containerHeight = $(window).height();
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
