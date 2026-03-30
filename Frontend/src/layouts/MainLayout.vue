<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router';
import { UserService } from '../features/users/api/UserService';
import Button from 'primevue/button';

const user = ref(null);
const route = useRoute();

// État du sidebar : réduit ou étendu
const isSidebarCollapsed = ref(false);

onMounted(async () => {
  try {
    // Récupération de l'utilisateur connecté
    user.value = await UserService.getCurrentUser();
  } catch (error) {
    // En cas de session absente/invalide
    console.error('Session introuvable, retour à l’accueil');
  }

  // Restauration de l'état du sidebar depuis le navigateur
  const savedSidebarState = localStorage.getItem('sidebar-collapsed');
  if (savedSidebarState !== null) {
    isSidebarCollapsed.value = savedSidebarState === 'true';
  }
});

// Sauvegarde automatique de l'état du sidebar
watch(isSidebarCollapsed, (value) => {
  localStorage.setItem('sidebar-collapsed', String(value));
});

// Ouvre / réduit le sidebar
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;
};

const modeDescriptions = {
  accompagnement: 'Faites-vous accompagner pour votre analyse de A à Z avec cette fonctionnalité. Définissez vos acteurs, vos User-Stories,.. je suis fatigué LOL.',
  audit: 'Analysez et auditez la qualité de vos modélisations fonctionnelles (BPMN, MCD, User Stories).'
};

const openMode = ref(null);

const toggleMode = (mode) => {
  openMode.value = openMode.value === mode ? null : mode;
};

// Déconnexion utilisateur
const logout = () => {
  window.location.href = 'http://localhost:8080/logout';
};

// Gestion simple de l'état actif des liens du menu
const isActiveRoute = (path) => {
  if (path === '/app/projects') {
    return route.path === '/app/projects' || route.path === '/app/projects/all' || route.path === '/app/project/create';
  }

  return route.path.startsWith(path);
};
</script>

<template>
  <div class="app-shell">
    <!-- Sidebar principal -->
    <aside :class="['sidebar', { 'sidebar--collapsed': isSidebarCollapsed }]">
      <!-- Marque / logo -->
      <div class="sidebar__brand">
        <div class="sidebar__brand-left">
          <i class="pi pi-chart-bar sidebar__brand-icon"></i>
          <span v-if="!isSidebarCollapsed" class="sidebar__brand-text">AnalytiQ</span>
        </div>
      </div>

      <!-- Navigation -->
      <nav class="sidebar__nav">
        <!-- Accueil conservé -->
        <router-link
            to="/app/projects"
            class="sidebar__link"
            :class="{ 'sidebar__link--active': isActiveRoute('/app/projects') }"
            :title="isSidebarCollapsed ? 'Accueil' : ''"
        >
          <i class="pi pi-home sidebar__link-icon"></i>
          <span v-if="!isSidebarCollapsed" class="sidebar__link-text">Accueil</span>
        </router-link>

        <!-- Section Modes -->
        <div v-if="!isSidebarCollapsed" class="sidebar__section-title">
          Modes
        </div>

        <!-- Mode Accompagnement -->
        <div class="sidebar__mode-block">
          <div
              class="sidebar__link"
              :class="{ 'sidebar__link--active': route.path.startsWith('/app/accompagnement') }"
              @click="toggleMode('accompagnement')"
          >
            <i class="pi pi-users sidebar__link-icon"></i>
            <span v-if="!isSidebarCollapsed" class="sidebar__link-text">Accompagnement</span>
            <i
                v-if="!isSidebarCollapsed"
                :class="openMode === 'accompagnement' ? 'pi pi-chevron-up' : 'pi pi-chevron-down'"
                class="sidebar__link-chevron"
            ></i>
          </div>
          <Transition name="slide-down">
            <div v-if="openMode === 'accompagnement' && !isSidebarCollapsed" class="sidebar__mode-desc">
              <p>{{ modeDescriptions.accompagnement }}</p>
              <router-link to="/app/projects/all?type=accompagnement" class="sidebar__mode-link">
                <i class="pi pi-arrow-right mr-1"></i> Voir mes projets d'accompagnement
              </router-link>
            </div>
          </Transition>
        </div>

        <!-- Mode Audit -->
        <div class="sidebar__mode-block">
          <div
              class="sidebar__link"
              :class="{ 'sidebar__link--active': route.path.startsWith('/app/audit') }"
              @click="toggleMode('audit')"
          >
            <i class="pi pi-search sidebar__link-icon"></i>
            <span v-if="!isSidebarCollapsed" class="sidebar__link-text">Audit Qualité</span>
            <i
                v-if="!isSidebarCollapsed"
                :class="openMode === 'audit' ? 'pi pi-chevron-up' : 'pi pi-chevron-down'"
                class="sidebar__link-chevron"
            ></i>
          </div>
          <Transition name="slide-down">
            <div v-if="openMode === 'audit' && !isSidebarCollapsed" class="sidebar__mode-desc">
              <p>{{ modeDescriptions.audit }}</p>
              <router-link to="/app/projects/all?type=audit" class="sidebar__mode-link">
                <i class="pi pi-arrow-right mr-1"></i> Voir mes projets d'audit
              </router-link>
            </div>
          </Transition>
        </div>

      </nav>

      <!-- Pied du sidebar -->
      <div class="sidebar__footer">
        <Button
            :label="isSidebarCollapsed ? '' : 'Déconnexion'"
            icon="pi pi-power-off"
            severity="danger"
            text
            class="sidebar__logout-btn"
            :title="isSidebarCollapsed ? 'Déconnexion' : ''"
            @click="logout"
        />
      </div>
    </aside>

    <!-- Zone de contenu principale -->
    <div class="content-area">
      <header class="topbar">
        <div class="topbar__left">
          <!-- Bouton hamburger -->
          <Button
              icon="pi pi-bars"
              text
              rounded
              aria-label="Réduire ou agrandir la barre latérale"
              class="topbar__menu-btn"
              @click="toggleSidebar"
          />
          <span class="topbar__title">IDE d'Analyse Fonctionnelle - AnalytiQ</span>
        </div>

        <!-- Infos utilisateur -->
        <div v-if="user" class="topbar__user">
          <span class="topbar__user-name">{{ user.fullName }}</span>
          <div class="topbar__avatar">
            {{ user.fullName.charAt(0) }}
          </div>
        </div>
      </header>

      <main class="page-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--bg-light);
}

