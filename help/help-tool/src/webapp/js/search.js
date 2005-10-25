
/** this file is appended to the Table of Contnets markup after the DOM is built */
collapseAll(["ol"]);
element = document.getElementById("default");
n = element;

while(n){
        
  /** walk up to the parent folder's li element */
  while(n){    
    if (n.className == "dir"){
    
      /** walk down to first image */
      tempNode = n;
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
          tempNode.firstChild.nextSibling.className == "docs"){
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

if (element){
  document.getElementById('default').focus();
  parent.content.location = document.getElementById('default').getAttribute('href');
}
