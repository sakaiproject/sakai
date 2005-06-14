var openImg = new Image();
   openImg.src = "../image/toc_open.gif";
   var closedImg = new Image();
   closedImg.src = "../image/toc_closed.gif";

function toggle(elm) {
 var newDisplay = "none";
 
 if(elm !=null && elm.nextSibling)
{
 var e = elm.nextSibling; 
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") {
   if (e.style.display == "none") {
    newDisplay = "block";
   }
   break;
  }
  e = e.nextSibling;
 }
 while (e != null) {
  if (e.tagName == "OL" || e.tagName == "ol") e.style.display = newDisplay;
  e = e.nextSibling;
 }
 swapFolder('I' + elm.id);
}
}


function collapseAll(tags) {
 for (i = 0; i < tags.length; i++) {
  var lists = document.getElementsByTagName(tags[i]);
  for (var j = 0; j < lists.length; j++) 
   lists[j].style.display = "none";
   var e = document.getElementById("root");
   e.style.display = "block";
 }
}


function swapFolder(img){
    objImg = document.getElementById(img);
    if(objImg.src.indexOf('closed.gif')>-1)
       objImg.src = openImg.src;
    else
       objImg.src = closedImg.src;
}
