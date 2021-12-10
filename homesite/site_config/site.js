// 全局的一些配置
export default {
  rootPath: '/product', // 发布到服务器的根目录，需以/开头但不能有尾/，如果只有/，请填写空字符串
  port: 8992, // 本地开发服务器的启动端口
  domain: 'dubbo.apache.org', // 站点部署域名，无需协议和path等
  defaultSearch: 'google', // 默认搜索引擎，baidu或者google
  defaultLanguage: 'zh-cn',
  'en-us': {
    pageMenu: [
      {
        key: 'home', // 用作顶部菜单的选中
        text: 'HOME',
        link: '/en-us/index.html',
      },
      {
        key: 'docs',
        text: 'DOCS',
        link: '/zh-cn/docs/README.html',
      }
    ],
    disclaimer: {
      title: 'Disclaimer',
      content: 'the disclaimer content',
    },
    documentation: {
      title: 'Documentation',
      list: [
        {
          text: 'Overview',
            link: '/zh-cn/docs/FASTSTART.html',
        },
        {
          text: 'Quick start',
            link: '/zh-cn/docs/FASTSTART.html',
        }
      ],
    },
    resources: {
      title: 'Resources',
      list: [
        {
          text: 'Community',
          link: 'https://gitee.com/plumeorg',
        },
      ],
    },
    copyright: 'Copyright © 2018 by plume stack 苏ICP备20032423号',
  },
  'zh-cn': {
    pageMenu: [
      {
        key: 'home',
        text: '首页',
        link: '/zh-cn/index.html',
      },
      {
        key: 'docs',
        text: '文档',
        link: '/zh-cn/docs/README.html',
      }
    ],
    disclaimer: {
      title: '免责声明',
      content: '本软件为免费软件并开放源码，用于交流学习，使用中遇到任何问题和纠纷本社区概不负责',
    },
    documentation: {
      title: '文档',
      list: [
        {
          text: '概览',
          link: '/zh-cn/docs/FASTSTART.html',
        },
        {
          text: '快速开始',
          link: '/zh-cn/docs/FASTSTART.html',
        },
        {
          text: '开发者指南',
          link: '/zh-cn/docs/FASTSTARThtml',
        },
      ],
    },
    resources: {
      title: '资源',
      list: [
        {
          text: '演示地址',
          link: 'http://demo.plumelog.com',
        },
        {
          text: '社区',
          link: 'https://gitee.com/plumeorg',
        },
      ],
    },
    copyright: 'Copyright © 2018 版权所有 plume开源社区  苏ICP备20032423号',
  },
};
