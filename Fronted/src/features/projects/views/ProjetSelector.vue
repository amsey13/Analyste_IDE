<script setup>

import {ref, onMounted} from 'vue';
import {ProjetService} from '../api/ProjetService';
import {useRouter} from 'vue-router';
import { useConfirm } from "primevue/useconfirm";
import { useToast } from "primevue/usetoast";
import Button  from 'primevue/button';
import Card from 'primevue/card';
import ConfirmDialog from 'primevue/confirmdialog';
import Toast from 'primevue/toast';


const projets = ref([]);
const router = useRouter();
const loading = ref(false);
const confirm = useConfirm();
const toast = useToast();

onMounted(async () => {
  loading.value = true;
  try {
    const data = await ProjetService.getProjects();
    projets.value = data;
    console.log("Projets récupérés avec succès", data);
  } catch (e) {

    console.error("Erreur de récupération des projets", e);

  }finally {
      loading.value = false;
    }
});

const openProjet = (idProjet) =>{
    router.push({
        name: 'projet-dashboard',
        params: { id: idProjet }
    });
};


const goToCreate = () => {
    router.push({ name: 'projet-create' });
};

const deleteProjet = (idProjet) => {
    confirm.require({
        message: 'Êtes-vous sûr de vouloir supprimer ce projet ?',
        header: 'Confirmation de suppression',
        icon: 'pi pi-exclamation-triangle',
        accept: async () => {
            try {
                await ProjetService.deleteProjet(idProjet);
                projets.value = projets.value.filter(p => p.idProjet !== idProjet);
                toast.add({ severity: 'success', summary: 'Succès', detail: 'Projet supprimé' });
            } catch (e) {
                console.error("Erreur lors de la suppression du projet", e);
                toast.add({ severity: 'error', summary: 'Erreur', detail: 'Impossible de supprimer le projet' });
            }
        }
    });
};




</script>

<template>
  <Toast />
    <ConfirmDialog />
   <div class="card p-5">
        <div class="flex justify-content-between align-items-center mb-5">
            <h1>Mes Projets</h1>
            <Button label="Créer un nouveau projet" icon="pi pi-plus" @click="goToCreate" />
        </div>

        <div v-if="loading">Chargement...</div>

        <div v-else class="grid">

            <div v-for="p in projets" :key="p.idProjet" class="col-12 md:col-6 lg:col-4">
                <Card class="h-full cursor-pointer hover:shadow-4 transition-duration-200" @click="openProjet(p.idProjet)">
                    <template #title>{{ p.nom }}</template>
                    <template #content>
                        <p class="m-0 text-600">{{ p.description || 'Pas de description' }}</p>
                    </template>
                    <template #footer>
                      <div class="flex justify-content-end pt-3">
                        <Button
                            icon="pi pi-trash"
                            severity="danger"
                            text
                            aria-label="Supprimer"
                            rounded
                            @click.stop="deleteProjet(p.idProjet)"
                        />
                      </div>
                    </template>
                </Card>
            </div>
             
            <div v-if="projets.length === 0" class="col-12 text-center text-500">
                Aucun projet trouvé. Cliquez sur "Créer" pour commencer.
            </div>
        </div>
    </div>
</template>