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
    const autoRefreshCheckbox = document.getElementById('autoRefresh');
    if (autoRefreshCheckbox.checked) {
        startPollForChanges(500);
    } else {
        window.clearTimeout(pollTimeout);
    }
}

function pollForChange() {
    const parameters = {
        filePath: encodeURIComponent(pathForScript),
        lastModified,
        size: fileSize
    }
    xmlGetRequest("checkFileChange", parameters,
        responseXml => {
                const resultItem = responseXml.getElementsByTagName("result")[0];
                if (resultItem && resultItem.firstChild.nodeValue === "true") {
                    document.getElementById('tailForm').submit();
                } else {
                    pollTimeout = setTimeout(pollForChange, 3000);
                }
            },
        null,
        true
    );
}

