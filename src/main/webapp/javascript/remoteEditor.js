function saveEditorContent(closeAfterSave) {
	const formData = getFormData(document.getElementById("editForm"));
	if (closeAfterSave) {
        xmlFetchPost(formData, handleCloseAfterSave);
	} else {
        xmlFetchPost(formData, handleSaveResult);
	}
}

function handleSaveResult(xmlDoc) {
    const resultElem = xmlDoc.getElementsByTagName("result")[0];
    const success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

    if (success == 'true') {
       	toast(resourceBundle["saveSuccess"], 1000);
    } else {
       	alert(resourceBundle["saveFailure"]);
    }
}

function handleCloseAfterSave(xmlDoc) {
    const resultElem = xmlDoc.getElementsByTagName("result")[0];
    const success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

    if (success == 'true') {
        const mobile = resultElem.getElementsByTagName("mobile")[0].firstChild.nodeValue;
        if (mobile == "true") {
            window.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList";
        } else {
      	    setTimeout("window.close()", 100);
        }
    } else {
     	alert(resourceBundle["saveFailure"]);
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
    const buttonCont = document.getElementById("editorButtonCont");
    let buttonContYPos = getAbsolutePos(buttonCont)[1];
    if (buttonContYPos == 0) {
        const rect = buttonCont.getBoundingClientRect();
        buttonContYPos = rect.top;
    }
    const textArea = document.getElementById("editorText");
    const textAreaYPos = getAbsolutePos(textArea)[1];
    const textAreaHeight = buttonContYPos - textAreaYPos - 10;
    textArea.style.height = textAreaHeight + "px";
}

