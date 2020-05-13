<template>
   <nav class="navbar navbar-dark bg-dark">
        <a class="navbar-brand" href="#"><Icon type="md-list" />&nbsp;EasyLog</a>
            <ul class="navbar-nav">
                <li class="nav-item " :class="{'active':pageName=='Home'}">
                    <a class="nav-link"  href="/#/">日志查询</a>
                </li>
                <li class="nav-item" :class="{'active':pageName=='Trace'}">
                    <a class="nav-link" href="/#/trace">链路追踪</a>
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
        .navbar-nav{
            flex-direction:row;
             .nav-link{
                padding-right: .5rem;
                padding-left: .5rem;
            }
        }
    }
</style>