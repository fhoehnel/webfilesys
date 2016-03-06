var selectedForUpload = new Array();

var MAX_PICTURE_SIZE_SUM = 40000000;

var MAX_BLOG_TEXT_LENGTH = 4096;
      
var xhr;
      
var lastUploadedFile;
      
var currentFileNum = 1;
	  
var totalSizeSum = 0;
	  
var totalLoaded = 0;
	  
var sizeOfCurrentFile = 0;

var pictureFileSize = 0;

var firefoxDragDrop = existFileReader();

var SINGLE_FILE_MAX_SIZE;

var uploadStartedByButton = false;

var publicUrl = null;
      
if (browserFirefox)
{
    // SINGLE_FILE_MAX_SIZE = 134217728;
    SINGLE_FILE_MAX_SIZE = 500000000;
}
else 
{
    SINGLE_FILE_MAX_SIZE = 999999999;
}

function existFileReader()
{
    try
    {
        var featureTest = new FileReader();
        if (featureTest != null) 
        {
            return true;
        } 
    }
    catch (Exception)
    {
    }
      
    return false;
}        

function isPictureFile(fileType) {
    lowerCaseFileType = fileType.toLowerCase();
          
    return((lowerCaseFileType.indexOf("jpg") >= 0) ||
           (lowerCaseFileType.indexOf("jpeg") >= 0) ||
           (lowerCaseFileType.indexOf("gif") >= 0) ||
           (lowerCaseFileType.indexOf("png") >= 0) ||
           (lowerCaseFileType.indexOf("bmp") >= 0));
}

function selectedDuplicate(fileName) {
    for (var i = 0; i < selectedForUpload.length; i++) {
        var selectedFileName;
        if (browserSafari) {
            selectedFileName = selectedForUpload[i].fileName
        } else {
            selectedFileName = selectedForUpload[i].name
        }
      
        if (selectedFileName == fileName) {
            return true;
        }  
    }
          
    return false;
}

function prepareDropZone() {
    if (browserSafari) {
        // in Safari files can be dropped only to the file input component, not to a div
        return;
    }
      
    var dropZone;
    dropZone = document.getElementById("dropZone"); 
    dropZone.addEventListener("mouseover", hideHint, false);      
    dropZone.addEventListener("mouseout", showHint, false);  
    dropZone.addEventListener("dragenter", dragenter, false);  
    dropZone.addEventListener("dragover", dragover, false);  
    dropZone.addEventListener("drop", drop, false);      
}
    
function dragenter(e) {  
    e.stopPropagation();  
    e.preventDefault();  
}  
  
function dragover(e) {  
    e.stopPropagation();  
    e.preventDefault();  
}     
    
function drop(e) { 
    e.stopPropagation();  
    e.preventDefault();  
          
    var dt = e.dataTransfer;  
    var files = dt.files;  

    if (firefoxDragDrop)
    {
        handleFiles(files);  
    }
    else
    {   
        positionStatusDiv();

        var fileNum = files.length;
  
        for (var i = 0; i < fileNum; i++) { 
             selectedForUpload.push(files[i]);
        }

        var file = selectedForUpload.shift();
        if (file) {
            new singleFileBinaryUpload(file); 
        }
    }
}     
  
function showHint() {
    var hintText = document.getElementById("dragDropHint");
    if (hintText != null) {
        hintText.style.visibility = 'visible';  
    }
}
  
function hideHint() {
    var hintText = document.getElementById("dragDropHint");
    if (hintText != null) {
        hintText.style.visibility = 'hidden';  
    }
}

function handleFiles(files) {  
    var dropZone = document.getElementById("dropZone");  
    var uploadFileList = document.getElementById("uploadFiles");

    for (var i = 0; i < files.length; i++) {  
        var file = files[i];  
              
        var fileName;
        var fileSize;
              
        if (browserSafari) {
            fileName = file.fileName;
            file.size = file.fileSize;
        } else {
            fileName = file.name
            fileSize = file.size;
        }
              
        if (file.size > SINGLE_FILE_MAX_SIZE) {
            alert(fileName + ': ' + resourceBundle["blog.uploadFileTooLarge"]);
        } else {
            if (!selectedDuplicate(fileName)) {
                if (!browserSafari) {
                    var hintText = document.getElementById("dragDropHint");
                    if (hintText) {
                        dropZone.removeChild(hintText);
                    }
                }

                if (firefoxDragDrop && isPictureFile(file.type)) {  
                      
                    if (pictureFileSize < MAX_PICTURE_SIZE_SUM) {
                        var img = document.createElement("img");  
                      
                        img.className += (img.className ? " " : "") + "uploadPreview";
                      
                        img.file = file;  
                        dropZone.appendChild(img);  
     
                        var reader = new FileReader();  
                        reader.onload = (function(aImg) { return function(e) { aImg.src = e.target.result; }; })(img);  
                        reader.readAsDataURL(file);  
                              
                        pictureFileSize += file.size;
                     }
                } 
                      
                var listElem = document.createElement("li");
                      
                listElem.className += (listElem.className ? " " : "") + "selectedForUpload";
                      
                var listElemText = document.createTextNode(fileName);
                listElem.appendChild(listElemText);
                uploadFileList.appendChild(listElem);
                      
                selectedForUpload.push(file);
            }

            /*
            document.getElementById('selectedForUpload').style.visibility = 'visible';
            document.getElementById('selectedForUpload').style.display = 'block';
            */
        }
    }  
} 

