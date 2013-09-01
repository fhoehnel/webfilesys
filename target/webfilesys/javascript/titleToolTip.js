function showToolTip(message)
{
    toolTipDiv = document.getElementById('toolTip');    

    if (navigator.appName=='Netscape')
    {
        windowWidth = window.innerWidth;
        windowHeight = window.innerHeight;
    }
    else
    {
        windowWidth = document.body.clientWidth;
        windowHeight = document.body.clientHeight;
    }
 
    xpos = (windowWidth - 200) / 2;
    ypos = (windowHeight - 100) / 2;
    
    if (xpos < 1)
    {
        xpos = 1;
    }

    if (ypos < 1)
    {
        ypos = 1;
    }

    toolTipDiv.style.left = xpos + "px";
    toolTipDiv.style.top = ypos + "px";
    
    toolTipDiv.innerHTML = message;

    toolTipDiv.style.visibility = 'visible';
}

function hideToolTip()
{
    toolTipDiv = document.getElementById('toolTip');    

    toolTipDiv.style.visibility = 'hidden';
}