
function cutCopyToClip(fileName, operation)
{
    url = '/webfilesys/servlet?command=cutCopy&fileName=' + encodeURIComponent(fileName) + '&cmd=' + operation;

    xmlRequest(url, showCopyResult);
}

function editLocal(fileName)
{
    url = "/webfilesys/servlet?command=editFile&fileName=" + encodeURIComponent(fileName);

    xmlRequest(url, ignoreResult);
}

function editLocalLink(filePath)
{
    url = "/webfilesys/servlet?command=editFile&filePath=" + encodeURIComponent(filePath);

    xmlRequest(url, ignoreResult);
}

function showCopyResult()
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
             var item = req.responseXML.getElementsByTagName("message")[0];            
             var message = item.firstChild.nodeValue;
             
             hideMenu();
        
             msgBox1 = document.getElementById("msg1");
        
             if (window.ActiveXObject) 
             {
                 windowWidth = document.body.clientWidth;
                 windowHeight = document.body.clientHeight;
                 yScrolled = document.body.scrollTop;
                 xScrolled = document.body.scrollLeft;
             }
             else
             {
                 windowWidth = window.innerWidth;
                 windowHeight = window.innerHeight;
                 yScrolled = window.pageYOffset;
                 xScrolled = window.pageXOffset;
             }
        
             msgXpos = 30 + xScrolled;
        
             msgBox1.style.left = msgXpos + 'px';

             msgYpos = (windowHeight - 100) / 2 + yScrolled;
             if (msgYpos < 10)
             {
                 msgYpos = 10;
             }

             msgBox1.style.top = msgYpos + 'px';
        
             msgBox1.style.visibility = "visible";
             
             msgBox1.innerHTML = message;
             
             item = req.responseXML.getElementsByTagName("clipboardWasEmpty")[0];
             var reload = item.firstChild.nodeValue;
             
             if (reload == 'true')
             {
                 setTimeout("reloadPage()", 2000);
             }
             else
             {
                 setTimeout("hideMsg()", 2000);
             }
        }
    }
}

function ignoreResult()
{
    hideMenu();
}

function reloadPage()
{
    window.location.href = '/webfilesys/servlet?command=listFiles';
}

function hideMsg()
{
     msgBox1 = document.getElementById("msg1");
     msgBox1.style.visibility = "hidden";
}

function diffSelect(path)
{
    parent.diffStarted = true;

    url = "/webfilesys/servlet?command=diffSelect&path=" + encodeURIComponent(path);

    xmlRequest(url, diffSelectResult);
}

function diffSelectResult()
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
             var item = req.responseXML.getElementsByTagName("success")[0];            
             var result = item.firstChild.nodeValue;
             
             hideMenu();

             if (result == 'targetSelected')
             {
                 if (openDiffWindow())
                 {
                     parent.diffStarted = false;
                 }
                 else
                 {
                     alert("Unable to open a popup window for the compare result. Please check your browser settings for popups!");
                 }
                 return;
             }

             item = req.responseXML.getElementsByTagName("message")[0];            
             var message = item.firstChild.nodeValue;
             
             msgBox1 = document.getElementById("msg1");
        
             if (window.ActiveXObject) 
             {
                 windowWidth = document.body.clientWidth;
                 windowHeight = document.body.clientHeight;
                 yScrolled = document.body.scrollTop;
                 xScrolled = document.body.scrollLeft;
             }
             else
             {
                 windowWidth = window.innerWidth;
                 windowHeight = window.innerHeight;
                 yScrolled = window.pageYOffset;
                 xScrolled = window.pageXOffset;
             }
        
             msgXpos = 10 + xScrolled;
        
             msgBox1.style.left = msgXpos + 'px';

             msgYpos = (windowHeight - 100) / 2 + yScrolled;
             if (msgYpos < 10)
             {
                 msgYpos = 10;
             }

             msgBox1.style.top = msgYpos + 'px';
        
             msgBox1.style.visibility = "visible";
             
             msgBox1.innerHTML = message;
             
             setTimeout("hideMsg()", 4000);
        }
    }
}

function cancelDiff()
{
    url = "/webfilesys/servlet?command=diffSelect&cmd=deselect";

    xmlRequestSynchron(url);

    parent.diffStarted = false;
    
    hideMenu();    
    
    stopMenuClose = true;
}

function openDiffWindow()
{
    diffWin = window.open('/webfilesys/servlet?command=startDiff','diffWin','width=' + (screen.width - 20) + ',height=' + (screen.height - 80) + ',scrollbars=yes,resizable=yes,status=no,menubar=no,toolbar=no,location=no,directories=no,screenX=0,screenY=0,left=0,top=0');
    if (diffWin) 
    {
        diffWin.focus();
        return true;
    }
    return false;
}

function touch(fileName)
{
    url = '/webfilesys/servlet?command=touch&fileName=' + encodeURIComponent(fileName);

    xmlRequest(url, showTouchResult);
}

function showTouchResult()
{
    if (req.readyState == 4)
    {
        if (req.status == 200)
        {
             var item = req.responseXML.getElementsByTagName("success")[0];            
             var success = item.firstChild.nodeValue;
             
             hideMenu();

             if (success == 'true')
             {
                 reloadPage();
                 return;
             }

             item = req.responseXML.getElementsByTagName("message")[0];            
             var message = item.firstChild.nodeValue;

             showMsgCentered(message, 260, 60, 3000);
        }
    }
}

function checkFileChange(filePath, lastModifiedOld, sizeOld)
{
    var url = "/webfilesys/servlet?command=checkFileChange&filePath=" + encodeURIComponent(filePath) + "&lastModified=" + lastModifiedOld + "&size=" + sizeOld;

    var responseXml = xmlRequestSynchron(url);
    
    var resultItem = responseXml.getElementsByTagName("result")[0];            
    
    if (!resultItem)
    {
        return("");
    }
    
    return(resultItem.firstChild.nodeValue == "true");
}
