<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute } from 'vue-router';
import { UserService } from '../features/users/api/UserService';
import Button from 'primevue/button';

const user = ref(null);
const route = useRoute();

const isSidebarCollapsed = ref(false);

const currentProjectId = computed(() => route.params.id || null);

const menuItems = computed(() => [
  {
    label: 'Accueil',
    icon: 'pi pi-home',
    to: '/app/projects',
    exact: false
  },
  {
    label: 'Accompagnement',
    icon: 'pi pi-users',
    to: currentProjectId.value ? `/app/accompagnement/${currentProjectId.value}` : '/app/projects',
    disabled: !currentProjectId.value
  },
  {
    label: 'Audit Qualité',
    icon: 'pi pi-check-circle',
    to: currentProjectId.value ? `/app/audit/${currentProjectId.value}` : '/app/projects',
    disabled: !currentProjectId.value
  }
]);

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
};

const logout = () => {
  window.location.href = 'http://localhost:8080/logout';
};

const isActiveRoute = (path) => {
  if (path === '/app/projects') {
    return route.path === '/app/projects' || route.path === '/app/projects/all' || route.path === '/app/project/create';
  }

  return route.path.startsWith(path.split('/:')[0]) || route.path.startsWith(path);
};
</script>

<template>
  <div class="app-shell">
    <aside
        :class="['sidebar', { 'sidebar--collapsed': isSidebarCollapsed }]"
    >
      <div class="sidebar__brand">
        <div class="sidebar__brand-left">
          <i class="pi pi-chart-bar sidebar__brand-icon"></i>
          <span v-if="!isSidebarCollapsed" class="sidebar__brand-text">AnalytiQ</span>
        </div>
      </div>

      <nav class="sidebar__nav">
        <router-link
            v-for="item in menuItems"
            :key="item.label"
            :to="item.to"
            class="sidebar__link"
            :class="{
            'sidebar__link--active': isActiveRoute(item.to),
            'sidebar__link--disabled': item.disabled
          }"
            :title="isSidebarCollapsed ? item.label : ''"
        >
          <i :class="[item.icon, 'sidebar__link-icon']"></i>
          <span v-if="!isSidebarCollapsed" class="sidebar__link-text">
            {{ item.label }}
          </span>
        </router-link>

        <div v-if="!isSidebarCollapsed" class="sidebar__section-title">
          Modes
        </div>

        <router-link
            :to="currentProjectId ? `/app/accompagnement/${currentProjectId}` : '/app/projects'"
            class="sidebar__link"
            :class="{
            'sidebar__link--active': route.path.startsWith('/app/accompagnement'),
            'sidebar__link--disabled': !currentProjectId
          }"
            :title="isSidebarCollapsed ? 'Accompagnement' : ''"
        >
          <i class="pi pi-users sidebar__link-icon"></i>
          <span v-if="!isSidebarCollapsed" class="sidebar__link-text">Accompagnement</span>
        </router-link>

        <router-link
            :to="currentProjectId ? `/app/audit/${currentProjectId}` : '/app/projects'"
            class="sidebar__link"
            :class="{
            'sidebar__link--active': route.path.startsWith('/app/audit'),
            'sidebar__link--disabled': !currentProjectId
          }"
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
  width: 18rem;
  min-width: 18rem;
  max-width: 18rem;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--secondary-color);
  color: white;
  border-right: 1px solid var(--border-color);
  transition: width 0.25s ease, min-width 0.25s ease, max-width 0.25s ease;
}

.sidebar--collapsed {
  width: 5rem;
  min-width: 5rem;
  max-width: 5rem;
}

.sidebar__brand {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 72px;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.sidebar__brand-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  overflow: hidden;
  white-space: nowrap;
}

.sidebar__brand-icon {
  font-size: 1.5rem;
  color: var(--accent-color);
}

.sidebar__brand-text {
  font-size: 1.25rem;
  font-weight: 700;
}

.sidebar__nav {
  flex: 1;
  padding: 1rem 0.75rem;
  overflow-y: auto;
}

.sidebar__section-title {
  margin: 1rem 0 0.5rem;
  padding: 0 0.75rem;
  font-size: 0.75rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(255, 255, 255, 0.6);
}

.sidebar__link {
  display: flex;
  align-items: center;
  gap: 0.875rem;
  padding: 0.9rem 0.95rem;
  margin-bottom: 0.35rem;
  border-radius: 12px;
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

.sidebar__link--disabled {
  opacity: 0.7;
}

.sidebar__link-icon {
  font-size: 1.1rem;
  min-width: 1.25rem;
  text-align: center;
}

.sidebar__link-text {
  font-weight: 500;
}

.sidebar--collapsed .sidebar__link {
  justify-content: center;
  padding-left: 0;
  padding-right: 0;
}

.sidebar__footer {
  padding: 0.75rem;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

:deep(.sidebar__logout-btn) {
  width: 100%;
  justify-content: flex-start;
  color: white !important;
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
  height: 4rem;
  background: white;
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 1.5rem;
}

.topbar__left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 0;
}

.topbar__menu-btn {
  flex-shrink: 0;
}

.topbar__title {
  font-weight: 600;
  color: #4b5563;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topbar__user {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-left: 1rem;
}

.topbar__user-name {
  font-weight: 500;
  color: #111827;
}

.topbar__avatar {
  width: 2rem;
  height: 2rem;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--primary-color);
  color: white;
  font-weight: 700;
}

.page-content {
  flex: 1;
  padding: 1.5rem;
  overflow-y: auto;
}
</style>