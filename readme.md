Justification des classes choisies :



L'emplacement des tests générés se trouvent dans le module tika-core.

Chemin : tika-core/chatunitest-tests/org/apache/tika/Tika_detect_1_0_Test.java

Est-ce que les tests compilaient et s'exécutaient immédiatement? Non.
Étant donné que le projet Apache suit une structure Maven standard et des règles de qualité strict (Checkstyle). Les tests qui sont générés dans un dossier qui n'est pas standard (chatunitest-tests) ne sont pas automatiquement reconnus et il faut modifier la commande Maven ou le projet pour qu'ils soient finalement reconnus.

Au total, il y eu 2 corrections faites.

1. Maven cherchait les tests dans src/test/java et ignoraient chatunitest-tests.
Correction : déplacement du fichier dans src/test/java.

2.Le plug-in maven-checkstyle-plugin bloquait le bluid en raison du non-respect des règles du projet.
Correction : ajout du paramètre -Dcheckstyle.skip=true pour passer outre la vérification statique.

Explication des tests et critique :

Comparaison des tests générés par IA et tests écrits :

