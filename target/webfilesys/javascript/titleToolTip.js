function showToolTip(message) {
    toolTipDiv = document.getElementById('toolTip');    
    toolTipDiv.innerHTML = message;
	centerBox(toolTipDiv);
    toolTipDiv.style.visibility = 'visible';
}

function hideToolTip() {
    toolTipDiv = document.getElementById('toolTip');    
    toolTipDiv.style.visibility = 'hidden';
}