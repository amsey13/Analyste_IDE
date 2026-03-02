<script setup>
import { ref, onMounted } from 'vue';
import { UserService } from '../features/core/api/UserService';
import Button from 'primevue/button';

const user = ref(null);

onMounted(async () => {
  try {
    // On récupère tes données une seule fois pour toute l'app
    user.value = await UserService.getCurrentUser();
  } catch (error) {
    console.error("Session introuvable, retour à l'accueil");
  }
});

const logout = () => {
  // Déconnexion propre côté Backend
  window.location.href = 'http://localhost:8080/logout';
};
</script>

<template>
  <div class="flex h-screen overflow-hidden">
    <aside class="w-18rem h-full flex flex-column border-right-1 surface-border"
           style="background-color: var(--secondary-color); color: white;">

      <div class="p-4 flex align-items-center gap-2 border-bottom-1 border-blue-800">
        <i class="pi pi-chart-bar text-2xl" style="color: var(--accent-color)"></i>
        <span class="text-xl font-bold">AFHelper</span>
      </div>

      <nav class="flex-grow-1 p-3">
        <router-link to="/dashboard" class="no-underline">
          <div class="flex align-items-center p-3 border-round text-blue-100 hover:bg-blue-800 cursor-pointer">
            <i class="pi pi-home mr-2"></i>
            <span class="font-medium">Tableau de bord</span>
          </div>
        </router-link>
      </nav>

      <div class="p-3 border-top-1 border-blue-800">
        <Button
            label="Déconnexion"
            icon="pi pi-power-off"
            severity="danger"
            text
            class="w-full text-white justify-content-start"
            @click="logout"
        />
      </div>
    </aside>

    <div class="flex-grow-1 flex flex-column surface-ground">

      <header class="h-4rem bg-white shadow-1 flex align-items-center justify-content-between px-5">
        <span class="text-700 font-semibold">IDE d'Analyse Fonctionnelle - AnalytiQ</span>

        <div v-if="user" class="flex align-items-center gap-2">
          <span class="text-900 font-medium">{{ user.fullName }}</span>
          <div class="w-2rem h-2rem border-circle flex align-items-center justify-content-center text-white"
               style="background-color: var(--primary-color)">
            {{ user.fullName.charAt(0) }}
          </div>
        </div>
      </header>

      <main class="p-5 overflow-y-auto">
        <router-view />
      </main>
    </div>
  </div>
</template>