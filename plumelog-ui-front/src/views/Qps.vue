<template>
  <div>
    <log-header></log-header>
    <div style="clear:both"></div>
    <div style="margin-top:10px" class="pnl_selectAppName">
      选择应用名称：
      <Select style="width:200px" filterable v-model="search.appName" @on-change="getExtendList"  placeholder="选择应用名称">
        <Option v-for="appName in appNameStore" :value="appName" :key="appName">{{ appName }}</Option>
      </Select>
    </div>
    <Row style="margin-top:10px">
      <Col span="4">
        <Table :columns="columns" :data="appNameStore"></Table>
      </Col>
      <Col span="20">qps</Col>

    </Row>
  </div>
</template>

<script>

import axios from "@/services/http";
import logHeader from '@/components/logHeader.vue'

export default {
  name: "Qps",
  components: {
    logHeader,
  },
  data() {
    this.columns = [
      {
        title: '应用',
        key: 'name'
      },
    ]
    return {
      search: {
        "appName": ""
      },
      appNameStore: [],
    }
  },
  methods: {
    completeFilter(value, option) {
      return option.indexOf(value) == 0;
    },
    appNameChange() {
      this.getExtendList();
    },
    getExtendList() {
      if (this.search.appName) {

      }
    },
    loadAppName() {
      if (this.appNameStore.length === 0) {
        if (sessionStorage['cache_appNames']) {
          this.appNameStore = JSON.parse(sessionStorage['cache_appNames'])
        } else {
          let q = "plume_log_qps_" + moment().format("YYYYMMDD") + '*'
          axios.post(process.env.VUE_APP_API + '/query?index=' + q + '&from=0&size=0&appName', {
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
<style lang="less">
.pnl_selectAppName{
  text-align: left;
  padding-left:30px;
}
</style>
