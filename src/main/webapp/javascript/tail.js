var pollTimeout;        

function startAutoRefresh() {
    if (autoRefresh) {
        startPollForChanges(3000);
    }
}

function startPollForChanges(startDelay) {
    pollTimeout = setTimeout(pollForChange, startDelay);
}

function changeAutoRefresh() {
    var autoRefreshCheckbox = document.getElementById('autoRefresh');
    if (autoRefreshCheckbox.checked) {
        startPollForChanges(500);
    } else {
        window.clearTimeout(pollTimeout);
    }
}

function pollForChange() {
    var url = "/webfilesys/servlet?command=checkFileChange&filePath=" + encodeURIComponent(pathForScript) + "&lastModified=" + lastModified + "&size=" + fileSize;

    xmlRequest(url, function() {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var responseXml = req.responseXML;
                var resultItem = responseXml.getElementsByTagName("result")[0];            
    
                if (resultItem && (resultItem.firstChild.nodeValue == "true")) {
                    document.getElementById('tailForm').submit();
                } else {
                    pollTimeout = setTimeout(pollForChange, 3000);
                }
            } else {
                alert("communication failure");
            }
        }
    });
}

