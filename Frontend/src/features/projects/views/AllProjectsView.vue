<script setup>
import { ref, computed, onMounted } from 'vue'
import { ProjectService } from '../api/ProjectService.js'
import Card from 'primevue/card'
import ProgressBar from 'primevue/progressbar'
import Paginator from 'primevue/paginator'
import { useRouter } from 'vue-router'

const router = useRouter()

const projects = ref([])
const isLoading = ref(false)

const first = ref(0)
const rows = ref(6)

onMounted(async () => {
  isLoading.value = true
  try {
    const data = await ProjectService.getProjects()
    projects.value = data
  } catch (error) {
    console.error('Erreur lors du chargement des projets :', error)
  } finally {
    isLoading.value = false
  }
});

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


const paginatedProjects = computed(() => {
  return projects.value.slice(first.value, first.value + rows.value)
})

const onPageChange = (event) => {
  first.value = event.first
  rows.value = event.rows
}

const openProject = (projectId) => {
  router.push({
    name: 'projet-dashboard',
    params: { id: projectId }
  })
}
</script>

<template>
  <div class="all-projects-view">

    <div class="header">
      <h1>Tous les projets</h1>
      <p>Liste complète des projets disponibles.</p>
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
            class="project-card"
            @click="openProject(project.idProjet)"
        >
          <template #content>

            <h3>{{ project.nom || 'Sans nom' }}</h3>

            <p>
              {{ project.description || 'Pas de description' }}
            </p>

            <span>
              {{ project.statut || 'En cours' }}
            </span>

            <ProgressBar
                :value="project.progress || 50"
                style="height: 12px; margin-top: 1rem"
            />

          </template>
        </Card>
      </div>

      <Paginator
          v-if="projects.length > rows"
          :first="first"
          :rows="rows"
          :totalRecords="projects.length"
          :rowsPerPageOptions="[6, 9, 12]"
          @page="onPageChange"
          style="margin-top: 2rem"
      />

    </div>
  </div>
</template>

<style scoped>
.all-projects-view {
  padding: 2rem;
}

.header {
  margin-bottom: 1.5rem;
}

.projects-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
}

.project-card {
  cursor: pointer;
  border-radius: 12px;
}

.project-card:hover {
  transform: translateY(-2px);
  transition: 0.2s ease;
}
</style>