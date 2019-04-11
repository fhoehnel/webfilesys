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

function setEditorHeight() {
    if (browserMSIE) {
        setTimeout('setEditorHeightInternal()', 200);
    } else {
    	setEditorHeightInternal();
    }
}

function setEditorHeightInternal() {

    var buttonCont = document.getElementById("editorButtonCont");
    var buttonContYPos = getAbsolutePos(buttonCont)[1];

    if (buttonContYPos == 0) {
        var rect = buttonCont.getBoundingClientRect();
        buttonContYPos = rect.top;
    }

    var textArea = document.getElementById("editorText");
    var textAreaYPos = getAbsolutePos(textArea)[1];
    
    var textAreaHeight = buttonContYPos - textAreaYPos - 10;
    
    textArea.style.height = textAreaHeight + "px";
}

