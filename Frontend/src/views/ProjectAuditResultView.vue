<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { SupportFeatureService } from '../features/projects/api/SupportFeatureService.js';
import { ProjectService } from '../features/projects/api/ProjectService.js';
import Card from 'primevue/card';
import Button from 'primevue/button';
import html2pdf from 'html2pdf.js';
import Message from 'primevue/message';
import Skeleton from 'primevue/skeleton';
import Chart from 'primevue/chart';
import BpmnViewer from 'bpmn-js/lib/Viewer';
import mermaid from 'mermaid';

const route = useRoute();
const router = useRouter();
const projectId = route.params.id;

const auditReport = ref(null);
const isLoading = ref(true);
const errorMessage = ref(null);
const project = ref(null);
const businessRules = ref([]);
const chartData = ref(null);
const chartOptions = ref(null);
const bpmnImageSrc = ref(null);
const mcdImageSrc = ref(null);

const scoreColor = computed(() => {
  if (!auditReport.value) return '#94a3b8';
  const s = auditReport.value.score;
  if (s >= 80) return '#22c55e';
  if (s >= 50) return '#f59e0b';
  return '#ef4444';
});

const scoreText = computed(() => {
  if (!auditReport.value) return '';
  const s = auditReport.value.score;
  if (s >= 80) return 'EXCELLENT';
  if (s >= 50) return 'PARTIELLEMENT CONFORME';
  return 'NON CONFORME';
});

const pairedIssues = computed(() => {
  if (!auditReport.value) return [];
  const incs = auditReport.value.inconsistencies || [];
  const corrs = auditReport.value.corrections || [];

  const maxLength = Math.max(incs.length, corrs.length);
  const pairs = [];

  for (let i = 0; i < maxLength; i++) {
    pairs.push({
      inconsistency: incs[i] || null,
      correction: corrs[i] || null
    });
  }
  return pairs;
});

const initChart = () => {
  chartData.value = {
    labels: ['Score', 'Reste'],
    datasets: [{
      data: [auditReport.value.score, 100 - auditReport.value.score],
      backgroundColor: [scoreColor.value, '#f8fafc'],
      hoverBackgroundColor: [scoreColor.value, '#f1f5f9'],
      borderWidth: 0
    }]
  };

  chartOptions.value = {
    cutout: '80%',
    plugins: { legend: { display: false }, tooltip: { enabled: false } },
    responsive: true,
    maintainAspectRatio: false
  };
};

const getActorName = (us) => {
  if (!us) return '-';
  if (us.actor && typeof us.actor === 'object' && us.actor.name) return us.actor.name;

  const targetId = typeof us.actor === 'string' ? us.actor : (us.actorId || us.actor_id);
  const foundActor = project.value?.actors?.find(a => a.id === targetId);

  return foundActor ? foundActor.name : 'Acteur inconnu';
};

const convertSvgToPng = (svgText) => {
  return new Promise((resolve, reject) => {
    let cleanSvg = svgText;
    cleanSvg = cleanSvg.replace(/<br>/g, '<br/>').replace(/&nbsp;/g, '&#160;');

    const vbMatch = cleanSvg.match(/viewBox="[\d.]+ [\d.]+ ([\d.]+) ([\d.]+)"/);
    if (vbMatch) {
      cleanSvg = cleanSvg.replace(/width="100%"/, `width="${vbMatch[1]}" height="${vbMatch[2]}"`);
    } else {
      cleanSvg = cleanSvg.replace(/width="100%"/, 'width="1000"').replace(/height="100%"/, 'height="800"');
    }

    const img = new Image();
    img.onload = () => {
      const canvas = document.createElement('canvas');
      canvas.width = img.width || 1000;
      canvas.height = img.height || 800;
      const ctx = canvas.getContext('2d');
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, canvas.width, canvas.height);
      ctx.drawImage(img, 0, 0);
      resolve(canvas.toDataURL('image/png', 1.0));
    };
    img.onerror = (e) => reject("Impossible de peindre le SVG sur le Canvas. SVG invalide.");
    img.src = "data:image/svg+xml;base64," + btoa(unescape(encodeURIComponent(cleanSvg)));
  });
};

