// methods relating to getting/manipulating site/path information
var removeEmptyElements = function(array) {
  return array.filter(function(e) {
                    return e;
                  });
};

var getSiteFromRelativePath = function(path) {
  var fullpath = path.split('/');
      fullpath = removeEmptyElements(fullpath);
  var site = fullpath[0] || null;
      site += ('/' + fullpath[1]) || null;

  return '/' + site + '/';
};

var getDirectoryFromPath = function(path) {
  var fullpath = path.split('/');
      fullpath = removeEmptyElements(fullpath);

  return '/' + fullpath.slice(2).join('/') + '/';
};
