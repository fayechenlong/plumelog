<template>
  <div class="pnl_wraper">
    <log-header></log-header>

    <Tabs active-key="Redis设置">
      <Tab-pane label="Redis设置" key="Redis设置">
        <Button icon="ios-add" @click="add" class="btn_add">添加</Button>
        <div style="clear:both"></div>
        <Table height="600" :content="self" :columns="columns" @on-selection-change="changeSelect" :data="warnData">
          <template slot-scope="{ row, index }" slot="action">
            <Button size="small" @click="edit(index)">修改</Button>&nbsp;&nbsp;
            <Button type="error" size="small" @click="del(index)">删除</Button>
          </template>
        </Table>
        <div class="modal" style="display:block" v-if="showDialog" role="dialog">
          <div class="modal-dialog">
            <div class="modal-content">
              <div class="modal-header">
                <h5 class="modal-title">{{ isEdit ? '修改' : '添加' }}Redis设置</h5>
                <button type="button" class="close" data-dismiss="modal" @click="showDialog=false" aria-label="关闭">
                  <span aria-hidden="true">&times;</span>
                </button>
              </div>
              <div class="modal-body">
                <Form :model="dataInfo" ref="dataForm" :label-width="100">
                  <FormItem label="类型" required>
                    <Select v-model="dataInfo.type" placeholder="请选择类型">
                      <Option v-for="item in hookServeList" :value="item.value" :key="item.value">{{
                          item.label
                        }}
                      </Option>
                    </Select>
                  </FormItem>
                  <FormItem
                      v-for="(item, index) in dataInfo.hostAndPorts"
                      :label="'节点地址' + index"
                      :prop="'hostAndPorts.' + index + '.host'">
                    <Row>
                      <Col span="18">
                        <Input type="text" v-model="item.host" placeholder="host"></Input>
                        <Input type="text" v-model="item.port" placeholder="port"></Input>
                      </Col>
                      <Col span="4" offset="1">
                        <Button icon="ios-trash" @click="handleRemove(index)"></Button>
                      </Col>
                    </Row>
                  </FormItem>
                  <FormItem>
                    <Row>
                      <Col span="12">
                        <Button type="dashed" long @click="handleAdd" icon="md-add">添加</Button>
                      </Col>
                    </Row>
                  </FormItem>
                  <FormItem label="密码">
                    <Input v-model="dataInfo.password" placeholder="输入正则表达式"/>
                  </FormItem>
                  <FormItem label="masterName">
                    <Input v-model="dataInfo.masterName" placeholder="输入哨兵masterName"/>
                  </FormItem>
                  <FormItem label="库节点" required>
                    <Input v-model="dataInfo.index" type="number" placeholder="输入库节点"/>
                  </FormItem>
                </Form>
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal" @click="showDialog=false">关闭
                </button>
                <button type="button" class="btn btn-primary" @click="save">{{ isEdit ? '保存' : '添加' }}</button>
              </div>
            </div>
          </div>
        </div>
      </Tab-pane>
    </Tabs>
  </div>
</template>

<script>
import axios from '@/services/http'
import _ from 'lodash'
import moment from 'moment'
import 'view-design/dist/styles/iview.css';
import logHeader from '@/components/logHeader.vue'
import confirmDelete from '@/components/confirmDelete.vue'
import "@/assets/less/base.less";

