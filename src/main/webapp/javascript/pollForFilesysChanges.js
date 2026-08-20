function visibilityChangeHandler() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
	}
	if (document["visibilityState"] == "visible") {
		pollForDirChanges(true);
	} 
}

function pollForDirChanges(immediateRefreshView) {
	
	var fileFilter = document.getElementById("fileMask").value;
	
    const params = {
        lastDirStatusTime: dirModified,
        lastSizeSum: fileSizeSum,
        mask: fileFilter
    };
    if (typeof pollThumbs !== 'undefined') {
        params.thumbnails = "true";
    }

    xmlGetRequest("pollForDirChange", params,
        responseXml => {
            if (pollingTimeout) {
                clearTimeout(pollingTimeout);
            }
            const item = responseXml.getElementsByTagName("result")[0];
            const result = item.firstChild.nodeValue;
            if (result === "true") {
                if (immediateRefreshView) {
                    window.location.href = "/webfilesys/servlet?command=listFiles";
                } else {
                    customConfirm(resourceBundle["folderContentModified"], resourceBundle["label.no"], resourceBundle["label.yes"],
                        function() {
                            window.top.frames[2].location.href = "/webfilesys/servlet?command=listFiles";
                        },
                        function() {
                            closeAlert();
                            if (pollingTimeout) {
                                clearTimeout(pollingTimeout);
                            }
                            pollingTimeout = setTimeout(pollForDirChanges, pollInterval);
                        }
                    );
                }
            } else {
                pollingTimeout = setTimeout(pollForDirChanges, pollInterval);
            }
        },
        () => {
            console.warn("pollForDirChange failed");
            redirectToLogin();
        },
        true
    );
}

function delayedPollForDirChanges() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
	}
	pollingTimeout = setTimeout(pollForDirChanges, pollInterval);	
}

function stopPolling() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
	}
}