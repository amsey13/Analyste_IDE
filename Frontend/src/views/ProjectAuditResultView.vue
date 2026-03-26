<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { SupportFeatureService } from '../features/projects/api/SupportFeatureService.js';
import Card from 'primevue/card';
import Button from 'primevue/button';
import Message from 'primevue/message';
import Skeleton from 'primevue/skeleton';
import Chart from 'primevue/chart';

const route = useRoute();
const router = useRouter();
const projectId = route.params.id;

const auditReport = ref(null);
const isLoading = ref(true);

const chartData = ref(null);
const chartOptions = ref(null);

const scoreColor = computed(() => {
  if (!auditReport.value) return '#94a3b8';
  const s = auditReport.value.score;
  if (s >= 80) return '#22c55e';
  if (s >= 50) return '#f59e0b';
  return '#ef4444';
});

const scoreText = computed(() => {
  if (!auditReport.value) return '';
  const s = auditReport.value.score;
  if (s >= 80) return 'EXCELLENT';
  if (s >= 50) return 'PARTIELLEMENT CONFORME';
  return 'NON CONFORME';
});

// L'ASTUCE : On associe chaque incohérence à sa recommandation par leur index (0 avec 0, 1 avec 1...)
const pairedIssues = computed(() => {
  if (!auditReport.value) return [];
  const incs = auditReport.value.inconsistencies || [];
  const corrs = auditReport.value.corrections || [];

  // On prend la taille de la liste la plus longue au cas où Mistral se trompe
  const maxLength = Math.max(incs.length, corrs.length);
  const pairs = [];

  for (let i = 0; i < maxLength; i++) {
    pairs.push({
      inconsistency: incs[i] || null,
      correction: corrs[i] || null
    });
  }
  return pairs;
});

const initChart = () => {
  chartData.value = {
    labels: ['Score', 'Reste'],
    datasets: [{
      data: [auditReport.value.score, 100 - auditReport.value.score],
      backgroundColor: [scoreColor.value, '#f8fafc'],
      hoverBackgroundColor: [scoreColor.value, '#f1f5f9'],
      borderWidth: 0
    }]
  };

  chartOptions.value = {
    cutout: '80%',
    plugins: { legend: { display: false }, tooltip: { enabled: false } },
    responsive: true,
    maintainAspectRatio: false
  };
};

onMounted(async () => {
  try {
    const report = await SupportFeatureService.getAudit(projectId);
    if (report) {
      auditReport.value = report;
      initChart();
    } else {
      router.push(`/project/${projectId}/accompagnement`);
    }
  } catch (e) {
    console.error("Erreur", e);
    router.push(`/project/${projectId}/accompagnement`);
  } finally {
    isLoading.value = false;
  }
});

const goBack = () => router.push(`/project/${projectId}/accompagnement`);
</script>

