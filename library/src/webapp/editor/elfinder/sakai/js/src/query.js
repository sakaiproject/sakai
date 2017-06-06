(function($) {

$.sakai.elfinder.query = function() {
  var query = {};
  var string = window.location.search.slice(1);
  var params = string.split('&');
  $.each(params, function(idx, param) {
    var keyValue = param.split('=');
    query[keyValue[0]] = keyValue[1];
  });

  return query;
};

})(jQuery);