function submitPost() {
    var blogText = document.getElementById("blogText").value;

    if ((selectedForUpload.length == 0) && (trim(blogText).length == 0)) {
        alert(resourceBundle["blog.emptyPost"]);
        return;
    }
    
    if (trim(blogText).length == 0) {
        if (!confirm(resourceBundle["blog.confirmSendEmptyText"])) {
            return;
        }
    } else if (trim(blogText).length > MAX_BLOG_TEXT_LENGTH) {
        alert(resourceBundle["blog.textTooLong"]);
        return;
    }
    
    var geoDataSwitcher = document.getElementById("blogGeoDataSwitcher");
    
    if (geoDataSwitcher && geoDataSwitcher.checked) {
        var latitude = parseFloat(document.getElementById("latitude").value);

        if (isNaN(latitude) || (latitude < (-90.0)) || (latitude > 90.0)) {
            alert(resourceBundle["error.latitudeInvalid"]);
            return;  
        }    

        var longitude = parseFloat(document.getElementById("longitude").value);

        if (isNaN(longitude) || (longitude < (-180.0)) || (longitude > 180.0)) {
            alert(resourceBundle["error.longitudeInvalid"]);
            return;  
        }    
    }
    
    if (selectedForUpload.length > 0) {
        positionStatusDiv();
        sendFiles();
    } else {
        document.getElementById("blogForm").submit();
    }   
}

function positionStatusDiv()
{
    var statusDiv = document.getElementById("uploadStatus");

    var statusDivWidth = statusDiv.offsetWidth;
    var statusDivHeight = statusDiv.offsetHeight; 

    var windowWidth;
    var windowHeight;
    var yScrolled;
    var xScrolled = 0;

    if (browserFirefox) 
    {
        windowWidth = window.innerWidth;
        windowHeight = window.innerHeight;
        yScrolled = window.pageYOffset;
        xScrolled = window.pageXOffset;
    }
    else 
    {
        windowWidth = document.body.clientWidth;
        windowHeight = document.body.clientHeight;
        yScrolled = document.body.scrollTop;
    }

    statusDiv.style.top = ((windowHeight - statusDivHeight) / 2 + yScrolled) + 'px';
    statusDiv.style.left = ((windowWidth - statusDivWidth) / 2 + xScrolled) + 'px';
}

function hideBrowserSpecifics()
{
    document.getElementById('lastUploaded').style.visibility = 'hidden';
    document.getElementById('lastUploaded').style.display = 'none';
    /*
    document.getElementById('selectedForUpload').style.visibility = 'hidden';
    document.getElementById('selectedForUpload').style.display = 'none';
    */
          
    if (browserSafari)
    {
        document.getElementById('dropTarget').style.visibility = 'hidden';
        document.getElementById('dropTarget').style.display = 'none';
    }
}

function sendFiles() {  
    uploadStartedByButton = true;

    var filesToUploadNumCont = document.getElementById("filesToUploadNum");

    filesToUploadNumCont.innerHTML = selectedForUpload.length;  
		  
	for (var i = 0; i < selectedForUpload.length; i++) {
	    if (browserSafari) {
	        totalSizeSum += selectedForUpload[i].fileSize;
	    } else {
		    totalSizeSum += selectedForUpload[i].size;
		}
	}
		  
    var file = selectedForUpload.shift();
          
    if (file) {
        singleFileBinaryUpload(file);
    }
} 
      
