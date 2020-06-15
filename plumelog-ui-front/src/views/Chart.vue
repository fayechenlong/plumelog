<template>
  <div class="pnl_wraper">
    <div class="pnl_filters">
      <log-header></log-header>
      <div style="clear:both"></div>
    </div>
    <div class="pnl_chart">
      日期和时间：
      <DatePicker ref='datePicker' v-model="dateTimeRange" @on-change="dateChange" type="datetimerange" :options="dateOption" format="yyyy-MM-dd HH:mm" placeholder="选择日期与时间" style="width: 280px"></DatePicker>
      <br/>
      <Button type="primary" icon="ios-search" class="btn_search" @click="doSearch">查询</Button>
      <div style="clear:both"></div>
      <div id="chart_errorRate" class="chart"></div>
      <!-- <div id="chart_appName" class="chart"></div>
      <div id="chart_serverName" class="chart"></div> -->
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
  name: "Chart",
  data(){
   return {
     dateTimeRange:[moment(new Date()).format('YYYY-MM-DD 00:00:00'),moment(new Date()).format('YYYY-MM-DD 23:59:59')],
     dateOption
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
      }
  },
  components: {
    logHeader
  },
  methods:{
    dateChange(){
        let startDate = new Date(this.dateTimeRange[0]);
        let endDate = new Date(this.dateTimeRange[1]);
        if(startDate.getHours() == 0 && startDate.getMinutes() ==0 && endDate.getHours() == 0 && endDate.getMinutes() == 0){
          this.dateTimeRange[1].setHours(23,59)
          this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
        }
    },
    draw(data,option){
        if(data.length==0){
          return false;
        }

        let myChart = this.$echarts.init(document.getElementById(option.chartId))

        window.addEventListener('resize',() => { myChart.resize(); });

        // 绘制图表
        // myChart.setOption({
        //   title: {
        //       text: option.chartTitle,
        //       left: 'center',
        //       top: 30,
        //       textStyle: {
        //           color: '#333'
        //       }
        //   },
        //   tooltip: {
        //       trigger: 'item',
        //       formatter: '{a} <br/>{b} : {c} ({d}%)'
        //   },
        //   series: [
        //       {
        //           name: option.seriesName,
        //           type: 'pie',
        //           radius: '55%',
        //           center: ['50%', '50%'],
        //           data: data.sort(function (a, b) { return a.value - b.value; }),
        //           // roseType: 'radius',
        //           label: {
        //               color: '#666'
        //           },
        //           labelLine: {
        //               lineStyle: {
        //                   color: '#ccc'
        //               },
        //               smooth: 0.2,
        //               length: 10,
        //               length2: 20
        //           },
        //           itemStyle: {
                      
        //               shadowBlur: 30,
        //               shadowColor: 'rgba(0, 0, 0, 0.2)'
        //           },

        //           animationType: 'scale',
        //           animationEasing: 'elasticOut',
        //           animationDelay: function (idx) {
        //               return Math.random() * 200;
        //           }
        //       }
        //   ]
        // });

        // 绘制图表
        myChart.setOption({
            title: {
                text: option.chartTitle,
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
                data: _.map(data,(d)=>{
                  return  moment(d.name).format(this.chartInterval.format) 
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
                name: option.seriesName,
                type: 'line',
                data: _.map(data,(d)=>{
                  return d.value
                }),
                itemStyle:{
                    borderColor: 'red',
                    color: 'red'
                }
            }]
        });
    },
    getChartData(name){

      let dateList=[];

      let query = {};
      let startDate=  _.clone(new Date(this.dateTimeRange[0]));
      let endDate=new Date(this.dateTimeRange[1]);
     
       //判断日期
        
      let _date = _.clone(new Date(this.dateTimeRange[0]));
    
      if(_date){
        while(_date<=this.dateTimeRange[1]){
          //plume_log_run_
          dateList.push('easy_log_'+moment(_date).format('YYYYMMDD'))
          _date = new Date(_date.setDate(_date.getDate()+1));
        }
      }
    
      if(dateList.length==0){
        dateList.push('easy_log_'+moment().format('YYYYMMDD'));
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
        }
      }
      query.aggs = {
        "dataCount":{
          "date_histogram": {
            "field": "dtTime",
            "interval": this.chartInterval.value,
            "min_doc_count": 0
          }
        }
      }
        // "dataCount": {
        //   "terms": {
        //   "field": name+".keyword",
        //     "size": 5,
        //     "order": {
        //       "_count": "desc"
        //     }
        //   }
        // }

      let url= process.env.VUE_APP_API+'/query?size=1000&from=0&index='+dateList.join(',')
      return axios.post(url,query).then(data=>{
        let _data = _.get(data,'data.aggregations.dataCount.buckets',[])
        return _.map(_data,d=>{
          return {
            name:d.key,
            value:d.doc_count
          }
        })
      })
    },
    doSearch(){
      this.getChartData('logLevel').then(data=>{
        this.draw(data,{
          chartId:'chart_errorRate',
          chartTitle:'日志错误量',
          seriesName:'错误数量'
        })
      })
      // this.getChartData('appName').then(data=>{
      //   this.draw(data,{
      //     chartId:'chart_appName',
      //     chartTitle:'应用名称分布图',
      //     seriesName:'应用名称'
      //   })
      // })
      // this.getChartData('serverName').then(data=>{
      //   this.draw(data,{
      //     chartId:'chart_serverName',
      //     chartTitle:'服务器名称分布图',
      //     seriesName:'服务器名称'
      //   })
      // })
    }
  },
  mounted(){
    this.doSearch();
  }
}
</script>
<style lang="less" src="../assets/less/filters.less" scoped></style>
<style lang="less">
  .pnl_chart
  {
    padding-left:20px;
    position: relative;
    text-align: left;

    .btn_search
    {
      margin-left: 88px;
      margin-top: 20px;
    }

    .chart{
      float:left;
      min-width:500px;
      width:100%;
      max-width:1000px;
      height:500px;
      margin-right:50px
    }
  }
  
</style>