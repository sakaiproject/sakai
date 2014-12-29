var openImg = new Image();
var closedImg = new Image();

openImg.src = "../image/toc_open.gif";
closedImg.src = "../image/toc_closed.gif";

function toggle(elm) {

  var e = elm.parentNode;
  while (e){
    if (e.tagName == "H1"){
      e = e.nextSibling;
      if (e && e.tagName == "OL"){
        if (!e.style.display || e.style.display== "none") {     
          e.style.display = "block";
        }
        else{
          e.style.display = "none";
        }
        break;
      }
    }
    e = e.parentNode;
  }
        
  swapFolder(elm);  
}

function collapseAll(tags) {
  for (i = 0; i < tags.length; i++) {
    var lists = document.getElementsByTagName(tags[i]);
    for (var j = 0; j < lists.length; j++){
      lists[j].style.display = "none";
    }          
  }
  var e = document.getElementById("root");
  e.style.display = "block";
}

/**
 walk up to table row then down to first column then image in this column
 */
function swapFolder(element){
  
  /** nodes should never be null */   
  e = element.parentNode; // get td
  e = e.parentNode; // get tr
  e = e.firstChild; // get td
  e = e.firstChild; // get img
  
  if (e && e.tagName == "IMG"){
    if(e.src.indexOf('closed.gif') > -1){
       e.src = openImg.src;
     }
     else {
       e.src = closedImg.src;
     }
  }
}