function singleFileBinaryUpload(file) {
      
    var fileName;
    var fileSize;
    if (browserSafari) {
        fileName = file.fileName;
        fileSize = file.fileSize;
    } else {
        fileName = file.name
        fileSize = file.size;
    }
      
    sizeOfCurrentFile = fileSize;
	  
	checkMultiUploadTargetExists(fileName, 
	    function() {
            var nextFile = selectedForUpload.shift();
            if (nextFile) {
                new singleFileBinaryUpload(nextFile)
            }
	    }, 
	    function() {
            lastUploadedFile = fileName;
      
            document.getElementById("currentFile").innerHTML = shortText(fileName, 50);
          
            document.getElementById("statusText").innerHTML = "0 " + resourceBundle["label.of"] + " " + formatDecimalNumber(fileSize) + " bytes ( 0%)";

            var statusWin = document.getElementById("uploadStatus");
            statusWin.style.visibility = 'visible';

            var now = new Date();

            var serverFileName = document.getElementById("dateYear").value + "-" +
                                 document.getElementById("dateMonth").value + "-" +
                                 document.getElementById("dateDay").value + "-" +
                                 now.getTime() + "-" + currentFileNum + 
                                 getFileNameExt(fileName).toLowerCase();
                         
            var firstUploadServerFileName = document.getElementById("firstUploadFileName");
            if (firstUploadServerFileName.value.length == 0) {
                firstUploadServerFileName.value = serverFileName;
            }

            var uploadUrl = "/webfilesys/upload/singleBinary/blog/" + serverFileName; 

            xhr = new XMLHttpRequest();  

            xhr.onreadystatechange = handleUploadState;
            xhr.upload.addEventListener("progress", updateProgress, false);
            xhr.upload.addEventListener("load", uploadComplete, false);

            xhr.open("POST", uploadUrl, true);  

	        if (!browserMSIE) {
                xhr.overrideMimeType('text/plain; charset=x-user-defined-binary');  
	        }
         
            if (firefoxDragDrop) {
                try {
                    xhr.sendAsBinary(file.getAsBinary());    
                } catch (ex) {
                    // Chrome has no file.getAsBinary() function
                    xhr.send(file);
                }
            } else {
                xhr.send(file);
            }    
	    }
	);
}

function handleUploadState() {
    if (xhr.readyState == 4) {
        var statusWin = document.getElementById("uploadStatus");
        statusWin.style.visibility = 'hidden';

        if (xhr.status == 200) {
			  
            totalLoaded += sizeOfCurrentFile;
			  
            // start uploading the next file
            var file = selectedForUpload.shift();
            if (file) {
		        currentFileNum++;
                var currentFileNumCont = document.getElementById("currentFileNum");
                currentFileNumCont.innerHTML = currentFileNum;  
				  
                new singleFileBinaryUpload(file)
            } else {
                if (firefoxDragDrop || uploadStartedByButton) {
                    document.getElementById("blogForm").submit();
                } else {
                    document.getElementById('lastUploadedFile').innerHTML = lastUploadedFile;
                    document.getElementById('lastUploaded').style.visibility = 'visible';
                    document.getElementById('lastUploaded').style.display = 'block';
                    document.getElementById('doneButton').style.visibility = 'visible';
                }
            }
        } else {
            alert(resourceBundle["upload.error"] + " " + lastUploadedFile);
            var file = selectedForUpload.shift();
            if (file) {
		        currentFileNum++;
                var currentFileNumCont = document.getElementById("currentFileNum");
                currentFileNumCont.innerHTML = currentFileNum;  
                new singleFileBinaryUpload(file)
			}
        }
    }
}

function updateProgress(e) {
    if (e.lengthComputable) {  
        var percent = Math.round((e.loaded * 100) / e.total);  
                
        document.getElementById("statusText").innerHTML = formatDecimalNumber(e.loaded) + " " + resourceBundle["label.of"] + " " + formatDecimalNumber(e.total) + " bytes (" + percent + "%)";

        document.getElementById("done").width = 3 * percent;

        document.getElementById("todo").width = 300 - (3 * percent);
			  
        percent = Math.round(((totalLoaded + e.loaded) * 100) / totalSizeSum);

        document.getElementById("totalStatusText").innerHTML = formatDecimalNumber(totalLoaded + e.loaded) + " " + resourceBundle["label.of"] + " " + formatDecimalNumber(totalSizeSum) + " bytes (" + percent + "%)";

        document.getElementById("totalDone").width = 3 * percent;

        document.getElementById("totalTodo").width = 300 - (3 * percent);
    }  
}
      
function uploadComplete(e) {
    document.getElementById("statusText").innerHTML = "100 %";

    document.getElementById("done").width = 300;

    document.getElementById("todo").width = 0;
}
      
function returnToList() {
    if (confirm(resourceBundle["blog.confirmCancel"])) {

        var returnURL = '/webfilesys/servlet?command=blog&cmd=list';
    
        var posInPage = document.getElementById("posInPage");
    
        if (posInPage && (posInPage.value.length > 0)) {
            returnURL = returnURL + "&random=" + ((new Date()).getTime()) + "#entry-" + posInPage.value;
        }
    
        window.location.href = returnURL;
    }
}

function editBlogEntry(fileName, posInPage) {
    window.location.href = "/webfilesys/servlet?command=blog&cmd=editEntry&fileName=" + encodeURIComponent(fileName) + "&posInPage=" + posInPage;
}

function jsComments(path) {
    var commentWin = window.open("/webfilesys/servlet?command=listComments&actPath=" + encodeURIComponent(path),"commentWin","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=550,height=400,resizable=yes,left=80,top=100,screenX=80,screenY=100");
    commentWin.focus();
}

