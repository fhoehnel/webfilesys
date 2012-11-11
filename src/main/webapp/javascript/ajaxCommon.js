var req;

function xmlRequest(url, callBackFunction)
{
    req = false;
    
    if (window.XMLHttpRequest) 
    {
    	try 
    	{
	    req = new XMLHttpRequest();
        } 
        catch (e) 
        {
	    req = false;
        }
    } 
    else
    {
        // branch for IE/Windows ActiveX version
        if (window.ActiveXObject) 
        {
       	    try 
       	    {
        	req = new ActiveXObject("Msxml2.XMLHTTP");
      	    } 
      	    catch (e) 
      	    {
        	try 
        	{
          	    req = new ActiveXObject("Microsoft.XMLHTTP");
        	} 
        	catch (e) 
        	{
          	    req = false;
        	}
	    }
        }
    }
        
    if (req) 
    {
	req.onreadystatechange = callBackFunction;
	req.open("GET", url, true);
	req.send("");
    }
    else
    {
        alert('Your browser does not support the XMLHttpRequest');
    }
}

function xmlRequestSynchron(url)
{
    req = false;
    
    if (window.XMLHttpRequest) 
    {
    	try 
    	{
	    req = new XMLHttpRequest();
        } 
        catch (e) 
        {
	    req = false;
        }
    } 
    else
    {
        // branch for IE/Windows ActiveX version
        if (window.ActiveXObject) 
        {
       	    try 
       	    {
        	req = new ActiveXObject("Msxml2.XMLHTTP");
      	    } 
      	    catch (e) 
      	    {
        	try 
        	{
          	    req = new ActiveXObject("Microsoft.XMLHTTP");
        	} 
        	catch (e) 
        	{
          	    req = false;
        	}
	    }
        }
    }
        
    if (req) 
    {
	req.open("GET", url, false);
	req.send(null);
    }
    else
    {
        alert('Your browser does not support the XMLHttpRequest');
    }
    
    if (req.status != 200)
    {
        alert('error code from XMLHttpRequest: ' + req.status);
        return;
    }
    
    return(req.responseXML);    
}

function xmlRequestPost(url, params, callBackFunction)
{
    req = false;
    
    if (window.XMLHttpRequest) 
    {
    	try 
    	{
	    req = new XMLHttpRequest();
        } 
        catch (e) 
        {
	    req = false;
        }
    } 
    else
    {
        // branch for IE/Windows ActiveX version
        if (window.ActiveXObject) 
        {
       	    try 
       	    {
        	req = new ActiveXObject("Msxml2.XMLHTTP");
      	    } 
      	    catch (e) 
      	    {
        	try 
        	{
          	    req = new ActiveXObject("Microsoft.XMLHTTP");
        	} 
        	catch (e) 
        	{
          	    req = false;
        	}
	    }
        }
    }
        
    if (req) 
    {
	req.onreadystatechange = callBackFunction;
	req.open("POST", url, true);

        req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        req.setRequestHeader("Content-length", params.length);
        req.setRequestHeader("Connection", "close");
	
	req.send(params);
    }
    else
    {
        alert('Your browser does not support the XMLHttpRequest');
    }
}

function ajaxRPC(method, param1)
{
    var url = "/webfilesys/servlet?command=ajaxRPC&method=" + method + "&param1=" + param1;

    var responseXml = xmlRequestSynchron(url);
    
    var resultItem = responseXml.getElementsByTagName("result")[0];            
    
    if (!resultItem)
    {
        return("");
    }
    
    return(resultItem.firstChild.nodeValue);
}

function browserXslt(xmlUrl, xslUrl)
{
    if (window.ActiveXObject) 
    {
        // MSIE  
        return(browserXsltMSIE(xmlUrl, xslUrl));
    }
    else
    {
        var browserIsFirefox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;
        
        if (browserIsFirefox)   
        { 
            // Firefox & Co

            return(browserXsltMozilla(xmlUrl, xslUrl));
        }
        else
        {
            var browserIsChrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;

            if (browserIsChrome)
            {
                return(browserXsltMozilla(xmlUrl, xslUrl));
            }
            else if (browserSafari) 
            {
                return(browserXsltMozilla(xmlUrl, xslUrl));
            }
            else 
            {
                // XSLT with Javascript (google ajaxslt)
                return(browserXsltJavascript(xmlUrl, xslUrl));
            }
        }
    }
}
    
function browserXsltMozilla(xmlUrl, xslUrl)
{ 
    var xslStyleSheet = xmlRequestSynchron(xslUrl);
   
    if (!xslStyleSheet)
    {
        alert('cannot load xsl stylesheet from ' + xslUrl);
        return;
    }

    var xmlDoc = xmlRequestSynchron(xmlUrl);

    if (!xmlDoc)
    {
        alert('cannot load xml from ' + xmlUrl);

        return;
    }

    var html;

    var xsltProcessor = new XSLTProcessor();
       
    xsltProcessor.importStylesheet(xslStyleSheet);

    result = xsltProcessor.transformToDocument(xmlDoc);
  
    xmlSerializer = new XMLSerializer();

    return(xmlSerializer.serializeToString(result));
}

