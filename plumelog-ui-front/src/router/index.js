import Vue from "vue";
import VueRouter from "vue-router";
import Home from "../views/Home.vue";
import Trace from "../views/Trace.vue";
import Size from "../views/Size.vue";
import Chart from "../views/Chart.vue";

Vue.use(VueRouter);

const routes = [
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
    path: "/size",
    name: "Size",
    component: Size
  },
  {
    path: "/chart",
    name: "Chart",
    component: Chart
  }
];

const router = new VueRouter({
  routes
});

export default router;
