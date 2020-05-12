<template>
    <div class="mm_tree">
        <div class="info" :class="{'closed':close}">
            <i class="icon"  @click="toggle"></i>
            <div class="title">{{data.method}}</div>
            <div class="time">
                应用名称：{{data.appName}}<br/>花费时间：{{data.end_time - data.start_time}}ms
            </div>
        </div>
        <div v-if="!close" class="children">
            <template v-if="data.children && data.children.length">
                <template v-for="item in data.children">
                    <Tree :key="item.name" :info="item" />
                </template>
            </template>
        </div>

    </div>
</template>
<script>
export default {
    name: "Tree",
    props: {
        info: {
            type: Object,
            required: true
        }
    },
    data(){
        return {
            data:{},
            close:false,
        }
    },
    methods:{
        toggle(){
            if(this.data.children.length>0){
                if(!this.close){
                    this.close=true;
                }
                else
                {
                    this.close=false;
                }
            }
            
            
            // console.log('status:'+this.data.close)
        }
    },
    mounted(){
        this.data = this.info;
    }
}
</script>
<style lang="less" scoped>

    @keyframes scale {
        from {
            transform: scale(0.5)
        }
        to {
            transform: scale(1);
        }
    }

    .mm_tree {
        border-left:1px dotted #ccc;
        text-align: left;
        padding-left:40px;

        .info{
            position: relative;
            padding-bottom: 20px;
            .title{
                font-size:16px;
                font-weight:700;
            }
            .time{
                color:#999;
            }

            .icon{
                display: block;
                position: absolute;
                top:5px;
                left:-27px;
                width:16px;
                height:16px;
                background:none;
                border:2px solid red;
                border-radius: 50%;
            }

            &.closed{
                 .icon::before{
                     content: '';
                     position: absolute;
                     width:12px;
                     height:12px;
                     left:0;
                     top:0;
                     background:red;
                     border-radius: 50%;
                     transform-origin: center;
                     animation-name: scale;
                     animation-duration: 0.5s;
                 }
            }
        }
    }
</style>