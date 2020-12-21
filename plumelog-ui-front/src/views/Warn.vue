<template>
  <div class="pnl_wraper">
     <log-header></log-header>

       <Tabs active-key="报警设置" >
        <Tab-pane label="报警设置" key="报警设置">
          <Button icon="ios-add" @click="add" class="btn_add">添加</Button>
          <Button icon="ios-trash" :disabled="isDisabled" class="btn_delete" @click="removeSelect" type="error">删除所选</Button>
          <div style="clear:both"></div>
          <Table height="600" :content="self" :columns="columns" @on-selection-change="changeSelect"  :data="warnData">
            <template slot-scope="{ row, index }" slot="action">
              <Button size="small" @click="edit(index)">修改</Button>&nbsp;&nbsp;
              <Button type="error" size="small" @click="del(index)">删除</Button>
            </template>
          </Table>
          <div class="modal" style="display:block" v-if="showDialog" role="dialog">
            <div class="modal-dialog">
              <div class="modal-content">
                <div class="modal-header">
                  <h5 class="modal-title">{{isEdit?'修改':'添加'}}报警设置</h5>
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
                      <FormItem label="正则匹配">
                          <Input v-model="dataInfo.regex" placeholder="输入正则表达式" />
                      </FormItem>
                    <FormItem label="接收者" required>
                        <Input type="textarea" :rows="4" v-model="dataInfo.receiver" placeholder="输入接收者（逗号分隔）; 如果包含all表示@所有人"  />
                    </FormItem>
                      <FormItem label="平台" required>
                          <Select v-model="dataInfo.hookServe" placeholder="请选择报警平台" >
                              <Option v-for="item in hookServeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
                          </Select>
                      </FormItem>
                    <FormItem label="钩子" required>
                        <Input v-model="dataInfo.webhookUrl" placeholder="输入钉钉钩子地址"  />
                    </FormItem>
                    <FormItem label="错误数量" required>
                        <Input v-model="dataInfo.errorCount" type="number" placeholder="输入错误数量"  />
                    </FormItem>
                    <FormItem label="时间间隔" required>
                        <Input v-model="dataInfo.time" type="number" placeholder="输入时间间隔(s)"  />
                    </FormItem>
                    <FormItem label="状态">
                         <i-switch v-model="dataInfo.status" size="large">
                            <span slot="open">开启</span>
                            <span slot="close">关闭</span>
                        </i-switch>
                    </FormItem>
                  </Form>
                </div>
                <div class="modal-footer">
                  <button type="button" class="btn btn-secondary" data-dismiss="modal" @click="showDialog=false">关闭</button>
                  <button type="button" class="btn btn-primary" @click="save">{{isEdit?'保存':'添加'}}</button>
                </div>
              </div>
            </div>
          </div>
        </Tab-pane>
         <Tab-pane label="报警记录" key="报警记录">
           <div v-if="logs.length>0">
              <Button icon="ios-trash" class="btn_clear"  @click="clearWarn">清空记录</Button>
              <ul class="logList">
                <li v-for="(log,index) in logs" :key="index">
                  <div class="time">{{formatTime(log.dataTime)}}</div>
                  <div class="cnt"><span class="key">应用名称: </span>{{log.appName}}</div>
                  <div class="cnt"><span class="key">类名: </span>{{log.className}}</div>
                  <div class="cnt"><span class="key">时间区间: </span>{{log.time}}秒</div>
                  <div class="cnt"><span class="key">实际错误: </span>{{log.errorCount}}条</div>
                  <div class="cnt"><span class="key">错误信息: </span>{{log.errorContent}}</div>
                  <div class="btn_showDetail">
                    <a href="javascript:void(0)" @click="doSearch(log)">查看详情>></a>
                  </div>
                </li>
              </ul>
              <Button @click="getMore" v-if="showMore" class="btn_more">加载更多</Button>
           </div>
            <div v-else style="text-align:center;padding-top:50px">暂无数据</div>
        </Tab-pane>
       </Tabs>
      <confirm-delete v-model="showConfirm" @on-confirm="confirmPassword"></confirm-delete>
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
import confirmDelete from '@/components/confirmDelete.vue'
import "@/assets/less/base.less";

