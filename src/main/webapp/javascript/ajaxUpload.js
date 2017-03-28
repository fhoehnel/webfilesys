function getUploadStatus() {

    var url = "/webfilesys/servlet?command=uploadStatus";

    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var responseXml = req.responseXML;

                var item = responseXml.getElementsByTagName("fileSize")[0];            
                var fileSize = item.firstChild.nodeValue;

                item = responseXml.getElementsByTagName("bytesUploaded")[0];            
                var bytesUploaded = item.firstChild.nodeValue;

                item = responseXml.getElementsByTagName("percent")[0];            
                var percent = item.firstChild.nodeValue;

                document.getElementById("statusText").innerHTML = bytesUploaded + " " + resourceLabelOf + " " + fileSize + " bytes (" + percent + "%)";

                document.getElementById("done").width = 3 * percent;

                if (browserMSIE) {
                    // workaround for MSIE hanging on the upload status screen
                    if (responseXml.getElementsByTagName("success")[0].firstChild.nodeValue == 'true') {
                        window.location.href = '/webfilesys/servlet?command=listFiles';        
                    }        
                }     
             
                window.setTimeout('getUploadStatus()',3000);
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });
}

function checkUploadTargetExists(targetFileName, callback) {
    var url = "/webfilesys/servlet?command=ajaxRPC&method=existFile&param1=" + encodeURIComponent(targetFileName);
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var responseXml = req.responseXML;
                var resultItem = responseXml.getElementsByTagName("result")[0];
                var result = resultItem.firstChild.nodeValue;  
                
                if (result && (result == "true")) {
                    var confirmMsg = targetFileName + " - " + resourceBundle["upload.file.exists"];
                	customConfirm(confirmMsg, resourceBundle["button.cancel"], resourceBundle["button.ok"], 
                			function() {
                                callback();
                	        }
                	);
                    return;
                }

                callback();
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });          
}

function checkMultiUploadTargetExists(targetFileName, callbackRejected, callbackOk) {
    var url = "/webfilesys/servlet?command=ajaxRPC&method=existFile&param1=" + encodeURIComponent(targetFileName);
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
            
                var responseXml = req.responseXML;
                var resultItem = responseXml.getElementsByTagName("result")[0];
                var result = resultItem.firstChild.nodeValue;  
                
                if (result && (result == "true")) {
                    var confirmMsg = targetFileName + " - " + resourceBundle["upload.file.exists"];
                	customConfirm(confirmMsg, resourceBundle["button.cancel"], resourceBundle["button.ok"], 
                			function() {
                		        callbackOk();
                	        },
                			function() {
                	        	callbackRejected();
                	        }
                	);
                    return;
                }
                callbackOk();
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });          
}

