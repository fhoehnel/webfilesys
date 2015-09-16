function menuEntry(href, label, target)
{
    targetText = "";

    if (target != null)
    {
        targetText = 'target="' + target + '"'; 
    }

    return('<tr>'
             + '<td class="jsmenu" onclick="' + href + '">'
             + '<span class="menuitem">' + label + '</a>'
             + '</td>'
             + '</tr>');
}

function hideMenu()
{
    document.getElementById('contextMenu').style.visibility = 'hidden';
}

