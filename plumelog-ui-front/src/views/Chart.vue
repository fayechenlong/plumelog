<template>
  <div class="pnl_wraper">
    <div class="pnl_filters">
      <log-header></log-header>
      <div style="clear:both"></div>
    </div>
    <div class="pnl_chart">
      日期和时间：
      <DatePicker ref='datePicker' v-model="dateTimeRange" @on-change="dateChange" type="datetimerange" :options="dateOption" format="yyyy-MM-dd HH:mm" placeholder="选择日期与时间" style="width: 280px"></DatePicker>
      <Button type="primary" icon="ios-search" class="btn_search" @click="doSearch">查询</Button>
      <div style="clear:both"></div>
      <br>
      <br>
      <div id="chart_error" class="chart"></div>
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
          } //小于1天按照小时统计
          else if (_range < 1000*60*60*24){
            return {
              format:'HH:mm',
              value: 1000*60
            }
          }else
          {
            return {
              format:'MM-DD HH:mm',
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
        myChart.setOption(option);
    },
    getChartData(){

      let query = {};
      let startDate=  _.clone(new Date(this.dateTimeRange[0]));
      let endDate = new Date(this.dateTimeRange[1]);

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
          "dataCount": {
            "date_histogram": {
              "field": "dtTime",
              "interval": this.chartInterval.value,
              "min_doc_count": 0
            }
          }
      }

      let url = process.env.VUE_APP_API + '/plumelogServer/clientQuery?size=1000&from=0&clientStartDate=' + Date.parse(this.dateTimeRange[0])
              + '&clientEndDate=' + Date.parse(this.dateTimeRange[1]);
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
    getErrorRate(){

      let startDate=  _.clone(new Date(this.dateTimeRange[0]));
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


      let url = process.env.VUE_APP_API + '/plumelogServer/clientQuery?size=1000&from=0&clientStartDate=' + Date.parse(this.dateTimeRange[0])
              + '&clientEndDate=' + Date.parse(this.dateTimeRange[1]);

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
                doc_count:(_errorCount/_totalCount).toFixed(4)
              })
            }
          }

          return _.map(_array,d=>{
            return {
              name:d.key,
              value:d.doc_count
            }
          })

        }
      })
    },
    doSearch(){
      this.getChartData().then(data=>{
        this.draw(data,{
          chartId:'chart_error',
          title: {
              text: '日志数量',
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
            name: '数量',
            type: 'bar',
            data: _.map(data,(d)=>{
              return d.value
            }),
            itemStyle: {
              borderColor: 'rgb(110, 173, 193)',
              color: 'rgba(110, 173, 193,0.6)'
            }
          }]
        })
      })

      this.getErrorRate().then(data=>{
        console.log(data);
        this.draw(data,{
          chartId:'chart_errorRate',
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
                return '时间：'+p.name+'<br/>错误率：'+p.value*100+'%'
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
                name: '数量',
                type: 'bar',
                data: _.map(data,(d)=>{
                  return d.value
                }),
                itemStyle:{
                    borderColor: 'rgb(255, 0, 0)',
                    color: 'rgba(255, 0, 0,0.6)'
                }
            }]
        })
      });

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
      margin-left: 10px;
      // margin-top: 20px;
    }

    .chart{
      float:left;
      width:45%;
      height:500px;
      margin-right:2%
    }
  }

</style>
