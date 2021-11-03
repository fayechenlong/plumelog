<style lang="less" scoped>

</style>
<template>
    <div>
        <Row class="expand-row">
            <Col span="24" style="min-height: 50px; max-height: 200px; overflow: scroll">
               <ul style="padding: 5px 0">
                   <li v-for="item in list">
                        <span class="key">{{item.key}}</span>&nbsp;&nbsp;
                        <span class="value">{{item.doc_count}}条</span>
                   </li>
               </ul>
            </Col>
        </Row>
    </div>
</template>
<style lang="less" scoped>
    li {
        padding-left:150px;
        text-align:left;
        .key {
            display:inline-block;
            width:180px;
            font-weight:700;
            text-align:left;
        }
        .value {
            padding-left:20px;
        }
    }
</style>
<script>
    import _ from 'lodash'
    import axios from '@/services/http'

    export default {
        props: {
            index: String,
            row: Object,
        },
        data(){
            return {
                list:[]
            }
        },
        methods:{
            getAppNameCount(index){
                let query = {
                    "size": 0,
                    "aggregations": {
                        "dataCount": {
                            "terms": {
                                "size": 1000,
                                "field": "appName"
                            }
                        }
                    }
                }
                let url= process.env.VUE_APP_API+'/plumelogServer/query?size=0&from=0&index='+index;
                axios.post(url,query).then(data=>{
                    this.list = _.get(data,'data.aggregations.dataCount.buckets',[])
                })
            }
        },
        mounted(){
            this.getAppNameCount(this.row.index);
        }
    };
</script>