function deleteBlogEntry(fileName) {
    if (!confirm(resourceBundle["blog.confirmDelete"])) {
        return;
    }

    showHourGlass();
    
    var url = "/webfilesys/servlet?command=blog&cmd=deleteEntry&fileName=" + encodeURIComponent(fileName);
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var responseXml = req.responseXML;
    
                var success = null;
                var successItem = responseXml.getElementsByTagName("success")[0];
                if (successItem) {
                    success = successItem.firstChild.nodeValue;
                }         
    
                hideHourGlass();    
    
                if (success == "deleted") {
                    window.location.href = "/webfilesys/servlet?command=blog";
                } else {
                    alert(resourceBundle["blog.deleteError"]);
                }
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
                hideHourGlass();    
            }
        }
    });
}

function moveBlogEntryUp(fileName, posInPage) {
    moveBlogEntry(fileName, "up", posInPage);
}

function moveBlogEntryDown(fileName, posInPage) {
    moveBlogEntry(fileName, "down", posInPage);
}

function moveBlogEntry(fileName, direction, posInPage) {

    showHourGlass();
    
    var url = "/webfilesys/servlet?command=blog&cmd=moveEntry&fileName=" + encodeURIComponent(fileName) + "&direction=" + direction;
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var responseXml = req.responseXML;
    
                var success = null;
                var successItem = responseXml.getElementsByTagName("success")[0];
                if (successItem) {
                    success = successItem.firstChild.nodeValue;
                }         
    
                hideHourGlass();    
    
                if (success == "true") {
                    if ((direction == "up") && (posInPage > 1)) {
                        posInPage--;
                    } else if (direction == "down") {
                        posInPage++;
                    }
    
                    window.location.href = "/webfilesys/servlet?command=blog&random=" + (new Date().getTime()) + "#entry-" + posInPage;
                } else {
                    alert(resourceBundle["blog.moveError"]);
                }
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
                hideHourGlass();    
            }
        }
    });
}

function loadGoogleMapsAPIScriptCode() {
    var script = document.createElement("script");
    script.type = "text/javascript";
    script.src = "http://maps.google.com/maps/api/js?sensor=false&callback=handleGoogleMapsApiReady";
    document.body.appendChild(script);
}
  
function handleGoogleMapsApiReady() {
    // console.log("Google Maps API loaded");
}
  
var posMarker;
  
function selectLocation() {
    var markerPos = posMarker.getPosition();
  
    document.blogForm.latitude.value = markerPos.lat(); 
    document.blogForm.longitude.value = markerPos.lng();
       
    hideMap();
}
  
function showMap(selectLocation) {
    document.getElementById("mapFrame").style.display = 'block';

    var latitude = document.blogForm.latitude.value;

    var coordinatesNotYetSelected = false;

    if (latitude == '') {
        coordinatesNotYetSelected = true;
            
        if (selectLocation) {
            latitude = '51.1';
        } else {
            alert(resourceBundle["alert.missingLatitude"]);
            return;
        }
    }
  
    var longitude = document.blogForm.longitude.value;

    if (longitude == '') {
        coordinatesNotYetSelected = true;

        if (selectLocation) {
            longitude = '13.76';
        } else {
            alert(resourceBundle["alert.missingLongitude"]);
            return;
        }
    }

    var zoomFactor = parseInt(document.blogForm.zoomFactor[document.blogForm.zoomFactor.selectedIndex].value);
      
    var infoText;

    if (selectLocation) {
        infoText = resourceBundle["label.hintGoogleMapSelect"];
    } else {
        infoText = document.blogForm.infoText.value;
    }        
      
    var mapCenter = new google.maps.LatLng(latitude, longitude);
    
    var myOptions = {
        zoom: zoomFactor,
        center: mapCenter,
        mapTypeId: google.maps.MapTypeId.HYBRID
    }
      
    var map = new google.maps.Map(document.getElementById("map"), myOptions);      
          
    if (selectLocation) {
        document.getElementById("selectButton").style.visibility = 'visible';
    }

    var markerPos = new google.maps.LatLng(latitude, longitude);

    posMarker = new google.maps.Marker({
        position: markerPos,
    });

    posMarker.setMap(map);
        
    if ((selectLocation && coordinatesNotYetSelected) ||
        (!selectLocation && (infoText != ''))) {
        var infowindow = new google.maps.InfoWindow({
            content: '<div style="width:160px;height:40px;overflow-x:auto;overflow-y:auto">' + infoText + '</div>'
        });

        infowindow.open(map, posMarker);
    }    
        
    google.maps.event.addListener(map, 'click', function(event) {
        var clickedPos = event.latLng;
        posMarker.setPosition(clickedPos);
        // map.setCenter(clickedPos);
    });        

    centerBox(document.getElementById("mapFrame"));

    document.getElementById("mapFrame").style.visibility = 'visible';
}  

