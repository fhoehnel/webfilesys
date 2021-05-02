function xmlRequest(url, callBackFunction) {
    const req = new XMLHttpRequest();
    req.onreadystatechange = function() {callBackFunction(req)};
    req.open("GET", url, true);
	req.send("");
}

function xmlRequestPost(url, params, callBackFunction) {
    const req = new XMLHttpRequest();
    req.onreadystatechange = function() {callBackFunction(req)};
    req.open("POST", url, true);
    req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	req.send(params);
}

function xmlGetRequest(command, parameters, successCallBack, failureCallBack) {
	showHourGlass();
    
    let url = "/webfilesys/servlet?command=" + command;
    for (const key in parameters) {
        url = url + "&" + key + "=" + parameters[key];
   	}
	
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
            	successCallBack(req.responseXML);
                hideHourGlass();
            } else {
            	hideHourGlass();
            	if (typeof failureCallback !== 'undefined') {
            		failureCallback();
            	} else {
                    customAlert(resourceBundle["alert.communicationFailure"]);
            	}
            }
        }
    });
}

function xmlPostRequest(command, parameters, successCallBack, failureCallBack) {
	showHourGlass();

    let postData = "";
    if (command) {
	    postData = "command=" + command;
    }
    for (const key in parameters) {
    	postData = postData + (postData.length > 0 ? "&" : "") + key + "=" + parameters[key];
   	}
	
	xmlRequestPost("/webfilesys/servlet", postData, function(req) {
		
        if (req.readyState == 4) {
            if (req.status == 200) {
            	successCallBack(req.responseXML);
                hideHourGlass();
            } else {
            	hideHourGlass();
            	if (typeof failureCallback !== 'undefined') {
            		failureCallback();
            	} else {
                    customAlert(resourceBundle["alert.communicationFailure"]);
            	}
            }
        }
    });
}

function htmlFragmentByXslt(xmlUrl, xslUrl, fragmentCont, callback, replaceCont) {
    if (window.ActiveXObject !== undefined) {
        // MSIE  
        htmlFragmentByXsltMSIE(xmlUrl, xslUrl, fragmentCont, callback, replaceCont);
    } else {
        if (browserFirefox) { 
            htmlFragmentByXsltMozilla(xmlUrl, xslUrl, fragmentCont, callback, replaceCont);
        } else {
            if (browserChrome) {
                htmlFragmentByXsltMozilla(xmlUrl, xslUrl, fragmentCont, callback, replaceCont);
            } else if (browserSafari) {
                htmlFragmentByXsltMozilla(xmlUrl, xslUrl, fragmentCont, callback, replaceCont);
            } else {
                // XSLT with Javascript (google ajaxslt)
                htmlFragmentByXsltJavascript(xmlUrl, xslUrl, fragmentCont, callback, replaceCont);
            }
        }
    }
}

function htmlFragmentByXsltMozilla(xmlUrl, xslUrl, fragmentCont, callback, replaceCont) {

	xmlRequest(xslUrl, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xslStyleSheet = req.responseXML;

	            xmlRequest(xmlUrl, function(req) {
                    if (req.readyState == 4) {
                        if (req.status == 200) {
			                var xmlDoc = req.responseXML;

                            var xsltProcessor = new XSLTProcessor();
       
                            xsltProcessor.importStylesheet(xslStyleSheet);

                            var result = xsltProcessor.transformToDocument(xmlDoc);
  
                            var xmlSerializer = new XMLSerializer();

                            var newDomFragment = xmlSerializer.serializeToString(result);

                            if (replaceCont) {
                                fragmentCont.outerHTML = newDomFragment;
                            } else {
                                fragmentCont.innerHTML = newDomFragment;
                            }
                            
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

function htmlFragmentByXsltMSIE(xmlUrl, xslUrl, fragmentCont, callback, replaceCont) {
    var newDomFragment = browserXsltMSIE(xmlUrl, xslUrl);

    if (replaceCont) {
        fragmentCont.outerHTML = newDomFragment;
    } else {
        fragmentCont.innerHTML = newDomFragment;
    }
    
    if (callback) {
        callback();
    }
}

function htmlFragmentByXsltJavascript(xmlUrl, xslUrl, fragmentCont, callback, replaceCont) {

	xmlRequest(xslUrl, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var xslStyleSheet = req.responseXML;

	            xmlRequest(xmlUrl, function(req) {
                    if (req.readyState == 4) {
                        if (req.status == 200) {
			                var xmlDoc = req.responseXML;

                            // browser-independend client-side XSL transformation with google ajaxslt 

                            var newDomFragment = xsltProcess(xmlDoc, xslStyleSheet);

                            if (replaceCont) {
                                fragmentCont.outerHTML = newDomFragment;
                            } else {
                                fragmentCont.innerHTML = newDomFragment;
                            }
                            
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

function getFormDataAsProps(formObj) {
    const formParams = {};
	
    var elemNum = formObj.elements.length;
	
    for (let i = 0; i < elemNum; i++) {
	    let formElem = formObj.elements[i];

	    switch (formElem.type) {
	        case 'checkbox' :
	            if (formElem.checked) {
                    formParams[formElem.name] = encodeURIComponent(formElem.value);
	            }
	      
	            break;
	      
	        case 'text':
	        case 'select-one':
	        case 'hidden':
	        case 'password':
	        case 'email':
	        case 'textarea':
                formParams[formElem.name] = encodeURIComponent(formElem.value);
	            break;
	    }
    }
    
    return formParams;
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
    waitDivElem.appendChild(hourGlassElem);

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

