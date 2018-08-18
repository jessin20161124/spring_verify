<html>
    <head>
        <title>
            baobaotao
        </title>
    </head>
    <body>
        用户列表
        <table>
            <#list userList as user>
                <tr>
                    <td>
                        ${user.name}
                    </td>
                    <td>
                        ${user.birthday?string("yyyy-MM-dd HH:mm:ss")}
                    </td>
                </tr>
            </#list>
        </table>
    </body>
</html>