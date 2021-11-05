<template>
  <div class="pnl_wraper">
    <div class="pnl_filters">
      <template>
        <log-header></log-header>
        <!--查询条件-->
        <div style="width: 1000px">
          <table class='tbl_filters' style="width: 100%">
            <tbody>
            <tr>
              <td class="key">应用名称</td>
              <td style="width: 245px">
                <AutoComplete
                    v-model="appName"
                    :data="appNameComplete"
                    class="txt"
                    placeholder="应用名称，可搜索"
                    :clearable="true"
                    :filter-method="completeFilter"
                    @on-change="appNameChange">
                </AutoComplete>
              </td>
              <td class="key">显示行数</td>
              <td style="width: 245px">
                <RadioGroup v-model="rowLen" type="button" button-style="solid">
                  <Radio :label="300"></Radio>
                  <Radio :label="500"></Radio>
                  <Radio :label="800"></Radio>
                  <Radio :label="1000"></Radio>
                </RadioGroup>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </template>
      <div style="clear:both"></div>
    </div>
    <div class="pnl_content" id="plume-console">
      <div class="log_model_normal auto_word_wrap  dark_mode" style="min-height: calc(100vh);" >
        <div class="log_model_normal_row" @mouseenter="logModelNormalRowMouseEnter($event)"
             @mouseleave="logModelNormalRowMouseLeave($event)" v-for="row in list" >
          <div class="log_model_normal_row_selector row_over_flow_hidden">
            <Tooltip placement="top-start" :delay="1500">
              <div slot="content">
                <table>
                  <tbody>
                  <tr>
                    <td class="tooltip_table_td_key">logTime:</td>
                    <td class="tooltip_table_td_value">{{ dateFormat(row.dtTime) }}</td>
                  </tr>
                  <tr v-if="row.dateTime">
                    <td class="tooltip_table_td_key">serverTime:</td>
                    <td class="tooltip_table_td_value">{{ row.dateTime }}</td>
                  </tr>
                  <tr>
                    <td class="tooltip_table_td_key">logLevel:</td>
                    <td class="tooltip_table_td_value">{{ row.logLevel }}</td>
                  </tr>
                  <tr>
                    <td class="tooltip_table_td_key">appName:</td>
                    <td class="tooltip_table_td_value">{{ row.appName }}</td>
                  </tr>
                  <tr v-if="row.env">
                    <td class="tooltip_table_td_key">env:</td>
                    <td class="tooltip_table_td_value">{{ row.env }}</td>
                  </tr>
                  <tr>
                    <td class="tooltip_table_td_key">serverName:</td>
                    <td class="tooltip_table_td_value">{{ row.serverName }}</td>
                  </tr>
                  <tr v-if="row.threadName">
                    <td class="tooltip_table_td_key">threadName:</td>
                    <td class="tooltip_table_td_value">{{ row.threadName }}</td>
                  </tr>
                  <tr>
                    <td class="tooltip_table_td_key">traceId:</td>
                    <td class="tooltip_table_td_value">{{ row.traceId }}</td>
                  </tr>
                  <tr>
                    <td class="tooltip_table_td_key">className:</td>
                    <td class="tooltip_table_td_value">{{ row.className + "." + row.method }}</td>
                  </tr>
                  </tbody>
                </table>
              </div>
              <span :class="row.logLevel">
                  <span :title="'日志时间: ' + dateFormat(row.dtTime)">{{ dateFormat(row.dtTime) }}</span>
                  <span class="row_underline row_search"
                        :title="'点击查询日志等级: ' + row.logLevel">{{ row.logLevel }}</span>
                  <span   class="row_app_name row_underline row_search"
                        @click="doSearch('appName',row)" :title="'点击查询应用名称: ' + row.appName">{{ row.appName }}</span>
                  <span   class="row_env row_underline row_search"
                         :title="'点击查询应用环境: ' + row.env">{{ row.env }}</span>
                  <span   class="row_server_name row_underline row_search"
                        :title="'点击查询服务器名称: ' + row.serverName">{{ row.serverName }}</span>
                  <span v-if="row.traceId" class="row_trace_id row_underline row_search"
                         :title="'点击查询TraceId: ' + row.traceId">{{ row.traceId }}</span>
                  <span  class="row_class_name row_underline row_search"
                        :title="'点击查询类名: ' + row.className">{{ row.className + "." + row.method }}</span>
                  <span>: {{ row.content }}<a class="row_pick_up_text">[点击收起]</a></span>
                </span>
            </Tooltip>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
