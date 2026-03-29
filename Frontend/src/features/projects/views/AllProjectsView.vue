<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { ProjectService } from '../api/ProjectService.js';

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

const getBadgeClass = (type) => {
  if (!type) return 'badge-default';
  const t = type.toLowerCase();
  if (t.includes('audit')) return 'badge-audit';
  if (t.includes('accompagnement')) return 'badge-accompagnement';
  return 'badge-default';
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

    <div v-if="isLoading" class="projects-page__state">Chargement...</div>

    <div v-else-if="projects.length === 0" class="projects-page__state">Aucun projet disponible.</div>

    <div v-else>

      <div class="search-container">
        <div class="search-wrap">
          <i class="pi pi-search search-icon"></i>
        <input
            v-model="searchQuery"
            type="text"
            placeholder="Rechercher un projet"
            class="search-input"
        />
        </div>
        <span class="count-badge">
          {{ filteredProjects.length }} projet{{filteredProjects.length > 1 ? 's' : ''}}
        </span>
      </div>

      <div class="projects-grid" :key="projects.length">        <div
            v-for="(project, index) in paginatedProjects"
            :key="project.idProject"
            :style="{ animationDelay: `${index * 0.08}s`, opacity: 0 }"
            class="project-card"
            @click="openProjectDrawer(project)"
        >
            <div class="project-card__top">
                <h3 class="project-card__title">{{ project.name }}</h3>
                <span class="badge" :class="getBadgeClass(project.project_type)">
                  {{ formatProjectType(project.project_type) }}
                </span>
            </div>

        <p class="project-card__desc">{{truncateDescription(project.description)}}</p>
            <div class="project-card__footer">
                <Button
                    label="Supprimer"
                    icon="pi pi-trash"
                    severity="danger"
                    text
                    rounded
                    size="small"
                    @click.stop="deleteProject(project.idProject)"
                />
            </div>
        </div>
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
      <span class="badge" :class="getBadgeClass(selectedProject.project_type)">        {{ formatProjectType(selectedProject.project_type) }}
      </span>
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
@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-12px); }
  to   { opacity: 1; transform: translateY(0); }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

.projects-page { padding: 2rem; background: #eef2f7; min-height: 100vh; }

.projects-page__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  animation: fadeInDown 0.4s ease both;
}

.projects-page__title { font-size: 1.8rem; font-weight: 700; color: #1f355e; margin: 0; }
.projects-page__subtitle { font-size: 0.9rem; color: #64748b; margin-top: 4px; }
.projects-page__actions { display: flex; gap: 10px; }
.projects-page__state { padding: 1rem 0; font-size: 0.95rem; color: #64748b; }

.search-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
  animation: fadeInDown 0.4s 0.1s ease both;
}

.search-wrap { position: relative; max-width: 340px; width: 100%; }
.search-input {
  width: 100%;
  padding: 10px 16px 10px 40px;
  border-radius: 10px;
  border: 1.5px solid #e2e8f0;
  background: white;
  font-size: 0.875rem;
  outline: none;
  transition: border 0.2s, box-shadow 0.2s;
}
.search-input:focus { border-color: #94a3b8; box-shadow: 0 0 0 3px rgba(148,163,184,0.15); }
.search-icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); color: #94a3b8; font-size: 16px; }

.count-badge { font-size: 0.8rem; color: #64748b; background: #f1f5f9; padding: 4px 12px; border-radius: 20px; border: 1px solid #e2e8f0; }

.projects-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
}

.project-card {
  background: #ffffff;
  border-radius: 14px;
  border: 1.5px solid #e8edf2;
  padding: 1.25rem;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  min-height: 180px;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s;
  animation: fadeInUp 0.4s ease both;
}
.project-card:hover { transform: translateY(-3px); box-shadow: 0 8px 24px rgba(31,53,94,0.1); border-color: #c7d2dc; }

.project-card__top { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 0.75rem; gap: 0.5rem; }
.project-card__title { font-size: 1rem; font-weight: 700; color: #1f355e; line-height: 1.3; }

.badge { font-size: 0.7rem; font-weight: 600; padding: 3px 10px; border-radius: 20px; white-space: nowrap; }
.badge-audit { background: #dbeafe; color: #1e40af; }
.badge-accompagnement { background: #dcfce7; color: #15803d; }
.badge-default { background: #f1f5f9; color: #475569; }

.project-card__desc { font-size: 0.82rem; color: #64748b; line-height: 1.5; flex: 1; margin-bottom: 1rem; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.project-card__footer { margin-top: auto; display: flex; justify-content: flex-end; }

.projects-page__paginator { margin-top: 2rem; display: flex; justify-content: center; }

.project-drawer__content { display: flex; flex-direction: column; gap: 1rem; }
.project-drawer__title { font-size: 1.5rem; font-weight: 700; color: #1f355e; margin: 0; }
.project-drawer__text { font-size: 0.95rem; line-height: 1.5; color: #374a67; margin: 0; }

@media (max-width: 1200px) { .projects-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 768px) { .projects-grid { grid-template-columns: 1fr; } .projects-page__header { flex-direction: column; } }
</style>
