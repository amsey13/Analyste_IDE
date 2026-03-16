<script setup>
import { ref } from 'vue';
import { SupportFeatureService } from '../api/SupportFeatureService.js';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
import Select from 'primevue/select';
import Dialog from 'primevue/dialog';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';

const props = defineProps({
  projectId: { type: String, required: true },
  userStories: { type: Array, default: () => [] },
  actors: { type: Array, default: () => [] }
});

const emit = defineEmits(['update:userStories']);
const confirm = useConfirm();
const toast = useToast();

// --- GESTION DE LA MODALE ---
const showDialog = ref(false);
const isEditing = ref(false);
const isSaving = ref(false);

// Modèle de données unifié
const storyForm = ref({
  id: null,
  actorId: null,
  description: '', // Bien corrigé ici (sans faute de frappe)
  benefit: '',
  acceptanceCriteria: ''
});

// --- ACTIONS MODALE ---
const openNew = () => {
  storyForm.value = { id: null, actorId: null, description: '', benefit: '', acceptanceCriteria: '' };
  isEditing.value = false;
  showDialog.value = true;
};

const openEdit = (story) => {
  storyForm.value = {
    id: story.id,
    actorId: story.actor ? story.actor.id : story.actorId,
    description: story.description || '', // Utilisation de description
    benefit: story.benefit || '',
    acceptanceCriteria: story.acceptanceCriteria || ''
  };
  isEditing.value = true;
  showDialog.value = true;
};

const hideDialog = () => {
  showDialog.value = false;
};

// --- SAUVEGARDE  ---
const saveStory = async () => {
  if (!storyForm.value.actorId || !storyForm.value.description.trim()) {
    toast.add({ severity: 'warn', summary: 'Attention', detail: 'L\'acteur et l\'action sont obligatoires.', life: 3000 });
    return;
  }

  isSaving.value = true;

  try {
    // FIX 500 : On remet l'acteur dans le payload pour que le Backend puisse le lier lors du PUT !
    // (Note: Si ton Backend attend "actorId" au lieu de "actor: {id}", adapte cette ligne)
    const payload = {
      description: storyForm.value.description.trim(),
      benefit: storyForm.value.benefit.trim(),
      acceptanceCriteria: storyForm.value.acceptanceCriteria.trim(),
      actorId: storyForm.value.actorId
    };

    const actions = {
      true: async () => {
        const updated = await SupportFeatureService.updateUserStory(storyForm.value.id, payload);
        const list = props.userStories.map(us => us.id === storyForm.value.id ? updated : us);
        return { data: list, msg: 'User Story mise à jour' };
      },
      false: async () => {
        const added = await SupportFeatureService.addUserStory(props.projectId, storyForm.value.actorId, payload);
        added.actorId = storyForm.value.actorId;
        const list = [...(props.userStories || []), added];
        return { data: list, msg: 'User Story ajoutée', sum: 'Créée' };
      }
    };

    const result = await actions[isEditing.value]();

    emit('update:userStories', result.data);
    toast.add({ severity: 'success', summary: result.sum, detail: result.msg, life: 3000 });
    hideDialog();

  } catch (error) {
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la sauvegarde', life: 3000 });
    console.error(error);
  } finally {
    isSaving.value = false;
  }
};
// --- SUPPRESSION ---
const confirmRemove = (storyId) => {
  confirm.require({
    message: 'Êtes-vous sûr de vouloir supprimer cette User Story ?',
    header: 'Confirmation',
    icon: 'pi pi-exclamation-triangle',
    rejectProps: { label: 'Annuler', severity: 'secondary', outlined: true },
    acceptProps: { label: 'Supprimer', severity: 'danger' },
    accept: async () => {
      try {
        await SupportFeatureService.deleteUserStory(storyId);
        emit('update:userStories', props.userStories.filter(us => us.id !== storyId));
        toast.add({ severity: 'info', summary: 'Supprimée', detail: 'User Story retirée', life: 3000 });
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de supprimer l\'US', life: 3000 });
      }
    }
  });
};

