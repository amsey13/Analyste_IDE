<script setup>
import Card from 'primevue/card'
import Button from 'primevue/button'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import auditProjectService from "../features/projects/api/AuditProjectService"

const route = useRoute()
const report = ref(null)

const isLoading = ref(true)

onMounted(async () => {
  try {
    isLoading.value = true

    const idDuRapport = route.params.reportId

    console.log("Tentative de récupération du rapport ID:", idDuRapport)

    report.value = await auditProjectService.getReportById(idDuRapport)

    console.log("Données reçues de Java:", report.value)
  } catch (e) {
    console.error("Erreur lors de la récupération :", e)
  } finally {
    isLoading.value = false
  }
})

const downloadPdf = () => {
  auditProjectService.downloadPdf(report.value.id)
}
</script>

<template>
  <div class="report-page">

    <div class="report-page__header">
      <div>
        <h1 class="report-page__title">
          <i class="pi pi-file-check" style="font-size: 1.5rem; margin-right: 0.5rem; vertical-align: middle;"></i>
          Rapport d'Audit de Cohérence
        </h1>
        <p class="report-page__subtitle">Analyse fonctionnelle du projet</p>
      </div>
      <div class="report-page__actions">
        <Button label="Retour" icon="pi pi-arrow-left" outlined @click="$router.back()" />
        <Button label="Télécharger PDF" icon="pi pi-download" style="background: #1f355e; border-color: #1f355e;" @click="downloadPdf" />
      </div>
    </div>

    <div v-if="isLoading" class="report-page__state">Chargement du rapport...</div>

    <div v-else-if="report">

      <div class="score-section">
        <div class="score-card">
          <span class="score-label">Score de cohérence</span>
          <div class="score-value-wrap">
            <span class="score-value">{{ report.score }}</span>
            <span class="score-pct">%</span>
          </div>
          <span class="score-sub">sur 100 points</span>
          <div class="score-bar-wrap">
            <div class="score-bar-fill" :style="{ width: report.score + '%' }"></div>
          </div>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-label">Anomalies détectées</div>
            <div class="stat-value stat-value--red">{{ report.anomalies?.length || 0 }}</div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Recommandations</div>
            <div class="stat-value stat-value--green">
              {{ report.anomalies?.filter(a => a.suggestion).length || 0 }}
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Score</div>
            <div class="stat-value" :class="report.score >= 70 ? 'stat-value--green' : 'stat-value--red'">
              {{ report.score >= 70 ? 'Bon' : 'À corriger' }}
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-label">Statut</div>
            <div class="stat-value stat-value--amber">
              {{ report.anomalies?.length > 0 ? '⚠ Anomalies' : '✓ Conforme' }}
            </div>
          </div>
        </div>
      </div>

      <div v-if="report.anomalies && report.anomalies.length">
        <div class="anomalies-title">
          {{ report.anomalies.length }} anomalie{{ report.anomalies.length > 1 ? 's' : '' }} détectée{{ report.anomalies.length > 1 ? 's' : '' }}
        </div>

        <div v-for="a in report.anomalies" :key="a.id" class="anomaly-card">
          <div class="anomaly-header">
            <div class="anomaly-dot"></div>
            <span class="anomaly-label">Anomalie détectée</span>
          </div>
          <div class="anomaly-body">{{ a.description }}</div>

          <template v-if="a.suggestion">
            <div class="suggestion-header">
              <div class="suggestion-dot"></div>
              <span class="suggestion-label">Recommandation de correction</span>
            </div>
            <div class="suggestion-body">"{{ a.suggestion.content }}"</div>
          </template>
        </div>
      </div>

      <div v-else class="no-anomaly-box">
        <i class="pi pi-check-circle"></i>
        Aucune anomalie détectée - votre projet est cohérent !
      </div>

    </div>

  </div>
</template>

