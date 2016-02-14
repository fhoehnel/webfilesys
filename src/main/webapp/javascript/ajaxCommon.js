var req;

function createAjaxRequest() {
    ajaxReq = false;
    
    if (window.XMLHttpRequest) {
    	try {
	        ajaxReq = new XMLHttpRequest();
        } catch (e) {
	        ajaxReq = false;
        }
    } else {
        // MSIE ActiveX
        if (window.ActiveXObject !== undefined) {
       	    try {
        	    ajaxReq = new ActiveXObject("Msxml2.XMLHTTP");
      	    } catch (e) {
        	    try {
          	        ajaxReq = new ActiveXObject("Microsoft.XMLHTTP");
        	    } catch (e) {
          	        ajaxReq = false;
        	    }
	        }
        }
    }
        
    if (!ajaxReq) {
        alert('Your browser does not support Ajax communication');
    }
	
	return ajaxReq;
}

function xmlRequest(url, callBackFunction) {
    req = createAjaxRequest();
        
    if (req) {
	    req.onreadystatechange = callBackFunction;
	    req.open("GET", url, true);
	    req.send("");
    } 
}

function xmlRequestPost(url, params, callBackFunction) {
    req = createAjaxRequest();
        
    if (req) {
	    req.onreadystatechange = callBackFunction;
	    req.open("POST", url, true);

        req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        
	    req.send(params);
    } 
}

function htmlFragmentByXslt(xmlUrl, xslUrl, fragmentCont, callback) {
    if (window.ActiveXObject !== undefined) {
        // MSIE  
        htmlFragmentByXsltMSIE(xmlUrl, xslUrl, fragmentCont, callback);
    } else {
        if (browserFirefox) { 
            htmlFragmentByXsltMozilla(xmlUrl, xslUrl, fragmentCont, callback);
        } else {
            if (browserChrome) {
                htmlFragmentByXsltMozilla(xmlUrl, xslUrl, fragmentCont, callback);
            } else if (browserSafari) {
                htmlFragmentByXsltMozilla(xmlUrl, xslUrl, fragmentCont, callback);
            } else {
                // XSLT with Javascript (google ajaxslt)
                htmlFragmentByXsltJavascript(xmlUrl, xslUrl, fragmentCont, callback);
            }
        }
    }
}

function htmlFragmentByXsltMozilla(xmlUrl, xslUrl, fragmentCont, callback) {

	xmlRequest(xslUrl, function() {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xslStyleSheet = req.responseXML;

	            xmlRequest(xmlUrl, function() {
                    if (req.readyState == 4) {
                        if (req.status == 200) {
			                var xmlDoc = req.responseXML;

                            var xsltProcessor = new XSLTProcessor();
       
                            xsltProcessor.importStylesheet(xslStyleSheet);

                            var result = xsltProcessor.transformToDocument(xmlDoc);
  
                            var xmlSerializer = new XMLSerializer();

                            fragmentCont.innerHTML = xmlSerializer.serializeToString(result);
                            
                            if (callback) {
                                callback();
                            }
                        } else {
                            alert('cannot load xml from ' + xmlUrl);
                        }
                    }
                });
            } else {
                alert('cannot load xsl stylesheet from ' + xslUrl);
            }
        }
    });
}

function htmlFragmentByXsltMSIE(xmlUrl, xslUrl, fragmentCont, callback) {
    fragmentCont.innerHTML = browserXsltMSIE(xmlUrl, xslUrl);
    
    if (callback) {
        callback();
    }
}

function htmlFragmentByXsltJavascript(xmlUrl, xslUrl, fragmentCont, callback) {

	xmlRequest(xslUrl, function() {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xslStyleSheet = req.responseXML;

	            xmlRequest(xmlUrl, function() {
                    if (req.readyState == 4) {
                        if (req.status == 200) {
			                var xmlDoc = req.responseXML;

                            // browser-independend client-side XSL transformation with google ajaxslt 
       
                            fragmentCont.innerHTML = xsltProcess(xmlDoc, xslStyleSheet);
                            
                            if (callback) {
                                callback();
                            }
                        } else {
                            alert('cannot load xml from ' + xmlUrl);
                        }
                    }
                });
            } else {
                alert('cannot load xsl stylesheet from ' + xslUrl);
            }
        }
    });
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

function showHourGlass() {
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
	
    waitDivElem.style.width = divWidth + "px";
    waitDivElem.style.height = divHeight + "px";

    document.getElementsByTagName('body')[0].appendChild(waitDivElem);    
	
	centerBox(waitDivElem);
	
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

