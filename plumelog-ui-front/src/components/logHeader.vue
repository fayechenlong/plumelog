<template>
    <nav class="navbar navbar-dark bg-dark">
        <a class="navbar-brand icon_log" target="_blank" href="http://www.plumelog.com"></a>
        <ul class="navbar-nav">
            <li class="nav-item " :class="{'active':pageName=='Home'}">
                <a class="nav-link"  href="./#/"><Icon type="logo-buffer" />日志查询</a>
            </li>
            <li class="nav-item" :class="{'active':pageName=='Trace'}">
                <a class="nav-link" href="./#/trace"><Icon type="md-git-merge" />链路追踪</a>
            </li>
            <!-- <li class="nav-item" :class="{'active':pageName=='Chart'}">
                <a class="nav-link" href="./#/chart">数据统计</a>
            </li> -->
            <li class="nav-item" :class="{'active':pageName=='Expand'}">
                <a class="nav-link" href="./#/expand"><Icon type="md-barcode" />扩展字段</a>
            </li>
          <li class="nav-item" :class="{'active':pageName=='Errors'}">
            <a class="nav-link" href="./#/errors"><Icon type="md-pie" />错误统计</a>
          </li>
            <li class="nav-item" :class="{'active':pageName=='Warn'}">
                <a class="nav-link" href="./#/warn"><Icon type="md-alert" />报警管理</a>
            </li>
            <li class="nav-item" :class="{'active':pageName=='Size'}">
                <a class="nav-link" href="./#/size"><Icon type="md-settings" />管理</a>
            </li>
          <li class="nav-item">
            <a class="nav-link" target="_blank" href="https://gitee.com/frankchenlong/plumelog/blob/master/HELP.md"><Icon style="font-size: 16px" type="md-help-circle" /></a>
          </li>
        </ul>
        <span class="quit"><a href="./#/login"><Icon style="font-size: 16px" type="md-exit" />退出</a></span>
        <span class="version"><a target="_blank" href="https://gitee.com/frankchenlong/plumelog">Version 3.4.2</a></span>
    </nav>
</template>
<script>
export default {
    name: "Nav",
    data(){
        return {

        }
    },
    computed:{
        pageName(){
            return this.$route.name;
        }
    },
    mounted(){

    }
}
</script>
<style lang="less">
    .navbar {
        justify-content: flex-start;
        .icon_log
        {
            background: url('../assets/icon_name.png') no-repeat;
            background-size: contain;
            padding-left: 150px;
            height:40px;
        }
        .navbar-nav{
            flex-direction:row;
             .nav-link{
                padding-right: .6rem;
                padding-left: .6rem;
            }
        }
        .version
        {
            position: absolute;
            top:16px;
            right:20px;
            color:#ddd;
          z-index: 2;
          a {
            color: white;
            cursor:pointer
          }
        }
        .quit {
          position: absolute;
          top:16px;
          right:125px;
          color:#ddd;
          z-index: 2;
          cursor: pointer;
          a {
            color: white;
            cursor:pointer
          }
        }
    }
</style>
