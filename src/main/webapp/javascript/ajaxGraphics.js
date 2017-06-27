function ajaxRotate(fileName, degrees, domId) {
    hideMenu();

    var xmlUrl = '/webfilesys/servlet?command=xformImage&action=rotate&degrees=' + degrees + '&imgName=' + encodeURIComponent(fileName) + "&domId=" + domId;

    var xslUrl = "/webfilesys/xsl/xformImageResult.xsl";

    var thumbCont = document.getElementById("thumbCont-" + fileName);
        
    if (thumbCont) {
        htmlFragmentByXslt(xmlUrl, xslUrl, thumbCont, null, true);
        
        var sizeSumElem = document.getElementById("sizeSum");
        if (sizeSumElem) {
            sizeSumElem.innerHTML = "";
        }
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
                var responseXml = xmlRequest(xmlUrl, autoImgRotateResult);
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

