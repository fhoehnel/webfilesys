function linkGraphicsMenu(linkName, realPath, imgType)
{
    menuDiv = document.getElementById('contextMenu');    
        
    shortFileName = linkName;
    
    if (linkName.length > 24)
    {
        shortFileName = linkName.substring(0,7) + "..." + linkName.substring(linkName.length - 14, linkName.length);
    }    

    fileNameExt = getFileNameExt(linkName);

    scriptPreparedPath = insertDoubleBackslash(realPath);

    menuText = '<table class="contextMenu">'
             + '<tr>'
             + '<th>'
             + shortFileName
             + '</th>'
             + '</tr>';

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:jsDelImageLink('" + linkName + "')",resourceBundle["label.deleteLink"],null);

        menuText = menuText 
                 + menuEntry("javascript:renameLink('" + linkName + "')",resourceBundle["label.renameLink"],null);

        menuText = menuText 
                 + menuEntry("javascript:jsDescription('" + scriptPreparedPath + "')",resourceBundle["label.editMetaInfo"],null);
    }
    
    if (imgType == '1') // JPEG
    {
        menuText = menuText 
                 + menuEntry("javascript:jsExifData('" + scriptPreparedPath + "')",resourceBundle["alt.cameradata"],null);
    }
    
    /*
    if ((parent.readonly != 'true') && 
        ((imgType == '1') ||   // JPEG
         (imgType == '3')))     // PNG
    {
        menuText = menuText 
                 + menuEntry("javascript:jsMakeThumb('" + scriptPreparedPath + "')",resourceBundle["label.makethumb"],null);
    }
    */

    menuText = menuText 
             + menuEntry("javascript:jsComments('" + scriptPreparedPath + "')",resourceBundle["label.comments"],null);

    if ((parent.readonly != 'true') && 
        (parent.mailEnabled == 'true'))
    {
        menuText = menuText 
                 + menuEntry("javascript:emailLink('" + scriptPreparedPath + "')",resourceBundle["label.sendfile"],null);
    }

    if (parent.serverOS == 'win')
    {
        realDir = realPath.substring(0,realPath.lastIndexOf('\\'));
        
        if (realDir.length < 3)
        {
            realDir = realDir + "\\";
        }
    }
    else
    {
        realDir = realPath.substring(0,realPath.lastIndexOf('/'));
        
        if (realDir.length == 0)
        {
            realDir = "/";
        }
        
    }

    menuText = menuText 
             + menuEntry("javascript:origDir('" + insertDoubleBackslash(realDir) + "')",resourceBundle["label.origDir"],null);

    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;

    menuDiv.style.bgcolor = '#c0c0c0';
    
    if (parent.readonly == 'true')
    {
        maxMenuHeight = 140;
    }
    else
    {
        maxMenuHeight = 220;
    }
    
    positionMenuDiv(menuDiv, maxMenuHeight);
}

function jsDelImageLink(linkName)
{
    window.location.href="/webfilesys/servlet?command=delImageLink&linkName=" + encodeURIComponent(linkName);
}

function jsDescription(path)
{
    descWin=window.open("/webfilesys/servlet?command=editMetaInf&path=" + encodeURIComponent(path) + "&random=" + new Date().getTime(),"descWin","status=no,toolbar=no,location=no,menu=no,width=600,height=300,resizable=yes,left=20,top=100,screenX=20,screenY=100");
    descWin.focus();
    descWin.opener=self;
}

function origDir(path)
{
    parent.parent.frames[1].location.href="/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(path) + "&fastPath=true";
}

function emailLink(filePath)
{
    showPrompt('/webfilesys/servlet?command=emailFilePrompt&filePath=' + encodeURIComponent(filePath), '/webfilesys/xsl/emailFile.xsl', 400, 250);
    
	setBundleResources();
    
    document.emailForm.receiver.focus();
    
    document.emailForm.receiver.select();
}


