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
  targetMultiplicity: '1..1'
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
    newAssoc.value.name = ''; // Reset champ
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

  // 1. On démarre un Flowchart (orientation Left-to-Right)
  let code = "flowchart LR\n";

  // 2. Définition des styles (Merise strict : rectangles bleus, ronds blancs)
  code += "  classDef entityClass fill:#eef,stroke:#333,stroke-width:1px,rx:5,ry:5;\n";
  code += "  classDef assocClass fill:#fff,stroke:#333,stroke-width:2px,color:#000;\n";

  // 3. Dessiner les Entités (rectangles)
  props.entries.forEach(e => {
    // Remplacer les espaces par underscores pour les IDs de nœuds Mermaid
    const entityNodeId = `ent_${e.name.replace(/\s+/g, '_')}`;
    code += `  ${entityNodeId}[${e.name}]:::entityClass;\n`;
  });

  // 4. Dessiner les Associations (ronds) et les connections avec cardinalités
  associations.value.forEach(a => {
    const srcNodeId = `ent_${a.sourceName.replace(/\s+/g, '_')}`;
    const tgtNodeId = `ent_${a.targetName.replace(/\s+/g, '_')}`;
    // Créer un ID unique pour le rond de l'association
    const assocNodeId = `assoc_${a.name.replace(/\s+/g, '_')}_${a.id.replace(/-/g, '_')}`;

    // Récupérer le texte de cardinalité Merise (ex: 0,N)
    const srcCardText = multiplicities.find(m => m.value === a.sourceMultiplicity)?.text || '0,N';
    const tgtCardText = multiplicities.find(m => m.value === a.targetMultiplicity)?.text || '1,1';

    // A. Définir le rond de l'association
    code += `  ${assocNodeId}((${a.name})):::assocClass;\n`;

    // B. Créer la connection 1 : Entité Source ---|Card| Rond
    code += `  ${srcNodeId} ---|${srcCardText}| ${assocNodeId};\n`;

    // C. Créer la connection 2 : Rond ---|Card| Entité Cible
    code += `  ${assocNodeId} ---|${tgtCardText}| ${tgtNodeId};\n`;
  });

  try {
    container.removeAttribute('data-processed');
    container.innerHTML = ""; // Vider le conteneur
    const { svg } = await mermaid.render('svg-mcd-merise-' + props.projectId, code);
    container.innerHTML = svg;
  } catch (error) {
    console.error("Mermaid Render Error", error);
    container.innerHTML = `<div class="p-4 text-red-500">Erreur de rendu du MCD Merise. Vérifiez les noms de vos entités.</div>`;
  }
};

onMounted(loadAssociations);
</script>

<template>
  <div class="grid p-fluid">
    <div class="col-12 lg:col-4">
      <Card class="mb-4">
        <template #title>Nouvelle Relation</template>
        <template #content>
          <div class="flex flex-column gap-3">
            <div>
              <label class="font-bold block mb-2">Source (De)</label>
              <Dropdown v-model="newAssoc.sourceId" :options="entries" optionLabel="name" optionValue="id" placeholder="Choisir l'entité" />
            </div>

            <div class="grid">
              <div class="col-6">
                <label class="font-bold block mb-2">Card. Source</label>
                <Dropdown v-model="newAssoc.sourceMultiplicity" :options="multiplicities" optionLabel="label" optionValue="value" />
              </div>
              <div class="col-6">
                <label class="font-bold block mb-2">Card. Cible</label>
                <Dropdown v-model="newAssoc.targetMultiplicity" :options="multiplicities" optionLabel="label" optionValue="value" />
              </div>
            </div>

            <div>
              <label class="font-bold block mb-2">Verbe / Nom</label>
              <InputText v-model="newAssoc.name" placeholder="ex: possède, contient..." />
            </div>

            <div>
              <label class="font-bold block mb-2">Cible (Vers)</label>
              <Dropdown v-model="newAssoc.targetId" :options="entries" optionLabel="name" optionValue="id" placeholder="Choisir l'entité" />
            </div>

            <Button label="Ajouter la relation" icon="pi pi-plus" @click="saveAssociation" :loading="loading" />
          </div>
        </template>
      </Card>

      <Card>
        <template #title>Relations existantes</template>
        <template #content>
          <DataTable :value="associations" class="p-datatable-sm" responsiveLayout="scroll">
            <template #empty>Aucune relation définie.</template>
            <Column header="Relation">
              <template #body="sp">
                <small>{{ sp.data.sourceName }} <b>{{ sp.data.name }}</b> {{ sp.data.targetName }}</small>
              </template>
            </Column>
            <Column style="width: 3rem">
              <template #body="sp">
                <Button icon="pi pi-trash" severity="danger" text rounded @click="deleteAssoc(sp.data.id)" />
              </template>
            </Column>
          </DataTable>
        </template>
      </Card>
    </div>

    <div class="col-12 lg:col-8">
      <Card class="h-full">
        <template #title>Aperçu du MCD</template>
        <template #content>
          <div class="mcd-viewer border-round surface-100 p-4 flex justify-content-center align-items-center shadow-inner" style="min-height: 500px;">
            <div id="mermaid-mcd"></div>
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