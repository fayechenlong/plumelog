<template>
  <div class="pnl_wraper">
    <div class="pnl_filters">
      <log-header></log-header>
      <div style="clear:both"></div>
    </div>
    <Row >
      <Col :span="8">
        请选择日期：<DatePicker ref='datePicker' v-model="currentDate" @on-change="dateChange" type="date"  format="yyyy-MM-dd" placeholder="选择日期" style="width: 200px"></DatePicker>
        <Button type="primary" style="margin-left: 10px" shape="circle" icon="ios-search" @click="dateChange"></Button>

      </Col>
    </Row>
    <div class="pnl_sizes">
      <Tabs active-key="运行数据" @on-click="changeTab">
        <Tab-pane label="运行数据" name="run" key="运行数据">
           <div class="pnl_size" v-if="sizeInfo.length>0">
             <Table height="600" @on-selection-change="changeSizeSelect" :content="self" :columns="columns_size" :data="sizeInfo">
               <template slot-scope="{ row, index }" slot="action">
                <Button type="info" size="small" @click="showDetail(index)">详情</Button>
               </template>
             </Table>
          </div>
        </Tab-pane>
        <Tab-pane label="链路数据" name="trace" key="链路数据">
          <div class="pnl_size" v-if="traceInfo.length>0">
             <Table height="600" @on-selection-change="changeTraceSelect" :content="self" :columns="columns_size" :data="traceInfo">
               <template slot-scope="{ row, index }" slot="action">
                <Button type="error" size="small" @click="remove(index)">删除</Button>
               </template>
             </Table>
          </div>
        </Tab-pane>
      </Tabs>
      <Button icon="ios-trash" :disabled="isDisabled" class="btn_delete" @click="checkRemove" type="error">清空所选</Button>
    </div>
    <div class="modal fade show model_pwd" style="display:block" v-if="showModal" tabindex="-1" role="dialog">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">管理密码</h5>
            <button type="button" class="close" @click="closeModal" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
            <p><input type="password" autofocus v-model="password" placeholder="输入管理员密码" class="form-control"  /></p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-dismiss="modal" @click="closeModal">关闭</button>
            <button type="button" class="btn btn-primary" @click="confirmModal">确认</button>
          </div>
        </div>
      </div>
    </div>
    <Row style="margin-top:20px" type="flex" justify="start">
      <Col :span="8">
        <span style="padding: 10px;">当前redis队列大小 日志队列：{{runSize}} 追踪队列： {{traceSize}}</span>
        <Button type="error" style="margin-left: 10px" icon="ios-trash" @click="clearRedisQueue">清空队列</Button>
      </Col>
    </Row>
     <div class="modal-backdrop fade show" v-if="showModal"></div>
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
import expandRow from '@/components/size-expand.vue';


