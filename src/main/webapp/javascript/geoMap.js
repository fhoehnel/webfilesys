    function showMapSelection(counter)
    {
        if (counter) 
        {
            document.getElementById("mapIcon-" + counter).style.display = "none";
            document.getElementById("geoLocSel-" + counter).style.display = "block";
        }
        else 
        {
            document.getElementById("mapIcon").style.display = "none";
            document.getElementById("geoLocSel").style.display = "block";
        }
    }
      
    function geoMapFolderSelected(folderPath) 
    {
        var mapSel = document.getElementById("geoLocSel");
    
        var idx = mapSel.selectedIndex;

        var mapType = mapSel.options[idx].value;

    	mapSel.selectedIndex = 0;
    	
        mapSel.style.display = "none";
        document.getElementById("mapIcon").style.display = "inline";

        if (mapType == "1")
        {
            var mapWin = window.open('/webfilesys/servlet?command=osMap&path=' + encodeURIComponent(folderPath),'mapWin','status=no,toolbar=no,location=no,menu=no,width=600,height=400,resizable=yes,left=20,top=20,screenX=20,screenY=20');
            if (!mapWin) 
            {
            	alert(resourceBundle["alert.enablePopups"]);
            }
            else
            {
                mapWin.focus();
            }
        } 
        else if (mapType == "2")
        {
            var mapWin = window.open('/webfilesys/servlet?command=googleMap&path=' + encodeURIComponent(folderPath),'mapWin','status=no,toolbar=no,location=no,menu=no,width=600,height=400,resizable=yes,left=20,top=20,screenX=20,screenY=20');
            if (!mapWin) 
            {
            	alert(resourceBundle["alert.enablePopups"]);
            } 
            else
            {
                mapWin.focus();
            }
        } 
        else
        {
            window.location.href = "/webfilesys/servlet?command=googleEarthFolderPlacemark";
        }
    }  

    function geoMapFileSelected(filePath, counter) 
    {
        var mapSel;
        if (counter) 
        {
            mapSel = document.getElementById("geoLocSel-" + counter);
        }
        else 
        {
            mapSel = document.getElementById("geoLocSel");
        }
    
        var idx = mapSel.selectedIndex;

        var mapType = mapSel.options[idx].value;

        mapSel.selectedIndex = 0;
        
        var mapIcon;
        if (counter)
        {
            mapIcon = document.getElementById("mapIcon-" + counter)
        }
        else 
        {
            mapIcon = document.getElementById("mapIcon")
        }
        
        if (mapIcon) {
            mapSel.style.display = "none";
            mapIcon.style.display = "inline";
        }

        if (mapType == "1")
        {
            var mapWin = window.open('/webfilesys/servlet?command=osMap&path=' + encodeURIComponent(filePath),'_blank','status=no,toolbar=no,location=no,menu=no,width=600,height=400,resizable=yes,left=20,top=20,screenX=20,screenY=20');
            if (!mapWin) 
            {
            	alert(resourceBundle["alert.enablePopups"]);
            }
            else
            {
                mapWin.focus();
            }
        } 
        else if (mapType == "2")
        {
            var mapWin = window.open('/webfilesys/servlet?command=googleMap&path=' + encodeURIComponent(filePath),'_blank','status=no,toolbar=no,location=no,menu=no,width=600,height=400,resizable=yes,left=20,top=20,screenX=20,screenY=20');
            if (!mapWin) 
            {
            	alert(resourceBundle["alert.enablePopups"]);
            }
            else
            {
                mapWin.focus();
            }
        } 
        else
        {
            window.location.href = "/webfilesys/servlet?command=googleEarthPlacemark&path=" + filePath;
        }
    }  

    function showImageOnMap(picFileName) {
        removeImageFromMap();
    
    	var picContElem = document.createElement("div");
    	picContElem.id = "picOnMapCont";
    	picContElem.setAttribute("class", "picOnMap");
	    document.documentElement.appendChild(picContElem);
	    
	    var closeIconElem = document.createElement("img");
	    closeIconElem.setAttribute("src", "/webfilesys/images/winClose.gif");
        closeIconElem.setAttribute("style", "float:right");
        picContElem.appendChild(closeIconElem);
	    
        var picElem = document.createElement("img");
        picElem.id = "picOnMap";
	    picElem.onload = resizeAndShowPic;
    	picElem.setAttribute("class", "picOnMap");
        picElem.setAttribute("src", "/webfilesys/servlet?command=getFile&fileName=" + encodeURIComponent(picFileName));
        picContElem.appendChild(picElem);
        
	    centerBox(picContElem);
	    
	    picContElem.onclick = removeImageFromMap;
    }
    
    function resizeAndShowPic() {
    	var picOnMapElem = document.getElementById("picOnMap");
    	
    	var thumbDimensions = calculateAspectRatioFit(picOnMapElem.width, picOnMapElem.height, 400, 400);
    	picOnMapElem.style.width = thumbDimensions.width + "px";
    	picOnMapElem.style.height = thumbDimensions.height + "px";
    	picOnMapElem.style.display = 'inline';
    	
    	if (thumbDimensions.height < 399) {
        	var picContElem = document.getElementById("picOnMapCont");
        	picContElem.style.height = (thumbDimensions.height + 30) + "px";
    	}
    	if (thumbDimensions.width < 399) {
        	var picContElem = document.getElementById("picOnMapCont");
        	picContElem.style.width = thumbDimensions.width + "px";
    	}
    }
    
    function removeImageFromMap() {
    	var picContElem = document.getElementById("picOnMapCont");
    	if (picContElem) {
        	document.documentElement.removeChild(picContElem);
    	}
    }