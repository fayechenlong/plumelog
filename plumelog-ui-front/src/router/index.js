import Vue from "vue";
import VueRouter from "vue-router";
import Home from "../views/Home.vue";
import Trace from "../views/Trace.vue";
import Size from "../views/Size.vue";
import Chart from "../views/Chart.vue";
import Warn from "../views/Warn.vue";
import Expand from "../views/Expand.vue";
import Errors from "../views/Errors.vue";
import Login from "../views/Login.vue";
import Qps from "../views/Qps.vue";
import Redis from "../views/Redis.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/login",
    name: "Login",
    component: Login
  },
  {
    path: "/trace",
    name: "Trace",
    component: Trace
  },
  {
    path: "/",
    name: "Home",
    component: Home
  },
  {
    path: "/top",
    name: "Home",
    component: Home
  },
  {
    path: "/size",
    name: "Size",
    component: Size
  },{
    path: "/qps",
    name: "Qps",
    component: Qps
  },{
    path: "/redis",
    name: "Redis",
    component: Redis
  },
  {
    path: "/expand",
    name: "Expand",
    component: Expand
  },{
    path: "/errors",
    name: "Errors",
    component: Errors
  },
  {
    path: "/chart",
    name: "Chart",
    component: Chart
  },
  {
    path: "/warn",
    name: "Warn",
    component: Warn
  }
];

const router = new VueRouter({
  routes
});

export default router;
