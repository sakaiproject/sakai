var DynamicListInput = function() {
  
  function $it(elementID) {
    return document.getElementById(elementID);
  }
  
  function deriveRowId(nameBase, index) {
    return nameBase + 'dynamic-list-input-row:' + index + ":";
    }
  
  function getControl(nameBase, index, extension) {
    var rowid = deriveRowId(nameBase, index);
    var controlid = rowid + extension;
    var control = $it(controlid);
    return control;
    }
  
  function lastRowInd(existrows) {
    var maxi = -1;
    for (var i in existrows) {
      if (i != 'count' && parseInt(i) > parseInt(maxi)) maxi = i;
      }
    return maxi;
    }
  
  function makeEnabledUpdater(nameBase, existrows, minlength, maxlength) {
    var removeenabled = true;
    var addcontrol = $it(nameBase + 'add-row');
    return function () {
      var makeremoveenabled = existrows.count > minlength;
      if (removeenabled ^ makeremoveenabled) {
        for (var row in existrows) {
          if (row == 'count') continue;
          var removec = getControl(nameBase, row, 'remove');
          removec.disabled = !makeremoveenabled;
          }
        removeenabled = makeremoveenabled;
        }
      var makeaddenabled = existrows.count < maxlength;
      addcontrol.disabled = !makeaddenabled;

      };
    }
  
  function assignRemoveClick(nameBase, i, existrows, enabledUpdater) {
    existrows[i] = true;
    ++existrows.count;
    var removec = getControl(nameBase, i, 'remove');
  
    removec.onclick = function () {
      var rowel = getControl(nameBase, i, '');
      rowel.parentNode.removeChild(rowel);
      delete existrows[i];
      --existrows.count;
      enabledUpdater();
      };
    }
  
  return {
    init_DynamicListInput: function(nameBase, rowcount, minlength, maxlength) {
      var existrows = new Object();
      existrows.count = 0;
          
      var enabledUpdater = makeEnabledUpdater(nameBase, existrows, minlength, maxlength);
      
      for (var i = 0; i < rowcount; ++ i) {
        assignRemoveClick(nameBase, i, existrows, enabledUpdater);
      }
      enabledUpdater();
      
      var sampleid = deriveRowId(nameBase, 0);
      var sampleel = $it(sampleid);
      var addid = nameBase + "add-row";
      var addel = $it(addid);
      var nextrowind = rowcount;
      addel.onclick = function() {
        var nextrowid = deriveRowId(nameBase, nextrowind);
        var lastrowind = lastRowInd(existrows);
        var lastrow = $it(deriveRowId(nameBase, lastrowind));
        
        var duprow = RSF.duplicateBranch(sampleel, nextrowid, lastrow);
        
        assignRemoveClick(nameBase, nextrowind, existrows, enabledUpdater);
        $it(nextrowid + 'input').value = "";
        ++nextrowind;
        enabledUpdater();
      };
    }
    
  };
}();