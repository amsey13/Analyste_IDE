<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import { ProjectService } from '../features/projects/api/ProjectService.js';
import Skeleton from 'primevue/skeleton';
import ActorManager from '../features/projects/components/ActorManager.vue';
import UserStoryManager from '../features/projects/components/UserStoryManager.vue';
import BpmnModeler from '../features/projects/components/BpmnModeler.vue';
import DictionaryManager from '../features/projects/components/DictionaryManager.vue';
import McdManager from '../features/projects/components/McdManager.vue';
import BusinessRulesManager from '../features/projects/components/BusinessRulesManager.vue';


import Tabs from 'primevue/tabs';
import TabList from 'primevue/tablist';
import Tab from 'primevue/tab';
import TabPanels from 'primevue/tabpanels';
import TabPanel from 'primevue/tabpanel';

const route = useRoute();
const projectId = route.params.id;

const project = ref(null);
const isLoading = ref(true);
const coverageBadgeClass = computed(() => {
  if (!project.value) return 'bg-gray-100 text-gray-800';

  const score = project.value.coverageScore || 0;

  const thresholds = [
    { max: 40, classes: 'bg-red-100 text-red-800' },
    { max: 70, classes: 'bg-orange-100 text-orange-800' },
    { max: 100, classes: 'bg-green-100 text-green-800' }
  ];
  const matchedThreshold = thresholds.find(t => score <= t.max);
  return matchedThreshold ? matchedThreshold.classes : thresholds[2].classes;
});

const loadProject = async () => {
  try {
    project.value = await ProjectService.getProjectById(projectId);

    // Sécurité réactivité
    if (!project.value.actors) project.value.actors = [];
    if (!project.value.userStories) project.value.userStories = [];
    if (!project.value.dictionaryEntries) project.value.dictionaryEntries = []; // <- NOUVEAU
  } catch (error) {
    console.error("Erreur lors du chargement du projet", error);
  } finally {
    isLoading.value = false;
    refreshKey.value += 1;
  }
};

onMounted(() => {
  loadProject();
});
</script>

<template>
  <div class="flex flex-column h-screen p-4 surface-ground">

    <div v-if="project" class="flex justify-content-between align-items-center mb-4 surface-0 p-4 border-round-xl shadow-1">
      <div>
        <h1 class="m-0 text-2xl text-900">{{ project.name }}</h1>
        <p class="m-0 mt-1 text-500">Espace de modélisation</p>
      </div>
      <div class="text-right">
        <span :class="['inline-block px-4 py-2 border-round-3xl font-bold text-lg', coverageBadgeClass]">
          Couverture : {{ project.coverageScore }}%
        </span>
      </div>
    </div>

    <div v-if="project" class="flex-1 flex flex-column surface-0 border-round-xl shadow-1 overflow-hidden">
      <Tabs value="0" class="flex-1 flex flex-column h-full">

        <TabList>
          <Tab value="0">1. Acteurs & User Stories</Tab>
          <Tab value="1">2. Modélisation BPMN</Tab>
          <Tab value="2">3. Règles de Gestion</Tab>
          <Tab value="3">4. Dictionnaire de Données</Tab>
          <Tab value="4">5. Modèle Conceptuel (MCD)</Tab>
        </TabList>

        <TabPanels class="flex-1 p-0 overflow-y-auto">

          <TabPanel value="0" class="h-full">
            <div class="flex gap-4 p-4 h-full">
              <div class="flex-1">
                <ActorManager
                    :projectId="projectId"
                    :actors="project.actors"
                    :userStories="project.userStories"
                    @update:actors="project.actors = $event"
                    @update:userStories="project.userStories = $event"
                />
              </div>
              <div class="flex-1 border-left-1 surface-border pl-4">
                <UserStoryManager
                    :projectId="projectId"
                    :userStories="project.userStories"
                    :actors="project.actors"
                    @update:userStories="project.userStories = $event"
                />
              </div>
            </div>
          </TabPanel>

          <TabPanel value="1" class="h-full">
            <div v-if="project.actors.length === 0" class="flex flex-column align-items-center justify-content-center h-full surface-50 border-round-xl m-4 border-1 surface-border">
              <i class="pi pi-exclamation-triangle text-orange-500 text-6xl mb-4"></i>
              <h2 class="text-900 font-bold mb-2">Modélisation bloquée</h2>
              <p class="text-600 text-center max-w-20rem">
                Il est impossible de dessiner un processus sans savoir qui l'exécute. Veuillez ajouter au moins un acteur dans l'onglet "Acteurs & User Stories".
              </p>
            </div>

            <div v-else class="h-full p-4">
              <BpmnModeler
                  :projectId="projectId"
                  :initialXml="project.bpmnXml"
                  :actors="project.actors"
                  :userStories="project.userStories"
                  @update:coverageScore="project.coverageScore = $event"
              />
            </div>
          </TabPanel>
          <TabPanel value="2" class="h-full">
            <div class="h-full p-4 overflow-y-auto">
              <BusinessRulesManager
                  :projectId="projectId"
                  @refresh="loadProject"
              />
            </div>
          </TabPanel>
          <TabPanel value="3" class="h-full">
            <div class="h-full p-4 overflow-y-auto">
              <DictionaryManager
                  :key="'dict-' + refreshKey"
                  :projectId="projectId"
                  :entries="project.dictionaryEntries"
                  @refresh="loadProject"
              />
            </div>
          </TabPanel>
          <TabPanel value="4" class="h-full">
            <div class="h-full p-4 overflow-y-auto">
              <div v-if="project.dictionaryEntries.length === 0" class="flex flex-column align-items-center justify-content-center h-full surface-50 border-round-xl border-1 surface-border p-6 text-center">
                <i class="pi pi-database text-orange-500 text-6xl mb-4"></i>
                <h2 class="text-900 font-bold mb-2">Dictionnaire vide</h2>
                <p class="text-600 max-w-20rem">
                  Vous devez d'abord définir des entités dans le <b>Dictionnaire de Données</b> avant de pouvoir créer des relations et générer le MCD.
                </p>
              </div>

              <McdManager
                  v-else
                  :key="'dict-' + refreshKey"
                  :projectId="projectId"
                  :entries="project.dictionaryEntries"
                  @refresh="loadProject"
              />
            </div>
          </TabPanel>
        </TabPanels>
      </Tabs>
    </div>

    <div v-else-if="isLoading" class="flex-1 flex align-items-center justify-content-center">
      <Skeleton width="100%" height="100%" borderRadius="16px"></Skeleton>
    </div>
    <div v-else class="flex-1 flex align-items-center justify-content-center">
      <h2 class="text-red-500">Impossible de charger le projet.</h2>
    </div>

  </div>
</template>