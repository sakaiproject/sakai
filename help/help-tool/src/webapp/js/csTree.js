var openImg = new Image();
var closedImg = new Image();

openImg.src = "../image/toc_open.gif";
closedImg.src = "../image/toc_closed.gif";

function toggle(elm) {
 
  if(elm && elm.nextSibling)
  {  
    var sibling = elm.nextSibling
    if (sibling.tagName == "OL") {
      if (!sibling.style.display || sibling.style.display == "none") {
          sibling.style.display = "block";
      }
      else{
        sibling.style.display = "none";
      }
    }
 
    swapFolder('I' + elm.id);
  }
}


function collapseAll(tags) {
  for (i = 0; i < tags.length; i++) {
    var lists = document.getElementsByTagName(tags[i]);
    for (var j = 0; j < lists.length; j++){
      lists[j].style.display = "none";
    }
      
    var e = document.getElementById("root");
    e.style.display = "block";
  }
}

function swapFolder(img){
  objImg = document.getElementById(img);
  if(objImg.src.indexOf('closed.gif') > -1){
    objImg.src = openImg.src;
  }
  else {
    objImg.src = closedImg.src;
  }
}
