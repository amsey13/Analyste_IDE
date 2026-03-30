<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { ProjectService } from '../api/ProjectService.js';
import ProjectLoadingOverlay from '../components/ProjectLoadingOverlay.vue';

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


const searchQuery = ref('');
const overlayVisible = ref(false);
const overlayProjectName = ref('');

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

const hasMoreThanFiveProjects = computed(() => projects.value.length > 5);

const displayedProjects = computed(() => {
  const query = searchQuery.value.trim().toLowerCase();

  if (query) {
    return projects.value.filter(project =>
        (project.name || '').toLowerCase().includes(query)
    );
  }

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

    projects.value = sortProjectsByLatestDate(data.map(normalizeProject));

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

const getBadgeClass = (type) => {
  if (!type) return 'badge-default';
  const t = type.toLowerCase();
  if (t.includes('audit')) return 'badge-audit';
  if (t.includes('accompagnement')) return 'badge-accompagnement';
  return 'badge-default';
};

const openProjectDrawer = async (project) => {
  selectedProject.value = project;
  drawerVisible.value = true;
};

const goToProject = async () => {
  const project = selectedProject.value;
  if (!project) return;

  const id = project.idProject || project.id;
  if (!id) {
    console.error('ID du projet manquant');
    return;
  }

  const type = project.project_type?.toLowerCase()?.trim();

  drawerVisible.value = false;
  overlayProjectName.value = project.name;
  overlayVisible.value = true;

  await new Promise(resolve => setTimeout(resolve, 4500));

  overlayVisible.value = false;

  if (type === 'audit') {
    router.push({ name: 'audit', params: { id } });
  } else if (type === 'accompagnement') {
    router.push({ name: 'accompagnement', params: { id } });
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





</script>

<template>
  <Toast />
  <ConfirmDialog />
  <ProjectLoadingOverlay
      :visible="overlayVisible"
      :projectName="overlayProjectName"
      mode="open"
  />

  <div class="projects-page">
    <div class="projects-page__header">
      <h1 class="projects-page__title">Gestion des Projets</h1>
      <p class="projects-page__subtitle">Sélectionnez un projet existant ou démarrez une nouvelle analyse.</p>
    </div>

    <div v-if="loading" class="projects-page__state">Chargement...</div>

    <div v-else>

      <div v-if="projects.length > 0" class="search-container">
        <div class="search-wrap">
          <i class="pi pi-search search-icon"></i>
          <input
              v-model="searchQuery"
              type="text"
              placeholder="Rechercher un projet..."
              class="search-input"
          />
        </div>
        <span class="count-badge">
          {{ projects.length }} projet{{ projects.length > 1 ? 's' : '' }}
        </span>
      </div>

      <div class="projects-grid" :key="projects.length">

        <Card
            class="project-card project-card--centered cursor-pointer"
            @click="goToCreate"
        >
          <template #content>
            <div class="project-card__content project-card__content--centered">
              <div class="project-card__hero-icon">
                <i class="pi pi-plus"></i>
              </div>
              <h3 class="project-card__title project-card__title--centered">Nouveau Projet</h3>
              <p class="project-card__description--centered">Créer une nouvelle analyse fonctionnelle</p>
            </div>
          </template>
        </Card>

        <Card
            v-for="(project, index) in displayedProjects"
            :key="project.idProject"
            class="project-card cursor-pointer"
            :style="{ animationDelay: `${index * 0.08}s`, opacity: 0 }"
            @click="openProjectDrawer(project)"
        >
          <template #content>
            <div class="project-card__content">
              <div class="project-card__top">
                <h3 class="project-card__title">{{ project.name }}</h3>
                <span class="badge" :class="getBadgeClass(project.project_type)">
                  {{ formatProjectType(project.project_type) }}
                </span>
              </div>

              <p class="project-card__desc">{{ truncateDescription(project.description) }}</p>

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
          </template>
        </Card>

        <Card
            v-if="hasMoreThanFiveProjects"
            class="project-card project-card--centered cursor-pointer"
            @click="goToAllProjects"
        >
          <template #content>
            <div class="project-card__content project-card__content--centered">
              <div class="project-card__hero-icon">
                <i class="pi pi-eye"></i>
              </div>
              <h3 class="project-card__title project-card__title--centered">Voir plus</h3>
              <p class="project-card__description--centered">Afficher tous les projets</p>
            </div>
          </template>
        </Card>

      </div>
    </div>

    <Drawer
        v-model:visible="drawerVisible"
        position="right"
        class="project-drawer !w-full md:!w-28rem lg:!w-[30rem]"
    >
      <div v-if="selectedProject">

        <div class="drawer__hero">
          <span class="badge" :class="getBadgeClass(selectedProject.project_type)">
          {{ formatProjectType(selectedProject.project_type) }}
        </span>
          <h2 class="drawer__hero-title">{{ selectedProject.name }}</h2>
          <p class="drawer__hero-desc">{{ selectedProject.description || 'Pas de description' }}</p>
        </div>


        <div class="drawer__actions">
          <Button
              label="Ouvrir ce projet"
              icon="pi pi-arrow-right"
              class="w-full"
              style="background: #1f355e; border-color: #1f355e;"
              @click="goToProject"
          />
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
  margin-bottom: 2rem;
  animation: fadeInDown 0.4s ease both;
}

.projects-page__title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1f355e;
  margin: 0 0 0.4rem;
  padding-left: 0.75rem;
  border-left: 4px solid #2563eb;
  line-height: 1.2;
}

.projects-page__subtitle {
  font-size: 0.9rem;
  color: #64748b;
  margin: 0.4rem 0 0 0;
  padding-left: 0.85rem;
  border-left: 4px solid transparent;
  font-style: italic;
}
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

.project-card :deep(.p-card-body) { height: 100%; padding: 1.05rem; }
.project-card :deep(.p-card-content) { height: 100%; padding: 0; }

.project-card__content { height: 100%; display: flex; flex-direction: column; }
.project-card__content--centered { justify-content: center; align-items: center; text-align: center; }
.project-card__main { min-height: 0; }

.project-card__hero-icon { margin-bottom: 1rem; font-size: 2.35rem; color: var(--primary-color); }

.project-card__title { margin: 0 0 0.75rem; font-size: 1rem; font-weight: 700; color: #1f355e; line-height: 1.3; }
.project-card__title--centered { margin-bottom: 0.5rem; }

.project-card__top { display: flex; align-items: flex-start; justify-content: space-between; margin-bottom: 0.75rem; gap: 0.5rem; }

.badge { font-size: 0.7rem; font-weight: 600; padding: 3px 10px; border-radius: 20px; white-space: nowrap; }
.badge-audit { background: #dbeafe; color: #1e40af; }
.badge-accompagnement { background: #dcfce7; color: #15803d; }
.badge-default { background: #f1f5f9; color: #475569; }

.project-card__desc { font-size: 0.82rem; color: #64748b; line-height: 1.5; flex: 1; margin-bottom: 1rem; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.project-card__description--centered { max-width: 18rem; font-size: 0.82rem; color: #64748b; }
.project-card__footer { margin-top: auto; display: flex; justify-content: flex-end; }

.drawer__hero {
  background: #1f355e;
  padding: 1.75rem 1.5rem 1.25rem;
}

.drawer__hero-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: white;
  line-height: 1.3;
  margin: 0.6rem 0 0.5rem;
}

.drawer__hero-desc {
  font-size: 0.82rem;
  color: rgba(255,255,255,0.65);
  line-height: 1.6;
  margin: 0;
}

.drawer__body {
  padding: 1.25rem 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  flex: 1;
}

.drawer__score {
  background: #eef2f7;
  border-radius: 12px;
  border: 1.5px solid #dde3ea;
  padding: 1rem 1.25rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.drawer__score-left { display: flex; flex-direction: column; gap: 2px; }
.drawer__score-label { font-size: 0.75rem; font-weight: 600; color: #64748b; text-transform: uppercase; letter-spacing: 0.05em; }
.drawer__score-sub { font-size: 0.78rem; color: #94a3b8; }
.drawer__score-right { display: flex; align-items: baseline; gap: 2px; }
.drawer__score-value { font-size: 2.5rem; font-weight: 700; color: #1f355e; line-height: 1; }
.drawer__score-pct { font-size: 1rem; color: #64748b; }

.drawer__score-bar {
  height: 4px;
  background: #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}

.drawer__score-fill {
  height: 100%;
  background: #2563eb;
  border-radius: 10px;
  transition: width 0.6s ease;
}

.drawer__actions {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  padding: 1.25rem 1.5rem;
  border-top: 1.5px solid #e8edf2;

  :deep(.p-drawer-content) {
    padding: 0 !important;
    display: flex;
    flex-direction: column;
  }

  :deep(.p-drawer) {
    border-radius: 16px 0 0 16px;
    border-left: 1.5px solid #dde3ea;
    box-shadow: -8px 0 32px rgba(31, 53, 94, 0.12);
  }

  :deep(.p-drawer-header) {
    background: #1f355e;
    padding: 1rem 1.5rem 0;
  }

  :deep(.p-drawer-close-button) {
    color: rgba(255,255,255,0.7) !important;
  }

  :deep(.p-drawer-close-button:hover) {
    color: white !important;
    background: rgba(255,255,255,0.1) !important;
  }
}

@media (max-width: 1200px) { .projects-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 768px) { .projects-grid { grid-template-columns: 1fr; } }
</style>