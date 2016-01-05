// bjones86 - SAM-2283
function toggleGroups(clickedElement) {
				
    // Get the elements
    var parent = clickedElement.parentNode;
    var childDivs = parent.getElementsByTagName("div");
    
    // Show/hide the groups panel
    for (i = 0; i < childDivs.length; i++) {
        if (endsWith(childDivs[i].id, "groupsPanel")) {
            if (childDivs[i].style.display === "none") {
                childDivs[i].style.display = "block";
                resizeFrame("grow");
            } else {
                childDivs[i].style.display = "none";
                resizeFrame("shrink");
            }
            
            // Change the triangle disclosure icon as appropriate
            toggleCollapse(clickedElement);
        }
    }
}

function toggleCollapse(element)
{
    if (element.className === "collapsed")
    {
        element.className = "expanded";
    }
    else
    {
        element.className = "collapsed";
    }
}

// Utility function equivalent to String.endsWith()
function endsWith(string, suffix) {
    return string.indexOf(suffix, string.length - suffix.length) !== -1;
}

// SAM-2382 - resize the iFrame to avoid double scroll bars
function resizeFrame(updown) {
    var frame;
    if (top.location !== self.location) {
        frame = parent.document.getElementById(window.name);
    }	
    if (frame) {
        var clientH;
        if (updown === "shrink") {
            clientH = document.body.clientHeight;
        } else {
            clientH = document.body.clientHeight + 30;
        }
        $(frame).height(clientH);
    }
}
