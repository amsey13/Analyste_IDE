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

// Formulaire pour une nouvelle association (ou modification)
const newAssoc = ref({
  id: null, // NOUVEAU: Permet de savoir si on est en mode édition
  sourceId: null,
  targetId: null,
  name: '',
  sourceMultiplicity: '0..N',
  targetMultiplicity: '1..1',
  isRelative: false,
  isCif: false,
  isInheritance: false,
  ruleId: null,
  attributes: [] // NOUVEAU: Liste des données portées
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

// --- NOUVEAU : Gestion des attributs dans le formulaire ---
const addAttributeToAssoc = () => {
  newAssoc.value.attributes.push({ name: '', dataType: 'VARCHAR', size: '255', primaryKey: false, notNull: false });
};

const removeAttributeFromAssoc = (index) => {
  newAssoc.value.attributes.splice(index, 1);
};

// --- NOUVEAU : Préparer le formulaire pour la modification ---
const editAssociation = (assoc) => {
  newAssoc.value = {
    ...assoc,
    attributes: assoc.attributes ? JSON.parse(JSON.stringify(assoc.attributes)) : [] // Deep copy pour éviter de modifier la table en direct
  };
};

// --- NOUVEAU : Réinitialiser le formulaire ---
const resetForm = () => {
  newAssoc.value = { id: null, sourceId: null, targetId: null, name: '', sourceMultiplicity: '0..N', targetMultiplicity: '1..1', isRelative: false, isCif: false, isInheritance: false, ruleId: null, attributes: [] };
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
    if (newAssoc.value.id) {
      // MODE MODIFICATION (Assure-toi d'avoir cette méthode dans ton Service côté JS)
      await SupportFeatureService.updateAssociation(newAssoc.value.id, newAssoc.value);
      toast.add({ severity: 'success', summary: 'Succès', detail: 'Relation modifiée avec succès',life: 3000 });
    } else {
      // MODE CRÉATION
      await SupportFeatureService.addAssociation(props.projectId, newAssoc.value);
      toast.add({ severity: 'success', summary: 'Succès', detail: 'Relation ajoutée avec succès',life: 3000 });
    }

    resetForm();

    // On recharge juste les associations
    associations.value = await SupportFeatureService.getAssociations(props.projectId);
    renderMcd();

  } catch (e) {
    console.error(e);
    toast.add({ severity: 'error', summary: 'Erreur', detail: 'Échec de l\'opération',life: 4000 });
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
  code += "  classDef entityClass fill:#eef,stroke:#333,stroke-width:1px,rx:5,ry:5;\n";
  code += "  classDef assocClass fill:#fff,stroke:#333,stroke-width:2px,color:#000;\n";
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

    if (a.isInheritance) {
      const inheritNodeId = `inh_${a.id.replace(/-/g, '_')}`;
      code += `  ${inheritNodeId}{{"Δ"}}:::inheritClass;\n`;
      code += `  ${srcNodeId} --- ${inheritNodeId};\n`;
      code += `  ${inheritNodeId} --> ${tgtNodeId};\n`;
    }
    else {
      const assocNodeId = `assoc_${a.name.replace(/\s+/g, '_')}_${a.id.replace(/-/g, '_')}`;

      let srcCardText = multiplicities.find(m => m.value === a.sourceMultiplicity)?.text || '0,N';
      let tgtCardText = multiplicities.find(m => m.value === a.targetMultiplicity)?.text || '1,1';

      if (a.isRelative) {
        if (a.sourceMultiplicity.includes('1')) srcCardText += ' (R)';
        if (a.targetMultiplicity.includes('1')) tgtCardText += ' (R)';
      }

      let nodeClass = a.isCif ? "cifClass" : "assocClass";
      let nodeLabel = a.isCif ? `<b>${a.name}</b><br/><i>(CIF)</i>` : `<b>${a.name}</b>`;

      if (a.attributes && a.attributes.length > 0) {
        nodeLabel += `<br/>---`;
        a.attributes.forEach(attr => {
          nodeLabel += `<br/>${attr.name}`;
        });
      }

      code += `  ${assocNodeId}(["${nodeLabel}"]):::${nodeClass};\n`;

      if (a.isCif && a.sourceMultiplicity.endsWith('1')) {
        code += `  ${assocNodeId} -->|"${srcCardText}"| ${srcNodeId};\n`;
      } else {
        code += `  ${srcNodeId} ---|"${srcCardText}"| ${assocNodeId};\n`;
      }

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

onMounted(loadInitialData);
</script>

<template>
  <div class="grid">
    <div class="col-12 lg:col-4 flex flex-column gap-4">

      <Card class="shadow-2 border-round-xl">
        <template #title>
          <div class="flex align-items-center gap-2">
            <i class="pi pi-link text-primary text-xl"></i>
            <span>{{ newAssoc.id ? 'Modifier Relation' : 'Nouvelle Relation' }}</span>
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

            <div v-if="!newAssoc.isInheritance" class="field mb-3 border-top-1 border-bottom-1 surface-border py-3 bg-surface-50 px-2 border-round">
              <div class="flex justify-content-between align-items-center mb-2">
                <label class="font-bold text-sm text-700 m-0"><i class="pi pi-database text-primary mr-1"></i> Données portées</label>
                <Button icon="pi pi-plus" label="Ajouter" size="small" outlined @click="addAttributeToAssoc" class="py-1 px-2 text-xs" />
              </div>

              <p v-if="newAssoc.attributes.length === 0" class="text-xs text-500 italic m-0">
                Aucune donnée (Ex: Date, Quantité...)
              </p>

              <div v-for="(attr, index) in newAssoc.attributes" :key="index" class="flex gap-2 align-items-center mt-2">
                <InputText v-model="attr.name" placeholder="Nom (ex: qte)" class="flex-1 text-sm" />
                <Dropdown v-model="attr.dataType" :options="['VARCHAR', 'INT', 'DATE', 'BOOLEAN', 'FLOAT']" placeholder="Type" class="w-7rem text-sm" />
                <Button icon="pi pi-times" severity="danger" text rounded @click="removeAttributeFromAssoc(index)" />
              </div>
            </div>

            <div class="field mb-3">
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

            <div class="flex gap-2">
              <Button v-if="newAssoc.id" label="Annuler" icon="pi pi-times" @click="resetForm" severity="secondary" outlined class="flex-1" />
              <Button :label="newAssoc.id ? 'Modifier' : 'Créer'" :icon="newAssoc.id ? 'pi pi-check' : 'pi pi-plus'" @click="saveAssociation" :loading="loading" severity="primary" class="flex-1" />
            </div>

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

                  <div v-if="sp.data.attributes && sp.data.attributes.length > 0" class="mt-2 pl-2 border-left-2 border-primary-200">
                    <span class="text-xs text-500 block mb-1"><i class="pi pi-database text-xs"></i> Données portées :</span>
                    <div class="flex flex-wrap gap-1">
                      <span v-for="attr in sp.data.attributes" :key="attr.name" class="text-xs bg-surface-100 text-700 px-2 py-1 border-round font-mono">
                        {{ attr.name }}
                      </span>
                    </div>
                  </div>

                </div>
              </template>
            </Column>
            <Column style="width: 6rem">
              <template #body="sp">
                <div class="flex gap-1 justify-content-end">
                  <Button icon="pi pi-pencil" severity="warning" text rounded aria-label="Éditer" @click="editAssociation(sp.data)" />
                  <Button icon="pi pi-trash" severity="danger" text rounded aria-label="Supprimer" @click="deleteAssoc(sp.data.id)" />
                </div>
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