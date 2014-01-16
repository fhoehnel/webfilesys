    function albumImg(imgName)
    {
        window.location.href = '/webfilesys/servlet?command=albumImg&imgName=' + encodeURIComponent(imgName) + '&windowWidth=' + getWinWidth() + '&windowHeight=' + getWinHeight();
    }

    function albumLinkedImg(realPath)
    {
        window.location.href = '/webfilesys/servlet?command=albumImg&realPath=' + encodeURIComponent(realPath) + '&windowWidth=' + getWinWidth() + '&windowHeight=' + getWinHeight();
    }
