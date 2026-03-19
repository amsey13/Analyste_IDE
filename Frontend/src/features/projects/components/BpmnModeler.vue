<script setup>
import { ref, shallowRef, toRaw, onMounted, onBeforeUnmount } from 'vue';
import Modeler from 'bpmn-js/lib/Modeler';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';
import { SupportFeatureService } from '../api/SupportFeatureService';

const props = defineProps({
  projectId: { type: String, required: true },
  initialXml: { type: String, default: null },
  actors: { type: Array, default: () => [] },
  userStories: { type: Array, default: () => [] }
});
const emit = defineEmits(['update:coverageScore']);

const emptyBpmn = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:custom="http://analytiq/schema/bpmn"
                  id="Definitions_1"
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:collaboration id="Collaboration_1" />
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_1" />
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

const container = ref(null);
let bpmnModeler = null;

const showPopover = ref(false);
const popoverPosition = ref({ top: '0px', left: '0px' });
const selectedTask = shallowRef(null);

const linkedUsIds = ref([]);
const updateLinkedUS = async () => {
  if (!selectedTask.value) return;
  const modeling = bpmnModeler.get('modeling');
  const newValue = linkedUsIds.value.join(',');
  modeling.updateProperties(toRaw(selectedTask.value), {
    'custom:linkedUserStories': newValue
  });
  await saveDiagram();
};
const isSaving = ref(false);
let saveTimeout = null;

const triggerAutoSave = () => {
  isSaving.value = true;
  if (saveTimeout) clearTimeout(saveTimeout);
  saveTimeout = setTimeout(async () => {
    await saveDiagram();
    isSaving.value = false;
  }, 1500);
};

const highlightOrphanTasks = () => {
  if (!bpmnModeler) return;

  const elementRegistry = bpmnModeler.get('elementRegistry');
  const canvas = bpmnModeler.get('canvas');

  // On récupère uniquement les éléments qui sont des tâches (Task, UserTask, ServiceTask...)
  const tasks = elementRegistry.filter(element => element.type.includes('Task'));

  tasks.forEach(task => {
    const linkedUS = task.businessObject.get('custom:linkedUserStories');

    // Si la propriété n'existe pas ou est vide = tâche orpheline
    if (!linkedUS || linkedUS.trim() === '') {
      canvas.addMarker(task.id, 'orphan-task');
    } else {
      // Si elle a des US, on retire le marqueur au cas où il y était
      canvas.removeMarker(task.id, 'orphan-task');
    }
  });
};
onMounted(async () => {
  bpmnModeler = new Modeler({ container: container.value });
  let xml = props.initialXml || emptyBpmn;

  await bpmnModeler.importXML(xml);
  highlightOrphanTasks();

  // --- ÉCOUTEUR D'ÉVÉNEMENTS BPMN ---
  const eventBus = bpmnModeler.get('eventBus');

  // Quand on clique sur un élément du diagramme
  eventBus.on('element.click', (e) => {
    const element = e.element;

    // On vérifie si l'élément est une tâche (Task, UserTask, ServiceTask, etc.)
    if (element.type.includes('Task')) {
      selectedTask.value = element;

      const existingLinks = element.businessObject.get('custom:linkedUserStories');
      linkedUsIds.value = existingLinks ? existingLinks.split(',') : [];

      // On récupère les coordonnées de la souris pour placer le pop-over
      popoverPosition.value = {
        top: `${e.originalEvent.clientY}px`,
        left: `${e.originalEvent.clientX + 20}px` // +20px pour décaler un peu à droite de la souris
      };
      showPopover.value = true;
    } else {
      // Si on clique sur une piscine ou une flèche, on ferme le pop-over
      showPopover.value = false;
      selectedTask.value = null;
    }
  });

  // Quand on clique dans le vide (sur le fond du canevas)
  eventBus.on('canvas.click', () => {
    showPopover.value = false;
    selectedTask.value = null;
  });

  eventBus.on('commandStack.changed', () => {
    triggerAutoSave();
    highlightOrphanTasks();
  });
});

// --- DRAG & DROP : CRÉATION D'UNE VÉRITABLE PISCINE ---
const startDrag = (event, actor) => {
  const elementFactory = bpmnModeler.get('elementFactory');
  const create = bpmnModeler.get('create');

  // 1. On utilise la méthode spécifique de bpmn-js pour générer une vraie piscine complète
  const shape = elementFactory.createParticipantShape();

  // 2. On injecte le nom de ton acteur pour qu'il s'affiche dans le bandeau de gauche
  shape.businessObject.name = actor.name;

  // 3. On lance le drag & drop
  create.start(event, shape);
};
// --- LOGIQUE DE ZOOM ---
const zoom = (step) => {
  bpmnModeler.get('canvas').zoom(bpmnModeler.get('canvas').zoom() + step);
};

const resetZoom = () => {
  bpmnModeler.get('canvas').zoom('fit-viewport');
};

