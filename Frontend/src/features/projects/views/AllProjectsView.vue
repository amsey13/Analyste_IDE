<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { ProjectService } from '../api/ProjectService.js';

import Card from 'primevue/card';
import Button from 'primevue/button';
import ConfirmDialog from 'primevue/confirmdialog';
import Toast from 'primevue/toast';
import Drawer from 'primevue/drawer';
import Paginator from 'primevue/paginator';
import auditProjectService from "../api/AuditProjectService.js";
const latestReport = ref(null);

const router = useRouter();
const confirm = useConfirm();
const toast = useToast();

const projects = ref([]);
const isLoading = ref(false);

const drawerVisible = ref(false);
const selectedProject = ref(null);

const first = ref(0);
const rows = ref(6);

const searchQuery = ref('');

// Normalisation des données projet pour éviter les différences de structure
const normalizeProject = (project) => {
  return {
    ...project,
    id: project.id || project.idProject,
    idProject: project.idProject || project.id,
    name: project.name || 'Sans titre',
    project_type: (project.projectType || project.project_type || project.typeProjet || project.type || '').toLowerCase(),
    description: project.description || '',
    creationDate: project.creationDate || null,
    updateDate: project.updateDate || null
  };
};

const sortProjectsByLatestDate = (projects) => {
  return [...projects].sort((a, b) => {
    const dateA = new Date(a.updateDate || a.creationDate || 0);
    const dateB = new Date(b.updateDate || b.creationDate || 0);
    return dateB - dateA;
  });
};

const filteredProjects = computed(() => {
  if (!searchQuery.value) return projects.value;

  return projects.value.filter(project =>
      (project.name || '')
          .toLowerCase()
          .includes(searchQuery.value.toLowerCase())
  );
});

const truncateDescription = (text, maxLength = 70) => {
  if (!text) return 'Pas de description';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength - 3) + '...';
};

onMounted(async () => {
  isLoading.value = true;

  try {
    const data = await ProjectService.getProjects();
    projects.value = sortProjectsByLatestDate(data.map(normalizeProject));
  } catch (error) {
    console.error('Erreur lors du chargement des projets :', error);
    toast.add({
      severity: 'error',
      summary: 'Erreur',
      detail: 'Impossible de récupérer les projets'
    });
  } finally {
    isLoading.value = false;
  }
});

const paginatedProjects = computed(() => {
  const start = first.value;
  const end = start + rows.value;
  return filteredProjects.value.slice(start, end);
});

const formatProjectType = (type) => {
  if (!type) return 'Non défini';

  const t = type.toLowerCase();
  if (t.includes('audit')) return 'Audit';
  if (t.includes('accompagnement') || t.includes('support')) return 'Accompagnement';

  return type;
};

const onPageChange = (event) => {
  first.value = event.first;
  rows.value = event.rows;
};


const openProjectDrawer = async (project) => {
  selectedProject.value = project;
  drawerVisible.value = true;
  latestReport.value = null;

  const projectId = project.idProject || project.id;

  if (project.project_type === 'audit') {
    try {
      const data = await auditProjectService.getLatestReport(projectId);
      if (data) {
        latestReport.value = data;
      }
    } catch (error) {
      console.error("Erreur lors de la récupération du rapport :", error);
    }
  }
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
  router.push('/app/projects');
};

const goToCreateProject = () => {
  router.push('/app/project/create');
};

const deleteProject = (idProject) => {
  confirm.require({
    message: 'Êtes-vous sûr de vouloir supprimer ce projet ?',
    header: 'Confirmation',
    icon: 'pi pi-exclamation-triangle',
    accept: async () => {
      try {
        await ProjectService.deleteProject(idProject);
        projects.value = projects.value.filter((project) => project.idProject !== idProject);

        if (first.value >= projects.value.length && first.value > 0) {
          first.value = Math.max(0, first.value - rows.value);
        }

        toast.add({
          severity: 'success',
          summary: 'Succès',
          detail: 'Projet supprimé'
        });
      } catch (error) {
        console.error('Erreur suppression projet', error);
        toast.add({
          severity: 'error',
          summary: 'Erreur',
          detail: 'Suppression échouée'
        });
      }
    }
  });
};


