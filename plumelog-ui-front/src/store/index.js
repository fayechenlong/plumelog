import Vue from "vue";
import Vuex from "vuex";
import axios from '@/services/http'
Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    config: {
      modeName:''
    }
  },
  getters: {
    config: state => state.config,
  },
  mutations: {
    setConfig : (state, config) => {
      state.config = config
    }
  },
  actions: {
    async getConfig({ commit ,state }) {
      axios.get(process.env.VUE_APP_API+ '/getRunModel').then(res=> {
          commit('setConfig', {modeName:res.data})
      })
    }
  },
  modules: {}
});
