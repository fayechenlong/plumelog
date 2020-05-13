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
    doSearch(){
      //列出范围内的日期
      this.traces=[];

      let url= '/getTrace?traceId='+this.traceId;
      this.$Loading.start();

      axios.get(url).then(data=>{
        this.$Loading.finish();
        this.traces = _.get(data,'data',[])
      })
    }
  },
  mounted(){
    if(this.$route.query.traceId){
      this.traceId = this.$route.query.traceId;
      sessionStorage['cache_traceId']=this.traceId;
      this.doSearch();
    }
    else if(sessionStorage['cache_traceId']){
      this.traceId = sessionStorage['cache_traceId'];
      this.doSearch();
    }
  }
};
</script>
<style lang="less" src="../assets/less/filters.less" scoped></style>
<style lang="less">

</style>