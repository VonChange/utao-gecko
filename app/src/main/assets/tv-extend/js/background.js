console.log("backgroundxx");
var portWeb =null;
let portApp=null;

const _connect={
    init(){
        this.connectWeb();
       // portApp=this.connectApp();
    },
    connectWeb(){
        function connected(p) {
            portWeb = p;
            portWeb.postMessage({service:"test", data: "hi there content script!" });
            portWeb.onMessage.addListener(function (m) {
                console.log("In background script, received message from content script "+m.service);
               // portApp.postMessage(m);
              /*  if(m.service.startsWith("history")){
                    portApp.postMessage(m);
                }
                if(m.service==="exit"){
                    portApp.postMessage(m);
                }*/
            });
        }
        browser.runtime.onConnect.addListener(connected);
    },
    connectApp(){
        console.log("portApp "+"portApp")
        let portApp = browser.runtime.connectNative("browser");
        portApp.onMessage.addListener(response => {
            console.log(`Received APP: ${JSON.stringify(response)}`);
            if(null!=portWeb){
                if(response.service==="sessionStorage"){
                    portWeb.postMessage({service:"sessionStorage",data:JSON.parse(response.data)});
                    return;
                }
                portWeb.postMessage(response);
            }else{
                let index=setInterval(function(){
                    if(null!=portWeb){
                        clearInterval(index);
                        portWeb.postMessage(response);
                    }
                },100);
            }
       });
        portApp.postMessage({service:"run",data:"app connect"});
       return portApp;
    }
}

const  _listener={
    init(){
        this.CompletedListener();
        this.headerListener();
        this.imageLoadListener();
       // this.localListener();
    },
    CompletedListener(){
        function logURL(requestDetails) {
            console.log("requestDetails"+requestDetails.url);
            let url= requestDetails.url;
            if(url.startsWith("https://mesh.if.iqiyi.com/tvg/v2/lw/base_info")){
                //sessionStorage.setItem("iqiyiXj",url);
                portWeb.postMessage({service:"sessionStorage",data:{key:"iqiyiXj",value:url}});
            }
            if(requestDetails.method=="POST"){
                portWeb.postMessage({service:"wxLogin",data:{url:requestDetails.url,statusCode: requestDetails.statusCode} });
                console.log(`Loading:  ${requestDetails.statusCode}`);
            }
        }
        browser.webRequest.onCompleted.addListener(logURL, {
            //<all_urls>
            urls: ["https://mesh.if.iqiyi.com/tvg/v2/lw/base_info*"]
        });
    },
    headerListener(){
        function headerListener(e) {
            for (var i = 0; i < e.requestHeaders.length; ++i) {
                var item = e.requestHeaders[i];
                if(e.type==="xmlhttprequest"){
                    if(item.name==='tv-ref'){
                        e.requestHeaders[i]={
                            name: 'Referer',
                            value: item.value
                        }
                    }
                    if(item.name==='tv-ori'){
                        e.requestHeaders[i]={
                            name: 'Origin',
                            value: item.value
                        }
                    }
                }
                if(item.name=="User-Agentx"&&item.value.includes("Gecko")){
                    console.log("ddddddddddddddddddddddddddddd"+item.value);
                    e.requestHeaders[i]={
                        name: 'User-Agent',
                        value: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0"
                    }
                }
            }
            return {
                requestHeaders: e.requestHeaders
            }
        };
        browser.webRequest.onBeforeSendHeaders.addListener(headerListener, {
            urls: ["<all_urls>"]
        },["blocking", "requestHeaders"]);
    },
    imageLoadListener(){
        browser.webRequest.onBeforeRequest.addListener(function(detail){
            if(!_listener.imageLoadIf(detail.url)){
                return  {cancel: true};
            }
        }, {
            urls: ["<all_urls>"],
            types: ["image","imageset"]
        },["blocking"]);
    },
    imageLoadIf(url){
    if(url.includes('tvImg')){
        return true;
    }
    if(url.includes('open.weixin.qq.com/connect/qrcode')){
        portWeb.postMessage({service:"loginQr",data:{type:"微信",url:url}});
        return true;
    }
    if(url.includes("ssl.ptlogin2.qq.com/ptqrshow")){
        portWeb.postMessage({service:"loginQr",data:{type:"手机端qq",url:url}});
        return true;
    }
    //xcode.png
    if(url.startsWith("https://img.alicdn.com/imgextra/")&&url.endsWith("xcode.png")){
        portWeb.postMessage({service:"loginQr",data:{type:"youkuQr",url:url}});
        return true;
    }
    return false;
 }
}
_connect.init();
_listener.init();






