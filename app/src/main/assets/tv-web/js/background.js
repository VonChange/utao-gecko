console.log("backgroundxx");
var portWeb =null;
let portApp=null;

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
        //this.ssl();
        //this.CompletedListener();
        this.headerListener();
        this.imageLoadListener();
        this.m3u8Listener();
		this.tvWebRedirectListener();
		this.fengshowsRedirectListener();

    },
    fengshowsRedirectListener(){
        function onBefore(details){
            try{
                const orgUrl = details.url || "";
                if(orgUrl.startsWith("https://tlive.fengshows.com/live/") || orgUrl.startsWith("https://hkmolive.fengshows.com/live/")){
                    const idx = orgUrl.indexOf("/live");
                    const realUrl = "http://qctv.fengshows.cn" + (idx !== -1 ? orgUrl.substring(idx) : "/live");
                    console.log("fengshows redirect:", orgUrl, "->", realUrl);
                   //  portApp.postMessage({service:"videoUrl",data:realUrl});
                    //return { redirectUrl: realUrl };
                }
            }catch(e){
                console.log("fengshowsRedirectListener error: "+e.message);
            }
        }
        browser.webRequest.onBeforeRequest.addListener(onBefore, {
            urls: [
                "https://tlive.fengshows.com/live/*",
                "https://hkmolive.fengshows.com/live/*"
            ]
        }, ["blocking"]);
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
	tvWebRedirectListener(){
		function onBefore(details){
			try{
				const url = details.url || "";
				const marker = "/tv-web/";
				const idx = url.indexOf(marker);
				if(idx === -1){
					return;
				}
				let suffix = url.substring(idx + marker.length); // includes path + query + hash
				if(suffix.startsWith('/')){
					suffix = suffix.substring(1);
				}
				if(!suffix){
					suffix = "index.html";
				}
				const redirectUrl = browser.runtime.getURL(suffix);
				console.log("tv-web redirect:", url, "->", redirectUrl);
                portWeb.postMessage({service:"redirect",data:{url:redirectUrl} });
                return { redirectUrl: redirectUrl };
				// Use a data: URL that performs in-page navigation to avoid tab API
				//const html = "<!doctype html><meta charset=\\\"utf-8\\\"><script>location.replace('" + redirectUrl.replace(/'/g, "\\\\'") + "');<\\/script>";
				//return { redirectUrl: "data:text/html;charset=utf-8," + encodeURIComponent(html) };
			}catch(e){
				console.log("tvWebRedirectListener error: "+e.message);
			}
		}
		browser.webRequest.onBeforeRequest.addListener(onBefore, {
			urls: ["<all_urls>"],
			types: ["main_frame"]
		}, ["blocking"]);
	},
    m3u8Listener(){
        function safePost(msg){
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
                const reqUrl = details.url || "";
                if(!reqUrl || reqUrl.indexOf('.m3u8')===-1){
                    return;
                }
                // 页面地址（发起该请求的文档）
                const locUrl = details.documentUrl || details.originUrl || "";
                if(!locUrl || locUrl.indexOf('u-link=1')===-1){
                    return;
                }
                console.log("m3u8 captured:", reqUrl, " loc:", locUrl);
                safePost({service:"sessionStorage",data:{key:"u-loc",value:locUrl}});
                safePost({service:"sessionStorage",data:{key:"u-m3u8",value:reqUrl}});
            }catch(e){
                console.log("m3u8Listener error: "+e.message);
            }
        }
        browser.webRequest.onBeforeRequest.addListener(onBefore, {
            urls: ["<all_urls>"]
        });
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
            ["blocking", "responseHeaders"]
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








