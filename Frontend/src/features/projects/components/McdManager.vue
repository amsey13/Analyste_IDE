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
import Checkbox from 'primevue/checkbox';

const props = defineProps({
  projectId: { type: String, required: true },
  entries: { type: Array, default: () => [] } // Liste des entités du dictionnaire
});

const toast = useToast();
const associations = ref([]);
const rules = ref([]);
const loading = ref(false);

// Formulaire pour une nouvelle association
const newAssoc = ref({
  sourceId: null,
  targetId: null,
  name: '',
  sourceMultiplicity: '0..N',
  targetMultiplicity: '1..1',
  isRelative: false,
  isCif: false,
  isInheritance: false,
  ruleId: null
});

const multiplicities = [
  { label: '0..N', value: '0..N', text: '0,N' },
  { label: '1..N', value: '1..N', text: '1,N' },
  { label: '0..1', value: '0..1', text: '0,1' },
  { label: '1..1', value: '1..1', text: '1,1' }
];

mermaid.initialize({
  startOnLoad: false,
  theme: 'base',
  themeVariables: { primaryColor: '#6366f1', edgeLabelBackground:'#ffffff' }
});

// On charge les associations ET les règles
const loadInitialData = async () => {
  try {
    const [assocsData, rulesData] = await Promise.all([
      SupportFeatureService.getAssociations(props.projectId),
      SupportFeatureService.getBusinessRules(props.projectId)
    ]);
    associations.value = assocsData || [];
    rules.value = rulesData || [];
    renderMcd();
  } catch (e) {
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de charger les données',life: 4000 });
  }
};

const saveAssociation = async () => {
  if (newAssoc.value.isInheritance) {
    newAssoc.value.name = "est un";
    newAssoc.value.sourceMultiplicity = "0..1";
    newAssoc.value.targetMultiplicity = "1..1";
  }
  if (!newAssoc.value.sourceId || !newAssoc.value.targetId || !newAssoc.value.name) {
    toast.add({ severity: 'warn', summary: 'Attention', detail: 'Veuillez remplir les entités source et cible et le verbe/nom.',life: 3000 });
    return;
  }

  loading.value = true;
  try {
    await SupportFeatureService.addAssociation(props.projectId, newAssoc.value);

    newAssoc.value.name = '';
    newAssoc.value.isRelative = false;
    newAssoc.value.isCif = false;
    newAssoc.value.isInheritance = false;
    newAssoc.value.ruleId = null;

    // On recharge juste les associations
    associations.value = await SupportFeatureService.getAssociations(props.projectId);
    renderMcd();

    toast.add({ severity: 'success', summary: 'Succès', detail: 'Relation ajoutée avec succès',life: 3000 });
  } catch (e) {
    console.error(e);
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de l\'ajout',life: 4000 });
  } finally {
    loading.value = false;
  }
};

const deleteAssoc = async (id) => {
  await SupportFeatureService.deleteAssociation(id);
  //  On recharge juste les associations
  associations.value = await SupportFeatureService.getAssociations(props.projectId);
  renderMcd();
};

