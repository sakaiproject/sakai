    function resize() {
        mySetMainFrameHeight(document.getElementById("toolId").value);
    }
    function changeSelect(obj) {
        $(obj).trigger("change");
    }
    function addTagSelector(obj) {
        if (obj) {
            $(obj).select2({formatNoMatches:function(){return'';}});
            $(obj).on('change',function(){resize();});
        }
    }
    
    function clearSelection(selectObject)
    {
        for (var i=0; i<selectObject.options.length; i++)
        {
            selectObject.options[i].selected=false;
        }
        changeSelect(selectObject);
    }
    
    $(document).ready(function() {
        addTagSelector(document.getElementById(document.getElementById("selectorId").value));
    });