const goToLatestReport = () => {
  if (!selectedProject.value?.idProject || !latestReport.value?.id) return;
  
  drawerVisible.value = false;
  router.push({
    name: 'AuditReport',
    params: { 
      id: selectedProject.value.idProject, 
      reportId: latestReport.value.id 
    }
  });
};

</script>

<template>
  <Toast />
  <ConfirmDialog />

  <div class="projects-page">
    <div class="projects-page__header">
      <div>
        <h1 class="projects-page__title">Tous les projets</h1>
        <p class="projects-page__subtitle">Liste complète des projets disponibles.</p>
      </div>

      <div class="projects-page__actions">
        <Button label="Nouveau projet" icon="pi pi-plus" @click="goToCreateProject" />
        <Button label="Retour" icon="pi pi-arrow-left" outlined @click="goBack" />
      </div>
    </div>

    <div v-if="isLoading" class="projects-page__state">
      Chargement...
    </div>

    <div v-else-if="projects.length === 0" class="projects-page__state">
      Aucun projet disponible.
    </div>

    <div v-else>

      <div class="search-container">
        <i class="pi pi-search search-icon"></i>
        <input
            v-model="searchQuery"
            type="text"
            placeholder="Nom de Projet ..."
            class="search-input"
        />
      </div>

      <div class="projects-grid">
        <Card
            v-for="(project, index) in paginatedProjects"
            :key="project.idProject"
            :style="{ animationDelay: `${index * 0.08}s`, opacity: 0 }"
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
      </div>

      <div v-if="projects.length > rows" class="projects-page__paginator">
        <Paginator
            :first="first"
            :rows="rows"
            :totalRecords="filteredProjects.length"
            :rowsPerPageOptions="[6, 9, 12]"
            @page="onPageChange"
        />
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

      <div class="flex flex-column gap-3 mt-4">
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
        
        <small v-if="latestReport" class="text-center text-500 italic">
          Dernière analyse : {{ latestReport.score }}% de cohérence
        </small>
      </div>
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
  --title-size: 2rem;
  --subtitle-size: 1rem;
  --card-title-size: 1rem;
  --card-text-size: 0.92rem;
  --section-spacing: 1.35rem;
  padding: calc(1.5rem * var(--page-scale));
  padding-bottom: 0.8rem;
}

.projects-page__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
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

.projects-page__actions {
  display: flex;
  gap: 0.675rem;
  flex-wrap: wrap;
}

.projects-page__state {
  padding: 1rem 0;
  font-size: 0.95rem;
  color: #5f6f86;
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

.project-card__main {
  min-height: 0;
}

.project-card__title {
  margin: 0 0 0.9rem;
  font-size: var(--card-title-size);
  line-height: 1.25;
  font-weight: 700;
  color: #27364f;
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

.project-card__footer {
  margin-top: auto;
  display: flex;
  justify-content: flex-end;
  align-items: flex-end;
}

.projects-page__paginator {
  margin-top: 2rem;
  display: flex;
  justify-content: center;
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
  .projects-page__header {
    flex-direction: column;
  }

  .projects-grid {
    grid-template-columns: 1fr;
  }

  .project-card {
    height: auto;
    min-height: 190px;
  }
}
.search-container {
  position: relative;
  width: 100%;
  max-width: 350px;
  margin-left: auto;
  margin-bottom: 30px;
}

.search-input {
  width: 100%;
  padding: 6px 14px 6px 45px; /* space for icon */
  font-size: 0.95rem;
  border-radius: 12px;
  border: 2px solid #d0d7de;
  outline: none;
  transition: all 0.2s ease;
}

.search-input:focus {
  border-color: #94a3b8;
  box-shadow: 0 0 0 2px rgba(148, 163, 184, 0.2);
}

.search-icon {
  position: absolute;
  left: 14px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 20px;
  color: #6b7280;
}
</style>

<style scoped>
/* ... ton style existant ... */

@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-12px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

.projects-page__header {
  animation: fadeInDown 0.4s ease both;
}

.search-container {
  animation: fadeInDown 0.4s 0.1s ease both;
}

.project-card {
  animation: fadeInUp 0.4s ease both;
  opacity: 0;
}
</style>