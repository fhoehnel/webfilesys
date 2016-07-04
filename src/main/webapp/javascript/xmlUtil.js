function getChildElementsByTagName(parentElem, tagName) {
    var childElements = new Array();
	var children = parentElem.childNodes;
   	var childNum = children.length;    
    
    for (var i = 0; i < childNum; i++) {
    	if (children[i].nodeType == 1) { // Element node
   		    if (children[i].tagName == tagName) {
   		    	childElements.push(children[i]);
   	        }
    	}
    }
    return childElements;	
}

function getChildValuesByTagName(parentElem, tagName) {
    var childValues = new Array();
	var children = parentElem.childNodes;
   	var childNum = children.length;    
    
    for (var i = 0; i < childNum; i++) {
    	if (children[i].nodeType == 1) {  // Element node
       		if (children[i].tagName == tagName) {
       			childValues.push(children[i].firstChild.nodeValue);
       	    }
    	}
    }
    return childValues;	
}

function getChildValueByTagName(parentElem, tagName) {
	var children = parentElem.childNodes;
   	var childNum = children.length;    
    
    for (var i = 0; i < childNum; i++) {
    	if (children[i].nodeType == 1) { // Element node
   		    if (children[i].tagName == tagName) {
   		    	return children[i].firstChild.nodeValue;
   	        }
    	}
    }
    return null;	
}

