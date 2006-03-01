role=getTheElement("revise:role");
role.selectedIndex=0;
i=0;
while(true){
  spanId=getTheElement("revise:perm:"+i+":permissionSet");
  if(spanId){
    spanId.style.display="none";
  }
  else{
    break;
  }
  i++;
}

spanId=getTheElement("revise:perm:0:permissionSet");
if(spanId){
  spanId.style.display="block";
}