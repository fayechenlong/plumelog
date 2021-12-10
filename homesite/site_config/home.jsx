import React from 'react';

export default {
  'zh-cn': {
    brand: {
      brandName: 'Plumelog',
      briefIntroduction: '简单易用的java分布式日志系统',
      buttons: [
        {
          text: '立即开始',
          link: '/zh-cn/docs/FASTSTART.html',
          type: 'primary',
        },
        {
          text: '查看Gitee',
          link: 'https://gitee.com/plumeorg/plumelog',
          type: 'normal'
        },
      ],
    },
    introduction: {
      title: '什么是plumelog?',
      desc: '一个简单易用的java日志系统，解放你的日志查询困难问题，方便快速追踪问题，安装配置简单，性能优秀',
      img: '/pic/plumelog.png',
    },
    features: {
      title: '特性一览',
      list: [
        {
          img: '/img/feature_transpart.png',
          title: '快速使用',
          content: 'java项目引用后无需复杂配置',
        },
        {
          img: '/img/feature_loadbalances.png',
          title: '部署方式灵活',
          content: '可以使用多种集群不是方案适合不同体量的系统',
        },
        {
          img: '/img/feature_service.png',
          title: '支持最轻量化使用',
          content: 'plumelog-lite作为嵌入式日志模块，可以不用部署，小型项目的福音',
        },
        {
          img: '/img/feature_hogh.png',
          title: '查询速度快',
          content: '3秒内出结果，让你定位问题更方便快捷',
        },
        {
          img: '/img/feature_runtime.png',
          title: '支持链路追踪',
          content: '自带链路追踪，可以排查项目性能问题',
        },
        {
          img: '/img/feature_maintenance.png',
          title: '支持错误报警',
          content: '支持错误报警，不错过任何故障',
        },
      ],
    },
    start: {
      title: '快速开始',
      desc: '简单描述',
      img: '/pic/0.png',
      button: {
        text: '阅读更多',
        link: '/zh-cn/docs/FASTSTART.html',
      },
    },
    users: {
      title: '用户',
      desc: <span>请到社区置顶ISSUES区域登记</span>,
      list: [
      ],
    },
  },
  'en-us': {
    brand: {
      brandName: 'Plumelog',
      briefIntroduction: 'some description of product',
      buttons: [
        {
          text: 'Quick Start',
          link: '/zh-cn/docs/FASTSTART.html',
          type: 'primary',
        },
        {
          text: 'View on Gitee',
          link: 'https://gitee.com/plumeorg/plumelog',
          type: 'normal',
        },
      ],
    },
    introduction: {
      title: 'Plumelog',
      desc: 'very easy to use  java log system',
      img: '/pic/plumelog.png',
    },
    features: {
      title: 'Feature List',
      list: [
        {
          img: '/img/feature_transpart.png',
          title: 'Fast use',
          content: 'No complex configuration is required after java project reference',
        },
        {
          img: '/img/feature_loadbalances.png',
          title: 'Flexible deployment',
          content: 'feature description',
        },
        {
          img: '/img/feature_service.png',
          title: 'Support the lightest use',
          content: 'feature description',
        },
        {
          img: '/img/feature_hogh.png',
          title: 'Fast query speed',
          content: 'feature description',
        },
        {
          img: '/img/feature_runtime.png',
          title: 'Support link tracking',
          content: 'feature description',
        },
        {
          img: '/img/feature_maintenance.png',
          title: 'Support error warning',
          content: 'feature description',
        }
      ]
    },
    start: {
      title: 'Quick start',
      desc: 'very easy to use  java log system',
      img: '/pic/0.png',
      button: {
        text: 'READ MORE',
        link: '/zh-cn/docs/FASTSTART.html',
      },
    },
    users: {
      title: 'users',
      desc: <span>some description</span>,
      list: [
      ],
    },
  },
};
