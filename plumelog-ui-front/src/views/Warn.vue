<template>
  <div class="pnl_wraper">
     <log-header></log-header>
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
    self:this
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

    }
  },
  mounted(){
    this.getData();
  }
}
</script>
<style lang="less">
  .modal
  {
    z-index: 1000;
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