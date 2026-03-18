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

// ==========================================
// --- GESTION DES ENTITÉS ---
// ==========================================

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
      <Button label="Nouvelle Entité" icon="pi pi-plus" @click="openNewEntry" />
    </div>

    <DataTable v-model:expandedRows="expandedRows" :value="entries" dataKey="id" class="p-datatable-sm">
      <template #empty>
        <div class="text-center p-4 text-gray-500">Aucune entité définie pour le moment.</div>
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

    <Dialog v-model:visible="entryDialog" :style="{width: '450px'}" :modal="true" class="p-fluid">
      <template #header>
        <div class="flex align-items-center gap-2">
          <i class="pi pi-database text-primary"></i>
          <span class="font-bold">Nouvelle Entité métier</span>
        </div>
      </template>

      <div class="flex flex-column gap-4 mt-3">
        <div class="field">
          <label for="entryName" class="font-semibold mb-2 block">Nom de l'objet</label>
          <InputText id="entryName" v-model.trim="currentEntry.name" placeholder="Ex: Client, Facture..." />
        </div>
        <div class="field">
          <label for="entryDesc" class="font-semibold mb-2 block">Description</label>
          <Textarea id="entryDesc" v-model="currentEntry.description" rows="3" placeholder="Rôle de cette entité..." />
        </div>
      </div>

      <template #footer>
        <Button label="Annuler" icon="pi pi-times" text severity="secondary" @click="entryDialog = false"/>
        <Button label="Enregistrer" icon="pi pi-check" @click="saveEntry" :disabled="!currentEntry.name" />
      </template>
    </Dialog>

    <Dialog v-model:visible="attributeDialog" :style="{width: '500px'}" :modal="true" class="p-fluid">
      <template #header>
        <div class="flex align-items-center gap-2">
          <i class="pi pi-tag text-primary"></i>
          <span class="font-bold">Configuration de l'attribut</span>
        </div>
      </template>

      <div class="formgrid grid mt-3">
        <div class="field col-12 mb-4">
          <label for="attrName" class="font-semibold mb-2 block">Nom technique</label>
          <InputText id="attrName" v-model.trim="currentAttribute.name" placeholder="Ex: email_client" />
        </div>

        <div class="field col-12 md:col-8">
          <label for="attrType" class="font-semibold mb-2 block">Type</label>
          <Dropdown id="attrType" v-model="currentAttribute.dataType" :options="dataTypes" />
        </div>

        <div class="field col-12 md:col-4">
          <label for="attrSize" class="font-semibold mb-2 block">Taille</label>
          <InputText id="attrSize" v-model.trim="currentAttribute.size" placeholder="255" />
        </div>

        <div class="col-12 mb-4">
          <div class="flex gap-4 p-3 surface-100 border-round">
            <div class="flex align-items-center">
              <Checkbox id="attrPk" v-model="currentAttribute.primaryKey" :binary="true" />
              <label for="attrPk" class="ml-2">Clé Primaire</label>
            </div>
            <div class="flex align-items-center">
              <Checkbox id="attrNotNull" v-model="currentAttribute.notNull" :binary="true" />
              <label for="attrNotNull" class="ml-2">Obligatoire</label>
            </div>
          </div>
        </div>

        <div class="field col-12">
          <label for="attrDesc" class="font-semibold mb-2 block">Note métier</label>
          <Textarea id="attrDesc" v-model="currentAttribute.description" rows="2" />
        </div>
      </div>

      <template #footer>
        <Button label="Annuler" icon="pi pi-times" text severity="secondary" @click="attributeDialog = false"/>
        <Button label="Ajouter" icon="pi pi-check" @click="saveAttribute" :disabled="!currentAttribute.name" />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
/* Ajoute un petit fond visuel pour distinguer la zone des attributs */
.bg-gray-50 {
  background-color: #f8fafc;
}
</style>