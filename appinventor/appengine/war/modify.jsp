<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String uid = request.getParameter("uid");
    String email = request.getParameter("email");
    String name = request.getParameter("name");
%>
<!doctype html>
<html>
    <head>
        <meta charset="utf8">
        <title>修改账号信息</title>
        <script src="jquery/jquery-3.2.1.min.js"></script>
        <script src="jquery/jquery-ui.min.js"></script>
        <link rel="stylesheet" href="jquery/jquery-ui.min.css">
    </head>
    <body style="background-image: url(images/squairy_light.png);">
        <script>
            $(()=>{
                const root = "http://127.0.0.1:8888";
                
                $("button").button();
                $("#container").css({
                    left: ($(window).width() - $("#container").outerWidth())/2,
                    top: ($(window).height() - $("#container").outerHeight())/3,
                });
                $("#submit").click(()=>{
                    $.ajax({
                        url: root + "/api/user?action=modify",
                        type: "POST",
                        data: encodeURI("uid=<%=uid%>&name=" + $("#name").val() + "&old=" + $("#old").val() + "&new=" + $("#new").val()),
                        success: (data)=>{
                            if(data == "OK"){
                                alert("修改信息成功!");
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
            <p><h1>修改账号信息</h1></p>
            <p>账号: <%=email%></p>
            <p>显示名称: <input type="text" id="name" value=<%=name%>></p>
            <p>旧密码: <input type="password" id="old" class="text ui-widget-content ui-corner-all"></p>
            <p>新密码: <input type="password" id="new" class="text ui-widget-content ui-corner-all"></p>
            <p><button id="submit" style="width: 100%;">提交</button></p>
            <p><button onclick="window.history.back();" style="width: 100%;">返回</button></p>
        </div>
    </body>
</html>