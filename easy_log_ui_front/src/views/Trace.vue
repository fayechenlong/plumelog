<template>
  <div class="pnl_wraper">
    <div class="pnl_filters">
      <log-header></log-header>
      <table class='tbl_filters' >
        <tbody>
          <tr>
            <td class="key">追踪码</td>
            <td>
              <Input class="txt" name="appName" v-model="traceId" placeholder="输入追踪码" :clearable="true" />
            </td>
            <td style="padding-left:20px">
              <Button type="primary" icon="ios-search" @click="doSearch">查询</Button>
            </td>
          </tr>
        </tbody>
      </table>
      <div style="clear:both"></div>
    </div>
    <div v-for="traceInfo in traces" :key="traceInfo.method" style="width:1020px;float:left;margin-top:20px;margin-left:50px;">
      <tree :info="traceInfo" />
    </div>
  </div>
</template>

<script>
// @ is an alias to /src
// import HelloWorld from "@/components/HelloWorld.vue";
import axios from '@/services/http'
import _ from 'lodash'
import moment from 'moment'
import '@/assets/prism.js' 
import '@/assets/prism.css'
import 'view-design/dist/styles/iview.css';
import * as $config from '@/config.json'
import tree from '@/components/tree.vue'
import logHeader from '@/components/logHeader.vue'
import "@/assets/less/base.less";


export default {
  name: "Trace",
  data(){
   return {
     traces:[],
     traceId: ""
   }
  },
  components: {
    // HelloWorld
    tree,
    logHeader
  },
  methods:{
    formartTrace(list){
      //todo:检测数据是否闭合（判断<和>的数量是否一致）
      let zIndex=0;
      let _list = [];

      function pushItem(item,isStart){

        let _arrary=_list;

        //找到该层级的最后一个元素往里插
        for(var i=0;i<zIndex;i++){
            _arrary = _arrary[_arrary.length-1].children;
        }
        
        //方法开始
        if(isStart){
          _arrary.push({
            method: item.method,
            appName: item.appName,
            start_time: item.time,
            zIndex: zIndex,
            children:[]
          });
        }
        //方法结束
        else
        {
          //找到一个没结束的item
          for(var f=0;f<_arrary.length;f++){
            if(!_arrary[f].end_time){
              _arrary[f].end_time = item.time;
              break
            }
          }
        }
      }

      for(var i=0;i<list.length;i++){
        //如果postion是 '<' 说明是上一个方法的子方法
        if(list[i]['position']=='<'){
          pushItem(list[i],true)
          zIndex++;

        }
        else if(list[i]['position']=='>')
        {
          zIndex--;
          pushItem(list[i],false)
        }
      }

      return _list;
    },
    doSearch(){
      //列出范围内的日期
      this.traces=[];
      sessionStorage['cache_traceId'] = this.traceId;

      let url= '/query?index=easy_log_trace_*&size=1000&from=0';

      let filter = {
        "query": {
          "bool": {
            "must": [{
              "match": {
                "traceId": {
                  "query": this.traceId
                }
              }
            }]
          }
        },
        "sort": [{
          "time":"asc",
          "positionNum": "asc"
        }]
      };

      this.$Loading.start();

      axios.post(url,filter).then(data=>{
        this.$Loading.finish();
        let hits = [];

        let _hits = [];
        _hits = _.get(data,'data.hits.hits',[]);

        _hits.map(hit=>{
          hits.push(hit._source)
        })
        if(hits.length>0)
        {
          this.traces = this.formartTrace(hits)
        }
      })
    }
  },
  mounted(){
    if(this.$route.query.traceId){
      this.traceId = this.$route.query.traceId;
    }
    else if(sessionStorage['cache_traceId']){
      this.traceId = sessionStorage['cache_traceId'];
    }
    this.doSearch();
  }
};
</script>
<style lang="less" src="../assets/less/filters.less" scoped></style>
<style lang="less">

</style>