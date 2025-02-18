var keepSearchResults = false;
var resultsDiscarded = false;

function showResults() {
	keepSearchResults = true;
            
    if (mobile) {
        window.opener.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + searchResultDir; 
    } else {
        window.opener.parent.DirectoryPath.location.href = "/webfilesys/servlet?command=exp&expandPath=" + searchResultDir + "&fastPath=true"; 
    }

	setTimeout("self.close()", 1000);
}
			
function discardSearchResults() {
    if (keepSearchResults || resultsDiscarded) {
        return;
    }

    const parameters = { "resultDir": searchResultDir };
    
	xmlPostRequest("discardSearchResults", parameters, function(responseXml) {
        resultsDiscarded = true;
        setTimeout("self.close()", 1000); 
    });
}

function discardAndClose() {
    discardSearchResults();
}
	
function gotoSearchResultFolder(folderPath) {
	if (window.opener) {
		window.opener.location.href = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(folderPath) + "&fastPath=true;"
	}
}

window.onbeforeunload = discardSearchResults;
			