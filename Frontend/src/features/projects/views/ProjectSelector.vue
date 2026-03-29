<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { ProjectService } from '../api/ProjectService.js';

import Button from 'primevue/button';
import Card from 'primevue/card';
import ConfirmDialog from 'primevue/confirmdialog';
import Toast from 'primevue/toast';
import Drawer from 'primevue/drawer';
import auditProjectService from "../api/AuditProjectService.js";


const projects = ref([]);
const router = useRouter();
const loading = ref(false);
const confirm = useConfirm();
const toast = useToast();

const drawerVisible = ref(false);
const selectedProject = ref(null);

const latestReport = ref(null);



const normalizeProject = (project) => ({


  ...project,
  id: project.id || project.idProject,
  idProject: project.idProject || project.id,
  name: project.name || 'Sans titre',
  project_type: project.projectType || '',
  description: project.description || ''
});

const hasMoreThanFiveProjects = computed(() => projects.value.length > 5);

const displayedProjects = computed(() => {
  return hasMoreThanFiveProjects.value ? projects.value.slice(0, 4) : projects.value;
});

const truncateDescription = (text, maxLength = 70) => {
  if (!text) return 'Pas de description';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength - 3) + '...';
};

onMounted(async () => {
  loading.value = true;

  try {
    const data = await ProjectService.getProjects();
    console.log('Données brutes reçues du Backend :', data);
    if (data.length > 0) {
      console.log('Clés du premier projet :', Object.keys(data[0]));
    }

    projects.value = data.map(normalizeProject);

    console.log('Projets après normalisation :', projects.value);
  } catch (error) {
    console.error('Erreur de récupération des projets', error);
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de récupérer les projets'
    });
  } finally {
    loading.value = false;
  }
});

const formatProjectType = (type) => {
  if (!type) return 'Non défini';
  const lowerType = type.toLowerCase();

  if (lowerType === 'audit') {
    console.log('Enfaite cest un projet ', lowerType);
    return 'Audit';
  }
  if (lowerType === 'accompagnement') {
    console.log('Enfaite cest un projet ', lowerType);
    return 'Accompagnement';
  }
  return type;
};

const openProjectDrawer = async (project) => {
  selectedProject.value = project;
  drawerVisible.value = true;
  latestReport.value = null; // Reset

  const id = project.idProject || project.id;
  
  if (project.project_type?.toLowerCase() === 'audit') {
    try {
      const data = await auditProjectService.getLatestReport(id);
      if (data) {
        latestReport.value = data;
      }
    } catch (error) {
      console.error("Pas de rapport trouvé pour ce projet");
    }
  }
};

const goToProject = () => {
  const project = selectedProject.value;

  console.log('PROJECT:', project);

  if (!project) return;

  const id = project.idProject || project.id;

  if (!id) {
    console.error('ID du projet manquant ');
    return;
  }

  const type = project.project_type?.toLowerCase()?.trim();

  console.log('TYPE:', type);
  console.log('ID:', id);

  drawerVisible.value = false;

  if (type === 'audit') {
    router.push({
      name: 'audit',
      params: { id }
    });
  } else if (type === 'accompagnement') {
    router.push({
      name: 'accompagnement',
      params: { id }
    });
  } else {
    console.warn('Type inconnu:', type);
  }
};

const goToAllProjects = () => {
  router.push({ name: 'all-projects' });
};

const goToCreate = () => {
  router.push({ name: 'project-create' });
};

const deleteProject = (idProject) => {
  confirm.require({
    message: 'Êtes-vous sûr de vouloir supprimer ce projet ?',
    header: 'Confirmation de suppression',
    icon: 'pi pi-exclamation-triangle',
    accept: async () => {
      try {
        await ProjectService.deleteProject(idProject);
        projects.value = projects.value.filter((project) => project.idProject !== idProject);

        toast.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Projet supprimé'
        });
      } catch (error) {
        console.error('Erreur lors de la suppression du projet', error);
        toast.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Impossible de supprimer le projet'
        });
      }
    }
  });
};



const goToLatestReport = () => {
  if (!selectedProject.value || !latestReport.value) return;
  drawerVisible.value = false;
  router.push({
    name: 'AuditReport', // Vérifie que ce nom correspond à ta route dans router/index.js
    params: { 
      id: selectedProject.value.idProject || selectedProject.value.id,
      reportId: latestReport.value.id 
    }
  });
};

</script>

