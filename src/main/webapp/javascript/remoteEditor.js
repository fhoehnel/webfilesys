function initRemoteEditor() {
    const urlParams = new URLSearchParams(window.location.search);
    const fileName = urlParams.get("fileName");
    const filePath = urlParams.get("filePath");
    const isLink = urlParams.get("isLink");

    let parameters;
    if (isLink) {
        parameters = { linkName: encodeURIComponent(fileName) };
    } else {
        parameters = { fileName: encodeURIComponent(fileName) };
    }

    fetchGet("checkTextFileSize", parameters,
        () => {
            setBundleResources();
            setEditorHeight();

            document.getElementById("headline").innerHTML = fileName;
            if (isLink) {
                document.getElementById("linkName").value = fileName;
            } else {
                document.getElementById("fileName").value = fileName;
            }

            fetchGet("getFile", parameters , responseData => {
                document.getElementById("editorText").innerHTML = responseData;
            });
        },
        () => customAlert(resourceBundle["alert.editFileSize"], null, () => self.close())
    );
}

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

    if (success === 'true') {
        const mobile = resultElem.getElementsByTagName("mobile")[0].firstChild.nodeValue;
        if (mobile === "true") {
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
    const textAreaHeight = buttonContYPos - textAreaYPos - 16;
    textArea.style.height = textAreaHeight + "px";
}

function cancelRemoteEdit() {
    if (window.opener) {
        self.close();
    } else {
        window.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList";
    }
}
