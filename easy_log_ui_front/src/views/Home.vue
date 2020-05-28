<template>
  <div class="pnl_wraper">
    <div class="pnl_filters">
      <div class="alert alert-danger" v-if="danger_str" role="alert">
        {{danger_str}}
      </div>
      <log-header></log-header>
      
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
            <td class="key">类名</td>
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
                <DatePicker ref='datePicker' v-model="dateTimeRange" @on-change="dateChange" type="datetimerange" :options="dateOption" format="yyyy-MM-dd HH:mm" placeholder="选择日期与时间" style="width: 280px"></DatePicker>
            </td>
          </tr>
      </table>

      <div id="myChart"></div>

    <div style="clear:both"></div>
      <table class="tbl_filters">
        <tr>
            <td class="key">内容</td>
            <td>
              <input class="txt ivu-input" @keyup.enter="doSearch()" style="width:711px" placeholder="输入搜索内容" v-model="searchKey" />
            </td>
          </tr>
          <tr>
            <td></td>
            <td style='padding-top:8px'>
              <Button type="primary" icon="ios-search" @click="doSearch">查询</Button>
              <Button style="margin-left:10px" @click="clear">重置</Button>
            </td>
          </tr>
      </table>
       <div style="clear:both"></div>
    </div>

    <div style="float:right;margin-right:20px;margin-bottom:5px">共 <b>{{totalCount}}</b> 条数据</div>
    <table v-if="list.hits.length>0" cellspacing="0" cellpadding="0"  class="table table-striped table_detail">
      <thead>
        <tr>
          <th scope="col">时间</th>
          <th scope="col">日志等级</th>
          <th scope="col">服务器名称</th>
          <th scope="col">应用名称</th>
          <th scope="col">追踪码</th>
          <th scope="col">类名</th>
          <th scope="col">内容</th>
          <th scope="col">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in list.hits" :class="item._source.logLevel" :key="item._id">
          <td>{{item._source.dtTime | filterTime}}</td>
          <td class="icon">{{item._source.logLevel}}<Icon type="ios-search" @click="doSearch('logLevel',item)"/></td>
          <td class="icon">{{item._source.serverName}}<Icon type="ios-search" @click="doSearch('serverName',item)"/></td>
          <td class="icon">{{item._source.appName}}<Icon type="ios-search" @click="doSearch('appName',item)" /></td>
          <td class="icon"> <a :href="'/#/trace?traceId='+item._source.traceId+'&timeRange='+JSON.stringify(dateTimeRange)" title="点击查看链路追踪">{{item._source.traceId}}</a><Icon type="ios-search" v-if="item._source.traceId" @click="doSearch('traceId',item)" /></td>
          <td class="icon" style="width:150px">{{item._source.className | substr}}<Icon type="ios-search" @click="doSearch('className',item)" /></td>
          <td class='td_cnt' v-html="showContent(item)"></td>
          <td><button class="btn btn-primary" @click="showDetail(item)">详情</button></td>
        </tr>
      </tbody>
    </table>
    <nav v-if="totalCount && parseInt(totalCount/size) > 0" class="page_nav" aria-label="Page navigation example">
      <ul class="pagination justify-content-center">
        <li class="page-item" :class="{'disabled': !isShowLastPage }">
          <a class="page-link" href="javascript:void(0)" @click="prevePage" tabindex="-1">上一页</a>
        </li>
        <li class="page-item" :class="{'disabled': !haveNextPage }">
          <a class="page-link" href="javascript:void(0)" @click="nextPage">下一页</a>
        </li>
        <li class="page-item"><div class="page-count">第{{parseInt(from/size)+1}}页 / 共{{  parseInt(totalCount/size)+1}}页</div></li>
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
                <template  v-for="item in contentItems">
                  <tr v-if="content._source[item.value]" :key="item.value">
                    <td class="key"><div>{{item.name}}</div></td>
                    <td v-if="item.value == 'dtTime'">{{content._source[item.value] | filterTime}}</td>
                    <td v-else-if="item.value == 'content'">
                      <div class="code_wrap">
                        <div v-html="hightLightCode(content.content)"></div>
                      </div>
                    </td>
                    <td v-else>{{content._source[item.value]}}</td>
                  </tr>
                </template>
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
import logHeader from '@/components/logHeader.vue'
import "@/assets/less/base.less";
import dateOption from './dateOption';


