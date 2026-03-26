import { createRouter, createWebHistory } from 'vue-router'
import { UserService } from '../features/users/api/UserService'

const HomeView = () => import('../views/HomeView.vue')
const MainLayout = () => import('../layouts/MainLayout.vue')
const ProjetCreateView = () => import('../features/projects/views/ProjectCreateView.vue')
const ProjetSelector = () => import('../features/projects/views/ProjectSelector.vue')
const DashboardView = () => import('../views/DashboardView.vue')
const AccompagnementView = () => import('../views/AccompagnementView.vue')
const AllProjectsView = () => import('../features/projects/views/AllProjectsView.vue')
const AuditView = () => import('../views/AuditView.vue')
const SupportProjectAuditResultView = () => import('../views/ProjectAuditResultView.vue')

const routes = [
    {
        path: '/',
        name: 'home',
        component: HomeView
    },
    {
        path: '/app',
        component: MainLayout,
        children: [
            {
                path: 'projects',
                name: 'projects-lite',
                component: ProjetSelector,
                meta: { requiresAuth: true }
            },
            {
                path: 'projects/all',
                name: 'all-projects',
                component: AllProjectsView,
                meta: { requiresAuth: true }
            },
            {
                path: 'project/create',
                name: 'project-create',
                component: ProjetCreateView,
                meta: { requiresAuth: true }
            },
            {
                path: 'project/:id',
                name: 'project-dashboard',
                component: DashboardView,
                meta: { requiresAuth: true }
            },
            {
            path: 'accompagnement/:id',
            name: 'accompagnement',
            component: AccompagnementView,
            props: true,
            meta: { requiresAuth: true }
            },
            {
            path: 'audit/:id',
            name: 'audit',
            component: AuditView,
            props: true,
            meta: { requiresAuth: true }
            },
            {
                path: '/project/:id/audit-result',
                name: 'ProjectAuditResult',
                component: SupportProjectAuditResultView,
                meta: { requiresAuth: true }
            },
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

let isUserAuthenticated = false

router.beforeEach(async (to, from, next) => {
    if (to.meta.requiresAuth) {
        if (isUserAuthenticated) {
            return next()
        }

        try {
            await UserService.getCurrentUser()
            isUserAuthenticated = true
            next()
        } catch (error) {
            console.error("Accès refusé, redirection vers l'accueil")
            next({ name: 'home' })
        }
    } else {
        next()
    }
})

export default router