<template>
  <div id="top" class="pnl_wraper">
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

        <table class='tbl_filters'>
          <tbody>
          <tr>
            <td class="key">应用名称</td>
            <td>
              <AutoComplete
                  v-model="filter.appName"
                  :data="appNameComplete"
                  class="txt txtAppName"
                  placeholder="搜索多个请用逗号或空格隔开"
                  :clearable="true"
                  :filter-method="completeFilter"
                  @on-change="appNameChange">
              </AutoComplete>
              <!-- <Select v-model="filter.appNames"
                      class="txt txtAppName"
                      filterable
                      allow-create
                      :loading="completeFilterLoading"
              >
                  <Option v-for="item in appNameComplete" :value="item.value" :key="item.value">{{ item.label }}</Option>
              </Select> -->
              <Checkbox v-model="isExclude">排除</Checkbox>
            </td>
          </tr>
          <tr>
            <td class="key">日志等级</td>
            <td>
              <Select v-model="filter.logLevel" placeholder="请选择日志等级">
                <Option value="" key="ALL">所有</Option>
                <Option value="INFO" key="INFO">INFO</Option>
                <Option value="ERROR" key="ERROR">ERROR</Option>
                <Option value="WARN" key="WARN">WARN</Option>
                <Option value="DEBUG" key="DEBUG">DEBUG</Option>
              </Select>
            </td>
          </tr>
          <tr>
            <td class="key">服务器名称</td>
            <td>
              <Input class="txt" name="serverName" v-model="filter.serverName" placeholder="搜索多个请用逗号或空格隔开"
                     :clearable="true"/>
            </td>
          </tr>
          </tbody>
        </table>

        <table class='tbl_filters'>
          <tr>
            <td class="key">类名</td>
            <td>
              <Input class="txt" name="className" v-model="filter.className" placeholder="搜索多个请用逗号或空格隔开"
                     :clearable="true"/>
            </td>
          </tr>
          <tr>
            <td class="key">追踪码</td>
            <td>
              <Input class="txt" name="traceId" v-model="filter.traceId" placeholder="搜索多个请用逗号或空格隔开" :clearable="true"/>
            </td>
          </tr>
          <tr>
            <td class="key">日期和时间</td>
            <td>
              <DatePicker ref='datePicker' v-model="dateTimeRange" @on-change="dateChange" type="datetimerange"
                          :options="dateOption" format="yyyy-MM-dd HH:mm:ss" placeholder="选择日期与时间"
                          style="width: 315px"></DatePicker>
            </td>
          </tr>
        </table>

        <Carousel v-model="slideIndex" arrow="never">
          <CarouselItem>
            <div id="myChart" class="chart"></div>
          </CarouselItem>
          <CarouselItem>
            <div id="errorChart" class="chart"></div>
          </CarouselItem>
        </Carousel>

        <div style="clear:both"></div>
        <table class="tbl_filters" style="width:900px">
          <tr v-if="extendList.length>0">
            <td class="key">扩展字段</td>
            <td>
              <Select v-model="select_extend" @on-change="selectExtendHandler" label-in-value placeholder="选择扩展字段" style="width:150px;margin-right:10px">
                <Option v-for="extend in extendList" :value="extend.field" :key="extend.field">{{
                    extend.fieldName
                  }}
                </Option>
              </Select>
              <Input class="txt" @on-enter="addExtendTag" :clearable="true" v-model="extendTag" placeholder="输入查询内容"
                     style="width:478px;"/>
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
              <Input class="txt" @on-enter="doSearch()" :clearable="true" style="width:638px" placeholder="输入搜索内容"
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
                     style="width:196px;"/>
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
      </template>
      <div style="clear:both"></div>
    </div>
    <div style="position:relative;margin-top:30px;">
      <div class="icon_arrow up" v-if="showFilter" @click="setShowFilter">
        <Icon type="ios-arrow-up" v-show="showFilter"/>
        <span class="text">收起</span>
      </div>
      <div style="position:absolute;top:-30px;right:20px">共 <b>{{ totalCount }}</b> 条数据</div>
      <div class="tip_table">
        <Icon size="14" type="md-star-outline"/>
        表格字段宽度可拖拽调节，双击或点击箭头可查看详情
      </div>
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
        <template slot-scope="{ row }" slot="traceId">
          <a :href="'./#/trace?traceId='+row.traceId+'&timeRange='+JSON.stringify(dateTimeRange)"
             title="点击查看链路追踪">{{ row.traceId }}</a>
          <Icon type="ios-search" v-if="row.traceId" @click="doSearch('traceId',row)"/>
        </template>
        <template slot-scope="{ row }" slot="content">
          <div style="white-space: pre-wrap; max-height: 100px;">
                <span v-for="item in hightLightCode(row.highlightCnt || row.content, !!row.highlightCnt)">
                                <span v-if="item.isH" v-html="item.content"></span>
                                <span v-else>{{ item.content }}</span>
                            </span>
          </div>
        </template>
      </Table>
    </div>

    <nav class="page_nav" aria-label="Page navigation example">
      <div class="pnl_select">
        <span class="name">显示字段：</span>
        <Select v-model="showColumnTitles" multiple placeholder="选择要显示的字段" @on-change="columnsChange" :max-tag-count="2"
                style="width:270px">
          <Option v-for="item in allColumns" :value="item.value" :key="item.value">{{ item.label }}</Option>
        </Select>
      </div>

      <ul v-if="totalCount && parseInt(totalCount/size) > 0" class="pagination justify-content-center"
          style="float:right;margin-right:30px">
        <li class="page-item" :class="{'disabled': !isShowLastPage }">
          <a class="page-link" href="javascript:void(0)" @click="prevePage" tabindex="-1">上一页</a>
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
          <div class="page-count"><a href="#top">返回顶部</a></div>
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

