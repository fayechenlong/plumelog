<template>
  <div class="pnl_wraper">
    <div class="icon_arrow down" v-if="!showFilter" @click="setShowFilter">
      <Icon type="ios-arrow-down" v-show="!showFilter"/>
      <span class="text">展开</span>
    </div>
    <div class="pnl_filters">
      <template v-if="showFilter">
        <div class="alert alert-danger" v-if="danger_str" role="alert">
          {{ danger_str }}
        </div>
        <log-header></log-header>

        <!--查询条件-->
        <div style="width: 1000px">
          <table class='tbl_filters' style="width: 100%">
            <tbody>
            <tr>
              <td class="key">应用名称</td>
              <td>
                <AutoComplete
                    v-model="filter.appName"
                    :data="appNameComplete"
                    class="txt"
                    placeholder="搜索多个请用逗号或空格隔开"
                    :clearable="true"
                    :filter-method="completeFilter"
                    @on-change="appNameChange">
                </AutoComplete>
              </td>
              <td class="key">应用环境</td>
              <td>
                <AutoComplete
                    v-model="filter.env"
                    :data="envComplete"
                    class="txt"
                    placeholder="项目的环境"
                    :clearable="true"
                    :filter-method="completeFilter"
                    @on-change="envChange">
                </AutoComplete>
              </td>
              <td class="key">日志等级</td>
              <td >
                <Select v-model="filter.logLevel"  placeholder="请选择日志等级" style="width: 162px">
                  <Option value="" key="ALL">所有</Option>
                  <Option value="INFO" key="INFO">INFO</Option>
                  <Option value="ERROR" key="ERROR">ERROR</Option>
                  <Option value="WARN" key="WARN">WARN</Option>
                  <Option value="DEBUG" key="DEBUG">DEBUG</Option>
                </Select>
              </td>
            </tr>
            </tbody>
          </table>

          <table class='tbl_filters' style="width: 100%">
            <tbody>
            <tr>
              <td class="key">追踪码</td>
              <td>
                <Input class="txt" name="traceId" v-model="filter.traceId" placeholder="TraceId" :clearable="true"/>
              </td>
              <td class="key">类名</td>
              <td>
                <Input class="txt" name="className" v-model="filter.className" placeholder="类名的全路径"
                       :clearable="true"/>
              </td>
              <td class="key">服务器名称</td>
              <td>
                <Input class="txt" style="width: 162px" name="serverName" v-model="filter.serverName" placeholder="服务器Ip或者host"
                       :clearable="true"/>
              </td>
            </tr>
            </tbody>
          </table>
          <table class='tbl_filters' style="width: 100%">
            <tbody>
            <tr>
              <td class="key">日期和时间</td>
              <td>
                <DatePicker ref='datePicker' v-model="dateTimeRange" @on-change="dateChange" type="datetimerange"
                            :options="dateOption" format="yyyy-MM-dd HH:mm:ss" placeholder="选择日期与时间"
                            style="width: 315px"></DatePicker>
              </td>
            </tr>
            </tbody>
          </table>

          <div style="clear:both"></div>
          <table class="tbl_filters" style="width:1000px">
            <tr v-if="extendList.length>0">
              <td class="key">扩展字段</td>
              <td>
                <Select v-model="select_extend" @on-change="selectExtendHandler" label-in-value placeholder="选择扩展字段" style="width:150px;margin-right:10px">
                  <Option v-for="extend in extendList" :value="extend.field" :key="extend.field">{{ extend.fieldName }}</Option>
                </Select>
                <Input class="txt" @on-enter="addExtendTag" :clearable="true" v-model="extendTag" placeholder="输入查询内容" style="width:478px;"/>
                <Button icon="md-add" @click="addExtendTag" style="margin-left:10px;width:100px">添加</Button>
              </td>
            </tr>
            <tr v-if="extendOptions.length>0">
              <td></td>
              <td>
                <Tag closable v-for="(tag,index) in extendOptions" size="medium" @on-close="closeExtendTag(index)"
                     :key="index">
                  <template v-if="index>0">{{ tag.type }}&nbsp;</template>
                  {{ tag.label }}:{{ tag.tag }}
                </Tag>
              </td>
            </tr>
            <tr v-if="!useSearchQuery">
              <td class="key">内容</td>
              <td>
                <Input @on-enter="doSearch()" :clearable="true" style="width:720px" placeholder="输入搜索内容"
                       v-model="searchKey"/>
                <a href="javascript:void(0)" @click="useSearchQuery=true" class="link_changeModal">切换为条件模式</a>
              </td>
            </tr>
            <tr v-if="useSearchQuery">
              <td class="key">条件</td>
              <td>
                <Select v-if="searchOptions.length>0" v-model="selectOption" style="width:80px;margin-right:10px">
                  <Option value="AND" key="AND">AND</Option>
                  <Option value="OR" key="OR">OR</Option>
                  <Option value="NOT" key="NOT">NOT</Option>
                </Select>
                <Input class="txt" @on-enter="addTag()" :clearable="true" v-model="tag" placeholder="输入搜索条件"
                       style="width:224px;"/>
                <Button icon="md-add" @click="addTag" style="margin-left:10px">添加</Button>
                <a href="javascript:void(0)" @click="useSearchQuery=false" class="link_changeModal">切换为内容模式</a>
              </td>
            </tr>
            <tr v-if="useSearchQuery">
              <td></td>
              <td>
                <Tag closable v-for="(tag,index) in searchOptions" @on-close="closeTag(index)" :key="index">
                  <template v-if="index>0">{{ tag.type }}&nbsp;</template>
                  {{ tag.tag }}
                </Tag>
              </td>
            </tr>
            <tr>
              <td></td>
              <td style='padding-top:8px;position:relative'>
                <Button type="primary" icon="ios-search" style="margin-right:10px" @click="doSearch">查询</Button>
                <Button @click="clear">重置</Button>
              </td>
            </tr>
          </table>
        </div>
        <!-- 统计图表 -->
        <Carousel v-model="slideIndex" arrow="never">
          <CarouselItem>
            <div id="myChart" class="chart"></div>
          </CarouselItem>
          <CarouselItem>
            <div id="errorChart" class="chart"></div>
          </CarouselItem>
        </Carousel>
      </template>
      <div style="clear:both"></div>
    </div>
    <div class="pnl_content">
      <div class="icon_arrow up" v-if="showFilter" @click="setShowFilter">
        <Icon type="ios-arrow-up" v-show="showFilter"/>
        <span class="text">收起</span>
      </div>
      <div style="position:absolute;top:-30px;right:20px">共 <b>{{ totalCount }}</b> 条数据</div>
      <div v-if="tableModel">
        <div class="tip_table">
          <Icon size="14" type="md-star-outline"/>
          表格字段宽度可拖拽调节，双击或点击箭头可查看详情
        </div>
        <!--日志列表-->
        <Table size="small" border highlight-row :columns="showColumns" :content="self" @on-row-dblclick="dblclick"
               :row-class-name="getRowName" :data="list.hits">
          <template slot-scope="{ row }" slot="className">
            {{ row.className | substr }}
            <Icon type="ios-search" v-if="row.className" @click="doSearch('className',row)"/>
          </template>
          <template slot-scope="{ row }" slot="logLevel">
            {{ row.logLevel }}
            <Icon type="ios-search" v-if="row.logLevel" @click="doSearch('logLevel',row)"/>
          </template>
          <template slot-scope="{ row }" slot="serverName">
            {{ row.serverName }}
            <Icon type="ios-search" v-if="row.serverName" @click="doSearch('serverName',row)"/>
          </template>
          <template slot-scope="{ row }" slot="appName">
            {{ row.appName }}
            <Icon type="ios-search" v-if="row.appName" @click="doSearch('appName',row)"/>
          </template>
          <template slot-scope="{ row }" slot="env">
            {{ row.env }}
            <Icon type="ios-search" v-if="row.env" @click="doSearch('env',row)"/>
          </template>
          <template slot-scope="{ row }" slot="traceId">
            <a :href="'./#/trace?traceId='+row.traceId+'&timeRange='+JSON.stringify(dateTimeRange)"
               title="点击查看链路追踪">{{ row.traceId }}</a>
            <Icon type="ios-search" v-if="row.traceId" @click="doSearch('traceId',row)"/>
          </template>
          <template slot-scope="{ row }" slot="content">
            <div style="white-space: pre-wrap; max-height: 100px;">
              <span v-for="item in highLightCode(row.highlightCnt || row.content, !!row.highlightCnt)">
                <span v-if="item.isH" v-html="item.content"></span>
                <span v-else>{{ item.content }}</span>
              </span>
            </div>
          </template>
        </Table>
      </div>
      <div v-else :style="this.darkMode && !this.autoWordWrap ? 'padding:0 0 0 10px' : 'padding:0 10px'">
        <div class="log_model_normal" :class="[autoWordWrap ? 'auto_word_wrap' : 'word_inline', darkMode ? 'dark_mode' : 'bright_mode']">
          <div class="log_model_normal_row" @mouseenter="logModelNormalRowMouseEnter($event)"
               @mouseleave="logModelNormalRowMouseLeave($event)" v-for="row in list.hits">
            <div class="log_model_normal_row_selector row_over_flow_hidden">
              <Tooltip placement="top-start" delay="1500">
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
                  <span v-if="showTheColumn('logLevel')" class="row_underline row_search"
                        @click="doSearch('logLevel', row)" :title="'点击查询日志等级: ' + row.logLevel">{{ row.logLevel }}</span>
                  <span v-if="showTheColumn('appName')" class="row_app_name row_underline row_search"
                        @click="doSearch('appName',row)" :title="'点击查询应用名称: ' + row.appName">{{ row.appName }}</span>
                  <span v-if="row.env && showTheColumn('env')" class="row_env row_underline row_search"
                        @click="doSearch('env', row)" :title="'点击查询应用环境: ' + row.env">{{ row.env }}</span>
                  <span v-if="showTheColumn('serverName')" class="row_server_name row_underline row_search"
                        @click="doSearch('serverName',row)" :title="'点击查询服务器名称: ' + row.serverName">{{ row.serverName }}</span>
                  <span v-if="row.traceId && showTheColumn('traceId')" class="row_trace_id row_underline row_search"
                        @click="doSearch('traceId',row)" :title="'点击查询TraceId: ' + row.traceId">{{ row.traceId }}</span>
                  <span v-if="showTheColumn('className')" class="row_class_name row_underline row_search"
                        @click="doSearch('className', row)" :title="'点击查询类名: ' + row.className">{{ row.className + "." + row.method }}</span>
                  <span>: {{ row.content }}<a class="row_pick_up_text">[点击收起]</a></span>
                </span>
              </Tooltip>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 功能区 -->
    <nav class="page_nav" aria-label="Page navigation example">
      <div class="pnl_select">
        <span class="name">显示字段：</span>
        <Select v-model="showColumnTitles" multiple placeholder="选择要显示的字段" @on-change="columnsChange" :max-tag-count="2"
                style="width:270px">
          <Option v-for="item in allColumns" :value="item.value" :key="item.value">{{ item.label }}</Option>
        </Select>
      </div>

      <div class="pnl_select" style="margin-left:20px">
        <Button @click="tableModelChange" v-if="tableModel" style="font-size:12px">切换至普通模式</Button>
        <Button @click="tableModelChange" v-if="!tableModel" style="font-size:12px">切换至表格模式</Button>
      </div>

      <div class="pnl_select" style="margin-left:20px">
        <Button @click="autoWordWrapChange" v-if="!tableModel && autoWordWrap" style="font-size:12px">切换至单行模式</Button>
        <Button @click="autoWordWrapChange" v-if="!tableModel && !autoWordWrap" style="font-size:12px">切换至自动换行</Button>
      </div>

      <div class="pnl_select" style="margin-left:20px">
        <Button @click="darkModeChange" v-if="!tableModel && darkMode" style="font-size:12px">切换至日间模式</Button>
        <Button @click="darkModeChange" v-if="!tableModel && !darkMode" style="font-size:12px">切换至夜间换行</Button>
      </div>

      <ul v-if="totalCount && parseInt(totalCount/size) > 0" class="pagination justify-content-center"
          style="float:right;margin-right:30px">
        <li class="page-item" :class="{'disabled': !isShowLastPage }">
          <a class="page-link" href="javascript:void(0)" @click="prevPage" tabindex="-1">上一页</a>
        </li>
        <li class="page-item" :class="{'disabled': !haveNextPage }">
          <a class="page-link" href="javascript:void(0)" @click="nextPage">下一页</a>
        </li>
        <li class="page-item">
          <div class="page-count">每页 <Select @on-change="doSearch" v-model="size" size="small" style="width:100px">
            <Option v-for="item in sizeList" :value="item.value" :key="item.value">{{ item.label }}</Option>
          </Select> 条
          </div>
        </li>
        <li class="page-item">
          <div class="page-count">跳转至第
            <InputNumber style="width:80px" size="small" :min="1" :max="parseInt(totalCount/size)+1"
                         v-model="jumpPageIndex"/>
            页
            <Button @click="goPage" style="font-size:12px" size="small">确定</Button>
          </div>
        </li>
        <li class="page-item">
          <div class="page-count">第{{ parseInt(from / size) + 1 }}页 / 共{{ parseInt(totalCount / size) + 1 }}页</div>
        </li>
        <li class="page-item">
          <div class="page-count">
            <a @click="goDown" href="javascript:void(0)" style="margin-right:10px">去往底部</a>
            <a @click="goTop" href="javascript:void(0)">返回顶部</a>
          </div>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script>
