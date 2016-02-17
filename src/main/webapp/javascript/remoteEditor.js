function saveEditorContent(closeAfterSave) {
	var url = "/webfilesys/servlet?command=saveRemoteEditor";
	
	var formData = getFormData(document.getElementById("editForm"));
	
	if (closeAfterSave) {
		xmlRequestPost(url, formData, handleCloseAfterSave);
	} else {
		xmlRequestPost(url, formData, handleSaveResult);
	}
}

function handleSaveResult(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true') {
            	toast(resourceBundle["saveSuccess"], 1000);
            } else {
            	alert(resourceBundle["saveFailure"]);
            }
        }
    }
}

function handleCloseAfterSave(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true') {
                var mobile = resultElem.getElementsByTagName("mobile")[0].firstChild.nodeValue;
                if (mobile == "true") {
                    window.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList";
                } else {
            	    setTimeout("window.close()", 100);
                }
            } else {
            	alert(resourceBundle["saveFailure"]);
            }
        }
    }
}