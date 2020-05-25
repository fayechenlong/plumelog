<template>
  <div class="pnl_wraper">
    <div class="pnl_filters">
      <log-header></log-header>
      <div style="clear:both"></div>
    </div>
    <div class="pnl_sizes">
      <div class="pnl_size" v-if="sizeInfo.length>0">
        <h4>运行数据</h4>
        <table class="table table-striped">
          <thead>
            <tr>
              <th></th>
              <th>时间</th>
              <th>条数</th>
              <th>大小</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in sizeInfo" :key="item.uuid">
              <td><i :class="item.health"></i></td>
              <td>{{item.index}}</td>
              <td>{{item['docs.count']}}</td>
              <td>{{item['pri.store.size']}}（{{item['store.size']}}）</td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pnl_size" v-if="traceInfo.length>0">
         <h4>链路数据</h4>
        <table class="table table-striped">
          <thead>
            <tr>
              <th></th>
              <th>时间</th>
              <th>条数</th>
              <th>大小</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in traceInfo" :key="item.uuid">
              <td><i :class="item.health"></i></td>
              <td>{{item.index}}</td>
              <td>{{item['docs.count']}}</td>
              <td>{{item['pri.store.size']}}（{{item['store.size']}}）</td>
            </tr>
          </tbody>
        </table>
      </div>
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
import logHeader from '@/components/logHeader.vue'
import "@/assets/less/base.less";


export default {
  name: "Size",
  data(){
   return {
     sizeInfo:[],
     traceInfo:[],
   }
  },
  components: {
    logHeader
  },
  methods:{
    formatData(data){
      //_.orderBy(users, ['user', 'age'], ['asc', 'desc']);

      data =  _.map(data,d=>{
        d.index = d.index.replace('easy_log_trace_','').replace('easy_log_','');
        return d;
      })

      data  = _.orderBy(data, ['index'], ['desc']);

      data =  _.map(data,d=>{

        d.index = d.index.substring(0,4)+'/'+d.index.substring(4,6)+'/'+d.index.substring(6,8)
        return d;
      })

      return data;
    },
    getTraceInfo(){
       this.$Loading.start();
       axios.post('/getServerInfo?index=easy_log_2*').then(data=>{
         this.$Loading.finish();
         this.sizeInfo = this.formatData(_.get(data,'data',[]));
       })

        axios.post('/getServerInfo?index=easy_log_trace*').then(data=>{
         this.$Loading.finish();
         this.traceInfo = this.formatData( _.get(data,'data',[]));
       })
    }
  },
  mounted(){
    this.getTraceInfo();
  }
};
</script>
<style lang="less" src="../assets/less/filters.less" scoped></style>
<style lang="less">
  .pnl_sizes
  {
    display: flex;
    // flex-direction: column; 
    // flex-wrap: nowrap;
    // justify-content: center;
    .pnl_size
    {
      text-align:left;
      flex: 1;
      width:500px;
      height:500px;
      margin:0 20px;
      table{
        margin-top:20px;
        width:100%;
        th{
          text-align: center;
          height: 30px;
        }
        td{
          text-align: center;
          height: 30px;
          i{
            display: inline-block;
            width:10px;
            height:10px;
            border-radius: 50%;
            &.green{
              background: green;
            }
            &.yellow{
              background:rgba(255, 166, 0, 0.856);
            }
            &.red{
              background:red;
            }
          }
        }
      }
    }
  }
</style>