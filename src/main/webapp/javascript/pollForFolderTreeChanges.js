function visibilityChangeHandler() {
	if (pollingTimeout) {
		clearTimeout(pollingTimeout);
	}
	if (document["visibilityState"] == "visible") {
		pollForFolderTreeChanges(true);
	} 
}

function pollForFolderTreeChanges(immediateRefreshView) {
	
	var pollUrl = "/webfilesys/servlet?command=pollForFolderTreeChange";
	
	xmlRequest(pollUrl, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
            	if (pollingTimeout) {
            		clearTimeout(pollingTimeout);
            	}
                var item = req.responseXML.getElementsByTagName("result")[0];            
                var result = item.firstChild.nodeValue;
                if (result == "true") {
                	if (immediateRefreshView) {
                	    window.location.href = "/webfilesys/servlet?command=exp";
                	} else {
                    	customConfirm(resourceBundle["folderTreeModified"], resourceBundle["label.no"], resourceBundle["label.yes"], 
                        	function() {
                                window.location.href = "/webfilesys/servlet?command=exp";
                        	},
                        	function() {
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
            } else {
            	if (window.console) {
                    console.log(resourceBundle["alert.communicationFailure"]);
            	}
            }
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