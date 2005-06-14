collapseAll(["ol"]);
var n = document.getElementById("default");
if (n != null){
  while(1 == 1){
    n = n.parentNode;  
    if (n == null){
      break;
    }
    if (n.tagName == "LI"){    
      var firstChild = n.firstChild;
      if (firstChild == null){
        break;
      }
      if (firstChild.tagName == "A"){     
        toggle(firstChild);
        break;
      }
    }
  }    
  window.setTimeout("document.getElementById('default').focus()", 250);
  window.setTimeout("parent.content.location = document.getElementById('default').getAttribute('href');", 300);
  
}      		