<template>
  <div class="min-h-screen surface-ground p-4">

    <div class="flex justify-content-between align-items-center mb-4 surface-card p-4 border-round-xl shadow-2">
      <div class="flex align-items-center gap-3">
        <Button icon="pi pi-arrow-left" text rounded severity="secondary" size="large" @click="goBack" />
        <div>
          <h1 class="m-0 text-2xl text-900 font-bold flex align-items-center gap-2">
            <i class="pi pi-verified text-primary text-3xl"></i> Rapport d'Audit IA
          </h1>
          <p class="m-0 mt-1 text-500">Validation de l'architecture Merise & BPMN</p>
        </div>
      </div>
    </div>

    <div v-if="isLoading" class="grid">
      <div class="col-12 lg:col-6"><Skeleton width="100%" height="20rem" borderRadius="16px"></Skeleton></div>
      <div class="col-12 lg:col-6"><Skeleton width="100%" height="20rem" borderRadius="16px"></Skeleton></div>
      <div class="col-12"><Skeleton width="100%" height="30rem" borderRadius="16px"></Skeleton></div>
    </div>

    <div v-else-if="auditReport" class="grid animate-fadein">

      <div class="col-12 lg:col-6">
        <div class="surface-card p-4 border-round-xl shadow-2 flex flex-column align-items-center justify-content-center h-full" style="min-height: 22rem;">
          <h2 class="text-700 font-semibold mt-0 mb-4 text-center">Taux de Conformité</h2>

          <div class="relative flex justify-content-center align-items-center" style="width: 220px; height: 220px;">
            <Chart type="doughnut" :data="chartData" :options="chartOptions" class="w-full h-full absolute z-1" />
            <div class="absolute z-2 flex flex-column align-items-center">
              <span class="text-6xl font-bold" :style="{ color: scoreColor }">{{ auditReport.score }}%</span>
            </div>
          </div>

          <div class="mt-4 px-4 py-2 border-round-3xl font-bold text-sm shadow-1"
               :class="auditReport.score >= 80 ? 'bg-green-100 text-green-700' : (auditReport.score >= 50 ? 'bg-orange-100 text-orange-700' : 'bg-red-100 text-red-700')">
            {{ scoreText }}
          </div>
        </div>
      </div>

      <div class="col-12 lg:col-6">
        <div class="surface-card p-5 border-round-xl shadow-2 flex flex-column h-full" style="min-height: 22rem;">
          <h2 class="text-700 font-semibold mt-0 mb-2 border-bottom-1 surface-border pb-3 flex align-items-center gap-2">
            <i class="pi pi-box text-2xl text-primary"></i> Livrables & Artéfacts
          </h2>
          <p class="text-500 mb-4">Générez les fichiers compatibles avec vos environnements de travail habituels.</p>

          <div class="flex flex-column gap-3 mt-auto mb-auto">
            <Button label="Exporter le modèle Merise (.mcd)" icon="pi pi-sitemap" severity="secondary" outlined size="large" class="w-full justify-content-start font-bold" disabled />
            <Button label="Exporter le processus Agile (.bpmn)" icon="pi pi-server" severity="secondary" outlined size="large" class="w-full justify-content-start font-bold" disabled />
            <Button label="Télécharger le Rapport Complet (.pdf)" icon="pi pi-file-pdf" severity="danger" size="large" class="w-full justify-content-start font-bold mt-2" disabled />
          </div>
        </div>
      </div>

      <div class="col-12 mt-3">
        <div class="surface-card p-5 border-round-xl shadow-2">
          <h2 class="text-900 font-semibold mt-0 mb-4 flex align-items-center gap-2 border-bottom-1 surface-border pb-3">
            <i class="pi pi-list text-2xl text-primary"></i> Analyse détaillée et Solutions
          </h2>

          <div v-if="pairedIssues.length > 0" class="flex flex-column gap-4">

            <div v-for="(pair, i) in pairedIssues" :key="'pair'+i" class="border-1 surface-border border-round-xl overflow-hidden shadow-1">

              <div v-if="pair.inconsistency" class="bg-red-50 p-4 border-bottom-1 border-red-100">
                <div class="flex gap-3">
                  <i class="pi pi-exclamation-triangle text-red-500 text-xl mt-1"></i>
                  <div>
                    <span class="text-red-800 font-bold block mb-1">Incohérence détectée</span>
                    <span class="text-700 line-height-3">{{ pair.inconsistency }}</span>
                  </div>
                </div>
              </div>

              <div v-if="pair.correction" class="bg-blue-50 p-4 flex align-items-start gap-3">
                <i class="pi pi-arrow-right text-blue-500 text-xl mt-1"></i>
                <div>
                  <span class="text-blue-800 font-bold block mb-1">Recommandation de l'Architecte</span>
                  <span class="text-700 line-height-3">{{ pair.correction }}</span>
                </div>
              </div>

            </div>
          </div>

          <Message v-else severity="success" :closable="false" class="mt-3 p-4 text-lg">
            <i class="pi pi-check-circle mr-2 text-2xl"></i> Votre architecture est optimale, aucune incohérence détectée !
          </Message>

        </div>
      </div>

    </div>
  </div>
</template>