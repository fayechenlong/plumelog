<template>
  <div class="pnl_wraper">
    <div class="pnl_filters">
      <log-header></log-header>
      <Row>
        <div class="pnl_selectAppName" style="display: inline-block;float:left;margin-left: 10px;">
          选择应用名称：{{this.loadType}}
          <Select style="width:220px" filterable clearable v-model="currentAppName" @on-change="handleSelectAppName"
                  placeholder="选择应用名称">
            <Option v-for="appName in appNames" :value="appName" :key="appName">{{ appName }}</Option>
          </Select>
        </div>
        <div style="display: inline-block;float:left;margin-left: 10px;">
          <RadioGroup v-model="range" @on-change="handleRangeChange" type="button">
            <Radio label="week">近7天</Radio>
            <Radio label="month">近30天</Radio>
          </RadioGroup>
        </div>
      </Row>
      <Row style="margin-top:10px;padding-left: 37px;">
        <RadioGroup style="float:left" v-model="indexKey" @on-change="handleIndexChange" type="button">
          <Radio v-for="index in indexStore" :key="index.key" :label="index.key">{{index.key}}</Radio>
        </RadioGroup>
      </Row>
      <div id="errorChart" class="chart"></div>
      <div id="classErrorChart" class="chart"></div>
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
  data() {
    return {
      range: 'week',
      showDialog: false,
      self: this,
      extendList: [],
      indexStore: [],
      appNames: [],
      currentAppName: '',
      formItem: {
        field: '',
        fieldName: ''
      },
      loadType: "all", //app //day //hours
      selectedIndex:null,
      indexKey:null
    }
  },
  computed: {},
  components: {
    logHeader
  },
  methods: {
    isNumber(val) {
      let regPos = /^\d+(\.\d+)?$/; //非负浮点数
      let regNeg = /^(-(([0-9]+\.[0-9]*[1-9][0-9]*)|([0-9]*[1-9][0-9]*\.[0-9]+)|([0-9]*[1-9][0-9]*)))$/; //负浮点数
      if(regPos.test(val) || regNeg.test(val)) {
        return true;
      } else {
        return false;
      }
    },
    handleIndexChange(indexName) {
      this.loadType = 'day'
      this.indexKey = indexName
      this.selectedIndex = "plume_log_run_" + indexName + "*"
      this.getErrorRate(this.selectedIndex).then(data => {
        this.drawErrorLine(data)
      });
      this.getClassErrorRate(this.selectedIndex).then(data=> {
        this.drawClassErrorLine(data)
      })
    },
    handleSelectAppName() {
      if(!this.currentAppName) {
        this.loadType = 'all'
        this.selectedIndex = null
        this.indexKey = null
        this.indexStore = []
      } else {
        this.loadType = 'app'
        this.selectedIndex = null
        this.indexKey = null
      }
      this.getErrorRate(this.selectedIndex).then(data => {
        this.drawErrorLine(data)
        this.currentAppName && (this.indexStore = data.filter(m => m.doc_count > 0));
      });
      this.getClassErrorRate(this.selectedIndex).then(data=> {
        this.drawClassErrorLine(data)
      })
    },
    handleRangeChange() {
      if(!this.currentAppName) {
        this.loadType = 'all'
        this.selectedIndex = null
        this.indexStore = []
      } else {
        this.loadType = 'app'
      }
      this.getErrorRate().then(data => {
        this.drawErrorLine(data)
        this.currentAppName && (this.indexStore = data.filter(m => m.doc_count > 0));
      });
      this.getClassErrorRate(this.selectedIndex).then(data=> {
        this.drawClassErrorLine(data)
      })
    },
    drawErrorLine(data) {
      let errorChart = this.$echarts.init(document.getElementById('errorChart'))
      errorChart.off('click') //防止click重复触发
      if (data.length == 0) {
        errorChart.clear()
        return;
      }
      errorChart.on('click', object => {
        if(this.loadType === "all") {
          this.loadType = "app";
          this.currentAppName = object.name;
          this.getErrorRate().then(data => {
            this.drawErrorLine(data)
            this.indexStore = data.filter(m => m.doc_count > 0);
          });
          this.getClassErrorRate(this.selectedIndex).then(data=> {
            this.drawClassErrorLine(data)
          })
        } else if(this.loadType === 'app') {
          this.loadType = 'day'
          this.selectedIndex = "plume_log_run_" + object.name + "*"
          this.indexKey = object.name
          this.getErrorRate(this.selectedIndex).then(data => {
            this.drawErrorLine(data)
          });
          this.getClassErrorRate(this.selectedIndex).then(data=> {
            this.drawClassErrorLine(data)
          })
        }
      });
      window.addEventListener('resize', () => {
        errorChart.resize();
      });
      errorChart.setOption({
        grid: {
          x: 70,
          y: 10,
          y2: 200
        },
        tooltip: {
          position: function (p) {   //其中p为当前鼠标的位置
            return [p[0] - 50, p[1] - 50];
          },
          extraCssText: 'text-align:left'
        },
        xAxis: {
          data: _.map(data, (d) => {
            if(this.isNumber(d.key)) {
              if (typeof(d.key) ===  'number') {
                return moment(d.key).format('MM-DD HH:mm')
              } else {
                return d.key
              }
            } else {
              return d.key.substring(d.key.lastIndexOf(".") + 1)

            }
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
            borderColor: 'rgb(255,0,0)',
            color: 'rgba(255, 0, 0,0.6)'
          }
        }]
      })
    },
    drawClassErrorLine(data) {
      let errorChart = this.$echarts.init(document.getElementById('classErrorChart'))

      if (data.length == 0) {
        errorChart.clear()
        return;
      }
      window.addEventListener('resize', () => {
        errorChart.resize();
      });
      errorChart.setOption({
        title: {
          text: '类统计',
          left: 20
        },
        grid: {
          x: 70,
          y: 70,
          y2: 200
        },
        tooltip: {
          position: function (p) {   //其中p为当前鼠标的位置
            return [p[0] - 50, p[1] - 50];
          },
          extraCssText: 'text-align:left'
        },
        xAxis: {
          data: _.map(data, (d) => {
            if(d.key.substring(d.key.lastIndexOf(".") + 1)) {
              return d.key.substring(d.key.lastIndexOf(".") + 1)
            } else {
              return d.key
            }
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
            borderColor: 'rgb(255,0,0)',
            color: 'rgba(255, 0, 0,0.6)'
          }
        }]
      })
    },
    getClassErrorRate(indexName) {
      this.$Loading.start()
      let _promise = [];
      //按时间查询日志数量
      let query = {
        "query": {
          "bool": {
            "must": [
              {"term": {"logLevel": 'ERROR'}}
            ]
          }
        },
        "aggs": {
          "dataCount": {
            "terms": {
              "size": 20,
              "min_doc_count": 0
            }
          }
        }
      }
      let dataList = [indexName]

      if(this.currentAppName) {
        query.query.bool.must.push({"term": {"appName": this.currentAppName}});
      }

      let localRange = '';
      if(this.loadType === 'all' || this.loadType === 'app') {
        localRange = this.range;
      }

      if (this.loadType === 'app' || this.loadType === 'day') {
        query.aggs.dataCount.terms["field"] = "className.keyword"
        query.aggs.dataCount.terms["size"] = 20
      }


      let url = process.env.VUE_APP_API + '/query?index=' + dataList.join(",") + '&range=' + localRange + '&size=0&from=0&classErrChat'

      _promise.push(axios.post(url, query).then(data => {
        return _.get(data, 'data.aggregations.dataCount.buckets', [])
      }))


      return Promise.all(_promise).then(datas => {
        let errorDatas = datas[0];
        this.$Loading.finish()
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
    getErrorRate(indexName) {
      this.$Loading.start()
      let _promise = [];
      //按时间查询日志数量
      let query = {
        "query": {
          "bool": {
            "must": [
              {"term": {"logLevel": 'ERROR'}}
            ]
          }
        },
        "aggs": {
          "dataCount": {
            "terms": {
              "size": 1000,
              "min_doc_count": 0
            }
          }
        }
      }
      let dataList = []

      if(this.currentAppName) {
        query.query.bool.must.push({"term": {"appName": this.currentAppName}});
      }

      if(this.loadType === 'day') {
        dataList.push(indexName);
        query.aggs.dataCount["date_histogram"] = {
              "field": "dtTime",
              "interval": 1000 * 60 * 60,
        }
        delete query.aggs.dataCount.terms
      }

      let localRange = '';
      if(this.loadType === 'all' || this.loadType === 'app') {
        localRange = this.range;
      }

      if (this.loadType === 'app') {
        query.aggs.dataCount.terms["field"] = "_index"
        query.aggs.dataCount.terms["size"] = 1000
      } else if(this.loadType === 'all') {
        query.aggs.dataCount.terms["field"] = "appName"
        query.aggs.dataCount.terms["size"] = 20
      }


      let url = process.env.VUE_APP_API + '/query?index=' + dataList.join(",") + '&range=' + localRange + '&size=0&from=0&errChat'

      _promise.push(axios.post(url, query).then(data => {
        return _.get(data, 'data.aggregations.dataCount.buckets', [])
      }))


      return Promise.all(_promise).then(datas => {
        let errorDatas = datas[0];
        this.$Loading.finish()
        if (errorDatas.length == 0) {
          return []
        } else {
          if (this.loadType === 'app') {
            let _array = [];
            for (let i = 0; i < errorDatas.length; i++) {
              let key = errorDatas[i].key;
              key = key.substring(14, 22)
              let _errorCount = errorDatas[i].doc_count;
              if (_errorCount <= 0) {
                if (_array.filter(x => x.key === key).length > 0) {
                  _array.filter(x => x.key === key)[0].doc_count + _errorCount
                } else {
                  _array.push({
                    key,
                    doc_count: 0
                  })
                }
              } else {
                if (_array.filter(x => x.key === key).length > 0) {
                  _array.filter(x => x.key === key)[0].doc_count + _errorCount
                } else {
                  _array.push({
                    key,
                    doc_count: _errorCount
                  })
                }
              }
            }
            _array.sort(function(a,b){
              return parseInt(a.key) - parseInt(b.key);
            });
            return _array
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
        }
      })
    },
    getAppNames() {
      if (sessionStorage['cache_appNames'] && sessionStorage['cache_appNames_time']
              && sessionStorage['cache_appNames_time'] > new Date().getTime() - 1800000) {
        this.appNames = JSON.parse(sessionStorage['cache_appNames'])
      } else {
        axios.post(process.env.VUE_APP_API + '/queryAppName?from=0&size=0', {
          "size": 0,
          "aggregations": {
            "dataCount": {
              "terms": {
                "size": 1000,
                "field": "appName"
              }
            }
          }
        }).then(data => {
          let buckets = _.get(data, 'data.aggregations.dataCount.buckets', []).map(item => {
            return item.key
          });
          sessionStorage['cache_appNames'] = JSON.stringify(buckets);
          sessionStorage['cache_appNames_time'] = new Date().getTime();
          this.appNames = buckets;
        })
      }
    }
  },
  mounted() {
    this.getAppNames();
    this.getErrorRate().then(data => {
      this.drawErrorLine(data)
    });
  }
}
</script>
<style lang="less">
.pnl_selectAppName {
  text-align: left;
  padding-left: 30px;
}

.pnl_data {
  width: 800px;
  padding: 30px;
  text-align: left;

  .pnl_controls {
    margin-bottom: 10px;

    .btn_delField {
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
