<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!doctype html>
<html>
    <head>
        <title>注册新账号</title>
    </head>
    <body>
        <center>
            <img src="/images/codi_long.png"></img>
            <p><h1>注册新账号</h1></p>
            <%
                String err = request.getParameter("error");
                if (err != null)
                    out.println("<p><font color=red><b>" + err + "</b></font></p>");
            %>
            <form action="/api/user/?action=register" method="POST">
                <p>账号: <input type="text" name="email"></p>
                <p>密码: <input type="password" name="password"></p>
                <p><input type="submit" style="font-size: 300%;"></p>
            </form>
            <a href="#" onclick="window.history.back();">返回</a>
        </center>
    </body>
</html>