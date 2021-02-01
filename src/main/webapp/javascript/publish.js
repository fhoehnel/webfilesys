function switchShowPageSize()
{
    var radios = document.getElementsByName("publishType");

    var pageSizeInput = document.getElementById("pageSize");

    if (radios[0].checked)
    {
        pageSizeInput.value = "";
        pageSizeInput.disabled = true;
    }
    else
    {
        pageSizeInput.value = initialPageSize;
        pageSizeInput.disabled = false;
    }
}

function switchInviteFlag()
{
    if (document.form1.invite.checked == true)
    {
        document.form1.receiver.disabled = false;
        document.form1.subject.disabled = false;
        document.form1.msgText.disabled = false;
    }
    else
    {
        document.form1.receiver.value = '';
        document.form1.receiver.disabled = true;
        document.form1.subject.value = '';
        document.form1.subject.disabled = true;
        document.form1.msgText.value = '';
        document.form1.msgText.disabled = true;
    }
}

function selectPublicLink() {
    var publicLinkCont = document.getElementById("publicLinkCont");
    if (publicLinkCont) {
    	publicLinkCont.select();
    }
}

function copyPublicUrl() {
    document.getElementById("publicLinkCont").select();
    document.execCommand("Copy");
    self.close();
}

function copyUrlToClip(copyLink, urlInputId) {
    let urlInput = document.getElementById(urlInputId);
    urlInput.focus;
    urlInput.select();
    document.execCommand("Copy");
    customAlert(resourceBundle["publicUrlCopied"]);
    urlInput.selectionEnd = urlInput.selectionStart;
}
