import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import ViewUI from 'view-design';
import 'view-design/dist/styles/iview.css';

// 引入基本模板
let echarts = require('echarts/lib/echarts')
// 引入柱状图组件
require('echarts/lib/chart/bar')
require('echarts/lib/chart/line')
require('echarts/lib/chart/pie')
// 引入提示框和title组件
require('echarts/lib/component/tooltip')
require('echarts/lib/component/title')

Vue.prototype.$echarts = echarts
Vue.config.productionTip = false;
Vue.use(ViewUI);


ViewUI.LoadingBar.config({
  color: '#5cb85c',
  failedColor: '#f0ad4e',
  height: 3
});

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount("#app");
