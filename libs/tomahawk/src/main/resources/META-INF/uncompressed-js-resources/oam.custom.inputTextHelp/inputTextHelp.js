function resetHelpValue(helpText, id)
{
    var element=document.getElementById(id);
    if(element.value==helpText)
    {
        element.value="";
    }
}

function selectText(helpText, id)
{
    var element=document.getElementById(id);
    if(element.value==helpText)
    {
        element.select();
    }
}