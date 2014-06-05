function getUploadStatus()
{
    url = "/webfilesys/servlet?command=uploadStatus";

    resp = xmlRequestSynchron(url);

    var item = resp.getElementsByTagName("fileSize")[0];            

    var fileSize = item.firstChild.nodeValue;

    item = resp.getElementsByTagName("bytesUploaded")[0];            

    var bytesUploaded = item.firstChild.nodeValue;

    item = resp.getElementsByTagName("percent")[0];            

    var percent = item.firstChild.nodeValue;

    document.getElementById("statusText").innerHTML = bytesUploaded + " " + resourceLabelOf + " " + fileSize + " bytes (" + percent + "%)";

    document.getElementById("done").width = 3 * percent;

    if (browserMSIE) {
        // workaround for MSIE hanging on the upload status screen
        if (resp.getElementsByTagName("success")[0].firstChild.nodeValue == 'true') {
            window.location.href = '/webfilesys/servlet?command=listFiles';        
        }        
    }     
             
    window.setTimeout('getUploadStatus()',3000);
}

function existUploadTargetFile(targetFileName)
{
   return (ajaxRPC("existFile", encodeURIComponent(targetFileName)) == 'true'); 
}
