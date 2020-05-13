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
          </tr>
        </tbody>
      </table>
      <div style="clear:both"></div>
      <table class="tbl_filters">
          <tr>
            <td class="key"></td>
            <td style='padding-top:20px'>
              <Button type="primary" icon="ios-search" @click="doSearch">查询</Button>
              <Button style="margin-left:10px" @click="clear">重置</Button>
            </td>
          </tr>
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
import dateOption from './dateOption';


export default {
  name: "Trace",
  data(){
   return {
     traces:[],
     traceId: "",
     dateOption,
     dateTimeRange: []
   }
  },
  components: {
    // HelloWorld
    tree,
    logHeader
  },
  methods:{
    clear(){
      this.traceId='';
      this.dateTimeRange = [];
      sessionStorage.removeItem('cache_traceId');
      this.doSearch();
    },
    dateChange(){
        let startDate = this.dateTimeRange[0]
        let endDate = this.dateTimeRange[1]
        if(startDate.getHours() == 0 && startDate.getMinutes() ==0 && endDate.getHours() == 0 && endDate.getMinutes() == 0){
          this.dateTimeRange[1].setHours(23,59)
          this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
        }
    },
    doSearch(){
      //列出范围内的日期
      let dateList=[];
      this.traces=[];
      let startDate = _.clone(this.dateTimeRange[0]);
      
      if(startDate){
         while(startDate<=this.dateTimeRange[1]){
          dateList.push($config.prefix_trace+moment(startDate).format('YYYYMMDD'))
          startDate = new Date(startDate.setDate(startDate.getDate()+1));
        }
      }
     
      if(dateList.length==0){
        dateList.push($config.prefix_trace+moment().format('YYYYMMDD'));
      }

      let url= '/getTrace?index='+dateList.join(',')+'&traceId='+this.traceId;

      axios.get(url).then(data=>{
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