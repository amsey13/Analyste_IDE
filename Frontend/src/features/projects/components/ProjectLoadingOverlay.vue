<script setup>
import { ref, onMounted, watch } from 'vue';

const props = defineProps({
  visible: { type: Boolean, default: false },
  projectName: { type: String, default: '' },
  mode: { type: String, default: 'open' } // 'open' ou 'create'
});

const messages = {
  open: [
    'Chargement de votre espace de travail...',
    'Récupération des données du projet...',
    'Synchronisation en cours...',
    'Presque prêt...'
  ],
  create: [
    'Création de votre projet...',
    'Initialisation des modules...',
    'Configuration de l\'environnement...',
    'Presque prêt...'
  ]
};

const currentMessage = ref('');
let interval = null;

const startMessages = () => {
  const list = messages[props.mode] || messages.open;
  let i = 0;
  currentMessage.value = list[0];
  interval = setInterval(() => {
    i = (i + 1) % list.length;
    currentMessage.value = list[i];
  }, 1500);
};

const stopMessages = () => {
  if (interval) clearInterval(interval);
};

watch(() => props.visible, (val) => {
  if (val) startMessages();
  else stopMessages();
});

onMounted(() => {
  if (props.visible) startMessages();
});
</script>

<template>
  <Transition name="overlay-fade">
    <div v-if="visible" class="overlay">
      <div class="spinner-wrap">
        <div class="spinner"></div>
        <div class="spinner-inner"></div>
      </div>

      <div class="project-name">{{ projectName }}</div>

      <Transition name="text-fade" mode="out-in">
        <div :key="currentMessage" class="loading-text">{{ currentMessage }}</div>
      </Transition>

      <div class="progress-bar-wrap">
        <div class="progress-bar"></div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
@keyframes spin {
  to { transform: rotate(360deg); }
}
@keyframes progress {
  from { width: 0%; } to { width: 100%; }
}

.overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: #1f355e;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2rem;
  z-index: 9999;
}

.spinner-wrap {
  position: relative;
  width: 64px;
  height: 64px;
}

.spinner {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.1);
  border-top-color: #00b8d9;
  animation: spin 0.8s linear infinite;
}

.spinner-inner {
  position: absolute;
  top: 8px; left: 8px;
  width: 48px; height: 48px;
  border-radius: 50%;
  border: 3px solid rgba(255,255,255,0.05);
  border-bottom-color: #2563eb;
  animation: spin 1.2s linear infinite reverse;
}

.project-name {
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
  text-align: center;
  padding: 0 2rem;
}

.loading-text {
  font-size: 0.95rem;
  color: rgba(255,255,255,0.65);
  text-align: center;
  min-height: 28px;
}

.progress-bar-wrap {
  width: 240px;
  height: 3px;
  background: rgba(255,255,255,0.1);
  border-radius: 10px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: #00b8d9;
  border-radius: 10px;
  animation: progress 4.5s ease both;
}

.overlay-fade-enter-active,
.overlay-fade-leave-active { transition: opacity 0.3s ease; }
.overlay-fade-enter-from,
.overlay-fade-leave-to { opacity: 0; }

.text-fade-enter-active,
.text-fade-leave-active { transition: all 0.3s ease; }
.text-fade-enter-from { opacity: 0; transform: translateY(8px); }
.text-fade-leave-to { opacity: 0; transform: translateY(-8px); }
</style>