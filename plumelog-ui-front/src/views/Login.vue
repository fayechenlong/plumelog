<template>
    <div class="pnl_wraper">
        <div class="pnl_content">
            <div class="pnl_desc">
                <div class="title">PlumeLog</div>
                <div class="desc">Java分布式日志组件，支持百亿级别，日志从搜集到查询，不用去文件中翻阅日志方便快捷</div>
            </div>
            <div class="pnl_login">
                <div class="title">登录</div>
                <div class="pnl_input">
                    <input type="text" placeholder="输入用户名" id="txtUserName" /><br/>
                    <input type="password" placeholder="输入密码" id="txtPassWord" /><br/>
                    <input type="button" value="提交" id="btn_login"/>
                </div>
            </div>
        </div>
    </div>
</template>
<script>
    export default {
        name: "Login"
    }
</script>
<style lang="less" scoped>
    .pnl_wraper{
        color:#fff;
        background: url("../assets/black_dot.png") 0% 0% repeat;
        height: 100vh;
        .pnl_content
        {
            width:1100px;
            margin:0 auto;
            position: relative;
            .pnl_desc
            {
                position: absolute;
                top:200px;
                left:0;
                text-align: left;
                font-family: Inter,-apple-system,BlinkMacSystemFont,Segoe UI,Helvetica,Arial,sans-serif,Apple Color Emoji,Segoe UI Emoji,Segoe UI Symbol;
                width:500px;

                .title{
                    font-size:50px;
                }
                .desc{
                    font-size:20px;
                }
            }
            .pnl_login
            {
                position: absolute;
                top: 100px;
                right: 0;
                height:450px;
                width:450px;
                border-radius: 20px;
                background: #fff;

                .title{
                    margin-top: 30px;
                    font-size:32px;
                    color:#000;
                }

                .pnl_input
                {
                    margin-top:50px;
                    #txtUserName,#txtPassWord
                    {
                        outline-color: #5584ff;
                        outline-width: 1px;
                        height:50px;
                        font-size:16px;
                        text-indent: 10px;
                        border:1px solid #ccc;
                        width:300px;
                        margin-bottom: 30px;
                    }
                }

                #btn_login
                {
                    display: inline-block;
                    width:300px;
                    height:50px;
                    line-height: 50px;
                    color:#fff;
                    font-size:16px;
                    background: #5584ff;
                    border:none;
                }
            }
        }
    }
</style>