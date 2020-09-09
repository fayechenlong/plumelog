import axios from 'axios'
import router from "@/router";
import ViewUI from 'view-design';
/** axios 请求连接等待时间 **/
axios.defaults.timeout = 300000
axios.defaults.withCredentials=true
axios.defaults.crossDomain=true
/** axios Request 配置 **/
axios.interceptors.request.use(
  config => {
    return config
  },
  error => {
    // 对请求错误做些什么
    return Promise.reject(error)
  }
)
/** axios Response 配置 **/
axios.interceptors.response.use(
  response => {
      if(response.data.code == 401) {
          router.replace({
              path: 'login'
          })
          ViewUI.LoadingBar.finish();
          return Promise.reject('未登陆')
      }
    return response
  },
  error => {
    return Promise.reject(error)
  }
)
export default axios
