<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ProjectService from '../api/ProjectService'

const router = useRouter()

const projects = ref([])
const loading = ref(false)

const fetchProjects = async () => {
  loading.value = true
  try {
    const response = await ProjectService.getProjects()
    projects.value = response.data || []
  } catch (error) {
    console.error('Erreur lors du chargement des projets :', error)
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/app/projects')
}

const goToCreateProject = () => {
  router.push('/app/project/create')
}

const goToProject = (projectId) => {
  router.push(`/app/project/${projectId}`)
}

const deleteProject = async (projectId) => {
  try {
    await ProjectService.deleteProject(projectId)
    projects.value = projects.value.filter((p) => p.id !== projectId)
  } catch (error) {
    console.error('Erreur lors de la suppression du projet :', error)
  }
}

onMounted(() => {
  fetchProjects()
})
</script>

<template>
  <div class="all-projects-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">Tous les projets</h1>
        <p class="page-subtitle">Liste complète de vos projets existants.</p>
      </div>

      <div class="header-actions">
        <button class="secondary-btn" @click="goBack">
          Retour
        </button>
        <button class="primary-btn" @click="goToCreateProject">
          Nouveau Projet
        </button>
      </div>
    </div>

    <div v-if="loading" class="loading-state">
      Chargement des projets...
    </div>

    <div v-else-if="projects.length === 0" class="empty-state">
      <div class="empty-card">
        <h3>Aucun projet trouvé</h3>
        <p>Vous n’avez pas encore créé de projet.</p>
        <button class="primary-btn" @click="goToCreateProject">
          Créer un projet
        </button>
      </div>
    </div>

    <div v-else class="projects-grid">
      <div
          v-for="project in projects"
          :key="project.id"
          class="project-card"
          @click="goToProject(project.id)"
      >
        <h3>{{ project.name || 'Sans titre' }}</h3>
        <p>{{ project.description || 'Pas de description' }}</p>

        <div class="project-status">En cours</div>

        <div class="progress-bar">
          <div
              class="progress-fill"
              :style="{ width: `${project.progress || 50}%` }"
          ></div>
        </div>

        <span class="progress-text">{{ project.progress || 50 }}%</span>

        <button class="delete-btn" @click.stop="deleteProject(project.id)">
          <i class="pi pi-trash"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.all-projects-page {
  padding: 2rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 2rem;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #16325c;
  margin-bottom: 0.5rem;
}

.page-subtitle {
  color: #5b6b82;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.primary-btn,
.secondary-btn {
  border: none;
  border-radius: 10px;
  padding: 0.8rem 1.1rem;
  font-size: 0.95rem;
  cursor: pointer;
  transition: 0.2s ease;
}

.primary-btn {
  background: #2563eb;
  color: white;
}

.primary-btn:hover {
  transform: translateY(-1px);
}

.secondary-btn {
  background: #e8eef8;
  color: #16325c;
}

.secondary-btn:hover {
  transform: translateY(-1px);
}

.loading-state {
  padding: 2rem 0;
  color: #5b6b82;
}

.empty-state {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

.empty-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 16px;
  padding: 2rem;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.empty-card h3 {
  color: #2d3e5e;
  margin-bottom: 0.75rem;
}

.empty-card p {
  color: #6b7a90;
  margin-bottom: 1.25rem;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.project-card {
  background: #fff;
  border-radius: 16px;
  padding: 1.5rem;
  min-height: 210px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.project-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(0, 0, 0, 0.08);
}

.project-card h3 {
  font-size: 1.2rem;
  color: #2d3e5e;
  margin-bottom: 0.75rem;
}

.project-card p {
  color: #6b7a90;
  margin-bottom: 1rem;
}

.project-status {
  font-size: 0.95rem;
  color: #6b7a90;
  margin-bottom: 0.5rem;
}

.progress-bar {
  width: 100%;
  height: 16px;
  background: #dbe3ef;
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 0.4rem;
}

.progress-fill {
  height: 100%;
  background: #0b5ed7;
  border-radius: 10px;
}

.progress-text {
  font-size: 0.85rem;
  font-weight: 600;
  color: #0b5ed7;
}

.delete-btn {
  position: absolute;
  bottom: 1rem;
  right: 1rem;
  border: none;
  background: transparent;
  color: #ff5b5b;
  cursor: pointer;
  font-size: 1rem;
}

@media (max-width: 1100px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .projects-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 700px) {
  .projects-grid {
    grid-template-columns: 1fr;
  }

  .header-actions {
    width: 100%;
  }
}
</style>