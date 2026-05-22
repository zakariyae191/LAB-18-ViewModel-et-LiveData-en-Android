

https://github.com/user-attachments/assets/e1df8b0d-86db-4791-98e5-d3567924d4eb

# Lab Android — ViewModel + LiveData : survivre à la rotation d’écran

Ce laboratoire Android en Java montre deux manières de gérer un compteur :

- une version classique avec une variable d'instance dans une `Activity`, fragile face aux recréations ;
- une version correcte avec `ViewModel` + `LiveData`, où l'état UI survit à une rotation et où l'interface est mise à jour automatiquement.

Le projet est prêt à ouvrir dans Android Studio.

## 1. Objectifs

- Comprendre pourquoi l'etat d'une `Activity` peut etre perdu lors d'une rotation.
- Comprendre le role de `onSaveInstanceState`.
- Utiliser un `ViewModel` pour conserver les donnees UI.
- Utiliser `LiveData` pour mettre a jour l'interface automatiquement.
- Comprendre `MutableLiveData` vs `LiveData`.
- Comprendre `setValue` vs `postValue`.
- Tester la rotation, le changement de theme, la mort du processus et une mise a jour depuis un thread background.

## 2. Theorie rapide mais complete

Quand on tourne l'ecran, Android considere qu'il s'agit d'un changement de configuration. Par defaut, il detruit l'`Activity` courante puis en cree une nouvelle avec les nouvelles ressources adaptees a l'orientation, a la taille d'ecran ou au theme.

Une variable d'instance comme `private int count = 0;` vit dans l'objet `Activity`. Si l'objet `Activity` est detruit, cette variable disparait. La nouvelle `Activity` repart donc avec ses valeurs initiales.

`onSaveInstanceState` permet de sauvegarder manuellement de petites donnees simples dans un `Bundle`. C'est utile pour un `int`, un `String`, un etat de formulaire ou une selection simple. En revanche, ce n'est pas ideal pour une logique plus complexe : objets riches, operations longues, threads, requetes reseau, acces Room, flux observes, etc.

`ViewModel` sert a conserver les donnees necessaires a l'UI pendant les changements de configuration. Il est stocke dans un `ViewModelStore`. Lors d'une rotation, l'ancienne `Activity` est detruite, une nouvelle est creee, mais elle recupere le meme `ViewModel` tant que le processus de l'application existe.

`LiveData` est un conteneur observable et lifecycle-aware. Une `Activity` ou un `Fragment` observe une donnee, et l'observer est appele quand la valeur change. Comme l'observation respecte le cycle de vie, l'UI ne recoit pas de mises a jour quand elle n'est pas active, et les observers sont nettoyes automatiquement.

Cette architecture suit l'esprit MVVM :

- l'`Activity` gere l'affichage et les clics utilisateur ;
- le `ViewModel` contient l'etat UI et la logique du compteur ;
- `LiveData` transporte les changements d'etat vers l'interface.

## 3. Installation

1. Ouvrir le dossier `ViewModelLiveDataDemoEnrichi` dans Android Studio.
2. Verifier que le projet utilise Java.
3. Verifier que le Minimum SDK est `24`.
4. Verifier les dependances Lifecycle dans `app/build.gradle.kts`.
5. Cliquer sur `Sync Now`.
6. Lancer l'application sur un emulateur ou un appareil Android.

Dependances demandees, adaptees ici au format Kotlin DSL du projet :

```kotlin
val lifecycle_version = "2.10.0"
implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version")
implementation("androidx.lifecycle:lifecycle-livedata:$lifecycle_version")
```

Equivalent Groovy si vous utilisez un `build.gradle` classique :

```groovy
def lifecycle_version = "2.10.0"
implementation "androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-livedata:$lifecycle_version"
```

## 5. Tests a faire

### Test 1 - Rotation

1. Lancer l'application.
2. Appuyer 15 fois sur `INCREMENTER`.
3. Tourner l'ecran.

Resultat attendu avec `MainActivity` : le compteur reste a `15`.

Demonstration theorique sans sauvegarde manuelle :

