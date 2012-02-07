
function validateAndSubmitCrypto(invalidKeyMsg)
{
    var secretKey = document.getElementById('cryptoForm').cryptoKey.value;

    if (!checkCryptoKeySyntax(secretKey))
    {
        alert(invalidKeyMsg);
        return;
    }

    document.getElementById('buttonRow').style.display = 'none';
    document.getElementById('hourGlass').style.visibility = 'visible';

    document.cryptoForm.submit();
}

function checkCryptoKeySyntax(secretKey)
{
    if (secretKey.length < 4)
    {
        return false;
    }
    
    return true;
}