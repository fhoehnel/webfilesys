function hideMenu()
{
    document.getElementById('contextMenu').style.visibility = 'hidden';
}

function getFileNameExt(fileName)
{
    fileExt="";

    extStart=fileName.lastIndexOf('.');

    if (extStart > 0)
    {
	fileExt=fileName.substring(extStart).toUpperCase();
    }
    
    return(fileExt);
}

function insertDoubleBackslash(source)
{
    return(source.replace(/\\/g,"\\\\"));
}


function menuEntry(href, label, target)
{
    targetText = "";

    if (target != null)
    {
        targetText = 'target="' + target + '"'; 
    }

    return('<tr>'
             + '<td class="jsmenu">'
             + '<a class="menuitem" href="' + href + '" ' + targetText + '>' + label + '</a>'
             + '</td>'
             + '</tr>');
}

function jsContextMenu(fileName, imgType)
{
    menuDiv = document.getElementById('contextMenu');    
        
    shortFileName = fileName;
    
    if (fileName.length > 24)
    {
        shortFileName = fileName.substring(0,7) + "..." + fileName.substring(fileName.length - 14, fileName.length);
    }    

    fileNameExt = getFileNameExt(fileName);
    
    lastPathChar = path.charAt(path.length - 1);
    
    if ((lastPathChar == '/') || (lastPathChar == '\\'))
    {
        fullPath = path + fileName;
    }
    else
    {
        if (path.indexOf('\\') >= 0)
        {
            fullPath = path + '\\' + fileName;
        }
        else
        {
            fullPath = path + '/' + fileName;
        }
    }

    scriptPreparedPath = insertDoubleBackslash(fullPath);

    scriptPreparedFile = insertDoubleBackslash(fileName);
        
    menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
             + '<tr>'
             + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
             + shortFileName
             + '</th>'
             + '</tr>';

    if (imgType == '1') // JPEG
    {
        menuText = menuText 
                 + menuEntry("javascript:jsExifData('" + scriptPreparedPath + "')",resourceExifData,null);
    }

    menuText = menuText 
             + menuEntry("javascript:jsComments('" + scriptPreparedPath + "')",resourceComments,null);
        
    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;

    menuDiv.style.bgcolor = '#c0c0c0';
    
    maxMenuHeight = 120;
    
    if (browserType == 'msie')
    {
        windowWidth = document.body.clientWidth;
        windowHeight = document.body.clientHeight;
        yScrolled = document.body.scrollTop;

        if (clickXPos > windowWidth - 200)
        {
            clickXPos = windowWidth - 200;
        }

        if (clickYPos > windowHeight - maxMenuHeight)
        {
            clickYPos = windowHeight - maxMenuHeight;
        }

        clickYPos = clickYPos + yScrolled;
    }
    else
    {
        windowWidth = window.innerWidth;
        windowHeight = window.innerHeight;
        yScrolled = window.pageYOffset;
        xScrolled = window.pageXOffset;
        
        if (clickYPos > yScrolled + windowHeight - maxMenuHeight)
        {
            clickYPos = yScrolled + windowHeight - maxMenuHeight;
        }

        if (clickXPos > xScrolled + windowWidth - 200)
        {
            clickXPos = xScrolled + windowWidth - 200;
        }
    }
    
    menuDiv.style.left = clickXPos;
    menuDiv.style.top = clickYPos;

    menuDiv.style.visibility = 'visible';
}

function jsExifData(path)
{
    exifWin = window.open('/_exifData?imgFile=' + encodeURIComponent(path),'exifWin','scrollbars=yes,status=no,toolbar=no,location=no,menu=no,width=400,height=500,left=200,top=90,screenX=200,screenY=90,resizable=no');
    exifWin.focus();
}

function jsComments(path)
{
    commentWin=window.open("/_listComments?actPath=" + encodeURIComponent(path),"commentWin","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=500,height=400,resizable=yes,left=100,top=100,screenX=100,screenY=100");
    commentWin.focus();
}

function jsSendFile(path)
{
    window.open("/_sendFileParms?actPath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"mailWin","status=no,toolbar=no,menu=no,resizable=yes,scrollbars=yes,height=400,width=500,left=200,top=100,screenX=200,screenY=100");
}
