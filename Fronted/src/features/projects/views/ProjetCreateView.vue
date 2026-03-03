<script setup>
  import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {ProjetService} from '../api/ProjetService';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import Password from 'primevue/password';

const router = useRouter();
const loading = ref(false);
const projet = ref({
   nom: '',
    description: '',
    taigaUserName: '',
    taigaPassword: '',
    taigaProjectUrl: ''
});

const createProject = async () => {
    loading.value = true;
    try {
        const response = await ProjetService.createProjet(projet.value);
        const newProjectId = response.idProjet;
        router.push({
            name: 'projet-dashboard',
            params: { id: newProjectId }
        });
        console.log("Projet créé avec succès", response.data);
    } catch (e) {
        console.error("Erreur lors de la création du projet", e);
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
        <label for="nom">Nom du projet *</label>
        <InputText id="nom" v-model="projet.nom" required />
      </div>
      
      <div class="field col-12">
        <label for="desc">Description détaillée</label>
        <Textarea id="desc" v-model="projet.description" rows="5" />
      </div>

      <div class="col-12 mt-4">
        <div class="border-1 surface-border p-3 border-round">
            <h3 class="mt-0">Intégration Taiga (Optionnel)</h3>
            <p class="text-sm text-600 mb-3">
                Vous pouvez lier votre projet Taiga maintenant. 
                <strong>Si vous renseignez ces champs, nous importerons automatiquement vos User Stories.</strong>
            </p>
            
            <div class="grid">
                <div class="field col-12 md:col-6">
                    <label for="taigaUser">Nom d'utilisateur Taiga</label>
                    <InputText id="taigaUser" v-model="projet.taigaUserName" placeholder="Votre username" />
                </div>
                <div class="field col-12 md:col-6">
                    <label for="taigaPass">Mot de passe Taiga</label>
                    <Password id="taigaPass" v-model="projet.taigaPassword" :feedback="false" toggleMask />
                </div>
                <div class="field col-12">
                    <label for="taigaUrl">URL du projet Taiga</label>
                    <InputText id="taigaUrl" v-model="projet.taigaProjectUrl" placeholder="url de votre projet Taiga" />
                </div>
            </div>
        </div>
      </div>
    </div>

    <div class="flex justify-content-end gap-2 mt-4">
      <Button label="Annuler" severity="secondary" @click="router.back()" />
      <Button label="Créer et Ouvrir" icon="pi pi-check" :loading="loading" @click="createProject" />
    </div>
  </div>  
</template>