      var firefoxDragDrop = existFileReader();

      var SINGLE_FILE_MAX_SIZE;
      
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

              var file = selectedForUpload.pop();
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

          // there is a bug in Safari:
          // when multiple files are dropped onto the file input component,
          // the files-Array contains n times the first file, all the other files are missing
          // this results in only one single file added per drag/drop operation
          // see http://www.thecssninja.com/javascript/gmail-upload

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
                  alert(fileName + ': ' + resourceFileTooLarge);
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
                      
                              // firefox 3.6 only
                              // img.classList.add("uploadPreview");  
                      
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
                      
                      // Firefox 3.6 only
                      // listElem.classList.add("selectedFile");
                      listElem.className += (listElem.className ? " " : "") + "selectedFile";
                      
                      var listElemText = document.createTextNode(fileName);
                      listElem.appendChild(listElemText);
                      uploadFileList.appendChild(listElem);
                      
                      selectedForUpload.push(file);
                      
                      updateSelectedFileSize();
                  }
                  
                  document.getElementById('uploadButton').style.visibility = 'visible';
                  document.getElementById('uploadButton').style.display = 'inline';
                  document.getElementById('selectedForUpload').style.visibility = 'visible';
                  document.getElementById('selectedForUpload').style.display = 'block';
              }
          }  
      } 
      
      function updateSelectedFileSize() {
          var sizeSum = 0;
    	  for (var i = 0; i < selectedForUpload.length; i++) {
              if (browserSafari) {
            	  sizeSum += selectedForUpload[i].fileSize;
              } else {
            	  sizeSum += selectedForUpload[i].size;
              }
    	  }
    	  
    	  document.getElementById("selectedFilesSize").innerHTML = formatDecimalNumber(sizeSum) + " Bytes";
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
      
      function isPictureFile(fileType) {
          lowerCaseFileType = fileType.toLowerCase();
          
          return((lowerCaseFileType.indexOf("jpg") >= 0) ||
                 (lowerCaseFileType.indexOf("jpeg") >= 0) ||
                 (lowerCaseFileType.indexOf("gif") >= 0) ||
                 (lowerCaseFileType.indexOf("png") >= 0) ||
                 (lowerCaseFileType.indexOf("bmp") >= 0));
      }
      
      function checkUploadFileConflicts() {
    	  
    	  showHourGlass();
    	  
    	  var postData = "command=checkUploadConflict";
		  for (var i = 0; i < selectedForUpload.length; i++) {
	    	  postData += "&file=";
	          if (browserSafari) {
	    	      postData += selectedForUpload[i].fileName;
	          } else {
	    	      postData += selectedForUpload[i].name;
	          }
		  }
		  
		  xmlRequestPost("/webfilesys/servlet", postData, function(req) {
	          if (req.readyState == 4) {
	              if (req.status == 200) {
		              var conflicts = req.responseXML.getElementsByTagName("conflict");            

			          if (conflicts.length > 0) {

		                  var msg = resourceBundle["upload.conflictHead"];
			            	
			              msg += "<br/>"; 
			                
			              for (var i = 0; i < conflicts.length; i++) {
			              	  msg = msg + "<br/>" + conflicts[i].firstChild.nodeValue;
			              }

			              msg = msg + "<br/><br/>" + resourceBundle["upload.overwrite"];

		            	  hideHourGlass();
			              
			              customConfirm(msg, resourceBundle["button.cancel"], resourceBundle["button.ok"], sendFiles);
			          } else {
		            	  hideHourGlass();
			              sendFiles();
			          }
	              } else {
	            	  hideHourGlass();
	                  alert(resourceBundle["alert.communicationFailure"]);
	              }
	          }
	      });
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
		  
          var file = selectedForUpload.pop();
          
          if (file) {
              new singleFileBinaryUpload(file)
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
	  
          lastUploadedFile = fileName;
      
          document.getElementById("currentFile").innerHTML = shortText(fileName, 50);
          
          document.getElementById("statusText").innerHTML = "0 " + resourceOf + " " + formatDecimalNumber(fileSize) + " bytes ( 0%)";

          var statusWin = document.getElementById("uploadStatus");
          statusWin.style.visibility = 'visible';

          xhr = new XMLHttpRequest();  

          xhr.onreadystatechange = handleUploadState;
          xhr.upload.addEventListener("progress", updateProgress, false);
          xhr.upload.addEventListener("load", uploadComplete, false);

          xhr.open("POST", "/webfilesys/upload/singleBinary/" + encodeURIComponent(fileName), true);  

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

      function handleUploadState() {
          if (xhr.readyState == 4) {
              var statusWin = document.getElementById("uploadStatus");
              statusWin.style.visibility = 'hidden';

              if (xhr.status == 200) {
			  
			      totalLoaded += sizeOfCurrentFile;
			  
                  // start uploading the next file
                  var file = selectedForUpload.pop();
                  if (file) {
				      currentFileNum++;
                      var currentFileNumCont = document.getElementById("currentFileNum");
                      currentFileNumCont.innerHTML = currentFileNum;  
				  
                      new singleFileBinaryUpload(file)
                  } else {
                      if (firefoxDragDrop || uploadStartedByButton) {
                          window.location.href = '/webfilesys/servlet?command=listFiles&keepListStatus=true';
                      } else {
                          document.getElementById('lastUploadedFile').innerHTML = lastUploadedFile;
                          document.getElementById('lastUploaded').style.visibility = 'visible';
                          document.getElementById('lastUploaded').style.display = 'block';
                          document.getElementById('doneButton').style.visibility = 'visible';
                      }
                  }
              } else {
                  alert(resourceBundle["upload.error"] + " " + lastUploadedFile);
                  var file = selectedForUpload.pop();
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
                
              document.getElementById("statusText").innerHTML = formatDecimalNumber(e.loaded) + " " + resourceOf + " " + formatDecimalNumber(e.total) + " bytes (" + percent + "%)";

              document.getElementById("done").width = 3 * percent;

              document.getElementById("todo").width = 300 - (3 * percent);
			  
			  percent = Math.round(((totalLoaded + e.loaded) * 100) / totalSizeSum);

              document.getElementById("totalStatusText").innerHTML = formatDecimalNumber(totalLoaded + e.loaded) + " " + resourceOf + " " + formatDecimalNumber(totalSizeSum) + " bytes (" + percent + "%)";

              document.getElementById("totalDone").width = 3 * percent;

              document.getElementById("totalTodo").width = 300 - (3 * percent);
          }  
      }
      
      function uploadComplete(e) {
          document.getElementById("statusText").innerHTML = "100 %";

          document.getElementById("done").width = 300;

          document.getElementById("todo").width = 0;
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