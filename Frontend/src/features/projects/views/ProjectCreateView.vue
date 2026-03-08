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


        <h1 class="text-900 font-bold mb-4">Créer un nouveau projet</h1>

        <div class="grid formgrid p-fluid">
          <div class="field col-12 md:col-6">
            <!--<label for="nom">Nom du projet *</label>-->
            <h3 for="nom" class="mt-0 mr-3">Nom du projet</h3>

            <InputText
                id="nom"
                v-model="projet.name"
                @blur="nameTouched = true"
                :class="{'p-invalid': nameTouched && projet.name.trim() === ''}"
                required
                class="w-full block w-full"
            />
            <small v-if="nameTouched && projet.name.trim() === ''" class="p-error">
              Le nom du projet est obligatoire
            </small>
          </div>
      
          <div class="field col-12">
            <!--<label for="desc">
              Description détaillée
            </label><br>-->
            <h3 for="desc" class="mt-0 mr-3">Description du projet</h3>
            <Textarea
                id="desc"
                v-model="projet.description"
                autoResize
                rows="5"
                class="w-full"
            />
          </div>

      <!-- toggle to enable Taiga-->
          <div class="col-12 mt-4">
            <div class="flex align-items-center justify-content-between mb-3">
              <h3 class="mt-0 mb-0">Intégration Taiga (Facultatif)</h3>

              <ToggleSwitch v-model="taigaEnabled" />
            </div>

            <Transition name="fade">
              <div
                  v-if="taigaEnabled"
                  class="border-1 surface-border p-4 border-round mt-2"
              >
                <p class="text-sm text-600 mb-4 line-height-3">
                  Vous pouvez lier votre projet Taiga maintenant à ce projet.
                  <strong>Si vous remplissez ces champs, vos User Stories seront importéés automatiquement.</strong>
                </p>

                <div class="grid fromgrid">
                  <div class="field col-12 md:col-6">
                    <label for="taigaUser" class="font-medium mb-2 block">
                      Nom d'utilisateur Taiga
                    </label>
                    <InputText
                        id="taigaUser"
                        v-model="projet.taigaUserName"
                        placeholder="Entrez votre username"
                        class="w-full"
                    />
                  </div>

                  <div class="field col-12 md:col-6">
                    <label for="taigaPass" class="font-medium mb-2 block">
                      Mot de passe Taiga
                    </label>
                    <Password
                        id="taigaPass"
                        v-model="projet.taigaPassword"
                        placeholder="Entrez votre password"
                        :feedback="false"
                        toggleMask
                    />
                  </div>

                  <div class="field col-12">
                    <label for="taigaUrl" class="font-medium mb-2 block">
                      URL du projet Taiga
                    </label>
                    <InputText
                        id="taigaUrl"
                        v-model="projet.taigaProjectUrl"
                        placeholder="Lien de votre projet Taiga"
                        class="w-full"
                        />
                  </div>
                </div>
              </div>
            </Transition>
          </div>
        </div>

        <!--  <div class="col-12 mt-4">-->
            <div class="border-1 surface-border p-4 border-round bg-gray-50">
              <div class="flex align-items-center gap-2 mb-3">
                <i class="pi pi-info-circle text-primary text-lg"></i>
                <h3 class="mt-0 mb-0 text-lg font-semibold">
                Résumé
                </h3>
              </div>

              <div class="line-height-3 text-700">
                <p class="mb-2">
                  <strong>Nom :</strong>
                  <span class="ml-1">{{ projet.name || 'Non renseigné' }}</span>
                </p>

                <p class="mb-2">
                  <strong>Description :</strong>
                  <span class="ml-1">{{ projet.description.length }} caractères </span>
                </p>

                <p class="mb-0">
                  <strong>Taiga :</strong>
                  <span
                      class="ml-1"
                      :class="taigaEnabled ? 'text-green-600 font-medium' : 'text-red-500 font-medium'"
                      >
                      {{ taigaEnabled ? 'Activé' : 'Désactivé' }}
                  </span>
                </p>
              </div>

            </div>
          </div>

          <div class="flex flex-column md:flex-row justify-content-end gap-3 mt-5">
            <Button
                label="Annuler"
                severity="secondary"
                class="p-button-outlined w-full md:w-auto"
                @click="router.back()"
            />
            <Button
                label="Créer et Ouvrir"
                icon="pi pi-check"
                :loading="loading"
                class="p-button-primary w-full md:w-auto"
                @click="createProject"
            />
          </div>

        </div>
      </div>
</template>