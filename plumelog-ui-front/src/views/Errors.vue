<template>
  <div class="pnl_wraper">
      <div class="pnl_filters">
        <log-header></log-header>
        <Row>
          <div class="pnl_selectAppName" style="display: inline-block;float:left;margin-left: 10px;">
            选择应用名称：
            <Select style="width:220px" filterable clearable v-model="currentAppName" @on-change="handleSelectAppName"  placeholder="选择应用名称">
              <Option v-for="appName in appNames" :value="appName" :key="appName">{{ appName }}</Option>
            </Select>
          </div>
          <div style="display: inline-block;float:left;margin-left: 10px;">
            <RadioGroup v-model="range" @on-change="handleRangeChange" type="button">
              <Radio label="day">日</Radio>
              <Radio label="week">周</Radio>
              <Radio label="month">月</Radio>
            </RadioGroup>
          </div>
          <div style="display: inline-block;float:left;margin-left: 10px;">
            <RadioGroup v-model="logLevel" @on-change="handleLogLevelChange" type="button">
              <Radio label="INFO">INFO</Radio>
              <Radio label="ERROR">ERROR</Radio>
            </RadioGroup>
          </div>
        </Row>
        <div id="errorChart" class="chart"></div>
       </div>
  </div>
</template>

<script>
import axios from '@/services/http'
import _ from 'lodash'
import moment from 'moment'
import 'view-design/dist/styles/iview.css';
import logHeader from '@/components/logHeader.vue'
import "@/assets/less/base.less";

export default {
  name: "Errors",
  data(){
   return {
      range: 'week',
     logLevel: 'ERROR',
       showDialog:false,
       self:this,
       extendList:[],
       appNames:[],
       currentAppName:'',
       formItem:{
           field:'',
           fieldName:''
       },
       appCount: true
   }
  },
  computed:{

  },
  components: {
    logHeader
  },
  methods:{
    handleSelectAppName() {
      this.getErrorRate().then(data => {
        this.drawErrorLine(data)
      });
    },
    handleRangeChange() {
      this.getErrorRate().then(data => {
        this.drawErrorLine(data)
      });
    },
    handleLogLevelChange() {
      this.getErrorRate().then(data => {
        this.drawErrorLine(data)
      });
    },
    drawErrorLine(data) {
      let errorChart = this.$echarts.init(document.getElementById('errorChart'))
      if (data.length == 0) {
        errorChart.clear()
        return;
      }
      errorChart.on('click', object => {
        if(this.appCount) {
          this.currentAppName = object.name;
          this.handleSelectAppName()
        }
      });
      window.addEventListener('resize', () => {
        errorChart.resize();
      });
      errorChart.setOption({
        grid: {
          x: 70,
          y: 10,
          y2:200
        },
        tooltip: {
          position: function (p) {   //其中p为当前鼠标的位置
            return [p[0] - 50, p[1] - 50];
          },
          extraCssText: 'text-align:left'
        },
        xAxis: {
          data: _.map(data, (d) => {
            return d.key.substring(d.key.lastIndexOf(".")+1)
          }),
          axisLabel: {
            fontSize: 12,
            color: '#666',
            interval: 0,
            rotate: 30
          }
        },
        yAxis: {
          axisLabel: {
            fontSize: 12,
            color: '#666',
            formatter: function (value) {
              return value > 1000 ? value / 1000 + "k" : value
            }
          }
        },
        series: [{
          name: '数量',
          type: 'bar',
          data: _.map(data, (d) => {
            return d.doc_count
          }),
          itemStyle: {
            borderColor: 'rgb(255, 0, 0)',
            color: 'rgba(255, 0, 0,0.6)'
          }
        }]
      })
    },
    getErrorRate() {
      let _promise = [];
      //按时间查询日志数量
      let query = {
        "query": {
          "bool": {
            "must": [
              { "term" : {"logLevel" : this.logLevel}}
            ]
          }
        },
        "aggs": {
          "dataCount": {
            "terms": {
              "size":20,
            }
          }
        }
      }
      let dataList = []
      if(this.range === 'day') {
        dataList.push('plume_log_run_' + moment().format('YYYYMMDD') + '*');
      } else if(this.range === 'week') {
        for (let i = 1; i < 8; i++) {
          dataList.push('plume_log_run_' + moment().subtract(i, 'days').format('YYYYMMDD') + '*');
        }
      } else if(this.range === 'month') {
        dataList.push('plume_log_run_' + moment().format('YYYYMM') + '*');
      }
      if(this.currentAppName) {
        query.query.bool.must.push({"term": {"appName": this.currentAppName}});
        query.aggs.dataCount.terms["field"] = "className.keyword"
        this.appCount = false;
      } else {
        query.aggs.dataCount.terms["field"] = "appName"
        this.appCount = true;
      }

      let url = process.env.VUE_APP_API + '/query?index='+dataList.join(",")+'&size=0&from=0&&errChat'

      _promise.push(axios.post(url, query).then(data => {
        return _.get(data, 'data.aggregations.dataCount.buckets', [])
      }))

      return Promise.all(_promise).then(datas => {
        let errorDatas = datas[0];
        if (errorDatas.length == 0) {
          return []
        } else {
          let _array = [];
          for (let i = 0; i < errorDatas.length; i++) {
            let key = errorDatas[i].key;
            let _errorCount = errorDatas[i].doc_count;
            if (_errorCount <= 0) {
              _array.push({
                key,
                doc_count: 0
              })
            } else {
              _array.push({
                key,
                doc_count: _errorCount
              })
            }
          }
          return _array
        }
      })
    },
      getAppNames(){
        if(sessionStorage['cache_appNames']){
            this.appNames = JSON.parse(sessionStorage['cache_appNames'])
        }
        else
        {
            let q = "plume_log_run_" + moment().format("YYYYMMDD") + '*'
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
    this.getAppNames();
    this.getErrorRate().then(data => {
      this.drawErrorLine(data)
    });
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
  .chart {
    position: relative;
    top: 38px;
    padding-left: 20px;
    left: 0;
    width: 100%;
    height: 500px;
  }

</style>