function hideMap() {
    document.getElementById("selectButton").style.visibility = 'hidden';
    document.getElementById("mapFrame").style.visibility = 'hidden';
    document.getElementById("mapFrame").style.display = 'none';
}

function toggleGeoData(checkbox) {

    var geoDataCont = document.getElementById("blogGeoTagCont");

    if (checkbox.checked) {
        geoDataCont.style.display = "block";
    } else {
        geoDataCont.style.display = "none";
    }
}

function publishBlog() {
    var publishCont = document.getElementById("publishCont");
        
    if (!publishCont) {
        alert('publishCont is not defined');
        return;
    }
        
    var xmlUrl = "/webfilesys/servlet?command=blog&cmd=publishForm";
        
    var xslUrl = "/webfilesys/xsl/blog/publishBlog.xsl";    
        
    htmlFragmentByXslt(xmlUrl, xslUrl, publishCont, function() {
        setBundleResources();
        centerBox(publishCont);
        publishCont.style.visibility = "visible";
    });
}

function hidePublishForm() {
    var publishCont = document.getElementById("publishCont");
        
    if (!publishCont) {
        alert('publishCont is not defined');
        return;
    }

    publishCont.style.visibility = "hidden";
}

function validatePublishFormAndSubmit() {
    var daysPerPage = parseInt(document.getElementById("daysPerPage").value);

    if (isNaN(daysPerPage) || (daysPerPage < 1) || (daysPerPage > 32)) {
        alert(resourceBundle["blog.invalidDaysPerPageValue"]);
        document.getElementById("daysPerPage").focus();
        return;
    }
    
    var expirationDays = parseInt(document.getElementById("expirationDays").value);

    if (isNaN(expirationDays) || (expirationDays < 1) || (expirationDays > 10000)) {
        alert(resourceBundle["blog.invalidExpirationDays"]);
        document.getElementById("expirationDays").focus();
        return;
    }
    
    if (document.getElementById("language").value.length == 0) {
        alert(resourceBundle["error.missingLanguage"]);
        document.getElementById("language").focus();
        return;
    }
    
	var formData = getFormData(document.getElementById("publishForm"));
	
	xmlRequestPost("/webfilesys/servlet", formData, showPublishResult);	
}

function showPublishResult(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true') {
                publicUrl = resultElem.getElementsByTagName("publicUrl")[0].firstChild.nodeValue;            

                document.getElementById("publishTable").innerHTML = "";

                var tableRow = document.createElement("tr");
                document.getElementById("publishTable").appendChild(tableRow);

                var tableCell = document.createElement("td");
                tableCell.setAttribute("class", "formParm1");
                tableCell.innerHTML = resourceBundle["blog.publicLink"] + ":";
                tableRow.appendChild(tableCell);

                tableRow = document.createElement("tr");
                document.getElementById("publishTable").appendChild(tableRow);

                tableCell = document.createElement("td");
                tableCell.setAttribute("class", "formParm2");
                tableCell.innerHTML = publicUrl;
                tableRow.appendChild(tableCell);

                tableRow = document.createElement("tr");
                document.getElementById("publishTable").appendChild(tableRow);

                tableCell = document.createElement("td");
                tableCell.style.paddingTop = "20px";
                tableCell.style.textAlign = "center";
                tableRow.appendChild(tableCell);

                var closeButton = document.createElement("input");
                closeButton.setAttribute("type", "button");
                closeButton.setAttribute("value", resourceBundle["button.closewin"]);
                closeButton.setAttribute("onclick", "hidePublishForm()");
                tableCell.appendChild(closeButton);
                
                document.getElementById("publishBlogButton").style.display = "none";                    
                document.getElementById("unpublishButton").style.display = "inline";
                document.getElementById("publicURLButton").style.display = "inline";

                centerBox(document.getElementById("publishCont"));
            }
        }
    }
}

function queryPublicLink() {
    var url = "/webfilesys/servlet?command=blog&cmd=getPublicURL";
    
    xmlRequest(url, handleQueryPublicLinkResult);
}

function handleQueryPublicLinkResult(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true') {
                publicUrl = resultElem.getElementsByTagName("publicUrl")[0].firstChild.nodeValue;   
                document.getElementById("publicURLButton").style.display = "inline";
                document.getElementById("unpublishButton").style.display = "inline";
            } else {
                if (document.getElementById("publishBlogButton")) {
                    document.getElementById("publishBlogButton").style.display = "inline";
                }
            }
        }
    }
}

