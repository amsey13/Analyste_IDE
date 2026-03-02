<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { UserService } from '../api/UserService';
import Button from 'primevue/button';
import Divider from 'primevue/divider';

const router = useRouter();
const loading = ref(false);
const initialCheck = ref(true); // Pour éviter un flash visuel du bouton si déjà connecté

onMounted(async () => {
  try {
    
    await UserService.getCurrentUser();

    
    router.push('/app/projets');
  } catch (error) {
   
    initialCheck.value = false;
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
    <div v-if="initialCheck" class="flex w-full align-items-center justify-content-center">
      <i class="pi pi-spin pi-spinner text-4xl text-primary"></i>
    </div>

    <template v-else>
      <section class="hidden lg:flex flex-column align-items-center justify-content-center w-6 bg-primary p-8 text-white">
        <i class="pi pi-chart-bar text-8xl mb-4"></i>
        <h1 class="text-5xl font-bold mb-3">AFHelper</h1>
        <p class="text-xl text-blue-100 text-center line-height-3">
          Optimisez vos <strong>analyses systémiques</strong> grâce à notre moteur d'analyse décisionnelle.
        </p>
      </section>

      <section class="flex-grow-1 flex align-items-center justify-content-center p-4">
        <div class="surface-card p-6 shadow-4 border-round-xl w-full max-w-26rem border-top-3 border-primary">
          <div class="text-center mb-5">
            <i class="pi pi-shield text-primary text-5xl mb-3"></i>
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
              <li class="flex align-items-center mb-2">
                <i class="pi pi-check-circle text-green-500 mr-2"></i>
                <span>Authentification Single Sign-On</span>
              </li>
              <li class="flex align-items-center">
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