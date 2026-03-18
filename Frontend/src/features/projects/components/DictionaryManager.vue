<script setup>
import { ref } from 'vue';
import { useConfirm } from 'primevue/useconfirm';
import { useToast } from 'primevue/usetoast';
import { SupportFeatureService } from '../api/SupportFeatureService.js';

import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Button from 'primevue/button';
import Dialog from 'primevue/dialog';
import InputText from 'primevue/inputtext';
import Textarea from 'primevue/textarea';
import Dropdown from 'primevue/dropdown';
import Checkbox from 'primevue/checkbox';

// Props & Emits
const props = defineProps({
  projectId: {
    type: String,
    required: true
  },
  entries: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['refresh']); // Permet de dire au parent de recharger les données après une modif

// Services
const confirm = useConfirm();
const toast = useToast();

// États pour le tableau
const expandedRows = ref({}); // Gère quelles lignes sont dépliées

// États pour la modale Entité
const entryDialog = ref(false);
const isEditingEntry = ref(false);
const currentEntry = ref({ name: '', description: '' });

// États pour la modale Attribut
const attributeDialog = ref(false);
const isEditingAttribute = ref(false);
const selectedEntryId = ref(null); // Savoir à quelle entité on ajoute l'attribut
const currentAttribute = ref({
  name: '', dataType: 'VARCHAR', size: '', primaryKey: false, notNull: false, description: ''
});

// Options pour le Dropdown des types SQL
const dataTypes = ref(['VARCHAR', 'INT', 'BOOLEAN', 'DATE', 'DATETIME', 'DECIMAL', 'TEXT']);
const isSuggesting = ref(false);
const suggestDialog = ref(false);
const suggestions = ref([]);
const selectedSuggestions = ref([]);

// ==========================================
// --- GESTION DES ENTITÉS ---
// ==========================================
const fetchSuggestions = async () => {
  isSuggesting.value = true;
  try {
    const data = await SupportFeatureService.suggestDictionary(props.projectId);
    if (data && data.length > 0) {
      suggestions.value = data;
      selectedSuggestions.value = [...data]; // Tout est coché par défaut
      suggestDialog.value = true;
    } else {
      toast.add({ severity: 'info', summary: 'Information', detail: 'Aucune entité claire trouvée dans ces User Stories.', life: 3000 });
    }
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Erreur IA', detail: 'Échec de la génération Mistral.', life: 3000 });
  } finally {
    isSuggesting.value = false;
  }
};

const applySuggestions = async () => {
  try {
    // Sauvegarde séquentielle des entités et de leurs attributs
    for (const entry of selectedSuggestions.value) {
      const savedEntry = await SupportFeatureService.addDictionaryEntry(props.projectId, entry);

      if (entry.attributes && entry.attributes.length > 0) {
        for (const attr of entry.attributes) {
          await SupportFeatureService.addDictionaryAttribute(savedEntry.id, attr);
        }
      }
    }
    toast.add({ severity: 'success', summary: 'Succès', detail: 'Dictionnaire généré !', life: 3000 });
    suggestDialog.value = false;
    emit('refresh'); // Met à jour le tableau principal
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Problème lors de la sauvegarde.', life: 3000 });
  }
};

const openNewEntry = () => {
  currentEntry.value = { name: '', description: '' };
  isEditingEntry.value = false;
  entryDialog.value = true;
};

const editEntry = (entry) => {
  currentEntry.value = { ...entry }; // Copie pour ne pas modifier direct
  isEditingEntry.value = true;
  entryDialog.value = true;
};

const saveEntry = async () => {
  try {
    if (isEditingEntry.value) {
      await SupportFeatureService.updateDictionaryEntry(currentEntry.value.id, currentEntry.value);
      toast.add({ severity: 'success', summary: 'Succès', detail: 'Entité mise à jour', life: 3000 });
    } else {
      await SupportFeatureService.addDictionaryEntry(props.projectId, currentEntry.value);
      toast.add({ severity: 'success', summary: 'Succès', detail: 'Entité créée', life: 3000 });
    }
    entryDialog.value = false;
    emit('refresh'); // On demande au parent de recharger le projet
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la sauvegarde', life: 3000 });
  }
};

const confirmDeleteEntry = (entry) => {
  confirm.require({
    message: `Voulez-vous vraiment supprimer l'entité "${entry.name}" et tous ses attributs ?`,
    header: 'Confirmation',
    icon: 'pi pi-exclamation-triangle',
    accept: async () => {
      try {
        await SupportFeatureService.deleteDictionaryEntry(entry.id);
        toast.add({ severity: 'success', summary: 'Succès', detail: 'Entité supprimée', life: 3000 });
        emit('refresh');
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la suppression', life: 3000 });
      }
    }
  });
};

// ==========================================
// --- GESTION DES ATTRIBUTS ---
// ==========================================

const openNewAttribute = (entry) => {
  selectedEntryId.value = entry.id;
  currentAttribute.value = { name: '', dataType: 'VARCHAR', size: '', primaryKey: false, notNull: false, description: '' };
  isEditingAttribute.value = false;
  attributeDialog.value = true;

  // Ouvre automatiquement la ligne pour voir l'attribut ajouté
  if (!expandedRows.value[entry.id]) {
    expandedRows.value[entry.id] = true;
  }
};

const editAttribute = (attribute) => {
  currentAttribute.value = { ...attribute };
  isEditingAttribute.value = true;
  attributeDialog.value = true;
};

const saveAttribute = async () => {
  try {
    if (isEditingAttribute.value) {
      await SupportFeatureService.updateDictionaryAttribute(currentAttribute.value.id, currentAttribute.value);
      toast.add({ severity: 'success', summary: 'Succès', detail: 'Attribut mis à jour', life: 3000 });
    } else {
      await SupportFeatureService.addDictionaryAttribute(selectedEntryId.value, currentAttribute.value);
      toast.add({ severity: 'success', summary: 'Succès', detail: 'Attribut ajouté', life: 3000 });
    }
    attributeDialog.value = false;
    emit('refresh');
  } catch (error) {
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la sauvegarde', life: 3000 });
  }
};

const confirmDeleteAttribute = (attribute) => {
  confirm.require({
    message: `Supprimer l'attribut "${attribute.name}" ?`,
    header: 'Confirmation',
    icon: 'pi pi-exclamation-triangle',
    accept: async () => {
      try {
        await SupportFeatureService.deleteDictionaryAttribute(attribute.id);
        toast.add({ severity: 'success', summary: 'Succès', detail: 'Attribut supprimé', life: 3000 });
        emit('refresh');
      } catch (error) {
        toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de la suppression', life: 3000 });
      }
    }
  });
};
</script>

<template>
  <div class="dictionary-manager card p-4">
    <div class="flex justify-content-between align-items-center mb-4">
      <h2 class="text-xl font-bold m-0">Dictionnaire de Données</h2>
      <div class="flex gap-2">
        <Button label="Suggérer via IA" icon="pi pi-sparkles" severity="help" @click="fetchSuggestions" :loading="isSuggesting" />
        <Button label="Nouvelle Entité" icon="pi pi-plus" @click="openNewEntry" />
      </div>
    </div>

    <DataTable v-model:expandedRows="expandedRows" :value="entries" dataKey="id" class="p-datatable-sm">
      <template #empty>
        <div class="flex flex-column align-items-center justify-content-center p-6 text-center surface-50 border-round-xl border-1 surface-border mt-3">
          <i class="pi pi-sparkles text-purple-400 text-6xl mb-4"></i>
          <h3 class="text-900 font-bold mb-2 text-xl">Votre dictionnaire est vide</h3>
          <p class="text-600 mb-4 max-w-20rem line-height-3">
            Gagnez du temps ! Laissez l'IA analyser les User Stories de votre projet pour générer une première version de votre modèle de données.
          </p>
          <div class="flex gap-3">
            <Button label="Suggérer via l'IA" icon="pi pi-sparkles" severity="help" size="large" @click="fetchSuggestions" :loading="isSuggesting" />
            <Button label="Créer manuellement" icon="pi pi-plus" outlined severity="secondary" size="large" @click="openNewEntry" />
          </div>
        </div>
      </template>

      <Column expander style="width: 3rem" />

      <Column field="name" header="Nom de l'Entité" class="font-bold" />
      <Column field="description" header="Description" />

      <Column header="Actions" :exportable="false" style="min-width:12rem">
        <template #body="slotProps">
          <Button icon="pi pi-plus" label="Attribut" class="p-button-text p-button-sm mr-2" @click="openNewAttribute(slotProps.data)" />
          <Button icon="pi pi-pencil" outlined rounded class="mr-2" @click="editEntry(slotProps.data)" />
          <Button icon="pi pi-trash" outlined rounded severity="danger" @click="confirmDeleteEntry(slotProps.data)" />
        </template>
      </Column>

      <template #expansion="slotProps">
        <div class="p-3 bg-gray-50 border-round">
          <h5 class="mt-0 mb-3">Attributs de l'entité "{{ slotProps.data.name }}"</h5>
          <DataTable :value="slotProps.data.attributes" dataKey="id" class="p-datatable-sm">
            <template #empty>
              <div class="text-sm text-gray-500 italic">Aucun attribut. Cliquez sur "+ Attribut" pour en ajouter.</div>
            </template>

            <Column field="name" header="Nom" />
            <Column field="dataType" header="Type" />
            <Column field="size" header="Taille" />
            <Column header="Clé Primaire">
              <template #body="attrProps">
                <i class="pi" :class="{ 'pi-check-circle text-green-500': attrProps.data.primaryKey, 'pi-times-circle text-red-500': !attrProps.data.primaryKey }"></i>
              </template>
            </Column>
            <Column header="Not Null">
              <template #body="attrProps">
                <i class="pi" :class="{ 'pi-check-circle text-green-500': attrProps.data.notNull, 'pi-times-circle text-red-500': !attrProps.data.notNull }"></i>
              </template>
            </Column>
            <Column header="Actions" style="width: 8rem">
              <template #body="attrProps">
                <Button icon="pi pi-pencil" text rounded class="mr-2" @click="editAttribute(attrProps.data)" />
                <Button icon="pi pi-trash" text rounded severity="danger" @click="confirmDeleteAttribute(attrProps.data)" />
              </template>
            </Column>
          </DataTable>
        </div>
      </template>
    </DataTable>

    <Dialog v-model:visible="entryDialog" :style="{width: '450px'}" :modal="true">
      <template #header>
        <div class="flex align-items-center gap-2">
          <i class="pi pi-database text-primary text-xl"></i>
          <span class="font-bold text-xl">{{ isEditingEntry ? 'Modifier Entité' : 'Nouvelle Entité' }}</span>
        </div>
      </template>

      <div class="flex flex-column gap-4 mt-3">
        <div class="flex flex-column gap-2">
          <label for="entryName" class="font-bold text-900">Nom de l'entité</label>
          <InputText id="entryName" v-model.trim="currentEntry.name" required autofocus placeholder="Ex: Client, Facture..." class="w-full" />
        </div>
        <div class="flex flex-column gap-2">
          <label for="entryDesc" class="font-bold text-900">Description</label>
          <Textarea id="entryDesc" v-model="currentEntry.description" required rows="3" placeholder="Rôle de cette entité dans le système..." class="w-full" />
        </div>
      </div>

      <template #footer>
        <div class="flex justify-content-end gap-2 w-full mt-4">
          <Button label="Annuler" icon="pi pi-times" outlined severity="secondary" @click="entryDialog = false"/>
          <Button label="Sauvegarder" icon="pi pi-check" @click="saveEntry" :disabled="!currentEntry.name" />
        </div>
      </template>
    </Dialog>

    <Dialog v-model:visible="attributeDialog" :style="{width: '550px'}" :modal="true">
      <template #header>
        <div class="flex align-items-center gap-2">
          <i class="pi pi-tag text-primary text-xl"></i>
          <span class="font-bold text-xl">{{ isEditingAttribute ? 'Modifier Attribut' : 'Nouvel Attribut' }}</span>
        </div>
      </template>

      <div class="grid mt-3">
        <div class="col-12 flex flex-column gap-2 mb-2">
          <label for="attrName" class="font-bold text-900">Nom de l'attribut</label>
          <InputText id="attrName" v-model.trim="currentAttribute.name" required autofocus placeholder="Ex: email, date_creation..." class="w-full" />
        </div>

        <div class="col-7 flex flex-column gap-2 mb-2">
          <label for="attrType" class="font-bold text-900">Type de donnée</label>
          <Dropdown id="attrType" v-model="currentAttribute.dataType" :options="dataTypes" placeholder="Type SQL" class="w-full" />
        </div>
        <div class="col-5 flex flex-column gap-2 mb-2">
          <label for="attrSize" class="font-bold text-900">Taille (Opt.)</label>
          <InputText id="attrSize" v-model.trim="currentAttribute.size" placeholder="Ex: 255" class="w-full" />
        </div>

        <div class="col-12 mb-3 mt-2">
          <div class="flex flex-wrap gap-5 p-3 surface-100 border-round border-1 surface-border">
            <div class="flex align-items-center gap-2">
              <Checkbox id="attrPk" v-model="currentAttribute.primaryKey" :binary="true" />
              <label for="attrPk" class="mb-0 font-bold text-900 cursor-pointer">Clé Primaire</label>
            </div>
            <div class="flex align-items-center gap-2">
              <Checkbox id="attrNotNull" v-model="currentAttribute.notNull" :binary="true" />
              <label for="attrNotNull" class="mb-0 font-bold text-900 cursor-pointer">Not Null</label>
            </div>
          </div>
        </div>

        <div class="col-12 flex flex-column gap-2">
          <label for="attrDesc" class="font-bold text-900">Description</label>
          <Textarea id="attrDesc" v-model="currentAttribute.description" rows="2" placeholder="Informations complémentaires..." class="w-full" />
        </div>
      </div>

      <template #footer>
        <div class="flex justify-content-end gap-2 w-full mt-4">
          <Button label="Annuler" icon="pi pi-times" outlined severity="secondary" @click="attributeDialog = false"/>
          <Button label="Sauvegarder" icon="pi pi-check" @click="saveAttribute" :disabled="!currentAttribute.name || !currentAttribute.dataType" />
        </div>
      </template>
    </Dialog>
    <Dialog v-model:visible="suggestDialog" :style="{width: '750px'}" :modal="true">
      <template #header>
        <div class="flex align-items-center gap-2">
          <i class="pi pi-sparkles text-purple-500 text-xl"></i>
          <span class="font-bold text-xl">Propositions de l'IA</span>
        </div>
      </template>

      <div class="mt-3">
        <p class="text-600 mb-4">Voici les entités détectées d'après vos User Stories. Décochez celles que vous ne souhaitez pas intégrer au dictionnaire.</p>

        <DataTable v-model:selection="selectedSuggestions" :value="suggestions" dataKey="name" class="p-datatable-sm border-1 surface-border border-round">
          <Column selectionMode="multiple" headerStyle="width: 3rem"></Column>
          <Column field="name" header="Entité proposée" class="font-bold"></Column>
          <Column field="description" header="Description" style="width: 45%"></Column>
          <Column header="Attributs détectés">
            <template #body="slotProps">
              <span class="font-medium text-primary">{{ slotProps.data.attributes ? slotProps.data.attributes.length : 0 }} attribut(s)</span>
            </template>
          </Column>
        </DataTable>
      </div>

      <template #footer>
        <div class="flex justify-content-end gap-2 w-full mt-4">
          <Button label="Annuler" icon="pi pi-times" outlined severity="secondary" @click="suggestDialog = false"/>
          <Button label="Valider la sélection" icon="pi pi-check" @click="applySuggestions" :disabled="selectedSuggestions.length === 0" />
        </div>
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
/* Conserve ton ancien CSS pour le bg-gray-50 */
.bg-gray-50 {
  background-color: #f8fafc;
}

/* Améliorations de l'espacement des modales */
:deep(.custom-dialog) .p-dialog-content {
  padding-top: 1rem !important; /* Décolle le formulaire du header */
}

:deep(.custom-dialog) .p-dialog-header {
  border-bottom: 1px solid var(--surface-border);
  padding-bottom: 1rem;
}

:deep(.custom-dialog) .p-dialog-footer {
  border-top: 1px solid var(--surface-border);
  padding-top: 1rem;
}
</style>