function showPublicURL() {

    var publishCont = document.getElementById("publishCont");
        
    if (!publishCont) {
        alert('publishCont is not defined');
        return;
    }
    
    publishCont.innerHTML = "";
    
    var publishHead = document.createElement("div");
    publishHead.setAttribute("class", "promptHead");    
    publishHead.innerHTML = resourceBundle["blog.publishTitle"];
    publishCont.appendChild(publishHead);
    
    var publishTable = document.createElement("table");
    publishTable.id = "publishTable";
    publishTable.setAttribute("class", "blogPublishForm");    
    publishCont.appendChild(publishTable);
    
    var tableRow = document.createElement("tr");
    document.getElementById("publishTable").appendChild(tableRow);

    var tableCell = document.createElement("td");
    tableCell.setAttribute("class", "formParm1");
    tableCell.innerHTML = resourceBundle["blog.publicLink"] + ":";
    tableRow.appendChild(tableCell);

    tableRow = document.createElement("tr");
    document.getElementById("publishTable").appendChild(tableRow);

    tableCell = document.createElement("td");
    tableCell.setAttribute("class", "formParm2");
    tableCell.innerHTML = publicUrl;
    tableRow.appendChild(tableCell);

    tableRow = document.createElement("tr");
    document.getElementById("publishTable").appendChild(tableRow);

    tableCell = document.createElement("td");
    tableCell.style.paddingTop = "20px";
    tableCell.style.textAlign = "center";
    tableRow.appendChild(tableCell);

    var closeButton = document.createElement("input");
    closeButton.setAttribute("type", "button");
    closeButton.setAttribute("value", resourceBundle["button.closewin"]);
    closeButton.setAttribute("onclick", "hidePublishForm()");
    closeButton.style.marginBottom = "10px";
    tableCell.appendChild(closeButton);
    
    centerBox(publishCont);
    
    publishCont.style.visibility = "visible";
}

function unpublish() {
    if (!confirm(resourceBundle["blog.confirmUnpublish"])) {
        return;
    }
    
    var url = "/webfilesys/servlet?command=blog&cmd=unpublish";

    xmlRequest(url, handleUnpublishResult);
}

function handleUnpublishResult(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true') {
                document.getElementById("unpublishButton").style.display = "none";
                document.getElementById("publicURLButton").style.display = "none";
                document.getElementById("publishBlogButton").style.display = "inline";
                publicUrl = null;
            } else {
                alert("failed to unpublish blog");
            }
        }
    }
}

function blogComments(filePath, posInPage) {

    var commentCont = document.getElementById("commentCont");
    
    var xmlUrl = '/webfilesys/servlet?command=blog&cmd=listComments&filePath=' + encodeURIComponent(filePath);
    
    var xslUrl = '/webfilesys/xsl/blog/comments.xsl';

    htmlFragmentByXslt(xmlUrl, xslUrl, commentCont, function() {
        document.getElementById("posInPage").value = posInPage;
    
        setBundleResources();
    
        centerBox(commentCont);

        commentCont.style.visibility = "visible";
    });
}

function closeBlogComments() {
    var posInPage = document.getElementById("posInPage").value;
    var commentNewLabel = document.getElementById("newComment-" + posInPage);
    if (commentNewLabel) {
        commentNewLabel.style.display = 'none';
    }

    var commentCont = document.getElementById("commentCont");
    commentCont.style.visibility = "hidden";
}

function submitComment() {
    if (document.getElementById("newComment").value.length == 0) {
        alert(resourceBundle["blog.newCommentEmpty"]);
        return;
    }
    
    var commentCont = document.getElementById("commentCont");
    commentCont.style.visibility = "hidden";

    xmlRequestPost("/webfilesys/servlet", getFormData(document.getElementById("blogCommentForm")), showPostCommentResult);
}

function showPostCommentResult(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true') {
                // var picSectionId = document.getElementById("picSectionId").value;
                // window.location.href = "/webfilesys/servlet?command=blog&random=" + (new Date().getTime()) + "#" + picSectionId;

                var newCommentCount = resultElem.getElementsByTagName("newCommentCount")[0].firstChild.nodeValue;
                var posInPage = document.getElementById("posInPage").value;
                document.getElementById("comment-" + posInPage).innerHTML = newCommentCount;
                
                var commentNewLabel = document.getElementById("newComment-" + posInPage);
                if (commentNewLabel) {
                    commentNewLabel.style.display = 'none';
                }

                var commentCont = document.getElementById("commentCont");
                commentCont.style.visibility = "hidden";
                
                toast(resourceBundle["blog.commentAdded"], 2000);
            } else {
                alert("failed to create comment");
            }
        }
    }
}

function limitCommentText() { 
    var newComment = document.getElementById("newComment");
 
    if (newComment.value.length > 2048) {  
        newComment.value = newComment.value.substring(0, 2048);
    }
}
  
