function mkdir(path) {  
    path = path.replace('`','\'');

    centeredDialog('/webfilesys/servlet?command=mkdirPrompt&path=' + encodeURIComponent(path), '/webfilesys/xsl/createFolder.xsl', 320, 190, function() {
        document.mkdirForm.NewDirName.focus();
        document.mkdirForm.NewDirName.select();
    });
}

function copyDir(path, domId)
{
    deselectFolder();
	selectFolder(domId);

    copyDirToClip(path);
}

function deleteDir(path, domId) {
    centeredDialog('/webfilesys/servlet?command=ajaxRPC&method=deleteDirPrompt&param1=' + encodeURIComponent(path), 
                   '/webfilesys/xsl/confirmDeleteDir.xsl', 
                   320, 110); 
}

function renameDir(path) {
    centeredDialog('/webfilesys/servlet?command=renDirPrompt&path=' + encodeURIComponent(path), '/webfilesys/xsl/renameDir.xsl', 320, 190, function() {
        document.mkdirForm.NewDirName.focus();
        document.mkdirForm.NewDirName.select();
    });
}

function zip(path)
{
    window.location.href="/webfilesys/servlet?command=zipDir&actPath=" + encodeURIComponent(path);
}

function downloadFolder(path)
{
    window.location.href="/webfilesys/servlet?command=downloadFolder&path=" + encodeURIComponent(path);
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
    var statWin = open("/webfilesys/html/wait.html?command=folderTreeStats&path=" + encodeURIComponent(path), "Statistics", "scrollbars=yes,resizable=yes,width=" + windowWidth + ",height=" + windowHeight);
    statWin.focus();
}

function fileSizeStatistics(path)
{
    var statWin;
    
    if (browserSafari || browserOpera) 
    {
        statWin=open("/webfilesys/servlet?command=fileStatistics&cmd=sizeStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=780,height=500");
    }
    else 
    {
        statWin=open("/webfilesys/html/waitFileSizeStats.html?command=fileStatistics&cmd=sizeStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=780,height=500");
    }
    statWin.focus();
}

function fileTypeStatistics(path)
{
    var statWin;
    
    if (browserSafari || browserOpera) 
    {
        statWin=open("/webfilesys/servlet?command=fileStatistics&cmd=typeStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=780,height=500");
    }
    else 
    {
        statWin=open("/webfilesys/html/waitFileTypeStats.html?command=fileStatistics&cmd=typeStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=780,height=500");
    }

    statWin.focus();
}

function fileAgeStatistics(path)
{
    var statWin;
    
    if (browserSafari || browserOpera) 
    {
        statWin=open("/webfilesys/servlet?command=fileStatistics&cmd=ageStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=780,height=500");
    }
    else 
    {
        statWin=open("/webfilesys/html/waitFileAgeStats.html?command=fileStatistics&cmd=ageStats&actpath=" + encodeURIComponent(path) + "&random=" + (new Date()).getTime(),"Statistics","scrollbars=yes,resizable=yes,width=780,height=500");
    }
    statWin.focus();
}

function search(path)
{
    searchWin=open("/webfilesys/servlet?command=search&actpath=" + encodeURIComponent(path),"Search","scrollbars=yes,resizable=yes,width=500,height=500,left=80,top=10,screenX=80,screenY=10");
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
    window.parent.frames[2].location.href = "/webfilesys/servlet?command=uploadParms&actpath=" + encodeURIComponent(path);
}

function publish(path,mailEnabled)
{
    if (parent.mailEnabled == 'true')
    {
         publishWin=window.open("/webfilesys/servlet?command=publishForm&actPath=" + encodeURIComponent(path) + "&type=common","publish","status=no,toolbar=no,menu=no,width=620,height=590,resizable=yes,scrollbars=no,left=30,top=20,screenX=40,screenY=20");
    }
    else
    {
         publishWin=window.open("/webfilesys/servlet?command=publishParms&actPath=" + encodeURIComponent(path) + "&type=common","publish","status=no,toolbar=no,menu=no,width=620,height=290,resizable=yes,scrollbars=no,left=30,top=80,screenX=40,screenY=80");
    }

    publishWin.focus();
}

function description(path)
{
    descWin=window.open("/webfilesys/servlet?command=editMetaInf&path=" + encodeURIComponent(path) + "&geoTag=true&random=" + new Date().getTime(),"descWin","status=no,toolbar=no,location=no,menu=no,width=600,height=590,scrollbars=yes,resizable=yes,left=20,top=10,screenX=20,screenY=10");
    descWin.focus();
    // descWin.opener=parent.FileList;
}

function driveInfo(path) {
    centeredDialog("/webfilesys/servlet?command=driveInfo&path=" + encodeURIComponent(path), "/webfilesys/xsl/driveInfo.xsl", 260, 400);
}

function refresh(path)
{
    window.location.href="/webfilesys/servlet?command=refresh&path=" + encodeURIComponent(path);
}

function rights(path)
{
    window.location.href="/webfilesys/servlet?command=unixRights&actpath=" + encodeURIComponent(path) + "&isDirectory=true&random=" + (new Date()).getTime();
}

function watchFolder(path) { 
    centeredDialog('/webfilesys/servlet?command=watchFolder&path=' + encodeURIComponent(path), '/webfilesys/xsl/watchFolder.xsl', 360, 160);
}

function cloneFolder(path, folderName) {   
	showPromptDialog("/webfilesys/html/cloneFolder.html", 300, function() {	
	
	    document.getElementById("sourceFolderPath").value = path;
	    document.getElementById("sourceFolderName").value = folderName;
	    document.getElementById("oldLinkNameShort").innerHTML = shortText(folderName, 25);
	    document.getElementById("oldLinkNameShort").title = folderName;

	    var newFolderName = document.getElementById("newFolderName");
	    newFolderName.value = folderName;
	    newFolderName.focus();
	    newFolderName.select();
	});
}
