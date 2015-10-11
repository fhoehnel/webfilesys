function description(path)
{
    var windowWidth = 600;
    var windowHeight = 300;
    
    var xpos = (screen.width - windowWidth) / 2;
    var ypos = (screen.height - windowHeight) / 2;

    descWin=window.open("/webfilesys/servlet?command=editMetaInf&path=" + encodeURIComponent(path) + "&random=" + new Date().getTime(),"descWin","status=no,toolbar=no,location=no,menu=no,width=" + windowWidth + ",height=" + windowHeight + ",resizable=yes,left=" + xpos + ",top=" + ypos + ",screenX=" + xpos + ",screenY=" + ypos);
    descWin.focus();
    descWin.opener=self;
}

function comments(path)
{ 
    var windowWidth = 550;
    var windowHeight = 500;
    if (windowHeight > screen.height - 120)
    {
        windowHeight = screen.height - 120;
    }
    
    var xpos = (screen.width - windowWidth) / 2;
    var ypos = (screen.height - windowHeight) / 2;

    commentWin = window.open("/webfilesys/servlet?command=listComments&actPath=" + encodeURIComponent(path),"commentWin","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=" + windowWidth + ",height=" + windowHeight + ",resizable=yes,left=" + xpos + ",top=" + ypos + ",screenX" + xpos + ",screenY=" + ypos);
    commentWin.focus();
}

function viewZip(path)
{
    unzipWin=window.open("/webfilesys/servlet?command=viewZip&filePath=" + encodeURIComponent(path),"unzipWin","status=no,toolbar=no,menu=yes,width=500,height=580,resizable=yes,scrollbars=yes,left=100,top=40,screenX=100,screenY=40");
    unzipWin.focus();
}

function hexView(fileName)
{
    var hexWin = window.open("/webfilesys/servlet?command=hexView&fileName=" + encodeURIComponent(fileName), "hexWin","status=no,toolbar=no,menu=yes,width=780,height=600,resizable=yes,scrollbars=yes,left=10,top=10,screenX=20,screenY=20");
    hexWin.focus();
}

function zip(path)
{
    window.location.href="/webfilesys/servlet?command=zipFile&filePath=" + encodeURIComponent(path);
}

function editMP3(path)
{
    mp3Win=window.open("/webfilesys/servlet?command=editMP3&path=" + encodeURIComponent(path) + "&random=" + new Date().getTime(),"mp3Win","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=500,height=380,resizable=yes,left=150,top=100,screenX=150,screenY=100");
    mp3Win.focus();
}

function renameFile(fileName)
{   
    showPrompt('/webfilesys/servlet?command=renameFilePrompt&fileName=' + encodeURIComponent(fileName), '/webfilesys/xsl/renameFile.xsl', 360);
    
    setBundleResources();
	
    document.renameForm.newFileName.focus();
    
    document.renameForm.newFileName.select();
}

function cloneFile(fileName)
{   
    showPrompt('/webfilesys/servlet?command=cloneFilePrompt&fileName=' + encodeURIComponent(fileName), '/webfilesys/xsl/cloneFile.xsl', 360);
    
    setBundleResources();

    document.renameForm.newFileName.focus();
    
    document.renameForm.newFileName.select();
}

function copyToClipboard(fileName)
{
    cutCopyToClip(fileName, 'copy');
}

function cutToClipboard(fileName)
{
    cutCopyToClip(fileName, 'move');
}

function addCopyToClipboard(fileName)
{
    cutCopyToClip(fileName, 'addCopy');
}

function addMoveToClipboard(fileName)
{
    cutCopyToClip(fileName, 'addMove');
}

function editRemote(fileName)
{
    var editWinWidth = screen.width - 80;
    var editWinHeight = screen.height - 70;

    if (editWinWidth > 800) 
    {
        editWinWidth = 800;
    }

    if (editWinHeight > 700) 
    {
        editWinHeight = 700;
    }
    
    editWin=window.open("/webfilesys/servlet?command=editFile&filename=" + encodeURIComponent(fileName) + "&screenHeight=" + editWinHeight,"editWin","status=no,toolbar=no,location=no,menu=no,width=" + editWinWidth + ",height=" + editWinHeight + ",resizable=yes,left=20,top=5,screenX=20,screenY=5");
    editWin.focus();
    editWin.opener=self;
}

function viewFile(path)
{
    var viewPath = "";
    
    if (path.charAt(0) == '/')
    {
       viewPath = '/webfilesys/servlet' + encodeURI(path);
    }
    else
    {
       viewPath = '/webfilesys/servlet/' + URLEncode(path);
    }
    
    window.open(viewPath,"_blank","status=yes,toolbar=yes,menubar=yes,location=yes,resizable=yes,scrollbars=yes");
}

