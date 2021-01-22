<template>
  <div>
    <log-header></log-header>
    <div style="clear:both"></div>
    <div style="margin-top:10px" class="pnl_selectAppName">
      选择应用名称：
      <Select style="width:350px" filterable v-model="search.appName" @on-change="appNameChange"  placeholder="选择应用名称">
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
        width: 150,
        tooltip: true
      },
      {
        title: 'QPS/MAX/MIN/AVG',
        key: 'key',
        ellipsis:true,
        width: 200,
        render:  (h, r) => {
          return h('span', r.row.avg_incr.value.toFixed(1) + "/" + r.row.max_time.value + "/" + r.row.min_time.value + "/" + r.row.avg_time.value.toFixed(1))
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
              },
              {
                "range": {
                  "dtTime": {
                    "gte": Date.parse(startDate),
                    "lt": Date.parse(endDate),
                  }
                }
              }
            ]
          }
        },
        "aggs": {
          "counts": {
            "date_histogram": {
              "field": "dtTime",
              "interval": this.chartInterval().value,
              "min_doc_count": 0
            },
            "aggs": {
              "sum_incr": {
                "sum": {
                  "field": "incr"
                }
              },
              "max_time": {
                "max": {
                  "field": "maxTime"
                }
              },
              "min_time": {
                "min": {
                  "field": "minTime"
                }
              },
              "avg_time": {
                "avg": {
                  "field": "avgTime"
                }
              }
            }
          }
        },
      "sort": [{
          "dtTime":"asc"
        }]
      }

      let q = "plume_log_qps_"  + '*' //+ moment().format("YYYYMMDD")
      axios.post(process.env.VUE_APP_API + '/query?index=' + q + '&from=0&size=100', query).then(data=> {
        let hits = _.get(data, 'data.aggregations.counts.buckets', []).map(x => x);
        this.drawQpsLine(hits)
      })
    },
    chartInterval() {
      if (this.dateTimeRange.length > 0) {
        let start = new Date(this.dateTimeRange[0]);
        let end = new Date(this.dateTimeRange[1]);
        let now = new Date()
        if(end > now) {
          end = now
        }

        let _range = (end.getTime() - start.getTime());

        //大于90天按照10天数据统计
        if (_range > 1000 * 60 * 60 * 24 * 90) {
          return {
            format: 'MM-DD',
            value: 1000 * 60 * 60 * 24 * 10
          }
        }
        //大于30天按照每天数据统计
        else if (_range >= 1000 * 60 * 60 * 24 * 30) {
          return {
            format: 'MM-DD',
            value: 1000 * 60 * 60 * 24
          }
        }
        //大于7天按照每小时数据统计
        else if (_range >= 1000 * 60 * 60 * 24 * 7) {
          return {
            format: 'MM-DD',
            value: 1000 * 60 * 60
          }
        }
        //大于3天按照12小时进行统计
        else if (_range >= 1000 * 60 * 60 * 24 * 3) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60 * 30
          }
        }
        else if (_range >= 1000 * 60 * 60 * 24) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60 * 10
          }
        } else if (_range >= 1000 * 60 * 60 * 12) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60 * 5
          }
        } else if (_range >= 1000 * 60 * 60 * 6) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60 * 3
          }
        } else if (_range >= 1000 * 60 * 60) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 30
          }
        } else if (_range >= 1000 * 60 * 30) {
          return {
            format: 'MM-DD HH:mm:ss',
            value: 1000 * 15
          }
        } else if (_range >= 1000 * 60 * 10) {
          return {
            format: 'MM-DD HH:mm:ss',
            value: 1000 * 10
          }
        } else if (_range >= 1000 * 60 * 2) {
          return {
            format: 'MM-DD HH:mm:ss',
            value: 1000 * 5
          }
        } else {
          return {
            format: 'MM-DD HH:mm',
            value: 1000
          }
        }
      }
      return {
        format: 'MM-DD',
        value: 1000 * 60 * 60 * 24 * 10
      }
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
        title: {
          text: this.selectedUri
        },
        legend: {
          data: ['Qps', 'Max', 'Min', 'Avg']
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        tooltip: {
          formatter(p, ticket) {
            let date = "";
            let qps = "";
            let max = "";
            let min = "";
            let avg = "";

            for (let i = 0; i < p.length; i++) {
              if (p[i].seriesName == 'Qps') {
                date = p[i].name
                qps = p[i].value ? p[i].value : 0
              } else if (p[i].seriesName == 'Max') {
                max = p[i].value ? p[i].value : 0
              } else if (p[i].seriesName == 'Min') {
                min = p[i].value ? p[i].value : 0
              } else if (p[i].seriesName == 'Avg') {
                avg = p[i].value ? p[i].value : 0
              }
            }
            return '时间：' + date + '<br/>Qps：' + qps + '<br/>Max：' + max + '<br/>Min：' + min + '<br/>Avg：' + avg
          },
          position: function (p) {   //其中p为当前鼠标的位置
            return [p[0] - 50, p[1] - 50];
          },
          extraCssText: 'text-align:left',
          trigger: 'axis'
        },
        xAxis: {
          data: _.map(data, (d) => {
            return moment(d.key).format('MM-DD HH:mm:ss')
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
            return d.sum_incr.value
          })
        },{
          name: 'Max',
          type: 'line',
          data: _.map(data, (d) => {
            return d.max_time.value ? d.max_time.value : 0
          })
        },{
          name: 'Min',
          type: 'line',
          data: _.map(data, (d) => {
            return d.min_time.value ? d.min_time.value : 0
          })
        },{
          name: 'Avg',
          type: 'line',
          data: _.map(data, (d) => {
            return d.avg_time.value ? d.avg_time.value.toFixed(1) : 0
          })
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
                  },
                  "max_time": {
                    "max": { "field": "maxTime" }
                  },
                  "min_time": {
                    "min": { "field": "minTime" }
                  },
                  "avg_time": {
                    "avg": { "field": "avgTime" }
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