.sidebar {
  width: 16.2rem;
  min-width: 16.2rem;
  max-width: 16.2rem;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--secondary-color);
  color: white;
  border-right: 1px solid var(--border-color);
  transition: width 0.25s ease, min-width 0.25s ease, max-width 0.25s ease;
}

.sidebar--collapsed {
  width: 4.5rem;
  min-width: 4.5rem;
  max-width: 4.5rem;
}

.sidebar__brand {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 65px;
  padding: 0.9rem 1.125rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar__brand-left {
  display: flex;
  align-items: center;
  gap: 0.675rem;
  overflow: hidden;
  white-space: nowrap;
}

.sidebar__brand-icon {
  font-size: 1.35rem;
  color: var(--accent-color);
}

.sidebar__brand-text {
  font-size: 1.125rem;
  font-weight: 700;
}

.sidebar__nav {
  flex: 1;
  padding: 0.9rem 0.675rem;
  overflow-y: auto;
}

.sidebar__section-title {
  margin: 0.9rem 0 0.45rem;
  padding: 0 0.675rem;
  font-size: 0.675rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(255, 255, 255, 0.6);
}

.sidebar__link-wrapper {
  position: relative;
}

.sidebar__link-wrapper[title]:hover::after {
  content: attr(title);
  position: absolute;
  left: calc(100% + 12px);
  top: 50%;
  transform: translateY(-50%);
  background: #1e293b;
  color: white;
  font-size: 0.78rem;
  line-height: 1.5;
  padding: 0.5rem 0.75rem;
  border-radius: 8px;
  white-space: normal;
  width: 200px;
  z-index: 999;
  pointer-events: none;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.sidebar__link {
  display: flex;
  align-items: center;
  gap: 0.7875rem;
  padding: 0.81rem 0.855rem;
  margin-bottom: 0.315rem;
  border-radius: 10.8px;
  color: #dbe7ff;
  text-decoration: none;
  transition: background-color 0.2s ease, color 0.2s ease;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar__link:hover {
  background-color: rgba(255, 255, 255, 0.08);
  color: white;
}

.sidebar__link--active {
  background-color: rgba(0, 184, 217, 0.16);
  color: white;
}

.sidebar__link-icon {
  font-size: 0.99rem;
  min-width: 1.125rem;
  text-align: center;
}

.sidebar__link-text {
  font-size: 0.9rem;
  font-weight: 500;
}

.sidebar--collapsed .sidebar__link {
  justify-content: center;
  padding-left: 0;
  padding-right: 0;
}

.sidebar__footer {
  padding: 0.675rem;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

:deep(.sidebar__logout-btn) {
  width: 100%;
  justify-content: flex-start;
  color: white !important;
  font-size: 0.9rem;
}

.sidebar--collapsed :deep(.sidebar__logout-btn) {
  justify-content: center;
}

.content-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: var(--bg-light);
}

.topbar {
  height: 3.6rem;
  background: white;
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.35rem;
}

.topbar__left {
  display: flex;
  align-items: center;
  gap: 0.675rem;
  min-width: 0;
}

.topbar__menu-btn {
  flex-shrink: 0;
}

.topbar__title {
  font-size: 0.9rem;
  font-weight: 600;
  color: #4b5563;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topbar__user {
  display: flex;
  align-items: center;
  gap: 0.675rem;
  margin-left: 0.9rem;
}

.topbar__user-name {
  font-size: 0.9rem;
  font-weight: 500;
  color: #111827;
}

.topbar__avatar {
  width: 1.8rem;
  height: 1.8rem;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  font-size: 0.9rem;
  font-weight: 700;
}

.page-content {
  flex: 1;
  padding: 1.35rem;
  overflow-y: auto;
}

.sidebar__mode-block {
  margin-bottom: 0.315rem;
}

.sidebar__link-chevron {
  margin-left: auto;
  font-size: 0.75rem;
  opacity: 0.7;
}

.sidebar__mode-desc {
  margin: 0 0.5rem 0.5rem;
  padding: 0.75rem;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 8px;
  border-left: 3px solid rgba(0, 184, 217, 0.5);
}

.sidebar__mode-desc p {
  font-size: 0.78rem;
  color: rgba(255, 255, 255, 0.75);
  line-height: 1.5;
  margin: 0 0 0.6rem;
}

.sidebar__mode-link {
  font-size: 0.78rem;
  font-weight: 600;
  color: #00b8d9;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  transition: opacity 0.2s;
}

.sidebar__mode-link:hover {
  opacity: 0.8;
}

.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}

.slide-down-enter-from,
.slide-down-leave-to {
  opacity: 0;
  max-height: 0;
  transform: translateY(-6px);
}

.slide-down-enter-to,
.slide-down-leave-from {
  opacity: 1;
  max-height: 200px;
}

</style>