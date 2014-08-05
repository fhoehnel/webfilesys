var stopMenuClose = false;

function mouseClickHandler(evt)
{
	var rightMouseButton = false;

	var clickEvent = window.event;
		
	if (clickEvent)
    {
		clickXPos = clickEvent.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft);
		clickYPos = clickEvent.clientY + (document.documentElement.scrollTop || document.body.scrollTop);		
    }
	else
	{
		clickEvent = evt;
		if (clickEvent) 
		{
            clickXPos = clickEvent.layerX;
            clickYPos = clickEvent.layerY;
		}
    }

	if (clickEvent && clickEvent.button && (clickEvent.button == 2))
    {
        rightMouseButton = true;
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

function positionMenuDiv(menuDiv, maxMenuHeight)
{
    windowWidth = getWinWidth();
    windowHeight = getWinHeight();

    var yScrolled;
    var xScrolled;
    
    if (browserFirefox || browserChrome || browserSafari)
    {
        yScrolled = window.pageYOffset;
        xScrolled = window.pageXOffset;
    }
    else
    {
        yScrolled = (document.documentElement.scrollTop || document.body.scrollTop);
        xScrolled = (document.documentElement.scrollLeft || document.body.scrollLeft);
    }
    
	if (clickYPos > yScrolled + windowHeight - maxMenuHeight)
    {
        clickYPos = yScrolled + windowHeight - maxMenuHeight;
    }

    if (clickXPos > xScrolled + windowWidth - 200)
    {
        clickXPos = xScrolled + windowWidth - 200;
    }

    menuDiv.style.left = clickXPos + 'px';
    menuDiv.style.top = clickYPos + 'px';
    
    menuDiv.style.visibility = 'visible';
}

var clickXPos = 0;
var clickYPos = 0;

var rightMouseButton = false;

document.onclick = mouseClickHandler;
