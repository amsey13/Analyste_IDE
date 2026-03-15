<script setup>
import { onMounted, ref, onBeforeUnmount } from 'vue';
import Modeler from 'bpmn-js/lib/Modeler';
import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css';
import { SupportFeatureService } from '../api/SupportFeatureService';

const props = defineProps({
  projectId: { type: String, required: true },
  initialXml: { type: String, default: null },
  actors: { type: Array, default: () => [] }
});
const emptyBpmn = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="false" />
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1" />
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

const container = ref(null);
let bpmnModeler = null;

onMounted(async () => {
  bpmnModeler = new Modeler({ container: container.value });

  let xmlToLoad = props.initialXml || emptyBpmn;

  try {
    await bpmnModeler.importXML(xmlToLoad);
  } catch (err) {
    console.error("Erreur lors de l'initialisation du diagramme vide", err);
  }
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
  const { xml } = await bpmnModeler.saveXML({ format: true });
  await SupportFeatureService.saveBpmnDiagram(props.projectId, xml);
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
             draggable="true"
             @mousedown="startDrag($event, actor)">
          <div class="w-2rem h-2rem border-circle bg-primary-50 flex align-items-center justify-content-center mr-3">
            <i class="pi pi-user text-primary"></i>
          </div>
          <span class="font-bold text-700">{{ actor.name }}</span>
        </div>
      </div>

      <div class="mt-4 pt-3 border-top-1 surface-border">
        <button @click="saveDiagram" class="p-button p-button-success w-full shadow-2 font-bold">
          <i class="pi pi-save mr-2"></i> Sauvegarder
        </button>
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
</style>