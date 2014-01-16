function handleFolderTreeKey(evt)
{
    var keyHandled = false;
    
    if (!evt) 
    {
        if (window.event) 
        {
            evt = window.event;
        }
    }
    if (evt) 
    {
        if (evt.keyCode == 27)
        {
            // ESC key
            handleESCKey();              
            
            keyHandled = true;
        }
    }
      
    if (keyHandled) 
    {
        evt.stopPropagation();
    }
}

function handleESCKey() 
{
    document.getElementById('contextMenu').style.visibility = 'hidden';

    var promptBox = document.getElementById('prompt');
    if (promptBox)
    {
        promptBox.style.visibility = 'hidden';
    }
}