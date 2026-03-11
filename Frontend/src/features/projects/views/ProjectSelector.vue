<script setup>
import { ref, onMounted, computed } from 'vue';
import {ProjectService} from '../api/ProjectService.js';
import {useRouter} from 'vue-router';
import { useConfirm } from "primevue/useconfirm";
import { useToast } from "primevue/usetoast";
import Button  from 'primevue/button';
import Card from 'primevue/card';
import ConfirmDialog from 'primevue/confirmdialog';
import Toast from 'primevue/toast';
import ProgressBar from 'primevue/progressbar';


const projets = ref([]);
const router = useRouter();
const loading = ref(false);
const confirm = useConfirm();
const toast = useToast();

const hasMoreThanFiveProjects = computed(() => projets.value.length > 5);

const displayedProjects = computed(() => {
  return hasMoreThanFiveProjects.value
      ? projets.value.slice(0, 4)
      : projets.value;
});

const goToAllProjects = () => {
  router.push({ name: 'all-projects' });
};

onMounted(async () => {
  loading.value = true;
  try {
    const data = await ProjectService.getProjects();
    projets.value = data;
    console.log("Projets récupérés avec succès", data);
  } catch (e) {

    console.error("Erreur de récupération des projets", e);

  }finally {
    loading.value = false;
  }
});

const openProjet = (idProjet) =>{
  router.push({
    name: 'projet-dashboard',
    params: { id: idProjet }
  });
};


const goToCreate = () => {
  router.push({ name: 'projet-create' });
};

const deleteProjet = (idProjet) => {
  confirm.require({
    message: 'Êtes-vous sûr de vouloir supprimer ce projet ?',
    header: 'Confirmation de suppression',
    icon: 'pi pi-exclamation-triangle',
    accept: async () => {
      try {
        await ProjectService.deleteProjet(idProjet);
        projets.value = projets.value.filter(p => p.idProjet !== idProjet);
        toast.add({ severity: 'success', summary: 'Succès', detail: 'Projet supprimé' });
      } catch (e) {
        console.error("Erreur lors de la suppression du projet", e);
        toast.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de supprimer le projet' });
      }
    }
  });
};




</script>

<template>
  <Toast />
  <ConfirmDialog />

  <div class="p-5">

    <div class="mb-5">
      <h1 class="text-3xl font-bold">Gestion des Projets</h1>
      <p class="text-600">
        Sélectionnez un projet existant ou démarrez une nouvelle analyse.
      </p>
    </div>

    <div v-if="loading">Chargement...</div>

    <div v-else class="grid">

      <!-- Carte Nouveau project -->
      <div class="col-12 md:col-6 lg:col-4">
        <Card class="h-full cursor-pointer border-2 border-primary flex align-items-center justify-content-center"
              @click="goToCreate">

          <template #content>
            <div class="text-center p-5">
              <i class="pi pi-plus text-4xl text-primary mb-3"></i>
              <h3>Nouveau Projet</h3>
              <p class="text-600">Créer une nouvelle analyse fonctionnelle</p>
            </div>
          </template>

        </Card>
      </div>

      <!-- Liste projets -->
      <div
          v-for="p in displayedProjects"
          :key="p.idProjet"
          class="col-12 md:col-6 lg:col-4"
      >
        <Card
            class="h-full cursor-pointer hover:shadow-4 transition-duration-200"
            @click="openProjet(p.idProjet)"
        >

          <template #title>
            {{ p.nom }}
          </template>

          <template #content>

            <p class="text-600 mb-3">
              {{ p.description || 'Pas de description' }}
            </p>

            <div class="mb-2 text-sm text-500">
              En cours
            </div>

            <ProgressBar :value="p.progress || 50"></ProgressBar>

          </template>

          <template #footer>
            <div class="flex justify-content-end">
              <Button
                  icon="pi pi-trash"
                  severity="danger"
                  text
                  rounded
                  @click.stop="deleteProjet(p.idProjet)"
              />
            </div>
          </template>

        </Card>
      </div>
      <!-- Carte Voir plus -->
      <div
          v-if="hasMoreThanFiveProjects"
          class="col-12 md:col-6 lg:col-4"
      >
        <Card
            class="h-full cursor-pointer border-2 border-300 flex align-items-center justify-content-center hover:shadow-4 transition-duration-200"
            @click="goToAllProjects"
        >
          <template #content>
            <div class="text-center p-5">
              <i class="pi pi-eye text-4xl text-primary mb-3"></i>
              <h3>Voir plus</h3>
              <p class="text-600">Afficher tous les projets</p>
            </div>
          </template>
        </Card>
      </div>

      <div v-if="projets.length === 0" class="col-12 text-center text-500">
        Aucun projet trouvé.
      </div>

    </div>
  </div>
</template>