<template>
  <div class="pnl_wraper">
     <log-header></log-header>

       <Tabs active-key="管理" >
        <Tab-pane label="管理" key="管理">
          <Button icon="ios-add" @click="add" class="btn_add">添加</Button>
          <div style="clear:both"></div>
          <Table height="600" :content="self" :columns="columns" :data="warnData">
            <template slot-scope="{ row, index }" slot="action">
              <Button size="small" @click="edit(index)">修改</Button>&nbsp;&nbsp;
              <Button type="error" size="small" @click="del(index)">删除</Button>
            </template>
          </Table>
          <div class="modal" style="display:block" v-if="showDialog" role="dialog">
            <div class="modal-dialog">
              <div class="modal-content">
                <div class="modal-header">
                  <h5 class="modal-title">添加报警设置</h5>
                  <button type="button" class="close" data-dismiss="modal" @click="showDialog=false" aria-label="关闭">
                    <span aria-hidden="true">&times;</span>
                  </button>
                </div>
                <div class="modal-body">
                  <Form :model="dataInfo" ref="dataForm" :label-width="80">
                    <FormItem label="应用名称" required>
                        <Input v-model="dataInfo.appName" placeholder="输入应用名称"  />
                    </FormItem>
                    <FormItem label="模块名称">
                        <Input v-model="dataInfo.className" placeholder="输入模块名称" />
                    </FormItem>
                    <FormItem label="接收者" required>
                        <Input v-model="dataInfo.receiver" placeholder="输入接收者"  />
                    </FormItem>
                    <FormItem label="钉钉钩子" required>
                        <Input v-model="dataInfo.webhookUrl" placeholder="输入钉钉钩子地址"  />
                    </FormItem>
                    <FormItem label="错误数量" required>
                        <Input v-model="dataInfo.errorCount" type="number" placeholder="输入错误数量"  />
                    </FormItem>
                    <FormItem label="时间间隔" required>
                        <Input v-model="dataInfo.time" type="number" placeholder="输入时间间隔(s)"  />
                    </FormItem>
                  </Form>
                </div>
                <div class="modal-footer">
                  <button type="button" class="btn btn-secondary" data-dismiss="modal" @click="showDialog=false">关闭</button>
                  <button type="button" class="btn btn-primary" @click="save">添加</button>
                </div>
              </div>
            </div>
          </div>
        </Tab-pane>
         <Tab-pane label="日志" key="日志">
           <ul class="logList">
             <li v-for="(log,index) in logs" :key="index">
               <div class="time">{{formatTime(log.time)}}</div>
               <div class="cnt"><pre>{{log.monitor_message}}</pre></div>
             </li>
           </ul>
            <Button @click="getMore" v-if="showMore" class="btn_more">加载更多</Button>
        </Tab-pane>
       </Tabs>

     
  </div>
</template>

<script>
// @ is an alias to /src
// import HelloWorld from "@/components/HelloWorld.vue";
import axios from '@/services/http'
import _ from 'lodash'
import moment from 'moment'
import dateOption from './dateOption';
import 'view-design/dist/styles/iview.css';
import logHeader from '@/components/logHeader.vue'
import "@/assets/less/base.less";

export default {
  name: "Warn",
  data(){
   return {
    dataInfo: {
        appName: '',
        className: '',
        receiver:'',
        webhookUrl:'',
        time: 60
    },
    pageSize:50,
    from:0,
    logs:[],
    warnData:[],
    showDialog:false,
    columns:[
      {
        title: 'ID',
        width:150,
        key:'id'
      },
      {
        title: '应用名称',
        key:'appName'
      },
      {
        title: '模块名称',
        key:'className'
      },
      {
        title: '接收者',
        width:150,
        key:'receiver'
      },
      {
        title: '错误数量',
        width:150,
        key:'errorCount'
      },
      {
        title: 'webHook',
        key:'webhookUrl'
      },
      {
        title: '时间',
        width:100,
        key:'time'
      },
      {
          title: '操作',
          slot: 'action',
          width: 150,
          align: 'center'
      }
    ],
    self:this,
    showMore:true,
   }
  },
  computed:{
   
  },
  components: {
    logHeader
  },
  methods:{
    add(){
      this.initDataInfo();
      this.showDialog = true;
    },
    del(index){
      let _info = this.warnData[index];
      if(confirm('确认要删除ID为 '+_info.id+' 的监控么')){
         axios.post(process.env.VUE_APP_API+'/deleteWarningRule?id='+_info.id).then(data=>{
          if(data.data.success){
            this.$Message.success('删除成功');
            this.getData();
          }
        })
      }
    },
    edit(index){
      this.dataInfo = this.warnData[index];
      this.showDialog = true;
    },
    save(){
      if(this.dataInfo.appName==''){
        this.$Message.error('请填写应用名称');
        return false;
      }
      else if(this.dataInfo.receiver==''){
        this.$Message.error('请填写接收者');
        return false;
      }
      else if(this.dataInfo.webhookUrl==''){
        this.$Message.error('请填写钉钉钩子地址');
        return false;
      }
      else if(this.dataInfo.errorCount==''){
        this.$Message.error('请填写错误数量');
        return false;
      }
      else if(this.dataInfo.time==''){
        this.$Message.error('请填写间隔时间');
        return false;
      }
      this.setData(this.dataInfo);

    },
    initDataInfo(){
      this.dataInfo = {
        appName: '',
        className: '',
        receiver:'',
        webhookUrl:'',
        errorCount: 10,
        time: 60
      }
    },
    setData(info){
       let id = info.id || Date.now();
       axios.post(process.env.VUE_APP_API+'/saveWarningRuleList?id='+id,info).then(data=>{
         if(data.data.success){
           this.$Message.success('保存成功');
           this.showDialog = false;
           this.getData();
         }
       })
    },
    getData(){
      this.$Loading.start();
      axios.get(process.env.VUE_APP_API+'/getWarningRuleList').then(data=>{
         this.$Loading.finish();
         this.warnData = _.get(data,'data',[]);
       })
    },
    formatTime(time){
      if(time){
        return moment(time).format('YYYY-MM-DD HH:mm:ss')
      }
      return ''
    },
    getLog(){
      //  
      axios.post(process.env.VUE_APP_API+'/query?index=plumelog_monitor_message_key'+'&from='+this.from+'&size='+this.pageSize,{
        "query": {
            "match_all": {}
        },
        "sort" : [
           { "time" : "desc" },
        ]
      }).then(data=>{
        let logs=_.get(data,"data.hits.hits",[]).map(item=>{
          return {
            ...item._source
          }
        })
        if(logs.length==this.pageSize){
          this.showMore = true;
        }
        else
        {
          this.showMore = false;
        }
        this.logs.push.apply(this.logs,logs);
      })
    },
    getMore(){
      this.from += this.pageSize;
      this.getLog();
    }
  },
  mounted(){
    this.getData();
    this.getLog();
  }
}
</script>
<style lang="less">
.logList
{
  li{
    text-align: left;
    padding-left:20px;
    margin-bottom:30px;
    .time{
      padding-bottom:8px;
      border-bottom:1px dotted #ccc;
      margin-bottom:10px;
    }
    .cnt{
      
    }
  }
}
  .modal
  {
    z-index: 1000;
  }
  .ivu-tabs-nav-scroll
  {
    margin-top:20px;
  }
  .ivu-form-item
  {
    margin-bottom:5px;
  }
  .btn_add
  {
    float: right;
    margin:10px 20px 10px 0;
  }
</style>