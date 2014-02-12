    function showMapSelection()
    {
        document.getElementById("mapIcon").style.display = "none";
        document.getElementById("geoLocSel").style.display = "block";
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

    function geoMapFileSelected(filePath) 
    {
        var mapSel = document.getElementById("geoLocSel");
    
        var idx = mapSel.selectedIndex;

        var mapType = mapSel.options[idx].value;

        mapSel.selectedIndex = 0;
        mapSel.style.display = "none";
        document.getElementById("mapIcon").style.display = "inline";

        if (mapType == "1")
        {
            var mapWin = window.open('/webfilesys/servlet?command=osMap&path=' + encodeURIComponent(filePath),'mapWin','status=no,toolbar=no,location=no,menu=no,width=600,height=400,resizable=yes,left=20,top=20,screenX=20,screenY=20');
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
            var mapWin = window.open('/webfilesys/servlet?command=googleMap&path=' + encodeURIComponent(filePath),'mapWin','status=no,toolbar=no,location=no,menu=no,width=600,height=400,resizable=yes,left=20,top=20,screenX=20,screenY=20');
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
