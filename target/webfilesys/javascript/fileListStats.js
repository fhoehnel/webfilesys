function previewFile(path)
{
    var viewPath = "";
    
    if (path.charAt(0) == '/')
    {
       viewPath = '/webfilesys/servlet' + encodeURI(path);
    }
    else
    {
       viewPath = '/webfilesys/servlet/' + URLEncode(path);
    }
    
    window.open(viewPath,"_blank","scrollbars=yes,resizable=yes,width=500,height=500,left=40,top=10,screenX=40,screenY=10");
}

function URLEncode(path)
{
    var encodedPath = '';

    for (i = 0; i < path.length; i++)
    {
        c = path.charAt(i);
    
        if (c == '\\')
        {
            encodedPath = encodedPath + '/';
        }
        else
        {
            encodedPath = encodedPath + c;
        }
    }
    
    return(encodeURI(encodedPath)); 
}