export default {
  name: "Size",
  data(){
   return {
     runSize:0,
     traceSize:0,
     timer:null,
     clearQueue:false,
     currentDate:null,
     showModal:false,
     password:'',
     self:this,
     size_selection:[],
     trace_selection:[],
     currentTab:'run',
     columns_size:[
       {
        type: 'selection',
        width: 60,
        align: 'center'
      },
      {
        type: 'expand',
        width: 50,
        render: (h, params) => {
            return h(expandRow, {
                props: {
                    row: params.row,
                }
            })
        }
      },
      {
        title: '健康',
        key:'health',
         width: 100,
        render: (h, params) => {
            return h('div', [
                h('i', {
                   'class':params.row.health
                })
            ]);
        }
      },
      {
        title: '时间',
        key:'index',
        sortable: true,
        sortType:"desc",//初始化排序
        render:(h,params)=>{
          let _index = params.row.index.replace('plume_log_trace_','').replace('plume_log_run_','')
          if(_index.length==8){
            _index = _index.substring(0,4)+'-'+_index.substring(4,6)+'-'+_index.substring(6,8)
          } else if(_index.length == 10) {
            _index = _index.substring(0,4)+'-'+_index.substring(4,6)+'-'+_index.substring(6,8) + " " + _index.substring(8,10)
          }
          return h('span',_index)
        }
      },
      {
        title:'条数',
        key:'docs.count',
        sortable: true
      },
      {
        title:'大小',
        key:'pri.store.size',
        sortable: true,
        render: (h, params) => {
            return h('div', [
                h('span', params.row['pri.store.size']+'（'+ params.row['store.size']+'）')
            ]);
        }
      }
     ],
     sizeInfo:[],
     traceInfo:[],
   }
  },
  computed:{
    isDisabled(){
      let isDisabled = true;
      if(this.currentTab=='run' && this.size_selection.length>0){
        isDisabled = false;
      }
      else if(this.currentTab=='trace' && this.trace_selection.length>0){
        isDisabled = false;
      }
      return isDisabled;
    }
  },
  components: {
    logHeader,
    expandRow
  },
  methods:{
    dateChange() {
      this.getTraceInfo()
    },
    closeModal(){
      this.showModal = false;
      this.clearQueue = false;
      this.password = '';
    },
    changeTab(name){
      this.currentTab = name;
    },
    checkRemove(){
     this.showModal = true;
    },
    confirmModal(){
      this.showModal = false;
      if(this.clearQueue) {
        this.clearQueueHandler()
      } else {
        this.removeSelect();
      }
    },
    clearRedisQueue() {
      this.showModal = true;
      this.clearQueue = true;
    },
    clearQueueHandler() {
      this.clearQueue= false
      axios.post(process.env.VUE_APP_API+'/deleteQueue?adminPassWord='+this.password).then(res=> {
        if(res.data.acknowledged) {
          alert('删除成功');
          this.password = '';
        }
      })
    },
    getQueueSize() {
      axios.post(process.env.VUE_APP_API+'/getQueueCounts').then(res=> {
        if(res.data.runSize > -1) {
            this.runSize = res.data.runSize
            this.traceSize = res.data.traceSize
        }
      })
    },
    removeSelect(){

      var selected = this.currentTab == 'run' ? this.size_selection : this.trace_selection;
      
      let deletePromise=[];
      for(var item of selected)
      {
        deletePromise.push(axios.post(process.env.VUE_APP_API+'/deleteIndex?index='+item.index+'&adminPassWord='+this.password))
      }
      Promise.all(deletePromise).then(results=>{
        let successResults=[];

        for(var result of results){
          if(result.data.acknowledged){
            successResults.push(result);
          }
        }
        if(successResults.length == results.length){
          //全部删除成功
          alert('删除成功');
          this.password = '';
        }
        else
        {
          alert(results[0].data.message);
        }
        this.getTraceInfo();
      })
    },
    changeSizeSelect(selection){
      this.size_selection = selection;
    },
    changeTraceSelect(selection){
      this.trace_selection = selection;
    },
    getTraceInfo(){
       this.size_selection =[];
       this.trace_selection = [];
       this.$Loading.start();
       if(!this.currentDate) {
         this.currentDate = new Date()
       }

       axios.post(process.env.VUE_APP_API+'/getServerInfo?index=plume_log_run_' + moment(this.currentDate).format('YYYYMMDD') + "*").then(data=>{
         this.$Loading.finish();
         this.sizeInfo = _.get(data,'data',[]);
       })

        axios.post(process.env.VUE_APP_API+'/getServerInfo?index=plume_log_trace_' + moment(this.currentDate).format('YYYYMMDD') + "*").then(data=>{
         this.$Loading.finish();
         this.traceInfo = _.get(data,'data',[]);
       })
    }
  },
  mounted(){
    this.getTraceInfo();
    this.getQueueSize();
    this.timer = setInterval(() => {
      this.getQueueSize()
    }, 1000)
  },
  beforeDestroy () {
    clearInterval(this.timer)
  }
};
</script>
<style lang="less" src="../assets/less/filters.less" scoped></style>
<style lang="less">
  .pnl_sizes
  {
    display: flex;
    padding-left:20px;
    position: relative;
    width:800px;
    // flex-direction: column; 
    // flex-wrap: nowrap;
    // justify-content: center;

    .btn_delete
    {
      position: absolute;
      top:-5px;
      right:0;
    }
    .pnl_size
    {
      text-align:left;
      flex: 1;
      width:800px;
      
      table{
        width:100%;
        
        th{
          border-top: none;
          text-align: center;
        }
        td{
          text-align: center;
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