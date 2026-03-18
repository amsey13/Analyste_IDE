<script setup>
import { ref, computed, onMounted } from 'vue';
import { ProjectService } from '../api/ProjectService.js';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';

import Card from 'primevue/card';
import Button from 'primevue/button';
import ConfirmDialog from 'primevue/confirmdialog';
import Toast from 'primevue/toast';
import Drawer from 'primevue/drawer';
import Paginator from 'primevue/paginator';


const router = useRouter();
const confirm = useConfirm();
const toast = useToast();

const projects = ref([])
const isLoading = ref(false)

const drawerVisible = ref(false);
const selectedProject = ref(null);

const first = ref(0)
const rows = ref(6)

onMounted(async () => {
  isLoading.value = true
  try {
    const data = await ProjectService.getProjects()
    projects.value = data.map((p) => ({
      ...p,
      id: p.id || p.idProject,
      name: p.name,
      project_type: p.project_type || p.typeProjet || p.type,
      description: p.description || ''
    }));
    if(data.length > 0){
      console.log("Structure du premier projet :", data[0]);
      console.log("Clés disponibles :", Object.keys(data[0]));
    }
  } catch (error) {
    console.error('Erreur lors du chargement des projets :', error);
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de récupérer les projets'
    });
  } finally {
    isLoading.value = false
  }
});

const paginatedProjects = computed(() => {
  return projects.value.slice(first.value, first.value + rows.value);
});

const formatProjectType = (type) => {
  if (!type) return 'Non défini';
  if (type === 'audit') return 'Audit';
  if (type === 'accompagnement') return 'Accompagnement';
  return type;
};

const onPageChange = (event) => {
  first.value = event.first;
  rows.value = event.rows;
};

const openProjectDrawer = (project) => {
  selectedProject.value = project;
  drawerVisible.value = true;
};

const goToProject = () => {
  if (!selectedProject.value?.idProject) return;

  drawerVisible.value = false;

  router.push({
    name: 'project-dashboard',
    params: { id: selectedProject.value.idProject }
  });
};

const goBack = () => {
  router.push('/app/projects')
}

const goToCreateProject = () => {
  router.push('/app/project/create')
}

const deleteProject = (id) => {
  confirm.require({
    message: 'Êtes-vous sûr de vouloir supprimer ce projet ?',
    header: 'Confirmation',
    icon: 'pi pi-exclamation-triangle',
    accept: async () => {
      try {
        await ProjectService.deleteProject(id);
        projects.value = projects.value.filter((p) => p.id !== id);

        if (first.value >= projects.value.length && first.value > 0) {
          first.value = Math.max(0, first.value - rows.value);
        }

        toast.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Projet supprimé'
        });
      } catch (e) {
        console.error('Erreur suppression projet', e);
        toast.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Suppression échouée'
        });
      }
    }
  });
};
</script>

<template>
  <Toast />
  <ConfirmDialog />

  <div class="all-projects-view">
    <div class="header">
      <div>
        <h1>Tous les projets</h1>
        <p>Liste complète des projets disponibles.</p>
      </div>

      <div class="header-actions">
        <Button label="Nouveau projet" icon="pi pi-plus" @click="goToCreateProject" />
        <Button label="Retour" icon="pi pi-arrow-left" outlined @click="goBack" />
      </div>
    </div>

    <div v-if="isLoading">
      Chargement...
    </div>

    <div v-else-if="projects.length === 0">
      Aucun projet disponible.
    </div>

    <div v-else>
      <div class="projects-grid">
        <Card
            v-for="project in paginatedProjects"
            :key="project.idProjet"
            class="project-card col-12 md:col-6 lg:col-4 h-full cursor-pointer"
            @click="openProjectDrawer(project)"
        >
          <template #content>
            <h3 class="font-bold mb-3">
              {{ project.name }}
            </h3>

            <p class="mb-3">
              <strong>Type :</strong>
              {{ formatProjectType(project.project_type) }}
            </p>

            <p class="mb-3">
              {{ project.description || 'Pas de description' }}
            </p>

            <Button
                label="Supprimer"
                icon="pi pi-trash"
                severity="danger"
                text
                rounded
                class="mt-2"
                @click.stop="deleteProject(project.idProject)"
            />
          </template>
        </Card>
      </div>

      <div v-if="projects.length > rows" class="paginator-wrapper">
        <Paginator
            :first="first"
            :rows="rows"
            :totalRecords="projects.length"
            :rowsPerPageOptions="[6, 9, 12]"
            @page="onPageChange"
        />
      </div>

    </div>

    <Drawer
        v-model:visible="drawerVisible"
        position="right"
        class="!w-full md:!w-30rem lg:!w-[32rem]"
    >
      <div v-if="selectedProject" class="flex flex-column gap-4">
        <h2 class="text-2xl font-bold">
          {{ selectedProject.name }}
        </h2>

        <p>
          <strong>Type :</strong>
          {{ formatProjectType(selectedProject.project_type) }}
        </p>

        <p>
          {{ selectedProject.description || 'Pas de description' }}
        </p>

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
.all-projects-view {
  padding: 2rem;
}

.header {
  margin-bottom: 1.5rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.project-card {
  cursor: pointer;
  border-radius: 12px;
  min-height: 230px;
}

.project-card:hover {
  transform: translateY(-2px);
  transition: 0.2s ease;
}

.paginator-wrapper {
  margin-top: 1.5rem;
  display: flex;
  justify-content: center;
}

.drawer-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
</style>