function downloadFile(path) {
    window.location.href = "/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(path) + "&disposition=download";
}

function execNativeProgram(path) {
    window.location.href = "/webfilesys/servlet?command=execProgram&progname=" + encodeURIComponent(path);
}

function openUrlFile(path) {
    var urlWin = window.open("/webfilesys/servlet?command=openUrlFile&actPath=" + encodeURIComponent(path),"_blank","status=yes,toolbar=yes,menubar=yes,location=yes,resizable=yes,scrollbars=yes");
    urlWin.focus();
}

function tail(path)
{
    window.open('/webfilesys/servlet?command=tail&filePath=' + encodeURIComponent(path) + "&initial=true","_blank","status=yes,toolbar=yes,menubar=yes,location=yes,resizable=yes,scrollbars=yes");
}

function grep(path, fileName)
{
    var checkResult = ajaxRPC("grepAllowed", encodeURIComponent(path));
    if (checkResult == 'true') 
    {
        showPrompt('/webfilesys/servlet?command=ajaxRPC&method=grepParams&param1=' + encodeURIComponent(fileName), '/webfilesys/xsl/grepParams.xsl', 320, 130);
    }
    else 
    {
        alert(checkResult);
    }
}

function delFile(fileName)
{
    showPrompt('/webfilesys/servlet?command=ajaxRPC&method=deleteFilePrompt&param1=' + encodeURIComponent(fileName), '/webfilesys/xsl/confirmDeleteFile.xsl', 320, 130);
}

/* not used anymore ? */
function deleteFile(fileName)
{
    window.location.href = "/webfilesys/servlet?command=fmdelete&fileName=" + fileName + "&deleteRO=yes";
}

function accessRights(path)
{
    rightWin = window.open("/webfilesys/servlet?command=unixRights&actpath=" + encodeURIComponent(path) + "&isDirectory=false&random=" + (new Date()).getTime(),"rightWin","status=no,toolbar=no,menu=no,resizable=yes,scrollbars=yes,height=530,width=350,left=200,top=40,screenX=200,screenY=40");
    rightWin.focus();
}

function gunzip(path)
{
    window.location.href="/webfilesys/servlet?command=gunzip&filename=" + encodeURIComponent(path);
}

function compress(path)
{
    window.location.href="/webfilesys/servlet?command=unixCompress&actPath=" + encodeURIComponent(path);
}

function untar(path)
{
    window.location.href="/webfilesys/servlet?command=untar&filePath=" + encodeURIComponent(path);
}

function sendFile(fileName)
{
    showPrompt('/webfilesys/servlet?command=emailFilePrompt&fileName=' + encodeURIComponent(fileName), '/webfilesys/xsl/emailFile.xsl', 400, 250);
    
	setBundleResources();

    document.emailForm.receiver.focus();
    
    document.emailForm.receiver.select();
}

function delLink(linkName)
{
    window.location.href="/webfilesys/servlet?command=deleteLink&linkName=" + encodeURIComponent(linkName);
}

function switchReadWrite(path)
{   
    showPrompt('/webfilesys/servlet?command=switchReadWrite&filePath=' + encodeURIComponent(path), '/webfilesys/xsl/switchReadWrite.xsl', 360);

	setBundleResources();
}

function associatedProg(path)
{
    var url = '/webfilesys/servlet?command=runAssociatedProgram&filePath=' + encodeURIComponent(path);

    xmlRequest(url, startProgramResult);
}

function encrypt(fileName)
{   
    showPrompt('/webfilesys/servlet?command=cryptoKeyPrompt&fileName=' + encodeURIComponent(fileName), '/webfilesys/xsl/encrypt.xsl', 360);
    
    document.cryptoForm.cryptoKey.focus();
    
    document.cryptoForm.cryptoKey.select();
}

function decrypt(fileName)
{   
    showPrompt('/webfilesys/servlet?command=cryptoKeyPrompt&fileName=' + encodeURIComponent(fileName), '/webfilesys/xsl/decrypt.xsl', 360);
    
    document.cryptoForm.cryptoKey.focus();
    
    document.cryptoForm.cryptoKey.select();
}

function startProgramResult() {
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
             var item = req.responseXML.getElementsByTagName("success")[0];            
             var success = item.firstChild.nodeValue;

             if (success != 'true') {
                 var msgItem = req.responseXML.getElementsByTagName("message")[0];            
                 var message = msgItem.firstChild.nodeValue;
                 alert(message);
             }             
        }
    }
}

function URLEncode(path)
{
    var encodedPath = '';

    for (i = 0; i < path.length; i++)
    {
        c = path.charAt(i);
    
        if (c == '\\')
        {
            encodedPath = encodedPath + '/';
        }
        else
        {
            encodedPath = encodedPath + c;
        }
    }
    
    return(encodeURI(encodedPath)); 
}