import axios from '@/services/http'
import moment from 'moment'
import '@/assets/prism.js'
import '@/assets/prism.css'
import 'view-design/dist/styles/iview.css';
import logHeader from '@/components/logHeader.vue'
import "@/assets/less/base.less";
import $ from 'jquery'
import expandRow from '@/components/table-expand.vue';

let isHtml = /<\/?[a-z][\s\S]*>/i;
export default {
  name: "Home",
  data() {
    return {
      rowLen: 300,
      appNameComplete: [],
      appName: '',
      list: [],
      messageList: [],
      ws: null,
      allAppNames:[],
      appNameWithEnvMap:{},
      envWithAppNameMap:{},
      allEnvs:[],
      stop:false,
      inited: false
    }
  },
  components: {
    // HelloWorld
    logHeader,
    expandRow
  },
  destroyed  () {
    console.log("destory")
  },
  methods: {
    completeFilter(value, option) {
      return option.indexOf(value) === 0;
    },
    dateFormat(time) {
      return moment(parseInt(time)).format('YYYY-MM-DD HH:mm:ss.SSS');
    },

    doSearch() {
      this.ws.send(this.appName)
    },
    appNameChange() {

    },
    searchAppName() {
      if (this.appNameComplete.length === 0) {

        // 为了防止客户端长时间未关闭造成的sessionStorage缓存不失效的问题加一个sessionStorage缓存半小时失效一次的判断
        if (sessionStorage['cache_appNameWithEnvs'] && sessionStorage['cache_appNameWithEnvs_time']
            && sessionStorage['cache_appNameWithEnvs_time'] > new Date().getTime() - 1800000) {
          this.analysisAppNameWithEnv();
        } else {
          axios.post(process.env.VUE_APP_API + '/queryAppNames?appNameWithEnv', {
            "size": 0,
            "aggregations": {
              "dataCount": {
                "terms": {
                  "size": 1000,
                  "field": "appNameWithEnv"
                }
              }
            }
          }).then(data => {
            sessionStorage['cache_appNameWithEnvs'] = JSON.stringify(data.data);
            sessionStorage['cache_appNameWithEnvs_time'] = new Date().getTime();
            this.analysisAppNameWithEnv();
          })
        }
      }
    },
    analysisAppNameWithEnv() {
      let appNameWithEnvs = JSON.parse(sessionStorage['cache_appNameWithEnvs']);
      this.appNameWithEnvMap = {};
      this.envWithAppNameMap = {};
      for (let i = 0,len = appNameWithEnvs.length; i < len; i++) {
        let splitStr = appNameWithEnvs[i].split('-_-');
        if (!this.appNameWithEnvMap[splitStr[0]]) {
          this.appNameWithEnvMap[splitStr[0]] = [];
        }
        if (splitStr.length > 1 && splitStr[1] !== '') {
          this.appNameWithEnvMap[splitStr[0]].push(splitStr[1]);
          if (!this.envWithAppNameMap[splitStr[1]]) {
            this.envWithAppNameMap[splitStr[1]] = [];
          }
          this.envWithAppNameMap[splitStr[1]].push(splitStr[0]);
        }
      }
      this.allAppNames = [];
      for (let appName in this.appNameWithEnvMap) {
        if (this.appNameWithEnvMap.hasOwnProperty(appName)) {
          this.allAppNames.push(appName);
        }
      }
      this.allAppNames.sort(function(a, b){return a.localeCompare(b)});
      this.appNameComplete = this.allAppNames;
      this.allEnvs = [];
      for (let env in this.envWithAppNameMap) {
        if (this.envWithAppNameMap.hasOwnProperty(env)) {
          this.allEnvs.push(env);
        }
      }
      this.allEnvs.sort(function(a, b){return a.localeCompare(b)});
      this.envComplete = this.allEnvs;
    },
    init() {
      this.searchAppName()
      const rowDiv = document.getElementById('plume-console');

      if ('WebSocket' in window) {
        let url = window.location.host;
        // url = 'localhost:8891'
        const ws = new WebSocket(`ws:${url}/plumelog/websocket`)
        ws.onerror = (e) => {
          this.list.push({dtTime: new Date().getTime(),content: `链接异常 ${JSON.stringify(e)}`, logLevel:'ERROR', appName: '', className:'', method:''})
        }
        // 连接成功的回调方法
        ws.onopen = () =>  {
          this.list.push({dtTime: new Date().getTime(),content: '链接成功', logLevel:'INFO', appName: '', className:'',method:''})
        }
        // 收到消息的回调方法
        ws.onmessage = (ev) =>  {
          if (this.list.length > this.rowLen) {
            this.list = this.list.slice(this.list.length - 20)
          }
          this.messageList.push(JSON.parse(ev.data))
          this.$nextTick(() => {
            if (this.messageList.length > 10) {
              this.list.push(...this.messageList)
              this.messageList = []
            } else {
              setTimeout(() => {
                if (this.messageList.length < 10) {
                  this.list.push(...this.messageList)
                  this.messageList = []
                }
              },1000);
            }

            !this.stop && ( rowDiv.scrollTop = rowDiv.scrollHeight)
          })
        }
        // 连接关闭的回调方法
        ws.onclose = () =>  {
          this.list.push({dtTime: new Date().getTime(),content: '链接关闭', logLevel:'INFO', appName: '', className:'',method:''})
        }
        this.ws = ws;
      } else {
        alert("浏览器不支持");
      }
    },
    logModelNormalRowMouseEnter($event) {
      this.stop = true
      $event.currentTarget.className = 'log_model_normal_row_enter';
    },
    logModelNormalRowMouseLeave($event) {
      this.stop = false
      $event.currentTarget.className = 'log_model_normal_row';
    },
    queryFilterCheck(localFilter, key, value) {
      if (value !== '') {
        localFilter[key] = value;
      } else {
        localFilter[key] && (delete localFilter[key]);
      }
    },
  },
  watch: {
    "appName": {
      handler() {
        this.doSearch();
      },
      deep: true
    },
    "dateTimeRange": {
      handler() {
        let localFilter = {...this.$route.query};
        if (this.dateTimeRange.length > 0) {
          if (this.dateTimeRange[0] !== '' && this.dateTimeRange[1] !== '') {
            let startTime = moment(this.dateTimeRange[0]).valueOf();
            let endTime = moment(this.dateTimeRange[1]).valueOf();
            this.queryFilterCheck(localFilter, 'time', [startTime, endTime].join());
          } else if (this.dateTimeRange[0] !== '') {
            let startTime = moment(this.dateTimeRange[0]).valueOf();
            this.queryFilterCheck(localFilter, 'time', [startTime, ''].join());
          } else if (this.dateTimeRange[1] !== '') {
            let endTime = moment(this.dateTimeRange[1]).valueOf();
            this.queryFilterCheck(localFilter, 'time', ['', endTime].join());
          } else {
            this.queryFilterCheck(localFilter, 'time', '');
          }
        } else {
          this.queryFilterCheck(localFilter, 'time', '');
        }
        this.$router.push({
          query: {...localFilter}
        }).catch(err => err)
      },
      deep: true
    },
    "searchKey": {
      handler() {
        let localFilter = {...this.$route.query};
        this.queryFilterCheck(localFilter, 'searchKey', this.searchKey);
        this.$router.push({
          query: {...localFilter}
        }).catch(err => err);
        this.from = 0;
      }
    },
    '$route.path': function (newVal, oldVal) {
      if (newVal === '/' && oldVal === '/login')
        this.init();
      else if (newVal === '/' && oldVal === '/warn')
        this.init()
    },
    'list': function () {
      this.$nextTick(function () {
        if (!this.tableModel) {
          $('.log_model_normal_row_selector').each(function () {
            let rowSelector = $(this);
            if (rowSelector.find('.ivu-tooltip-rel')[0].offsetHeight > 72) {
              let rowSelectorClickFunc = function (e) {
                if (e.target.nodeName !== 'A' && e.target.className.indexOf('row_search') < 0) {
                  e.preventDefault();
                  if (rowSelector.hasClass('row_over_flow_hidden')) {
                    rowSelector.removeClass('row_over_flow_hidden can_click').removeAttr('title').off("click");
                  } else {
                    rowSelector.addClass('row_over_flow_hidden can_click').attr('title', '点击显示完整信息').on("click", function (e) {
                      rowSelectorClickFunc(e)
                    });
                  }
                }
              };
              rowSelector.addClass('can_click').attr('title', '点击显示完整信息').on("click", function (e) {
                rowSelectorClickFunc(e)
              });
              $(rowSelector.find('.row_pick_up_text')[0]).show().on("click", function (e) {
                e.preventDefault();
                if (!rowSelector.hasClass('row_over_flow_hidden')) {
                  let rowSelectorTop = rowSelector[0].offsetTop + document.getElementsByClassName('pnl_content')[0].offsetTop;
                  if (rowSelectorTop < (document.documentElement.scrollTop || document.body.scrollTop)) {
                    document.documentElement.scrollTop = document.body.scrollTop = rowSelectorTop;
                  }
                  rowSelector.addClass('row_over_flow_hidden can_click').attr('title', '点击显示完整信息').on("click", function (e) {
                    rowSelectorClickFunc(e)
                  });
                }
              });
            } else {
              $(rowSelector.find('.row_pick_up_text')[0]).hide()
            }
          });
        }
      })
    }
  },
  deactivated() {
    this.ws && this.ws.close()
    this.list = []
    this.messageList = []
  },
  activated() {
    this.init()
  },
  // mounted() {
  //   this.init()
  // }
};
</script>
<style lang="less">
.ivu-table {
  line-height: 14px;
}

