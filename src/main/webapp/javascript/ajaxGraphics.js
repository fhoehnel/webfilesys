function ajaxRotate(fileName, degrees, domId) {
    hideMenu();

    const thumbCont = document.getElementById("thumbCont-" + fileName.replaceAll(" ", "_"));
    if (thumbCont) {
        const params = {
            action: "rotate",
            degrees,
            imgName: encodeURIComponent(fileName),
            domId
        }
        xmlGetRequest("xformImage", params, htmlFragment => {
            const fragment = htmlFragment.documentElement.outerHTML;
            thumbCont.innerHTML = fragment;
            const sizeSumElem = document.getElementById("sizeSum");
            if (sizeSumElem) {
                sizeSumElem.innerHTML = "";
            }
        });
    }
}

function autoImgRotate() {
	
	customConfirm(resourceBundle["confirm.rotateByExif"], resourceBundle["button.cancel"], resourceBundle["button.ok"], 
			function() {
                showHourGlass();
                
            	if (typeof stopPolling == "function") {
            		stopPolling();
                }
                
                var xmlUrl = '/webfilesys/servlet?command=autoImgRotate';
                xmlRequest(xmlUrl, autoImgRotateResult);
	});
}

function autoImgRotateResult(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
             var anyRotatedItem = req.responseXML.getElementsByTagName("anyImageRotated")[0];            
             var anyRotated = anyRotatedItem.firstChild.nodeValue;
                
             hideHourGlass();
                          
             if (anyRotated == "true") {
                 window.location.href = '/webfilesys/servlet?command=thumbnail';
                 return
             }
             
             customAlert(resourceBundle["rotateByExif.noop"]);
        } else {
            alert(resourceBundle["alert.communicationFailure"]);
        }
    }
}

function resetExifOrientation(imgPath) {
	
    const parameters = { "imgPath": imgPath };
    
	xmlPostRequest("resetExifOrientation", parameters, function(responseXml) {
	
        const resultElem = responseXml.getElementsByTagName("result")[0];            
        const success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;
        if (success === "true") {
          	customAlert(resourceBundle["resetExifOrientationSuccess"], null, function() {
          	    window.close();
            });
        } else {
           	customAlert(resourceBundle["resetExifOrientationFailed"]);
        }
	});
}
