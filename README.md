# 🚀 AnalytiQ — Analyste IDE

**AnalytiQ** est une plateforme complète d’assistance à la conception et à la modélisation des **Systèmes d’Information**.

Pensé pour les **analystes métiers**, **architectes logiciels** et **étudiants**, cet IDE web centralise les spécifications, facilite la modélisation et intègre un **audit intelligent automatisé** basé sur l’IA.

---

## ✨ Fonctionnalités principales

### 📖 Spécifications fonctionnelles
* Gestion des **acteurs**
* Rédaction des **user stories**
* Définition des **règles de gestion**
* Centralisation des exigences métier

### ⚙️ Modélisation des processus
* Éditeur **BPMN interactif** (via `bpmn-js`)
* Visualisation claire des flux métiers
* Structuration des processus complexes

### 🗄️ Modélisation des données (MCD)
* Création d’un **dictionnaire de données**
* Génération automatique du **MCD**
* Visualisation via `Mermaid.js`

### 🤖 Audit intelligent (IA)
* Analyse de cohérence entre :
    * User Stories
    * Règles de gestion
    * BPMN
    * MCD
* Détection d’incohérences
* Suggestions d’amélioration automatisées (via **Mistral AI**)

### 📄 Génération de rapports
* Export d’un **rapport d’architecture complet**
* Format **PDF professionnel**
* Génération via `html2pdf.js`

### 💾 Interopérabilité
* Export vers :
    * **JMerise** (`.mcd`)
* Compatible avec les standards académiques et professionnels

---

## 🛠️ Stack technique

Le projet est structuré en deux couches principales :

### 🔙 Backend — Java / Spring Boot
* **Framework :** Spring Boot 3.x
* **ORM :** Spring Data JPA / Hibernate
* **Base de données :** PostgreSQL (Docker)
* **IA :** API Mistral AI
* **Sécurité :** Spring Security + OIDC
* **Migrations DB :** Flyway

### 🎨 Frontend — Vue.js
* **Framework :** Vue.js 3 (Composition API) + Vite
* **UI :** PrimeVue + TailwindCSS / PrimeFlex
* **Modélisation :**
    * `bpmn-js` (processus)
    * `mermaid.js` (diagrammes MCD)
* **Export PDF :** html2pdf.js

---

## 🏗️ Architecture et Découpage des Packages

Le projet adopte une architecture modulaire stricte (proche du *Modular Monolith* pour le Back et du *Feature-Sliced Design* pour le Front) afin de garantir la maintenabilité et l'évolutivité.

### Backend (`src/main/java/com/example/backend`)
L'API est découpée en **modules métier** autonomes :
* `core/` : Configurations globales, utilitaires (cryptage), sécurité (OIDC) et intégration de l'IA Mistral.
* `modules/projects/core/` : Gestion du cycle de vie principal des projets.
* `modules/projects/acc/` : (*Accompagnement*) Entités et services liés à la spécification (User Stories, Acteurs, Dictionnaire, MCD).
* `modules/projects/audit/` : Logique complexe d'audit IA, exports PDF, et connecteurs externes (ex: Taiga).
* `modules/analysis/` : Parsing et traitement des fichiers XML/BPMN/MCD/MFC.
* `modules/analytics/` : Suivi des KPIs, logs d'exécution et statistiques du projet.

---
Au sein de chaque module, le code respecte une stricte séparation des responsabilités (Séparation of Concerns) répartie dans les packages suivants :

