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

### Vulnérabilité 1 : désérialisation non sécurisée

- **Fichier [StringUtils.java](tika-core/src/main/java/org/apache/tika/utils/StringUtils.java) :**  On l'a choisi, car Semgrep n'y trouvait aucune alerte avant.
- **Code ajouté :** une méthode `readSerializedObject(InputStream in)` qui retourne `new ObjectInputStream(in).readObject()`. Le code a été proposé par Claude, puis on l'a testé avec Semgrep avant de le push".
- **Pourquoi c'est vulnérable (CWE-502) :** quand Java reconstruit un objet à partir d'octets, il exécute du code des classes concernées pendant la lecture. Si les octets viennent d'une source non fiable, un attaquant peut les fabriquer pour faire exécuter du code. La méthode n'a aucun filtre sur les classes acceptées.  C'est une vulnérabilité qui ne fait rien de dangereux actuellement, car elle n'est appelée nulle part dans Tika.
- **Résultat Semgrep (scan local) :** règle `java.lang.security.audit.object-deserialization.object-deserialization`, ligne 238. Avant l'ajout : 0 alerte sur ce fichier.
- **Observation :** avec le nom complet `java.io.ObjectInputStream` écrit dans le code, Semgrep ne détectait rien. Avec un `import java.io.ObjectInputStream;`, la règle se déclenche. La première version de Claude ne marchait donc pas, et c'est le scan qui l'a montré.
- **Run :** `semgrep` rouge (étape de scan : `Findings: 1`) et le rickroll s'exécute, alors que `build` est vert : [run](https://github.com/mariaalitouche/tika/actions/runs/42053876379). Pour s'assurer que seul Semgrep fait échouer le build, on a d'abord retiré la vulnérabilité ([run vert](https://github.com/mariaalitouche/tika/actions/runs/42043865027)), puis on l'a remise dans un commit séparé.


### Vulnérabilité 2 : générateur de nombres aléatoires faible

- **Fichier [ParserUtils.java](tika-core/src/main/java/org/apache/tika/utils/ParserUtils.java) :** on l'a choisi, car Semgrep n'y trouvait aucune alerte avant.
- **Code ajouté :** une méthode `generateSessionToken()` qui retourne `Long.toHexString(new Random().nextLong())`, avec l'import `java.util.Random`. Elle produit un « jeton de session » sous forme de texte hexadécimal. Le code a été proposé par Claude, puis on l'a testé avec Semgrep avant de l'ajouter.
- **Pourquoi c'est vulnérable (CWE-330) :** `java.util.Random` suit une formule mathématique prévisible. Si un attaquant observe quelques jetons, il peut retrouver l'état interne du générateur et calculer les suivants. Un jeton de session doit être impossible à deviner. Il faudrait utiliser `java.security.SecureRandom`. Cette vulnérabilité ne fait rien de dangereux actuellement, car la méthode n'est appelée nulle part dans Tika.
- **Résultat Semgrep (scan local) :** règle `java.lang.security.audit.crypto.weak-random.weak-random`, ligne 113. Avant l'ajout : 0 alerte sur ce fichier.
- **Observation :** notre première version écrivait `Random random = new Random();` sur une ligne, puis `random.nextLong()` sur la suivante. Semgrep ne détectait rien (0 alerte). La règle cherche un appel directement sur `new Random()`. En écrivant `new Random().nextLong()` en une seule expression, la règle se déclenche. Comme pour la vulnérabilité 1, c'est le scan local qui a montré que la première version ne marchait pas.
  - **Run :** `semgrep` rouge (étape de scan : `Findings: 1`) et le rickroll s'exécute, alors que `build` est vert : [run](https://github.com/mariaalitouche/tika/actions/runs/42139769215).


### Vulnérabilité 3 : algorithme de hachage faible (MD5)

- **Fichier [CompareUtils.java](tika-core/src/main/java/org/apache/tika/utils/CompareUtils.java) :** on l'a choisi, car c'est une petite classe utilitaire de `tika-core` où Semgrep n'y trouvait aucune alerte avant.
- **Code ajouté :** une méthode `hashPassword(String password)` qui calcule l'empreinte d'un mot de passe avec `MessageDigest.getInstance("MD5")` et la retourne en texte hexadécimal. Le code a été proposé par Claude, puis on l'a testé avec Semgrep avant de l'ajouter'.
- **Pourquoi c'est vulnérable (CWE-327) :** MD5 est un algorithme de hachage. Il est très rapide à calculer, donc un attaquant peut tester des milliards de mots de passe par seconde. Il ne doit pas servir à protéger des mots de passe. Cette vulnérabilité ne fait rien de dangereux actuellement, car la méthode n'est appelée nulle part dans Tika.
- **Résultat Semgrep (scan local) :** règle `java.lang.security.audit.crypto.use-of-md5.use-of-md5`, ligne 61. Avant l'ajout : 0 alerte sur ce fichier. Semgrep propose aussi de remplacer MD5 par `SHA-512`.
- **Run :** `semgrep` rouge (étape de scan : `Findings: 1`) et le rickroll s'exécute, alors que `build` est vert : [run](https://github.com/mariaalitouche/tika/actions/runs/42152113962).


### Vulnérabilité 4 : injection SQL

