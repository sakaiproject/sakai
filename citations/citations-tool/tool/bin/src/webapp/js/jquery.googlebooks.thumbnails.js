/**
 * JQuery Plugin to get thumbnail images from Google Books
 *
 *  Populates the src of any img.googleBookCover with a data-isbn attribute
 *  of an ISBN, with a small thumbnail from the Google Books API.
 */
(function ($) {

    var GOOGLE_BOOKS_SEARCH_API = 'https://www.googleapis.com/books/v1/volumes?q=isbn:';

    $.fn.googleBooksCover = function() {

        $('.googleBookCover').each(function(index, element) {

            var isbn = $(element).attr('data-isbn');
            if (isbn!=''){
            var googleBooksURL = GOOGLE_BOOKS_SEARCH_API + $(element).attr('data-isbn');

            $.getJSON(googleBooksURL, function(data){
                try {
                    element.src = data.items[0].volumeInfo.imageLinks.smallThumbnail;
                }
                catch(err) {
                    if (typeof console !== "undefined" && typeof console.log !== "undefined") {
                        console.log ('There was a problem getting the small thumbnail using this URL:' + googleBooksURL );
                    }
                }
            });
            }
        });
    };
}(jQuery));
