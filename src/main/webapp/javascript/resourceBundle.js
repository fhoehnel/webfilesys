function setBundleResources(domNode)
{    
    var domNode = domNode || document.documentElement;    

    if (domNode.nodeType === 1) // Element node 
    {
    	var resourceBundleKey = domNode.getAttribute("resource");
    	if (resourceBundleKey)
    	{
    		var tagName = domNode.tagName
    		
    		var resourceText = resourceBundle[resourceBundleKey];
    		
    		if (resourceText)
    		{
                if ((tagName == "LABEL") || (tagName == "A") || (tagName == "TD") || (tagName == "DIV")  || 
                	(tagName == "SPAN") || (tagName == "TH") || (tagName == "OPTION") ||
                	(tagName == "TITLE"))
                {
                  	setNodeValue(domNode, resourceText);
                }
                else if ((tagName == "INPUT") && ((domNode.getAttribute("type") == "button") || (domNode.getAttribute("type") == "submit")))
                {
                 	domNode.setAttribute("value", resourceText);
                }
    		}
    		else
    		{
    			if (typeof console != "undefined")
    			{
    				console.log("resource bundle text not found for key: " + resourceBundleKey);
    			}
    		}
    	}

    	var titleKey = domNode.getAttribute("titleResource");
    	if (titleKey)
    	{
			var titleText = resourceBundle[titleKey];
    		
    		if (titleText)
    		{
        		domNode.setAttribute("title", resourceBundle[titleKey]);
    		}
    		else
    		{
    			if (typeof console != "undefined")
    			{
    				console.log("resource bundle text not found for key: " + titleKey);
    			}
    		}
    	}
    }
    
    if (domNode.hasChildNodes()) 
    {      
        var child = domNode.firstChild;
        
        while (child)
        {        
            if (child.nodeType === 1) // Element node 
            {          
            	setBundleResources(child);        
            }
            child = child.nextSibling;
        }
    }
	
	if (browserMSIE9) 
	{
        // Workaround for MSIE 9 bug: touch styles to trigger new rendering of selectbox
	    if ((domNode.nodeType === 1) && (domNode.tagName == "SELECT")) 
		{
			var newStyleAttr = "";
		    var oldStyleAttr = domNode.getAttribute("style");
			if (oldStyleAttr) 
			{
			    newStyleAttr = oldStyleAttr + " ";
			}
		    domNode.setAttribute("style", newStyleAttr);
		}
	}
}

function setNodeValue(domNode, text)
{
    if (domNode.hasChildNodes()) 
    {      
        var child = domNode.firstChild;
        
        if (child)
        {        
            if (child.nodeType === 3) // text node
            {          
            	child.nodeValue = text;
            	return;
            }
        }
    }
    
    domNode.appendChild(document.createTextNode(text));
}
