
/** this file is appended to the Table of Contnets markup after the DOM is built */
collapseAll(["ol"]);
var element = document.getElementById("default"),
n = element;

while(n){
        
  /** walk up to the parent folder's li element */
  while(n){    
    if (n.className && n.className.search(/dir/) !== -1){
    
      /** walk down to first image */
      var tempNode = n;
      while (tempNode){
         if (tempNode.tagName == "IMG"){
           if(tempNode.src.indexOf('closed.gif') > -1){
             tempNode.src = openImg.src;
           }
           else {
             tempNode.src = closedImg.src;
           }
           break;
         }
         tempNode = tempNode.firstChild;
      }
      
      /** walk down to docs list item and set style */
      tempNode = n;
      if (tempNode.firstChild && tempNode.firstChild.nextSibling && 
          tempNode.firstChild.nextSibling.className.search(/docs/) !== -1){
        e = tempNode.firstChild.nextSibling;
        if (e.style.display || e.style.display== "none") {     
          e.style.display = "block";
        }
        else{
          e.style.display = "none";
        }  
      }            
    }                       
    n = n.parentNode;
  }       
}

if (element !== null){
    try{
        element.focus();
    } catch(e){
        //failed to get window focus
    }
  parent.content.location = element.getAttribute('href');
}