* 📍 **`api/` (Controllers) :** Expose les endpoints REST. Il réceptionne les requêtes HTTP du frontend, délègue le travail au Service, et ne renvoie **que** des DTOs (jamais d'entités directes).
* 📦 **`dto/` (Data Transfer Object) :** Objets de transport. Ils servent de "bouclier" entre la base de données et l'extérieur. Ils permettent de filtrer les données (ex: cacher les mots de passe ou les ID internes) et de valider les requêtes entrantes.
* 🔄 **`mapper/` :** Assure la conversion fluide, sécurisée et automatisée entre les objets de la base de données (`Entity`) et les objets exposés à l'API (`DTO`).
* ⚙️ **`service/` :** Le "cerveau" de l'application. Contient toute la logique métier complexe (vérification des droits, calcul des scores d'audit, appels à l'IA Mistral).
* 🗄️ **`dao/` (Data Access Object / Repositories) :** Interfaces Spring Data gérant exclusivement les requêtes et les interactions directes avec la base de données PostgreSQL.
* 🗃️ **`entity/` :** Représente la structure exacte et les relations des tables en base de données (modèles JPA/Hibernate).
* ⚠️ **`exception/` :** Gestion centralisée des erreurs métiers (ex: `ProjectNotFoundException`, `UnauthorizedAccessException`) pour garantir que le frontend reçoive toujours des codes d'erreur HTTP clairs et propres.

---

### Frontend (`src/`)
* `features/` : Découpage par domaine métier (`projects`, `users`). Contient les composants spécifiques, les appels API (`api/`) et les vues internes.
* `views/` : Pages principales de routing (`DashboardView`, `AuditView`, `AccompagnementView`...).
* `api/` : Client HTTP centralisé (`HttpClient.js`) avec intercepteurs.
* `layouts/` : Structure de l'interface (Header, Sidebar).

---

## 💎 Bonnes Pratiques Appliquées

1. **Sécurité par l'isolation :** Le frontend ne voit jamais la structure réelle de la base de données grâce au pattern **DTO + Mapper**.
2. **Inversion de Contrôle (IoC) & Injection de Dépendances :** Utilisation systématique de l'injection par constructeur pour garantir que les services soient testables (Mocking) et faiblement couplés.
3. **Traçabilité & Historisation :** Utilisation des annotations `@CreationTimestamp` et `@UpdateTimestamp` d'Hibernate pour le suivi automatique du cycle de vie des entités.
4. **Gestion des Migrations BDD :** Utilisation de scripts SQL versionnés (Flyway/Scripts natifs) garantissant la reproductibilité de la base de données sur n'importe quel environnement.
5. **Chiffrement des données sensibles :** Utilisation d'`AttributeEncryptor` pour sécuriser les données directement avant l'insertion en base.

---
## ⚙️ Prérequis

Avant installation, assure-toi d’avoir :
* ☕ **Java 17+**
* 🟢 **Node.js (v16+) + npm**
* 🐳 **Docker & Docker Compose**

---

## 🚀 Installation & Lancement

### 1️⃣ Cloner le projet

```bash
git clone [https://gitlab-etu.fil.univ-lille.fr/glauriel-fosther.badjila-wandja-legara.etu/projet-si.git](https://gitlab-etu.fil.univ-lille.fr/glauriel-fosther.badjila-wandja-legara.etu/projet-si.git)
cd projet-si
```

---

### 2️⃣ Démarrer la base de données

```bash
docker-compose up -d
```

---

### 3️⃣ Lancer le Backend

```bash
cd Backend
```
⚠️ Étape cruciale : Configuration de l'environnement
Vous devez copier le fichier d'exemple .env.example, le renommer en .env, et y renseigner vos identifiants locaux.

Pour cela, exécutez d'abord :

```bash
cp .env.example .env
```

Ouvrez ensuite le fichier .env fraîchement créé et collez/remplissez les valeurs suivantes

```bash
JUMPCLOUD_CLIENT_ID=9c839027-42b2-4219-a0ec-664a896f9813
JUMPCLOUD_CLIENT_SECRET=bJd8zDtWfETF1npFmXm8WoSYym
ENCRYPTION_KEY=hgkauthkpnchirzg
API_KEY_MISTRAL=df8d6CT9rG9nBJEdIUVaSGDeIIl5A4Bz
```
Puis lancer :

```bash
mvn clean spring-boot:run
```

Backend disponible sur :
http://localhost:8080

---

### 4️⃣ Lancer le Frontend

```bash
cd Frontend
npm install
npm run dev
```

Frontend disponible sur :
http://localhost:5173

---

## 🏗️ Structure du projet

```plaintext
analyste_ide/
├── Backend/               # API Spring Boot
│   ├── src/main/java/     # Code métier (Core, Auth, Analyse)
│   ├── libs/              # JMerise, JFlux
│   ├── pom.xml
│   └── .env.example
│
├── Frontend/              # Application Vue.js
│   ├── src/features/      # Modules fonctionnels
│   ├── src/views/         # Pages (Dashboard, Audit...)
│   ├── package.json
│   └── vite.config.js
│
└── compose.yaml           # Infrastructure Docker
```

---

## 🎯 Objectif du projet

Ce projet vise à créer une **synergie entre** :

* 🧠 **Ingénierie des SI**

    * Merise
    * BPMN
    * Méthodes agiles

* 🤖 **Intelligence Artificielle Générative**

    * Analyse automatique
    * Assistance à la conception
    * Amélioration continue des modèles

---

## 👨‍💻 Auteur

- Christian KALEMBA ZAYI
- Thi Thuy Tien NGUYEN
- Kadiatou BARRY
- Mamady MANSARE
- Glauriel Fosther BADJILA WANDJA

---


