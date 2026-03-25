<script setup>
import { ref, onMounted, nextTick } from 'vue';
import mermaid from 'mermaid';
import { SupportFeatureService } from '../api/SupportFeatureService.js';
import { useToast } from 'primevue/usetoast';
import Card from 'primevue/card';
import Dropdown from 'primevue/dropdown';
import InputText from 'primevue/inputtext';
import Button from 'primevue/button';
import DataTable from 'primevue/datatable';
import Column from 'primevue/column';
import Checkbox from 'primevue/checkbox'; // <-- Nouvel import pour la case à cocher

const props = defineProps({
  projectId: { type: String, required: true },
  entries: { type: Array, default: () => [] } // Liste des entités du dictionnaire
});

const toast = useToast();
const associations = ref([]);
const loading = ref(false);

// Formulaire pour une nouvelle association
const newAssoc = ref({
  sourceId: null,
  targetId: null,
  name: '',
  sourceMultiplicity: '0..N',
  targetMultiplicity: '1..1',
  isRelative: false // <-- Ajout du champ isRelative
});

const multiplicities = [
  { label: '0..N', value: '0..N', text: '0,N' },
  { label: '1..N', value: '1..N', text: '1,N' },
  { label: '0..1', value: '0..1', text: '0,1' },
  { label: '1..1', value: '1..1', text: '1,1' }
];

// Initialisation Mermaid
mermaid.initialize({
  startOnLoad: false,
  theme: 'base',
  themeVariables: { primaryColor: '#6366f1', edgeLabelBackground:'#ffffff' }
});

const loadAssociations = async () => {
  try {
    associations.value = await SupportFeatureService.getAssociations(props.projectId);
    renderMcd();
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de charger les relations' });
  }
};

const saveAssociation = async () => {
  if (!newAssoc.value.sourceId || !newAssoc.value.targetId || !newAssoc.value.name) return;

  loading.value = true;
  try {
    await SupportFeatureService.addAssociation(props.projectId, newAssoc.value);
    newAssoc.value.name = '';
    newAssoc.value.isRelative = false; // <-- On reset la checkbox après l'ajout
    await loadAssociations();
    toast.add({ severity: 'success', summary: 'Succès', detail: 'Relation ajoutée' });
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de l\'ajout' });
  } finally {
    loading.value = false;
  }
};

const deleteAssoc = async (id) => {
  await SupportFeatureService.deleteAssociation(id);
  await loadAssociations();
};

const renderMcd = async () => {
  await nextTick();
  const container = document.getElementById('mermaid-mcd');
  if (!container) return;

  let code = "flowchart LR\n";
  code += "  classDef entityClass fill:#eef,stroke:#333,stroke-width:1px,rx:5,ry:5;\n";
  code += "  classDef assocClass fill:#fff,stroke:#333,stroke-width:2px,color:#000;\n";

  // Dessiner les Entités
  props.entries.forEach(e => {
    const entityNodeId = `ent_${e.name.replace(/\s+/g, '_')}`;
    code += `  ${entityNodeId}[${e.name}]:::entityClass;\n`;
  });

  // Dessiner les Associations
  associations.value.forEach(a => {
    const srcNodeId = `ent_${a.sourceName.replace(/\s+/g, '_')}`;
    const tgtNodeId = `ent_${a.targetName.replace(/\s+/g, '_')}`;
    const assocNodeId = `assoc_${a.name.replace(/\s+/g, '_')}_${a.id.replace(/-/g, '_')}`;

    let srcCardText = multiplicities.find(m => m.value === a.sourceMultiplicity)?.text || '0,N';
    let tgtCardText = multiplicities.find(m => m.value === a.targetMultiplicity)?.text || '1,1';

    // --- MAGIE DU RELATIF ---
    // Si la relation est relative, on ajoute la mention (R) sur la patte 1,1 ou 0,1
    if (a.isRelative) {
      if (a.sourceMultiplicity.includes('1')) srcCardText += ' (R)';
      if (a.targetMultiplicity.includes('1')) tgtCardText += ' (R)';
    }

    code += `  ${assocNodeId}((${a.name})):::assocClass;\n`;
    code += `  ${srcNodeId} ---|"${srcCardText}"| ${assocNodeId};\n`;
    code += `  ${assocNodeId} ---|"${tgtCardText}"| ${tgtNodeId};\n`;
  });

  try {
    container.removeAttribute('data-processed');
    container.innerHTML = "";
    const { svg } = await mermaid.render('svg-mcd-merise-' + props.projectId, code);
    container.innerHTML = svg;
  } catch (error) {
    console.error("Mermaid Render Error", error);
    container.innerHTML = `<div class="p-4 text-red-500">Erreur de rendu du MCD Merise.</div>`;
  }
};

onMounted(loadAssociations);
</script>

