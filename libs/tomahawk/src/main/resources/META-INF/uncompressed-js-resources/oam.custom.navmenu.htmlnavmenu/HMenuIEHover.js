startList = function() 
{
    if (document.all && document.getElementById) 
    {
        navDivRoot = document.getElementById("hNav_outer");
        navRoot = navDivRoot.childNodes[0];    
        for (i=0; i<navRoot.childNodes.length; i++) 
        {
            node = navRoot.childNodes[i];
            if ((node.nodeName || node.tagName).toLowerCase()=="li") 
            {
                node.onmouseover=function() 
                {
                    this.className+=" over";
                }
                node.onmouseout=function() 
                {
                    this.className=this.className.replace(" over", "");
                }
            }
        }
    }
}
window.onload=startList;