var SakaiProject;
if (!SakaiProject) SakaiProject = {};

SakaiProject.TableRenderer = function(form_id) {
  this.param_form = jQuery(jQuery('form[@id='+form_id+']'));
  this.update_funcs = new Array(); // This is an array of functions that should be called on submit
}
SakaiProject.TableRenderer.prototype.addSakaiPager = function(divID, pagenum, pagesize) {
  var firstPageButton = jQuery('*[@id='+divID+'] > form:eq(0) > div:eq(0) > input:eq(0)');
  var prevPageButton = jQuery('*[@id='+divID+'] > form:eq(0) > div:eq(0) > input:eq(1)');
  var nextPageButton = jQuery('*[@id='+divID+'] > form:eq(0) > div:eq(0) > input:eq(2)');
  var lastPageButton = jQuery('*[@id='+divID+'] > form:eq(0) > div:eq(0) > input:eq(3)');
  var pageSizeSelect = jQuery('*[@id='+divID+'] > form:eq(0) > div:eq(0) > select:eq(0)');
  var thisObj = this;
  thisObj.param_form.find('*[@name=pagesize]').val(pagesize);
  thisObj.param_form.find('*[@name=pagenum]').val(pagenum);
  firstPageButton.bind("click", this, function(e) {
    thisObj.param_form.find('*[@name=pagenum]').val('0');
    thisObj.submitTable();
    return false;
  });
  prevPageButton.bind("click", this, function(e) {
    var newpage = ((Number(pagenum)-1)+"");
    thisObj.param_form.find('*[@name=pagenum]').val(newpage);
    thisObj.submitTable();
    return false;
  });
  nextPageButton.bind("click", this, function(e) {
    var newpage = ((Number(pagenum)+1)+"");
    thisObj.param_form.find('*[@name=pagenum]').val(newpage);
    thisObj.submitTable();
    return false;
  });
  lastPageButton.bind("click", this, function(e) {
    thisObj.param_form.find('*[@name=pagenum]').val("-1");
    thisObj.submitTable();
    return false;
  });
  pageSizeSelect.bind("change", this, function(e) {
    thisObj.param_form.find('*[@name=pagesize]').val(pageSizeSelect.val());
    thisObj.submitTable();
    return false;
  });
  
}
SakaiProject.TableRenderer.prototype.addSortableHeader = function(thID, sortby, sortdir) {
  var thisObj = this;
  jQuery('*[@id='+thID+']').find('a').bind('click', this, function(e) {
    thisObj.param_form.find('*[@name=sortby]').val(sortby);
    thisObj.param_form.find('*[@name=sortdir]').val(sortdir);
    thisObj.submitTable();
    return false;
  });
}
SakaiProject.TableRenderer.prototype.addSortedHeader = function(thID, sortby, cursortdir, nextsortdir) {
  var thisObj = this;
  thisObj.param_form.find('*[@name=sortby]').val(sortby);
  thisObj.param_form.find('*[@name=sortdir]').val(cursortdir);
  jQuery('*[@id='+thID+']').find('a').bind('click', this, function(e) {
    thisObj.param_form.find('*[@name=sortby]').val(sortby);
    thisObj.param_form.find('*[@name=sortdir]').val(nextsortdir);
    thisObj.submitTable();
    return false;
  });
}
SakaiProject.TableRenderer.prototype.addSearchFilter = function(divID, startingtext) {
  var filterInput = jQuery('*[@id='+divID+'] > input:eq(0)');
  var findButton = jQuery('*[@id='+divID+'] > input:eq(1)');
  var clearButton = jQuery('*[@id='+divID+'] > input:eq(2)');
  var thisObj = this;
  //filterInput.bind('click', this, function(e) {
    /* We could put some code here to clear the box if it has the default text,
       might need to use the focus or another event besides click. */
  //});
  thisObj.update_funcs.push(function() {
    thisObj.param_form.find('*[@name=filter]').val(filterInput.val());
  });
  filterInput.bind('keydown', this, function(e) {
    if (e.keyCode == 13)
      thisObj.submitTable();
  });
  findButton.bind('click', this, function(e) {
    thisObj.submitTable();
  });
  clearButton.bind('click', this, function(e) {
    filterInput.val('');
    thisObj.submitTable();
  });
}
SakaiProject.TableRenderer.prototype.submitTable = function() {
  for (var i = 0; i < this.update_funcs.length; i++) {
    this.update_funcs[i].call(this);
  }
  this.param_form.submit();
}