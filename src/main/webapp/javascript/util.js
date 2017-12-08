function formatDecimalNumber(nStr)
{
  nStr += '';
  x = nStr.split('.');
  x1 = x[0];
  x2 = x.length > 1 ? '.' + x[1] : '';
  var rgx = /(\d+)(\d{3})/;
  while (rgx.test(x1))
  {
      x1 = x1.replace(rgx, '$1' + '.' + '$2');
  }
  return x1 + x2;
}

function getWinHeight() 
{
    var h;
    if (window.innerHeight) 
    {
        h = window.innerHeight; 
    } 
    else if (document.body)
    {
        h = document.body.clientHeight;
        if ((document.body.offsetHeight == h) && document.documentElement && document.documentElement.clientHeight)
        {
            h = document.documentElement.clientHeight;
        }
    }
    return h;
}    
  
function getWinWidth() 
{
    var w;
    if (window.innerWidth) 
    {
        w = window.innerWidth; 
    } 
    else if (document.body)
    {
        w = document.body.clientWidth;
        if ((document.body.offsetWidth == w) && document.documentElement && document.documentElement.clientWidth)
        {
            w = document.documentElement.clientWidth;
        }
    }
    return w;
}  

/*
  Places a box centered vertically and horizontally on the browser window.
  @param box a DOM element of type div
*/
function centerBox(box)
{
    box.style.visibility = "hidden";

    if (window.ActiveXObject !== undefined) 
    {
        windowWidth = document.body.clientWidth;
        windowHeight = document.body.clientHeight;
        yScrolled = document.body.scrollTop;
        xScrolled = document.body.scrollLeft;
    }
    else
    {
        windowWidth = window.innerWidth;
        windowHeight = window.innerHeight;
        yScrolled = window.pageYOffset;
        xScrolled = window.pageXOffset;
        
        if (yScrolled > 0)
        {
            // scrollbar exists 
            windowWidth = windowWidth - 20;
        }
    }
        
    boxWidth = box.offsetWidth;
    
    xoffset = (windowWidth - boxWidth) / 2;
    
    if (xoffset < 2)
    {
        xoffset = 2;
    }
        
    xpos = xoffset + xScrolled;

    box.style.left = xpos + 'px';

    boxHeight = box.offsetHeight;

    ypos = (windowHeight - boxHeight) / 2 + yScrolled;
    if (ypos < 2)
    {
        ypos = 2;
    }

    box.style.top = ypos + 'px';

    box.style.visibility = "visible";
}
  
function removeAllChildNodes(parentElementId)
{
	var parentNode = document.getElementById(parentElementId);

	if (parentNode && parentNode.hasChildNodes())
	{
	    while (parentNode.childNodes.length >= 1)
	    {
	    	parentNode.removeChild(parentNode.firstChild);       
	    } 
	}
}

function shortText(origText, maxLength)
{
	if (origText.length <= maxLength)
	{
		return origText;
	}
	
	return (origText.substring(0, maxLength - 4) + " ...");
}

function abbrevText(origText, maxLength)
{
	if (origText.length <= maxLength)
	{
		return origText;
	}
	
	return (origText.substring(0, maxLength / 2) + " ... " + origText.substring(origText.length - (maxLength / 2) + 5));
}

function extractFileName(filePath) {
	var lastSepIdx = filePath.lastIndexOf("\\");
	if (lastSepIdx < 0) {
		lastSepIdx = filePath.lastIndexOf("/");
	}
	if ((lastSepIdx < 1) || (lastSepIdx == filePath.length - 1)) {
		return filePath;
	}
	return filePath.substring(lastSepIdx + 1);
}

function getFileNameExt(fileName)
{
    fileExt="";

    extStart=fileName.lastIndexOf('.');

    if (extStart > 0)
    {
	fileExt=fileName.substring(extStart).toUpperCase();
    }
    
    return(fileExt);
}

