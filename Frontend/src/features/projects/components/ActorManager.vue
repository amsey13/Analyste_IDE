<script setup>
import { ref } from 'vue';
import { SupportFeatureService } from '../api/SupportFeatureService.js';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import ConfirmDialog from 'primevue/confirmdialog';
import { useConfirm } from 'primevue/useconfirm';

// 1. Nouvel import pour utiliser le Toast
import { useToast } from 'primevue/usetoast';

const props = defineProps({
  projectId: { type: String, required: true },
  actors: { type: Array, default: () => [] },
  userStories:  {type: Array, default: () => [] }
});
const emit = defineEmits(['update:actors','update:userStories']);

const newActorName = ref('');
const isAdding = ref(false);
const editingActorId = ref(null);
const editingName = ref('');
const isUpdating = ref(false);

const confirm = useConfirm();
const toast = useToast(); // 2. Instanciation du Toast

// --- AJOUT ---
const addActor = async () => {
  if (!newActorName.value.trim()) return;
  isAdding.value = true;
  try {
    const addedActor = await SupportFeatureService.addActor(props.projectId, { name: newActorName.value.trim() });
    emit('update:actors', [...(props.actors || []), addedActor]);

    // 3. Déclenchement du Toast de Succès
    toast.add({ severity: 'success', summary: 'Succès', detail: 'Acteur ajouté avec succès', life: 3000 });

    newActorName.value = '';
  } catch (error) {
    // 4. Déclenchement du Toast d'Erreur
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible d\'ajouter l\'acteur', life: 3000 });
  } finally {
    isAdding.value = false;
  }
};

// --- MODIFICATION ---
const startEdit = (actor) => {
  editingActorId.value = actor.id;
  editingName.value = actor.name;
};

const cancelEdit = () => {
  editingActorId.value = null;
  editingName.value = '';
};

const saveEdit = async () => {
  if (!editingName.value.trim()) return;
  isUpdating.value = true;
  try {
    const updatedActor = await SupportFeatureService.updateActor(editingActorId.value, { name: editingName.value.trim() });
    const updatedList = props.actors.map(a => a.id === editingActorId.value ? updatedActor : a);
    emit('update:actors', updatedList);
    cancelEdit();

    // Toast de Succès
    toast.add({ severity: 'success', summary: 'Modifié', detail: 'Acteur mis à jour', life: 3000 });
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la modification', life: 3000 });
  } finally {
    isUpdating.value = false;
  }
};

// --- SUPPRESSION ---
const confirmRemove = (actorId) => {
  confirm.require({
    message: 'En supprimant cet acteur vous supprimez toutes les US qui lui sont associées.\nÊtes-vous sûr de vouloir supprimer cet acteur ?',
    header: 'Confirmation',
    icon: 'pi pi-exclamation-triangle',
    rejectProps: { label: 'Annuler', severity: 'secondary', outlined: true },
    acceptProps: { label: 'Supprimer', severity: 'danger' },
    accept: async () => {
      try {
        await SupportFeatureService.deleteActor(actorId);
        emit('update:actors', props.actors.filter(a => a.id !== actorId));
        emit('update:userStories', props.userStories.filter(us => us.actorId !== actorId));

        // Toast de Succès
        toast.add({ severity: 'info', summary: 'Supprimé', detail: 'Acteur retiré du projet', life: 3000 });
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de supprimer l\'acteur', life: 3000 });
      }
    }
  });
};
</script>

<template>
  <div class="flex flex-column gap-3">

    <ConfirmDialog></ConfirmDialog>

    <div class="flex justify-content-between align-items-center">
      <h3 class="m-0 text-xl text-800">Acteurs ({{ actors?.length || 0 }})</h3>
    </div>

    <div class="flex gap-2">
      <InputText v-model="newActorName" placeholder="Nouvel acteur (ex: Client)" @keyup.enter="addActor" class="flex-1" />
      <Button icon="pi pi-plus" :loading="isAdding" @click="addActor" />
    </div>

    <ul class="list-none p-0 m-0 flex flex-column gap-2">
      <li v-if="actors?.length === 0" class="text-500 font-italic text-sm text-center p-3 border-1 border-dashed surface-border border-round">
        Aucun acteur défini.
      </li>

      <li v-for="actor in actors" :key="actor.id" class="flex align-items-center justify-content-between p-2 border-1 surface-border border-round surface-0 shadow-1 transition-colors transition-duration-200 hover:surface-100">

        <template v-if="editingActorId === actor.id">
          <InputText v-model="editingName" @keyup.enter="saveEdit" class="flex-1 mr-2 p-inputtext-sm" autofocus />
          <div class="flex gap-1">
            <Button icon="pi pi-check" severity="success" text rounded @click="saveEdit" :loading="isUpdating" />
            <Button icon="pi pi-times" severity="secondary" text rounded @click="cancelEdit" :disabled="isUpdating" />
          </div>
        </template>

        <template v-else>
          <span class="font-medium text-700 flex align-items-center">
            <i class="pi pi-user mr-2 text-primary"></i>
            {{ actor.name }}
          </span>
          <div class="flex gap-1">
            <Button icon="pi pi-pencil" severity="info" text rounded aria-label="Modifier" @click="startEdit(actor)" />
            <Button icon="pi pi-trash" severity="danger" text rounded aria-label="Supprimer" @click="confirmRemove(actor.id)" />
          </div>
        </template>

      </li>
    </ul>
  </div>
</template>