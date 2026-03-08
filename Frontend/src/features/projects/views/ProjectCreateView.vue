<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import {ProjectService} from '../api/ProjectService.js';
import Button from 'primevue/button';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import Password from 'primevue/password';
import ToggleSwitch from 'primevue/toggleswitch';


const router = useRouter();
const loading = ref(false);
const taigaEnabled = ref(false);
const nameTouched = ref(false);
const projet = ref({
    name: '',
    description: '',
    taigaUserName: '',
    taigaPassword: '',
    taigaProjectUrl: ''
});

const createProject = async () => {
    loading.value = true;
    try {
        const response = await ProjectService.createProjet(projet.value);
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
  <div class="flex justify-content-center px-3">
    <div class="w-full md:w-10 lg:w-8">
      <div class="mt-4">

   <!--<div class="card p-4 w-full md:w-9 lg:w-7">-->
   <!-- <div class="card p-5">-->
    <h1 class="text-900 font-bold mb-4">Créer un nouveau projet</h1>
    
    <div class="grid formgrid p-fluid">
      <div class="field col-12 md:col-6">
        <label for="nom">Nom du projet *</label>

        <InputText
            id="nom"
            v-model="projet.name"
            @blur="nameTouched = true"
            :class="{'p-invalid': nameTouched && projet.name.trim() === ''}"
            required
        />

        <small v-if="nameTouched && projet.name.trim() === ''" class="p-error">
          Le nom du projet est obligatoire
        </small>
      </div>
      
      <div class="field col-12">
        <label for="desc">Description détaillée</label>
        <Textarea id="desc" v-model="projet.description" rows="5" />
      </div>

      <!-- toggle to enable Taiga-->
      <div class="col-12 mt-4">
        <div class="flex align-items-center mb-3">
          <h3 class="mt-0 mr-3">Intégration Taiga</h3> <ToggleSwitch v-model="taigaEnabled" />
        </div>

        <Transition name="fade">
          <div
              v-if="taigaEnabled"
              class="border-1 surface-border p-3 border-round"
          >
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
        </Transition>
      </div>
      </div>
    </div>

      <div class="col-12 lg:col-4">
        <div class="border-1 surface-border p-3 border-round bg-gray-50">
          <h3 class="mt-0">Résumé</h3>
          <p><strong>Nom :</strong> {{ projet.name || 'Non renseigné' }}</p>
          <p><strong>Description :</strong> {{ projet.description.length }} caractères</p>
          <p><strong>Taiga :</strong> {{ taigaEnabled ? 'Activé' : 'Désactivé' }}</p>
        </div>
      </div>


      <div class="flex justify-content-end gap-2 mt-4">
      <Button label="Annuler" severity="secondary" @click="router.back()" />
      <Button label="Créer et Ouvrir" icon="pi pi-check" :loading="loading" @click="createProject" />
  </div>
    </div>
  </div>
  <!--</div>
  </div>-->
</template>