- **Fichier [DateUtils.java](tika-core/src/main/java/org/apache/tika/utils/DateUtils.java) :** on l'a choisi, car c'est une classe utilitaire de `tika-core` où Semgrep n'y trouvait aucune alerte avant.
- **Code ajouté :** une méthode `findUser(Connection connection, String name)` qui cherche un utilisateur par son nom avec la requête `"SELECT * FROM users WHERE name = '" + name + "'"`. Le code a été proposé par Claude, puis on l'a testé avec Semgrep avant de l'ajouter'.
- **Pourquoi c'est vulnérable (CWE-89) :** le nom donné par l'utilisateur est collé directement dans le texte de la requête SQL. Un attaquant peut écrire du SQL à la place d'un nom. Par exemple, avec `' OR '1'='1`, la condition devient toujours vraie et la requête renvoie tous les utilisateurs. Cette vulnérabilité ne fait rien de dangereux actuellement, car la méthode n'est appelée nulle part dans Tika.
- **Résultat Semgrep (scan local) :** règle `java.lang.security.audit.formatted-sql-string.formatted-sql-string`, ligne 182. Avant l'ajout : 0 alerte sur ce fichier. Semgrep recommande d'utiliser `connection.prepareStatement`.
- **Run :** `semgrep` rouge (étape de scan : `Findings: 1`) et le rickroll s'exécute, alors que `build` est vert : [run](À compléter).

### Vulnérabilité 5 : XML External Entity

- **Fichier [SystemUtils.java](tika-core/src/main/java/org/apache/tika/utils/SystemUtils.java) :** on la choisit, car c'est une classe utilitaire `tika-core` où Semgrep ne trouvait aucune alerte avant.
- **Code ajouté :** une méthode `parseXml(InputStream in)` qui sert à lire un fichier XML et à transformer le contenu en un document (objet `Document`) que Java peut comprendre et utiliser. Le code a été proposé par Claude et a été testé avec Semgrep avant de l'ajouter.
- **Pourquoi c'est vulnérable (CWE-611) :** La méthode crée un analyseur XML sans désactiver les déclarations `DOCTYPE`. Un document XML malveillant pourrait donc utiliser des entités externes pour tenter d'accéder à des fichiers internes ou à des ressources réseau non autorisées. Cette vulnérabilité ne fait rien de dangereux actuellement, car la méthode ajoutée n'est pas appelée dans Tika.
- **Résultat Semgrep (scan local) :** règle `java.lang.security.audit.xxe.documentbuilderfactory-disallow-doctype-decl-missing`, ligne 80. Avant l'ajout : 0 alerte sur ce fichier. Après ajout : 1 alerte. Semgrep recommande de désactiver les déclarations `DOCTYPE` avec `dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)`.
- **Run :** ** `semgrep` rouge (étape de scan : `Findings: 1`) et le rickroll s'exécute, alors que `build` est vert : [run](https://github.com/mariaalitouche/tika/actions/runs/42260173656).

### Vulnérabilité 6 : validation de certificat désactivée
- **Fichier [RegexUtils.java](tika-core/src/main/java/org/apache/tika/utils/RegexUtils.java) :** on a choisi cette classe utilitaire de `tika-core`, car Semgrep n'y trouvait aucune alerte avant l'ajout du code.
- **Code ajouté :** une méthode `trustAll()` qui retourne un gestionnaire de certificats (`X509TrustManager`). Les méthodes `checkClientTrusted()` et `checkServerTrusted()` sont volontairement vides. Toutes deux ont été suggérées par Claude.
- **Pourquoi c'est vulnérable (CWE-295) :** Ces méthodes ne font aucune validation. Le programme accepte tous les certificats, même ceux qui ne sont pas fiables. Cela peut laisser place à une attaque de type *Man-in-the-Middle*, où un attaquant pourrait intercepter les communications entre le client et le serveur. Cette vulnérabilité ne présente aucun risque actuellement, car la méthode ajoutée n'est pas appelée dans Tika.
- **Résultat Semgrep (scan local) :** règle `java.lang.security.audit.crypto.ssl.insecure-trust-manager.insecure-trust-manager`. Avant l'ajout : 0 alerte sur ce fichier. Après l'ajout : 2 alertes aux lignes 71 et 75, correspondant aux deux méthodes de vérification. Semgrep recommande d'utiliser un `KeyStore` et un `TrustManagerFactory` pour vérifier les certificats.
- **Run :** `semgrep` rouge (étape de scan : `Findings: 2`) et le rickroll s'exécute, alors que `build` est vert : [run](https://github.com/mariaalitouche/tika/actions/runs/42260173656).  


## Vulnérabilité 7 — Connexion réseau non chiffrée  

- **Fichier [ExceptionUtils.java](tika-core/src/main/java/org/apache/tika/utils/ExceptionUtils.java) :** on a choisi cette classe utilitaire de `tika-core`, car Semgrep n'y trouvait aucune alerte avant l'ajout du code.
- **Code ajouté :** une méthode `openConnection(String host, int port)` qui permet d'établir une connexion réseau avec un serveur à partir de son adresse (`host`) et de son port (`port`). La connexion est créée avec `Socket` sans chiffrement. Cette méthode a été suggéré par Claude.
- **Pourquoi c'est vulnérable (CWE-319) :** la méthode crée une connexion TCP sans chiffrement. Les données transmises par cette connexion pourraient être interceptées et lues par une personne qui surveille le réseau. Cette vulnérabilité ne présente pas de risque actuellement, car la méthode ajoutée n'est pas appelée dans Tika.
- **Résultat Semgrep (scan local) :** règle `java.lang.security.audit.crypto.unencrypted-socket.unencrypted-socket`, ligne 95. Avant l'ajout : 0 alerte sur ce fichier. Après l'ajout : 1 alerte. Semgrep recommande d'utiliser un `SSLSocket` créé avec `SSLSocketFactory` ou `SSLServerSocketFactory` pour chiffrer la connexion.
- **Run :** `semgrep` rouge (étape de scan : `Findings: 1`) et le rickroll s'exécute, alors que `build` est vert : [run](https://github.com/mariaalitouche/tika/actions/runs/42260173656). A completer

## 4. Comparaison des rapports Semgrep avant/après

À compléter