import axios from '@/services/http'
import _ from 'lodash'
import moment from 'moment'
import '@/assets/prism.js'
import '@/assets/prism.css'
import 'view-design/dist/styles/iview.css';
import logHeader from '@/components/logHeader.vue'
import "@/assets/less/base.less";
import dateOption from './dateOption';
import $ from 'jquery'
import expandRow from '@/components/table-expand.vue';

let isHtml = /<\/?[a-z][\s\S]*>/i;
export default {
  name: "Home",
  data() {
    this.slotColumns = ['logLevel', 'serverName', 'appName', 'env', 'traceId', 'className'];
    return {
      sizeList: [{label: "30", value: 30}, {label: "50", value: 50}, {label: "100", value: 100}, {
        label: "200",
        value: 200
      }, {label: "500", value: 500}],
      isSearching: false,
      tag: "",
      extendTag: "",
      extendList: [],
      extendOptions: [],
      select_extend: "",
      select_extend_label: "",
      completeFilterLoading: false,
      appNameWithEnvMap: {},
      envWithAppNameMap: {},
      appNameComplete: [],
      envComplete: [],
      useSearchQuery: false,
      selectOption: 'AND',
      isExclude: false,
      slideIndex: 0,
      self: this,
      jumpPageIndex: 1,
      chartData: [],
      searchOptions: [],
      showColumnTitles: ['appName', 'traceId'],
      allColumns: [
        {
          label: '日志等级',
          value: 'logLevel',
          width: 90
        },
        {
          label: '应用名称',
          value: 'appName',
          width: 150
        },
        {
          label: '应用环境',
          value: 'env',
          width: 100
        },
        {
          label: '服务器名称',
          value: 'serverName',
          width: 150
        },
        {
          label: '类名',
          value: 'className',
          width: 270
        },
        {
          label: '追踪码',
          value: 'traceId',
          width: 170,
        }
      ],
      tableModel: false,
      autoWordWrap: true,
      darkMode: true,
      showFilter: true,
      api: process.env.api,
      dateOption,

      dateTimeRange: [moment(new Date().getTime() - 3600000).format('YYYY-MM-DD HH:mm:ss'), moment(new Date()).format('YYYY-MM-DD 23:59:59')],
      content: {
        _source: {}
      },
      searchKey: '',
      danger_str: '',
      filter: {
        "logLevel": '',
        "appName": "",
        "env": "",
        "traceId": "",
        "className": "",
        "serverName": ""
      },
      list: {
        hits: []
      },
      size: 100,
      from: 0,
      columns: [
        {
          type: 'expand',
          width: 50,
          render: (h, params) => {
            return h(expandRow, {
              props: {
                row: params.row,
                searchKey: this.searchKey
              }
            })
          }
        },
        {
          title: '时间',
          key: 'dtTime',
          sortable: true,
          width: 180,
          resizable: true,
          render: (h, params) => {
            return h('div', moment(parseInt(params.row.dtTime)).format('YYYY-MM-DD HH:mm:ss.SSS'))
          }
        },
        {
          title: '日志等级',
          key: 'logLevel',
          align: 'center',
          slot: 'logLevel',
          className: 'icon',
          resizable: true,
          sortable: true,
          width: 90
        },
        {
          title: '服务器名称',
          align: 'center',
          key: 'serverName',
          slot: 'serverName',
          className: 'icon',
          sortable: true,
          resizable: true,
          width: 150
        },
        {
          title: '应用名称',
          align: 'center',
          key: 'appName',
          slot: 'appName',
          className: 'icon',
          sortable: true,
          resizable: true,
          width: 150
        },
        {
          title: '应用环境',
          align: 'center',
          key: 'env',
          slot: 'env',
          className: 'icon',
          sortable: true,
          resizable: true,
          width: 100
        },
        {
          title: '追踪码',
          align: 'center',
          key: 'traceId',
          width: 170,
          className: 'icon',
          sortable: true,
          resizable: true,
          slot: 'traceId',
        },
        {
          title: '类名',
          align: 'center',
          key: 'className',
          slot: 'className',
          className: 'icon',
          sortable: true,
          resizable: true,
          width: 270
        },
        {
          title: '内容',
          align: 'left',
          key: 'content',
          slot: 'content',
        }
      ],
      sort: [{
        "dtTime": "desc"
      }]
    }
  },
  components: {
    // HelloWorld
    logHeader,
    expandRow
  },
  filters: {
    substr(str) {
      if (str.length > 30) {
        return str.substring(0, 30) + '...';
      }
      return str;
    },
    filterTime(date) {
      return moment(date).format('YYYY-MM-DD HH:mm:ss')
    }
  },
  computed: {
    searchQuery() {
      let query = "";
      for (let i = 0; i < this.searchOptions.length; i++) {
        let item = this.searchOptions[i];
        if (i > 0) {
          query += " " + item.type + " ";
        }
        query += '"' + item.tag + '"';
      }
      return query;
    },
    showColumns() {
      let columns = [this.columns[0], this.columns[1]];
      for (let title of this.showColumnTitles) {
        let _c = _.find(this.columns, ['key', title]);
        if (_c) {
          columns.push(_c)
        } else {
          //从allColmns里获取label和value
          let item = _.find(this.allColumns, (o) => {
            return o.value === title
          });
          if (item) {

            let col = {
              title: item.label,
              align: 'center',
              resizable: true,
              key: item.value,
              className: 'icon',

            };
            if(this.slotColumns.includes(item.value)) {
              col.slot = item.value
            }
            if (item.width) {
              col.width = item.width
            } else {
              col.width = 200
            }
            columns.push(col)
          }
        }
      }
      columns.push(_.find(this.columns, ['key', 'content']));
      this.columns = columns; //让新加入的拓展字段，可以被vue管理
      return columns;
    },
    chartInterval() {
      if (this.dateTimeRange.length > 0) {
        let start = new Date(this.dateTimeRange[0]);
        let end = new Date(this.dateTimeRange[1]);
        let now = new Date();
        if(end > now) {
          end = now
        }

        let _range = (end.getTime() - start.getTime());
        //大于7天按照每天数据统计
        if (_range > 1000 * 60 * 60 * 24 * 7) {
          return {
            format: 'MM-DD',
            value: 1000 * 60 * 60 * 24
          }
        }
        //大于3天按照12小时进行统计
        else if (_range > 1000 * 60 * 60 * 24 * 3) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60 * 60 * 12
          }
        }
        //大于1天按照3小时进行统计
        else if (_range > 1000 * 60 * 60 * 24) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60 * 60
          }
        } else if (_range >= 1000 * 60 * 60 * 12) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60 * 60
          }
        } else if (_range >= 1000 * 60 * 60 * 6) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60 * 15
          }
        } else if (_range >= 1000 * 60 * 60) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60
          }
        } else if (_range >= 1000 * 60 * 30) {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60
          }
        } else if (_range >= 1000 * 60 * 15) {
          return {
            format: 'MM-DD HH:mm:ss',
            value: 1000 * 30
          }
        } else {
          return {
            format: 'MM-DD HH:mm',
            value: 1000 * 60 * 60
          }
        }
      }
      return {
        format: 'MM-DD HH:mm',
        value: 1000 * 60 * 60
      }
    },
    totalCount() {
      return _.get(this.list, 'total.value', _.get(this.list, 'total', 0))
    },
    isShowLastPage() {
      return this.from > 0
    },
    haveNextPage() {
      return this.totalCount > (this.from + this.size);
    }
  },
  methods: {
    dateFormat(time) {
      return moment(parseInt(time)).format('YYYY-MM-DD HH:mm:ss.SSS');
    },
    tableModelChange() {
      this.tableModel = !this.tableModel;
      this.localStorageChange('tableModel', this.tableModel);
    },
    autoWordWrapChange() {
      this.autoWordWrap = !this.autoWordWrap;
      this.localStorageChange('autoWordWrap', this.autoWordWrap);
      // 由于单行模式下字符串超出当前屏幕宽度，因此将屏幕外的html也修改背景颜色以保证风格统一
      if (this.darkMode && !this.autoWordWrap) {
        document.querySelector('html').style.cssText = `background: #2b2b2b;`;
      } else {
        document.querySelector('html').style.cssText = `background: #ffffff;`;
      }
    },
    darkModeChange() {
      this.darkMode = !this.darkMode;
      this.localStorageChange('darkMode', this.darkMode);
      // 由于单行模式下字符串超出当前屏幕宽度，因此将屏幕外的html也修改背景颜色以保证风格统一
      if (this.darkMode && !this.autoWordWrap) {
        document.querySelector('html').style.cssText = `background: #2b2b2b;`;
      } else {
        document.querySelector('html').style.cssText = `background: #ffffff;`;
      }
    },
    showTheColumn(columnName) {
      return this.showColumnTitles.indexOf(columnName) > -1;
    },
    highLightCode(code, isHighlight) {
      code = code.replace(/\\n/g, '\n').replace(/\\t/g, '\t');
      let rows = [];
      if (code.indexOf('java.') > -1) {
        let content = '<pre style="word-break:break-all;white-space: normal;">' + code.replace(/\n/g, '<br/>').replace(/\t/g, '　　').replace(/&lt;/g, '<').replace(/&gt;/g, '>') + "</pre>";
        rows.push({isH: true, content});
        return rows;
      } else if (isHtml.test(code)) {
        let content = code;
        if (isHighlight) {
          let h = [];
          let hContent = code.match(/<em>([\s\S]*?)<\/em>/g);
          code = code.replace(/<em>([\s\S]*?)<\/em>/g, "@Highlight@");

          let strings = code.split('@');
          strings = strings.filter(x => !!x);
          let hi = 0;
          for (let i = 0; i < strings.length; i++) {
            let isH = strings[i] === 'Highlight';
            let content = isH ? hContent[hi] : strings[i];
            h.push({isH, content});
            isH && hi++
          }
          rows.push(...h);
          return rows;
        }
        rows.push({isH: false, content});
        return rows;
      } else {
        let content = '<div style="word-break:break-all;white-space: normal;">' + code.replace(/\n/g, '<br/>').replace(/\t/g, '　　') + "</div>";
        rows.push({isH: true, content});
        return rows;
      }
    },
    appNameChange() {
      this.getExtendList();
      if (this.filter.appName) {
        this.envComplete = this.appNameWithEnvMap[this.filter.appName] || [];
      } else {
        this.envComplete = [];
        for (let env in this.envWithAppNameMap) {
          if (this.envWithAppNameMap.hasOwnProperty(env)) {
            this.envComplete.push(env);
          }
        }
      }
      if (this.filter.env && this.envComplete.indexOf(this.filter.env) < 0) {
        this.filter.env = '';
      }
      this.envComplete.sort(function(a, b){return a.localeCompare(b)});
    },
    envChange() {
      if (this.filter.env) {
        this.appNameComplete = this.envWithAppNameMap[this.filter.env] || [];
      } else {
        this.appNameComplete = [];
        for (let appName in this.appNameWithEnvMap) {
          if (this.appNameWithEnvMap.hasOwnProperty(appName)) {
            this.appNameComplete.push(appName);
          }
        }
      }
      if (this.filter.appName && this.appNameComplete.indexOf(this.filter.appName) < 0) {
        this.filter.appName = '';
      }
      this.appNameComplete.sort(function(a, b){return a.localeCompare(b)});
    },
    getExtendList() {
      this.allColumns = [
        ...this.allColumns
      ];
      if (this.filter.appName) {
        axios.post(process.env.VUE_APP_API + '/getExtendfieldList?appName=' + this.filter.appName).then(data => {
          let _data = _.get(data, 'data', {});
          let list = [];
          for (let item in _data) {
            if (_data.hasOwnProperty(item)) {
              list.push({
                field: item,
                fieldName: _data[item]
              });
              this.allColumns.push({
                label: _data[item],
                value: item
              })
            }
          }
          this.extendList = list;
        })
      } else {
        this.extendList = [];
        this.extendOptions = [];
      }
    },
    completeFilter(value, option) {
      return option.indexOf(value) === 0;
    },
    searchAppName() {
      if (this.appNameComplete.length === 0) {

        // 为了防止客户端长时间未关闭造成的sessionStorage缓存不失效的问题加一个sessionStorage缓存半小时失效一次的判断
        if (sessionStorage['cache_appNameWithEnvs'] && sessionStorage['cache_appNameWithEnvs_time']
                && sessionStorage['cache_appNameWithEnvs_time'] > new Date().getTime() - 1800000) {
          this.analysisAppNameWithEnv();
        } else {
          this.completeFilterLoading = true;
          axios.post(process.env.VUE_APP_API + '/queryAppName?appNameWithEnv', {
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
            this.completeFilterLoading = false;
            let buckets = _.get(data, 'data.aggregations.dataCount.buckets', []).map(item => {
              return item.key
            });
            sessionStorage['cache_appNameWithEnvs'] = JSON.stringify(buckets);
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
        if (splitStr.length > 1) {
          this.appNameWithEnvMap[splitStr[0]].push(splitStr[1]);
          if (!this.envWithAppNameMap[splitStr[1]]) {
            this.envWithAppNameMap[splitStr[1]] = [];
          }
          this.envWithAppNameMap[splitStr[1]].push(splitStr[0]);
        } else {
          this.appNameWithEnvMap[splitStr[0]].push('');
        }
      }
      this.appNameComplete = [];
      for (let appName in this.appNameWithEnvMap) {
        if (this.appNameWithEnvMap.hasOwnProperty(appName)) {
          this.appNameComplete.push(appName);
        }
      }
      this.appNameComplete.sort(function(a, b){return a.localeCompare(b)});
      this.envComplete = [];
      for (let env in this.envWithAppNameMap) {
        if (this.envWithAppNameMap.hasOwnProperty(env)) {
          this.envComplete.push(env);
        }
      }
      this.envComplete.sort(function(a, b){return a.localeCompare(b)});
    },
    closeExtendTag(index) {
      this.extendOptions.splice(index, 1);
    },
    closeTag(index) {
      this.searchOptions.splice(index, 1);
    },
    selectExtendHandler(extend) {
      this.select_extend = extend.value;
      this.select_extend_label = extend.label
    },
    addExtendTag() {
      if (this.extendTag) {
        //同样的field只能出现一次，有的话覆盖
        let isExistField = false;
        for (let i = 0; i < this.extendOptions.length; i++) {
          if (this.extendOptions[i].field === this.select_extend) {
            this.extendOptions[i] = {
              field: this.select_extend,
              label: this.select_extend_label,
              tag: this.extendTag
            };
            isExistField = true;
            break;
          }
        }

        if (!isExistField) {
          this.extendOptions.push({
            field: this.select_extend,
            label: this.select_extend_label,
            tag: this.extendTag
          })
        }
        this.extendTag = "";
      }
    },
    addTag() {
      if (this.tag) {
        this.searchOptions.push({
          type: this.selectOption,
          tag: this.tag
        });
        this.tag = "";
      }
    },
    columnsChange() {

      this.list.hists = _.clone(this.list.hists);
      this.$nextTick(()=> {
        this.localStorageChange('showColumnTitles', this.showColumnTitles);
      })
    },
    substr(str, limit) {
      limit = limit || 30;
      if (str.length > limit) {
        return str.substring(0, limit) + '...';
      }
      return str;
    },
    getRowName(row) {
      return row.logLevel + ' ' + row.id
    },
    dblclick(row) {
      let ele = $('.' + row.id);
      ele.find('.ivu-table-cell-expand').click();
    },
    sortChange({key, order}) {
      let sort = {};
      sort[key] = order;
      this.sort = [sort];
      $('.row_detail').remove();
      this.doSearch();
    },
    setShowFilter() {
      this.showFilter = !this.showFilter;
      if (this.showFilter) {
        this.$nextTick(() => {
          this.drawLine()
        })
      }
    },
    drawLine() {
      let myChart = this.$echarts.init(document.getElementById('myChart'));
      if (this.chartData.length === 0) {
        myChart.clear();
        return false;
      }
      window.addEventListener('resize', () => {
        myChart.resize();
      });

      // 绘制图表
      myChart.setOption({
        grid: {
          x: 70,
          y: 10
        },
        // title: {
        //     text: '数量',
        //     left: 'center',
        //     textStyle: {
        //         color: '#333'
        //     }
        // },
        tooltip: {
          formatter(p) {
            return '时间：' + p.name + '<br/>数量：' + p.value + '条'
          },
          position: function (p) {   //其中p为当前鼠标的位置
            return [p[0] - 50, p[1] - 50];
          },
          extraCssText: 'text-align:left'
        },
        xAxis: {
          data: _.map(this.chartData, (d) => {
            return moment(d.key).format(this.chartInterval.format)
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
          name: '数量',
          type: 'bar',
          data: _.map(this.chartData, (d) => {
            return d.doc_count
          }),
          itemStyle: {
            borderColor: 'rgb(110, 173, 193)',
            color: 'rgba(110, 173, 193,0.6)'
          }
        }]
      });
    },
    drawErrorLine(data) {
      let errorChart = this.$echarts.init(document.getElementById('errorChart'));
      if (data.length === 0) {
        errorChart.clear();
        return;
      }

      window.addEventListener('resize', () => {
        errorChart.resize();
      });
      errorChart.setOption({
        grid: {
          x: 70,
          y: 10
        },
        // title: {
        //     text: '错误数',
        //     left: 'center',
        //     textStyle: {
        //         color: '#333'
        //     }
        // },
        tooltip: {
          formatter(p) {
            return '时间：' + p.name + '<br/>错误数：' + p.value
          },
          position: function (p) {   //其中p为当前鼠标的位置
            return [p[0] - 50, p[1] - 50];
          },
          extraCssText: 'text-align:left'
        },
        xAxis: {
          data: _.map(data, (d) => {
            return moment(d.key).format(this.chartInterval.format)
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
    getShouldFilter() {
      let filters = [];
      for (let itemKey in this.filter) {
        if (this.filter.hasOwnProperty(itemKey)) {
          if (this.isExclude && itemKey === 'appName') {
            continue;
          } else if (this.filter[itemKey]) {

            let _data = this.filter[itemKey];
            let query = '';
            //判断是否是数组
            if (Array.isArray(_data)) {
              query = _data.join(',');
              if (query) {
                filters.push({
                  "query_string": {
                    "query": query,
                    "default_field": itemKey
                  }
                })
              }
            } else {
              query = _data.replace(/,/g, ' ');
              filters.push({
                "match_phrase": {
                  [itemKey]: {
                    "query": query
                  }
                }
              })
            }
          }
        }
      }

      for (let extend of this.extendOptions) {
        filters.push({
          "match_phrase": {
            [extend.field]: {
              "query": extend.tag
            }
          }
        })
      }

      if ((this.searchQuery && this.useSearchQuery) || (this.searchKey && !this.useSearchQuery)) {
        filters.push({
          "query_string": {
            "query": this.useSearchQuery ? this.searchQuery : this.searchKey,
            "default_field": "content"
          }
        })
      }

      //判断日期
      if (this.dateTimeRange.length > 0 && this.dateTimeRange[0] !== '') {
        let now = new Date();
        let startDate = new Date(this.dateTimeRange[0]);
        let endDate = new Date(this.dateTimeRange[1]);
        if(endDate > now) {
          endDate = now
        }
        filters.push({
          "range": {
            "dtTime": {
              "gte": Date.parse(startDate),
              "lt": Date.parse(endDate),
            }
          }
        })
      }

      return filters
    },
    clear() {
      this.filter = {
        "logLevel": '',
        "appName": "",
        "env": "",
        "traceId": ""
      };
      this.searchKey = "";
      this.dateTimeRange = [moment(new Date().getTime() - 3600000).format('YYYY-MM-DD HH:mm:ss'), moment(new Date()).format('YYYY-MM-DD 23:59:59')];
      this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
      this.doSearch();
    },
    dateChange() {
      if (this.dateTimeRange.length > 0) {
        if (this.dateTimeRange[0] !== '' && this.dateTimeRange[1] !== '') {
          let startDate = new Date(this.dateTimeRange[0]);
          let endDate = new Date(this.dateTimeRange[1]);
          if (startDate.getHours() === 0 && startDate.getMinutes() === 0 && endDate.getHours() === 0 && endDate.getMinutes() === 0) {
            this.dateTimeRange[1].setHours(23, 59);
            this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
          }
        } else if (this.dateTimeRange[0] !== '') {
          let startDate = new Date(this.dateTimeRange[0]);
          this.dateTimeRange[1] = moment(startDate).format('YYYY-MM-DD 23:59:59');
          this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
        } else if (this.dateTimeRange[1] !== '') {
          let endDate = new Date(this.dateTimeRange[1]);
          this.dateTimeRange[0] = moment(endDate).format('YYYY-MM-DD 00:00:00');
          this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
        }
      }
    },
    doSearch(keyName, item) {
      this.localStorageChange('size', this.size);

      if (this.isSearching) {
        return false;
      }

      if (keyName && item) {

        if (keyName === 'appName' && this.isExclude && this.filter[keyName]) {
          this.filter[keyName] += ',' + item[keyName]
        } else {
          this.filter[keyName] = item[keyName]
        }
      }

      //列出范围内的日期
      let shouldFilter = this.getShouldFilter();

      let url = process.env.VUE_APP_API + '/clientQuery?clientStartDate=' + Date.parse(this.dateTimeRange[0])
              + '&clientEndDate=' + Date.parse(this.dateTimeRange[1]);

      let query = {
        "query": {
          "bool": {
            "must": [
              ...shouldFilter
            ]
          }
        }
      };

      if (this.isExclude && this.filter['appName']) {

        let mustNotArr = [];
        for (let appName of this.filter['appName'].split(',')) {
          mustNotArr.push({
            "match_phrase": {
              'appName': {
                "query": appName.replace(/,/g, ' ')
              }
            }
          })
        }

        query.query.bool['must_not'] = mustNotArr;
      }

      // 如果指定了traceId，根据阅读习惯，把排序规则改为正序排序
      let localSort = this.sort;
      if (this.filter.traceId !== '') {
        localSort = [{"dtTime": "asc"}];
      }

      let esFilter = {
        ...query,
        "highlight": {
          "fields": {
            "content": {
              "fragment_size": 2147483647
            }
          }
        },
        "sort": localSort
      };

      this.$Loading.start();

      let searchUrl = url + '&size=' + this.size + "&from=" + this.from;
      this.isSearching = true;
      axios.post(searchUrl, esFilter).then(data => {
        this.isSearching = false;
        this.$Loading.finish();
        let _searchData = _.get(data, 'data.hits', {
          total: 0,
          hits: []
        });

        _searchData.hits = _.map(_searchData.hits, item => {
          return {
            id: item._id,
            highlightCnt: _.get(item, "highlight.content[0]", ""),
            ...item._source,
          }
        });

        this.list = _searchData;

      });


      let chartFilter = {
        "query": {
          "bool": {
            "must": [
              ...shouldFilter,
            ]
          }
        },
        "aggregations": {
          "2": {
            "date_histogram": {
              "field": "dtTime",
              "interval": this.chartInterval.value,
              "min_doc_count": 0
            }
          }
        }
      };

      axios.post(process.env.VUE_APP_API + '/clientQuery?clientStartDate=' + Date.parse(this.dateTimeRange[0])
              + '&clientEndDate=' + Date.parse(this.dateTimeRange[1]) + '&from=0&size=0&chartData', chartFilter).then(data => {
        this.chartData = _.get(data, 'data.aggregations.2.buckets', []);
        this.drawLine();
      });

      this.getErrorRate().then(data => {
        this.drawErrorLine(data)
      });
    },
    getErrorRate() {
      let now = new Date();
      let startDate = new Date(this.dateTimeRange[0]);
      let endDate = new Date(this.dateTimeRange[1]);
      if(endDate > now) {
         endDate = now
      }
      let _promise = [];
      //按时间查询日志数量
      let query = {};
      let aggs = {
        "aggregations": {
          "dataCount": {
            "date_histogram": {
              "field": "dtTime",
              "interval": this.chartInterval.value,
              "min_doc_count": 0
            }
          }
        }
      };
      query = {
        "query": {
          "bool": {
            "must": [{
              "range": {
                "dtTime": {
                  "gte": Date.parse(startDate),
                  "lt": Date.parse(endDate)
                }
              }
            }
            ]
          }
        },
        ...aggs
      };

      let _errorQuery = {
        "query": {
          "bool": {
            "must": [{
              "range": {
                "dtTime": {
                  "gte": Date.parse(startDate),
                  "lt": Date.parse(endDate)
                }
              }
            }, {
              "match_phrase": {
                'logLevel': {
                  "query": 'ERROR'
                }
              }
            }
            ]
          }
        },
        ...aggs
      };

      let url = process.env.VUE_APP_API + '/clientQuery?clientStartDate=' + Date.parse(this.dateTimeRange[0])
              + '&clientEndDate=' + Date.parse(this.dateTimeRange[1]) + '&from=0&size=0&errChat';
      _promise.push(axios.post(url, _errorQuery).then(data => {
        return _.get(data, 'data.aggregations.dataCount.buckets', [])
      }));

      return Promise.all(_promise).then(datas => {
        let errorDatas = datas[0];
        if (errorDatas.length === 0) {
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
    prevPage() {
      let localFrom = this.from - this.size;
      if (localFrom < 0) {
        localFrom = 0;
      }
      this.from = localFrom;
      this.doSearch();
    },
    nextPage() {
      this.from = this.from + this.size;
      this.doSearch();
    },
    goPage() {
      this.from = (this.jumpPageIndex - 1) * this.size;
      if (this.from > 0) {
        this.doSearch();
      }
    },
    init() {
      let plumeLogParamsStr = localStorage['cache_plumeLogParams'];
      if (plumeLogParamsStr) {
        this.$nextTick(() => {
          let plumeLogParams = JSON.parse(plumeLogParamsStr);
          plumeLogParams['showColumnTitles'] !== undefined && (this.showColumnTitles = plumeLogParams['showColumnTitles']);
          plumeLogParams['tableModel'] !== undefined && (this.tableModel = plumeLogParams['tableModel']);
          plumeLogParams['autoWordWrap'] !== undefined && (this.autoWordWrap = plumeLogParams['autoWordWrap']);
          plumeLogParams['darkMode'] !== undefined && (this.darkMode = plumeLogParams['darkMode']);
          plumeLogParams['size'] !== undefined && (this.size = plumeLogParams['size']);
          if (!this.tableModel && this.darkMode && !this.autoWordWrap) {
            document.querySelector('html').style.cssText = `background: #2b2b2b;`;
          } else {
            document.querySelector('html').style.cssText = `background: #ffffff;`;
          }
        })
      } else {
        localStorage['cache_plumeLogParams'] = '{}';
      }

      if (this.$route.query.appName) {
        this.filter['appName'] = this.$route.query.appName;
      }
      if (this.$route.query.env) {
        this.filter['env'] = this.$route.query.env;
      }
      if (this.$route.query.className) {
        this.filter['className'] = this.$route.query.className;
      }
      if (this.$route.query.traceId) {
        this.filter['traceId'] = this.$route.query.traceId;
      }
      if (this.$route.query.logLevel) {
        this.filter['logLevel'] = [this.$route.query.logLevel];
      }
      if (this.$route.query.serverName) {
        this.filter['serverName'] = [this.$route.query.serverName];
      }
      if (this.$route.query.time) {
        let times = this.$route.query.time.split(',');
        if (times.length > 1) {
          this.dateTimeRange = [moment(parseInt(times[0])).format('YYYY-MM-DD HH:mm:ss'), moment(parseInt(times[1])).format('YYYY-MM-DD HH:mm:ss')];
          this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
        }
      }
      if(this.$route.query.searchKey) {
        this.searchKey = this.$route.query.searchKey
      }
      this.isSearching = false;

      setTimeout(() => {
        this.doSearch();
        this.searchAppName();
        this.getExtendList();
      }, 0)
    },
    queryFilterCheck(localFilter, key, value) {
      if (value !== '') {
        localFilter[key] = value;
      } else {
        localFilter[key] && (delete localFilter[key]);
      }
    },
    localStorageChange(key, value) {
      let plumeLogParamsStr = localStorage['cache_plumeLogParams'];
      let plumeLogParams = plumeLogParamsStr ? JSON.parse(plumeLogParamsStr) : {};
      plumeLogParams[key] = value;
      localStorage['cache_plumeLogParams'] = JSON.stringify(plumeLogParams);
    },
    logModelNormalRowMouseEnter($event) {
      $event.currentTarget.className = 'log_model_normal_row_enter';
    },
    logModelNormalRowMouseLeave($event) {
      $event.currentTarget.className = 'log_model_normal_row';
    },
    goTop() {
      let timer = setInterval(function() {
        let osTop = document.documentElement.scrollTop || document.body.scrollTop;
        let speed = Math.floor(-osTop / 5);
        if (speed > -100) {
          speed = -100;
        }
        document.documentElement.scrollTop = document.body.scrollTop = osTop + speed;
        if(osTop <= 0) {
          clearInterval(timer);
        }
      }, 30)
    },
    goDown() {
      let timer = setInterval(function() {
        let currTop = document.documentElement.scrollTop || document.body.scrollTop;
        let targetTop = document.documentElement.scrollHeight - $(window).height();
        let speed = Math.floor((targetTop - currTop) / 5);
        if (speed < 100) {
          speed = 100;
        }
        document.documentElement.scrollTop = document.body.scrollTop = currTop + speed;
        if(currTop >= targetTop) {
          clearInterval(timer);
        }
      }, 30)
    }
  },
  watch: {
    "filter": {
      handler() {
        let localFilter = {...this.$route.query};
        for (let k in this.filter) {
          if (this.filter.hasOwnProperty(k)) {
            this.queryFilterCheck(localFilter, k, this.filter[k]);
          }
        }
        this.$router.push({
          query: {...localFilter}
        }).catch(err => err);
        this.from = 0;
      },
      deep: true
    },
    "dateTimeRange": {
        handler() {
          let localFilter = {...this.$route.query};
          if(this.dateTimeRange.length > 0) {
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
    'list': function() {
      this.$nextTick(function() {
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
                    rowSelector.addClass('row_over_flow_hidden can_click').attr('title', '点击显示完整信息').on("click", function(e) {rowSelectorClickFunc(e)});
                  }
                }
              };
              rowSelector.addClass('can_click').attr('title', '点击显示完整信息').on("click", function(e) {rowSelectorClickFunc(e)});
              $(rowSelector.find('.row_pick_up_text')[0]).show().on("click", function(e) {
                e.preventDefault();
                if (!rowSelector.hasClass('row_over_flow_hidden')) {
                  let rowSelectorTop = rowSelector[0].offsetTop + document.getElementsByClassName('pnl_content')[0].offsetTop;
                  if (rowSelectorTop < (document.documentElement.scrollTop || document.body.scrollTop)) {
                      document.documentElement.scrollTop = document.body.scrollTop = rowSelectorTop;
                  }
                  rowSelector.addClass('row_over_flow_hidden can_click').attr('title', '点击显示完整信息').on("click", function(e) {rowSelectorClickFunc(e)});
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
  activated() {
    // console.log("activated")
    // this.init();
  },
  mounted() {
    this.init()
  }
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
  margin-top: 30px;
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
    padding: 5px 10px;
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
}

.bright_mode {

  .log_model_normal_row {
    background-color: #ffffff;

    .row_app_name {
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
    }
    .DEBUG {
      color: rgba(0,0,0,.4);
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
  .log_model_normal_row {
    background-color: #2b2b2b;

    .row_app_name {
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
    }
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
      color: #ff3b3b;
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
