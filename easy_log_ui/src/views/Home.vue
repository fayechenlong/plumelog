<template>
  <div class="pnl_wraper">
    <div class="pnl_filters">
      <div class="alert alert-danger" v-if="danger_str" role="alert">
        {{danger_str}}
      </div>
      <nav aria-label="breadcrumb">
        <ol class="breadcrumb">
          <li class="breadcrumb-item active" aria-current="page">
            <h3><Icon type="ios-bug" />&nbsp;日志查询系统</h3>
          </li>
        </ol>
      </nav>
      <iframe id="rfFrame" name="rfFrame" src="about:blank" style="display:none;"></iframe> 
      <form action="" target="rfFrame" ref="form" autocomplete="on">
        <table class='tbl_filters'>
          <tbody>
            <tr>
              <td class="key">应用名称</td>
              <td>
                <Input class="txt" name="appName" v-model="filter.appName" placeholder="搜索多个请用逗号或空格隔开" :clearable="true" />
              </td>
            </tr>
            <tr>
              <td class="key">日志等级</td>
              <td>
                <Select v-model="filter.logLevel" placeholder="请选择日志等级">
                    <Option value="" key="ALL">所有</Option>
                    <Option value="INFO" key="INFO">INFO</Option>
                    <Option value="ERROR" key="ERROR">ERROR</Option>
                    <Option value="WARN" key="WARN">WARN</Option>
                    <Option value="DEBUG" key="DEBUG">DEBUG</Option>
                </Select>
              </td>
            </tr>
            <tr>
              <td class="key">服务器名称</td>
              <td>
                <Input class="txt" name="serverName" v-model="filter.serverName" placeholder="搜索多个请用逗号或空格隔开" :clearable="true"/>
              </td>
            </tr>
          </tbody>
        </table>

        <table class='tbl_filters'>
          <tr>
              <td class="key">模块名</td>
              <td>
                <Input class="txt"  name="className" v-model="filter.className" placeholder="搜索多个请用逗号或空格隔开" :clearable="true"/>
              </td>
            </tr>
            <tr>
              <td class="key">追踪码</td>
              <td>
                <Input class="txt" name="traceId" v-model="filter.traceId" placeholder="搜索多个请用逗号或空格隔开" :clearable="true"/>
              </td>
            </tr>
            <tr>
              <td class="key">日期和时间</td>
              <td>
                  <DatePicker ref='datePicker' v-model="dateTimeRange" :editable="true" @on-change="dateChange" type="datetimerange" :options="dateOption" format="yyyy-MM-dd HH:mm" placeholder="选择日期与时间" style="width: 378px"></DatePicker>
              </td>
            </tr>
        </table>
      </form>
    <div style="clear:both"></div>
      <table class="tbl_filters">
        <tr>
            <td class="key">内容</td>
            <td>
              <input class="txt ivu-input" @keyup.enter="doSearch()" style="width:907px" placeholder="输入搜索内容" v-model="searchKey" />
            </td>
          </tr>
          <tr>
            <td></td>
            <td style='padding-top:20px'>
              <Button type="primary" icon="ios-search" @click="doSearch">查询</Button>
              <Button style="margin-left:10px" @click="clear">重置</Button>
            </td>
          </tr>
      </table>
       <div style="clear:both"></div>
    </div>

    <div style="float:right;margin-right:20px;margin-bottom:5px">共 <b>{{list.total}}</b> 条数据</div>
   
    <table v-if="list.hits.length>0" cellspacing="0" cellpadding="0"  class="table table-striped table_detail">
      <thead>
        <tr>
          <th v-for="item in contentItems" :key="item.name" scope="col">{{item.name}}</th>
          <th scope="col">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list.hits" :class="item._source.logLevel" :key="item._id">
          <td class="icon">{{item._source.appName}}<Icon type="ios-search" @click="doSearch('appName',item)" /></td>
          <td class="icon">{{item._source.logLevel}}<Icon type="ios-search" @click="doSearch('logLevel',item)"/></td>
          <td class="icon">{{item._source.serverName}}<Icon type="ios-search" @click="doSearch('serverName',item)"/></td>
          <td class="icon">{{item._source.traceId}}<Icon type="ios-search" v-if="item._source.traceId" @click="doSearch('traceId',item)" /></td>
          <td class="icon" style="width:150px">{{item._source.className | substr}}<Icon type="ios-search" @click="doSearch('className',item)" /></td>
          <td>{{item._source.dtTime | filterTime}}</td>
          <td class='td_cnt' v-html="showContent(item)"></td>
          <td><button class="btn btn-primary" @click="showDetail(item)">详情</button></td>
        </tr>
      </tbody>
    </table>
    <nav v-if="list.total && parseInt(list.total/size) > 0" class="page_nav" aria-label="Page navigation example">
      <ul class="pagination justify-content-center">
        <li class="page-item" :class="{'disabled': !isShowLastPage }">
          <a class="page-link" href="javascript:void(0)" @click="prevePage" tabindex="-1">上一页</a>
        </li>
        <li class="page-item" :class="{'disabled': !haveNextPage }">
          <a class="page-link" href="javascript:void(0)" @click="nextPage">下一页</a>
        </li>
        <li class="page-item"><div class="page-count">第{{parseInt(from/size)+1}}页 / 共{{  parseInt(list.total/size)+1}}页</div></li>
      </ul>
    </nav>

    <!-- Modal -->
    
    <div class="modal fade show" style="display:block" v-if="content.title" tabindex="-1" role="dialog" aria-labelledby="exampleModalScrollableTitle" aria-hidden="true">
      <div class="modal-dialog modal-dialog-scrollable" style="max-width:1200px" role="document">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">{{content.title}}</h5>
            <button type="button" class="close" @click="content={}" data-dismiss="modal" aria-label="Close">
              <span aria-hidden="true">&times;</span>
            </button>
          </div>
          <div class="modal-body">
              <table>
                <tr v-for="item in contentItems" :key="item.value">
                  <td class="key">{{item.name}}</td>
                  <td v-if="item.value == 'dtTime'">{{content._source[item.value] | filterTime}}</td>
                  <td v-else-if="item.value == 'content'">
                    <div class="code_wrap">
                      <pre v-html="hightLightCode(content.content)"></pre>
                    </div>
                  </td>
                  <td v-else>{{content._source[item.value]}}</td>
                </tr>
              </table>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="content={}"  data-dismiss="modal">关闭</button>
          </div>
        </div>
      </div>
    </div>
    <div class="modal-backdrop fade show" v-if="content.title"></div>

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


