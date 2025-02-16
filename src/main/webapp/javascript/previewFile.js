var PREVIEW_PIC_SIZE = 400;

var filePreviewTimeout = null;

var filePreviewActive = false;

function addPreviewHandler() {
	
	$(".fn").each(function() {
		if (previewSupported($(this).text())) {
			
			$(this).mouseover(function() {
				const fileNameOrPath = insertDoubleBackslash($(this).text());
		   		var timeoutFunctionCall = "previewSearchResult('" + fileNameOrPath + "')";
		   		filePreviewTimeout = setTimeout(timeoutFunctionCall, 500);
			});			

			$(this).mouseout(function() {
				cancelSearchPreview();
			});
		}
	});
}

function previewSearchResult(fileNameOrPath) {
	filePreviewTimeout = null;

   	var filePreviewCont = document.createElement("div");
   	filePreviewCont.id = "filePreviewCont";
   	filePreviewCont.setAttribute("class", "filePreviewCont");
   	document.documentElement.appendChild(filePreviewCont);
   	
   	var previewPic = document.createElement("img");
   	
   	previewPic.onload = function() {

		var picOrigWidth = getNaturalWidth(previewPic);
		var picOrigHeight = getNaturalHeight(previewPic);
		
		if ((picOrigWidth > PREVIEW_PIC_SIZE) || (picOrigHeight > PREVIEW_PIC_SIZE)) {
			if (picOrigWidth > picOrigHeight) {
				previewPic.width = PREVIEW_PIC_SIZE;
				var scaledHeight = picOrigHeight * PREVIEW_PIC_SIZE / picOrigWidth;
				previewPic.height = scaledHeight;
				filePreviewCont.style.height = scaledHeight + "px";
			} else {
				previewPic.height = PREVIEW_PIC_SIZE;
				var scaledWidth =  picOrigWidth * PREVIEW_PIC_SIZE / picOrigHeight;
				previewPic.width = scaledWidth;
				filePreviewCont.style.width = scaledWidth + "px";
			}
		} else {
			filePreviewCont.style.height = picOrigHeight + "px";
			filePreviewCont.style.width = picOrigWidth + "px";
		}

		previewPic.style.display = "inline";
	};

	if (fileNameOrPath.indexOf('/') >= 0 || fileNameOrPath.indexOf('\\') >= 0) {
	    previewPic.src = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(fileNameOrPath) + "&cached=true";
	} else {
	    previewPic.src = "/webfilesys/servlet?command=getFile&fileName=" + encodeURIComponent(fileNameOrPath) + "&cached=true";
	}
   	filePreviewCont.appendChild(previewPic);
   	
    filePreviewActive = true;
}

function cancelSearchPreview() {
	if (filePreviewTimeout) {
		clearTimeout(filePreviewTimeout);
		return;
	}
	
	if (!filePreviewActive) {
		return;
	}
	
	document.documentElement.removeChild(document.getElementById("filePreviewCont"));

	filePreviewActive = false;
}


function previewSupported(fileName) {
	var fileNameExt = getFileNameExt(fileName);
	
	return fileNameExt == ".JPG"  || fileNameExt == ".JPEG" || fileNameExt == ".PNG" || fileNameExt == ".GIF" || fileNameExt == ".BMP";
	
}

