import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import ViewUI from 'view-design';
import 'view-design/dist/styles/iview.css';

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
