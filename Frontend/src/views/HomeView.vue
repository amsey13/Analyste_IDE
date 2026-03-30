<script setup>
import { ref, onMounted, nextTick } from 'vue';
import { useRouter } from 'vue-router';
import { UserService } from '../features/users/api/UserService.js';
import Button from 'primevue/button';
import Divider from 'primevue/divider';
import Toast from 'primevue/toast';
import { useToast } from 'primevue/usetoast';


const router = useRouter();
const toast = useToast();
const loading = ref(false);
const initialCheck = ref(true);

const typedText = ref(null);

const typeWriter = (element, text, speed = 30) => {
  let i = 0
  element.innerHTML = '';

  const type = () => {
    if (i < text.length) {
      element.innerHTML += text.charAt(i);
      i++;
      setTimeout(type, speed);
    } else {
      // 3-second pause then restart
      setTimeout(() => {
        i = 0;
        element.innerHTML = '';
        type();
      }, 3000);
    }
  };

  type();
};

onMounted(async () => {
  try {

    await UserService.getCurrentUser();
    router.push('/app/projects');
  } catch (error) {

    initialCheck.value = false;

    await nextTick();
      if (typedText.value) {
        typeWriter(typedText.value, 'Optimisez vos analyses systémiques grâce à notre moteur d\'analyse décisionnelle.');

    }

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('error') && urlParams.get('error') === 'authentification_echouee') {

      setTimeout(() => {
        toast.add({
          severity: 'error',
          summary: 'Accès Refusé',
          detail: 'La connexion a échoué. Vérifiez vos accès JumpCloud.',
          life: 5000
        });
      }, 100);
      const cleanUrl = window.location.protocol + "//" + window.location.host + window.location.pathname;
      window.history.replaceState({ path: cleanUrl }, '', cleanUrl);
    }
  } finally {
    loading.value = false
  }
});

const login = () => {
  loading.value = true;
  setTimeout(() => {
    window.location.href = 'http://localhost:8080/oauth2/authorization/jumpcloud';
  }, 300);
};


</script>

<template>
  <main class="flex min-h-screen bg-blue-50">
    <Toast />

    <div v-if="initialCheck" class="flex w-full align-items-center justify-content-center">
      <i class="pi pi-spin pi-spinner text-4xl text-primary"></i>
    </div>

    <template v-else>
      <section class="left-panel hidden lg:flex flex-column align-items-center justify-content-center w-6 bg-primary p-8 text-white">        <i class="pi pi-chart-bar text-8xl mb-4"></i>
        <h1 class="text-5xl font-bold mb-3">AnalytiQ</h1>
        <p class="text-xl text-blue-100 text-center line-height-3" ref="typedText">
        </p>
      </section>

      <section class="flex-grow-1 flex align-items-center justify-content-center p-4">
        <div class="login-card surface-card p-6 shadow-4 border-round-xl w-full max-w-26rem border-top-3 border-primary">          <div class="text-center mb-5">
          <i class="shield-icon pi pi-shield text-primary text-5xl mb-3"></i>
          <div class="text-900 text-3xl font-bold mb-2">Bienvenue</div>
            <span class="text-600 font-medium">Connectez-vous à votre espace sécurisé</span>
          </div>

          <div class="flex flex-column gap-4">
            <Button
                label="Se connecter avec JumpCloud"
                icon="pi pi-lock"
                class="w-full py-3"
                :loading="loading"
                @click="login"
            />

            <Divider align="center" class="text-500 font-normal text-sm">PROTECTION OIDC</Divider>

            <ul class="list-none p-0 m-0 text-700 text-sm">
              <li class="check-item-1 flex align-items-center mb-2">
                <i class="pi pi-check-circle text-green-500 mr-2"></i>
                <span>Authentification Single Sign-On</span>
              </li>
              <li class="check-item-2 flex align-items-center">
                <i class="pi pi-check-circle text-green-500 mr-2"></i>
                <span>Accès réservé L3 MIAGE</span>
              </li>
            </ul>
          </div>

          <footer class="mt-6 text-center text-500 text-xs">
            © 2026 AnalytiQ <br>
            Université de Lille - Projet AFHelper
          </footer>
        </div>
      </section>
    </template>
  </main>
</template>

<style scoped>
/* Entrée de la carte */
@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0); }
}

/* Entrée du panneau gauche */
@keyframes fadeIn {
  from { opacity: 0; }
  to   { opacity: 1; }
}

/* Icône shield qui pulse doucement */
@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50%       { transform: scale(1.1); }
}

/* Items de la liste qui glissent */
@keyframes slideRight {
  from { opacity: 0; transform: translateX(-12px); }
  to   { opacity: 1; transform: translateX(0); }
}

.left-panel {
  animation: fadeIn 0.6s ease both;
}

.login-card {
  animation: slideUp 0.5s 0.1s ease both;
}

.shield-icon {
  animation: pulse 2.5s ease-in-out infinite;
  display: inline-block;
}

.check-item-1 { animation: slideRight 0.4s 0.4s ease both; opacity: 0; }
.check-item-2 { animation: slideRight 0.4s 0.55s ease both; opacity: 0; }
</style>