.ivu-table-wrapper {
  overflow: unset;
}

.tip_table {
  position: absolute;
  top: -30px;
  left: 10px;
  color: #aaa;
  font-size: 12px;

}

.ivu-table-row-highlight td, .ivu-table-row-hover td {
  background-color: #ebf7ff !important;
}

.ivu-table-row:nth-child(2n) td {
  background-color: #f8f8f9
}

.ivu-table-small td {
  height: 30px;
}

.ivu-table {
  font-size: 12px;
  overflow: unset;

  tr.WARN {
    td {
      background: #fff1d7;
    }
  }

  tr.ERROR {
    td {
      background: #ffeae5;
    }
  }

  .ivu-table-cell {
    position: relative;
  }

  .ivu-table-header {
    position: sticky !important;
    top: -1px;
    z-index: 10;
  }

  td {
    padding: 3px 0;
    position: relative;

    &.ivu-table-expanded-cell {
      padding: 0;
    }

    &.content {
      word-break: break-all;
    }

    &.icon {
      &:hover {
        i {
          display: inline;
        }
      }

      i {
        cursor: pointer;
        position: absolute;
        top: 0px;
        right: 6px;
        font-size: 14px;
        display: none;
      }
    }

    em {
      background: #ff0;
    }
  }

  .detail_table {
    width: 100%;
    margin: 10px auto 30px auto;

    .key {
      width: 150px;
      text-align: right;
      padding-right: 20px;
      vertical-align: top;
      font-weight: 700;

      div {
        width: 150px;
      }
    }

    .value {
      text-align: left;
      vertical-align: top;
    }

    tr {
      background: none !important;

      td {
        padding-top: 10px;
        border: none;
        height: auto;
      }
    }
  }
}

