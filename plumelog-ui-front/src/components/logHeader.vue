<template>
   <nav class="navbar navbar-dark bg-dark">
        <a class="navbar-brand icon_log" href="./#/"></a>
            <ul class="navbar-nav">
                <li class="nav-item " :class="{'active':pageName=='Home'}">
                    <a class="nav-link"  href="./#/">日志查询</a>
                </li>
                <li class="nav-item" :class="{'active':pageName=='Trace'}">
                    <a class="nav-link" href="./#/trace">链路追踪</a>
                </li>
                <!-- <li class="nav-item" :class="{'active':pageName=='Chart'}">
                    <a class="nav-link" href="./#/chart">数据统计</a>
                </li> -->
                 <li class="nav-item" :class="{'active':pageName=='Size'}">
                    <a class="nav-link" href="./#/size">管理</a>
                </li>
            </ul>
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
                padding-right: .5rem;
                padding-left: .5rem;
            }
        }
    }
</style>