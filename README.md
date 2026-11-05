# Tâche 3 : automatisation des tests de sécurité


## 1. Workflow GitHub Actions

### Ce qu'on a fait

Avec l'aide d'une IA (Claude), on a ajouté une tâche `semgrep` dans `.github/workflows/main-jdk17-build.yml`. 

### Problèmes rencontrés

À compléter si nécessaire
## 2. Rickroll

Avec l'aide d'une IA (Claude), on a créé l'action[.github/actions/rickroll/action.yml](.github/actions/rickroll/action.yml) . Elle écrit un message avec un lien vers la vidéo YouTube de vidéoclip dans le résumé du run et affiche une annotation d'erreur. Elle est appelée avec `if: failure()` (l'étape ne s'exécute que si une étape précédente a échoué) à la fin des tâches `build`, `e2e-tests` et `semgrep`.

Validation : lors d'un [run](https://github.com/mariaalitouche/tika/actions/runs/41768395046) où `build` a échoué, l'étape s'est exécutée et le log affiche `Never gonna give you up !` avec le lien. ![log](images3/Rickroll.png)

## 3. Vulnérabilités introduites

### Vulnérabilité 1 : désérialisation non sécurisée (en cours)

- **Fichier [StringUtils.java](tika-core/src/main/java/org/apache/tika/utils/StringUtils.java) :**  On l'a choisi, car Semgrep n'y trouvait aucune alerte avant.
- **Code ajouté :** une méthode `readSerializedObject(InputStream in)` qui retourne `new ObjectInputStream(in).readObject()`. Le code a été proposé par Claude, puis on l'a testé avec Semgrep avant de le push".
- **Pourquoi c'est vulnérable (CWE-502) :** quand Java reconstruit un objet à partir d'octets, il exécute du code des classes concernées pendant la lecture. Si les octets viennent d'une source non fiable, un attaquant peut les fabriquer pour faire exécuter du code. La méthode n'a aucun filtre sur les classes acceptées.  C'est une vulnérabilité qui ne fait rien de dangereux actuellement, car elle n'est appelée nulle part dans Tika.
- **Résultat Semgrep (scan local) :** règle `java.lang.security.audit.object-deserialization.object-deserialization`, ligne 238. Avant l'ajout : 0 alerte sur ce fichier.
- **Observation :** avec le nom complet `java.io.ObjectInputStream` écrit dans le code, Semgrep ne détectait rien. Avec un `import java.io.ObjectInputStream;`, la règle se déclenche. La première version de Claude ne marchait donc pas, et c'est le scan qui l'a montré.
- **Run :** À compléter.

Les 6 autres vulnérabilités : **À compléter**.

## 4. Comparaison des rapports Semgrep avant/après

À compléter