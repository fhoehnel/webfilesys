var keepSearchResults = false;
var resultsDiscarded = false;

function showResults() {
	keepSearchResults = true;
            
    if (mobile) {
        window.opener.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + searchResultDir; 
    } else {
        window.opener.parent.DirectoryPath.location.href = "/webfilesys/servlet?command=exp&expand=" + searchResultDir + "&fastPath=true"; 
    }

	setTimeout("self.close()", 1000);
}
			
function discardSearchResults() {
    if (keepSearchResults || resultsDiscarded) {
        return;
    }

    var discardURL = "/webfilesys/servlet?command=discardSearchResults&resultDir=" + searchResultDir;
    
	xmlRequest(discardURL, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                resultsDiscarded = true;
                setTimeout("self.close()", 1000); 
            } else {
                alert('communication error');
            }
        }
    });
}

function discardAndClose() {
    discardSearchResults();
}
			
window.onbeforeunload = discardSearchResults;
			