function insertDoubleBackslash(source)
{
    return(source.replace(/\\/g,"\\\\").replace(/'/g, "\\'"));
}

function trim(str) 
{
    return str.replace (/^\s+/, '').replace (/\s+$/, '');
}

function getAbsolutePos(obj) {
	var curleft = 0;
	var curtop = 0;
	if (obj.offsetParent) {
		do {
			curleft += obj.offsetLeft;
			curtop += obj.offsetTop;
		} while (obj = obj.offsetParent);			
	}
	return [curleft, curtop];
}

function removeCSSRecursive(domNode, cssClassName)
{
    if (domNode.nodeType != 1) 
	{
	    return;
	}

    var cssValue = domNode.getAttribute("class");
	if (cssValue)
	{
	    var newCssValue = cssValue.replace(cssClassName, "");
		if (newCssValue != cssValue) 
		{
		    domNode.setAttribute("class", newCssValue);
		}
    }

	var children = domNode.childNodes;
	if (children) 
	{
	    for (var i = 0; i < children.length; i++) 
		{
		    removeCSSRecursive(children.item(i), cssClassName);
		}
	}
}

function validateEmail(emailAddress) {  
    return (/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(emailAddress));
}

function toast(message, duration) {
   	toastBox = document.createElement("div");
   	toastBox.id = "toastBox";
   	toastBox.setAttribute("class", "toastBox");
   	document.documentElement.appendChild(toastBox);
   	
    var yScrolled;
        
    if (window.ActiveXObject !== undefined) {
        yScrolled = document.body.scrollTop;
    } else {
        yScrolled = window.pageYOffset;
    }
   	
   	toastText = document.createElement("span");
   	toastText.innerHTML = message;
   	toastBox.appendChild(toastText);

   	centerBox(toastBox);
   	
   	setTimeout(hideToast, duration);
}

function hideToast() {
	var toastBox = document.getElementById("toastBox");
	if (toastBox) {
		toastBox.parentNode.removeChild(toastBox);
	}
}

function customAlert(alertText, buttonText) {
   	var mouseShield = document.createElement("div");
   	mouseShield.id = "mouseClickShield";
   	mouseShield.setAttribute("class", "mouseClickShield");
   	document.documentElement.appendChild(mouseShield);
    mouseShield.setAttribute("onclick", "javascript:void(0);");
	
    mouseShield.style.height = document.documentElement.clientHeight + "px";   
    
    var alertCont = document.createElement("div");
    alertCont.setAttribute("class", "alertCont");
   	mouseShield.appendChild(alertCont);

    var alertTextElem = document.createElement("span");
    alertTextElem.setAttribute("class", "alertText");
    alertTextElem.innerHTML = alertText;
    alertCont.appendChild(alertTextElem);
    
    var buttonContElem = document.createElement("div");
    buttonContElem.setAttribute("class", "alertButtonCont");
    alertCont.appendChild(buttonContElem);
    
    var confirmButtonElem = document.createElement("button");
    confirmButtonElem.setAttribute("class", "alertConfirmButton");
    confirmButtonElem.setAttribute("onclick", "closeAlert()");
    if (buttonText) {
        confirmButtonElem.innerHTML = buttonText;
    } else {
        confirmButtonElem.innerHTML = "OK";
    }
    buttonContElem.appendChild(confirmButtonElem);

    centerBox(alertCont);
}

function closeAlert() {
	var clickShield = document.getElementById("mouseClickShield");
	if (clickShield) {
		clickShield.parentNode.removeChild(clickShield);
	}
}

function customConfirm(confirmText, cancelButtonText, continueButtonText, continueCallback, cancelCallback) {
   	var mouseShield = document.createElement("div");
   	mouseShield.id = "mouseClickShield";
   	mouseShield.setAttribute("class", "mouseClickShield");
    
   	var bodyElem = document.getElementsByTagName("BODY")[0];
    bodyElem.appendChild(mouseShield);
    
    mouseShield.setAttribute("onclick", "javascript:void(0);");
	
    mouseShield.style.height = window.innerHeight + "px";
    
    var alertCont = document.createElement("div");
    alertCont.setAttribute("class", "alertCont");
   	mouseShield.appendChild(alertCont);

    var alertTextCont = document.createElement("div");
    alertTextCont.setAttribute("class", "alertTextCont");
    alertCont.appendChild(alertTextCont);
   	
    var alertTextElem = document.createElement("span");
    alertTextElem.setAttribute("class", "alertText");
    alertTextElem.innerHTML = confirmText;
    alertTextCont.appendChild(alertTextElem);
    
    var buttonContElem = document.createElement("div");
    buttonContElem.setAttribute("class", "alertButtonCont");
    alertCont.appendChild(buttonContElem);
    
    var confirmButtonElem = document.createElement("button");
    confirmButtonElem.setAttribute("class", "alertConfirmButton");
    confirmButtonElem.style.marginRight = "40px";
    confirmButtonElem.onclick = function() {
    	closeAlert();
    	continueCallback();
    };
    if (continueButtonText) {
    	confirmButtonElem.innerHTML = continueButtonText;
    } else {
    	confirmButtonElem.innerHTML = "Yes";
    }
    buttonContElem.appendChild(confirmButtonElem);
    
    var cancelButtonElem = document.createElement("button");
    cancelButtonElem.setAttribute("class", "alertConfirmButton");
    if (cancelCallback) {
    	cancelButtonElem.onclick = cancelCallback;
    } else {
        cancelButtonElem.setAttribute("onclick", "closeAlert()");
    }
    if (cancelButtonText) {
    	cancelButtonElem.innerHTML = cancelButtonText;
    } else {
    	cancelButtonElem.innerHTML = "No";
    }
    buttonContElem.appendChild(cancelButtonElem);

    centerBox(alertCont);
}

function insertAtCursor(myField, myValue) {
    if (document.selection) {  // MSIE
        myField.focus();
        sel = document.selection.createRange();
        sel.text = myValue;
    } else if (myField.selectionStart || myField.selectionStart == '0') {
        var startPos = myField.selectionStart;
        var endPos = myField.selectionEnd;
        myField.value = myField.value.substring(0, startPos)
            + myValue
            + myField.value.substring(endPos, myField.value.length);
        myField.selectionStart = startPos + myValue.length;
        myField.selectionEnd = startPos + myValue.length;
    } else {
        myField.value += myValue;
    }
}

function resizeViewPort(width, height) {
    if (window.outerWidth) {
        window.resizeTo(width + (window.outerWidth - window.innerWidth), height + (window.outerHeight - window.innerHeight));
    }
}

function calculateAspectRatioFit(srcWidth, srcHeight, maxWidth, maxHeight) {
   var ratio = Math.min(maxWidth / srcWidth, maxHeight / srcHeight);
   return {width: srcWidth * ratio, height: srcHeight * ratio};
}

function escapeParam(origText) {
	return origText.replace(/'/g, "\\'");	
}

function escapeForId(origValue) {
    return origValue.replace(/ /g, "_"); 
}

function getPrevSiblingElement(element) {
    var prevSibling = element.previousSibling;
    while (prevSibling) {
    	if (prevSibling.nodeType == 1) {
    		return prevSibling;
    	}
    	prevSibling = prevSibling.previousSibling;
    }
	return null;
}

function getNextSiblingElement(element) {
    var nextSib = element.nextSibling;
    while (nextSib) {
    	if (nextSib.nodeType == 1) {
    		return nextSib;
    	}
    	nextSib = nextSib.nextSibling;
    }
	return null;
}
