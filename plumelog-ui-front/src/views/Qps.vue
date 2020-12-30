<template>
  <div>
    <log-header></log-header>
    <Row style="margin-top:10px">
      <Col span="4">
        <Table :columns="appColumns" :data="appNameStore"></Table>
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
    this.appColumns = [
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
          this.appNameStore = JSON.parse(sessionStorage['cache_appNames']).map(k => {
            return {
              "name": k
            }
          })
        } else {
          let q = "plume_log_run_" + moment().format("YYYYMMDD") + '*'
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
            this.appNameStore = buckets.map(k => {
              return {
                "name": k
              }
            });
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