<template>
  <Toast />
  <ConfirmDialog />

  <div class="projects-page projects-page--selector">
    <div class="projects-page__header">
      <div>
        <h1 class="projects-page__title">Gestion des Projets</h1>
        <p class="projects-page__subtitle">
          Sélectionnez un projet existant ou démarrez une nouvelle analyse.
        </p>
      </div>
    </div>

    <div v-if="loading" class="projects-page__state">
      Chargement...
    </div>

    <div v-else class="projects-grid">
      <Card
          class="project-card project-card--centered cursor-pointer border-2 border-300 hover:shadow-4"
          @click="goToCreate"
      >
        <template #content>
          <div class="project-card__content project-card__content--centered">
            <div class="project-card__hero-icon">
              <i class="pi pi-plus"></i>
            </div>
            <h3 class="project-card__title project-card__title--centered">Nouveau Projet</h3>
            <p class="project-card__description project-card__description--centered">
              Créer une nouvelle analyse fonctionnelle
            </p>
          </div>
        </template>
      </Card>

      <Card
          v-for="project in displayedProjects"
          :key="project.idProject"
          class="project-card cursor-pointer border-2 border-300 hover:shadow-4"
          @click="openProjectDrawer(project)"
      >
        <template #content>
          <div class="project-card__content">
            <div class="project-card__main">
              <h3 class="project-card__title">{{ project.name }}</h3>

              <p class="project-card__meta">
                <strong>Type :</strong>
                {{ formatProjectType(project.project_type) }}
              </p>

              <p class="project-card__description">
                {{ truncateDescription(project.description) }}
              </p>
            </div>

            <div class="project-card__footer">
              <Button
                  label="Supprimer"
                  icon="pi pi-trash"
                  severity="danger"
                  text
                  rounded
                  @click.stop="deleteProject(project.idProject)"
              />
            </div>
          </div>
        </template>
      </Card>

      <Card
          v-if="hasMoreThanFiveProjects"
          class="project-card project-card--centered cursor-pointer border-2 border-300 hover:shadow-4"
          @click="goToAllProjects"
      >
        <template #content>
          <div class="project-card__content project-card__content--centered">
            <div class="project-card__hero-icon">
              <i class="pi pi-eye"></i>
            </div>
            <h3 class="project-card__title project-card__title--centered">Voir plus</h3>
            <p class="project-card__description project-card__description--centered">
              Afficher tous les projets
            </p>
          </div>
        </template>
      </Card>

      <div v-if="displayedProjects.length === 0" class="projects-page__state projects-page__state--full">
        Aucun projet trouvé.
      </div>
    </div>

    <Drawer
        v-model:visible="drawerVisible"
        position="right"
        class="project-drawer !w-full md:!w-28rem lg:!w-[30rem]"
    >
      <div v-if="selectedProject" class="project-drawer__content">
        <h2 class="project-drawer__title">{{ selectedProject.name }}</h2>

        <p class="project-drawer__text">
          <strong>Type :</strong>
          {{ formatProjectType(selectedProject.project_type) }}
        </p>

        <p class="project-drawer__text">
          {{ selectedProject.description || 'Pas de description' }}
        </p>

        <Button
            label="Ouvrir ce projet"
            icon="pi pi-arrow-right"
            class="w-full"
            @click="goToProject"
        />

        <Button
          v-if="latestReport"
          label="Voir le dernier audit"
          icon="pi pi-history"
          severity="secondary"
          outlined
          class="w-full"
          @click="goToLatestReport"
        />


      </div>
    </Drawer>
  </div>
</template>

<style scoped>
.projects-page {
  --page-scale: 0.9;
  --page-padding: 1.35rem;
  --grid-gap: 1.125rem;
  --card-radius: 14px;
  --card-height: 210px;
  --card-padding: 1.05rem;
  --card-title-size: 1rem;
  --card-text-size: 0.92rem;
  padding: calc(1.5rem * var(--page-scale));
}

.projects-page__header {
  margin-bottom: 1.35rem;
}

.projects-page__title {
  margin: 0;
  font-size: 2.2rem;
  line-height: 1.15;
  font-weight: 700;
  color: #1f355e;
}

.projects-page__subtitle {
  margin: 0.45rem 0 0;
  font-size: 1rem;
  color: #5f6f86;
}

.projects-page__state {
  padding: 1rem 0;
  font-size: 0.95rem;
  color: #5f6f86;
}

.projects-page__state--full {
  grid-column: 1 / -1;
  text-align: center;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: var(--grid-gap);
  align-items: stretch;
}

.project-card {
  height: var(--card-height);
  border-radius: var(--card-radius);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.project-card:hover {
  transform: translateY(-2px);
}

.project-card :deep(.p-card-body) {
  height: 100%;
  padding: var(--card-padding);
}

.project-card :deep(.p-card-content) {
  height: 100%;
  padding: 0;
}

.project-card__content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.project-card__content--centered {
  justify-content: center;
  align-items: center;
  text-align: center;
}

.project-card__main {
  min-height: 0;
}

.project-card__hero-icon {
  margin-bottom: 1rem;
  font-size: 2.35rem;
  color: var(--primary-color);
}

.project-card__title {
  margin: 0 0 0.9rem;
  font-size: var(--card-title-size);
  line-height: 1.25;
  font-weight: 700;
  color: #27364f;
}

.project-card__title--centered {
  margin-bottom: 0.7rem;
}

.project-card__meta {
  margin: 0 0 0.8rem;
  font-size: var(--card-text-size);
  color: #374a67;
}

.project-card__description {
  margin: 0;
  font-size: var(--card-text-size);
  line-height: 1.45;
  color: #4e617d;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
}

.project-card__description--centered {
  max-width: 18rem;
}

.project-card__footer {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
}

.project-drawer__content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.project-drawer__title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f355e;
}

.project-drawer__text {
  margin: 0;
  font-size: 0.95rem;
  line-height: 1.5;
  color: #374a67;
}

@media (max-width: 1200px) {
  .projects-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .projects-grid {
    grid-template-columns: 1fr;
  }

  .project-card {
    height: auto;
    min-height: 190px;
  }
}
</style>
