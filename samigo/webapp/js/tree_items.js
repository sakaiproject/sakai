 var TREE_ITEMS = [
 			['Test Settings', 'javascript:showHide(0)',
				['General Settings','javascript:showHide(1)'],
				['Publishing Settings', 'javascript:showHide(2)'],
				['Feedback Settings', 'javascript:showHide(3)'],
				['Gradebook Settings', 'javascript:showHide(4)'],
				['WWW Settings', 'javascript:showHide(5)'],
			],
			 
];
function showHide(divNo)
{
	for(i=1; i < 6; i++)
	{
		divisionNo = "div"+i;
		//alert(divisionNo);  
		if(i==divNo)
		{
	 	  document.getElementById(divisionNo).style.display="block";
   		}
   		else
   		{
  		  document.getElementById(divisionNo).style.display="none";
   		}
   	}	
};

