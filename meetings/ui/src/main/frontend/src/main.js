import { createApp } from 'vue';
import App from './App.vue';
import router from './router/index.js';

//import B5 for Sakai < 23
import './resources/namespaced-bootstrap.scss';
//unsest some B3 rulesfor Sakai < 23
import './resources/bootstrap-overwrite.scss';

createApp(App).use(router).mount('#app');