function browserXsltMSIE(xmlUrl, xslUrl)
{ 
    var xsl = new ActiveXObject('MSXML2.FreeThreadedDOMDocument.3.0');
    xsl.async = false;
    if (!xsl.load(xslUrl))
    {
        alert('cannot load xsl stylesheet from ' + xslUrl);
        return;
    }

    var xslTemplate = new ActiveXObject("Msxml2.XSLTemplate.3.0");
    xslTemplate.stylesheet = xsl;

    xml = new ActiveXObject("Msxml2.DOMDocument.3.0");
    xml.async = false;
    if (!xml.load(xmlUrl))
    {
        alert('cannot load xml from ' + xmlUrl);
        return;
    }
    
    var newId = xml.documentElement.getAttribute('id');

    var xslProcessor = xslTemplate.createProcessor();
    
    xslProcessor.input = xml;
   
    xslProcessor.transform();
    
    return(xslProcessor.output);
}

function browserXsltJavascript(xmlUrl, xslUrl)
{ 
    var xslStyleSheet = xmlRequestSynchron(xslUrl);
   
    if (!xslStyleSheet)
    {
        alert('cannot load xsl stylesheet from ' + xslUrl);
        return;
    }

    var xmlDoc = xmlRequestSynchron(xmlUrl);
   
    if (!xmlDoc)
    {
        alert('cannot load xml from ' + xmlUrl);
        return;
    }
    
    // browser-independend client-side XSL transformation with google ajaxslt 
       
    return(xsltProcess(xmlDoc, xslStyleSheet));
}

function getFormData(formObj) 
{
    var buff= '';
	
    var elemNum = formObj.elements.length;
	
    for (i = 0; i < elemNum; i++) 
    {
	    formElem = formObj.elements[i];

	    switch (formElem.type) 
	    {
	        case 'checkbox' :
	            if (formElem.checked)
	            {
	                buff += formElem.name + '=' + encodeURIComponent(formElem.value) + '&'
	            }
	      
	            break;
	      
	        case 'text':
	        case 'select-one':
	        case 'hidden':
	        case 'password':
	        case 'email':
	        case 'textarea':
	            buff += formElem.name + '=' + encodeURIComponent(formElem.value) + '&'
	            break;
	    }
    }
    
    return(buff);
}

function getPageYScrolled()
{
    if (!browserFirefox) 
    {
        return document.body.scrollTop;
    }
    
    return window.pageYOffset;
}

function getPageXScrolled()
{
    if (!browserFirefox) 
    {
        return document.body.scrollLeft;
    }
    
    return window.pageXOffset;
}

function showHourGlass()
{
    var waitDivElem = document.createElement('div');
    
    waitDivElem.setAttribute("id", "waitDiv");
    
    var hourGlassElem = document.createElement('img');
    
    hourGlassElem.setAttribute("src", "/webfilesys/images/hourglass.gif");
    hourGlassElem.setAttribute("width", "32");
    hourGlassElem.setAttribute("height", "32");
    hourGlassElem.setAttribute("border", "0");
    
    waitDivElem.appendChild(hourGlassElem);
    
    var divWidth = 60;
    var divHeight = 30;
    
    var windowWidth;
    var windowHeight;
    
    if (!browserFirefox) 
    {
        windowWidth = document.body.clientWidth;
        windowHeight = document.body.clientHeight;
    }
    else
    {
        windowWidth = window.innerWidth;
        windowHeight = window.innerHeight
    }    
    
    var xpos = getPageXScrolled() + (windowWidth - divWidth) / 2;
    var ypos = getPageYScrolled() + (windowHeight - divHeight) / 2;
    
    document.getElementsByTagName('body')[0].appendChild(waitDivElem);    
    
    waitDivElem.style.visibility = "hidden";
    waitDivElem.style.position = "absolute";
    waitDivElem.style.top = ypos + "px";
    waitDivElem.style.left = xpos + "px";
    waitDivElem.style.width = divWidth + "px";
    waitDivElem.style.height = divHeight + "px";
    waitDivElem.style.border = "1px solid navy";
    waitDivElem.style.backgroundColor = "ivory";
    waitDivElem.style.textAlign = "center";
    waitDivElem.style.verticalAlign = "middle";
    waitDivElem.style.zIndex = "10";
    waitDivElem.style.visibility = "visible";
}

function hideHourGlass()
{
    var waitDiv = document.getElementById("waitDiv");
    if (waitDiv)
    {
        document.getElementsByTagName('body')[0].removeChild(waitDiv);
    }
}

