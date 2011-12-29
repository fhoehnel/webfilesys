// Firefox 2/3
// var browserFirefox = /a/[-1]=='a';
var browserFirefox = navigator.userAgent.toLowerCase().indexOf('firefox') > -1;

// Internet Explorer 6/7/8:
var browserMSIE = '\v'=='v';

// Google Chrome
// var browserChrome = /source/.test((/a/.toString+''));
var browserChrome = navigator.userAgent.toLowerCase().indexOf('chrome') > -1;

var browserSafari = /a/.__proto__=='//';

var browserOpera = false;
if (window.opera)
{
    browserOpera = true;
}

