function visibilityChangeHandler() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
	}
	if (document["visibilityState"] == "visible") {
		pollForFolderTreeChanges(true);
	} 
}

function pollForFolderTreeChanges(immediateRefreshView) {
	
    xmlGetRequest("pollForFolderTreeChange", {}, responseXml => {
       	if (pollingTimeout) {
        	clearTimeout(pollingTimeout);
        }
        const item = responseXml.getElementsByTagName("result")[0];
        const result = item.firstChild.nodeValue;
        if (result == "true") {
           	if (immediateRefreshView) {
           	    window.location.href = "/webfilesys/servlet?command=exp";
           	} else {
               	customConfirm(resourceBundle["folderTreeModified"], resourceBundle["label.no"], resourceBundle["label.yes"],
                    () => window.location.href = "/webfilesys/servlet?command=exp",
                   	() => {
                   		closeAlert();
                       	if (pollingTimeout) {
                       		clearTimeout(pollingTimeout);
                       	}
                       	pollingTimeout = setTimeout(pollForFolderTreeChanges, pollInterval);
                   	}
                );
           	}
        } else {
           	pollingTimeout = setTimeout(pollForFolderTreeChanges, pollInterval);
        }
    });
}

function delayedPollForFolderTreeChanges() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
	}
	pollingTimeout = setTimeout(pollForFolderTreeChanges, pollInterval);	
}

function stopPolling() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
	}
}