<template>
  <div class="grid">
    <div class="col-12 lg:col-4 flex flex-column gap-4">

      <Card class="shadow-2 border-round-xl">
        <template #title>
          <div class="flex align-items-center gap-2">
            <i class="pi pi-link text-primary text-xl"></i>
            <span>Nouvelle Relation</span>
          </div>
        </template>
        <template #content>
          <div class="p-fluid">
            <div class="field mb-3">
              <label class="font-bold text-sm mb-1 block text-600">Entité Source</label>
              <Dropdown v-model="newAssoc.sourceId" :options="entries" optionLabel="name" optionValue="id" placeholder="Ex: Utilisateur" class="w-full" />
            </div>

            <div class="grid formgrid mb-3">
              <div class="col-3">
                <label class="font-bold text-sm mb-1 block text-600">Card.</label>
                <Dropdown v-model="newAssoc.sourceMultiplicity" :options="multiplicities" optionLabel="text" optionValue="value" class="w-full" />
              </div>
              <div class="col-6">
                <label class="font-bold text-sm mb-1 block text-600 text-center">Verbe</label>
                <InputText v-model="newAssoc.name" placeholder="ex: possède" class="text-center font-bold text-primary w-full" />
              </div>
              <div class="col-3">
                <label class="font-bold text-sm mb-1 block text-600 text-right">Card.</label>
                <Dropdown v-model="newAssoc.targetMultiplicity" :options="multiplicities" optionLabel="text" optionValue="value" class="w-full" />
              </div>
            </div>

            <div class="field mb-3">
              <label class="font-bold text-sm mb-1 block text-600">Entité Cible</label>
              <Dropdown v-model="newAssoc.targetId" :options="entries" optionLabel="name" optionValue="id" placeholder="Ex: Produit" class="w-full" />
            </div>

            <div class="field-checkbox mb-4 mt-2 flex align-items-center bg-blue-50 p-2 border-round">
              <Checkbox v-model="newAssoc.isRelative" inputId="isRelative" :binary="true" />
              <label for="isRelative" class="ml-2 mb-0 font-bold text-blue-800 text-sm cursor-pointer">
                Identification relative (Entité faible)
              </label>
            </div>

            <Button label="Créer la relation" icon="pi pi-plus" @click="saveAssociation" :loading="loading" severity="primary" class="w-full" />
          </div>
        </template>
      </Card>

      <Card class="shadow-2 border-round-xl">
        <template #title>
          <div class="flex align-items-center gap-2">
            <i class="pi pi-list text-primary text-xl"></i>
            <span>Relations établies</span>
          </div>
        </template>
        <template #content>
          <DataTable :value="associations" class="p-datatable-sm" responsiveLayout="scroll">
            <template #empty>
              <div class="text-center p-3 text-500">
                <i class="pi pi-inbox text-3xl mb-2"></i>
                <p class="m-0">Aucune relation définie.</p>
              </div>
            </template>
            <Column header="Détails de la relation">
              <template #body="sp">
                <div class="flex align-items-center gap-2 text-sm">
                  <span class="font-semibold">{{ sp.data.sourceName }}</span>
                  <span class="text-500">({{ sp.data.sourceMultiplicity.replace('..', ',') }})</span>
                  <span class="text-primary font-italic">{{ sp.data.name }}</span>
                  <span class="text-500">({{ sp.data.targetMultiplicity.replace('..', ',') }})</span>
                  <span class="font-semibold">{{ sp.data.targetName }}</span>

                  <span v-if="sp.data.isRelative" class="ml-1 text-xs bg-orange-100 text-orange-700 px-2 py-1 border-round font-bold" title="Identification Relative"> (R)</span>
                </div>
              </template>
            </Column>
            <Column style="width: 3rem">
              <template #body="sp">
                <Button icon="pi pi-times" severity="danger" text rounded aria-label="Supprimer" @click="deleteAssoc(sp.data.id)" />
              </template>
            </Column>
          </DataTable>
        </template>
      </Card>
    </div>

    <div class="col-12 lg:col-8">
      <Card class="h-full shadow-2 border-round-xl">
        <template #title>
          <div class="flex align-items-center justify-content-between">
            <div class="flex align-items-center gap-2">
              <i class="pi pi-share-alt text-primary text-xl"></i>
              <span>Modèle Conceptuel de Données</span>
            </div>
            <span class="text-xs bg-primary-100 text-primary-700 px-2 py-1 border-round">Mermaid.js</span>
          </div>
        </template>
        <template #content>
          <div class="mcd-viewer border-round surface-50 p-4 flex justify-content-center align-items-center border-1 border-300 relative" style="min-height: 600px;">

            <div v-if="associations.length === 0" class="text-center text-400 absolute">
              <i class="pi pi-sitemap" style="font-size: 4rem;"></i>
              <p class="mt-3 font-semibold text-lg">Votre canevas est vide</p>
              <p class="text-sm">Ajoutez votre première relation à gauche pour générer le diagramme.</p>
            </div>

            <div id="mermaid-mcd" class="w-full"></div>
          </div>
        </template>
      </Card>
    </div>
  </div>
</template>

<style scoped>
.mcd-viewer {
  background-image: radial-gradient(var(--surface-border) 1px, transparent 1px);
  background-size: 20px 20px;
  overflow: auto;
}
#mermaid-mcd {
  width: 100%;
}
</style>