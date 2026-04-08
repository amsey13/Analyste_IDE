<script setup>
import {ref, onMounted, watch} from 'vue';
import {useRoute} from 'vue-router';
import {UserService} from '../features/users/api/UserService';
import Button from 'primevue/button';

const user = ref(null);
const route = useRoute();


const isSidebarCollapsed = ref(false);

onMounted(async () => {
  try {
    user.value = await UserService.getCurrentUser();
  } catch (error) {
    console.error('Session introuvable, retour à l’accueil');
  }


  const savedSidebarState = localStorage.getItem('sidebar-collapsed');
  if (savedSidebarState !== null) {
    isSidebarCollapsed.value = savedSidebarState === 'true';
  }
});

watch(isSidebarCollapsed, (value) => {
  localStorage.setItem('sidebar-collapsed', String(value));
});


const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value;

  if (!isSidebarCollapsed.value) {
    openMode.value = null;
  }
};

const modeDescriptions = {
  accompagnement: 'Faites-vous accompagner pour votre analyse de A à Z avec cette fonctionnalité. Définissez vos acteurs, vos User-Stories,..',
  audit: 'Analysez et auditez la qualité de vos modélisations fonctionnelles (BPMN, MCD, User Stories).'
};

const logout = () => {
  window.location.href = 'http://localhost:8080/logout';
};


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

        <router-link
            to="/app/projects"
            class="sidebar__link"
            :class="{ 'sidebar__link--active': isActiveRoute('/app/projects') }"
            :title="isSidebarCollapsed ? 'Accueil' : ''"
        >
          <i class="pi pi-home sidebar__link-icon"></i>
          <span v-if="!isSidebarCollapsed" class="sidebar__link-text">Accueil</span>
        </router-link>


        <div v-if="!isSidebarCollapsed" class="sidebar__section-title">
          Modes
        </div>


        <router-link
            to="/app/accompagnement"
            class="sidebar__link"
            :class="{ 'sidebar__link--active': route.path.startsWith('/app/accompagnement') }"
            :title="isSidebarCollapsed ? 'Accompagnement' : ''"
        >
          <i class="pi pi-users sidebar__link-icon"></i>
          <span v-if="!isSidebarCollapsed" class="sidebar__link-text">Accompagnement</span>
        </router-link>


        <router-link
            to="/app/audit"
            class="sidebar__link"
            :class="{ 'sidebar__link--active': route.path.startsWith('/app/audit') }"
            :title="isSidebarCollapsed ? 'Audit Qualité' : ''"
        >
          <i class="pi pi-check-circle sidebar__link-icon"></i>
          <span v-if="!isSidebarCollapsed" class="sidebar__link-text">Audit Qualité</span>
        </router-link>
      </nav>


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


    <div class="content-area">
      <header class="topbar">
        <div class="topbar__left">

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
</style>