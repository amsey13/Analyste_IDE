<script setup>
import Card from 'primevue/card'
import Button from 'primevue/button'
import FileUpload from 'primevue/fileupload'
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router' 
import auditProjectService from "../features/projects/api/AuditProjectService.js"
import Dialog from 'primevue/dialog'

const router = useRouter() 
const route = useRoute()
const idProject = route.params.id

const showDialog = ref(false)
const errorMessage = ref('')
const isLoading = ref(false)
const report = ref(null)

const files = ref({
  bpmn: null,
  mcd: null,
  mfc: null
})

const fileTypes = [
  {
    key: 'bpmn',
    title: '1. Processus (BPMN)',
    icon: 'pi pi-cog',
    accept: '.bpmn',
    description: 'Diagramme de processus'
  },
  {
    key: 'mcd',
    title: '2. Données (MCD)',
    icon: 'pi pi-database',
    accept: '.mcd',
    description: 'Fait par JMerise'
  },
  {
    key: 'mfc',
    title: '3. Flux (MFC)',
    icon: 'pi pi-sync',
    accept: '.flu',
    description: 'Fait par JFlux'
  }
]

const uploadedCount = computed(() => {
  return Object.values(files.value).filter(f => f !== null).length
})

const handleUpload = (event, type) => {
  if (event.files && event.files.length > 0) {
    files.value[type] = event.files[0]
  }
}

const removeFile = (type) => {
  files.value[type] = null
}

const startAudit = async () => {
  if (uploadedCount.value < 2) {
    errorMessage.value = "Veuillez importer au moins deux fichiers."
    return
  }

  isLoading.value = true
  errorMessage.value = ""

  try {
       const result = await auditProjectService.analyzeProject(
      idProject,
      files.value.bpmn,
      files.value.mcd,
      files.value.mfc
    )
    
        router.push({ 
      name: 'AuditReport', 
      params: { 
        id: idProject, 
        reportId: result.id 
      } 
    })

  } catch (error) {
    errorMessage.value = "Erreur : " + (error.response?.data?.message || error.message)
  } finally {
    isLoading.value = false
  }
}

const downloadReport = async() => {
  if(!report.value || !report.value.id) {
    errorMessage.value = "Impossible de trouver l'ID du rapport.";
    return;
  }
  try {
    await auditProjectService.downloadPdf(report.value.id);
  } catch(error) {
    alert(errorMessage.value);
  }
}
</script>

<template>
  <div class="p-5">

    <div class="mb-5">
      <h1 class="text-3xl font-bold">Configuration de l'Audit</h1>
      <p class="text-600">
        Sélectionnez les sources à croiser pour l'analyse de cohérence.
      </p>
    </div>

    <div class="grid">
      <div v-for="type in fileTypes" :key="type.key" class="col-12 md:col-4">
        <Card class="text-center">
          <template #content>
            <i :class="type.icon + ' text-4xl mb-3'"></i>
            <h3>{{ type.title }}</h3>

            <p class="text-600 mb-3">
              {{ type.description }}
            </p>

            <FileUpload
              mode="basic"
              :name="type.key"
              :accept="type.accept"
              chooseLabel="Importer fichier"
              @select="(e) => handleUpload(e, type.key)"
            />

            <div v-if="files[type.key]" class="mt-3 flex justify-content-between align-items-center">
              <span class="text-sm overflow-hidden text-overflow-ellipsis">{{ files[type.key].name }}</span>
              <Button
                icon="pi pi-times"
                severity="danger"
                text
                @click="removeFile(type.key)"
              />
            </div>
          </template>
        </Card>
      </div>
    </div>

    <div v-if="uploadedCount < 2" class="mt-5 text-center text-600 italic">
      ⚠ Veuillez fournir au moins 2 fichiers pour lancer l'analyse
    </div>

    <div v-if="errorMessage" class="text-red-500 text-center mt-3 font-bold">
      {{ errorMessage }}
    </div>

    <div class="mt-4 flex justify-content-center">
      <Button
        label="Générer le Rapport d'Audit"
        icon="pi pi-file"
        :disabled="uploadedCount < 2"
        :loading="isLoading"
        @click="startAudit"
        class="p-button-lg"
      />
    </div>

    </div>
</template>