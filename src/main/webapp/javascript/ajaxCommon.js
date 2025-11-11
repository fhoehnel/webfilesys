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

function xmlFetchPost(postData, successCallBack, failureCallBack) {
    showHourGlass();

    const requestOptions = {
        method: 'POST',
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
        body: postData
    };

    fetch("/webfilesys/servlet", requestOptions)
        .then((response) => {
            hideHourGlass();
            if (response.ok) {
                return response.text();
            }
            if (typeof failureCallback !== 'undefined') {
                failureCallback();
            } else {
                throw new Error('fetch communication error');
            }
        })
        .then((data) => {
            if (successCallBack) {
                const parser = new DOMParser();
                const xmlDoc = parser.parseFromString(data, 'text/xml');
                successCallBack(xmlDoc);
            }
        })
        .catch(error => {
            hideHourGlass();
            customAlert(resourceBundle["alert.communicationFailure"]);
            console.error("communication error:", error);
        });
}

function xmlGetRequest(command, parameters, successCallBack, failureCallBack) {
	showHourGlass();
    
    let url = "/webfilesys/servlet?command=" + command;
    for (const key in parameters) {
        url = url + "&" + key + "=" + parameters[key];
   	}
	
    fetch(url)
        .then((response) => {
            hideHourGlass();
            if (response.ok) {
                return response.text();
            }
            if (typeof failureCallback !== 'undefined') {
                failureCallback();
            } else {
                throw new Error('fetch communication error');
            }
        })
        .then((data) => {
            if (successCallBack) {
                const parser = new DOMParser();
                const xmlDoc = parser.parseFromString(data, 'text/xml');
                successCallBack(xmlDoc);
            }
        })
        .catch(error => {
            hideHourGlass();
            customAlert(resourceBundle["alert.communicationFailure"]);
            console.error("communication error:", error);
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

    const requestOptions = {
        method: 'POST',
        headers: {"Content-Type": "application/x-www-form-urlencoded"},
        body: postData
    };

    fetch("/webfilesys/servlet", requestOptions)
        .then((response) => {
            hideHourGlass();
            if (response.ok) {
                return response.text();
            }
            if (typeof failureCallback !== 'undefined') {
                failureCallback();
            } else {
                throw new Error('fetch communication error');
            }
        })
        .then((data) => {
            if (successCallBack) {
                const parser = new DOMParser();
                const xmlDoc = parser.parseFromString(data, 'text/xml');
                successCallBack(xmlDoc);
            }
        })
        .catch(error => {
            hideHourGlass();
            customAlert(resourceBundle["alert.communicationFailure"]);
            console.error("communication error:", error);
        });
}

function htmlFragmentByXslt(xmlUrl, xslUrl, fragmentCont, callback, replaceCont) {
    console.log("htmlFragmentByXsltJavascript");
    // XSLT with Javascript (google ajaxslt)
    htmlFragmentByXsltJavascript(xmlUrl, xslUrl, fragmentCont, callback, replaceCont);

    /*
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
    */
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

function getFormData(formObj) {
    let buff = "";
	
    const elemNum = formObj.elements.length;
	
    for (let i = 0; i < elemNum; i++) {
	    const formElem = formObj.elements[i];

	    switch (formElem.type) {
	        case 'checkbox':
	            if (formElem.checked) {
                    if (buff.length > 0) {
                        buff += "&";
                    }
                    buff += formElem.name + '=' + encodeURIComponent(formElem.value)
	            }
	            break;
	        case 'text':
	        case 'select-one':
	        case 'hidden':
	        case 'password':
	        case 'email':
	        case 'textarea':
                if (buff.length > 0) {
                    buff += "&";
                }
	            buff += formElem.name + '=' + encodeURIComponent(formElem.value);
	            break;
	    }
    }
    
    return(buff);
}

function getFormDataAsProps(formObj) {
    const formParams = {};
	
    const elemNum = formObj.elements.length;
	
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
    const waitDivElem = document.createElement('div');
    
    waitDivElem.setAttribute("id", "waitDiv");
    
    const hourGlassElem = document.createElement('img');
    hourGlassElem.setAttribute("src", "/webfilesys/images/hourglass.gif");
    waitDivElem.appendChild(hourGlassElem);

    document.getElementsByTagName('body')[0].appendChild(waitDivElem);    
	
	centerBox(waitDivElem);
	
    waitDivElem.style.visibility = "visible";
}

function hideHourGlass() {
    const waitDiv = document.getElementById("waitDiv");
    if (waitDiv) {
        document.getElementsByTagName('body')[0].removeChild(waitDiv);
    }
}

