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

function jsLinkMenu(linkName, realPath)
{
    menuDiv = document.getElementById('contextMenu');    
        
    shortFileName = linkName;
    
    if (linkName.length > 24)
    {
        shortFileName = linkName.substring(0,7) + "..." + linkName.substring(linkName.length - 14, linkName.length);
    }    

    fileNameExt = getFileNameExt(linkName);

    scriptPreparedPath = insertDoubleBackslash(realPath);

    menuText = '<table border="0" width="180" cellpadding="0" cellspacing="0" height="100%">'
             + '<tr>'
             + '<th class="datahead" style="padding-left:5px;padding-right:5px;padding-top:4px;padding-bottom:4px;text-align:left;border-bottom-width:1px;border-bottom-style:solid;border-bottom-color:black;">'
             + shortFileName
             + '</th>'
             + '</tr>';


    if (fileExt == ".ZIP" || fileExt == ".JAR" || fileExt == ".WAR")
    {
        menuText = menuText 
                 + menuEntry("javascript:viewZip('" + scriptPreparedPath + "')",parent.resourceViewZip,null);
    }
    else
    {
	if (fileExt == ".URL")
	{
            menuText = menuText 
                     + menuEntry("/webfilesys/servlet?command=openUrlFile&actPath=" + encodeURIComponent(realPath) + "&random=" + (new Date().getTime()),parent.resourceView,"_blank");
	}
	else
	{
            menuText = menuText 
                     + menuEntry("javascript:viewFile('" + scriptPreparedPath + "')",parent.resourceView,null);
	}
    }

    if (parent.clientIsLocal != 'true')
    {
	if (fileExt == ".MP3")
	{
	    downloadLabel= parent.resourcePlay;
	}
	else
	{
	    downloadLabel = parent.resourceDownload;
	}

        menuText = menuText 
                 + menuEntry("/webfilesys/servlet?command=getFile&filePath=" + encodeURIComponent(realPath) + "&disposition=download",downloadLabel,null);
    }

    if (parent.readonly != 'true')
    {
        menuText = menuText 
                 + menuEntry("javascript:delLink('" + linkName + "')",parent.resourceDelLink,null);

        menuText = menuText 
                 + menuEntry("javascript:renameLink('" + linkName + "')",parent.resourceRenLink,null);

	if (parent.clientIsLocal == 'true')
	{
            menuText = menuText 
                     + menuEntry("javascript:editLocalLink('" + scriptPreparedPath + "')",parent.resourceEdit,null);
        }
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:editRemoteLink('" + scriptPreparedPath + "')",parent.resourceEdit,null);
        }

        if (parent.serverOS == 'win')
        {
            if (parent.webspaceUser != 'true')
            {
		if ((fileExt == ".EXE") || (fileExt == ".COM") || (fileExt == ".BAT") || (fileExt == ".CMD"))
                {
                    menuText = menuText 
                             + menuEntry("/webfilesys/servlet?command=execProgram&progname=" + encodeURIComponent(realPath), parent.resourceRun, null);
                }
                else
                {
                    menuText = menuText 
                             + menuEntry("javascript:associatedProg('" + scriptPreparedPath + "')",parent.resourceOpen,null);
                }
            }
        }
        else
        {
            if (parent.webspaceUser != 'true')
            {
                menuText = menuText 
                         + menuEntry("javascript:accessRights('" + scriptPreparedPath + "')",parent.resourceRights,null);

                menuText = menuText 
                         + menuEntry("javascript:associatedProg('" + scriptPreparedPath + "')",parent.resourceOpen,null);
            }
        }

        if (parent.serverOS == 'win')
        {
            menuText = menuText 
                     + menuEntry("javascript:switchReadWrite('" + scriptPreparedPath + "')",parent.resourceSwitchReadOnly,null);

        }

        if (parent.mailEnabled == 'true')
        {
            menuText = menuText 
                     + menuEntry("javascript:emailLink('" + scriptPreparedPath + "')",parent.resourceSendFile,null);
        }
        
	if (fileExt == ".MP3")
	{
            menuText = menuText 
                     + menuEntry("javascript:editMP3('" + scriptPreparedPath + "')",parent.resourceEditMP3,null);
	}
        else
        {
            menuText = menuText 
                     + menuEntry("javascript:description('" + scriptPreparedPath + "')",parent.resourceEditDesc,null);
        }

    }

    menuText = menuText 
             + menuEntry("javascript:comments('" + scriptPreparedPath + "')",parent.resourceComments,null);

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
             + menuEntry("javascript:origDir('" + insertDoubleBackslash(realDir) + "')",parent.resourceOrigDir,null);
        
    menuText = menuText + '</table>'; 

    menuDiv.innerHTML = menuText;

    menuDiv.style.bgcolor = '#c0c0c0';
    
    if (parent.readonly == 'true')
    {
        maxMenuHeight = 200;
    }
    else
    {
        if (parent.serverOS == 'win')
        {
            maxMenuHeight = 280;
        }
        else
        {
            if (parent.webspaceUser == 'true')
            {
                maxMenuHeight = 260;
            }
            else
            {
                maxMenuHeight = 330;
            }
        }
    }
    
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
    
    menuDiv.style.left = clickXPos + 'px';
    menuDiv.style.top = clickYPos + 'px';

    menuDiv.style.visibility = 'visible';
}

function editRemoteLink(path)
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
    
    editWin=window.open("/webfilesys/servlet?command=editFile&filePath=" + encodeURIComponent(path) + "&screenHeight=" + editWinHeight,"editWin","status=no,toolbar=no,location=no,menu=no,width=" + editWinWidth + ",height=" + editWinHeight + ",resizable=yes,left=20,top=5,screenX=20,screenY=5");
    editWin.focus();
    editWin.opener=self;
}

function origDir(path)
{
    parent.parent.frames[1].location.href="/webfilesys/servlet?command=exp&expandPath=" + encodeURIComponent(path) + "&fastPath=true";
}

function emailLink(filePath)
{
    showPrompt('/webfilesys/servlet?command=emailFilePrompt&filePath=' + encodeURIComponent(filePath), '/webfilesys/xsl/emailFile.xsl', 400, 250);
    
    document.emailForm.receiver.focus();
    
    document.emailForm.receiver.select();
}

