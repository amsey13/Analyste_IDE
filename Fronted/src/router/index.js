import { createRouter, createWebHistory } from 'vue-router'
import { UserService } from '../services/UserService'

// 1. Imports des vues et du Layout (Ne pas oublier MainLayout !)
import HomeView from '../views/HomeView.vue'
import DashboardView from '../views/DashboardView.vue'
import MainLayout from '../layouts/MainLayout.vue' // 👈 Correction ici

const routes = [
    {
        path: '/',
        name: 'home', // 👈 Ajout du nom pour le guard
        component: HomeView
    },
    {
        path: '/app',
        component: MainLayout, // Le conteneur avec Sidebar et Topbar
        children: [
            {
                path: 'dashboard',
                name: 'dashboard',
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