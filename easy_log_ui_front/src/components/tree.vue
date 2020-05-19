<template>
    <div class="mm_tree">
        <div class="info" :class="{'closed':close}">
            <i @click="toggle"><Tooltip :disabled="toolTip==''" offset="-22"  placement="left-start" class="icon" :class="{'disable':toolTip==''}" :content="toolTip"></Tooltip></i>
            <div class="title">{{data.method}}</div>
            <div class="time">
                应用名称：{{data.appName}}<template v-if="data.end_time>=data.start_time"><br/>花费时间：{{data.end_time - data.start_time}}ms</template>
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
    computed:{
        toolTip(){
            return this.data.children && this.data.children.length ? this.close ? '点击展开':'点击收起' : ''
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
        if(this.data.zIndex>1 && this.data.children.length>0)
        {
            this.close = true;
        }
        else
        {
            this.close =false;
        }
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
                cursor: pointer;
                &.disable
                {
                    cursor: auto;
                }
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