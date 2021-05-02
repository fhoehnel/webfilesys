function mkdir(path) {  
    centeredDialog('/webfilesys/servlet?command=mkdirPrompt&path=' + encodeURIComponent(path), '/webfilesys/xsl/createFolder.xsl', 320, 190, function() {
        document.mkdirForm.NewDirName.focus();
        document.mkdirForm.NewDirName.select();
    });
}

function deleteDir(path, domId)
{
    deleteFolder(path, 'false');
}

function renameDir(path) {
    centeredDialog('/webfilesys/servlet?command=renDirPrompt&path=' + encodeURIComponent(path), '/webfilesys/xsl/renameDir.xsl', 320, 190, function() {
        document.mkdirForm.NewDirName.focus();
        document.mkdirForm.NewDirName.select();
    });
}

function showPictureThumbs() {
	window.location.href = "/webfilesys/servlet?command=mobile&cmd=folderPictures";
}

function zip(path)
{
    window.location.href="/webfilesys/servlet?command=zipDir&actPath=" + encodeURIComponent(path);
}

function paste(path)
{
    window.location.href="/webfilesys/servlet?command=pasteFiles&actpath=" + encodeURIComponent(path) + "&random=" + (new Date().getTime());
}

function statistics(path)
{
    statWin=open("/webfilesys/servlet?command=statistics&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=580,height=590");
    statWin.focus();
}

function search(path)
{
    searchWin=open("/webfilesys/servlet?command=search&actpath=" + encodeURIComponent(path),"Search","scrollbars=yes,resizable=yes,width=500,height=480,left=80,top=20,screenX=80,screenY=20");
    searchWin.focus();
    searchWin.opener=self;
}

function mkfile(path) {
    centeredDialog('/webfilesys/servlet?command=mkfilePrompt&path=' + encodeURIComponent(path), '/webfilesys/xsl/createFile.xsl', 320, 190, function() {
        document.mkfileForm.NewFileName.focus();
        document.mkfileForm.NewFileName.select();
    });
}

function upload(path)
{
    window.parent.frames['FileList'].location.href = "/webfilesys/servlet?command=uploadParms&actpath=" + encodeURIComponent(path);
}

function publish(path,mailEnabled)
{
    if (parent.mailEnabled == 'true')
    {
         publishWin=window.open("/webfilesys/servlet?command=publishForm&actPath=" + encodeURIComponent(path) + "&type=common","publish","status=no,toolbar=no,menu=no,width=620,height=550,resizable=yes,scrollbars=no,left=30,top=20,screenX=40,screenY=20");
    }
    else
    {
         publishWin=window.open("/webfilesys/servlet?command=publishParms&actPath=" + encodeURIComponent(path) + "&type=common","publish","status=no,toolbar=no,menu=no,width=620,height=290,resizable=yes,scrollbars=no,left=30,top=80,screenX=40,screenY=80");
    }

    publishWin.focus();
}

function description(path)
{
    window.location.href="/webfilesys/servlet?command=editMetaInf&relPath=" + encodeURIComponent(path) + "&geoTag=true";
}

function driveInfo(path)
{
    propWin=window.open("/webfilesys/servlet?command=driveInfo&path=" + encodeURIComponent(path) + "&random=" + (new Date().getTime()),"propWin","status=no,toolbar=no,location=no,menu=no,width=400,height=200,resizable=yes,left=100,top=200,screenX=100,screenY=200");
    propWin.focus();
}

function refresh(path)
{
    window.location.href="/webfilesys/servlet?command=refresh&path=" + encodeURIComponent(path);
}


function rights(path)
{
    window.location.href="/webfilesys/servlet?command=unixRights&actpath=" + encodeURIComponent(path) + "&isDirectory=true&random=" + (new Date()).getTime();
}

function deleteFolder(path, confirmed) {
	
    hideMenu();

    const parameters = { 
    	"path": encodeURIComponent(path),
    	"confirmed": confirmed
    };
        
    xmlPostRequest("deleteDir", parameters, function(responseXml) {
        const successItem = responseXml.getElementsByTagName("success")[0];            
        const success = successItem.firstChild.nodeValue;
             
        const messageItem = responseXml.getElementsByTagName("message")[0];            
        let message = "";
             
        if (messageItem.firstChild) {
            message = messageItem.firstChild.nodeValue;
        }
             
        if (success == "notEmpty") {
	        customConfirm(message, resourceBundle["button.cancel"], resourceBundle["button.ok"], () => deleteFolder(path, "true"));
        } else {
            if (success == "deleted") {
                window.location.href = '/webfilesys/servlet?command=mobile&cmd=folderFileList';
            } else {
                customAlert(path + '\n' + message);
            }
        }
    });
}

function pasteFromClipboard() {
	window.location.href = "/webfilesys/servlet?command=pasteFiles";
}

function pasteAsLink() {
	window.location.href = "/webfilesys/servlet?command=pasteLinks";	
}

function uploadParams() {
	window.location.href = "/webfilesys/servlet?command=uploadParms";	
}
