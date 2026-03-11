<script setup>
import Card from 'primevue/card'
import Button from 'primevue/button'
</script>

<template>
  <div class="p-5">

    <div class="mb-5">
      <h1 class="text-3xl font-bold">Configuration de l'Audit</h1>
      <p class="text-600">
        Sélectionnez les sources à croiser pour l'analyse de cohérence.
      </p>
    </div>

    <div class="grid">

      <div class="col-12 md:col-4">
        <Card>
          <template #title>
            <i class="pi pi-cog mr-2"></i>
            1. Processus (BPMN)
          </template>

          <template #content>
            <p class="text-600">Le "Qui fait quoi".</p>
            <p class="text-500">En attente...</p>
          </template>
        </Card>
      </div>

      <div class="col-12 md:col-4">
        <Card>
          <template #title>
            <i class="pi pi-database mr-2"></i>
            2. Données (MCD)
          </template>

          <template #content>
            <p class="text-600">La structure statique (JMerise).</p>
            <p class="text-500">En attente...</p>
          </template>
        </Card>
      </div>

      <div class="col-12 md:col-4">
        <Card>
          <template #title>
            <i class="pi pi-sync mr-2"></i>
            3. Flux (MFC)
          </template>

          <template #content>
            <p class="text-600">La vue macroscopique (PlantUML).</p>
            <p class="text-500">En attente...</p>
          </template>
        </Card>
      </div>

    </div>

    <div class="mt-5 text-center text-600">
      ⚠ Veuillez fournir au moins 2 fichiers pour lancer une comparaison.
    </div>

    <div class="mt-4 flex justify-content-center">
      <Button label="Générer le Rapport d'Audit" icon="pi pi-file" />
    </div>

  </div>
</template>