const saveDiagram = async () => {
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true });
    const updatedProject = await SupportFeatureService.saveBpmnDiagram(props.projectId, xml);
    if (updatedProject && updatedProject.coverageScore !== undefined) {
      emit('update:coverageScore', updatedProject.coverageScore);
    }
  } catch (error) {
    console.error("Erreur lors de la sauvegarde du BPMN:", error);
  }
};

onBeforeUnmount(() => {
  if (bpmnModeler) bpmnModeler.destroy();
});
</script>

<template>
  <div class="flex h-full border-1 surface-border border-round-xl overflow-hidden bg-white shadow-2">

    <div class="w-18rem surface-100 p-4 border-right-1 surface-border flex flex-column">
      <h3 class="mt-0 mb-3 text-900 font-bold"><i class="pi pi-users mr-2 text-primary"></i>Acteurs</h3>
      <p class="text-xs text-500 mb-4 uppercase font-bold tracking-wider">Glisser pour créer un couloir</p>

      <div class="flex-1 overflow-y-auto">
        <div v-for="actor in actors" :key="actor.id"
             class="p-3 mb-3 bg-white border-round-lg shadow-1 cursor-move hover:shadow-3 hover:border-primary transition-all border-1 surface-border flex align-items-center"
             @mousedown="startDrag($event, actor)">
          <div class="w-2rem h-2rem border-circle bg-primary-50 flex align-items-center justify-content-center mr-3">
            <i class="pi pi-user text-primary"></i>
          </div>
          <span class="font-bold text-700">{{ actor.name }}</span>
        </div>
      </div>

      <div class="mt-4 pt-3 border-top-1 surface-border">
        <div class="mt-4 pt-3 border-top-1 surface-border flex justify-content-center">
        <span v-if="isSaving" class="text-sm font-bold text-orange-500 flex align-items-center gap-2 fadein">
          <i class="pi pi-spin pi-spinner"></i> Sauvegarde en cours...
        </span>
          <span v-else class="text-sm font-bold text-green-500 flex align-items-center gap-2 fadein">
          <i class="pi pi-cloud-upload"></i> Sauvegardé dans le cloud
        </span>
        </div>
      </div>
    </div>

    <div class="flex-1 relative">
      <div class="absolute bottom-0 left-0 z-5 p-3 flex gap-2">
        <button @click="zoom(0.1)" class="p-button p-button-secondary p-button-rounded shadow-3"><i class="pi pi-plus"></i></button>
        <button @click="zoom(-0.1)" class="p-button p-button-secondary p-button-rounded shadow-3"><i class="pi pi-minus"></i></button>
        <button @click="resetZoom" class="p-button p-button-secondary p-button-rounded shadow-3"><i class="pi pi-search"></i></button>
      </div>

      <div ref="container" class="h-full w-full"></div>
    </div>
    <div v-if="showPopover"
         class="fixed z-5 bg-white border-round-lg shadow-6 border-1 surface-border p-3 w-15rem transition-duration-100"
         :style="{ top: popoverPosition.top, left: popoverPosition.left }">

      <div class="flex justify-content-between align-items-center mb-2 border-bottom-1 surface-border pb-2">
    <span class="font-bold text-700 text-sm">
      <i class="pi pi-check-square text-primary mr-2"></i>
      {{ selectedTask?.businessObject?.name || 'Tâche sans nom' }}
    </span>
        <button @click="showPopover = false" class="p-link text-500 hover:text-700">
          <i class="pi pi-times"></i>
        </button>
      </div>

      <p class="text-xs text-500 mb-2">Lier des User Stories :</p>

      <div class="text-sm font-italic text-400 p-2 text-center border-round surface-50">
        <div class="max-h-15rem overflow-y-auto pr-2 mt-2">
          <div v-for="us in userStories" :key="us.id" class="flex align-items-start mb-3 border-bottom-1 surface-border pb-2">
            <input type="checkbox"
                   :id="'us-' + us.id"
                   :value="us.id"
                   v-model="linkedUsIds"
                   @change="updateLinkedUS"
                   class="mt-1 mr-2 cursor-pointer" />

            <label :for="'us-' + us.id" class="text-sm text-700 cursor-pointer line-height-2">
              {{ us.description }} </label>
          </div>

          <div v-if="userStories.length === 0" class="text-sm font-italic text-400 p-2 text-center">
            Aucune User Story disponible.
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
/* Masquer l'outil de création de piscine par défaut pour forcer l'usage des acteurs */
.bpmn-icon-participant {
  display: none !important;
}

.bjs-container {
  height: 100%;
}

/* Style spécifique pour que les en-têtes de piscines soient bien visibles */
.djs-visual rect {
  stroke-width: 2px !important;
}

.orphan-task .djs-visual rect {
  stroke: #ef4444 !important; /* Rouge PrimeVue/Tailwind */
  stroke-width: 3px !important;
  stroke-dasharray: 5, 5 !important; /* Effet pointillé */
  fill: #fff5f5 !important; /* Fond légèrement rouge pour accentuer */
  transition: all 0.3s ease;
}

.orphan-task .djs-visual rect {
  fill: #fff5f5 !important;
}
</style>