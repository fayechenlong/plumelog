import Vue from "vue";
import VueRouter from "vue-router";
import Home from "../views/Home.vue";
import Trace from "../views/Trace.vue";

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
  }
];

const router = new VueRouter({
  routes
});

export default router;
