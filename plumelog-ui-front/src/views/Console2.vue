<template>
  <div class="pnl_wraper" style="padding-bottom: 0">
    <div class="pnl_filters">
      <template>
        <log-header></log-header>
        <!--查询条件-->
        <div style="width: 1300px">
          <table class='tbl_filters' style="width: 100%">
            <tbody>
            <tr>
              <td class="key">应用名称</td>
              <td style="width: 245px">
                <AutoComplete
                    v-model="filter.appName"
                    :data="appNameComplete"
                    class="txt"
                    placeholder="应用名称，可搜索"
                    :clearable="true"
                    :filter-method="completeFilter"
                    @on-change="appNameChange">
                </AutoComplete>
              </td>
              <td class="key">应用环境</td>
              <td style="width: 245px">
                <AutoComplete
                    v-model="filter.env"
                    :data="envComplete"
                    class="txt"
                    placeholder="项目的环境"
                    :clearable="true"
                    :filter-method="completeFilter">
                </AutoComplete>
              </td>
              <td class="key">日志等级</td>
              <td style="width: 180px">
                <Select v-model="filter.logLevel"  placeholder="请选择日志等级" style="width: 162px">
                  <Option value="" key="ALL">所有</Option>
                  <Option value="INFO" key="INFO">INFO</Option>
                  <Option value="ERROR" key="ERROR">ERROR</Option>
                  <Option value="WARN" key="WARN">WARN</Option>
                  <Option value="DEBUG" key="DEBUG">DEBUG</Option>
                </Select>
              </td>
              <td class="key">服务器</td>
              <td style="width: 180px">
                <Input class="txt" style="width: 162px" name="serverName" v-model="filter.serverName" placeholder="服务器Ip或者host"
                       :clearable="true"/>
              </td>
              <td style="width: 180px">
<!--                :disabled="!filter.appName"-->
                <Button  @click="connect" type="primary" style="margin-left:10px;">连接</Button>
                <Button @click="closeHandler" style="margin-left:10px">停止</Button>
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </template>
      <div style="clear:both"></div>
    </div>
    <div class="pnl_content" id="plume-console">
      <div style="width: 100%; height: 100%" ref="terminal" id="terminal-container"></div>
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
import { Terminal } from "xterm";
import "xterm/css/xterm.css";
import "xterm/lib/xterm.js";
import { FitAddon } from 'xterm-addon-fit';
let isHtml = /<\/?[a-z][\s\S]*>/i;
export default {
  name: "Home",
  data() {
    return {
      rowLen: 300,
      appNameComplete: [],
      filter: {
        appName:'',
        env:'',
        logLevel:''
      },
      list: [],
      messageList: [],
      ws: null,
      allAppNames:[],
      appNameWithEnvMap:{},
      envWithAppNameMap:{},
      allEnvs:[],
      envComplete:[],
      stop:false,
      inited: false,
      shellWs: "",
      term: "", // 保存terminal实例
      rows: 40,
      cols: 100
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
      if (!this.term) {
        let _this = this;
        // 获取容器宽高/字号大小，定义行数和列数
        var content = document.querySelector(".pnl_content");
        this.rows = document.querySelector(".pnl_content").clientHeight / 16 - 6;
        this.cols = document.querySelector(".pnl_content").clientWidth / 14;
        let term = new Terminal({
          fontSize: 14,
          rendererType: "canvas", //渲染类型
          rows: parseInt(_this.rows), //行数
          cols: parseInt(_this.cols), // 不指定行数，自动回车后光标从下一行开始
          convertEol: true, //启用时，光标将设置为下一行的开头
          //   scrollback: 50, //终端中的回滚量
          disableStdin: false, //是否应禁用输入。
          cursorStyle: "underline", //光标样式
          cursorBlink: true, //光标闪烁
          theme: {
            foreground: "#7e9192", //字体
            background: "#002833", //背景色
            cursor: "help", //设置光标
            lineHeight: 16
          }
        });

        const fitAddon = new FitAddon();
        term.loadAddon(fitAddon);
        term.open(this.$refs["terminal"]);
        fitAddon.fit();

        _this.term = term;
      }

    },
    closeHandler() {
      this.ws && this.ws.close()
      this.ws = null
    },
    connect() {

      if (this.ws) {
        this.ws.send(JSON.stringify(this.filter))
        return
      }

      if ('WebSocket' in window) {

      	var hostName=window.document.location.host;
      	var pathName=window.document.location.pathname;
      	var projectName=pathName.substring(0,pathName.substr(1).indexOf('/')+1);
        let host=hostName;

        if(projectName!='/plumelog'){
      	host =hostName+projectName;
         }

        let ishttps = 'https:' == document.location.protocol ? true : false;
        let url='ws://'+host+'/plumelog/websocket';
         if(ishttps){
           url='wss://'+host+'/plumelog/websocket';
         }
        const ws = new WebSocket(url);
        ws.onerror = (e) => {
          this.term.writeln(`${color['ERROR']} ${this.dateFormat(new Date().getTime())}  链接异常`)
        }
        // 连接成功的回调方法
        ws.onopen = () =>  {
          this.term.writeln(`${this.dateFormat(new Date().getTime())} 链接成功`)
          this.ws.send(JSON.stringify(this.filter))
        }
        const color = {
          'ERROR' : '\x1b[1;31m',
          'INFO' : '\x1b[1;37m',
          "WARN" : '\x1b[1;33m',
          "DEBUG" : '\x1b[2;37m',
        }
        const colorBo = {
          'ERROR' : '1',
          'INFO' : '1',
          "WARN" : '1',
          "DEBUG" : '2',
        }
        const level = {
          'ERROR' : '[ERROR]',
          'INFO' : '[INFO ]',
          "WARN" : '[WARN ]',
          "DEBUG" : '[DEBUG]',
        }
        // 收到消息的回调方法
        ws.onmessage = (ev) =>  {
          const data = JSON.parse(ev.data);
          this.term.writeln(`\x1b[0m\x1b[${colorBo[data.logLevel]};37m${this.dateFormat(data.dtTime)} ${color[data.logLevel]}${level[data.logLevel]} \x1b[0m\x1b[${colorBo[data.logLevel]};34m${data.appName} \x1b[0m\x1b[${colorBo[data.logLevel]};33m${data.serverName} \x1b[0m\x1b[${colorBo[data.logLevel]};32m${data.className}.${data.method} \x1b[0m${color[data.logLevel]}${data.content}`)
        }
        // 连接关闭的回调方法
        ws.onclose = () =>  {
          this.term.writeln(`${this.dateFormat(new Date().getTime())} 链接关闭`)
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
    this.ws = null
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

#terminal-container .terminal.xterm {
  height: 100%;
}
#terminal-container .xterm-viewport {
  height: 100% !important;
}
</style>
<style lang="less" src="../assets/less/filters.less" scoped></style>
<style lang="less" scoped>
.pnl_wraper {
  padding-bottom: 0!important;
}
.pnl_content {
  position: relative;
  height: calc(100vh - 140px);
  width: 100%;
  overflow: auto;
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
