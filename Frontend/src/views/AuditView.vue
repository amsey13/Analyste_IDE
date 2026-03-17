<script setup>
import Card from 'primevue/card'
import Button from 'primevue/button'
import FileUpload from 'primevue/fileupload'
import { ref, computed } from 'vue'
//import axios from "axios"
import apiClient from '../api/HttpClient.js'
import { useRoute } from "vue-router"

const route = useRoute()
const projectId = route.params.id

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
    accept: '.bpmn, .xml',
    description: 'Diagramme de processus'
  },
  {
    key: 'mcd',
    title: '2. Données (MCD)',
    icon: 'pi pi-database',
    accept: '.mcd',
    description: 'Fait par JMerise !!'
  },
  {
    key: 'mfc',
    title: '3. Flux (MFC)',
    icon: 'pi pi-sync',
    accept: '.flu',
    description: 'Fait par JFlux !!'
  }
]

const errorMessage = ref('')

const handleUpload = (event, type) => {
  files.value[type] = event.files[0]
}

const removeFile = (type) => {
  files.value[type] = null
}

const uploadedCount = computed(() => {
  return Object.values(files.value).filter(f => f !== null).length
})

const report = ref(null)

const generateAudit = async () => {

  if (uploadedCount.value < 2) {
    errorMessage.value = "Veuillez importer au moins deux fichiers."
    return
  }

  const formData = new FormData()

  if (files.value.bpmn) formData.append("bpmn", files.value.bpmn)
  if (files.value.mcd) formData.append("mcd", files.value.mcd)
  if (files.value.mfc) formData.append("mfc", files.value.mfc)
  
  console.log("FILES :", files.value)

  for (let pair of formData.entries()) {
    console.log(pair[0], pair[1])
  }

  try {

    const response = await apiClient.post(
      `/audit/${projectId}/analyze`,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data"
        }
      }
    )

    report.value = response.data

  } catch (error) {
    console.error("Erreur audit :", error)
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

      <div
        v-for="type in fileTypes"
        :key="type.key"
        class="col-12 md:col-4"
      >

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

            <div
              v-if="files[type.key]"
              class="mt-3 flex align-items-center justify-content-between"
            >
              <span class="text-600">
                {{ files[type.key].name }}
              </span>

              <Button
                icon="pi pi-times"
                severity="danger"
                text
                rounded
                @click="removeFile(type.key)"
              />
            </div>

          </template>

        </Card>

      </div>

    </div>

    <div class="mt-5 text-center text-600">
      ⚠ Veuillez fournir au moins 2 fichiers pour lancer une comparaison.
    </div>

    <div v-if="errorMessage" class="text-red-500 text-center mt-2">
      {{ errorMessage }}
    </div>

    <div class="mt-4 flex justify-content-center">
      <Button
        label="Générer le Rapport d'Audit"
        icon="pi pi-file"
        @click="generateAudit"
      />
    </div>
    <div v-if="report" class="mt-5">

      <h2>Résultat de l'audit</h2>

      <p>
        Score de cohérence : <b>{{ report.score }} %</b>
      </p>

      <p>
        Généré le : {{ report.creationDate }}
      </p>

      <h3>Anomalies détectées</h3>

      <ul>
        <li v-for="a in report.anomalies" :key="a.id">
          {{ a.message }}
        </li>
      </ul>

    </div>

  </div>
</template>