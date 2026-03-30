<script setup>
import {computed, ref, watch} from 'vue';
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
  const nameIsValid = computed(() => {
    return /^[a-zA-ZÀ-ÿ\s]+$/.test(project.value.name.trim())
  })

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
    if(!nameIsValid.value) return;

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

      await new Promise(resolve => setTimeout(resolve, 1000));

      if (targetRouteName) {
        await router.push({
          name: targetRouteName,
          params: {id: newProjectId}
        });
      } else {
        await router.push({
          name: 'project-dashboard',
          params: {id: newProjectId}
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
  <div class="create-page">

    <div class="create-page__header">
      <h1 class="create-page__title">
        <i class="pi pi-plus-circle" style="font-size: 1.5rem; margin-right: 0.5rem; vertical-align: middle;"></i>
        Créer un nouveau projet
      </h1>
      <p class="create-page__subtitle">Renseignez les informations de votre projet pour démarrer.</p>
    </div>

    <div class="create-card">
      <div class="grid formgrid p-fluid">

        <div class="field col-12 md:col-6">
          <h3 class="section-title">Nom du projet</h3>
          <InputText
              id="nom"
              v-model="project.name"
              @blur="nameTouched = true"
              :class="{'p-invalid': nameTouched && !nameIsValid}"
              placeholder="Exemple : Approvisionnement en poissons rouges"
              required
              class="w-full"
          />
          <small v-if="nameTouched && project.name.trim() === ''" class="p-error">
            Le nom du projet est obligatoire
          </small>
          <small v-else-if="nameTouched && !nameIsValid" class="p-error">
            Le nom du projet ne doit contenir que des lettres
          </small>
        </div>

        <div class="field col-12">
          <h3 class="section-title">Fonctionnalité</h3>
          <div class="feature-cards">
            <div
                class="feature-option"
                :class="{ 'feature-option--selected': project.project_type === 'audit' }"
                @click="project.project_type = 'audit'"
            >
              <RadioButton v-model="project.project_type" inputId="audit" name="project_type" value="audit" />
              <label for="audit" class="cursor-pointer" style="font-weight: 600; color: #1f355e;">
                <i class="pi pi-search mr-2"></i> Audit
              </label>
            </div>
            <div
                class="feature-option"
                :class="{ 'feature-option--selected': project.project_type === 'accompagnement' }"
                @click="project.project_type = 'accompagnement'"
            >
              <RadioButton v-model="project.project_type" inputId="accompagnement" name="project_type" value="accompagnement" />
              <label for="accompagnement" class="cursor-pointer" style="font-weight: 600; color: #1f355e;">
                <i class="pi pi-users mr-2"></i> Accompagnement
              </label>
            </div>
          </div>
          <small v-if="featureTouched && project.project_type === ''" class="p-error">
            Merci de choisir une fonctionnalité
          </small>
        </div>

        <div class="field col-12">
          <h3 class="section-title">Description du projet</h3>
          <Textarea
              id="desc"
              v-model="project.description"
              autoResize
              rows="4"
              placeholder="Décrivez brièvement votre projet..."
              class="w-full"
          />
        </div>

        <div class="col-12">
          <div
              v-if="project.project_type === 'audit'"
              class="flex align-items-center justify-content-between mb-3"
          >
            <h3 class="section-title mb-0">Intégration Taiga (optionnelle)</h3>
            <ToggleSwitch v-model="taigaEnabled" />
          </div>

          <Transition name="fade">
            <div
                v-if="project.project_type === 'audit' && taigaEnabled"
                class="border-1 surface-border p-4 border-round mt-3 mb-5"
            >
              <p class="text-sm text-600 mb-4 line-height-3">
                Vous pouvez lier votre projet Taiga maintenant.
                <strong>Si vous remplissez ces champs, vos User Stories seront importées automatiquement.</strong>
              </p>
              <div class="grid">
                <div class="field col-12 md:col-6">
                  <label for="taigaUser" class="font-medium mb-2 block">Nom d'utilisateur Taiga</label>
                  <InputText id="taigaUser" v-model="project.taigaUserName" placeholder="Votre username" class="w-full" />
                </div>
                <div class="field col-12 md:col-6">
                  <label for="taigaPass" class="font-medium mb-2 block">Mot de passe Taiga</label>
                  <Password id="taigaPass" v-model="project.taigaPassword" placeholder="Votre password" :feedback="false" toggleMask />
                </div>
                <div class="field col-12">
                  <label for="taigaUrl" class="font-medium mb-2 block">URL du projet Taiga</label>
                  <InputText id="taigaUrl" v-model="project.taigaProjectUrl" placeholder="Lien de votre projet Taiga" class="w-full" />
                </div>
              </div>
            </div>
          </Transition>
        </div>

      </div>

      <div class="summary-box">
        <div class="summary-title">
          <i class="pi pi-info-circle text-primary"></i>
          Résumé
        </div>
        <p class="summary-row"><strong>Nom :</strong> {{ project.name || 'Non renseigné' }}</p>
        <p class="summary-row"><strong>Description :</strong> {{ project.description.length }} caractères</p>
        <p class="summary-row"><strong>Mode :</strong> {{ project.project_type || 'Non sélectionné' }}</p>
        <p v-if="project.project_type === 'audit'" class="summary-row">
          <strong>Taiga :</strong>
          <span :class="taigaEnabled ? 'text-green-600 font-medium' : 'text-red-500 font-medium'">
            {{ taigaEnabled ? ' Activé' : ' Désactivé' }}
          </span>
        </p>
      </div>
    </div>

    <div class="form-actions">
      <Button
          label="Annuler"
          severity="secondary"
          outlined
          @click="router.back()"
      />
      <Button
          label="Créer et Ouvrir"
          icon="pi pi-check"
          :loading="loading"
          :disabled="!nameIsValid || project.name.trim() === '' || project.project_type === ''"
          @click="createProject"
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

.create-page {
  padding: 2rem;
  background: #eef2f7;
  min-height: 100vh;
}

.create-page__header {
  margin-bottom: 2rem;
  animation: fadeInDown 0.4s ease both;
}

.create-page__title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1f355e;
  margin: 0 0 0.4rem;
  padding-left: 0.75rem;
  border-left: 4px solid #2563eb;
  line-height: 1.2;
}

.create-page__subtitle {
  font-size: 0.9rem;
  color: #64748b;
  margin: 0.4rem 0 0;
  padding-left: 0.85rem;
  border-left: 4px solid transparent;
  font-style: italic;
}

.create-card {
  background: white;
  border-radius: 14px;
  border: 1.5px solid #dde3ea;
  box-shadow: 0 1px 4px rgba(31, 53, 94, 0.06);
  padding: 2rem;
  animation: fadeInUp 0.4s 0.1s ease both;
  opacity: 0;
}

.section-title {
  font-size: 1rem;
  font-weight: 700;
  color: #1f355e;
  margin: 0 0 0.75rem;
}

.feature-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.feature-option {
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  padding: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  transition: border-color 0.2s, background 0.2s;
}

.feature-option:hover {
  border-color: #94a3b8;
  background: #f8fafc;
}

.feature-option--selected {
  border-color: #2563eb;
  background: #eff6ff;
}

.summary-box {
  background: #f8fafc;
  border: 1.5px solid #dde3ea;
  border-radius: 10px;
  padding: 1.25rem;
  margin-top: 1.5rem;
}

.summary-title {
  font-size: 0.95rem;
  font-weight: 700;
  color: #1f355e;
  margin: 0 0 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.summary-row {
  font-size: 0.875rem;
  color: #374a67;
  margin: 0 0 0.4rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  margin-top: 1.5rem;
  animation: fadeInUp 0.4s 0.2s ease both;
  opacity: 0;
}
</style>

