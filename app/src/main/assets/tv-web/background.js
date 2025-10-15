console.log("backgroundxx");
var portWeb =null;
let portApp=null;
let processedULinkPages = new Set();

const _connect={
    init(){
        this.connectWeb();
        portApp=this.connectApp();
    },
    connectWeb(){
        function connected(p) {
            portWeb = p;
            portWeb.postMessage({service:"test", data: "hi there content script!" });
            portWeb.onMessage.addListener(function (m) {
                console.log("In background script, received message from content script "+m.service);
                portApp.postMessage(m);
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
        //this.startListener();
        this.CompletedListener();
        this.headerListener();
        this.imageLoadListener();
        this.uLinkListener();

    },
    CompletedListener(){
        function postToWeb(msg){
            if(null!=portWeb){
                portWeb.postMessage(msg);
            }else{
                let index=setInterval(function(){
                    if(null!=portWeb){
                        clearInterval(index);
                        portWeb.postMessage(msg);
                    }
                },100);
            }
        }
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
            // handle pages containing 'u-link=1': extract m3u8 and write to sessionStorage
            try{
                if(requestDetails.type==="main_frame" && url.includes("u-link=1")){
                    if(processedULinkPages.has(url)){
                        return;
                    }
                    processedULinkPages.add(url);
                    // write current page url
                    postToWeb({service:"sessionStorage",data:{key:"u-loc",value:url}});
                    // fetch page content to extract m3u8
                    fetch(url, {credentials: "include"}).then(r=>{
                        if(!r.ok){
                            throw new Error("fetch status "+r.status);
                        }
                        return r.text();
                    }).then(html=>{
                        // simple regex to find first m3u8 url
                        const m = html.match(/https?:\/\/[^\s"'<>]+?\.m3u8(?:\?[^\s"'<>]*)?/i);
                        if(m && m[0]){
                            postToWeb({service:"sessionStorage",data:{key:"u-m3u8",value:m[0]}});
                        }
                    }).catch(e=>{
                        console.log("u-link m3u8 fetch error: "+e.message);
                    });
                }
            }catch(e){
                console.log("u-link handler error: "+e.message);
            }
        }
        browser.webRequest.onCompleted.addListener(logURL, {
            //<all_urls>
            urls: ["https://mesh.if.iqiyi.com/tvg/v2/lw/base_info*"]
        });
        // watch all main-frame navigations to handle u-link=1 pages
        browser.webRequest.onCompleted.addListener(logURL, {
            urls: ["<all_urls>"],
            types: ["main_frame"]
        });
    },
    uLinkListener(){
        function postToWeb(msg){
            if(null!=portWeb){
                portWeb.postMessage(msg);
            }else{
                let index=setInterval(function(){
                    if(null!=portWeb){
                        clearInterval(index);
                        portWeb.postMessage(msg);
                    }
                },100);
            }
        }
        function onBefore(details){
            try{
                let url = details.url || "";
                if(details.type==="main_frame" && url.includes("u-link=1")){
                    console.log("u-link onBeforeRequest:"+url);
                    if(processedULinkPages.has(url)){
                        return;
                    }
                    processedULinkPages.add(url);
                    postToWeb({service:"sessionStorage",data:{key:"u-loc",value:url}});
                    fetch(url, {credentials: "include"}).then(r=>{
                        if(!r.ok){
                            throw new Error("fetch status "+r.status);
                        }
                        return r.text();
                    }).then(html=>{
                        const m = html.match(/https?:\/\/[^\s"'<>]+?\.m3u8(?:\?[^\s"'<>]*)?/i);
                        if(m && m[0]){
                            postToWeb({service:"sessionStorage",data:{key:"u-m3u8",value:m[0]}});
                        }
                    }).catch(e=>{
                        console.log("u-link fetch error: "+e.message);
                    });
                }
            }catch(e){
                console.log("u-link before handler error: "+e.message);
            }
        }
        browser.webRequest.onBeforeRequest.addListener(onBefore, {
            urls: ["<all_urls>"]
        });
    },
    startListener(){
        function logURL(requestDetails) {
            console.log("requestDetails" + requestDetails.url);
            let originalUrl = requestDetails.url;
            // 示例：将 example.com 替换为 example.org
            let modifiedUrl = originalUrl.replace("tlive.fengshows.com", "qctv.fengshows.cn");
            console.log("modifiedUrl"+modifiedUrl);
            // 2. 修改请求头
            let newHeaders = new Headers(requestDetails.requestHeaders || {});

            // 添加/修改头
            newHeaders.set("Origin", "qctv.fengshows.cn");
            //newHeaders.set("User-Agent", "Mozilla/5.0 (Custom GeckoView)");, requestHeaders: Array.from(newHeaders.entries())
            return {redirectUrl: modifiedUrl};
        }
        browser.webRequest.onBeforeRequest.addListener(logURL, {
            //<all_urls>
            urls: ["https://tlive.fengshows.com/live/*"]
        },  ["blocking"] );
    },
    ssl(){
        // 拦截证书错误并允许加载
        browser.webRequest.onHeadersReceived.addListener(
            details => {
                return {
                    cancel: false, // 不取消请求
                    responseHeaders: details.responseHeaders.filter(h =>
                        !(h.name.toLowerCase() === 'strict-transport-security')
                    )
                };
            },
            { urls: ["https://qctv.fengshows.cn/live/*"] },
            ["blocking", "responseHeaders", "extraHeaders"]
        );
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
    if(url.includes("cctvpic.com")){
        return true;
    }
    if(url.includes('open.weixin.qq.com/connect/qrcode')){
        portWeb.postMessage({service:"loginQr",data:{type:"微信",url:url}});
        return true;
    }
    //xui.ptlogin2.qq.com/ssl/ptqrshow?
    if(url.includes("ptlogin2.qq.com/ssl/ptqrshow")){
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






