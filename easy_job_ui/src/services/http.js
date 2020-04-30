import axios from 'axios'

/** axios 请求连接等待时间 **/
axios.defaults.timeout = 5000
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
    return response
  },
  error => {
    return Promise.reject(error)
  }
)
export default axios
