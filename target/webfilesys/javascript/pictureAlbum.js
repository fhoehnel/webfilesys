    function albumImg(imgName)
    {
        if (browserMSIE)
        {
            windowWidth = document.body.clientWidth;
            windowHeight = document.body.clientHeight;
        }
        else
        {
            windowWidth = window.innerWidth;
            windowHeight = window.innerHeight;
        }    
    
        window.location.href = '/webfilesys/servlet?command=albumImg&imgName=' + encodeURIComponent(imgName) + '&windowWidth=' + windowWidth + '&windowHeight=' + windowHeight;
    }

    function albumLinkedImg(realPath)
    {
        if (browserMSIE)
        {
            windowWidth = document.body.clientWidth;
            windowHeight = document.body.clientHeight;
        }
        else
        {
            windowWidth = window.innerWidth;
            windowHeight = window.innerHeight;
        }    
    
        window.location.href = '/webfilesys/servlet?command=albumImg&realPath=' + encodeURIComponent(realPath) + '&windowWidth=' + windowWidth + '&windowHeight=' + windowHeight;
    }
