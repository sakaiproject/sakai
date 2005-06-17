
//collapseAll(["ol"]);
var n = document.getElementById("default");

if (n != null){
  while(true){
    n = n.parentNode;  
    
    if (n == null){
      break;
    }            
    
    if (n.className == "dir"){
      if (n.childNodes && n.childNodes.length > 1 && n.childNodes[1].tagName == "A"){
        toggle(n.childNodes[1]);
      }
    }    
  }
  document.getElementById('default').focus();
  parent.content.location = document.getElementById('default').getAttribute('href');
  //window.setTimeout("document.getElementById('default').focus()", 250);
  //window.setTimeout("parent.content.location = document.getElementById('default').getAttribute('href');", 300);
}