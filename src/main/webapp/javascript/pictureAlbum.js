function albumImg(imgName)
{
    window.location.href = '/webfilesys/servlet?command=bookPicture&imgName=' + encodeURIComponent(imgName) + '&windowWidth=' + getWinWidth() + '&windowHeight=' + getWinHeight();
}

function albumLinkedImg(realPath)
{
    window.location.href = '/webfilesys/servlet?command=bookPicture&realPath=' + encodeURIComponent(realPath) + '&windowWidth=' + getWinWidth() + '&windowHeight=' + getWinHeight();
}

function jsComments(path)
{
    var commentWin = window.open("/webfilesys/servlet?command=listComments&actPath=" + encodeURIComponent(path),"commentWin","status=no,toolbar=no,location=no,menu=no,scrollbars=yes,width=550,height=400,resizable=yes,left=80,top=100,screenX=80,screenY=100");
    commentWin.focus();
}
    