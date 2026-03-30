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
  <div class="audit-page">

    <div class="audit-page__header">
      <h1 class="audit-page__title">
        <i class="pi pi-search" style="font-size: 1.5rem; margin-right: 0.5rem; vertical-align: middle;"></i>
        Configuration de l'Audit
      </h1>
      <p class="audit-page__subtitle">Sélectionnez les sources à croiser pour l'analyse de cohérence.</p>
    </div>

    <div class="upload-grid">
      <div
          v-for="type in fileTypes"
          :key="type.key"
          class="upload-card"
          :class="{ 'upload-card--filled': files[type.key] }"
      >
        <div class="upload-card__icon">
          <i :class="type.icon"></i>
        </div>
        <div class="upload-card__title">{{ type.title }}</div>
        <div class="upload-card__desc">{{ type.description }}</div>

        <FileUpload
            v-if="!files[type.key]"
            mode="basic"
            :name="type.key"
            :accept="type.accept"
            chooseLabel="Importer fichier"
            class="upload-card__btn-upload"
            @select="(e) => handleUpload(e, type.key)"
        />

        <div v-else class="upload-card__file">
          <span class="upload-card__filename">{{ files[type.key].name }}</span>
          <button class="upload-card__remove" @click="removeFile(type.key)">
            <i class="pi pi-times"></i>
          </button>
        </div>
      </div>
    </div>

    <div class="progress-wrap">
      <div class="progress-label">
        <span>Fichiers importés</span>
        <span>{{ uploadedCount }} / 3</span>
      </div>
      <div class="progress-track">
        <div class="progress-fill" :style="{ width: (uploadedCount / 3 * 100) + '%' }"></div>
      </div>
    </div>

    <div v-if="uploadedCount < 2" class="warning-box">
      <i class="pi pi-exclamation-triangle"></i>
      Au moins 2 fichiers requis pour lancer l'analyse.
    </div>

    <div v-if="uploadedCount >= 2" class="ready-box">
      <i class="pi pi-check-circle"></i>
      Vous pouvez lancer l'analyse !
    </div>

    <div v-if="errorMessage" class="error-box">
      <i class="pi pi-times-circle"></i>
      {{ errorMessage }}
    </div>

    <div class="audit-actions">
      <Button
          label="Générer le Rapport d'Audit"
          icon="pi pi-file"
          :disabled="uploadedCount < 2"
          :loading="isLoading"
          style="background: #1f355e; border-color: #1f355e;"
          class="p-button-lg"
          @click="startAudit"
      />
    </div>

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

.audit-page {
  padding: 2rem;
  background: #eef2f7;
  min-height: 100vh;
}

.audit-page__header {
  margin-bottom: 2rem;
  animation: fadeInDown 0.4s ease both;
}

.audit-page__title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1f355e;
  margin: 0 0 0.4rem;
  padding-left: 0.75rem;
  border-left: 4px solid #2563eb;
  line-height: 1.2;
}

.audit-page__subtitle {
  font-size: 0.9rem;
  color: #64748b;
  margin: 0.4rem 0 0;
  padding-left: 0.85rem;
  font-style: italic;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.upload-card {
  background: white;
  border-radius: 14px;
  border: 1.5px solid #e8edf2;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  box-shadow: 0 1px 4px rgba(31,53,94,0.06);
  transition: border-color 0.2s, background 0.2s;
  animation: fadeInUp 0.4s ease both;
}

.upload-card--filled {
  border-color: #2563eb;
  background: #f0f7ff;
}

.upload-card__icon {
  width: 52px; height: 52px;
  border-radius: 12px;
  background: #eef2f7;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.4rem;
  color: #1f355e;
  transition: background 0.2s, color 0.2s;
}

.upload-card--filled .upload-card__icon {
  background: #dbeafe;
  color: #2563eb;
}

.upload-card__title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1f355e;
  text-align: center;
}

.upload-card__desc {
  font-size: 0.78rem;
  color: #64748b;
  text-align: center;
}

.upload-card__btn-upload {
  width: 100%;
}

:deep(.upload-card__btn-upload .p-fileupload-choose) {
  width: 100%;
  background: none;
  border: 1.5px dashed #c7d2dc;
  color: #64748b;
  font-size: 0.82rem;
  border-radius: 8px;
  justify-content: center;
  transition: all 0.2s;
}

:deep(.upload-card__btn-upload .p-fileupload-choose:hover) {
  border-color: #2563eb;
  color: #2563eb;
  background: #f0f7ff;
}

.upload-card__file {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #dcfce7;
  border-radius: 8px;
  padding: 0.5rem 0.75rem;
}

.upload-card__filename {
  font-size: 0.78rem;
  color: #15803d;
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 120px;
}

.upload-card__remove {
  background: none;
  border: none;
  color: #ef4444;
  cursor: pointer;
  font-size: 0.75rem;
  padding: 2px 4px;
  border-radius: 4px;
  transition: background 0.15s;
}

.upload-card__remove:hover { background: #fee2e2; }

.progress-wrap {
  background: white;
  border-radius: 14px;
  border: 1.5px solid #e8edf2;
  padding: 1.25rem 1.5rem;
  margin-bottom: 1rem;
  box-shadow: 0 1px 4px rgba(31,53,94,0.06);
}

.progress-label {
  display: flex;
  justify-content: space-between;
  font-size: 0.82rem;
  color: #64748b;
  margin-bottom: 0.6rem;
  font-weight: 600;
}

.progress-track {
  height: 6px;
  background: #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #2563eb;
  border-radius: 10px;
  transition: width 0.4s ease;
}

.warning-box {
  display: flex; align-items: center; gap: 0.5rem;
  background: #fef9c3;
  border: 1.5px solid #fde68a;
  border-radius: 10px;
  padding: 0.75rem 1rem;
  font-size: 0.82rem;
  color: #92400e;
  margin-bottom: 1.5rem;
}

.ready-box {
  display: flex; align-items: center; gap: 0.5rem;
  background: #dcfce7;
  border: 1.5px solid #86efac;
  border-radius: 10px;
  padding: 0.75rem 1rem;
  font-size: 0.82rem;
  color: #15803d;
  margin-bottom: 1.5rem;
}

.error-box {
  display: flex; align-items: center; gap: 0.5rem;
  background: #fee2e2;
  border: 1.5px solid #fca5a5;
  border-radius: 10px;
  padding: 0.75rem 1rem;
  font-size: 0.82rem;
  color: #991b1b;
  margin-bottom: 1.5rem;
}

.audit-actions {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
}

@media (max-width: 768px) {
  .upload-grid { grid-template-columns: 1fr; }
}
</style>