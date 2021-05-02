function editMetaInfo(fileName)
{
    window.location.href = "/webfilesys/servlet?command=editMetaInf&fileName=" + encodeURIComponent(fileName) + "&mobile=true&random=" + new Date().getTime();
}

function comments(path)
{ 
    window.location.href = '/webfilesys/servlet?command=listComments&actPath=' + encodeURIComponent(path);
}

function viewZip(path)
{
    unzipWin=window.open("/webfilesys/servlet?command=viewZip&filePath=" + encodeURIComponent(path),"unzipWin","status=no,toolbar=no,menu=yes,width=500,height=580,resizable=yes,scrollbars=yes,left=100,top=40,screenX=100,screenY=40");
    unzipWin.focus();
}

function zipFile(path)
{
    window.location.href="/webfilesys/servlet?command=zipFile&filePath=" + encodeURIComponent(path);
}

function openUrlFile(path) {
    var urlWin = window.open("/webfilesys/servlet?command=openUrlFile&actPath=" + encodeURIComponent(path),"_blank","status=yes,toolbar=yes,menubar=yes,location=yes,resizable=yes,scrollbars=yes");
    urlWin.focus();
}


function editMP3(path)
{
    window.location.href = "/webfilesys/servlet?command=editMP3&path=" + encodeURIComponent(path);
}

function renameFile(fileName) {   
    centeredDialog('/webfilesys/servlet?command=renameFilePrompt&mobile=true&fileName=' + encodeURIComponent(fileName), '/webfilesys/xsl/renameFile.xsl', 340, 160, function() {
        document.renameForm.newFileName.focus();
        const newFileName = document.renameForm.newFileName.value;
        if (newFileName) {
	        const extStart = newFileName.lastIndexOf(".");
            if (extStart > 0) {
	            document.renameForm.newFileName.setSelectionRange(0, extStart);
            } else {
                document.renameForm.newFileName.select();
            }
        }
    });
}

function copyToClipboard(fileName)
{
    cutCopyToClip(fileName, 'copy');
}

function cutToClipboard(fileName)
{
    cutCopyToClip(fileName, 'move');
}

function editRemote(fileName)
{
    window.location.href = '/webfilesys/servlet?command=mobile&cmd=editFile&filename=' + encodeURIComponent(fileName) + '&screenHeight=' + screen.height;
}

function downloadFile(path) {
    window.location.href = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(path) + "&disposition=download";
}


function viewFile(path)
{
    var lowerCasePath = path.toLowerCase();

    if (((lowerCasePath.indexOf('.jpeg') >= 0) && (lowerCasePath.lastIndexOf('.jpeg') == path.length - 5)) || 
        ((lowerCasePath.indexOf('.jpg') >= 0) && (lowerCasePath.lastIndexOf('.jpg') == path.length - 4)))
    {
        var windowWidth;
        var windowHeigth;
        if (document.all)
        {  
            windowWidth = document.body.clientWidth;
            windowHeight = document.body.clientHeight;
        }
        else
        {  
            windowWidth = self.innerWidth;
            windowHeight = self.innerHeight;
        }

        showImage(path, windowWidth, windowHeight);
        return;
    } 

    var viewPath = "";
    
    if (path.charAt(0) == '/')
    {
       viewPath = '/webfilesys/servlet' + URLEncode(path);
    }
    else
    {
       viewPath = '/webfilesys/servlet/' + URLEncode(path);
    }
    
    // window.open(viewPath,"_blank","status=yes,toolbar=yes,menubar=yes,location=yes,resizable=yes,scrollbars=yes");

    window.location.href = viewPath;
}

function showImage(imgPath, windowWidth, windowHeight)
{
    var url = '/webfilesys/servlet?command=mobile&cmd=showImg&imgPath=' + encodeURIComponent(imgPath);
    
    if (windowWidth)
    {
        url = url + '&windowWidth=' + windowWidth  + '&windowHeight=' + windowHeight;
    }

    window.location.href = url;
}

function delFile(fileName) {
    centeredDialog('/webfilesys/servlet?command=ajaxRPC&method=deleteFilePrompt&param1=' + encodeURIComponent(fileName),
                   '/webfilesys/xsl/mobile/confirmDeleteFile.xsl', 
                   320, 130);
}

function deleteFile(fileName)
{
    window.location.href = "/webfilesys/servlet?command=fmdelete&fileName=" + fileName + "&deleteRO=yes&mobile=true";
}

function accessRights(path)
{
    rightWin = window.open("/webfilesys/servlet?command=unixRights&actpath=" + encodeURIComponent(path) + "&isDirectory=false&random=" + (new Date()).getTime(),"rightWin","status=no,toolbar=no,menu=no,resizable=yes,scrollbars=yes,height=500,width=350,left=300,top=100,screenX=300,screenY=100");
    rightWin.focus();
}

function sendFile(fileName) {
    centeredDialog('/webfilesys/servlet?command=emailFilePrompt&fileName=' + encodeURIComponent(fileName), '/webfilesys/xsl/emailFile.xsl', 400, 240, function() {
        document.emailForm.receiver.focus();
        document.emailForm.receiver.select();
    });
}

function delLink(linkName)
{
    window.location.href="/webfilesys/servlet?command=deleteLink&linkName=" + encodeURIComponent(linkName);
}

function switchReadWrite(path) {  
    centeredDialog('/webfilesys/servlet?command=switchReadWrite&filePath=' + encodeURIComponent(path), '/webfilesys/xsl/switchReadWrite.xsl', 360, 190);
}

function associatedProg(path)
{
    parent.parent.menu.document.getElementById('download').src="/webfilesys/servlet?command=runAssociatedProgram&filePath=" + encodeURIComponent(path);
}

function URLEncode(path)
{
    var encodedPath = '';

    for (i = 0; i < path.length; i++)
    {
        c = path.charAt(i);
    
        if (c == '/')
        {
            encodedPath = encodedPath + c;
        }
        else if (c == '\\')
        {
            encodedPath = encodedPath + '/';
        }
        else
        {
            encodedPath = encodedPath + encodeURIComponent(c);
        }
    }
    
    return(encodedPath); 
}




