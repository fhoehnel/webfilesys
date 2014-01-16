function mkdir(path)
{  
    window.opener.location.href="/_mkdir?actpath=" + encodeURIComponent(path);
    setTimeout("self.close()",1000);
}

function copyDir(path)
{
    window.opener.location.href="/_winDirTree?actPath=" + encodeURIComponent(path) + "&cmd=copyToClipboard";
    setTimeout("self.close()",1000);
}

function deleteDir(path)
{
    window.opener.location.href="/_deldir?actpath=" + encodeURIComponent(path);
    setTimeout("self.close()",1000);
}

function renameDir(path)
{
    window.opener.location.href="/_renamedir?actpath=" + encodeURIComponent(path);
    setTimeout("self.close()",1000);
}

function zip(path)
{
    window.opener.location.href="/_zipDir?actPath=" + encodeURIComponent(path);
    setTimeout("self.close()",1000);
}

function paste(path)
{
    window.opener.location.href="/_pasteFiles?actpath=" + encodeURIComponent(path) + "&random=" + (new Date().getTime());
    setTimeout("self.close()",1000);
}

function statistics(path)
{
    statwin=open("/_statistics?actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=580,height=590");
    setTimeout("self.close()",1000);
}

function search(path)
{
    statwin=open("/_search?actpath=" + encodeURIComponent(path),"Search","scrollbars=yes,resizable=yes,width=500,height=450");
    setTimeout("self.close()",1000);
}

function mkfile(path)
{
    window.opener.location.href="/_mkfile?actpath=" + encodeURIComponent(path);
    setTimeout("self.close()",1000);
}

function upload(path)
{
    // window.opener.location.href="/_upload?actpath=" + encodeURIComponent(path);
    window.opener.parent.frames['FileList'].location.href = "/_upload?actpath=" + encodeURIComponent(path);
    setTimeout("self.close()",1000);
}

function publish(path,mailEnabled)
{
    if (mailEnabled)
    {
         window.opener.location.href="/_publishParms?actPath=" + encodeURIComponent(path);
    }
    else
    {
         publishWin=window.open("/_publishForm?actPath=" + encodeURIComponent(path) + "&type=common","publish","status=no,toolbar=no,menu=no,width=550,height=280,resizable=no,scrollbars=no,left=80,top=100,screenX=80,screenY=100");
         publishWin.focus();
    }

    setTimeout("self.close()",1000);
}

function clearThumbs(path)
{
    window.open("/_clearThumbs?actPath=" + encodeURIComponent(path),"Thumbnails","status=no,toolbar=no,menu=no,width=10,height=10,resizable=no,scrollbars=no,left=400,top=200,screenX=400,screenY=200");
    setTimeout("self.close()",1000);
}

function description(path)
{
    descWin=window.open("/_editDescription?path=" + encodeURIComponent(path) + "&random=" + new Date().getTime(),"descWin","status=no,toolbar=no,location=no,menu=no,width=500,height=190,resizable=yes,left=150,top=100,screenX=150,screenY=100");
    descWin.focus();
    descWin.opener=window.opener.parent.FileList;
    setTimeout("self.close()",1000);
}

function driveInfo(path)
{
    propWin=window.open("/_driveInfo?path=" + encodeURIComponent(path) + "&random=" + (new Date().getTime()),"propWin","status=no,toolbar=no,location=no,menu=no,width=400,height=200,resizable=yes,left=100,top=200,screenX=100,screenY=200");
    propWin.focus();
    setTimeout("self.close()",1000);
}

function refresh(path)
{
    window.opener.location.href="/_refresh?actPath=" + encodeURIComponent(path);
    setTimeout("self.close()",1000);
}


function rights(path)
{
    window.opener.location.href="/_fmmode?actpath=" + encodeURIComponent(path) + "&isDirectory=true&random=" + (new Date()).getTime();
    setTimeout("self.close()",1000);
}

