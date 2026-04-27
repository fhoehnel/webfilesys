function getUploadStatus() {

    fetchGet("uploadStatus", {},
        responseText => {
            const response = JSON.parse(responseText);
            const fileSize = response.fileSize;
            const bytesUploaded = response.bytesUploaded;
            const percent = response.percent;

            let statusText;
            if (fileSize != "0") {
                statusText = bytesUploaded + " " + resourceLabelOf + " " + fileSize + " bytes (" + percent + "%)";
            } else {
                statusText = bytesUploaded  + " bytes";
            }

            document.getElementById("statusText").innerHTML = statusText;
            document.getElementById("done").width = 3 * percent;

            if (browserMSIE) {
                // workaround for MSIE hanging on the upload status screen
                if (response.success) {
                    window.location.href = '/webfilesys/servlet?command=listFiles';
                    return;
                }
            }
            window.setTimeout(() => getUploadStatus(), 3000);
        },
       	null,
       	true,
       	false
    );
}

function checkUploadTargetExists(targetFileName, callback) {
	
    const parameters = { "method": "existFile", "param1": encodeURIComponent(targetFileName) };
    
	xmlGetRequest("ajaxRPC", parameters, function(responseXml) {
        const resultItem = responseXml.getElementsByTagName("result")[0];
        const result = resultItem.firstChild.nodeValue;  
                
        if (result && (result === "true")) {
            var confirmMsg = targetFileName + " - " + resourceBundle["upload.file.exists"];
          	customConfirm(confirmMsg, resourceBundle["button.cancel"], resourceBundle["button.ok"], 
       			function() {
                    callback();
       	        }
           	);
            return;
        }
        callback();
    });          
}