let isHtml = /<\/?[a-z][\s\S]*>/i
export default {
  name: "Home",
  data() {
    this.slotColumns = ['logLevel','serverName', 'appName', 'traceId', 'className'];
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
      appNameComplete: [],
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
          width:90
        },
        {
          label: '服务器名称',
          value: 'serverName',
          width: 150
        },
        {
          label: '应用名称',
          value: 'appName',
          width: 150
        },
        {
          label: '追踪码',
          value: 'traceId',
          width: 170,
        },
        {
          label: '类名',
          value: 'className',
          width: 270
        }
      ],
      showFilter: true,
      api: process.env.api,
      dateOption,

      dateTimeRange: [moment(new Date().getTime() - 60 * 1000 * 15).format('YYYY-MM-DD HH:mm:ss'), moment(new Date()).format('YYYY-MM-DD 23:59:59')],
      content: {
        _source: {}
      },
      searchKey: '',
      danger_str: '',
      filter: {
        "logLevel": '',
        "appName": "",
        "traceId": "",
        "className": "",
        "serverName": ""
      },
      list: {
        hits: []
      },
      size: 30,
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
            return h('div', moment(params.row.dtTime).format('YYYY-MM-DD HH:mm:ss.SSS'))
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
            return o.value == title
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
      columns.push(_.find(this.columns, ['key', 'content']))
      this.columns = columns; //让新加入的拓展字段，可以被vue管理
      return columns;
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
      if (this.totalCount > (this.from + this.size))
        return true
      else
        return false
    }
  },
  methods: {
    hightLightCode(code, isHighlight) {
      code = code.replace(/\\n\\t/g, "\n").replace(/\\n\\tat/g, "\n").replace(/\\n/g, '\n');
      let rows = [];
      if (code.indexOf('java.') > -1) {
        let content = '<pre style="word-break:break-all;white-space: normal;">' + code.replace(/\n/g, '<br/>').replace(/&lt;/g, '<').replace(/&gt;/g, '>') + "</pre>"
        rows.push({isH: true, content})
        return rows;
      } else if (isHtml.test(code)) {
        let content = code;
        if (isHighlight) {
          let h = [];
          let hContent = code.match(/<em>([\s\S]*?)<\/em>/g);
          code = code.replace(/<em>([\s\S]*?)<\/em>/g, "@Highlight@")

          var strings = code.split('@');
          strings = strings.filter(x => !!x)
          // console.log(code)
          // console.log(hContent)
          // console.log(strings)
          let hi = 0
          for (let i = 0; i < strings.length; i++) {
            let isH = strings[i] === 'Highlight';
            let content = isH ? hContent[hi] : strings[i];
            h.push({isH, content})
            isH && hi++
          }
          rows.push(...h)
          // console.log(rows)
          return rows;
        }
        rows.push({isH: false, content})
        return rows;
      } else {
        let content = '<div style="word-break:break-all;white-space: normal;">' + code.replace(/\n/g, '<br/>').replace(/\tat/g, '') + "</div>";
        rows.push({isH: true, content})
        return rows;
      }
    },
    appNameChange() {
      this.getExtendList();
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
            list.push({
              field: item,
              fieldName: _data[item]
            });
            this.allColumns.push({
              label: _data[item],
              value: item
            })
          }
          this.extendList = list;
        })
      } else {
        this.extendList = [];
        this.extendOptions = [];
      }
    },
    completeFilter(value, option) {
      return option.indexOf(value) == 0;
    },
    searchAppName() {
      if (this.appNameComplete.length == 0) {

        if (sessionStorage['cache_appNames']) {
          this.appNameComplete = JSON.parse(sessionStorage['cache_appNames'])
        } else {
          this.completeFilterLoading = true;
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
            this.completeFilterLoading = false;
            let buckets = _.get(data, 'data.aggregations.dataCount.buckets', []).map(item => {
              return item.key
            });
            sessionStorage['cache_appNames'] = JSON.stringify(buckets);
            this.appNameComplete = buckets;
          })

        }
      }
    },
    closeExtendTag(index) {
      this.extendOptions.splice(index, 1);
    },
    closeTag(index) {
      this.searchOptions.splice(index, 1);
    },
    selectExtendHandler(extend) {
      this.select_extend = extend.value
      this.select_extend_label = extend.label
    },
    addExtendTag() {
      if (this.extendTag) {
        //同样的field只能出现一次，有的话覆盖
        let isExistField = false;
        for (let i = 0; i < this.extendOptions.length; i++) {
          if (this.extendOptions[i].field == this.select_extend) {
            this.extendOptions[i] = {
              field: this.select_extend,
              label: this.select_extend_label,
              tag: this.extendTag
            }
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
        })
        this.tag = "";
      }
    },
    columnsChange() {
      this.list.hists = _.clone(this.list.hists);
      localStorage['cache_showColumnTitles'] = JSON.stringify(this.showColumnTitles);
    },
    substr(str, limit) {
      limit = limit || 30;
      if (str.length > limit) {
        return str.substring(0, limit) + '...';
      }
      return str;
    },
    getRowName(row, index) {
      return row.logLevel + ' ' + row.id
    },
    dblclick(row, index) {
      let ele = $('.' + row.id);
      ele.find('.ivu-table-cell-expand').click();
    },
    sortChange({key, order}) {
      let sort = {};
      sort[key] = order;
      this.sort = [sort]
      $('.row_detail').remove();
      this.doSearch();
    },
    setShowFilter(show) {
      this.showFilter = !this.showFilter;
      if (this.showFilter) {
        this.$nextTick(() => {
          this.drawLine()
        })
      }
    },
    drawLine() {
      let myChart = this.$echarts.init(document.getElementById('myChart'))
      if (this.chartData.length == 0) {
        myChart.clear()
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
          formatter(p, ticket) {
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
      let errorChart = this.$echarts.init(document.getElementById('errorChart'))
      if (data.length == 0) {
        errorChart.clear()
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
          formatter(p, ticket) {
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
      let date = [];
      for (let itemKey in this.filter) {
        if (this.isExclude && itemKey == 'appName') {
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
      if (this.dateTimeRange.length > 0 && this.dateTimeRange[0] != '') {
        let now = new Date()
        let startDate = new Date(this.dateTimeRange[0]);
        let endDate = new Date(this.dateTimeRange[1]);
        if(endDate > now) {
          endDate = now
        }
        // console.log(startDate, endDate)
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
        "traceId": ""
      }
      this.searchKey = "";
      this.dateTimeRange = [moment(new Date().getTime() - 60 * 1000 * 15).format('YYYY-MM-DD HH:mm:ss'), moment(new Date()).format('YYYY-MM-DD 23:59:59')];
      this.$refs.datePicker.internalValue = [moment(new Date().getTime() - 60 * 1000 * 15).format('YYYY-MM-DD HH:mm:ss'), moment(new Date()).format('YYYY-MM-DD 23:59:59')];
      this.doSearch();
    },
    dateChange() {
      let startDate = new Date(this.dateTimeRange[0]);
      let endDate = new Date(this.dateTimeRange[1]);
      if (startDate.getHours() == 0 && startDate.getMinutes() == 0 && endDate.getHours() == 0 && endDate.getMinutes() == 0) {
        this.dateTimeRange[1].setHours(23, 59)
        this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
      }
    },
    doSearch(keyName, item) {

      if (this.isSearching) {
        return false;
      }

      if (keyName && item) {

        if (keyName == 'appName' && this.isExclude && this.filter[keyName]) {
          this.filter[keyName] += ',' + item[keyName]
        } else {
          this.filter[keyName] = item[keyName]
        }
      }

      //列出范围内的日期
      let dateList = [];
      let startDate = _.clone(new Date(this.dateTimeRange[0]));
      let shouldFilter = this.getShouldFilter();

      if (startDate) {
        while (startDate <= this.dateTimeRange[1]) {
          dateList.push('plume_log_run_' + moment(startDate).format('YYYYMMDD') + '*')
          startDate = new Date(startDate.setDate(startDate.getDate() + 1));
        }
      }

      if (dateList.length == 0) {
        dateList.push('plume_log_run_' + moment().format('YYYYMMDD') + '*');
      }

      let url = process.env.VUE_APP_API + '/query?index=' + dateList.join(',');

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

      let esFilter = {
        ...query,
        "highlight": {
          "fields": {
            "content": {
              "fragment_size": 2147483647
            }
          }
        },
        "sort": this.sort
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
        })

        _searchData.hits = _.map(_searchData.hits, item => {
          return {
            id: item._id,
            highlightCnt: _.get(item, "highlight.content[0]", ""),
            ...item._source,
          }
        })

        this.list = _searchData;

      })


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
      }
      axios.post(process.env.VUE_APP_API + '/query?index=' + dateList.join(',') + '&from=0&size=0&chartData', chartFilter).then(data => {
        this.chartData = _.get(data, 'data.aggregations.2.buckets', []);
        this.drawLine();
      })

      this.getErrorRate(dateList).then(data => {
        this.drawErrorLine(data)
      });
    },
    getErrorRate(dateList) {
      let now = new Date()
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
      }
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
      }

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
      }


      let url = process.env.VUE_APP_API + '/query?size=0&from=0&index=' + dateList.join(',') + "&errChat"

      _promise.push(axios.post(url, _errorQuery).then(data => {
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
    prevePage() {
      let from = this.from - this.size
      if (from < 0) {
        from = 0;
      }
      this.from = from;
      this.doSearch();
    },
    nextPage() {
      let from = this.from + this.size
      this.from = from;
      this.doSearch();
    },
    goPage() {
      this.from = (this.jumpPageIndex - 1) * this.size;
      if (this.from > 0) {
        this.doSearch();
      }
    },
    init() {
      let titles = localStorage['cache_showColumnTitles'];
      if (titles) {
        this.showColumnTitles = JSON.parse(titles)
      }

      if (this.$route.query.appName) {
        this.filter['appName'] = this.$route.query.appName;
      }
      if (this.$route.query.className) {
        this.filter['className'] = this.$route.query.className;
      }
      if (this.$route.query.logLevel) {
        this.filter['logLevel'] = [this.$route.query.logLevel];
      }
      if (this.$route.query.time) {
        let times = this.$route.query.time.split(',');
        if (times.length > 1) {
          this.dateTimeRange = [moment(parseInt(times[0]) - 1000).format('YYYY-MM-DD HH:mm:ss'), moment(parseInt(times[1]) + 1000).format('YYYY-MM-DD HH:mm:ss')]
          this.$refs.datePicker.internalValue = _.clone(this.dateTimeRange);
        }
      }
      this.isSearching = false;

      setTimeout(() => {
        this.doSearch();
        this.searchAppName();
        this.getExtendList();
      }, 0)
    }
  },
  watch: {
    searchKey() {
      this.from = 0;
    },
    "filter": {
      handler() {
        this.from = 0;
      },
      deep: true
    },
    '$route.path': function (newVal, oldVal) {
      // console.log(newVal, oldVal)
      if (newVal === '/' && oldVal === '/login')
        this.init()
      else if (newVal === '/' && oldVal === '/warn')
        this.init()
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
        top: 2px;
        right: 5px;
        font-size: 16px;
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
</style>
<style lang="less" src="../assets/less/filters.less" scoped></style>
<style lang="less" scoped>


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
</style>
