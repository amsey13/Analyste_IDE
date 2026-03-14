<script setup>
import Card from 'primevue/card'
import Button from 'primevue/button'
import FileUpload from 'primevue/fileupload'
import { ref } from 'vue'

const bpmnFile = ref(null)
const mcdFile = ref(null)
const mfcFile = ref(null)
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

      <div class="col-12 md:col-4">
        <Card>
          <template #content>
            <i class="pi pi-database text-4xl mb-3"></i>
            2. Données (MCD)
            <p class="text-600 mb-3">
              Fait par JMerise !!
            </p>
            <FileUpload
                mode="basic"
                name="mcd"
                accept=".mcd"
                chooseLabel="Importer fichier MCD"
                @select="(e) => mcdFile = e.files[0]"
            />

          </template>
        </Card>
      </div>


      <div class="col-12 md:col-4">
        <Card class="text-center">

          <template #content>
            <i class="pi pi-cog text-4xl mb-3"></i>
             1. Processus (BPMN)
            <FileUpload
                mode="basic"
                name="bpmn"
                accept=".bpm"
                chooseLabel="Importer fichier BPMN"
                @select="(e) => bpmnFile.value = e.files[0]"
            />

          </template>
        </Card>
      </div>



      <div class="col-12 md:col-4">
        <Card>
          <template #content>
            <i class="pi pi-sync text-4xl mb-3"></i>
             3. Flux (MFC)
            <p class="text-600 mb-3">
              Fait par JFlux !!
            </p>
            <FileUpload
                mode="basic"
                name="mfc"
                accept=".flu"
                chooseLabel="Importer fichier MFC"
                @select="(e) => mfcFile = e.files[0]"
            />
          </template>
        </Card>
      </div>

    </div>

    <div class="mt-5 text-center text-600">
      ⚠ Veuillez fournir au moins 2 fichiers pour lancer une comparaison.
    </div>

    <div class="mt-4 flex justify-content-center">
      <Button label="Générer le Rapport d'Audit" icon="pi pi-file" />
    </div>

  </div>
</template>