function confirmDelComments(filePath) {  
    if (!confirm(resourceBundle["confirm.delcomments"])) { 
        return;
    }
    
    showHourGlass();
    
    var url = "/webfilesys/servlet?command=blog&cmd=delComments&filePath=" + encodeURIComponent(filePath);
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
			    var responseXml = req.responseXML;
    
                var success = null;
                var successItem = responseXml.getElementsByTagName("success")[0];
                if (successItem) {
                    success = successItem.firstChild.nodeValue;
                }         
    
                if ((success != null) && (success == "true")) {
                    var posInPage = document.getElementById("posInPage").value;
                    document.getElementById("comment-" + posInPage).innerHTML = "0";
                
                    var commentNewLabel = document.getElementById("newComment-" + posInPage);
                    if (commentNewLabel) {
                        commentNewLabel.style.display = 'none';
                    }
    
                    var commentCont = document.getElementById("commentCont");
                    commentCont.style.visibility = "hidden";
                } else {
                    alert("failed to delete comments");
                }

                hideHourGlass();    
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
                hideHourGlass();    
            }
        }
    });
}

function showSubscribers() {
    var subscribeCont = document.getElementById("subscribeCont");

    var xmlUrl = "/webfilesys/servlet?command=blog&cmd=listSubscribers";
        
    var xslUrl = "/webfilesys/xsl/blog/subscriberList.xsl";    
        
    htmlFragmentByXslt(xmlUrl, xslUrl, subscribeCont, function() {
        setBundleResources();
        subscribeCont.style.visibility = "visible";
    });
}

function showSubscribeForm() {
    var subscribeCont = document.getElementById("subscribeCont");
    
    var subscriberEmail = document.getElementById("subscriberEmail");
    if (subscriberEmail) {
        subscriberEmail.value = "";
    }
    
    subscribeCont.style.visibility = "visible";
}

function hideSubscribeForm() {
    var subscribeCont = document.getElementById("subscribeCont");
    
    subscribeCont.style.visibility = "hidden";
}

function submitSubscription() {
    var subscriberEmail = document.getElementById("subscriberEmail");
    
    if (!validateEmail(subscriberEmail.value)) {
        alert(resourceBundle["blog.invalidSubscriberEmail"]);
        subscriberEmail.select();
        subscriberEmail.focus();
        return;
    }
    
    var formData = getFormData(document.getElementById("subscribeForm"));
	
	xmlRequestPost("/webfilesys/servlet", formData, handleSubscribeResult)	    
}

function handleSubscribeResult(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success == 'true') {
                toast(resourceBundle["blog.subscribeSuccess"], 3000);
            } else {
                alert(resourceBundle["blog.subscribeError"]);
            }
            hideSubscribeForm();
        }
    }
}

function subscribeKeyPress(e) {
    e = e || window.event;
    if (e.keyCode == 13) {
        submitSubscription();
        return false;
    }
    return true;
}

function showSettings() {

    var settingsCont = document.getElementById("settingsCont");
    
    var xmlUrl = "/webfilesys/servlet?command=blog&cmd=showSettings";
    
    var xslUrl = "/webfilesys/xsl/blog/settings.xsl";

    htmlFragmentByXslt(xmlUrl, xslUrl, settingsCont, function() {
        setBundleResources();
        centerBox(settingsCont);
        settingsCont.style.visibility = "visible";
    });
}

function hideSettings() {
    var settingsCont = document.getElementById("settingsCont");

    settingsCont.style.visibility = "hidden";
}

function validateSettingsForm() {
    
  	var daysPerPage = document.getElementById("daysPerPage").value;

    var pageSize = parseInt(daysPerPage);

    if ((daysPerPage == "") || isNaN(pageSize) || (pageSize < 1) || (pageSize > 64)) {
        alert(resourceBundle["blog.invalidDaysPerPage"]);
    	return;
    }
    
    var newPassword = document.getElementById("newPassword").value;
    
    if ((newPassword.length > 0) && (newPassword.length < 5)) {
        alert(resourceBundle["error.passwordlength"]);
        return;
    }

    var newPasswdConfirm = document.getElementById("newPasswdConfirm").value;
    
    if (newPassword != newPasswdConfirm) {
        alert(resourceBundle["error.pwmissmatch"]);
        return;
    }

    showHourGlass();

    xmlRequestPost("/webfilesys/servlet", getFormData(document.getElementById("blogSettingsForm")), showSaveSettingsResult);
}

function showSaveSettingsResult(req) {
    if (req.readyState == 4) {
        if (req.status == 200) {
            var resultElem = req.responseXML.getElementsByTagName("result")[0];            
            var success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;

            if (success != 'true') {
                alert("failed to save settings");
            }

            var pageSizeChanged = resultElem.getElementsByTagName("pageSizeChanged")[0].firstChild.nodeValue;
            var blogTitleChanged = resultElem.getElementsByTagName("blogTitleChanged")[0].firstChild.nodeValue;

            var settingsCont = document.getElementById("settingsCont");
            settingsCont.style.visibility = "hidden";
            
            hideHourGlass();

            if ((pageSizeChanged && (pageSizeChanged == "true")) || (blogTitleChanged && (blogTitleChanged == "true"))) {
                window.location.href = "/webfilesys/servlet?command=blog";
            }
        }
        hideHourGlass();
    }
}

