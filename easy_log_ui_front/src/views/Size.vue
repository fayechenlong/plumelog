<template>
  <div class="pnl_wraper">
    <div class="pnl_filters">
      <log-header></log-header>
      <div style="clear:both"></div>
    </div>
    
    <div class="pnl_sizes">
      <Tabs active-key="运行数据" @on-click="changeTab">
        <Tab-pane label="运行数据" name="run" key="运行数据">
           <div class="pnl_size" v-if="sizeInfo.length>0">
             <Table height="600" @on-selection-change="changeSizeSelect" :content="self" :columns="columns_size" :data="sizeInfo">
               <template slot-scope="{ row, index }" slot="action">
                <Button type="error" size="small" @click="remove(index)">删除</Button>
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
    <Button icon="ios-trash" :disabled="isDisabled" class="btn_delete" @click="removeSelect" type="error">删除所选</Button>
      
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
          let _index = params.row.index.replace('easy_log_trace_','').replace('easy_log_','');;
          if(_index.length>=8){
            _index = _index.substring(0,4)+'-'+_index.substring(4,6)+'-'+_index.substring(6,8)
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
      },
      // {
      //     title: '操作',
      //     slot: 'action',
      //     width: 150,
      //     align: 'center'
      // }
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
    logHeader
  },
  methods:{
    changeTab(name){
      this.currentTab = name;
    },
    removeSelect(){

      var selected = this.currentTab == 'run' ? this.size_selection : this.trace_selection;

      if(!confirm('确定要删除这 '+selected.length+' 条记录么？')){
        return false
      }
      
      let deletePromise=[];
      for(var item of selected)
      {
        deletePromise.push(axios.get('http://10.33.80.49:8989/deleteIndex?index='+item.index))
      }
      Promise.all(deletePromise).then(results=>{
        let successResults=[];

        for(var result of results){
          if(result.data.acknowledged){
            successResults.push(result);
          }
        }
        if(successResults.length == results.length){
          //全部删除成功，提示
          alert('删除成功')
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
       axios.post('/getServerInfo?index=easy_log_2*').then(data=>{
         this.$Loading.finish();
         this.sizeInfo = _.get(data,'data',[]);
       })

        axios.post('/getServerInfo?index=easy_log_trace*').then(data=>{
         this.$Loading.finish();
         this.traceInfo = _.get(data,'data',[]);
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