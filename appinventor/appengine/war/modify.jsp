<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!doctype html>
<html>
    <head>
        <title>修改账号信息</title>
    </head>
    <body>
        <center>
            <img src="/images/codi_long.png"></img>
            <p><h1>修改账号信息</h1></p>
            <%
                String uid = request.getParameter("uid");
                String email = request.getParameter("email");
                String name = request.getParameter("name");
                String err = request.getParameter("error");
                if (err != null)
                    out.println("<p><font color=red><b>" + err + "</b></font></p>");
            %>
            <form action="/api/user/?action=modify" method="POST">
                <input type="hidden" name="uid" value=<%=uid%>></p>
                <p>账号: <%=email%></p>
                <p>显示名称: <input type="text" name="name" value=<%=name%>></p>
                <p>旧密码: <input type="password" name="old"></p>
                <p>新密码: <input type="password" name="new"></p>
                <p><input type="submit" style="font-size: 300%;"></p>
            </form>
            <a href="#" onclick="window.history.back();">返回</a>
        </center>
    </body>
</html>