.ivu-tooltip {
  width: 100%;
}

.ivu-tooltip-inner {
  max-width: fit-content;
}
</style>
<style lang="less" src="../assets/less/filters.less" scoped></style>
<style lang="less" scoped>
.pnl_content {
  position: relative;
  max-height: calc(100vh - 180px);
  overflow: scroll;
}

.ivu-carousel {
  position: absolute;
  top: 20px;
  right: 10px;
  width: calc(100% - 1000px);
  min-width: 300px;
  height: 300px;
}

.chart {
  position: relative;
  top: 38px;
  padding-left: 20px;
  left: 0;
  width: 100%;
  height: 280px;
}

.icon_arrow {
  cursor: pointer;
  position: absolute;
  font-size: 20px;
  top: -50px;
  left: 50%;
  transform: translateX(-50%);
  width: 100px;
  height: 50px;
  z-index: 10;

  &.down {
    top: 10px;
  }

  .text {
    font-size: 14px;
  }
}

.breadcrumb {
  position: relative;
  padding: 20px 10px 20px 50px;
}

.pnl_search {
  position: absolute;
  top: 10px;
  left: 300px;
  width: 600px;
}

.link_trans {
  color: #1313bd;
  text-decoration: underline;
  cursor: pointer;
}

.page-count {
  padding: .5rem 1rem;
}

