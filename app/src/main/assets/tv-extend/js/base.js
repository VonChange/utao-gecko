let _browser={
   getURL(src){
      return browser.runtime.getURL(src);
   }
};
const _tvLoadRes={
    js(scrJs){
        let script = document.createElement('script');
        script.setAttribute('type', 'text/javascript');
        //charset="utf-8"
        script.setAttribute('charset','utf-8');
        script.src = scrJs;
        script.async = false;
        document.body.appendChild(script); 
    },
      jsC(scrJs){
            let script = document.createElement('script');
            script.setAttribute('type', 'text/javascript');
            //charset="utf-8"
            script.setAttribute('charset','utf-8');
            script.appendChild(document.createTextNode(scrJs))
            document.body.appendChild(script);
        },
    css(scrCss){
        let script = document.createElement('link');
        script.setAttribute('rel', 'stylesheet');
        script.setAttribute('type', 'text/css');
        script.href = scrCss;
        script.async = false;
        document.head.appendChild(script);
    },
      loadCssCode(code) {
           var style = document.createElement('style')
          // style.type = 'text/css'
           style.rel = 'stylesheet'
           style.appendChild(document.createTextNode(code))
           var head = document.getElementsByTagName('head')[0]
           head.appendChild(style)
        },
    jsBottom(scrJs){
        let script = document.createElement('script');
        script.setAttribute('type', 'text/javascript');
        script.setAttribute('charset','utf-8');
        script.src = scrJs;
        script.async = false;
        document.head.appendChild(script);
    }
}
let _api={
    message(service,data){
        myPort.postMessage({ service: service,data:data });
    },
    userAgent(isH5){
        let userAgent="Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.0.0";
        if(isH5){
            userAgent="Mozilla/5.0 (iPhone; CPU iPhone OS 17_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Cronet Mobile/15E148 Safari/604.1";
        }
        return userAgent;
    },
    getHtml(reqUrl,headerStr){
     let header=  JSON.parse(headerStr);
     let result=null;
     $$.ajax({
         type: 'GET',
         url: reqUrl,
         headers:header,
         async: false,
         //dataType: 'json',
         //contentType: "application/json",
         success: function (html) {
             //console.log("html:::"+html);      
             result=html; 
         },
         error: function (xhr, type) {
             console.log('Ajax error!');
             result= "500";
         }
     });
     return result;
    },
    getJson(reqUrl,headerStr){
        let header=  JSON.parse(headerStr);
        let result=null;
        $$.ajax({
            type: 'GET',
            url: reqUrl,
            headers: header,
            async: false,
            dataType: 'json',
            contentType: "application/json",
            success: function (data) {
                result=JSON.stringify(data);
                //console.log("html:::"+html);      
            },
            error: function (xhr, type) {
                console.log('Ajax error!'+reqUrl);
                result= "500";
            }
        });
        return result;
    },
    postJson(reqUrl,headerStr,data){
        let header=  JSON.parse(headerStr);
        let result=null;
        $$.ajax({
            type: 'POST',
            url: reqUrl,
            data:data,
            headers: header,
            async: false,
            dataType: 'json',
            contentType: "application/json",
            success: function (data) {
                result=JSON.stringify(data);
                //console.log("html:::"+html);      
            },
            error: function (xhr, type) {
                console.log('Ajax error!'+reqUrl);
                result= "500";
            }
        });
        return result;
    },
      getCss(reqUrl){
         reqUrl="http://127.0.0.1:8080/tv-web/"+reqUrl;
                  let result=null;
                  $$.ajax({
                      type: 'GET',
                      url: reqUrl,
                      async: false,
                      dataType: 'text/css',
                      contentType: "text/css",
                      success: function (data) {
                          result=data;
                          //console.log("html:::"+html);
                      },
                      error: function (xhr, type) {
                          console.log('Ajax error!'+reqUrl);
                          result= "500";
                      }
                  });
                  return result;
          },
      getJs(reqUrl){
               reqUrl="http://127.0.0.1:8080/tv-web/"+reqUrl;
              let result=null;
              $$.ajax({
                  type: 'GET',
                  url: reqUrl,
                  async: false,
                  dataType: 'application/javascript',
                  contentType: "application/javascript",
                  success: function (data) {
                      result=data;
                      //console.log("html:::"+html);
                  },
                  error: function (xhr, type) {
                      console.log('Ajax error!'+reqUrl);
                      result= "500";
                  }
              });
              return result;
      }
 };

//document.body.hidden=true;
//document.body.innerHTML="<h1>hello23</h1>"
//browser.extension.getURL("page.js")
//exportFunction(_api, window, { defineAs: "_api.gethtml" });
//xAppLoadJsHead(browser.extension.getURL("page.js"));
if(typeof cloneInto!=="undefined"){
    window.wrappedJSObject._api = cloneInto(_api, window, {
        cloneFunctions: true,
    });
    window.wrappedJSObject._browser = cloneInto(_browser, window, {
        cloneFunctions: true,
    });
   // window.wrappedJSObject._tvEnv = cloneInto(_tvEnv, window);
}

var myPort = browser.runtime.connect({ name: "port-from-cs" });
myPort.postMessage({service:"test",data: "hello from content script" });
myPort.onMessage.addListener(function (m) {
  console.log("In content script, received message from background script: "+m.service);
  if(m.service==="sessionStorage"){
    console.log("In content script, sessionStorage ");
        sessionStorage.setItem(m.data.key,m.data.value);
        sessionStorage.setItem(m.data.key+"Time",new Date().getTime());
        return;
  }
  if(m.service==="ctrl"){
      console.log(`from background ${m.data}` );
      _messageCtrl.ctrl(m.data);
      return;
  }
    if(m.service==="loginQr"){
        let loginQr=window.wrappedJSObject._loginQr;
        loginQr(m.data.url,m.data.type);
        return;
    }
    if(m.service==="click"){
        console.log(`from background ${m.data}` );
        $("#"+m.data).trigger("click");
       // document.getElementById(m.data).click();
        return;
    }
    if(m.service==="app"){
        sessionStorage.setItem("_tvRunEnv",m.data);
    }
});
const  _messageCtrl={
    ctrl(message){

        let menuCtrl=null;
        if(typeof _menuCtrl == "undefined"){
            menuCtrl=window.wrappedJSObject._menuCtrl;
        }else{
            menuCtrl=_menuCtrl;
        }
            //||window.wrappedJSObject._menuCtrl;
            //||window.wrappedJSObject._menuCtrl ;
        switch (message){
            case "right":
                menuCtrl.right();
                break;
            case "left":
                menuCtrl.left();
                break;
            case "up":
                menuCtrl.up();
                break;
            case "down":
                menuCtrl.down();
                break;
            case "ok":
                menuCtrl.ok();
                break;
            case "menu":
                menuCtrl.menu();
                break;
            case "back":
                menuCtrl.back();
                break;
        }
    }
}
/* function notify(message) {
    console.log("message");
     console.dir(message);
}
browser.runtime.onMessage.addListener(notify); */
