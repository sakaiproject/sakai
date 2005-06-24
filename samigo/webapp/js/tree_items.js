// Title: Tigra Tree
// Description: See the demo at url
// URL: http://www.softcomplex.com/products/tigra_menu_tree/
// Version: 1.1 (size optimized)
// Date: 11-12-2002 (mm-dd-yyyy)
// Contact: feedback@softcomplex.com (specify product title in the subject)
// Notes: This script is free. Visit official site for further details.
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

