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
    
    var folderExits = (directPath.length > 0)  && (ajaxRPC("existFolder", encodeURIComponent(directPath)) == 'true');
    
    if (!folderExits) {
        alert('\"' + directPath + '\"\n' + resourceBundle["invalidDirectPath"]);
		return;
	}
       
	if (directPath.length > 2) {
	    if (directPath.charAt(1) == ':') {
	        if ((directPath.charAt(0) < 'A') || (directPath.charAt(0) > 'Z')) {
			    directPath = directPath.substring(0, 1).toUpperCase() + directPath.substring(1);
			}
	    }
	}
	   
	var url = "/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(directPath) + "&mask=*&fastPath=true";
	  
	hideDirectPath();
	  
	window.parent.frames[1].location.href = url;
}
