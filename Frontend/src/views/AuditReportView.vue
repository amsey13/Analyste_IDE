<template>
  <div class="p-5">
    <Button label="Retour aux imports" icon="pi pi-arrow-left" @click="$router.back()" class="p-button-text" />
    
    <Card v-if="report" class="mt-4">
      <template #title>Rapport d'Audit de Cohérence</template>
      <template #content>
        <div class="text-5xl">{{ report.score }}%</div>
        
          <ul v-if="report.anomalies && report.anomalies.length" class="list-none p-0">
            <li v-for="a in report.anomalies" :key="a.id" class="mb-4 shadow-1 border-round overflow-hidden">
              
                  <div class="p-3 border-left-3 border-red-500 bg-red-50">
                    <div class="flex align-items-center mb-1">
                      <i class="pi pi-exclamation-circle text-red-600 mr-2 font-bold"></i>
                      <span class="font-bold text-red-800">Anomalie détectée</span>
                    </div>
                    <p class="m-0 text-red-700 line-height-3">
                      {{ a.description }}
                    </p>
                  </div>

                  <div v-if="a.suggestion" class="p-3 border-left-3 border-green-500 bg-green-50">
                    <div class="flex align-items-center mb-1 text-green-800">
                      <i class="pi pi-check-circle mr-2 font-bold"></i>
                      <span class="font-bold">Recommandation de correction</span>
                    </div>
                    <p class="m-0 text-green-700 italic line-height-3">
                      "{{ a.suggestion.content }}"
                    </p>
                  </div>

                </li>
            </ul>
        
        <Button label="Télécharger PDF" @click="downloadPdf" />
      </template>
    </Card>
  </div>
</template>

<script setup>
import Card from 'primevue/card'    
import Button from 'primevue/button'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import auditProjectService from "../features/projects/api/AuditProjectService"

const route = useRoute()
const report = ref(null)

const isLoading = ref(true)

onMounted(async () => {
  try {
    isLoading.value = true
   
    const idDuRapport = route.params.reportId 
    
    console.log("Tentative de récupération du rapport ID:", idDuRapport)
    
    report.value = await auditProjectService.getReportById(idDuRapport)
    
    console.log("Données reçues de Java:", report.value)
  } catch (e) {
    console.error("Erreur lors de la récupération :", e)
  } finally {
    isLoading.value = false
  }
})

const downloadPdf = () => {
  auditProjectService.downloadPdf(report.value.id)
}
</script>