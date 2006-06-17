role=getTheElement("revise:role");
role.selectedIndex=0;
i=0;
while(true){
  spanElement=getTheElement("revise:perm:"+i+":permissionSet");  
  
  if(spanElement){  
    rowNode = getSurroundingRowNode(spanElement);

    if (rowNode){    
      rowNode.style.display="none";
    }    
  }
  else{
    break;
  }
  i++;
}

spanElement=getTheElement("revise:perm:0:permissionSet");

if(spanElement){
  rowNode = getSurroundingRowNode(spanElement);    
  if (rowNode){    
    rowNode.style.display="block";
  }    
}


