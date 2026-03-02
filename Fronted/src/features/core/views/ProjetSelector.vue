<script setup>

import {ref, onMounted} from 'vue';
import {ProjetService} from '../api/ProjetService';
import {useRouter} from 'vue-router';
import Button  from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';


const projets = ref([]);
const router = useRouter();

onMounted(async () => {
  try {
    const response = await ProjetService.getProjets();
    projets.value = response.data;
  } catch (e) {
    console.error("Erreur de récupération des projets");
  }
});

const openProjet = (idProjet) =>{
    router.push({
        name: 'projet-dashboard',
        params: { id: idProjet }
    });
};




</script>

<template>
    <div class="card">
    <h2>Mes projets existants</h2>
    
    <DataTable :value="projects" paginator :rows="5" tableStyle="min-width: 50rem">
      <Column field="nom" header="Nom du projet"></Column>
      <Column field="dateCreation" header="Date de création"></Column>
      <Column header="Action">
        <template #body="slotProps">
          <Button 
            label="Go" 
            icon="pi pi-arrow-right" 
            @click="openProject(slotProps.data.idProjet)" 
          />
        </template>
      </Column>
    </DataTable>
  </div>
  
</template>