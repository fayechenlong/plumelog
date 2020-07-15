<template>
  <div class="pnl_wraper">
    <div class="icon_arrow" :class="{'up':showFilter,'down':!showFilter}" @click="setShowFilter">
      <Icon type="ios-arrow-up" v-show="showFilter"  />
      <Icon type="ios-arrow-down" v-show="!showFilter" />
      <span class="text">{{showFilter?'收起':'展开'}}</span>
    </div>
    <div class="pnl_filters" >
      <template v-if="showFilter">
        <div class="alert alert-danger" v-if="danger_str" role="alert">
          {{danger_str}}
        </div>
        <log-header></log-header>
        
        <table class='tbl_filters'>
          <tbody>
            <tr>
              <td class="key">应用名称</td>
              <td>
                <Input class="txt txtAppName" name="appName" v-model="filter.appName" placeholder="搜索多个请用逗号或空格隔开" :clearable="true" />
                <Checkbox v-model="isExclude">排除</Checkbox>
              </td>
            </tr>
            <tr>
              <td class="key">日志等级</td>
              <td>
                <Select v-model="filter.logLevel" multiple placeholder="请选择日志等级">
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

        <Carousel v-model="slideIndex" arrow="never">
          <CarouselItem>
            <div id="myChart" class="chart"></div>
          </CarouselItem>
          <CarouselItem>
              <div id="errorChart" class="chart"></div>
          </CarouselItem>
        </Carousel>
      
        <div style="clear:both"></div>
        <table class="tbl_filters" style="width:865px">
            <tr v-if="!useSearchQuery">
              <td class="key">内容</td>
              <td>
                <Input class="txt" @on-enter="doSearch()" :clearable="true" style="width:605px" placeholder="输入搜索内容" v-model="searchKey" />
                <a href="javascript:void(0)" @click="useSearchQuery=true" class="link_changeModal">切换为条件模式</a>
              </td>
            </tr>
             <tr v-if="useSearchQuery">
              <td class="key">条件</td>
              <td>
                <Select v-if="searchOptions.length>0" v-model="selectOption" style="width:80px;margin-right:10px">
                    <Option value="AND" key="AND">AND</Option>
                    <Option value="OR" key="OR">OR</Option>
                    <Option value="NOT" key="NOT">NOT</Option>
                </Select>
                <Input class="txt" @on-enter="addTag()" :clearable="true" v-model="tag" placeholder="输入搜索条件" style="width:196px;"   />
                <Button icon="md-add" @click="addTag" style="margin-left:10px">添加</Button>
                <a href="javascript:void(0)" @click="useSearchQuery=false" class="link_changeModal">切换为内容模式</a>
              </td>
            </tr>
            <tr v-if="useSearchQuery">
              <td></td>
              <td>
                <Tag closable v-for="(tag,index) in searchOptions" @on-close="closeTag(index)" :key="index">
                  <template v-if="index>0">{{tag.type}}&nbsp;</template>
                  {{tag.tag}}
                  </Tag>
              </td>
            </tr>
            <tr>
              <td></td>
              <td style='padding-top:8px;text-align:right'>
                <Button style="margin-right:10px" @click="clear">重置</Button>
                <Button type="primary" icon="ios-search" @click="doSearch">查询</Button>
              </td>
            </tr>
        </table>
      </template>
       <div style="clear:both"></div>
    </div>
    <div style="position:relative;margin-top:30px;">
      <div style="position:absolute;top:-30px;right:20px">共 <b>{{totalCount}}</b> 条数据</div>
      <div class="tip_table"><Icon size="14" type="md-star-outline" /> 表格字段宽度可拖拽调节，双击或点击箭头可查看详情</div>
          <Table size="small" border highlight-row :columns="showColumns" :content="self" @on-row-dblclick="dblclick" :row-class-name="getRowName" :data="list.hits">
            <template slot-scope="{ row }" slot="className">
              {{row.className | substr}}
              <Icon type="ios-search" v-if="row.logLevel" @click="doSearch('className',row)" />
            </template>
            <template slot-scope="{ row }" slot="logLevel">
              {{row.logLevel}}
              <Icon type="ios-search" v-if="row.logLevel" @click="doSearch('logLevel',row)" />
            </template>
            <template slot-scope="{ row }" slot="serverName">
              {{row.serverName}}
              <Icon type="ios-search" v-if="row.serverName" @click="doSearch('serverName',row)" />
            </template>
            <template slot-scope="{ row }" slot="appName">
              {{row.appName}}
              <Icon type="ios-search" v-if="row.appName" @click="doSearch('appName',row)" />
            </template>
            <template slot-scope="{ row }" slot="traceId">
              <a :href="'./#/trace?traceId='+row.traceId+'&timeRange='+JSON.stringify(dateTimeRange)" title="点击查看链路追踪">{{row.traceId}}</a>
              <Icon type="ios-search" v-if="row.traceId" @click="doSearch('traceId',row)" />
            </template>
            <template  slot-scope="{ row }" slot="content">
              <div v-html="substr((row.highlightCnt || row.content),200)"></div>
            </template>
          </Table>
    </div>

    <nav class="page_nav" aria-label="Page navigation example">
      <div class="pnl_select">
        <span class="name">显示字段：</span>
         <Select v-model="showColumnTitles" multiple placeholder="选择要显示的字段" @on-change="columnsChange" :max-tag-count="2" style="width:270px">
          <Option v-for="item in allColumns" :value="item.value" :key="item.value">{{ item.label }}</Option>
        </Select>
      </div>
     
      <ul  v-if="totalCount && parseInt(totalCount/size) > 0"  class="pagination justify-content-center" style="float:right;margin-right:30px">
        <li class="page-item" :class="{'disabled': !isShowLastPage }">
          <a class="page-link" href="javascript:void(0)" @click="prevePage" tabindex="-1">上一页</a>
        </li>
        <li class="page-item" :class="{'disabled': !haveNextPage }">
          <a class="page-link" href="javascript:void(0)" @click="nextPage">下一页</a>
        </li>
        <li class="page-item">
          <div class="page-count">跳转至第 <InputNumber style="width:50px" size="small" :min="1" :max="parseInt(totalCount/size)+1" v-model="jumpPageIndex" /> 页 <Button @click="goPage" style="font-size:12px" size="small">确定</Button></div>
        </li>
        <li class="page-item"><div class="page-count">第{{parseInt(from/size)+1}}页 / 共{{  parseInt(totalCount/size)+1}}页</div></li>
      </ul>
    </nav>
    
  </div>
