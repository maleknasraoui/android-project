# Rapport - HiCo Assets Manager

## 1. Presentation

HiCo Assets Manager est une application Android developpee en Java pour une entreprise fictive appelee HiCo, Hive Corporation. Elle permet de gerer les biens mobiliers et immobiliers de l'entreprise avec un stockage local SQLite.

## 2. Fonctionnalites

- Connexion avec deux roles: administrateur et utilisateur normal.
- Administrateur: ajouter, modifier, supprimer et consulter les biens.
- Utilisateur normal: consulter uniquement les biens.
- Demande de creation de compte avec choix du role `admin` ou `user`.
- Validation ou refus des demandes de comptes par l'administrateur.
- Liste des biens avec RecyclerView et CardView.
- Recherche par nom.
- Filtre par type: Tous, Mobilier, Immobilier.
- Compteur simple des biens affiches.
- Confirmation avant suppression.
- Splash screen et theme jaune/noir inspire du monde des abeilles.

## 3. Comptes de test

- Administrateur: `admin` / `admin123`
- Utilisateur: `user` / `user123`

## 4. Architecture du projet

```text
HiCoAssetsManager/
  app/src/main/
    AndroidManifest.xml
    java/com/hico/assetsmanager/
      SplashActivity.java
      LoginActivity.java
      MainActivity.java
      AddEditBienActivity.java
      SignupActivity.java
      UserRequestsActivity.java
      adapter/BienAdapter.java
      db/DatabaseHelper.java
      model/Bien.java
      provider/BienContentProvider.java
      receiver/BootReceiver.java
      receiver/TimeChangeReceiver.java
      receiver/BienRequestReceiver.java
    res/layout/
      activity_splash.xml
      activity_login.xml
      activity_main.xml
      activity_add_edit_bien.xml
      item_bien.xml
    res/drawable/
      ic_hico_logo.xml
      bg_login.xml
      bg_field.xml
    res/values/
      colors.xml
      strings.xml
      themes.xml
```

## 5. Diagramme simplifie

```text
Utilisateur
   |
   v
SplashActivity -> LoginActivity -> MainActivity
                    |              |
                    v              v
              SignupActivity   UserRequestsActivity
                                  |
                                  v
                         AddEditBienActivity
                                  |
                                  v
                           DatabaseHelper
                                  |
                                  v
                              SQLite

Autres applications -> ContentProvider lecture seule -> SQLite
Systeme Android -> BroadcastReceivers -> SQLite / Toast
```

## 6. Composants Android utilises

- `SQLiteOpenHelper`: creation et gestion de la base locale `hico_assets.db`.
- `RecyclerView`: affichage performant de la liste des biens.
- `CardView`: affichage moderne de chaque bien sous forme de carte.
- `ContentProvider`: exposition des biens en lecture seule avec l'URI `content://com.hico.assetsprovider/biens`.
- `BroadcastReceiver`: reaction au demarrage du telephone, au changement manuel de l'heure et aux demandes externes de bien.
- `Intent`: navigation entre les activites et communication broadcast.
- `Material Design`: boutons, toolbar et FloatingActionButton.
- Table `users`: gestion simple des comptes approuves, en attente ou refuses.

## 7. Base de donnees

Table `biens`:

```sql
CREATE TABLE biens (
id INTEGER PRIMARY KEY AUTOINCREMENT,
nom TEXT,
type TEXT,
description TEXT,
valeur REAL,
date_creation TEXT,
date_modification TEXT,
date_consultation TEXT,
date_suppression TEXT
);
```

La suppression est une suppression logique: la date de suppression est remplie, puis le bien n'est plus affiche dans la liste principale.

## 8. Etapes d'execution

1. Ouvrir Android Studio.
2. Choisir `Open`.
3. Selectionner le dossier `HiCoAssetsManager`.
4. Attendre la synchronisation Gradle.
5. Lancer l'application sur un emulateur ou un telephone Android.
6. Se connecter avec un des comptes de test.

## 9. Captures d'ecran a ajouter

Apres execution, prendre les captures suivantes:

- Splash screen.
- Page de connexion.
- Liste des biens.
- Formulaire d'ajout ou modification.
- Confirmation de suppression.

## 10. Conclusion

Cette application respecte les exigences principales du mini-projet: authentification par role, gestion CRUD des biens, stockage SQLite, affichage RecyclerView/CardView, ContentProvider en lecture seule, BroadcastReceivers et interface moderne inspiree des abeilles.
