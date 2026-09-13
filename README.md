## Justification des classes choisies

On a choisi de travailler avec le module `tika-core`, et plus précisément avec la classe [Tika](tika-core/src/main/java/org/apache/tika).

**Pourquoi cette classe a déjà des tests, mais pas assez :**

Il existe déjà un fichier de test [TikaDetectionTest](tika-core/src/test/java/org/apache/tika/TikaDetectionTest.java) pour cette classe. Ce fichier contient une seule méthode de test, mais elle fait des centaines de vérifications d'un coup : elle vérifie que `detect(String)` reconnaît correctement le bon type de fichier pour presque toutes les extensions possibles (`.pdf`, `.docx`, `.mp3`, etc.) en se basant sur le nom du fichier.

On a utilisé l'outil **JaCoCo**, qui montre combien de lignes de code sont vraiment testées. Résultat : seulement **12% du code de la classe `Tika`** est couvert.

On a aussi utilisé **pitest**, un outil qui crée des petites erreurs volontaires (des "mutants") dans le code pour voir si les tests remarquent que quelque chose ne va pas. Sur **43 mutants créés**, seulement **5 ont été détectés (12%)**. Les **38 autres** n'ont pas été touchés par le test existant.

Ces résultats confirment que `Tika` correspond bien au critère demandé : une classe **qui est déjà testée, mais qui n'est pas couverte à 100%**.

**Pourquoi on a choisi précisément les méthodes `detect(InputStream, Metadata)` et `detect(String)` :**

Ce sont les deux seules méthodes qui satisfont les contraintes du travail.
## Emplacement des tests générés

Les tests se trouvent dans le module [tika-core](tika-core).

Les tests générés par l'IA sont dans le fichier [Tika_detect_1_0_Test.java](tika-core/src/test/java/org/apache/tika/Tika_detect_1_0_Test.java)

## Est-ce que les tests compilaient et s'exécutaient immédiatement ?

**Non.**

Étant donné que le projet Apache suit une structure Maven standard et des règles de qualité strictes (Checkstyle), les tests générés dans un dossier qui n'est pas standard (`chatunitest-tests`) ne sont pas automatiquement reconnus. Il a fallu modifier la commande Maven ou le projet pour qu'ils soient finalement reconnus.

Au total, **2 corrections** ont été nécessaires :

1. **Maven cherchait les tests dans `src/test/java/tika`** et ignorait `chatunitest-tests`.
  
   **Correction :** déplacement du fichier dans [tika-core/src/test/java/org/apache/tika](tika-core/src/test/java/org/apache/tika).

2. **Le plugin `maven-checkstyle-plugin` bloquait le build** en raison du non-respect des règles du projet.
   **Correction :** ajout du paramètre `-Dcheckstyle.skip=true` pour passer outre la vérification statique.

## Explication des tests générés et critique

### Méthode ciblée : Tika.detect(InputStream, Metadata)

### Un problème trouvé avec le test généré

Le test généré par l'IA utilisait le texte `"Hello, World!"` pour simuler un fichier texte. Au début, ce test fonctionnait bien tout seul.

Mais quand on a essayé de faire rouler tous les tests du projet ensemble (`mvn clean install -Dcheckstyle.skip=true`), ce test-là a échoué. Le message d'erreur disait :
`expected: <text/plain> but was: <hello/x-world-hello>`

En cherchant pourquoi, on a trouvé la cause dans un fichier du projet ([custom-mimetypes.xml](tika-core/src/test/resources/custom-mimetypes.xml)). Ce fichier contient une règle spéciale, écrite par les développeurs de Tika, qui dit : "si un fichier commence exactement par le texte `Hello, World!`, considère-le comme un type de fichier spécial appelé `hello/x-world-hello`".

Le problème, c'est que le texte `"Hello, World!"` est aussi le texte le plus utilisé au monde comme exemple en programmation. L'IA a choisi ce texte parce que c'est un choix très commun et logique, sans savoir que, dans ce projet précis, ce texte a un sens spécial et déclenche une règle cachée.

**Ce que ça montre :** l'IA ne connaît pas les détails internes ou les "pièges" spécifiques d'un projet. Elle choisit des exemples génériques, alors qu'une personne qui travaille depuis longtemps sur le projet aurait pu éviter ce piège en connaissant déjà cette règle spéciale.

**Correction :** on a changé le texte du test pour `"This is a plain text document for testing purposes."`, qui ne correspond à aucune règle spéciale du projet.

Mais après ce changement, un autre problème est apparu. Le projet Tika a une règle qui dit qu'on ne peut pas utiliser une certaine commande en Java, appelée `.getBytes()` sans dire clairement comment le texte doit être écrit en mémoire.

Le test généré par l'IA ne respectait pas cette règle non plus. Encore une fois, l'IA ne pouvait pas savoir que ce projet précis avait cette exigence particulière, puisque ce n'est pas une règle universelle en programmation, mais une règle propre à ce projet.

**Deuxième correction :** `.getBytes());` a été remplacé par `.getBytes(java.nio.charset.StandardCharsets.UTF_8));`. Une fois ce changement fait, le test respecte toutes les règles du projet et fonctionne correctement.

