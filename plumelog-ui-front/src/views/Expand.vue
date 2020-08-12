<template>
  <div class="pnl_wraper">
      <div class="pnl_filters">
        <log-header></log-header>
        <div style="clear:both"></div>
        <div class="pnl_selectAppName">
            选择应用名称：
            <Select style="width:200px" filterable v-model="currentAppName" @on-change="getExtendList"  placeholder="选择应用名称">
                <Option v-for="appName in appNames" :value="appName" :key="appName">{{ appName }}</Option>
            </Select>
        </div>
        <div class="pnl_data" v-if="currentAppName">
            <div class="pnl_controls">
                <Button style="float:right" class="btn_addField" @click="addField" icon="md-add">添加字段</Button>
                <div style="clear:both"></div>
                <!-- <Button class="btn_delField" type="error" @click="delAppName" icon="ios-trash">删除所有扩展字段</Button> -->
            </div>
            <Table height="600" :content="self" :columns="columns" :data="extendList">
                <template slot-scope="{ row }" slot="action">
                    <Button type="error" size="small" @click="removeExtend(row.field)">删除</Button>
                </template>
            </Table>
        </div>
       </div>

        <Modal
            v-model="showDialog"
            title="添加扩展字段"
            ok-text="添加"
            @on-ok="confirmModal"
            @on-cancel="showDialog=false">
             <Form :model="formItem" :label-width="100">
                <FormItem label="扩展字段名">
                    <Input v-model="formItem.field" placeholder="输入扩展字段名" />
                </FormItem>
                    <FormItem label="字段显示名">
                    <Input v-model="formItem.fieldName" placeholder="输入扩展字段显示名" />
                </FormItem>
            </Form>
        </Modal>
  </div>
</template>

<script>
// @ is an alias to /src
// import HelloWorld from "@/components/HelloWorld.vue";
import axios from '@/services/http'
import _ from 'lodash'
import moment from 'moment'
import 'view-design/dist/styles/iview.css';
import logHeader from '@/components/logHeader.vue'
import "@/assets/less/base.less";

export default {
  name: "Expand",
  data(){
   return {
       showDialog:false,
       self:this,
       extendList:[],
       appNames:[],
       currentAppName:'',
       formItem:{
           field:'',
           fieldName:''
       },
       columns:[
           {
                title: '扩展字段',
                key:'field',
           },
           {
               title: '字段名',
               key:'fieldName',
           },{
              title:'操作',
              width:100,
              slot: 'action',
           }
       ]
   }
  },
  computed:{
    
  },
  components: {
    logHeader
  },
  methods:{
    //   delAppName(){
    //       if(this.currentAppName){
    //           axios.post(process.env.VUE_APP_API+'/delAppName?id='+this.currentAppName).then(data=>{
    //             if(_.get(data,'data',false)){
    //                 this.$Message.success(`删除成功`);
    //                 this.getExtendList(this.currentAppName);
    //             }
    //             else
    //             {
    //                 this.$Message.error(`删除失败`);
    //             }
    //         })
    //       }
    //   },
      confirmModal(){
          if(this.formItem.field && this.formItem.fieldName){
            axios.post(process.env.VUE_APP_API+'/addExtendfield?appName='+this.currentAppName+'&field='+this.formItem.field+'&fieldName='+this.formItem.fieldName).then(data=>{
                if(_.get(data,'data',false)){
                    this.$Message.success(`添加成功`);
                    this.getExtendList(this.currentAppName);
                    this.formItem = {
                        field:'',
                        fieldName:''
                    }
                }
                else
                {
                    this.$Message.error(`添加失败`);
                }
            })
          }
          else
          {
              this.$Message.warning('请把扩展字段数据填写完整');
              setTimeout(()=>{
                  this.showDialog = true
              },10)           
          }
          
      },
      addField(){
          this.showDialog = true;
      },
      removeExtend(field){
          if(confirm("确认要删除应用 "+this.currentAppName+" 下的扩展字段 "+field+" 吗?")){
              axios.post(process.env.VUE_APP_API+'/delExtendfield?appName='+this.currentAppName+"&field="+field).then(data=>{
                if(_.get(data,'data',false)){
                    this.$Message.success(`删除成功`);
                    this.getExtendList(this.currentAppName);
                }
                else
                {
                    this.$Message.error(`删除失败`);
                }
              })
          }
      },
      getExtendList(appName){
          if(appName){
            this.$Loading.start();
            axios.post(process.env.VUE_APP_API+'/getExtendfieldList?appName='+appName).then(data=>{
                let _data = _.get(data,'data',{});
                let list = [];
                for(var item in _data){
                    list.push({
                        field:item,
                        fieldName:_data[item]
                    });
                }
                this.extendList = list;
                this.$Loading.finish();
            })
          }
      },
      getAppNames(){

        if(sessionStorage['cache_appNames']){
            this.appNames = JSON.parse(sessionStorage['cache_appNames'])
        }
        else
        {
            let q = "plume_log_run_" + moment().format("YYYYMMDD")
            axios.post(process.env.VUE_APP_API+'/query?index='+q+'&from=0&size=0',{
                "size": 0,
                "aggregations": {
                    "dataCount": {
                        "terms": {
                            "size": 1000,
                            "field": "appName"
                        }
                    }
                }
            }).then(data=>{
                let buckets = _.get(data,'data.aggregations.dataCount.buckets',[]).map(item=>{
                    return item.key
                });
                sessionStorage['cache_appNames'] = JSON.stringify(buckets);
                this.appNames = buckets;
            })
        }
        
    }
  },
  mounted(){
    //this.getExtendList("plumelog_demo");
    this.getAppNames();
    
  }
}
</script>
<style lang="less">
  .pnl_selectAppName{
      text-align: left;
      padding-left:30px;
  }
  .pnl_data
  {
      width:800px;
      padding:30px;
      text-align: left;
      .pnl_controls
      {
          margin-bottom:10px;
          .btn_delField
          {
           margin-left: 10px;   
          }
      }
  }
  
</style>