const renderMcd = async () => {
  await nextTick();
  const container = document.getElementById('mermaid-mcd');
  if (!container) return;

  let code = "flowchart LR\n";
  // Styles Merise
  code += "  classDef entityClass fill:#eef,stroke:#333,stroke-width:1px,rx:5,ry:5;\n";
  code += "  classDef assocClass fill:#fff,stroke:#333,stroke-width:2px,color:#000;\n";
  // Style spécial pour la CIF (Bordure rouge/violette pour la distinguer)
  code += "  classDef cifClass fill:#fff,stroke:#d946ef,stroke-width:3px,color:#d946ef;\n";
  code += "  classDef inheritClass fill:#fef08a,stroke:#ca8a04,stroke-width:2px,color:#854d0e;\n";
  // Dessiner les Entités
  props.entries.forEach(e => {
    const entityNodeId = `ent_${e.name.replace(/\s+/g, '_')}`;
    code += `  ${entityNodeId}[${e.name}]:::entityClass;\n`;
  });

  // Dessiner les Associations
  associations.value.forEach(a => {
    const srcNodeId = `ent_${a.sourceName.replace(/\s+/g, '_')}`;
    const tgtNodeId = `ent_${a.targetName.replace(/\s+/g, '_')}`;

    // --- 1. CAS DE L'HÉRITAGE (SPÉCIALISATION) ---
    if (a.isInheritance) {
      const inheritNodeId = `inh_${a.id.replace(/-/g, '_')}`;

      // On crée un hexagone/triangle (Δ) jaune pour représenter l'héritage
      code += `  ${inheritNodeId}{{"Δ"}}:::inheritClass;\n`;

      // Lien : L'enfant (Source) va vers le triangle...
      code += `  ${srcNodeId} --- ${inheritNodeId};\n`;
      // ... et le triangle pointe vers le parent (Cible) avec une flèche
      code += `  ${inheritNodeId} --> ${tgtNodeId};\n`;

    }
    // --- 2. CAS CLASSIQUE (ASSOCIATION, CIF, RELATIF) ---
    else {
      const assocNodeId = `assoc_${a.name.replace(/\s+/g, '_')}_${a.id.replace(/-/g, '_')}`;

      let srcCardText = multiplicities.find(m => m.value === a.sourceMultiplicity)?.text || '0,N';
      let tgtCardText = multiplicities.find(m => m.value === a.targetMultiplicity)?.text || '1,1';

      if (a.isRelative) {
        if (a.sourceMultiplicity.includes('1')) srcCardText += ' (R)';
        if (a.targetMultiplicity.includes('1')) tgtCardText += ' (R)';
      }

      // --- LOGIQUE CIF ---
      let nodeClass = a.isCif ? "cifClass" : "assocClass";
      let nodeLabel = a.isCif ? `${a.name}<br><b>(CIF)</b>` : a.name;

      // Création du rond
      code += `  ${assocNodeId}(("${nodeLabel}")):::${nodeClass};\n`;

      // Dessin du lien Source <--> Association
      if (a.isCif && a.sourceMultiplicity.endsWith('1')) {
        code += `  ${assocNodeId} -->|"${srcCardText}"| ${srcNodeId};\n`;
      } else {
        code += `  ${srcNodeId} ---|"${srcCardText}"| ${assocNodeId};\n`;
      }

      // Dessin du lien Association <--> Cible
      if (a.isCif && a.targetMultiplicity.endsWith('1')) {
        code += `  ${assocNodeId} -->|"${tgtCardText}"| ${tgtNodeId};\n`;
      } else {
        code += `  ${assocNodeId} ---|"${tgtCardText}"| ${tgtNodeId};\n`;
      }
    }
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

//  Appel de la nouvelle fonction de chargement
onMounted(loadInitialData);
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
              <label class="font-bold text-sm mb-1 block text-600">Entité Source <span v-if="newAssoc.isInheritance" class="text-yellow-600">(Enfant)</span></label>
              <Dropdown v-model="newAssoc.sourceId" :options="entries" optionLabel="name" optionValue="id" placeholder="Ex: Utilisateur" class="w-full" />
            </div>

            <div v-if="!newAssoc.isInheritance" class="grid formgrid mb-3">
              <div class="col-3">
                <label class="font-bold text-sm mb-1 block text-600">Card.</label>
                <Dropdown v-model="newAssoc.sourceMultiplicity" :options="multiplicities" optionLabel="text" optionValue="value" class="w-full" />
              </div>
              <div class="col-6">
                <label class="font-bold text-sm mb-1 block text-600 text-center">Verbe / Nom</label>
                <InputText v-model="newAssoc.name" placeholder="ex: possède" class="text-center font-bold text-primary w-full" />
              </div>
              <div class="col-3">
                <label class="font-bold text-sm mb-1 block text-600 text-right">Card.</label>
                <Dropdown v-model="newAssoc.targetMultiplicity" :options="multiplicities" optionLabel="text" optionValue="value" class="w-full" />
              </div>
            </div>

            <div class="field mb-3">
              <label class="font-bold text-sm mb-1 block text-600">Entité Cible <span v-if="newAssoc.isInheritance" class="text-yellow-600">(Parent)</span></label>
              <Dropdown v-model="newAssoc.targetId" :options="entries" optionLabel="name" optionValue="id" placeholder="Ex: Produit" class="w-full" />
            </div>

            <div class="field mb-3 border-top-1 surface-border pt-3">
              <label class="font-bold text-sm mb-1 block text-600">
                <i class="pi pi-book text-primary mr-1"></i> Règle de Gestion associée <span class="text-400 font-normal">(Optionnel)</span>
              </label>
              <Dropdown v-model="newAssoc.ruleId" :options="rules" optionLabel="code" optionValue="id" placeholder="Lier à une règle (Ex: RG-01)" class="w-full" showClear>
                <template #option="slotProps">
                  <div class="flex flex-column">
                    <span class="font-bold">{{ slotProps.option.code }}</span>
                    <span class="text-sm text-500 overflow-hidden white-space-nowrap text-overflow-ellipsis" style="max-width: 200px;">
                      {{ slotProps.option.description }}
                    </span>
                  </div>
                </template>
              </Dropdown>
            </div>

            <div class="flex flex-column gap-2 mb-4 mt-2">

              <div class="field-checkbox flex align-items-center bg-yellow-50 p-2 border-round m-0">
                <Checkbox v-model="newAssoc.isInheritance" inputId="isInheritance" :binary="true" />
                <label for="isInheritance" class="ml-2 mb-0 font-bold text-yellow-800 text-sm cursor-pointer">
                  Héritage / Spécialisation (Δ)
                </label>
              </div>

              <div v-if="!newAssoc.isInheritance" class="field-checkbox flex align-items-center bg-orange-50 p-2 border-round m-0">
                <Checkbox v-model="newAssoc.isRelative" inputId="isRelative" :binary="true" />
                <label for="isRelative" class="ml-2 mb-0 font-bold text-orange-800 text-sm cursor-pointer">
                  Identification relative (R)
                </label>
              </div>

              <div v-if="!newAssoc.isInheritance" class="field-checkbox flex align-items-center bg-purple-50 p-2 border-round m-0">
                <Checkbox v-model="newAssoc.isCif" inputId="isCif" :binary="true" />
                <label for="isCif" class="ml-2 mb-0 font-bold text-purple-800 text-sm cursor-pointer">
                  Contrainte d'Intégrité Fonctionnelle (CIF)
                </label>
              </div>

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
                <div class="flex flex-column gap-1">
                  <div class="flex align-items-center gap-2 text-sm">

                    <template v-if="sp.data.isInheritance">
                      <span class="font-semibold">{{ sp.data.sourceName }}</span>
                      <span class="text-yellow-600 font-bold mx-2">──Δ──></span>
                      <span class="font-semibold">{{ sp.data.targetName }}</span>
                      <span class="ml-2 text-xs bg-yellow-100 text-yellow-800 px-2 py-1 border-round font-bold" title="Héritage"> (Héritage)</span>
                    </template>

                    <template v-else>
                      <span class="font-semibold">{{ sp.data.sourceName }}</span>
                      <span class="text-500">({{ sp.data.sourceMultiplicity.replace('..', ',') }})</span>
                      <span class="text-primary font-italic">{{ sp.data.name }}</span>
                      <span class="text-500">({{ sp.data.targetMultiplicity.replace('..', ',') }})</span>
                      <span class="font-semibold">{{ sp.data.targetName }}</span>

                      <span v-if="sp.data.isRelative" class="ml-1 text-xs bg-orange-100 text-orange-700 px-2 py-1 border-round font-bold" title="Identification Relative"> (R)</span>
                      <span v-if="sp.data.isCif" class="ml-1 text-xs bg-purple-100 text-purple-700 px-2 py-1 border-round font-bold" title="Contrainte d'Intégrité Fonctionnelle"> (CIF)</span>
                    </template>

                  </div>

                  <div v-if="sp.data.ruleCode" class="mt-1">
                    <span class="text-xs bg-blue-100 text-blue-800 px-2 py-1 border-round font-bold inline-flex align-items-center gap-1">
                      <i class="pi pi-link text-xs"></i> Justifié par : {{ sp.data.ruleCode }}
                    </span>
                  </div>
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