export default {
  name: "Home",
  data(){
   return {
     api: process.env.api,
     dateOption,
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
        'name': '类名',
        'value': 'className'
       },
       {
        'name': '方法名',
        'value': 'method'
       },
       {
        'name': '时间',
        'value': 'dtTime'
       },
       {
        'name': '内容',
        'value': 'content'
       }
     ],
     dateTimeRange:[moment(new Date()).format('YYYY-MM-DD 00:00:00'),moment(new Date()).format('YYYY-MM-DD 23:59:59')],
     content:{},
     searchKey:'',
     danger_str:'',
     filter:{
       "logLevel": '',
       "appName": "",
       "traceId": ""
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
    logHeader
  },
  filters:{
    substr(str){
      if(str.length>30){
        return str.substring(0,30)+'...';
      }
      return str;
    },
    filterTime(date){
      return moment(date).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  computed:{
    chartInterval(){
      if(this.dateTimeRange.length>0){
        let _range = (new Date(this.dateTimeRange[1])).getTime() - (new Date(this.dateTimeRange[0])).getTime();
         //大于7天按照每天数据统计
        if(_range>1000*60*60*24*7){
          return {
            format:'MM-DD',
            value: 1000*60*60*24
          }
        }
        //大于3天按照12小时进行统计
        else if (_range>1000*60*60*24*3){
          return {
            format:'MM-DD HH:mm',
            value: 1000*60*60*12
          }
        }
        //大于1天按照6小时进行统计
        else if (_range>1000*60*60*24){
          return {
            format:'MM-DD HH:mm',
            value: 1000*60*60*6
          }
        }
        else
        {
          return {
            format:'HH:mm',
            value: 1000*60*60
          }
        }
      }
      return {
        format:'MM-DD HH:mm',
        value: 1000*60*60
      }
    },
    totalCount(){
      if(!this.list.total){
        return 0
      }
      return this.list.total.value || this.list.total
    },
    isShowLastPage(){
      return this.from > 0 
    },
    haveNextPage(){
      if(this.totalCount>=(this.from+this.size))
        return true
      else
        return false
    }
  },
  methods:{
     drawLine(data){
        let myChart = this.$echarts.init(document.getElementById('myChart'))

        window.addEventListener('resize',() => { myChart.resize(); });

        // 绘制图表
        myChart.setOption({
            tooltip: {
              formatter(p,ticket){
                return '时间：'+p.name+'<br/>数量：'+p.value+'条'
              },
              extraCssText:'text-align:left'
            },
            xAxis: {
                data: _.map(data,(d)=>{
                  return  moment(d.key).format(this.chartInterval.format) 
                }),
                axisLabel:{
                  fontSize:12,
                  color:'#666',
                  // rotate:30
                }
            },
            yAxis: {
               axisLabel:{
                  fontSize:12,
                  color:'#666',
                }
            },
            series: [{
                name: '数量',
                type: 'line',
                data: _.map(data,(d)=>{
                  return d.doc_count
                }),
                itemStyle:{
                    borderColor: 'rgb(110, 173, 193)',
                    color: 'rgba(110, 173, 193,0.6)'
                }
            }]
        });
    },
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
     this.dateTimeRange = [moment(new Date()).format('YYYY-MM-DD 00:00:00'),moment(new Date()).format('YYYY-MM-DD 23:59:59')];
     this.$refs.datePicker.internalValue=[moment(new Date()).format('YYYY-MM-DD 00:00:00'),moment(new Date()).format('YYYY-MM-DD 23:59:59')];
     this.doSearch();
    },
    dateChange(){
        let startDate = new Date(this.dateTimeRange[0]);
        let endDate = new Date(this.dateTimeRange[1]);
        if(startDate.getHours() == 0 && startDate.getMinutes() ==0 && endDate.getHours() == 0 && endDate.getMinutes() == 0){
          this.dateTimeRange[1].setHours(23,59)
          this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
        }
    },
    hightLightCode(code){

      if(this.searchKey){
         let re = new RegExp("(" + this.searchKey.replace(/\*/g,'') + ")", "gmi");
          code = code.replace(re, '<em>$1</em>');
      }

      if(code.indexOf('java.')>-1){
        return '<pre>'+Prism.highlight(code, Prism.languages.stackjava, 'stackjava').replace(/&lt;/g,'<').replace(/&gt;/g,'>')+"</pre>";
      }
      else
      {
        return code;
      }
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
        content: _.get(item,"_source.content",""),
        ...item
      }
    },
    doSearch(keyName,item){

      if(keyName && item){
        this.filter[keyName] = item._source[keyName]
      }

      //列出范围内的日期
      let dateList=[];
      let startDate = _.clone(new Date(this.dateTimeRange[0]));

      let shouldFilter = this.getShouldFilter();
      
      if(startDate){
         while(startDate<=this.dateTimeRange[1]){
          dateList.push('easy_log_'+moment(startDate).format('YYYYMMDD'))
          startDate = new Date(startDate.setDate(startDate.getDate()+1));
        }
      }
     
      if(dateList.length==0){
        dateList.push('easy_log_'+moment().format('YYYYMMDD'));
      }
          
      let url= process.env.VUE_APP_API+'/query?index='+dateList.join(',');

      let query = {
         "query":{
          "bool":{
            "must":[
              ...shouldFilter
            ]
          }
        }
      };

      let esFilter = {
        ...query,
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

      this.$Loading.start();
      
      let searchUrl = url+'&size='+this.size+"&from="+this.from;
      axios.post(searchUrl,esFilter).then(data=>{
        this.$Loading.finish();
        this.list = _.get(data,'data.hits',{
          total:0,
          hits:[]
        })
      })


      let chartFilter = {
        "query":{
          "bool":{
            "must":[
              ...shouldFilter,
            ]
          }
        },
        "aggs": {
          "2": {
            "date_histogram": {
              "field": "dtTime",
              "interval": this.chartInterval.value,
              
              "min_doc_count": 0
            }
          }
        }
      }

      axios.post(process.env.VUE_APP_API+'/query?index='+dateList.join(',')+'&from=0&size=50',chartFilter).then(data=>{
        let _data = _.get(data,'data.aggregations.2.buckets',[]);

        if(_data.length>0) {
          this.drawLine(_data);
        }
      })
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
    this.doSearch();
  }
};
</script>
<style lang="less">

</style>
<style lang="less" src="../assets/less/filters.less" scoped></style>
<style lang="less" scoped>

  #myChart{
    position: absolute;
    top: 20px;
    left: 900px;
    width: calc(100% - 900px);
    min-width: 300px;
    height: 300px;
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