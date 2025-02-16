let keepSearchResults = false;
let resultsDiscarded = false;

function appendSearchResult(filePath, viewLink, iconImg, distance) {
    const searchResult = document.createElement("li");

    const searchResultImg = document.createElement("span");
    searchResultImg.setAttribute("class", "icon-font fileIcon icon-file-camera");
    searchResult.appendChild(searchResultImg);

    const searchResultLink = document.createElement("a");
    searchResultLink.setAttribute("href", viewLink);
    searchResultLink.setAttribute("class", "fn");
    searchResultLink.setAttribute("target", "_blank");
    searchResult.appendChild(searchResultLink);

    /*
    const searchResultImg = document.createElement("img");
    searchResultImg.setAttribute("src", "icons/" + iconImg);
    searchResultLink.appendChild(searchResultImg);
     */

    const filePathText = document.createTextNode(filePath);
    searchResultLink.appendChild(filePathText);

    const distanceCont = document.createElement("span");
    distanceCont.setAttribute("class", "searchMatchInContext");
    distanceCont.innerHTML = distance + " km";
    searchResult.appendChild(distanceCont);

    document.getElementById("searchResultList").appendChild(searchResult);
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

function showResults() {
    keepSearchResults = true;

    if (mobile) {
        window.opener.location.href = "/webfilesys/servlet?command=mobile&cmd=folderFileList&absPath=" + searchResultDir;
    } else {
        window.opener.parent.DirectoryPath.location.href = "/webfilesys/servlet?command=exp&expandPath=" + searchResultDir + "&fastPath=true";
    }

    setTimeout("self.close()", 1000);
}