.log_model_normal {
  text-align: left;
  font-size: 12px;
  font-family: "Roboto Mono", Consolas, Menlo, Courier, monospace, Avenir, Helvetica, Arial, sans-serif;

  .log_model_normal_row_selector {
    padding: 6px 10px;
    line-height: 20px;
  }

  .row_over_flow_hidden {
    max-height: 68px;
    overflow: hidden;
  }

  .row_pick_up_text {
    float: right;
    cursor: pointer;
    position: relative;
    display: none;
  }

  .row_underline {
    cursor: pointer;
    position: relative;
    margin-left: 10px;
  }

  .row_underline::after, .row_pick_up_text::after {
    position: absolute;
    left: 0;
    top: 100%;
    content: '';
    background-color: aqua;
    width: 100%;
    transform: scale(0);
    -webkit-transform: scale(0);
    transition: all .5s;
    -webkit-transition: all .5s;
  }

  .row_underline:hover::after, .row_pick_up_text:hover::after {
    height: 1px;
    -webkit-transform: scale(1);
    transform: scale(1);
  }

  .row_date_time {
    margin-left: 10px;
  }
}

.auto_word_wrap {
  white-space: pre-wrap;
  word-break: break-all;
}

.word_inline {
  white-space: pre;
  word-break: break-all;
  width: fit-content;
}

.bright_mode {

  .log_model_normal_row {
    background-color: #ffffff;

    /*.row_app_name {
      color: #f7c4c4;
    }
    .row_env {
      color: #d89bd0;
    }
    .row_server_name {
      color: #ffffff;
    }
    .row_trace_id {
      color: #ffffff;
    }
    .row_class_name {
      color: #ffffff;
    }*/

    .DEBUG {
      color: rgba(0, 0, 0, .4);
    }

    .INFO {
      color: #13750B;
    }

    .WARN {
      color: #878A03;
    }

    .ERROR {
      color: #FB0014;
    }

    .row_pick_up_text {
      color: #d6d6d6;
    }

    .row_pick_up_text::after {
      background-color: #d6d6d6;
    }
  }

  .log_model_normal_row_enter {
    background-color: #ffe8e5;

    .row_app_name {
      color: #404dff;
    }

    .row_env {
      color: #4f8494;
    }

    .row_server_name {
      color: #717933;
    }

    .row_trace_id {
      color: #ff00af;
    }

    .row_class_name {
      color: #1F8483;
    }

    .DEBUG {
      color: #888888;
    }

    .INFO {
      color: #1c8400;
    }

    .WARN {
      color: #926e00;
    }

    .ERROR {
      color: #f71111;
    }

    .row_pick_up_text {
      color: #7e7ecc;
    }

    .row_pick_up_text::after {
      background-color: #7e7ecc;
    }
  }
}

.dark_mode {
  background-color: #2b2b2b;
  .log_model_normal_row {
    background-color: #2b2b2b;

    /*.row_app_name {
      color: #f7c4c4;
    }
    .row_env {
      color: #d89bd0;
    }
    .row_server_name {
      color: #a7b962;
    }
    .row_trace_id {
      color: #8b8cc3;
    }
    .row_class_name {
      color: #1F8483;
    }*/

    .DEBUG {
      color: #7a7a7a;
    }

    .INFO {
      color: #92c584;
    }

    .WARN {
      color: #e5e431;
    }

    .ERROR {
      color: #fc5759;
    }

    .row_pick_up_text {
      color: #484848;
    }

    .row_pick_up_text::after {
      background-color: #484848;
    }
  }

  .log_model_normal_row_enter {
    background-color: #1a1a1a;

    .row_app_name {
      color: #ff9a9a;
    }

    .row_env {
      color: #ff6aeb;
    }

    .row_server_name {
      color: #d4f555;
    }

    .row_trace_id {
      color: #8789f7;
    }

    .row_class_name {
      color: #1F8483;
    }

    .DEBUG {
      color: #8e8e8e;
    }

    .INFO {
      color: #4de03c;
    }

    .WARN {
      color: #e2ae0d;
    }

    .ERROR {
      color: #ff5353;
    }

    .row_pick_up_text {
      color: #9ea5ff;
    }

    .row_pick_up_text::after {
      background-color: #9ea5ff;
    }
  }
}

.tooltip_table_td_key {
  font-size: 12px;
}

.tooltip_table_td_value {
  font-size: 12px;
  padding-left: 6px;
}

.can_click {
  cursor: pointer;
}
</style>
