<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!doctype html>
<% String uid = request.getParameter("uid"); %>
<html>
    <head>
        <meta charset="utf8">
        <title></title>
        <script src="../jquery/jquery-3.2.1.min.js"></script>
        <script src="../jquery/jquery-ui.min.js"></script>
        <link rel="stylesheet" href="../jquery/jquery-ui.min.css">
        <link rel="stylesheet" href="../jquery/jquery.dropdown.min.css">
        <script src="../jquery/jquery.dropdown.min.js"></script>
        <style>
            .hover{background-color: #e9e9e9;}
            .selected{background-color: #0099cc;}
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
    <body>
        <script>
            const root = "http://127.0.0.1:8888";
            var username = "";
            var selection;
            
            $(()=>{
                $("button").button();
                initUserName();

                $("#deleteProject").click(()=>{
                    if(!selection){
                        alert("未选择项目");
                        return;
                    }
                    if(!confirm("确定删除项目?"))
                        return;
                    
                    $.ajax({
                        url: root + "/api/admin?action=deleteProject&uid=<%=uid%>&pid=" + encodeURIComponent(selection),
                        type: "POST",
                        success: (data)=>{
                            if(data == "OK")
                                initUserProjects();
                            else
                                alert(data);
                        }
                    });
                });
                
                $("#confirmImportProject").click(()=>{
                    var files = $("#fileImportProject").prop("files");
                    if(files.length == 0){
                        alert("未选择文件");
                        return;
                    }
                    
                    var parts = files[0].name.split(/\./);
                    var name = parts[0];
                    var extension = parts[1];
                    if(extension != "aia"){
                        alert("文件扩展名应为aia");
                        return;
                    }
                    
                    var reader = new FileReader();
                    reader.readAsBinaryString(files[0]);
                    reader.onload = (e)=>{
                        var data = btoa(e.target.result);
                        $.ajax({
                            url: root + "/api/file?action=importProject",
                            type: "POST",
                            data: "users=" + encodeURIComponent("[\"<%=uid%>\"]") + "&name=" + encodeURIComponent(name) + "&content=" + encodeURIComponent(data),
                            success: (data)=>{
                                if(data == "OK"){
                                    alert("上传成功");
                                    initUserProjects();
                                }
                            }
                        });
                    };
                });
                
                $("#exportProject").click(()=>{
                    if(!selection){
                        if(confirm("未选择用户, 是否导出所有项目?"))
                            window.open("/api/file?action=exportAllProjectsForUser&uid=<%=uid%>");
                    }
                    else
                        window.open(encodeURI("/api/file?action=exportProject&uid=<%=uid%>&pid=" + selection));
                });
                
                $("#doTextFilter").click(()=>{
                    var text = $("#filterText").val();
                    if($("#useRegex").prop("checked")){
                        var regex = new RegExp(text);
                        chainFilter((row)=>regex.test(row.attr("name")));
                    }
                    else
                        chainFilter((row)=>row.attr("name").indexOf(text) != -1);
                });
                
                $("#resetFilter").click(()=>{
                    selection = null;
                    $("tr").removeClass("selected");
                    $("#filterText").val("");
                    $("#content").children().show();
                });
            });
            
            function initUserName(){
                $.ajax({
                    url: root + "/api/user",
                    type: "GET",
                    success: (data)=>{
                        var json = JSON.parse(data);
                        for(var user of json){
                            if(user["uid"] == "<%=uid%>"){
                                userName = user["email"];
                                $("#title").text("用户" + userName + "的项目列表");
                                break;
                            }
                        }
                        if(userName)
                            initUserProjects();
                    }
                });
            }
            
            function initUserProjects(){
                $.ajax({
                    url: root + "/api/file?action=userProjects&uid=<%=uid%>",
                    type: "GET",
                    success: (data)=>{
                        var json = JSON.parse(data);
                        $("#content").empty();
                        for(var project of json){
                            var tr = $("<tr>");
                            tr.attr("pid", project["pid"]);
                            tr.attr("name", project["name"]);
                            
                            $("<td>").text(project["name"]).appendTo(tr);
                            
                            var viewFilesLink = $("<a>");
                            viewFilesLink.attr("href", encodeURI("/admin/files.jsp?uid=<%=uid%>&pid=" + project["pid"]));
                            viewFilesLink.text("查看文件...");
                            $("<td>").append(viewFilesLink).appendTo(tr);
                            
                            $("<td>").text(formatDate(project["dateCreated"])).appendTo(tr);
                            $("<td>").text(formatDate(project["dateModified"])).appendTo(tr);
                            
                            $("#content").append(tr);
                            
                            tr.click(function(){
                                selection = $(this).attr("pid");
                                $("tr").removeClass("selected");
                                $(this).addClass("selected");
                            });
                            
                            tr.hover(function(){
                                    $(this).addClass("hover");
                                }, 
                                function(){
                                    $(this).removeClass("hover");
                                }
                            );
                        }
                    }
                });
            }
            
            function formatDate(time){
                return (time == 0) ? "未知" : new Date(time).toLocaleString();
            }
            
            function chainFilter(filterFunc){
                $("tr").removeClass("selected");
                var rows = $("#content").children();
                for(var i=0;i<rows.length;i++){
                    var row = $(rows[i]);
                    if(row.is(":hidden"))
                        continue;
                    if(filterFunc(row))
                        row.show();
                    else
                        row.hide();
                }
            }
        </script>
        <h1 id="title"></h1>
        <p>
            <button onclick="window.location.reload()">
                <span class="ui-icon ui-icon-arrowrefresh-1-s"></span>刷新
            </button>
        </p>
        <p>
            筛选项目名称:&nbsp;<input type="text" id="filterText" class="text ui-widget-content ui-corner-all" />
            <input type="checkbox" id="useRegex"/>正则表达式
            <button id="doTextFilter" class="btn-primary">确定</button>
            <button id="resetFilter">重置</button>
        </p>
        <p>
            操作:
            <button id="importProject" data-jq-dropdown="#dropdown_importProject">
                <span class="ui-icon ui-icon-script"></span>导入项目
            </button>
            <button id="deleteProject">
                <span class="ui-icon ui-icon-minusthick"></span>删除项目
            </button>
            <button id="exportProject">
                <span class="ui-icon ui-icon-copy"></span>导出项目
            </button>
        </p>
        <div id="dropdown_importProject" class="jq-dropdown jq-dropdown-tip">
            <div class="jq-dropdown-panel">
                <input type="file" id="fileImportProject"/>
                <button id="confirmImportProject" class="btn-primary">确定</button>
            </div>
        </div>
        <table class="ui-widget ui-widget-content ui-corner-all" style="text-align: center; width: 100%;">
            <thead>
                <tr class="ui-widget-header">
                    <th>项目名</th>
                    <th>文件</th>
                    <th>创建时间</th>
                    <th>最后修改时间</th>
                </tr>
            </thead>
            <tbody id="content"></tbody>
        </table>
    </body>
    </body>
</html>