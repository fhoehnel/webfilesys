var browserType = "msie";

if (navigator.appName=='Netscape')
{
    browserType = "netscape";
}

var stopMenuClose = false;

function mouseClickHandler(Ereignis)
{
    if (!mouseClickHandled)
    {
        if (window.event)
        {
            clickXPos = window.event.clientX;
            clickYPos = window.event.clientY;
        }
    
        document.getElementById('contextMenu').style.visibility = 'hidden';
        
        if (window.name == 'DirectoryPath')
        {
            if (parent.frames[2].document.getElementById('contextMenu'))
            {
                parent.frames[2].document.getElementById('contextMenu').style.visibility = 'hidden';
            }
        }
        else if (window.name == 'FileList')
        {   
            if (parent.frames[1].document.getElementById('contextMenu'))
            {
                parent.frames[1].document.getElementById('contextMenu').style.visibility = 'hidden';
            }
        }
    }
    else
    {
        mouseClickHandled = false;
    }
}

function handleMouseClickNetscape(evt)
{
    if (!browserMSIE) 
    {
        rightMouseButton = false;
        
        clickXPos = evt.layerX;
        clickYPos = evt.layerY;

        if ((evt.button) && (evt.button == 2))
        {
            rightMouseButton = true;
        }
    }
}

function menuClicked()
{
    mouseClickHandled = true;
    
    setTimeout('conditionalHideMenu()',2000);
}

function conditionalHideMenu()
{
    if (stopMenuClose)
    {
        stopMenuClose = false;
        return;
    }
    
    hideMenu();
}

var clickXPos = 0;
var clickYPos = 0;

var rightMouseButton = false;

var mouseClickHandled = false;

document.onmouseup = handleMouseClickNetscape;