const getActorName = (actorId) => {
  const actor = props.actors.find(a => a.id === actorId);
  return actor ? actor.name : 'Acteur inconnu';
};
</script>

<template>
  <div class="flex flex-column gap-3 h-full">

    <div class="flex justify-content-between align-items-center">
      <h3 class="m-0 text-xl text-800">User Stories ({{ userStories?.length || 0 }})</h3>
      <Button icon="pi pi-plus" label="Nouvelle US" severity="primary" size="small" @click="openNew" :disabled="actors.length === 0" />
    </div>

    <div v-if="actors.length === 0" class="p-3 border-round bg-orange-50 text-orange-700 border-1 border-orange-200">
      <i class="pi pi-info-circle mr-2"></i> Vous devez créer au moins un acteur avant de rédiger une User Story.
    </div>

    <ul class="list-none p-0 m-0 flex flex-column gap-3 overflow-y-auto pr-2 pb-4">
      <li v-if="userStories?.length === 0" class="text-500 font-italic text-center p-4 border-1 border-dashed surface-border border-round">
        Aucune User Story définie.
      </li>

      <li v-for="story in userStories" :key="story.id" class="p-3 border-1 surface-border border-round surface-0 shadow-1 transition-colors transition-duration-200 hover:surface-100">

        <div class="flex justify-content-between align-items-start mb-2">
          <span class="inline-block bg-blue-50 text-blue-700 px-2 py-1 border-round text-xs font-bold">
             En tant que : {{ story.actorName }}
          </span>
          <div class="flex gap-1">
            <Button icon="pi pi-pencil" text rounded size="small" @click="openEdit(story)" />
            <Button icon="pi pi-trash" severity="danger" text rounded size="small" @click="confirmRemove(story.id)" />
          </div>
        </div>

        <div class="text-800 mb-2">
          <span class="font-bold">Je veux :</span> {{ story.description }}
        </div>
        <div class="text-700 font-italic mb-3" v-if="story.benefit">
          <span class="font-bold not-italic">Afin de :</span> {{ story.benefit }}
        </div>

        <div class="bg-gray-50 p-2 border-round text-sm border-1 border-gray-200" v-if="story.acceptanceCriteria">
          <div class="font-bold text-gray-700 mb-1">Je suis satisfait si :</div>
          <div class="white-space-pre-wrap text-600">{{ story.acceptanceCriteria }}</div>
        </div>
      </li>
    </ul>

    <Dialog v-model:visible="showDialog" :header="isEditing ? 'Modifier la User Story' : 'Nouvelle User Story'" :modal="true" :style="{ width: '450px' }">
      <div class="flex flex-column gap-4 py-3">

        <div class="flex flex-column gap-2">
          <label for="actor" class="font-bold">En tant que...</label>
          <Select id="actor" v-model="storyForm.actorId" :options="actors" optionLabel="name" optionValue="id" placeholder="Sélectionnez un acteur" class="w-full" />
        </div>

        <div class="flex flex-column gap-2">
          <label for="action" class="font-bold">Je veux... <span class="text-red-500">*</span></label>
          <InputText id="action" v-model="storyForm.description" placeholder="ex: me connecter à l'application" class="w-full" />
        </div>

        <div class="flex flex-column gap-2">
          <label for="benefit" class="font-bold">Afin de...</label>
          <InputText id="benefit" v-model="storyForm.benefit" placeholder="ex: accéder à mes données privées" class="w-full" />
        </div>

        <div class="flex flex-column gap-2">
          <label for="criteria" class="font-bold">Je suis satisfait si...</label>
          <Textarea id="criteria" v-model="storyForm.acceptanceCriteria" rows="3" placeholder="Critères d'acceptation (un par ligne...)" class="w-full" autoResize />
        </div>

      </div>

      <template #footer>
        <Button label="Annuler" icon="pi pi-times" text severity="secondary" @click="hideDialog" />
        <Button label="Enregistrer" icon="pi pi-check" :loading="isSaving" @click="saveStory" />
      </template>
    </Dialog>

  </div>
</template>