export default {
  name: "Redis",
  data() {
    return {
      selection: [],
      showConfirm: false,
      hookServeList: [{value: 'single', label: 'single'}, {value: 'sentinel', label: 'sentinel'}, {
        value: 'cluster',
        label: 'cluster'
      }],
      dataInfo: {
        configId: '',//配置id
        type: 'single',//redis 连接类型
        hostAndPorts: [{
          host: '',
          port: ''
        }],
        password: '',
        masterName: '',
        index: 0
      },
      from: 0,
      warnData: [],
      showDialog: false,
      columns: [
        {
          type: 'selection',
          width: 60,
          align: 'center'
        },
        {
          title: '配置ID',
          width: 150,
          key: 'configId'
        },
        {
          title: '类型',
          width: 150
        },
        {
          title: '连接信息',
          key: 'hostAndPorts',
          render: (h, r) => {

            if (!r.row.hostAndPorts) {
              return ""
            }

            let html = [];
            for (let i = 0; i < r.row.hostAndPorts.length; i++) {
              html.push(r.row.hostAndPorts[i].host + ':' + r.row.hostAndPorts[i].port)
            }
            return h('div', {
              domProps: {
                innerHTML: html.join("<br>")
              }
            })
          }
        }, {
          title: 'masterName',
          key: 'masterName'
        },
        {
          title: '库节点',
          key: 'index'
        },
        {
          title: '操作',
          slot: 'action',
          width: 150,
          align: 'center'
        }
      ],
      self: this,
      showMore: true,
      isEdit: false
    }
  },
  computed: {
    isDisabled() {
      return this.selection.length == 0;
    }
  },
  components: {
    logHeader,
    confirmDelete
  },
  methods: {
    changeSelect(selection) {
      this.selection = selection;
    },
    add() {
      this.initDataInfo();
      this.isEdit = false;
      this.showDialog = true;
    },
    delIndex(_info) {
      return new Promise((res, rej) => {
        axios.post(process.env.VUE_APP_API + '/deleteRedisConfig?configId=' + _info.configId).then(data => {
          if (data.data.success) {
            res();
          } else {
            rej();
          }
        })
      })
    },
    del(index) {
      let _info = this.warnData[index];
      if (confirm('确认要删除ID为 ' + _info.configId + ' 的配置么')) {
        this.delIndex(_info).then(() => {
          this.$Message.success('删除成功');
          this.getData();
        })
      }
    },
    edit(index) {
      this.dataInfo = this.warnData[index];
      this.isEdit = true;
      this.showDialog = true;
    },
    save() {
      if (this.dataInfo.type == '') {
        this.$Message.error('请选择类型');
        return false;
      } else if (this.dataInfo.index == null) {
        this.$Message.error('请填库节点');
        return false;
      }
      this.setData(this.dataInfo);
    },
    initDataInfo() {
      this.dataInfo = {
        configId: '',//配置id
        type: 'single',//redis 连接类型
        hostAndPorts: [{
          host: '',
          port: ''
        }],
        password: '',
        masterName: '',
        index: 0
      };
    },
    setData(info) {
      let _info = _.clone(info)
      axios.post(process.env.VUE_APP_API + '/saveRedisConfig', _info).then(data => {
        if (data.data.success) {
          this.$Message.success('保存成功');
          this.showDialog = false;
          this.getData();
        } else {
          this.$Message.error(data.data.message);
        }
      })
    },
    getData() {
      this.$Loading.start();
      axios.post(process.env.VUE_APP_API + '/getRedisConfigs').then(data => {
        this.$Loading.finish();
        this.warnData = _.get(data, 'data', []).map(item => {
          return {
            ...item,
            status: item.status == 1
          }
        });
      })
    },
    formatTime(time) {
      if (time) {
        return moment(time).format('YYYY-MM-DD HH:mm:ss')
      }
      return ''
    },
    handleAdd() {
      this.dataInfo.hostAndPorts.push({
        host: '',
        port: ''
      });
    },
    handleRemove(index) {
      this.dataInfo.hostAndPorts.splice(index, 1)
    }
  },
  mounted() {
    this.getData();
  }
}
</script>
<style lang="less">

.ivu-tabs-tabpane {
  text-align: left;
}

.logList {
  li {
    text-align: left;
    padding-left: 20px;
    margin-bottom: 30px;

    .time {
      padding-bottom: 8px;
      border-bottom: 1px dotted #ccc;
      margin-bottom: 10px;
    }

    .btn_showDetail {
      margin-top: 10px;
      padding-left: 20px;
      font-size: 12px;

    }

    .cnt {
      .key {
        display: inline-block;
        width: 80px;
        padding-right: 10px;
        text-align: right;
        font-size: 12px;
        font-weight: 700;
      }
    }
  }
}

.modal {
  z-index: 1000;
}

.modal-backdrop {
  z-index: 900;
}

.ivu-tabs-nav-scroll {
  margin-top: 20px;
}

.ivu-form-item {
  margin-bottom: 5px;
}

.btn_add {
  float: right;
  margin: 10px 20px 10px 0;
}

.btn_delete {
  float: right;
  margin: 10px 10px 10px 0;
}

.btn_clear {
  margin: 0 0 20px 10px;
}
</style>
