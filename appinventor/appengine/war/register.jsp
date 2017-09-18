<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!doctype html>
<html>
    <head>
        <meta charset="utf8">
        <title>注册新账号</title>
        <script src="jquery/jquery-3.2.1.min.js"></script>
        <script src="jquery/jquery-ui.min.js"></script>
        <link rel="stylesheet" href="jquery/jquery-ui.min.css">
        <style>
            .btn-primary {
                color: #fff;
                background-color: #337ab7;
                border-color: #2e6da4;
            }
            .btn-primary:focus,
            .btn-primary.focus {
                color: #fff;
                background-color: #286090;
                border-color: #122b40;
            }
            .btn-primary:hover {
                color: #fff;
                background-color: #286090;
                border-color: #204d74;
            }
        </style>
    </head>
    <body style="background-image: url(images/squairy_light.png);">
        <script>
            $(()=>{
                const root = "";
                
                $("button").button();
                $("#container").css({
                    left: ($(window).width() - $("#container").outerWidth())/2,
                    top: ($(window).height() - $("#container").outerHeight())/3,
                });
                $("#submit").click(()=>{
                    $.ajax({
                        url: root + "/api/user/?action=register",
                        type: "POST",
                        data: encodeURI("email=" + $("#email").val() + "&password=" + $("#password").val()),
                        success: (data)=>{
                            if(data == "OK"){
                                alert("注册成功!");
                                window.location.replace("/");
                            }
                            else
                                alert(data);
                        }
                    });
                });
            });
        </script>
        <div id="container" style="position: absolute; background: white; border: 24px solid white; text-align: center;">
            <img src="/images/codi_long.png"></img>
            <p><h1>注册新账号</h1></p>
            <p>账号: <input type="text" id="email" class="text ui-widget-content ui-corner-all"></p>
            <p>密码: <input type="password" id="password" class="text ui-widget-content ui-corner-all"></p>
            <p><button id="submit" class="btn-primary" style="width: 100%;">注册</button></p>
            <p><button onclick="window.history.back();" style="width: 100%;">返回</button></p>
        </div>
    </body>
</html>