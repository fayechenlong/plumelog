<template>
   <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
        <a class="navbar-brand" href="#"><Icon type="md-list" />&nbsp;EasyLog</a>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNav" aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav">
                <li class="nav-item " :class="{'active':pageName=='Home'}">
                    <a class="nav-link"  href="/#/">日志查询</a>
                </li>
                <li class="nav-item" :class="{'active':pageName=='Trace'}">
                    <a class="nav-link" href="/#/trace">链路追踪</a>
                </li>
            </ul>
        </div>
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