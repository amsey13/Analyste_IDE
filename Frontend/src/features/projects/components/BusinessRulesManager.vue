<script setup>
import { ref, onMounted } from 'vue';
import { SupportFeatureService } from '../api/SupportFeatureService.js';
import { useToast } from 'primevue/usetoast';
import { useConfirm } from 'primevue/useconfirm';
import Card from 'primevue/card';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import ConfirmDialog from 'primevue/confirmdialog';

const props = defineProps({
  projectId: { type: String, required: true }
});
const emit = defineEmits(['refresh']);

const toast = useToast();
const confirm = useConfirm();
const rules = ref([]);
const loading = ref(false);

const newRule = ref({
  code: '',
  description: ''
});

const loadRules = async () => {
  try {
    const data = await SupportFeatureService.getBusinessRules(props.projectId);
    rules.value = data || [];
  } catch (e) {
    console.error("Erreur chargement:", e);
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de charger les règles' });
  }
};

const saveRule = async () => {
  if (!newRule.value.code || !newRule.value.description) {
    toast.add({ severity: 'warn', summary: 'Attention', detail: 'Veuillez remplir le code et la description.' });
    return;
  }

  loading.value = true;
  try {
    await SupportFeatureService.addBusinessRule(props.projectId, newRule.value);
    newRule.value.code = '';
    newRule.value.description = '';
    await loadRules();
    toast.add({ severity: 'success', summary: 'Succès', detail: 'Règle de gestion ajoutée avec succès', life: 3000 });
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de l\'ajout' });
  } finally {
    loading.value = false;
  }
};

// L'ancienne fonction de suppression
const deleteRule = async (id) => {
  try {
    await SupportFeatureService.deleteBusinessRule(id);
    await loadRules();
    toast.add({ severity: 'success', summary: 'Supprimée', detail: 'La règle a été supprimée.', life: 3000 });
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de supprimer cette règle.' });
  }
};
const confirmDelete = (id, code) => {
  confirm.require({
    message: `Êtes-vous sûr de vouloir supprimer la règle ${code} ? Cette action est irréversible.`,
    header: 'Confirmation de suppression',
    icon: 'pi pi-exclamation-triangle',
    rejectProps: { label: 'Annuler', severity: 'secondary', outlined: true },
    acceptProps: { label: 'Supprimer', severity: 'danger' },
    accept: () => {
      deleteRule(id);
    }
  });
};

const generateAutoCode = () => {
  const count = (rules.value ? rules.value.length : 0) + 1;
  newRule.value.code = `RG-${count.toString().padStart(2, '0')}`;
};

const generatingAi = ref(false);

const generateMcdAi = async () => {
  generatingAi.value = true;
  try {
    await SupportFeatureService.generateMcdWithAi(props.projectId);
    emit('refresh');
    toast.add({
      severity: 'success',
      summary: 'Magie opérée 🪄',
      detail: 'Le Dictionnaire et le MCD ont été générés ! Allez voir les onglets suivants.',
      life: 6000
    });
  } catch (e) {
    console.error(e);
    toast.add({
      severity: 'error',
      summary: 'Erreur IA',
      detail: 'Impossible de générer les modèles. Veuillez réessayer.',
      life: 5000
    });
  } finally {
    generatingAi.value = false;
  }
};

onMounted(loadRules);
</script>

<template>
  <div class="grid h-full">

    <div class="col-12 lg:col-4">
      <Card class="shadow-2 border-round-xl h-full">
        <template #title>
          <div class="flex align-items-center gap-2 border-bottom-1 surface-border pb-3">
            <i class="pi pi-book text-primary text-2xl"></i>
            <span class="text-xl font-bold">Nouvelle Règle</span>
          </div>
        </template>
        <template #content>
          <div class="p-fluid mt-3">

            <div class="field mb-4">
              <label class="font-semibold text-sm mb-2 block text-700">Identifiant (Code)</label>
              <div class="p-inputgroup">
                <span class="p-inputgroup-addon bg-primary-reverse border-primary-100">
                  <i class="pi pi-tag text-primary"></i>
                </span>
                <InputText v-model="newRule.code" placeholder="Ex: RG-01" class="font-bold text-primary p-inputtext-lg" />
                <Button icon="pi pi-bolt" severity="secondary" @click="generateAutoCode" title="Générer un code automatique" class="px-3" />
              </div>
              <small class="text-500 mt-1 block">Utilisez l'éclair pour générer un code auto.</small>
            </div>

            <div class="field mb-5">
              <label class="font-semibold text-sm mb-2 block text-700">Description métier</label>
              <Textarea v-model="newRule.description" rows="6" placeholder="Ex: Un client peut posséder un ou plusieurs comptes bancaires, mais un compte bancaire appartient à un seul client..." class="w-full text-base border-round-md" autoResize />
            </div>

            <Button label="Enregistrer la règle" icon="pi pi-check" @click="saveRule" :loading="loading" severity="primary" size="large" class="w-full font-bold border-round-lg shadow-2" />
          </div>
        </template>
      </Card>
    </div>

    <div class="col-12 lg:col-8">
      <Card class="shadow-2 border-round-xl h-full">
        <template #title>
          <div class="flex align-items-center justify-content-between border-bottom-1 surface-border pb-3">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-list text-primary text-2xl"></i>
              <span class="text-xl font-bold">Référentiel des Règles de Gestion</span>
            </div>
          </div>
        </template>
        <template #content>
          <DataTable :value="rules" class="p-datatable-sm mt-3" responsiveLayout="scroll" :paginator="true" :rows="8" stripedRows>
            <template #empty>
              <div class="text-center p-5 text-500 surface-50 border-round-lg border-1 surface-border mt-3">
                <i class="pi pi-inbox text-5xl mb-3 text-400"></i>
                <p class="m-0 text-lg font-semibold text-700">Aucune règle définie</p>
                <p class="text-sm mt-2">Commencez par ajouter les règles métier de votre projet dans le formulaire à gauche.</p>
              </div>
            </template>

            <Column field="code" header="Code" style="width: 15%">
              <template #body="slotProps">
                <span class="bg-blue-50 text-blue-700 font-bold px-3 py-2 border-round-2xl border-1 border-blue-200 text-sm inline-block shadow-1">
                  {{ slotProps.data.code }}
                </span>
              </template>
            </Column>

            <Column field="description" header="Description" style="width: 75%">
              <template #body="slotProps">
                <span class="text-700 line-height-3 text-base">{{ slotProps.data.description }}</span>
              </template>
            </Column>

            <Column style="width: 10%" bodyStyle="text-align: center">
              <template #body="slotProps">
                <Button icon="pi pi-trash" severity="danger" text rounded aria-label="Supprimer" @click="confirmDelete(slotProps.data.id, slotProps.data.code)" title="Supprimer cette règle" />
              </template>
            </Column>
          </DataTable>
        </template>
      </Card>
    </div>
  </div>
</template>

<style scoped>
:deep(.p-inputgroup-addon) {
  min-width: 3rem;
  justify-content: center;
}
:deep(.p-datatable .p-datatable-tbody > tr) {
  transition: background-color 0.2s;
}
:deep(.p-datatable .p-datatable-tbody > tr:hover) {
  background-color: var(--surface-hover);
}
</style>