export default {
  name: "Home",
  data(){
   return {
     api: process.env.api,
     dateOption:{
       shortcuts: [
         {
              text: '15分钟',
              value () {
                  const end = new Date();
                  const start = new Date();
                  start.setTime(start.getTime() - 60 * 1000 * 15);
                  return [start, end];
              }
          },
          {
              text: '30分钟',
              value () {
                  const end = new Date();
                  const start = new Date();
                  start.setTime(start.getTime() - 60 * 1000 * 30);
                  return [start, end];
              }
          },
          {
              text: '1小时',
              value () {
                  const end = new Date();
                  const start = new Date();
                  start.setTime(start.getTime() - 3600 * 1000);
                  return [start, end];
              }
          },
          {
              text: '24小时',
              value () {
                  const end = new Date();
                  const start = new Date();
                  start.setTime(start.getTime() - 3600 * 1000 * 24);
                  return [start, end];
              }
          },
          {
              text: '1周',
              value () {
                  const end = new Date();
                  const start = new Date();
                  start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
                  return [start, end];
              }
          },
          {
              text: '当天',
              value () {
                  const end = new Date();
                  var start = new Date();
                  start.setTime(start.setHours(0,0));
                  return [start, end];
              }
          }
      ],
       disabledDate(date){
         return date && date.valueOf() > Date.now();
       }
     },
     contentItems:[
       {
        'name': '应用名称',
        'value': 'appName'
       },
       {
        'name': '日志等级',
        'value': 'logLevel'
       },
       {
        'name': '服务器名称',
        'value': 'serverName'
       },
       {
        'name': '追踪码',
        'value': 'traceId'
       },
       {
        'name': '模块名',
        'value': 'className'
       },
       {
        'name': '时间',
        'value': 'dtTime'
       },
       {
        'name': '内容',
        'value': 'content'
       },
       
     ],
     dateTimeRange:[],
     content:{},
     searchKey:'',
     danger_str:'',
     filter:{
       "logLevel":'',
       "appName":"",
       "traceId":""
     },
     list:{
       hits:[]
     },
     size:30,
     from:0
   }
  },
  components: {
    // HelloWorld
  },
  filters:{
    substr(str){
      if(str.length>50){
        return str.substring(0,50)+'...';
      }
      return str;
    },
    filterTime(date){
      return moment(date).format('YYYY/MM/DD HH:mm:ss')
    }
  },
  computed:{
    isShowLastPage(){
      return this.from > 0 
    },
    haveNextPage(){
      if(this.list.total>=(this.from+this.size))
        return true
      else
        return false
    }
  },
  methods:{
    getShouldFilter(){
      let filters = [];
      let date=[];
      for(let itemKey in this.filter)
      {
        if(this.filter[itemKey]){
           filters.push({
            "match":{
              [itemKey]:{
                "query":this.filter[itemKey].replace(/,/g,' '),
                // "type":"phrase"
              }
            }
          })
        }
      }

      if(this.searchKey){
        filters.push({
            "query_string":{
              "query": this.searchKey,
              "default_field": "content"
            }
          })
      }

      //判断日期
      if(this.dateTimeRange.length>0 && this.dateTimeRange[0]!=''){
        let startDate=new Date(this.dateTimeRange[0]);
        let endDate=new Date(this.dateTimeRange[1]);
        filters.push({
          "range":{
            "dtTime":{
              "gte": Date.parse(startDate),
              "lt": Date.parse(endDate),
            }
          }
        })
      }

      return filters
    },
    clear(){
     this.filter = {
       "logLevel":'',
       "appName":"",
       "traceId":""
     }
     this.searchKey = "";
     this.dateTimeRange = [];
     this.$refs.datePicker.internalValue=[];
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
    hightLightCode(code){
      if(code.indexOf('java.')>-1){
        return Prism.highlight(code, Prism.languages.stackjava, 'stackjava');
      }
      else
      {
        return code;
      }
    },
    init(){
      let from = this.$route.query.from || 0
      from = parseInt(from);
      this.filter = JSON.parse(this.$route.query.filter || '{}')
      this.from = from;
    },
    checkExistsIndex(dateList) {
      var promises=[];
    
      for(var date of dateList){
           promises.push(new Promise((res,reject)=>{
               axios.head('/'+date)
                    .then(r=>{
                        res(true)
                    })
                    .catch(error=>{
                      res(false)
                    })
           }))
      }

      return Promise.all(promises).then(data=>{
        var existDateList=[];
        for(var i=0;i<dateList.length;i++)
        {
          if(data[i]){
            existDateList.push(dateList[i])
          }
        }
        return existDateList
      })
     
    },
    showContent(item){
      var str = (_.get(item,"highlight.content[0]","") || _.get(item,"_source.content",""))
      if(str.length>30)
      {
        return str.substring(0,30)+'...'
      }
      return str;
    },
    showDetail(item){
      this.content = {
        "title":'日志详情',
        content: _.get(item,"highlight.content[0]","") || _.get(item,"_source.content",""),
        ...item
      }
    },
    doSearch(keyName,item){

      this.$refs.form.submit();

      if(keyName && item){
        this.filter[keyName] = item._source[keyName]
      }

      //列出范围内的日期
      let dateList=[];
      let startDate = _.clone(this.dateTimeRange[0]);

      let shouldFilter = this.getShouldFilter();
      
      if(startDate){
         while(startDate<=this.dateTimeRange[1]){
          dateList.push($config.prefix+moment(startDate).format('YYYYMMDD'))
          startDate = new Date(startDate.setDate(startDate.getDate()+1));
        }
      }
     
      if(dateList.length==0){
        dateList.push($config.prefix+moment().format('YYYYMMDD'));
      }

      //this.checkExistsIndex(dateList).then(existDateList=>{
          
          let url= '/getInfo?index='+dateList.join(',')+'&size='+this.size+"&from="+this.from

          let esFilter = {
            "query":{
              "bool":{
                "must":[
                  ...shouldFilter
                ]
              }
            },
            "highlight": {
                "fields" : {
                    "content" : {}
                }
            },
            "sort":[
              {
                "dtTime":"desc"
              }
            ]
          };

          axios.post(url,esFilter).then(data=>{
            this.list = _.get(data,'data.hits')
          })

          this.$router.push({
            name:'Home',
            query:{
              from:this.from,
              filter: JSON.stringify(this.filter)
            }
          }).catch(err=>{
            
          })
      //});
    },
    prevePage(){
      let from = this.from - this.size
      if(from < 0)
      {
        from = 0;
      }
      this.from = from;   
      this.doSearch();   
    },
    nextPage(){
      let from = this.from + this.size
      this.from = from;   
      this.doSearch();
    }
  },
  watch:{
    searchKey(){
      this.from = 0;
    },
    "filter":{
      handler(){
        this.from = 0;
      },
      deep:true
    }
  },
  mounted(){
    this.init();
    this.doSearch();
  }
};
</script>
<style lang="less">

  pre {
    // white-space: pre-wrap;       /* css-3 */
    // white-space: -moz-pre-wrap;  /* Mozilla, since 1999 */
    // white-space: -pre-wrap;      /* Opera 4-6 */
    // white-space: -o-pre-wrap;    /* Opera 7 */
    word-wrap: break-word;       /* Internet Explorer 5.5+ */
  }

  .pnl_wraper
  {
    padding-bottom:80px;
    .pnl_filters
    {
      padding-bottom:20px; 
      nav{
        margin-bottom:20px;
      }
    }
    .page_nav
    {
      position:fixed;
      bottom:0;
      width:100%;
      padding:15px 0;
      background:#fff;
      border-top:1px solid #ccc;
    }
  }

  .table_detail {
    position:relative;
    border-collapse: collapse;
    thead {
      position:sticky;
        top: 0;
      th{
        position:sticky;
        top: -1px;
        background:#fff;
        box-shadow: 0 2px 2px -1px rgba(0, 0, 0, 0.1);
        z-index:99;
      }
    }

    td{
      position: relative;
      &.icon{
         &:hover{
          i{
            display: inline;
          }
        }
        i{
           cursor: pointer;
           position: absolute;
           top: 15px;
           right:5px;
           font-size: 16px;
           display:none;
        }
       
      }
    }

    .btn{
      font-size:14px !important;
    }

    tr.WARN{
      td{
        background: #fff1d7;
      }
    }

    tr.ERROR{
      td{
        background: #f7b8a8;
      }
    }

    tr:hover{
      td{
        background:#e3ecf3;
      }
    }
  }

  .modal-body
  {
    table{
      tr{
        td{
          vertical-align:top;
          padding:10px 0;
          border-bottom:1px dotted #ececec;
          .code_wrap
          {
            width:950px;
          }
        }
        .key{
          width:150px;
          font-weight:700;
          text-align:right;
          padding:10px 50px 10px 10px;
        }
      }
      
    }
  }

  .td_cnt
  {
    em {
      background:yellow;
    }
  }

  .modal-body
  {
    text-align:left;
    em {
      background:yellow;
    }
  }

  
</style>
<style lang="less" scoped>

  .tbl_filters {
    float:left;
    tr{
      td{
        height:30px;
        line-height:30px;
        text-align:left;
        padding-bottom:10px;
        &.key{
          padding-left:20px;
          text-align:right;
          font-weight:700;
          width:150px;
          padding-right:30px;
        }
        .txt{
          width:378px;
        }
      }
    }
  }

  .breadcrumb
  {
    position: relative;
    padding:20px 10px 20px 50px;
  }
  .pnl_search
  {
    position: absolute;
    top:10px;
    left:300px;
    width:600px;
  }

   .link_trans
    {
      color: #1313bd;
      text-decoration:underline;
      cursor: pointer;
    }

    .page-count
    {
      padding:.5rem 1rem;
      
    }
</style>