1. Lancer une version classique avec `private int count = 0`.
2. Incrementer 10 fois.
3. Tourner l'ecran.
4. Le compteur revient a `0`, car une nouvelle `Activity` est creee.

Avec `onSaveInstanceState`, le `int` peut etre restaure, mais cette solution reste surtout adaptee aux donnees simples.

### Test 2 - Theme sombre/clair

Changer le theme systeme sombre/clair. Selon l'appareil, Android peut recreer l'`Activity`. Le compteur reste conserve avec le `ViewModel`.

### Test 3 - Observer LiveData

Commenter temporairement l'appel :

```java
viewModel.getCount().observe(...)
```

Lancer l'application et cliquer sur les boutons. La valeur interne change dans le `ViewModel`, mais l'interface ne se met plus a jour automatiquement, car aucun observer ne connecte la donnee a l'UI.

### Test 4 - Thread background

Pour tester `postValue`, appeler temporairement `viewModel.incrementFromBackground()` depuis un clic de bouton, par exemple sur `INCREMENTER`. La mise a jour arrive depuis un thread de fond et `LiveData` la propage correctement au thread principal.

### Test 5 - Process death

Lancer l'application, incrementer le compteur, puis executer :

```bash
adb shell am kill com.example.viewmodellivedatademoenrichi
```

Relancer l'application. Le compteur revient a `0`.

Explication : un `ViewModel` survit aux changements de configuration, mais pas a une vraie mort du processus. Pour survivre a ce cas, utiliser `SavedStateHandle` pour un petit etat restaurable, ou une persistance locale comme Room/DataStore pour des donnees durables.

## 6. Tableau comparatif

| Critere | Version SANS ViewModel | Version AVEC ViewModel + LiveData |
| --- | --- | --- |
| Survie rotation | Non, sauf sauvegarde manuelle avec `onSaveInstanceState` | Oui, via le `ViewModelStore` |
| Mise a jour UI automatique | Non, il faut appeler `updateUI()` | Oui, via l'observation de `LiveData` |
| Gestion thread principal | Manuelle | `setValue` pour UI thread, `postValue` pour background |
| Code propre MVVM | Faible, logique dans l'`Activity` | Meilleur, logique dans le `ViewModel` |
| Support objets complexes | Peu adapte avec `Bundle` | Meilleur pour l'etat UI et la logique |
| Lifecycle-aware | Non | Oui, `LiveData` respecte le cycle de vie |
| Risque memory leak | Plus eleve si observers/callbacks mal geres | Reduit grace aux observers lifecycle-aware |

## 7. Bonus avances

### postValue depuis un thread background

`setValue` doit etre appele depuis le thread principal. Si une valeur arrive depuis un thread de fond, par exemple apres une operation longue, il faut utiliser `postValue`.

Dans ce lab :

```java
countLiveData.postValue(currentValue + 1);
```

### SavedStateHandle

`SavedStateHandle` combine `ViewModel` et sauvegarde/restauration d'un petit etat. Il est utile quand l'application doit restaurer une valeur apres une mort de processus, tant que cette valeur est serialisable et raisonnablement petite.

### Room et DataStore

Pour persister reellement des donnees :

- utiliser Room pour des donnees structurees relationnelles ;
- utiliser DataStore pour des preferences ou petits etats applicatifs ;
- utiliser un repository pour separer la source de donnees du `ViewModel`.

### ViewModel n'est pas une base de donnees

Un `ViewModel` garde un etat UI tant que le processus vit. Il ne remplace pas une base de donnees, un cache disque ou un systeme de persistance. Il sert a rendre l'UI robuste face aux recreations d'`Activity` et de `Fragment`.

## 8. Conclusion

`onSaveInstanceState` est utile pour sauvegarder de petites donnees simples, mais il devient limite pour une logique d'ecran plus complete.

`ViewModel` est la bonne pratique pour conserver l'etat UI pendant les changements de configuration.

`LiveData` met a jour l'interface automatiquement en respectant le cycle de vie.

Ensemble, `ViewModel` + `LiveData` permettent une architecture plus propre, plus testable et plus proche de MVVM.