<style scoped>
@keyframes fadeInDown {
  from { opacity: 0; transform: translateY(-12px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to   { opacity: 1; transform: translateY(0); }
}

.report-page {
  padding: 2rem;
  background: #eef2f7;
  min-height: 100vh;
}

.report-page__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 2rem;
  animation: fadeInDown 0.4s ease both;
}

.report-page__title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #1f355e;
  margin: 0 0 0.4rem;
  padding-left: 0.75rem;
  border-left: 4px solid #2563eb;
  line-height: 1.2;
}

.report-page__subtitle {
  font-size: 0.9rem;
  color: #64748b;
  margin: 0.4rem 0 0;
  padding-left: 0.85rem;
  font-style: italic;
}

.report-page__actions {
  display: flex;
  gap: 0.75rem;
  flex-shrink: 0;
}

.report-page__state {
  padding: 1rem 0;
  font-size: 0.95rem;
  color: #64748b;
}

.score-section {
  display: grid;
  grid-template-columns: 260px 1fr;
  gap: 1rem;
  margin-bottom: 1.5rem;
  animation: fadeInUp 0.4s 0.1s ease both;
  opacity: 0;
}

.score-card {
  background: #1f355e;
  border-radius: 14px;
  padding: 2rem 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
}

.score-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: rgba(255,255,255,0.6);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.score-value-wrap {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.score-value {
  font-size: 3.5rem;
  font-weight: 700;
  color: white;
  line-height: 1;
}

.score-pct {
  font-size: 1.5rem;
  color: rgba(255,255,255,0.7);
}

.score-sub {
  font-size: 0.78rem;
  color: rgba(255,255,255,0.5);
}

.score-bar-wrap {
  width: 100%;
  height: 5px;
  background: rgba(255,255,255,0.15);
  border-radius: 10px;
  overflow: hidden;
  margin-top: 0.5rem;
}

.score-bar-fill {
  height: 100%;
  background: #00b8d9;
  border-radius: 10px;
  transition: width 0.6s ease;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  border: 1.5px solid #e8edf2;
  padding: 1rem 1.25rem;
  box-shadow: 0 1px 4px rgba(31,53,94,0.06);
}

.stat-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: #64748b;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.3rem;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f355e;
}

.stat-value--red { color: #dc2626; }
.stat-value--green { color: #16a34a; }
.stat-value--amber { color: #d97706; font-size: 1rem; }

.anomalies-title {
  font-size: 1rem;
  font-weight: 700;
  color: #1f355e;
  padding-left: 0.75rem;
  border-left: 4px solid #ef4444;
  border-radius: 0;
  margin-bottom: 1rem;
}

.anomaly-card {
  background: white;
  border-radius: 12px;
  border: 1.5px solid #e8edf2;
  overflow: hidden;
  margin-bottom: 0.75rem;
  box-shadow: 0 1px 4px rgba(31,53,94,0.06);
  animation: fadeInUp 0.4s ease both;
  opacity: 0;
}

.anomaly-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #fef2f2;
  border-bottom: 1px solid #fee2e2;
}

.anomaly-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #ef4444;
  flex-shrink: 0;
}

.anomaly-label {
  font-size: 0.8rem;
  font-weight: 700;
  color: #991b1b;
}

.anomaly-body {
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
  color: #374a67;
  line-height: 1.6;
  border-bottom: 1px solid #e8edf2;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem 0.4rem;
  background: #f0fdf4;
}

.suggestion-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  background: #16a34a;
  flex-shrink: 0;
}

.suggestion-label {
  font-size: 0.8rem;
  font-weight: 700;
  color: #15803d;
}

.suggestion-body {
  padding: 0 1rem 0.75rem;
  font-size: 0.82rem;
  color: #15803d;
  line-height: 1.6;
  font-style: italic;
  background: #f0fdf4;
}

.no-anomaly-box {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #dcfce7;
  border: 1.5px solid #86efac;
  border-radius: 10px;
  padding: 0.75rem 1rem;
  font-size: 0.875rem;
  color: #15803d;
  font-weight: 600;
}

@media (max-width: 768px) {
  .score-section { grid-template-columns: 1fr; }
  .report-page__header { flex-direction: column; gap: 1rem; }
}
</style>
