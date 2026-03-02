<script setup>
import { ref, onMounted } from 'vue';
import Card from 'primevue/card';
import Chart from 'primevue/chart';
import { UserService } from '../api/UserService';

const user = ref(null);
const chartData = ref();
const chartOptions = ref();

onMounted(async () => {
  // 1. Récupération des données utilisateur pour personnalisation
  try {
    user.value = await UserService.getCurrentUser();
  } catch (e) {
    console.error("Erreur de récupération utilisateur");
  }
});

</script>

<template>
  <div class="grid">
    <div class="col-12 mb-4">
      <div class="surface-card p-4 border-round shadow-2">
        <h1 class="text-900 font-bold m-0">Analyse Fonctionnelle : Vue d'Ensemble</h1>
        <p class="text-600 mt-2" v-if="user">
          Expert analyste en charge : <strong>{{ user.fullName }}</strong>
        </p>
      </div>
    </div>
  </div>
</template>