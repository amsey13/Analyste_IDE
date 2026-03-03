import { createRouter, createWebHistory } from 'vue-router'
import { UserService } from '../features/users/api/UserService';

const HomeView = () => import('../features/users/views/HomeView.vue')
const MainLayout = () => import('../layouts/MainLayout.vue')
const ProjetCreateView = () => import('../features/projects/views/ProjetCreateView.vue')
const ProjetSelector = () => import('../features/projects/views/ProjetSelector.vue')
const DashboardView = () => import('../features/users/views/DashboardView.vue')

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
                path: 'projets',
                name: 'projets-lite',
                component: ProjetSelector,
                meta: { requiresAuth: true }
            },

            {
                path: 'projet/create',
                name: 'projet-create',
                component: ProjetCreateView,
                meta: { requiresAuth: true }
            },
            {
                path: 'projet/:id',
                name: 'projet-dashboard',
                component: DashboardView,
                meta: { requiresAuth: true }
            }
            // Les futurs modules d'analyse fonctionnelle iront ici
        ]
    }
]

// 2. Initialisation de l'instance router (Crucial !)
const router = createRouter({
    history: createWebHistory(),
    routes
})

let isUserAuthenticated = false;

// 3. Le Vigile (Navigation Guard)
router.beforeEach(async (to, from, next) => {
    if (to.meta.requiresAuth) {

        if (isUserAuthenticated) {
            return next();
        }

        try {
            await UserService.getCurrentUser();
            next(); // Session valide : accès accordé
        } catch (error) {
            console.error("Accès refusé, redirection vers l'accueil");
            next({ name: 'home' }); // Retour sécurisé vers la page de login
        }
    } else {
        next(); // Page publique
    }
})

export default router