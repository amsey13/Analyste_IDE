import { createRouter, createWebHistory } from 'vue-router'
import { UserService } from '../features/core/api/UserService';

// 1. Imports des vues et du Layout (Ne pas oublier MainLayout !)
import HomeView from '../features/core/views/HomeView.vue'
import DashboardView from '../features/core/views/DashboardView.vue'
import MainLayout from '../layouts/MainLayout.vue'
import ProjetSelector from '../features/core/views/ProjetSelector.vue'

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
                name: 'projets',
                component: ProjetSelector,
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

// 3. Le Vigile (Navigation Guard)
router.beforeEach(async (to, from, next) => {
    if (to.meta.requiresAuth) {
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