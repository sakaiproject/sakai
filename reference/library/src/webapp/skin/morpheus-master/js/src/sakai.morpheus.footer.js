/**
 * For Footer toggles in Morpheus
 */

$(".js-footer-toggle__panel").addClass("is-hidden");
$(".js-footer-toggle__control").addClass("plus");

$('.js-footer-toggle__control').click(function() {
event.preventDefault();
$(this).next('.js-footer-toggle__panel').toggleClass("is-hidden is-visible");
//$(this).next('pre').next(".source__link").slideToggle("fast");
$(this).toggleClass("plus minus");
});