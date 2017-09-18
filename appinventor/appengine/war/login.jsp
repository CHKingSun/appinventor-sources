<%@page import="javax.servlet.http.HttpServletRequest"%>
<%@page import="com.google.appinventor.server.util.UriBuilder"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!doctype html>
<%
   String error = request.getParameter("error");
   String useGoogleLabel = (String) request.getAttribute("useGoogleLabel");
   String locale = request.getParameter("locale");
   String redirect = request.getParameter("redirect");
   String repo = (String) request.getAttribute("repo");
   String galleryId = (String) request.getAttribute("galleryId");
   if (locale == null) {
       locale = "zh_CN";
   }

%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta HTTP-EQUIV="pragma" CONTENT="no-cache"/>
    <meta HTTP-EQUIV="Cache-Control" CONTENT="no-cache, must-revalidate"/>
    <meta HTTP-EQUIV="expires" CONTENT="0"/>
    <title>MIT App Inventor</title>
    <script src="/jquery/jquery-3.2.1.min.js"></script>
    <script src="/jquery/jquery-ui.min.js"></script>
    <link rel="stylesheet" href="/jquery/jquery-ui.min.css">
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
    <script>
        $(()=>{
            $(":submit").button();
            $("button").button();
            $("#container").css({
                left: ($(window).width() - $("#container").outerWidth())/2,
                top: ($(window).height() - $("#container").outerHeight())/3,
            });
        });
    </script>
  </head>
    <body style="background-image: url(/images/squairy_light.png);">
        <div id="container" style="position: absolute; background: white; border: 24px solid white; text-align: center;">
        <img src="/images/codi_long.png"></img>
        <h1>${pleaselogin}</h1></center>
        <% if (error != null) {
        out.println("<center><font color=red><b>" + error + "</b></font></center><br/>");
           } %>
        <form method=POST action="/login" autocomplete="new-password">
        <p>${emailAddressLabel}<input type=text name=email value="" size="35" class="text ui-widget-content ui-corner-all"></p>
        <p>${passwordLabel}<input type=password name=password value="" size="35" class="text ui-widget-content ui-corner-all"></p>
        <% if (locale != null && !locale.equals("")) {
           %>
        <input type=hidden name=locale value="<%= locale %>">
        <% }
           if (repo != null && !repo.equals("")) {
           %>
        <input type=hidden name=repo value="<%= repo %>">
        <% }
           if (galleryId != null && !galleryId.equals("")) {
           %>
        <input type=hidden name=galleryId value="<%= galleryId %>">
        <% } %>
        <% if (redirect != null && !redirect.equals("")) {
           %>
        <input type=hidden name=redirect value="<%= redirect %>">
        <% } %>
        <p><input type=Submit value="${login}" style="width:100%;" class="btn-primary"></p>
        </form>
        <p></p>
        <p><button onclick="window.location.href='/register.jsp';" style="width:100%;">注册新账号</button></p>
<!--
<center><p><a href="/login/sendlink"  style="text-decoration:none;">${passwordclickhereLabel}</a></p></center>
-->
<%    if (useGoogleLabel != null && useGoogleLabel.equals("true")) { %>
<p><a href="<%= new UriBuilder("/login/google")
                              .add("locale", locale)
                              .add("repo", repo)
                              .add("galleryId", galleryId)
                              .add("redirect", redirect).build() %>" style="text-decoration:none;">Click Here to use your Google Account to login</a></p>
<%    } %>
<!--
<footer>
<center><a href="<%= new UriBuilder("/login")
                           .add("locale", "zh_CN")
                           .add("repo", repo)
                           .add("galleryId", galleryId)
                           .add("redirect", redirect).build() %>"  style="text-decoration:none;" >中文</a>&nbsp;
<a href="<%= new UriBuilder("/login")
                   .add("locale", "en")
                   .add("repo", repo)
                   .add("galleryId", galleryId)
                   .add("redirect", redirect).build() %>"  style="text-decoration:none;" >English</a></center>
<p></p>
<center>
<%    if (locale != null && locale.equals("zh_CN")) { %>
<a href="http://www.weibo.com/mitappinventor" target="_blank"><img class="img-scale"
                  src="/images/mzl.png" width="30" height="30" title="Sina WeiBo"></a>&nbsp;
<%    } %>
<a href="http://www.appinventor.mit.edu" target="_blank"><img class="img-scale"
                src="/images/login-app-inventor.jpg" width="50" height="30" title="MIT App Inventor"></a></center>
<p></p>

<p style="text-align: center; clear:both;"><a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/"
                                              target="_blank"><img alt="Creative Commons License" src="/images/cc3.png"></a> <br>
  <a rel="license" href="http://creativecommons.org/licenses/by-sa/3.0/" target="_blank"></a></p>
</footer>
-->
        </div>
    </body>
</html>

