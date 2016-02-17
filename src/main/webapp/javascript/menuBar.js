function enterDirectPath() {
    document.getElementById("directPath").value = "";
    document.getElementById("directPathCont").style.visibility = "visible";
	document.getElementById("directPath").focus();
}
  
function hideDirectPath() {
    document.getElementById("directPathCont").style.visibility = "hidden";
}
  
function gotoDirectPath() {
    var directPath = document.getElementById("directPath").value;
    
    if (directPath.length == 0) {
        alert('\"' + directPath + '\"\n' + resourceBundle["invalidDirectPath"]);
		return;
    }
    
    var url = "/webfilesys/servlet?command=ajaxRPC&method=existFolder&param1=" + encodeURIComponent(directPath);
    
    xmlRequest(url, function(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var responseXml = req.responseXML;
                var resultItem = responseXml.getElementsByTagName("result")[0];
                var result = resultItem.firstChild.nodeValue;            
                if (result == "true") {
	                if (directPath.length > 2) {
	                    if (directPath.charAt(1) == ':') {
	                        if ((directPath.charAt(0) < 'A') || (directPath.charAt(0) > 'Z')) {
			                    directPath = directPath.substring(0, 1).toUpperCase() + directPath.substring(1);
			                }
	                    }
	                }
	   
	                var expUrl = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(directPath) + "&mask=*&fastPath=true";
	  
	                hideDirectPath();
	  
	                window.parent.frames[1].location.href = expUrl;
                } else {
                    alert('\"' + directPath + '\"\n' + resourceBundle["invalidDirectPath"]);
                }
            } else {
                alert(resourceBundle["alert.communicationFailure"]);
            }
        }
    });
}
