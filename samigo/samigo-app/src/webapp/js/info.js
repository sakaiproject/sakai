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
            } else {
                childDivs[i].style.display = "none";
            }
            
            // Change the triangle disclosure icon as appropriate
            if (clickedElement.className === "collapse") {
                clickedElement.className = "expand";
            } else {
                clickedElement.className = "collapse";
            }
        }
    }
}

// Utility function equivalent to String.endsWith()
function endsWith(string, suffix) {
    return string.indexOf(suffix, string.length - suffix.length) !== -1;
}
