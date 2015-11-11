<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<!-- Copyright 2010 Nicholas C. Zakas. All rights reserved. BSD Licensed. -->
<html>
<body>
<script type="text/javascript">
    function getCookie(cname){
        var name = cname + "=";
        var ca = document.cookie.split(';');
        for(var i=0; i<ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0)==' ') c = c.substring(1);
            if (c.indexOf(name) == 0) return c.substring(name.length,c.length);
        }
        return "";
    }
    
(function(){

    //allowed domains
    var whitelist = ["localhost","vipsweb","nibio.no"];

    function verifyOrigin(origin){
        var domain = origin.replace(/^https?:\/\/|:\d{1,4}$/g, "").toLowerCase(),
            i = 0,
            len = whitelist.length;

        while(i < len){
            if (whitelist[i] === domain){
                return true;
            }
            i++;
        }

        return false;
    }

    function handleRequest(event){
        if (verifyOrigin(event.origin)){
            var data = JSON.parse(event.data);
            if(data.type === "localStorage")
            {
                var value = localStorage.getItem(data.key);
                event.source.postMessage(JSON.stringify({id: data.id, type:data.type, key:data.key, value: value}), event.origin);
            }
            else if(data.type === "cookie")
            {
                var value = getCookie(data.name);
                event.source.postMessage(JSON.stringify({id: data.id, type:data.type, name:data.name, value: value}), event.origin);
            }
        }
    }

    if(window.addEventListener){
        window.addEventListener("message", handleRequest, false);
    } else if (window.attachEvent){
        window.attachEvent("onmessage", handleRequest);
    }
})();
</script>
</body>
</html>