const generateBpmnImage = async () => {
  if (!project.value?.bpmnXml) return;

  const container = document.createElement('div');
  container.style.position = 'absolute';
  container.style.left = '-9999px';
  container.style.width = '1200px';
  container.style.height = '800px';
  document.body.appendChild(container);

  try {
    const viewer = new BpmnViewer({ container: container });
    await viewer.importXML(project.value.bpmnXml);
    viewer.get('canvas').zoom('fit-viewport', 'auto');
    const { svg } = await viewer.saveSVG();
    bpmnImageSrc.value = await convertSvgToPng(svg);
  } catch (err) {
    console.error("Erreur BPMN :", err);
  } finally {
    if (container && container.parentNode) {
      container.parentNode.removeChild(container);
    }
  }
};

const generateMcdImage = async (associationsData) => {
  if (!project.value?.dictionaryEntries) return;

  let code = "%%{init: {'flowchart': {'htmlLabels': false}}}%%\n";
  code += "flowchart LR\n";
  code += "  classDef entityClass fill:#eef,stroke:#333,stroke-width:1px,rx:5,ry:5;\n";
  code += "  classDef assocClass fill:#fff,stroke:#333,stroke-width:2px,color:#000;\n";
  code += "  classDef cifClass fill:#fff,stroke:#d946ef,stroke-width:3px,color:#d946ef;\n";
  code += "  classDef inheritClass fill:#fef08a,stroke:#ca8a04,stroke-width:2px,color:#854d0e;\n";

  project.value.dictionaryEntries.forEach(e => {
    const entityNodeId = `ent_${e.name.replace(/[^a-zA-Z0-9]/g, '_')}`;
    code += `  ${entityNodeId}[${e.name}]:::entityClass;\n`;
  });

  const multiplicities = [
    { value: '0..N', text: '0,N' }, { value: '1..N', text: '1,N' },
    { value: '0..1', text: '0,1' }, { value: '1..1', text: '1,1' }
  ];

  if (associationsData && associationsData.length > 0) {
    associationsData.forEach(a => {
      const srcName = a.sourceName || a.source?.name || 'Unknown';
      const tgtName = a.targetName || a.target?.name || 'Unknown';

      const srcNodeId = `ent_${srcName.replace(/[^a-zA-Z0-9]/g, '_')}`;
      const tgtNodeId = `ent_${tgtName.replace(/[^a-zA-Z0-9]/g, '_')}`;
      const safeId = (a.id || Date.now().toString()).replace(/[^a-zA-Z0-9]/g, '_');

      if (a.isInheritance) {
        const inheritNodeId = `inh_${safeId}`;
        code += `  ${inheritNodeId}{{"Δ"}}:::inheritClass;\n`;
        code += `  ${srcNodeId} --- ${inheritNodeId};\n`;
        code += `  ${inheritNodeId} --> ${tgtNodeId};\n`;
      } else {
        const assocName = a.name || 'Relation';
        const assocNodeId = `assoc_${assocName.replace(/[^a-zA-Z0-9]/g, '_')}_${safeId}`;

        let srcCardText = multiplicities.find(m => m.value === a.sourceMultiplicity)?.text || a.sourceMultiplicity?.replace('..', ',') || '0,N';
        let tgtCardText = multiplicities.find(m => m.value === a.targetMultiplicity)?.text || a.targetMultiplicity?.replace('..', ',') || '1,1';

        if (a.isRelative) {
          if (a.sourceMultiplicity && a.sourceMultiplicity.includes('1')) srcCardText += ' (R)';
          if (a.targetMultiplicity && a.targetMultiplicity.includes('1')) tgtCardText += ' (R)';
        }

        let nodeClass = a.isCif ? "cifClass" : "assocClass";
        let nodeLabel = a.isCif ? `${assocName}\n(CIF)` : `${assocName}`;

        if (a.attributes && a.attributes.length > 0) {
          nodeLabel += `\n---`;
          a.attributes.forEach(attr => { nodeLabel += `\n${attr.name}`; });
        }

        nodeLabel = nodeLabel.replace(/"/g, "'");

        code += `  ${assocNodeId}(["${nodeLabel}"]):::${nodeClass};\n`;

        if (a.isCif && a.sourceMultiplicity && a.sourceMultiplicity.endsWith('1')) {
          code += `  ${assocNodeId} -->|"${srcCardText}"| ${srcNodeId};\n`;
        } else {
          code += `  ${srcNodeId} ---|"${srcCardText}"| ${assocNodeId};\n`;
        }

        if (a.isCif && a.targetMultiplicity && a.targetMultiplicity.endsWith('1')) {
          code += `  ${assocNodeId} -->|"${tgtCardText}"| ${tgtNodeId};\n`;
        } else {
          code += `  ${assocNodeId} ---|"${tgtCardText}"| ${tgtNodeId};\n`;
        }
      }
    });
  }

  const tempId = 'mermaid-temp-' + Date.now();
  const container = document.createElement('div');
  container.id = tempId;
  container.style.position = 'absolute';
  container.style.left = '-9999px';
  document.body.appendChild(container);

  try {
    mermaid.initialize({ startOnLoad: false, theme: 'default', flowchart: { htmlLabels: false } });
    const { svg } = await mermaid.render(tempId, code);
    mcdImageSrc.value = await convertSvgToPng(svg);
  } catch (err) {
    console.error("Erreur Mermaid MCD :", err);
  } finally {
    if (container && container.parentNode) container.parentNode.removeChild(container);
    const replacedNode = document.getElementById(tempId);
    if (replacedNode && replacedNode.parentNode) replacedNode.parentNode.removeChild(replacedNode);
  }
};

onMounted(async () => {
  try {
    await new Promise(resolve => setTimeout(resolve, 800));

    const reportPromise = SupportFeatureService.getAudit(projectId).catch(err => {
      console.error("❌ Erreur API getAudit :", err);
      return null;
    });

    const projectPromise = ProjectService.getProjectById(projectId).catch(err => {
      console.error("❌ Erreur API getProjectById :", err);
      return null;
    });

    const assocsPromise = SupportFeatureService.getAssociations(projectId).catch(err => {
      console.warn("⚠️ Impossible de charger les associations :", err);
      return [];
    });

    const rulesPromise = SupportFeatureService.getBusinessRules(projectId).catch(err => {
      console.warn("⚠️ Impossible de charger les règles de gestion :", err);
      return [];
    });

    const [report, projData, assocsData, rulesData] = await Promise.all([reportPromise, projectPromise, assocsPromise, rulesPromise]);

    if (report && projData) {


      auditReport.value = report;
      project.value = projData;
      businessRules.value = rulesData || [];
      initChart();

      await nextTick();
      await generateBpmnImage();
      await generateMcdImage(assocsData);
    } else {
      errorMessage.value = "Impossible de trouver les données de l'audit ou du projet.";
    }
  } catch (e) {
    console.error("💥 Erreur fatale dans onMounted :", e);
    errorMessage.value = "Une erreur technique s'est produite pendant le chargement.";
  } finally {
    isLoading.value = false;
  }
});

const exportJMerise = async () => {
  try {
    const blobData = await SupportFeatureService.exportMcdFile(projectId);
    const blob = new Blob([blobData], { type: "application/octet-stream" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `Modele_${project.value?.name || 'Projet'}.mcd`;

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  } catch (error) {
    console.error("Erreur lors de l'export JMerise", error);
  }
};

const exportBPMN = () => {
  const bpmnContent = project.value?.bpmnXml;

  if (!bpmnContent) {
    console.error("Aucun processus BPMN n'est associé à ce projet.");
    return;
  }

  const blob = new Blob([bpmnContent], { type: "application/bpmn20-xml;charset=utf-8;" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = `Processus_${project.value?.name || 'Projet'}.bpmn`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(link.href);
};

const exportPDF = async () => {
  const element = document.getElementById("pdf-export-content");
  if (!element) return console.error("Gabarit PDF introuvable");

  element.parentElement.style.left = "0px";
  element.parentElement.style.top = "0px";
  element.parentElement.style.zIndex = "-9999";

  const opt = {
    margin:       0,
    filename:     `Rapport_Architecture_${project.value?.name || 'Projet'}.pdf`,
    image:        { type: 'jpeg', quality: 0.98 },
    html2canvas:  { scale: 2, useCORS: true },
    jsPDF:        { unit: 'mm', format: 'a4', orientation: 'portrait' },
    pagebreak:    { mode: ['css', 'legacy'] }
  };

  try {
    await html2pdf().set(opt).from(element).save();
  } finally {
    element.parentElement.style.left = "-9999px";
    element.parentElement.style.top = "-9999px";
  }
};
const goBack = () => router.push(`/app/accompagnement/${projectId}`);
</script>

<template>
  <div class="min-h-screen surface-ground p-4">

    <div class="flex justify-content-between align-items-center mb-4 surface-card p-4 border-round-xl shadow-2">
      <div class="flex align-items-center gap-3">
        <Button icon="pi pi-arrow-left" text rounded severity="secondary" size="large" @click="goBack" />
        <div>
          <h1 class="m-0 text-2xl text-900 font-bold flex align-items-center gap-2">
            <i class="pi pi-verified text-primary text-3xl"></i> Rapport d'Audit IA
          </h1>
          <p class="m-0 mt-1 text-500">Validation de l'architecture Merise & BPMN</p>
        </div>
      </div>
    </div>

    <div v-if="isLoading" class="grid animate-fadein">
      <div class="col-12 lg:col-6">
        <div class="surface-card p-4 border-round-xl shadow-2 flex flex-column align-items-center justify-content-center h-full" style="min-height: 22rem;">
          <Skeleton width="50%" height="2rem" class="mb-4"></Skeleton>
          <Skeleton shape="circle" size="220px" class="mb-4"></Skeleton>
          <Skeleton width="40%" height="2.5rem" borderRadius="16px"></Skeleton>
        </div>
      </div>
      <div class="col-12 lg:col-6">
        <div class="surface-card p-5 border-round-xl shadow-2 flex flex-column h-full" style="min-height: 22rem;">
          <Skeleton width="40%" height="2rem" class="mb-3"></Skeleton>
          <Skeleton width="70%" height="1rem" class="mb-4"></Skeleton>
          <div class="flex flex-column gap-3 mt-auto mb-auto">
            <Skeleton width="100%" height="3rem" borderRadius="8px"></Skeleton>
            <Skeleton width="100%" height="3rem" borderRadius="8px"></Skeleton>
            <Skeleton width="100%" height="3rem" borderRadius="8px" class="mt-2"></Skeleton>
          </div>
        </div>
      </div>
      <div class="col-12 mt-3">
        <div class="surface-card p-5 border-round-xl shadow-2">
          <Skeleton width="30%" height="2rem" class="mb-4"></Skeleton>
          <div class="flex flex-column gap-4">
            <Skeleton width="100%" height="8rem" borderRadius="12px"></Skeleton>
            <Skeleton width="100%" height="8rem" borderRadius="12px"></Skeleton>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="errorMessage" class="flex flex-column align-items-center justify-content-center mt-5">
      <i class="pi pi-exclamation-triangle text-red-500 text-6xl mb-4"></i>
      <h2 class="text-red-600 font-bold m-0 text-center">{{ errorMessage }}</h2>
      <p class="text-500 mt-2 text-center">Appuyez sur F12 pour ouvrir la console et voir les détails.</p>
    </div>

    <div v-else-if="auditReport" id="report-content" class="grid animate-fadein">
      <div class="col-12 lg:col-6">
        <div class="surface-card p-4 border-round-xl shadow-2 flex flex-column align-items-center justify-content-center h-full" style="min-height: 22rem;">
          <h2 class="text-700 font-semibold mt-0 mb-4 text-center">Taux de Conformité</h2>

          <div class="relative flex justify-content-center align-items-center" style="width: 220px; height: 220px;">
            <Chart type="doughnut" :data="chartData" :options="chartOptions" class="w-full h-full absolute z-1" />
            <div class="absolute z-2 flex flex-column align-items-center">
              <span class="text-6xl font-bold" :style="{ color: scoreColor }">{{ auditReport.score }}%</span>
            </div>
          </div>

          <div class="mt-4 px-4 py-2 border-round-3xl font-bold text-sm shadow-1"
               :class="auditReport.score >= 80 ? 'bg-green-100 text-green-700' : (auditReport.score >= 50 ? 'bg-orange-100 text-orange-700' : 'bg-red-100 text-red-700')">
            {{ scoreText }}
          </div>
        </div>
      </div>

      <div class="col-12 lg:col-6">
        <div class="surface-card p-5 border-round-xl shadow-2 flex flex-column h-full" style="min-height: 22rem;">
          <h2 class="text-700 font-semibold mt-0 mb-2 border-bottom-1 surface-border pb-3 flex align-items-center gap-2">
            <i class="pi pi-box text-2xl text-primary"></i> Livrables & Artéfacts
          </h2>
          <p class="text-500 mb-4">Générez les fichiers compatibles avec vos environnements de travail habituels.</p>

          <div class="flex flex-column gap-3 mt-auto mb-auto">
            <Button
                label="Exporter le modèle Merise (.mcd)"
                icon="pi pi-sitemap"
                severity="secondary"
                outlined
                size="large"
                class="w-full justify-content-start font-bold"
                :disabled="auditReport.score < 50"
                @click="exportJMerise"
            />
            <Button
                label="Exporter le processus Agile (.bpmn)"
                icon="pi pi-server"
                severity="secondary"
                outlined
                size="large"
                class="w-full justify-content-start font-bold"
                :disabled="auditReport.score < 50"
                @click="exportBPMN"
            />
            <Button
                label="Télécharger le Rapport Complet (.pdf)"
                icon="pi pi-file-pdf"
                severity="danger"
                size="large"
                class="w-full justify-content-start font-bold mt-2"
                @click="exportPDF"
            />
          </div>

          <Message v-if="auditReport.score < 50" severity="warn" :closable="false" class="mt-4 mb-0 text-sm py-2">
            <span class="font-bold"><i class="pi pi-lock mr-1"></i> Exports verrouillés :</span>
            Votre score de qualité doit être d'au moins 50% pour générer les fichiers techniques.
          </Message>
        </div>
      </div>

      <div class="col-12 mt-3">
        <div class="surface-card p-5 border-round-xl shadow-2">
          <h2 class="text-900 font-semibold mt-0 mb-4 flex align-items-center gap-2 border-bottom-1 surface-border pb-3">
            <i class="pi pi-list text-2xl text-primary"></i> Analyse détaillée et Solutions
          </h2>

          <div v-if="pairedIssues.length > 0" class="flex flex-column gap-4">
            <div v-for="(pair, i) in pairedIssues" :key="'pair'+i" class="border-1 surface-border border-round-xl overflow-hidden shadow-1">
              <div v-if="pair.inconsistency" class="bg-red-50 p-4 border-bottom-1 border-red-100">
                <div class="flex gap-3">
                  <i class="pi pi-exclamation-triangle text-red-500 text-xl mt-1"></i>
                  <div>
                    <span class="text-red-800 font-bold block mb-1">Incohérence détectée</span>
                    <span class="text-700 line-height-3">{{ pair.inconsistency }}</span>
                  </div>
                </div>
              </div>
              <div v-if="pair.correction" class="bg-blue-50 p-4 flex align-items-start gap-3">
                <i class="pi pi-arrow-right text-blue-500 text-xl mt-1"></i>
                <div>
                  <span class="text-blue-800 font-bold block mb-1">Recommandation de l'Architecte</span>
                  <span class="text-700 line-height-3">{{ pair.correction }}</span>
                </div>
              </div>
            </div>
          </div>
          <Message v-else severity="success" :closable="false" class="mt-3 p-4 text-lg">
            <i class="pi pi-check-circle mr-2 text-2xl"></i> Votre architecture est optimale, aucune incohérence détectée !
          </Message>
        </div>
      </div>

    </div>
  </div>

  <div style="position: absolute; left: -9999px; top: -9999px; width: 210mm; background: white;">
    <div id="pdf-export-content" class="pdf-document">

      <div class="pdf-page cover-page">
        <div class="cover-header">
          <h1 class="pdf-title">Rapport d'Architecture Système</h1>
          <h2 class="pdf-project-name">{{ project?.name || 'Projet' }}</h2>
          <p class="pdf-date">Généré le {{ new Date().toLocaleDateString('fr-FR') }}</p>
        </div>
        <div class="cover-score" :style="{ borderColor: scoreColor }">
          <h3>Score de Conformité IA</h3>
          <span class="score-number" :style="{ color: scoreColor }">{{ auditReport?.score }}%</span>
          <span class="score-text" :style="{ color: scoreColor }">{{ scoreText }}</span>
        </div>
      </div>

      <div class="pdf-page-break"></div>

      <div class="pdf-page">
        <h2 class="section-title">1. Présentation & Analyse Métier</h2>
        <p class="pdf-intro-text">
          Ce chapitre définit le périmètre du projet, les parties prenantes, les besoins fonctionnels (User Stories) ainsi que les règles de gestion qui encadrent le comportement du système.
        </p>

        <h3 class="subsection-title">1.1 Description du projet</h3>
        <p class="pdf-text mb-4">{{ project?.description || 'Aucune description spécifique n\'a été fournie pour ce projet.' }}</p>

        <h3 class="subsection-title">1.2 Acteurs du système</h3>
        <ul v-if="project?.actors?.length" class="pdf-list mb-4">
          <li v-for="actor in project.actors" :key="actor.id" class="mb-2">
            <strong>{{ actor.name }}</strong>
          </li>
        </ul>
        <p v-else class="pdf-empty mb-4">Aucun acteur défini.</p>

        <h3 class="subsection-title">1.3 User Stories</h3>
        <table v-if="project?.userStories?.length" class="pdf-table mb-4">
          <thead>
          <tr>
            <th style="width:15%">Code</th>
            <th style="width:20%">En tant que</th>
            <th style="width:35%">Je veux</th>
            <th style="width:30%">Afin de</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="us in project.userStories" :key="us.id">
            <td><strong>{{ us.identifier }}</strong></td>
            <td>{{ getActorName(us) }}</td>
            <td>{{ us.description }}</td>
            <td>{{ us.benefit || '-' }}</td>
          </tr>
          </tbody>
        </table>
        <p v-else class="pdf-empty mb-4">Aucune User Story définie.</p>

        <h3 class="subsection-title mt-4">1.4 Règles de Gestion</h3>
        <table v-if="businessRules.length" class="pdf-table">
          <thead>
          <tr>
            <th style="width:20%">Code</th>
            <th style="width:80%">Description de la règle</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="rule in businessRules" :key="rule.id">
            <td><strong>{{ rule.code }}</strong></td>
            <td>{{ rule.description }}</td>
          </tr>
          </tbody>
        </table>
        <p v-else class="pdf-empty">Aucune règle de gestion n'a été spécifiée.</p>

      </div>

      <div class="pdf-page-break"></div>

      <div class="pdf-page">
        <h2 class="section-title">2. Cartographie des Processus (BPMN)</h2>
        <p class="pdf-intro-text">
          Le modèle BPMN (Business Process Model and Notation) ci-dessous illustre l'enchaînement chronologique des tâches, les interactions entre les acteurs et les flux d'informations du processus métier cible.
        </p>

        <div class="diagram-wrapper">
          <img v-if="bpmnImageSrc" :src="bpmnImageSrc" alt="BPMN" class="pdf-image" />
          <p v-else class="pdf-empty">Processus non modélisé ou erreur de génération.</p>
        </div>

        <h2 class="section-title mt-6">3. Modélisation des Données (MCD)</h2>
        <p class="pdf-intro-text">
          Le Modèle Conceptuel de Données (MCD) représente la structure des informations du système. Il met en évidence les entités, leurs attributs et les relations qui les lient selon le formalisme Merise.
        </p>

        <div class="diagram-wrapper mb-2">
          <img v-if="mcdImageSrc" :src="mcdImageSrc" alt="MCD" class="pdf-image" />
          <p v-else class="pdf-empty">Modèle de données introuvable.</p>
        </div>

        <div class="mcd-legend">
          <h4>Légende de lecture du Modèle</h4>
          <div class="legend-grid">
            <div class="legend-item"><span class="legend-box entity"></span> Entité (Table)</div>
            <div class="legend-item"><span class="legend-box assoc"></span> Relation standard</div>
            <div class="legend-item"><span class="legend-box cif"></span> CIF (Contrainte d'Intégrité Fonctionnelle)</div>
            <div class="legend-item"><span class="legend-box inherit">Δ</span> Héritage / Spécialisation</div>
            <div class="legend-item"><strong>(R)</strong> Identification Relative</div>
          </div>
        </div>

      </div>

      <div class="pdf-page-break"></div>

      <div class="pdf-page">
        <h2 class="section-title">4. Dictionnaire de Données</h2>
        <p class="pdf-intro-text">
          Le dictionnaire détaille de manière exhaustive l'ensemble des propriétés (attributs) contenues dans chaque entité du modèle, en précisant leur type, leur taille et leurs contraintes (Clé primaire, Obligatoire).
        </p>

        <div v-if="project?.dictionaryEntries?.length">
          <div v-for="entry in project.dictionaryEntries" :key="entry.id" class="entity-dictionary-block">
            <h3 class="entity-title text-primary">📦 Table : {{ entry.name }}</h3>
            <table class="pdf-table small-text">
              <thead>
              <tr>
                <th style="width:30%">Attribut</th>
                <th style="width:25%">Type</th>
                <th style="width:15%">Taille</th>
                <th style="width:15%" class="text-center">PK</th>
                <th style="width:15%" class="text-center">Obligatoire</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="attr in entry.attributes" :key="attr.id">
                <td><strong>{{ attr.name }}</strong></td>
                <td>{{ attr.dataType }}</td>
                <td>{{ attr.size || '-' }}</td>
                <td class="text-center">{{ attr.primaryKey ? '🔑 Oui' : '-' }}</td>
                <td class="text-center">{{ attr.notNull ? 'Oui' : 'Non' }}</td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
        <p v-else class="pdf-empty">Aucun dictionnaire défini.</p>
      </div>

      <div class="pdf-page-break"></div>

      <div class="pdf-page">
        <h2 class="section-title">5. Recommandations de l'Audit IA</h2>
        <p class="pdf-intro-text">
          Cette section présente l'analyse automatisée de la cohérence entre le dictionnaire, le MCD, le processus BPMN et les règles de gestion. Elle souligne les axes d'amélioration identifiés par l'IA d'architecture.
        </p>

        <div v-if="pairedIssues.length > 0">
          <div v-for="(pair, i) in pairedIssues" :key="'pdf-pair'+i" class="pdf-issue-box">
            <div v-if="pair.inconsistency" class="issue-inc">
              <strong>⚠️ Incohérence :</strong> {{ pair.inconsistency }}
            </div>
            <div v-if="pair.correction" class="issue-cor">
              <strong>💡 Recommandation :</strong> {{ pair.correction }}
            </div>
          </div>
        </div>
        <div v-else class="pdf-success">
          ✅ L'architecture est optimale, aucune incohérence détectée.
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.pdf-document {
  font-family: 'Helvetica', 'Arial', sans-serif;
  color: #333;
  width: 210mm;
}

.pdf-page {
  padding: 15mm 20mm;
  min-height: 270mm;
}

.pdf-page-break {
  page-break-before: always;
  height: 0;
}

.cover-page {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.cover-header { margin-top: 50mm; }
.pdf-title { font-size: 28pt; color: #1e293b; margin-bottom: 10px; }
.pdf-project-name { font-size: 22pt; color: #3b82f6; margin-bottom: 20px; }
.pdf-date { font-size: 12pt; color: #64748b; }

.cover-score {
  margin-top: 40mm;
  padding: 30px;
  border: 4px solid;
  border-radius: 15px;
  width: 70%;
}

.score-number { font-size: 40pt; font-weight: bold; display: block; margin: 10px 0; }
.score-text { font-size: 14pt; font-weight: bold; }

.section-title {
  color: #1e293b;
  border-bottom: 2px solid #3b82f6;
  padding-bottom: 5px;
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 16pt;
}

.subsection-title { color: #334155; font-size: 13pt; margin-bottom: 8px; }

.pdf-intro-text {
  color: #475569;
  font-size: 10pt;
  margin-bottom: 20px;
  font-style: italic;
  line-height: 1.5;
  background: #f8fafc;
  padding: 10px;
  border-left: 3px solid #cbd5e1;
}

.pdf-text {
  color: #334155;
  font-size: 11pt;
  line-height: 1.5;
}

.mcd-legend {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  padding: 12px;
  border-radius: 6px;
  font-size: 9pt;
  page-break-inside: avoid;
}

.mcd-legend h4 {
  margin: 0 0 10px 0;
  color: #1e293b;
  font-size: 10pt;
}

.legend-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 15px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #475569;
}

.legend-box {
  width: 14px;
  height: 14px;
  border: 1px solid #333;
  display: inline-block;
}

.legend-box.entity { background: #eef; border-radius: 2px; }
.legend-box.assoc { background: #fff; border-radius: 7px; border-width: 2px; }
.legend-box.cif { background: #fff; border-color: #d946ef; border-width: 2px; border-radius: 7px; }
.legend-box.inherit {
  background: #fef08a;
  border-color: #ca8a04;
  border-width: 2px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 8pt;
  font-weight: bold;
  color: #854d0e;
  border: none;
  width: 16px;
  height: 16px;
}

.pdf-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
.pdf-table th, .pdf-table td { border: 1px solid #cbd5e1; padding: 8px; text-align: left; font-size: 10pt; }
.pdf-table th { background-color: #f8fafc; font-weight: bold; }
.pdf-table tr { page-break-inside: avoid; }
.small-text td, .small-text th { font-size: 9pt; padding: 6px; }

.diagram-wrapper { width: 100%; text-align: center; margin: 15px 0; }
.pdf-image { max-width: 100%; max-height: 140mm; object-fit: contain; }

.pdf-issue-box { border: 1px solid #e2e8f0; margin-bottom: 15px; border-radius: 8px; page-break-inside: avoid; }
.issue-inc { background-color: #fef2f2; padding: 10px; border-bottom: 1px solid #fecaca; color: #991b1b; font-size: 10pt;}
.issue-cor { background-color: #eff6ff; padding: 10px; color: #1e40af; font-size: 10pt;}
.pdf-success { background-color: #f0fdf4; padding: 15px; color: #166534; font-weight: bold; text-align: center; border-radius: 8px; }
.pdf-empty { color: #94a3b8; font-style: italic; margin-bottom: 15px;}
.mt-4 { margin-top: 1rem; }
.mt-6 { margin-top: 2rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-4 { margin-bottom: 1rem; }

.entity-dictionary-block {
  margin-bottom: 25px;
  page-break-inside: avoid;
}
.entity-title {
  font-size: 12pt;
  color: #2563eb;
  margin-bottom: 8px;
  background: #f1f5f9;
  padding: 5px 10px;
  border-radius: 4px;
}
</style>