**Ce que le test vérifie comme résultat (l'oracle) :** la ligne `assertEquals("text/plain", detectedType)` vérifie que la réponse donnée par Tika est bien `"text/plain"`.

Dans ce cas, l'oracle est correct : le texte donné en exemple au test est un texte normal, sans mise en forme spéciale, donc c'est bien normal que Tika le reconnaisse comme `"text/plain"`.

**Ce qu'on pense de la qualité de ce test :**

Point positif : pour créer l'objet `Tika`, le test lui donne directement les outils dont il a besoin. C'est une bonne façon de faire, plus simple que d'autres méthodes plus compliquées.

Point à noter : le test utilise le vrai détecteur de Tika, pas une version simplifiée juste pour le test. Ça veut dire qu'il vérifie que plusieurs parties du code fonctionnent bien ensemble.

Point faible : le test ne vérifie qu'un seul cas simple, un texte normal. Il ne teste pas d'autres situations qui pourraient arriver, comme un fichier vide, un fichier qui vaut `null` (rien du tout), etc.

### Sur les 9 méthodes surchargées `detect(...)` de la classe `Tika`

ChatUniTest a essayé de créer un test pour chacune des 9 versions différentes de la méthode `detect(...)`.

Sur ces 9 tentatives, **seulement 1 a réussi** à compiler et bien fonctionner (celle présentée plus haut). Les 8 autres ont échoué :
- la plupart parce que le code généré ne compilait pas, même après que l'IA ait essayé de se corriger elle-même ;
- une fois parce que l'IA n'a même pas réussi à produire un bout de code utilisable.

Ce faible taux de réussite (1 sur 9, donc environ 11%) montre une limite du modèle utilisé localement : quand une classe a plusieurs méthodes qui portent le même nom, l'IA semble avoir plus de difficulté à savoir laquelle elle est en train de tester, ce qui cause plus d'erreurs.

**Un défi important : les ressources de l'ordinateur**

Générer des tests pour ces 9 méthodes a demandé beaucoup de puissance de l'ordinateur. Pendant que ça tournait, l'ordinateur devenait très lent et difficile à utiliser pour autre chose. À un moment, il a même complètement planté et a dû être redémarré.

Pour réussir à terminer la génération sans que l'ordinateur plante à nouveau, on a dû réduire le nombre de "tours de correction" que l'IA pouvait faire (on a baissé cette limite de 5 à 2 tentatives par méthode). Ça a permis de finir le processus, mais ça a aussi probablement réduit encore plus le taux de réussite, puisque l'IA avait moins de chances de corriger ses erreurs avant d'abandonner.

## Analyse de mutation

On a comparé le score de mutation avant et après l'ajout du test généré par l'IA.

| Métrique | Avant | Après |
|---|---|---|
| Couverture de lignes (classe `Tika`) | 15/128 (12%) | 18/128 (14%) |
| Mutants générés | 43 | 43 |
| Mutants tués | 5 (12%) | 6 (14%) |
| Mutants sans couverture | 38 | 37 |

### Ce que ça montre

Avec le nouveau test ajouté par l'IA, on a réussi à couvrir un peu plus de code : la couverture est passée de 12% à 14%, et un mutant de plus a été détecté (le score de mutation est passé de 12% à 14% aussi).

C'est une petite amélioration. Ça montre que le test généré aide un peu, mais il reste encore du code non testé dans la classe `Tika` (37 mutants sur 43 ne sont toujours pas détectés).
### Comparaison JaCoCo (couverture de code) avant et après

En plus de pitest, on a aussi regardé les rapports JaCoCo avant et après l'ajout du test généré par l'IA, pour voir l'effet précis sur la méthode ciblée.

| Méthode | Couverture avant | Couverture après |
|---|---|---|
| `detect(InputStream, Metadata)` | 44% | 100% |
| `detect(String)` | 46% | 46% (inchangé) |
| Toute la classe `Tika` | 11% | 14% |

Le résultat le plus clair est sur `detect(InputStream, Metadata)` : la couverture est passée de 44% à 100%. Le nouveau test couvre maintenant complètement cette méthode.

La méthode `detect(String)` reste pareille, à 46%, car le test généré par l'IA ne touchait pas à cette méthode, seulement à `detect(InputStream, Metadata)`.

### Mutants détectés grâce au nouveau test

La méthode `detect(InputStream, Metadata)` fonctionne de deux façons différentes selon ce qu'on lui donne :

1. Si on ne donne pas de fichier (juste `null`), elle devine le type seulement à partir du nom.
2. Si on donne un vrai fichier, elle lit son contenu pour deviner le type.

Le test qui existait déjà avant utilisait toujours la première façon (sans vrai fichier). La deuxième façon n'était donc jamais testée.

Le nouveau test généré par l'IA donne un vrai texte comme fichier. Ça permet de tester la deuxième façon pour la première fois.

C'est pour ça qu'un mutant de plus est maintenant détecté : avant, si quelqu'un cassait accidentellement la partie du code qui lit un vrai fichier, aucun test ne l'aurait remarqué. Maintenant, ce serait remarqué.

## Tests supplémentaires écrits à la main

*(à compléter)*

## Comparaison des tests générés par IA et tests écrits à la main

*(à compléter)*
