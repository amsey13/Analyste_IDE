<script setup>
  import { ref,watch } from 'vue';
  import { useRouter } from 'vue-router';
  import {ProjectService} from '../api/ProjectService.js';
  import Button from 'primevue/button';
  import InputText from 'primevue/inputtext';
  import Textarea from 'primevue/textarea';
  import Password from 'primevue/password';
  import ToggleSwitch from 'primevue/toggleswitch';
  import RadioButton from 'primevue/radiobutton';


  const router = useRouter();
  const loading = ref(false);
  const taigaEnabled = ref(false);
  const nameTouched = ref(false);
  const featureTouched = ref(false);

  const project = ref({
    name: '',
    description: '',
    project_type:'',
    taigaUserName: '',
    taigaPassword: '',
    taigaProjectUrl: ''
  });

  const ROUTES_BY_TYPE = {
    'accompagnement': 'accompagnement',
    'audit': 'audit'
  };

  watch(
      () => project.value.project_type,
      (newValue) => {
        if (newValue !== 'audit') {
          taigaEnabled.value = false;
        }
      }
  );

  const createProject = async () => {
    nameTouched.value = true;
    featureTouched.value = true;
    if(project.value.project_type === '') return;
    if(project.value.name.trim() === '') return;

    const payload = { ...project.value };


    const isAudit = payload.project_type === 'audit';
    const isTaigaActivated = taigaEnabled.value;

    if (!isAudit || !isTaigaActivated) {
      delete payload.taigaUserName;
      delete payload.taigaPassword;
      delete payload.taigaProjectUrl;
    }
    loading.value = true;
    try {
      const response = await ProjectService.createProject(payload);
      const newProjectId = response.idProject;
      const targetRouteName = ROUTES_BY_TYPE[payload.project_type];

      if (targetRouteName) {
        router.push({
          name: targetRouteName,
          params: { id: newProjectId }
        });
      } else {
        router.push({
          name: 'project-dashboard',
          params: { id: newProjectId }
        });
      }
    } catch (e) {
      console.error("Erreur lors de la création du project", e);
    } finally {
      loading.value = false;
    }


  };
</script>



<template>
  <div class="flex justify-content-center px-3"> <!-- in case we all aggree to center it-->
    <div class="w-full md:w-10 lg:w-8">
      <div class="card-container p-4 md:p-5 border-round shadow-2 surface-card">


        <h1 class="text-900 font-bold mb-4">Créer un nouveau projet</h1>

        <div class="grid formgrid p-fluid">
          <div class="field col-12 md:col-6">
            <!--<label for="nom">Nom du project *</label>-->
            <h3 for="nom" class="mt-0 mr-3">Nom du projet</h3>

            <InputText
                id="nom"
                v-model="project.name"
                @blur="nameTouched = true"
                :class="{'p-invalid': nameTouched && project.name.trim() === ''}"
                required
                class="w-full block w-full"
            />
            <small v-if="nameTouched && project.name.trim() === ''" class="p-error">
              Le nom du projet est obligatoire
            </small>
          </div>

          <div class="field col-12">
            <h3 class="mt-0 mb-3"> Fonctionnalité </h3>

            <div class="flex gap-3 flex-wrap">

              <div class="feature-card flex-1 flex-align-items-center gap-2">
                <RadioButton
                    v-model="project.project_type"
                    inputId="audit"
                    name="project_type"
                    value="audit"
                />
                <label for="audit" class="cursor-pointer">Audit </label>
              </div>

              <div class="feature-card flex-1 flex-align-items-center gap-2">
                <RadioButton
                    v-model="project.project_type"
                    inputId="accompagnement"
                    name="project_type"
                    value="accompagnement"
                />
                <label for="accompagnement" class="cursor-pointer">Accompagnement </label>
              </div>
            </div>

            <small v-if="featureTouched && project.project_type === ''" class="p-error">
              Merci de choisir la fonctionnalité que vous voulez utiliser s'il vous plait !
            </small>
          </div>


          <div class="field col-12">
            <!--<label for="desc">
              Description détaillée
            </label><br>-->
            <h3 for="desc" class="mt-0 mr-3">Description du projet</h3>
            <Textarea
                id="desc"
                v-model="project.description"
                autoResize
                rows="5"
                class="w-full"
            />
          </div>
          <br>

          <!-- toggle to enable Taiga-->
          <div class="col-12">
            <div
                v-if="project.project_type === 'audit'"
                class="flex align-items-center justify-content-between mb-3"
            >
              <h3 class="mt-0 mb-0">Intégration Taiga (optionnelle)</h3>

              <ToggleSwitch v-model="taigaEnabled" />
            </div>

            <Transition name="fade">
              <div
                  v-if="project.project_type === 'audit' && taigaEnabled"
                  class="border-1 surface-border p-4 border-round mt-3 mb-5"
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
                        v-model="project.taigaUserName"
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
                        v-model="project.taigaPassword"
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
                        v-model="project.taigaProjectUrl"
                        placeholder="Lien de votre projet Taiga"
                        class="w-full"
                    />
                  </div>
                </div>
              </div>
            </Transition>
          </div>
        </div>

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
              <span class="ml-1">{{ project.name || 'Non renseigné' }}</span>
            </p>

            <p class="mb-2">
              <strong>Description :</strong>
              <span class="ml-1">{{ project.description.length }} caractères </span>
            </p>

            <p v-if="project.project_type === 'audit'" class="mb-0">
              <strong>Taiga :</strong>
              <span
                  class="ml-1"
                  :class="taigaEnabled ? 'text-green-600 font-medium' : 'text-red-500 font-medium'"
              >
                      {{ taigaEnabled ? 'Activé' : 'Désactivé' }}
                  </span>
            </p>

            <p class="mb-2">
              <strong>Mode :</strong>
              <span class="ml-1">{{ project.project_type || 'Non sélectionné' }}</span>
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
            :disabled="project.name.trim() === '' || project.project_type === ''"
            class="p-button-primary w-full md:w-auto"
            @click="createProject"
        />
      </div>
    </div>
  </div>
</template>