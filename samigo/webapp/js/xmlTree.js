var openImg = new Image();
   openImg.src = "../../images/tree/open.gif";
   var closedImg = new Image();
   closedImg.src = "../../images/tree/closed.gif";
   
   function showBranch(branch){
      var objBranch = 
         document.getElementById(branch).style;
      if(objBranch.display=="none")
         objBranch.display="block";
      else
         objBranch.display="none";
      swapFolder('I' + branch);
   }
   
   function swapFolder(img){
      objImg = document.getElementById(img);
      if(objImg.src.indexOf('open.gif')>-1)
         objImg.src = closedImg.src;
      else
         objImg.src = openImg.src;
   }
