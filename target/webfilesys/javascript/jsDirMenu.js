function mkdir(path)
{  
    path = path.replace('`','\'');

    showPrompt('/webfilesys/servlet?command=mkdirPrompt&path=' + encodeURIComponent(path), '/webfilesys/xsl/createFolder.xsl', 320, 190);

    document.mkdirForm.NewDirName.focus();
    
    document.mkdirForm.NewDirName.select();
}

function copyDir(path)
{
    copyDirToClip(path);
}

function deleteDir(path, domId)
{
    showPrompt('/webfilesys/servlet?command=ajaxRPC&method=deleteDirPrompt&param1=' + encodeURIComponent(path), '/webfilesys/xsl/confirmDeleteDir.xsl', 320, 110);
}

function renameDir(path)
{
    showPrompt('/webfilesys/servlet?command=renDirPrompt&path=' + encodeURIComponent(path), '/webfilesys/xsl/renameDir.xsl', 320, 190);

    document.mkdirForm.NewDirName.focus();
    
    document.mkdirForm.NewDirName.select();
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
    var statWin=open("/webfilesys/servlet?command=fileStatistics&cmd=treeStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=600,height=590");
    statWin.focus();
}

function statSunburst(path)
{
    var windowWidth = screen.availWidth - 10;
    var windowHeight = screen.availHeight - 20;
    
    if (browserChrome) 
    {
        windowHeight = windowHeight - 40;
    }
    
    if (windowHeight > windowWidth - 200)
    {
        windowHeight = windowWidth - 200;
    }
    var statWin = open("/webfilesys/servlet?command=folderTreeStats&path=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=" + windowWidth + ",height=" + windowHeight);
    statWin.focus();
}

function fileSizeStatistics(path)
{
    var statWin;
    
    if (browserSafari || browserOpera) 
    {
        statWin=open("/webfilesys/servlet?command=fileStatistics&cmd=sizeStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=700,height=500");
    }
    else 
    {
        statWin=open("/webfilesys/html/waitFileSizeStats.html?command=fileStatistics&cmd=sizeStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=700,height=500");
    }
    statWin.focus();
}

function fileTypeStatistics(path)
{
    var statWin;
    
    if (browserSafari || browserOpera) 
    {
        statWin=open("/webfilesys/servlet?command=fileStatistics&cmd=typeStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=700,height=500");
    }
    else 
    {
        statWin=open("/webfilesys/html/waitFileTypeStats.html?command=fileStatistics&cmd=typeStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=700,height=500");
    }

    statWin.focus();
}

function fileAgeStatistics(path)
{
    var statWin;
    
    if (browserSafari || browserOpera) 
    {
        statWin=open("/webfilesys/servlet?command=fileStatistics&cmd=ageStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=700,height=500");
    }
    else 
    {
        statWin=open("/webfilesys/html/waitFileAgeStats.html?command=fileStatistics&cmd=ageStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=700,height=500");
    }
    statWin.focus();
}

function search(path)
{
    searchWin=open("/webfilesys/servlet?command=search&actpath=" + encodeURIComponent(path),"Search","scrollbars=yes,resizable=yes,width=500,height=500,left=80,top=10,screenX=80,screenY=10");
    searchWin.focus();
    searchWin.opener=self;
}

function mkfile(path)
{
    showPrompt('/webfilesys/servlet?command=mkfilePrompt&path=' + encodeURIComponent(path), '/webfilesys/xsl/createFile.xsl', 320, 190);
    
    document.mkfileForm.NewFileName.focus();
    
    document.mkfileForm.NewFileName.select();
}

function upload(path)
{
    window.parent.frames[2].location.href = "/webfilesys/servlet?command=uploadParms&actpath=" + encodeURIComponent(path);
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
    descWin=window.open("/webfilesys/servlet?command=editMetaInf&path=" + encodeURIComponent(path) + "&geoTag=true&random=" + new Date().getTime(),"descWin","status=no,toolbar=no,location=no,menu=no,width=600,height=450,scrollbars=yes,resizable=yes,left=20,top=20,screenX=20,screenY=20");
    descWin.focus();
    // descWin.opener=parent.FileList;
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

function watchFolder(path)
{   
    showPrompt('/webfilesys/servlet?command=watchFolder&path=' + encodeURIComponent(path), '/webfilesys/xsl/watchFolder.xsl', 360);
}
