import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

// PrimeVue et Thème
import PrimeVue from 'primevue/config';
import { definePreset } from '@primevue/themes';
import Aura from '@primevue/themes/aura';
import { createPinia } from 'pinia'

// PrimeFlex et Styles
import 'primeflex/primeflex.css';
import 'primeicons/primeicons.css';
import './assets/style.css';

const MyPreset = definePreset(Aura, {
    semantic: {
        primary: {
            50: '{blue.50}',
            100: '{blue.100}',
            200: '{blue.200}',
            300: '{blue.300}',
            400: '{blue.400}',
            500: '#0052cc',
            600: '{blue.600}',
            700: '{blue.700}',
            800: '{blue.800}',
            900: '{blue.900}',
            950: '{blue.950}'
        }
    }
});

const app = createApp(App);
const pinia = createPinia()

app.use(router);
app.use(PrimeVue, {
    theme: {
        preset: MyPreset,
        options: {
            darkModeSelector: '.my-app-dark' // Évite le switch auto en mode sombre pour l'instant
        }
    }
});
app.use(pinia)

app.mount('#app');