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
import Drawer from 'primevue/drawer';


const projects = ref([]);
const router = useRouter();
const loading = ref(false);
const confirm = useConfirm();
const toast = useToast();
// for Drawer
const drawerVisible = ref(false);
const selectedProject = ref(null);


const hasMoreThanFiveProjects = computed(() => projects.value.length > 5);

const displayedProjects = computed(() => {
  return hasMoreThanFiveProjects.value
      ? projects.value.slice(0, 4)
      : projects.value;
});

const truncateDescription = (text, maxLength = 40) => {
  if (!text) return 'Pas de description';

  if (text.length <= maxLength) return text;

  return text.substring(0, maxLength - 3) + '...';
};

onMounted(async () => {
  loading.value = true;
  try {
    const data = await ProjectService.getProjects();
    projects.value = data.map((p) => ({
      ...p,
      id: p.id,
      name: p.name,
      project_type: p.project_type
    }));
    console.log("Projets récupérés avec succès", data);
  } catch (e) {
    console.error("Erreur de récupération des projets", e);
  }finally {
    loading.value = false;
  }
});

const formatProjectType = (type) => {
  if (!type) return 'Non défini';
  if (type === 'audit') return 'Audit';
  if (type === 'accompagnement') return 'Accompagnement';
  return type;
};

const openProjectDrawer = (project) => {
  selectedProject.value = project;
  drawerVisible.value = true;
};

const goToProject = () => {
  if (!selectedProject.value?.idProject) return;

  drawerVisible.value = false;
 console.log(selectedProject.value.idProject)
  router.push({
    name: 'project-dashboard',
    params: { id: selectedProject.value.idProject }
  });
};

const goToAllProjects = () => {
  router.push({ name: 'all-projects' });
};

const goToCreate = () => {
  router.push({ name: 'project-create' });
};

const deleteProject = (idProject) => {
  console.log("Tentative de suppression de l'ID :", idProject)
  confirm.require({
    message: 'Êtes-vous sûr de vouloir supprimer ce projet ?',
    header: 'Confirmation de suppression',
    icon: 'pi pi-exclamation-triangle',
    accept: async () => {
      try {
        await ProjectService.deleteProject(idProject);
        projects.value = projects.value.filter(p => p.idProject !== idProject);;
        toast.add({ severity: 'success', summary: 'Succès', detail: 'Projet supprimé' });

      } catch (e) {
        console.error("Erreur lors de la suppression du projet", e.title);
        console.log("ID envoyé :", idProject)
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

    <!-- HEADER -->
    <div class="mb-5">
      <h1 class="text-3xl font-bold">Gestion des Projets</h1>
      <p class="text-600">
        Sélectionnez un projet existant ou démarrez une nouvelle analyse.
      </p>
    </div>

    <!-- LOADING -->
    <div v-if="loading">Chargement...</div>

    <!-- CONTENT -->
    <div v-else class="grid">

      <!-- NEW PROJECT -->
      <div class="col-12 md:col-6 lg:col-4">
        <Card
            class="h-full cursor-pointer border-2 border-300 flex align-items-center justify-content-center hover:shadow-4"
            @click="goToCreate"
        >
          <template #content>
            <div class="text-center p-5">
              <i class="pi pi-plus text-4xl text-primary mb-3"></i>
              <h3>Nouveau Projet</h3>
              <p class="text-600">Créer une nouvelle analyse fonctionnelle</p>
            </div>
          </template>
        </Card>
      </div>

      <!-- PROJECT LIST -->
      <div
          v-for="project in displayedProjects"
          :key="project.id || project.idProject"
          class="col-12 md:col-6 lg:col-4"
      >
        <Card
            class=" project-card h-full cursor-pointer border-2 border-300 flex hover:shadow-4"
            @click="openProjectDrawer(project)"
        >
          <template #content>

            <!-- TITLE -->
            <h3 class="font-bold">
              {{ project.name || 'Sans titre' }}
            </h3>

            <!-- TYPE -->
            <p>
              <strong>Type :</strong>
              {{ formatProjectType(project.project_type || project.typeProjet) }}
            </p>

            <!-- DESCRIPTION -->
            <p>
              {{ truncateDescription(project.description) }}
            </p>

            <!-- DELETE BUTTON -->
            <div class="project-card-bottom">
              <Button
                  label="Supprimer"
                  icon="pi pi-trash"
                  severity="danger"
                  text
                  rounded
                  class="mt-2"
                  @click.stop="deleteProject(project.id || project.idProject)"
              />
            </div>

          </template>
        </Card>
      </div>

      <!-- VOIR PLUS -->
      <div
          v-if="hasMoreThanFiveProjects"
          class="col-12 md:col-6 lg:col-4"
      >
        <Card
            class="h-full cursor-pointer border-2 border-300 flex align-items-center justify-content-center hover:shadow-4"
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

      <!-- EMPTY -->
      <div v-if="displayedProjects.length === 0" class="col-12 text-center text-500">
        Aucun projet trouvé.
      </div>

    </div>

    <!-- DRAWER -->
    <Drawer
        v-model:visible="drawerVisible"
        position="right"
        class="!w-full md:!w-30rem lg:!w-[32rem]"
    >
      <div v-if="selectedProject" class="flex flex-column gap-4">

        <!-- TITLE -->
        <h2 class="text-2xl font-bold">
          {{ selectedProject.name || 'Sans titre' }}
        </h2>

        <!-- TYPE -->
        <p>
          <strong>Type :</strong>
          {{
            selectedProject.project_type === 'audit'
                ? 'Audit'
                : selectedProject.project_type === 'accompagnement'
                    ? 'Accompagnement'
                    : selectedProject.typeProjet === 'audit'
                        ? 'Audit'
                        : selectedProject.typeProjet === 'accompagnement'
                            ? 'Accompagnement'
                            : 'Non défini'
          }}
        </p>

        <!-- DESCRIPTION -->
        <p>
          {{ selectedProject.description || 'Pas de description' }}
        </p>

        <!-- BUTTON -->
        <Button
            label="Ouvrir ce projet"
            icon="pi pi-arrow-right"
            class="w-full"
            @click="goToProject"
        />
      </div>
    </Drawer>

  </div>
</template>

<style scoped>
  .project-card-bottom {
    margin-top: auto;
    display: flex;
    justify-content: flex-end;
    align-items: flex-end;
  }
</style>