<template>
  <div>
    <log-header></log-header>
    <div style="clear:both"></div>
    <div style="margin-top:10px" class="pnl_selectAppName">
      选择应用名称：
      <Select style="width:200px" filterable v-model="search.appName" @on-change="getExtendList"  placeholder="选择应用名称">
        <Option v-for="appName in appNameStore" :value="appName" :key="appName">{{ appName }}</Option>
      </Select>
      <DatePicker ref='datePicker' v-model="dateTimeRange" @on-change="dateChangeHander" type="datetimerange"
                  :options="dateOption" format="yyyy-MM-dd HH:mm:ss" placeholder="选择日期与时间"
                  style="width: 315px;margin-left:10px"></DatePicker>
    </div>
    <Row style="margin-top:10px">
      <Col span="4">
        <Table height="600" highlight-row @on-row-click="rowClickHander" :columns="columns" :data="uriStore"></Table>
      </Col>
      <Col span="20">
        <div id="qps" class="chart"></div>
      </Col>
    </Row>
  </div>
</template>

<script>
import dateOption from './dateOption';
import axios from "@/services/http";
import logHeader from '@/components/logHeader.vue'
import moment from 'moment'
export default {
  name: "Qps",
  components: {
    logHeader,
  },
  data() {
    this.columns = [
      {
        title: 'URI',
        key: 'key',
        ellipsis:true
      },
      {
        title: 'QPS',
        key: 'key',
        ellipsis:true,
        width: 80,
        render:  (h, r) => {
          return h('span', r.row.avg_incr.value.toFixed(2))
        }
      },
    ]
    return {
      dateOption,
      dateTimeRange: [moment(new Date().getTime() - 60 * 1000 * 15).format('YYYY-MM-DD HH:mm:ss'), moment(new Date()).format('YYYY-MM-DD 23:59:59')],
      search: {
        "appName": ""
      },
      appNameStore: [],
      uriStore: [],
      selectedUri:''
    }
  },
  methods: {
    completeFilter(value, option) {
      return option.indexOf(value) == 0;
    },
    appNameChange() {
      this.getExtendList();
    },
    rowClickHander(row, index) {
      this.selectedUri = row.key;
      this.$nextTick(() => {
        this.loadQps()
      })
    },
    dateChangeHander() {
      let startDate = new Date(this.dateTimeRange[0]);
      let endDate = new Date(this.dateTimeRange[1]);
      if (startDate.getHours() == 0 && startDate.getMinutes() == 0 && endDate.getHours() == 0 && endDate.getMinutes() == 0) {
        this.dateTimeRange[1].setHours(23, 59)
        this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
      }
      this.$nextTick(() => {
        this.loadQps()
      })
    },
    loadQps() {
      let now = new Date()
      let startDate = new Date(this.dateTimeRange[0]);
      let endDate = new Date(this.dateTimeRange[1]);
      if(endDate > now) {
        endDate = now
      }
      let uri = this.selectedUri
      let query = {
        "query": {
          "bool": {
            "must": [
              {
                "match": {
                  "appName": this.search.appName
                }
              },
              {
                "match": {
                  "requestURI": uri
                }
              }
              // ,
              // {
              //   "match": {
              //     "serverName": "10.102.126.17"
              //   }
              // }
            ]
          }
          //,
          // "range" : {
          //     "dtTime" : {
          //        "gte": Date.parse(startDate),
          //         "lt": Date.parse(endDate),
          //     }
          // }
        }
      }

      let q = "plume_log_qps_"  + '*' //+ moment().format("YYYYMMDD")
      axios.post(process.env.VUE_APP_API + '/query?index=' + q + '&from=0&size=100', query).then(data=> {
        let hits = _.get(data, 'data.hits.hits', []).map(x => x._source);
        this.drawQpsLine(hits)
      })
    },

    drawQpsLine(data) {
      let chart = this.$echarts.init(document.getElementById('qps'))
      if (data.length == 0) {
        chart.clear()
        return;
      }

      window.addEventListener('resize', () => {
        chart.resize();
      });
      chart.setOption({
        grid: {
          x: 70,
          y: 10
        },
        tooltip: {
          formatter(p, ticket) {
            return '时间：' + p.name + '<br/>Qps：' + p.value
          },
          position: function (p) {   //其中p为当前鼠标的位置
            return [p[0] - 50, p[1] - 50];
          },
          extraCssText: 'text-align:left'
        },
        xAxis: {
          data: _.map(data, (d) => {
            return d.dtTime
          }),
          axisLabel: {
            fontSize: 12,
            color: '#666',
            // rotate:30
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
          name: 'Qps',
          type: 'line',
          data: _.map(data, (d) => {
            return d.incr
          }),
          itemStyle: {
            borderColor: 'rgb(110, 173, 193)',
            color: 'rgba(110, 173, 193,0.6)'
          }
        }]
      })
    },

    getExtendList() {
      if (this.search.appName) {
          let query  = {
            "query": {
              "match": {
                "appName": this.search.appName
              }
            },
            "aggs": {
              "url_avg": {
                "terms": {
                  "field": "requestURI.keyword",
                  "order": { "avg_incr": "desc" } // 根据下述统计的结果排序
                },
                "aggs": {
                  "avg_incr": {
                    "avg": { "field": "incr" }
                  }
                }
              }
            }
          }
        let q = "plume_log_qps_"  + '*' //+ moment().format("YYYYMMDD")
        axios.post(process.env.VUE_APP_API + '/query?index=' + q + '&from=0&size=0', query).then(data=> {
          let buckets = _.get(data, 'data.aggregations.url_avg.buckets', []);
          this.uriStore = buckets
        })
      }
    },
    loadAppName() {
      if (this.appNameStore.length === 0) {
        if (sessionStorage['cache_qps_appNames']) {
          this.appNameStore = JSON.parse(sessionStorage['cache_qps_appNames'])
        } else {
          let q = "plume_log_qps_"  + '*' //+ moment().format("YYYYMMDD")
          axios.post(process.env.VUE_APP_API + '/query?index=' + q + '&from=0&size=0&appName', {
            "aggregations": {
              "dataCount": {
                "terms": {
                  "size": 1000,
                  "field": "appName.keyword"
                }
              }
            }
          }).then(data => {
            let buckets = _.get(data, 'data.aggregations.dataCount.buckets', []).map(item => {
              return item.key
            });
            sessionStorage['cache_qps_appNames'] = JSON.stringify(buckets);
            this.appNameStore = buckets
          })

        }
      }
    },
    init() {
      this.loadAppName()
    }
  },
  mounted() {
    this.init()
  }
}
</script>
<style lang="less" scoped>
.pnl_selectAppName{
  text-align: left;
  padding-left:30px;
}
.chart {
  position: relative;
  top: 38px;
  padding-left: 20px;
  left: 0;
  width: 100%;
  height: 280px;
}
</style>
