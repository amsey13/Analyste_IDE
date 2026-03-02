<script setup>
  import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {ProjetService} from '../api/ProjetService';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';

const router = useRouter();
const loading = ref(false);
const projet = ref({
    nom: '',
    description: '',
    dateCreation: new Date(),
    UrlTaiga: '',
    NomTaiga: '',
    MotdePasseTaiga: ''

});

const createProject = async () => {
    loading.value = true;
    try {
        const response = await ProjetService.createProjet(projet.value);
        const newProjectId = response.data.idProjet;
        router.push({
            name: 'projet-dashboard',
            params: { id: newProjectId }
        });
    } catch (e) {
        console.error("Erreur lors de la création du projet");
    } finally {
        loading.value = false;
    }
};
</script>





<template>
    <div class="card p-5">
    <h1 class="text-900 font-bold mb-4">Créer un nouveau projet</h1>
    
    <div class="grid formgrid p-fluid">
      <div class="field col-12 md:col-6">
        <label for="nom">Nom du projet</label>
        <InputText id="nom" v-model="projet.nom" />
      </div>
      
      <div class="field col-12">
        <label for="desc">Description détaillée</label>
        <Textarea id="desc" v-model="projet.description" rows="5" />
      </div>

      </div>

    <div class="flex justify-content-end gap-2 mt-4">
      <Button label="Annuler" severity="secondary" @click="router.back()" />
      <Button label="Créer et Ouvrir" icon="pi pi-check" :loading="loading" @click="createProject" />
    </div>
  </div>
    
</template>