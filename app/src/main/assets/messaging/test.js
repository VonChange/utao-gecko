
 //$(".head_inner").hide();
 function xAppLoadJsHead(scrJs) {
     let script = document.createElement('script');
     script.setAttribute('type', 'text/javascript');
     script.src = scrJs;
     script.async = false;
     document.head.appendChild(script);
 }
function xAppLoadJs(scrJs) {
    let script = document.createElement('script');
    script.setAttribute('type', 'text/javascript');
    script.src = scrJs;
    script.async = false;
    document.body.appendChild(script);
}
function xAppLoadCss(scrCss) {
    let script = document.createElement('link');
    script.setAttribute('rel', 'stylesheet');
    script.setAttribute('type', 'text/css');
    script.href = scrCss;
    script.async = false;
    document.head.appendChild(script);
}
function _appInit(){
    xAppLoadJsHead("http://192.168.0.110:5500/qq/js/common.js");
    xAppLoadCss("http://192.168.0.110:5500/qq/css/layer.css");
    xAppLoadCss("http://192.168.0.110:5500/qq/css/my.css");
    xAppLoadJsHead("http://192.168.0.110:5500/qq/js/focus.js");
    xAppLoadJsHead("http://192.168.0.110:5500/qq/js/layer.js");
    xAppLoadJsHead("http://192.168.0.110:5500/qq/js/main.js");
}
//_appInit()
//xAppLoadJsHead("resource://android/assets/messaging/testa.js");
//console.log("111111111111xxxxx");
document.body.hidden=false;
//setTimeout("alert('对不起, 要你久候')", 3000 );