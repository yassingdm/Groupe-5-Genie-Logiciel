![CI](https://github.com/yassingdm/Groupe-5-Genie-Logiciel/actions/workflows/ci.yml/badge.svg)
[![SonarQube Cloud](https://sonarcloud.io/images/project_badges/sonarcloud-light.svg)](https://sonarcloud.io/summary/new_code?id=yassingdm_Groupe-5-Genie-Logiciel)

# Groupe-5-Genie-Logiciel

# Team
- Yassin Gdaiem
- Alban Branger
- Bastien Adiveze
- Salim Kaimoussi
- Abdelkhalek Beghdadi

# Vue Console

## Prerequis

- Java 21
- Maven 3.9+

## Installation et execution

Pour compiler le projet:

```bash
cd grp5
mvn clean compile
```

Pour lancer les tests:

```bash
cd grp5
mvn test
```

Pour lancer le projet en mode console:

```bash
cd grp5
java -cp target/classes eidd.grp5.app.App
```

## Formats attendus

- Date/heure: `yyyy-MM-dd HH:mm` (ex: `2026-04-09 10:00`)
- Date/heure ISO acceptee aussi: `yyyy-MM-ddTHH:mm`
- Date (planning journalier): `yyyy-MM-dd`
- Role utilisateur: `CUSTOMER` ou `ADMIN` (vide = `CUSTOMER`)
- Nombre de participants: entier > 0 (vide = 1 a la creation, vide = conserver en modification)
- Motif: texte optionnel (vide = non renseigne)

## Fonctionnalites disponibles

- Gestion des utilisateurs
- Gestion des salles
- Creation, consultation, confirmation et annulation des reservations
- Modification d'une reservation par son proprietaire
- Modification d'une reservation par un admin
- Recherche des reservations par client, statut ou reference
- Liste des reservations a venir pour un client
- Vue admin des reservations par salle
- Verification de disponibilite et detection des conflits
- Planning journalier d'une salle
- Liste des salles disponibles maintenant ou sur un creneau
- Gestion des equipements par salle (ajout/retrait/recherche par equipement via le service)

## Couverture des attendus soutenance (Sujet 2)

Obligatoires:
- Catalogue de salles (nom, capacite): OK
- Creer et annuler une reservation (salle, date, creneau): OK
- Empêcher les conflits de reservation: OK
- Lister les reservations d'une salle ou d'une journee: OK

Bonus:
- Rechercher les salles disponibles sur un creneau: OK
- Modifier une reservation existante: OK
- Gestion des equipements: OK (niveau domaine/service)

## Guide rapide des menus

- Menu principal: `1` statistiques, `2-5` utilisateurs/salles, `6-7` creation et listing reservations, `8` gestion reservations, `9` disponibilite/conflits, `10` quitter.
- Sous-menu Gestion Reservations (`8`): recherche client/statut/reference, confirmation/annulation, modification client/admin, reservations a venir, vue admin par salle.
- Sous-menu Disponibilite et Conflits (`9`): disponibilite salle, conflits, taux d'occupation, planning journalier, salles disponibles maintenant ou sur creneau.

## Notes de comportement

- Une reservation en conflit est refusee a la creation et a la modification.
- Une reservation annulee n'est plus prise en compte dans les conflits/disponibilites.
- Le champ `Participants` aide a respecter la capacite de la salle.
- Le champ `Motif` sert au contexte (type de reunion, cours, soutenance, etc.).

## Couverture de tests

- Le projet est configure avec JaCoCo et un seuil minimal de 60% de couverture ligne.
- La suite actuelle depasse ce seuil.
