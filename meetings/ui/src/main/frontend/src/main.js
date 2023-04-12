import { createApp } from 'vue';
import { createPinia } from 'pinia'
import App from './App.vue';
import router from './router/index.js';

//import B5 for Sakai < 23
import './resources/namespaced-bootstrap.scss';
//unsest some B3 rulesfor Sakai < 23
import './resources/bootstrap-overwrite.scss';

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);

router.isReady().then(() => {
    app.mount('#app')
})
