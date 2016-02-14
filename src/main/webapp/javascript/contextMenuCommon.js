function menuEntry(href, label)
{
    return('<tr>'
             + '<td class="jsmenu" onclick="' + href + '">'
             + '<span class="menuitem">' + label + '</span>'
             + '</td>'
             + '</tr>');
}

function hideMenu()
{
    document.getElementById('contextMenu').style.visibility = 'hidden';
}