export default {
  name: "Warn",
  data(){
   return {
    selection:[],
    showConfirm:false,
    hookServeList: [{value: 1, label:'钉钉'},{value: 2, label:'企业微信'}],
    hookServeMap: {"1":'钉钉', "2":'企业微信'},
    dataInfo: {
        appName: '',
        className: '',
        receiver:'',
        webhookUrl:'',
        time: 60,
        hookServe: 1,
        regex:'',
        status:false,
    },
    pageSize:50,
    from:0,
    logs:[],
    warnData:[],
    showDialog:false,
    columns:[
      {
        type: 'selection',
        width: 60,
        align: 'center'
      },
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
            title: '平台',
            key:'hookServe',
            width: 100,
            render:  (h, r) => {
                return h('span', this.hookServeMap[r.row.hookServe])
            }
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
    isEdit:false
   }
  },
  computed:{
    isDisabled(){
      return this.selection.length==0;
    }
  },
  components: {
    logHeader,
    confirmDelete
  },
  methods:{
    removeSelect(){
       if(this.selection.length>0 && confirm('确认要删除所选的监控么')){
          var ps = [];
          for(var info of this.selection){
            ps.push(this.delIndex(info))
          }
          Promise.all(ps).then(()=>{
             this.$Message.success('删除成功');
              this.getData();
          })
       }
    },
    changeSelect(selection){
      this.selection = selection;
    },
    add(){
      this.initDataInfo();
      this.isEdit = false;
      this.showDialog = true;
    },
    delIndex(_info){
      return new Promise((res,rej)=>{
        axios.post(process.env.VUE_APP_API+'/deleteWarningRule?id='+_info.id).then(data=>{
          if(data.data.success){
           res();
          }
          else
          {
            rej();
          }
        })
      })
    },
    del(index){
      let _info = this.warnData[index];
      if(confirm('确认要删除ID为 '+_info.id+' 的监控么')){
        this.delIndex(_info).then(()=>{
            this.$Message.success('删除成功');
            this.getData();
        })
      }
    },
    edit(index){
      this.dataInfo = this.warnData[index];
      this.isEdit = true;
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
    initDataInfo() {
      this.dataInfo = {
        appName: '',
        className: '',
        receiver:'',
        webhookUrl:'',
        errorCount: 10,
        errorContent: '',
        status:true,
        time: 60
      };
    },
    setData(info){
       let _info = _.clone(info)
       let id = _info.id || Date.now();
      
       _info.status = _info.status ? 1 : 0;
       axios.post(process.env.VUE_APP_API+'/saveWarningRuleList?id='+id,_info).then(data=>{
         if(data.data.success){
           this.$Message.success('保存成功');
           this.showDialog = false;
           this.getData();
         }
       })
    },
    getData(){
      this.$Loading.start();
      axios.post(process.env.VUE_APP_API+'/getWarningRuleList').then(data=>{
         this.$Loading.finish();
         this.warnData = _.get(data,'data',[]).map(item=>{
           return {
             ...item,
             status:item.status ==1
           }
         });
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
           { "dataTime" : "desc" },
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
    },
    doSearch(info){
      this.$emit('init',{})
      this.$router.push({
        name:"Home",
        query:{
          className:info.className,
          appName:info.appName,
          time:info.dataTime-1000*60+','+info.dataTime,
          logLevel:'ERROR'
        }
      })
    },
    clearWarn(){
      this.showConfirm = true;
    },
    confirmPassword(pwd){
      axios.post(process.env.VUE_APP_API+'/deleteIndex?index=plumelog_monitor_message_key&adminPassWord='+pwd).then(result=>{
        console.log(result)
        if(result.data.acknowledged){
           alert('删除成功');
           this.logs=[];
        }
        else
        {
          alert(results[0].data.message);
        }
      })
    }
  },
  mounted(){
    this.getData();
    this.getLog();
  }
}
</script>
<style lang="less">

.ivu-tabs-tabpane
{
  text-align: left;
}

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
    .btn_showDetail {
      margin-top:10px;
      padding-left:20px;
      font-size:12px;

    }
    .cnt{
      .key{
        display: inline-block;
        width:80px;
        padding-right:10px;
        text-align: right;
        font-size:12px;
        font-weight: 700;
      }
    }
  }
}
  .modal
  {
    z-index: 1000;
  }
  .modal-backdrop
  {
    z-index:900;
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
  .btn_delete
  {
    float: right;
    margin: 10px 10px 10px 0;
  }
  .btn_clear
  {
    margin:0 0 20px 10px;
  }
</style>