function setCalendarStyles() {
    if (browserFirefox) {
        var calendarCssElem = document.getElementById("calendarStyle");
        calendarCssElem.innerHTML = getCalStyles();
    }
}
   
function selectDate() {
    cal1x.setReturnFunction("setSelectedDate");
    cal1x.select(document.getElementById("blogDate"), "anchorDate", "MM/dd/yyyy");
    centerBox(document.getElementById("calDiv"));
}

function setSelectedDate(y, m, d) { 
    document.getElementById("dateDay").value = LZ(d);        
    document.getElementById("dateMonth").value = LZ(m);        
    document.getElementById("dateYear").value = y;        
            
    var selectedDate = new Date();
    selectedDate.setYear(y);
    selectedDate.setMonth(m - 1);
    selectedDate.setDate(d);
        
    var now = new Date();
           
    if (selectedDate.getTime() - (24 * 60 * 60 * 1000) > now.getTime()) {
        alert(resourceBundle["blog.dateInFuture"]);
    }
        
    var options = {year: 'numeric', month: '2-digit', day: '2-digit' };
       
    var language = (navigator.language || navigator.browserLanguage).split('-')[0];
       
    document.getElementById("blogDate").value = selectedDate.toLocaleDateString(language, options);
}

function rotateBlogPic(imgName, direction) {

    showHourGlass();

    var xmlUrl = "/webfilesys/servlet?command=blog&cmd=rotate&imgName=" + imgName + "&direction=" + direction;

	xmlRequest(xmlUrl, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var successItem = req.responseXML.getElementsByTagName("success")[0];            
                var success = successItem.firstChild.nodeValue;
             
                if (success == 'true') {
                    window.location.href = "/webfilesys/servlet?command=blog";
                } else {
                    alert(resourceBundle["blog.rotateError"]);
                }
                hideHourGlass();
            } else {
                hideHourGlass();
                alert(resourceBundle["blog.rotateError"]);
            }
        }
    });   
}

function setTitlePic(imgName) {
    if (!confirm(resourceBundle["blog.confirmSetTitlePic"])) {
        return;
    }
     
    showHourGlass();

    var xmlUrl = "/webfilesys/servlet?command=blog&cmd=setTitlePic&imgName=" + imgName;

	xmlRequest(xmlUrl, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var successItem = req.responseXML.getElementsByTagName("success")[0];            
                var success = successItem.firstChild.nodeValue;
             
                if (success == 'true') {
                    window.location.href = "/webfilesys/servlet?command=blog";
                } else {
                    alert(resourceBundle["blog.setTitlePicError"]);
                }
                hideHourGlass();
            } else {
                hideHourGlass();
                alert(resourceBundle["blog.setTitlePicError"]);
            }
        }
    });   
}

function unsetTitlePic() {
    if (!confirm(resourceBundle["blog.confirmUnsetTitlePic"])) {
        return;
    }
    showHourGlass();

    var xmlUrl = "/webfilesys/servlet?command=blog&cmd=unsetTitlePic";

	xmlRequest(xmlUrl, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var successItem = req.responseXML.getElementsByTagName("success")[0];            
                var success = successItem.firstChild.nodeValue;
             
                if (success == 'true') {
                    window.location.href = "/webfilesys/servlet?command=blog";
                } else {
                    alert(resourceBundle["blog.setTitlePicError"]);
                }
                hideHourGlass();
            } else {
                hideHourGlass();
                alert(resourceBundle["blog.setTitlePicError"]);
            }
        }
    });   
}

function firefoxJumpToIdWorkaround() {
   // Firefox bug 645075
   if (browserFirefox) {
       if (location.href.indexOf('#') > -1) {
           if (location.href.length > location.href.indexOf('#') + 1) {
               var currentEntryId = location.href.substring(location.href.indexOf('#') + 1);
               var currentEntry = document.getElementById(currentEntryId);
               if (currentEntry) {
                   currentEntry.scrollIntoView();
               }
           }
       }        
   }
}
 
function switchEmojiSelection(textareaId) {
    var visible = (document.getElementById("emojiSelCont").style.display == "block");

    if (visible) {
        document.getElementById("emojiSelCont").style.display = "none";
    } else {
        var xmlUrl = "/webfilesys/servlet?command=blog&cmd=emojiList&textareaId=" + textareaId;
    
        var xslUrl = "/webfilesys/xsl/blog/emojiList.xsl";

        htmlFragmentByXslt(xmlUrl, xslUrl, document.getElementById("emojiSelCont"), function() {
            setBundleResources();
            document.getElementById("emojiSelCont").style.display = "block";
        });
    }
}

function insertEmoji(textAreaId, emojiName) {
    var textAreaField = document.getElementById(textAreaId);
    if (textAreaField) {
        var emojiPlaceHolder = " {" + emojiName + "} ";
        insertAtCursor(textAreaField, emojiPlaceHolder);
    }
}