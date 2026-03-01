import { createApp } from 'vue';
import App from './App.vue';
import router from './router';

// PrimeVue et Thème
import PrimeVue from 'primevue/config';
import { definePreset } from '@primevue/themes'; // 👈 Indispensable
import Aura from '@primevue/themes/aura';

// PrimeFlex et Styles
import 'primeflex/primeflex.css';
import 'primeicons/primeicons.css';
import './assets/style.css';

// 1. On définit ton propre Preset (Le bleu de la MIAGE Analyst Suite)
const MyPreset = definePreset(Aura, {
    semantic: {
        primary: {
            50: '{blue.50}',
            100: '{blue.100}',
            200: '{blue.200}',
            300: '{blue.300}',
            400: '{blue.400}',
            500: '#0052cc', // 👈 Ton bleu exact
            600: '{blue.600}',
            700: '{blue.700}',
            800: '{blue.800}',
            900: '{blue.900}',
            950: '{blue.950}'
        }
    }
});

const app = createApp(App);

app.use(router);
app.use(PrimeVue, {
    // 2. On applique ton nouveau thème bleu ici
    theme: {
        preset: MyPreset,
        options: {
            darkModeSelector: '.my-app-dark' // Évite le switch auto en mode sombre pour l'instant
        }
    }
});

app.mount('#app');