# Utilisation de l’application

# Compilation 
invoquez la commande `ant` a la racine du projet

# Utilisation
L’application compte 3 scripts:
 - nameService.sh
 - computationNode.sh
 - loadBalancer.sh

Ceux-ci permettent de lancer respectivement le répartiteur, le serveur de
calcul et le répertoire de noms. Les scripts sont pleinement documentés, un
invocation sans paramètre révèle les options requises.

Veuillez noter que les noeuds de calcul doivent être sur une machine différente
que le serveur de nom et le répartiteur pour fonctionner.

Notez également que le registre rmi (rmiregistry) est géré par l’application et
ne doit pas être lancé manuellement par l’utilisateur.

# Exemple d’utilisation
```
machine 1 (128.0.0.1)
$ ./nameService.sh -i 128.0.0.1 -p 5013

machine 2 (128.0.0.2)
$ ./computationNode.sh -i 128.0.0.2 -t 128.0.0.1 -p 5014 -tp 5013

*enregistrement de machine 2 auprès de machine 1*

machine 3 (128.0.0.3)
$ ./loadBalancer.sh -a 128.0.0.1 -p 5013 mon_fichier_de_calcul

*saisie de l’authentification et démarrage des calculs*
```