</template>

<script>
import axios from '@/services/http'
import _ from 'lodash'
import moment from 'moment'
import '@/assets/prism.js' 
import '@/assets/prism.css'
import 'view-design/dist/styles/iview.css';
import logHeader from '@/components/logHeader.vue'
import "@/assets/less/base.less";
import dateOption from './dateOption';
import $ from 'jquery'
import expandRow from '@/components/table-expand.vue';

export default {
  name: "Home",
  data(){
   return {
     tag:"",
     useSearchQuery:false,
     selectOption:'AND',
     isExclude:false,
     slideIndex:0,
     self:this,
     jumpPageIndex:1,
     chartData:[],
     searchOptions:[],
     showColumnTitles: ['logLevel','serverName','appName','traceId','className'],
     allColumns:[
       {
         label:'日志等级',
         value:'logLevel'
       },
       {
         label:'服务器名称',
         value:'serverName'
       },
       {
         label:'应用名称',
         value:'appName'
       },
       {
         label:'追踪码',
         value:'traceId'
       },
       {
         label:'类名',
         value:'className'
       }
     ],
     showFilter: true,
     api: process.env.api,
     dateOption,
     dateTimeRange:[moment(new Date()).format('YYYY-MM-DD 00:00:00'),moment(new Date()).format('YYYY-MM-DD 23:59:59')],
     content:{
       _source:{}
     },
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
     from:0,
     columns:[
       {
            type: 'expand',
            width: 50,
            render: (h, params) => {
                return h(expandRow, {
                    props: {
                        row: params.row,
                        searchKey: this.searchKey
                    }
                })
            }
        },
       {
            title: '时间',
            key: 'dtTime',
            sortable: true,
            width:150,
            resizable: true,
            render: (h, params) => {
              return h('div', moment(params.row.dtTime).format('YYYY-MM-DD HH:mm:ss'))
            }
        },
        {
            title: '日志等级',
            key: 'logLevel',
            align:'center',
            slot: 'logLevel',
            className: 'icon',
            resizable: true,
            sortable: true,
            width:120
        },
        {
            title: '服务器名称',
            align:'center',
            key: 'serverName',
            slot: 'serverName',
            className: 'icon',
            sortable: true,
            resizable: true,
            width:150
        },
        {
            title: '应用名称',
            align:'center',
            key: 'appName',
            slot: 'appName',
            className: 'icon',
            sortable: true,
            resizable: true,
            width:150
        },
        {
            title: '追踪码',
            align:'center',
            key: 'traceId',
            width:170,
            className: 'icon',
            sortable: true,
            resizable: true,
            slot: 'traceId',
        },
        {
            title: '类名',
            align:'center',
            key: 'className',
            slot: 'className',
            className: 'icon',
            sortable: true,
            width:270
        },
        {
            title: '内容',
            align:'center',
            key: 'content',
            slot:'content',
            ellipsis:true
        }
     ],
     sort:[{
        "dtTime":"desc"
     }]
   }
  },
  components: {
    // HelloWorld
    logHeader,
    expandRow
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
    searchQuery(){
      var query="";
      for(var i=0;i<this.searchOptions.length;i++){
        var item = this.searchOptions[i];
        if(i>0){
          query+=" "+item.type+" ";
        }
        query+='"'+item.tag+'"';
      }
      return query;
    },
    showColumns(){
      var columns =[this.columns[0],this.columns[1]];
      for(let title of this.showColumnTitles){
        let _c = _.find(this.columns,['key',title]);
        if(_c){
          columns.push(_c)
        }
      }
      columns.push(_.find(this.columns,['key','content']))
      return columns;
    },
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
      let value = _.get(this.list,'total.value',0)
      if(!this.list.total && value==0){
        return 0
      }
      if(value == 0 ) return 0;
      return value || this.list.total
    },
    isShowLastPage(){
      return this.from > 0 
    },
    haveNextPage(){
      if(this.totalCount>(this.from+this.size))
        return true
      else
        return false
    }
  },
  methods:{
    closeTag(index){
      this.searchOptions.splice(index,1);
    },
    addTag(){
      if(this.tag){
        this.searchOptions.push({
          type:this.selectOption,
          tag:this.tag
        })
        this.tag="";
      }
    },
    columnsChange(){
      this.list.hists = _.clone(this.list.hists);
      localStorage['cache_showColumnTitles'] = JSON.stringify(this.showColumnTitles);
    },
    substr(str,limit){
      limit = limit || 30;
      if(str.length>limit){
        return str.substring(0,limit)+'...';
      }
      return str;
    },
    getRowName(row,index){
      return row.logLevel+' '+row.id
    },
    dblclick(row,index){
      var ele = $('.'+row.id);
      ele.find('.ivu-table-cell-expand').click();
    },
    sortChange({key,order}){
      let sort = {};
      sort[key]=order;
      this.sort = [sort]
      $('.row_detail').remove();
      this.doSearch();
    },
    setShowFilter(show){
      this.showFilter = !this.showFilter;
      if(this.showFilter ){
        this.$nextTick(()=>{
          this.drawLine()
        })
      }
    },
    drawLine(){
        if(this.chartData.length==0){
          return false;
        }
        let myChart = this.$echarts.init(document.getElementById('myChart'))

        window.addEventListener('resize',() => { myChart.resize(); });

        // 绘制图表
        myChart.setOption({
            title: {
                text: '数量',
                left: 'center',
                top: 20,
                textStyle: {
                    color: '#333'
                }
            },
            tooltip: {
              formatter(p,ticket){
                return '时间：'+p.name+'<br/>数量：'+p.value+'条'
              },
              extraCssText:'text-align:left'
            },
            xAxis: {
                data: _.map(this.chartData,(d)=>{
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
                type: 'bar',
                data: _.map(this.chartData,(d)=>{
                  return d.doc_count
                }),
                itemStyle:{
                    borderColor: 'rgb(110, 173, 193)',
                    color: 'rgba(110, 173, 193,0.6)'
                }
            }]
        });
    },
    drawErrorLine(data){
        let errorChart = this.$echarts.init(document.getElementById('errorChart'))
        window.addEventListener('resize',() => { errorChart.resize(); });
        errorChart.setOption({
            title: {
                text: '错误率',
                left: 'center',
                top: 20,
                textStyle: {
                    color: '#333'
                }
            },
            tooltip: {
              formatter(p,ticket){
                return '时间：'+p.name+'<br/>错误率：'+parseInt(p.value*100)+'%'
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
                type: 'bar',
                data: _.map(data,(d)=>{
                  return d.doc_count
                }),
                itemStyle:{
                    borderColor: 'rgb(255, 0, 0)',
                    color: 'rgba(255, 0, 0,0.6)'
                }
            }]
        })
    },
    getShouldFilter(){
      let filters = [];
      let date=[];
      for(let itemKey in this.filter)
      {
        if(this.isExclude && itemKey == 'appName'){
          continue;
        }
        else if(this.filter[itemKey]){

          let _data = this.filter[itemKey];
          let query = '';
          //判断是否是数组
          if(Array.isArray(_data)){
           query=_data.join(',');
            if(query){
               filters.push({
                "query_string":{
                  "query":query,
                  "default_field": itemKey
                }
              })
            }
          }
          else
          {
            query = _data.replace(/,/g,' ');
            filters.push({
              "match_phrase":{
                [itemKey]:{
                  "query":query
                }
              }
            })
          }
        }
      }

      if((this.searchQuery && this.useSearchQuery) || (this.searchKey && !this.useSearchQuery)){
        filters.push({
            "query_string":{
              "query": this.useSearchQuery ? this.searchQuery : this.searchKey,
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
    doSearch(keyName,item){

      if(keyName && item){

        if(keyName == 'appName' && this.isExclude && this.filter[keyName]){
          this.filter[keyName]+=','+item[keyName]
        }
        else
        {
          this.filter[keyName] = item[keyName]
        }
      }

      //列出范围内的日期
      let dateList=[];
      let startDate = _.clone(new Date(this.dateTimeRange[0]));

      let shouldFilter = this.getShouldFilter();
      
      if(startDate){
         while(startDate<=this.dateTimeRange[1]){
          dateList.push('plume_log_run_'+moment(startDate).format('YYYYMMDD'))
          startDate = new Date(startDate.setDate(startDate.getDate()+1));
        }
      }
     
      if(dateList.length==0){
        dateList.push('plume_log_run_'+moment().format('YYYYMMDD'));
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

      if(this.isExclude && this.filter['appName']){

        let mustNotArr=[];
        for(let appName of this.filter['appName'].split(',')){
          mustNotArr.push({
              "match_phrase":{
                  'appName':{
                    "query":appName.replace(/,/g,' ')
                  }
                }
            })
        }

        query.query.bool['must_not']=mustNotArr;
      }

      let esFilter = {
        ...query,
        "highlight": {
            "fields" : {
                "content" : {}
            }
        },
        "sort":this.sort
      };

      this.$Loading.start();
      
      let searchUrl = url+'&size='+this.size+"&from="+this.from;
      axios.post(searchUrl,esFilter).then(data=>{
        
        this.$Loading.finish();
        let _searchData = _.get(data,'data.hits',{
          total:0,
          hits:[]
        })

        _searchData.hits = _.map(_searchData.hits,item=>{
          return {
            id:item._id,
            highlightCnt:_.get(item,"highlight.content[0]",""),
            ...item._source,
          }
        })

         this.list = _searchData;
        
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
        this.chartData = _.get(data,'data.aggregations.2.buckets',[]);
        this.drawLine();
      })

      this.getErrorRate(dateList).then(data=>{
        console.log('errorData',data)
        this.drawErrorLine(data)
      });
    },
    getErrorRate(dateList){
        let startDate=new Date(this.dateTimeRange[0]);
        let endDate=new Date(this.dateTimeRange[1]);
        let _promise =[];
        //按时间查询日志数量
        let query = {};
        let aggs = {
          "dataCount":{
            "date_histogram": {
              "field": "dtTime",
              "interval": this.chartInterval.value,
              "min_doc_count": 0
            }
          }
        }
        query = { 
          "query": {
              "bool": {
                "must": [{
                  "range":{
                    "dtTime":{
                      "gte": Date.parse(startDate),
                      "lt": Date.parse(endDate)
                    }
                  }
                }
              ]
            }
          },
          aggs
        }

        let _errorQuery = {
          "query": {
              "bool": {
                "must": [{
                  "range":{
                    "dtTime":{
                      "gte": Date.parse(startDate),
                      "lt": Date.parse(endDate)
                    }
                  }
                },{
                  "match_phrase":{
                    'logLevel':{
                      "query":'ERROR'
                    }
                  }
                }
              ]
            }
          },
          aggs
        }
   

        let url= process.env.VUE_APP_API+'/query?size=1000&from=0&index='+dateList.join(',')

        _promise.push(axios.post(url,query).then(data=>{
          return _.get(data,'data.aggregations.dataCount.buckets',[])
        }))
        _promise.push(axios.post(url,_errorQuery).then(data=>{
          return _.get(data,'data.aggregations.dataCount.buckets',[])
        }))

        return Promise.all(_promise).then(datas=>{
          let totalDatas = datas[0];
          let errorDatas = datas[1];
          if(errorDatas.length==0 || errorDatas.length<totalDatas.length){
            return []
          }
          else
          {
            let _array = [];
            for(var i=0;i<errorDatas.length;i++){
              let key = errorDatas[i].key;
              let _errorCount = errorDatas[i].doc_count;
              let _totalCount = totalDatas[i].doc_count;
              if(_errorCount<=0 || _totalCount<=0)
              {
                _array.push({
                  key,
                  doc_count:0
                })
              }
              else
              {
                _array.push({
                  key,
                  doc_count:(_errorCount/_totalCount).toFixed(2)
                })
              }
            }
            return _array
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
    },
    goPage(){
      this.from = (this.jumpPageIndex-1) * this.size;
      if(this.from >0)
      {
        this.doSearch();
      }
    },
    init(){
      this.clear();
      let titles = localStorage['cache_showColumnTitles'];
      if(titles){
        this.showColumnTitles = JSON.parse(titles)
      }
      
      if(this.$route.query.appName){
        this.filter['appName'] = this.$route.query.appName;
      }
      if(this.$route.query.className){
        this.filter['className'] = this.$route.query.className;
      }
      if(this.$route.query.logLevel){
        this.filter['logLevel'] = this.$route.query.logLevel;
      }
      if(this.$route.query.time){
        let times = this.$route.query.time.split(',');
        if(times.length>1){
          this.dateTimeRange = [moment(parseInt(times[0])).format('YYYY-MM-DD HH:mm:ss'),moment(parseInt(times[1])).format('YYYY-MM-DD HH:mm:ss')]
          this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
        }
      }

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
    },
    "$route": "init"
  },
  mounted(){
    this.init();
  }
};
</script>
<style lang="less">
  .ivu-table-wrapper{
    overflow: unset;
  }

  .tip_table{
    position:absolute;
    top:-30px;
    left:10px;
    color:#aaa;
    font-size:12px;
  }

  .ivu-table-row-highlight td,.ivu-table-row-hover td
  {
    background-color:#ebf7ff !important;
  }
  .ivu-table-row:nth-child(2n) td{
    background-color: #f8f8f9
  }
  .ivu-table-small td
  {
    height: 30px;
  }
  .ivu-table
  {
    font-size:12px;
    overflow: unset;

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

    .ivu-table-cell{
      position: relative;
    }

    .ivu-table-header
    {
       position:sticky !important;
       top:-1px; 
       z-index: 10;       
    }
    td{
        position: relative;
        &.ivu-table-expanded-cell{
          padding:0;
        }
        &.icon{
          &:hover{
            i{
                display: inline;
              }
          }
          i{
              cursor: pointer;
              position: absolute;
              top: 2px;
              right:5px;
              font-size: 16px;
              display:none;
          }
      }
      em{
        background: #ff0;
      }
    }

    .detail_table
    {
      width:100%;
      margin: 10px auto 30px auto;

      .key{
        width:150px;
        text-align: right;
        padding-right:20px;
        vertical-align: top;
        font-weight:700;
        div{
          width:150px;
        }
      }
      .value{
        text-align: left;
         vertical-align: top;
      }
      tr{
        background: none !important;
        td{
          padding-top:10px;
          border: none;
          height: auto;
        }
      }
    }
  }
</style>
<style lang="less" src="../assets/less/filters.less" scoped></style>
<style lang="less" scoped>


  .ivu-carousel
  {
    position: absolute;
     top: 20px;
      right: 10px;
      width: calc(100% - 1000px);
      min-width: 300px;
      height: 300px;
  }

  .chart{
    position: relative;
    top: 20px;
    left: 0;
    width: 100%;
    height: 280px;
  }


  .icon_arrow
  {
    cursor: pointer;
    position: absolute;
    font-size:20px;
    top: 290px;
    left: 50%;
    transform: translateX(-50%);
    width:100px;
    height:50px;
    z-index:10;
    
    &.down{
      top: 10px;
    }

    .text{
      font-size:14px;
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