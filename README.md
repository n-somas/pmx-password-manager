# PMX - Lokaler Passwortmanager

[![Java CI](https://github.com/n-somas/pmx-password-manager/actions/workflows/maven.yml/badge.svg)](https://github.com/n-somas/pmx-password-manager/actions/workflows/maven.yml)

**PMX** ist ein lokaler Offline Passwortmanager als JavaFX Desktopanwendung.  
Das Projekt verbindet Java, JavaFX, lokale Datenhaltung, Verschlüsselung, Benutzerführung und GitHub Dokumentation in einer vollständigen Desktopanwendung.

> **Hinweis:** Dieses Repository ist ein Demo- und Bewerbungsprojekt. Es enthält keine produktiven Benutzerdaten, keine echten Zugangsdaten und keine sensiblen lokalen Datenbanken.

## Projektfokus

- Lokale Desktop Anwendung ohne Cloud Synchronisierung
- Moderne JavaFX Oberfläche mit dunklem PMX Design
- Verschlüsselte Speicherung sensibler Zugangsdaten
- Benutzerregistrierung, Login und Recovery Funktion
- Tresoransicht mit Suche, Bearbeiten und Löschen
- Sicherheitsnahe Funktionen wie zeitlich begrenztes Anzeigen, automatisches Leeren der Zwischenablage, Passwort Alter Status und verschlüsselte Backups

## Screenshots

### Login

<p>
  <img src="docs/screenshots/login.png" alt="PMX Login" width="420">
</p>

### Tresor

<p>
  <img src="docs/screenshots/vault.png" alt="PMX Tresoransicht" width="760">
</p>

### Einträge verwalten

<p>
  <img src="docs/screenshots/add-entry.png" alt="Neuen Eintrag hinzufügen" width="420">
  <img src="docs/screenshots/edit-entry.png" alt="Eintrag bearbeiten" width="420">
</p>

### Löschen Dialog

<p>
  <img src="docs/screenshots/delete-dialog.png" alt="Eintrag löschen" width="460">
</p>

## Funktionen

- Benutzer anlegen und anmelden
- Zugangsdaten lokal speichern
- Einträge suchen, hinzufügen, bearbeiten und löschen
- Passwortgenerator für neue Zugangsdaten
- Recovery Funktion für das Zurücksetzen des Master Passworts
- Getrennte Speicherung von Benutzer und Tresordaten
- Änderungsdatum pro Eintrag
- Passwort Alter Status mit den Zuständen `Aktuell`, `Prüfen` und `Alt`
- Passwort zeitlich begrenzt anzeigen
- Passwort in die Zwischenablage kopieren
- Zwischenablage nach kurzer Zeit automatisch leeren
- Verschlüsselter Backup Export
- Verschlüsselter Backup Import
- Automatische Tresor Sperre nach Inaktivität
- Login Sperre nach mehreren Fehlversuchen
- Leerer Tresor Zustand mit Hinweis und Schnellzugriff auf neuen Eintrag

## Sicherheitskonzept

- **AES-256-GCM** zur Verschlüsselung sensibler Daten
- **PBKDF2-HMAC-SHA-256** zur Schlüsselableitung
- Lokale Speicherung ohne externe Synchronisierung
- Keine Speicherung produktiver Daten im Repository
- Passwortanzeige wird nach kurzer Zeit automatisch wieder maskiert
- Kopierte Passwörter werden nach 20 Sekunden aus der Zwischenablage entfernt, sofern dort noch genau dieses Passwort enthalten ist
- Passwort Alter Status unterstützt den Nutzer dabei, alte Zugangsdaten zu erkennen
- Backup Dateien werden nicht als Klartext exportiert, sondern verschlüsselt gespeichert
- Automatische Sperre schließt den Tresor nach Inaktivität und entfernt den aktiven Sitzungsschlüssel
- Nach mehreren falschen Login Versuchen wird die Anmeldung kurzzeitig gesperrt

## Technische Details

### Eintragsmetadaten

Jeder Tresoreintrag besitzt Metadaten:

```text
createdAt
updatedAt
passwordChangedAt
```

Damit kann PMX unterscheiden, ob ein Eintrag allgemein bearbeitet wurde oder ob tatsächlich das Passwort geändert wurde.

### Passwort Alter Status

Der Status wird aus `passwordChangedAt` berechnet:

```text
Aktuell  = Passwort wurde kürzlich geändert
Prüfen   = Passwort sollte geprüft werden
Alt      = Passwort ist länger nicht geändert worden
```

Diese Funktion ist bewusst einfach gehalten, aber fachlich sinnvoll für einen Passwortmanager.

### Zeitlich begrenzte Passwortanzeige

Beim Klick auf Anzeigen wird das Passwort entschlüsselt und nur kurz sichtbar gemacht.  
Nach Ablauf des Timers wird die Tabellenanzeige wieder maskiert.

### Sicheres Kopieren

Beim Kopieren wird das Passwort in die Systemzwischenablage gelegt.  
Nach 20 Sekunden prüft PMX, ob die Zwischenablage noch genau dieses Passwort enthält. Nur dann wird sie geleert. Dadurch wird nicht versehentlich ein anderer kopierter Inhalt des Nutzers gelöscht.

### Verschlüsselte Backups

PMX kann Tresoreinträge als verschlüsselte Backup Datei exportieren und wieder importieren.  
Die Backup Datei besitzt ein eigenes PMX Format und speichert die Daten nicht als lesbaren Klartext.

```text
PMX-BACKUP-1
<verschlüsselte Backup Daten>
```

Beim Import wird die Datei entschlüsselt, geprüft und anschließend werden neue Einträge ergänzt oder bestehende Einträge aktualisiert.

### Login Schutz

PMX zählt fehlgeschlagene Login Versuche während der laufenden Sitzung.  
Nach mehreren falschen Eingaben wird der Login für kurze Zeit gesperrt und der Login Button deaktiviert. Dadurch wird eine einfache Schutzlogik gegen wiederholte Fehlversuche umgesetzt.

## Architektur

PMX ist bewusst in mehrere Verantwortungsbereiche aufgeteilt:

```text
JavaFX UI und FXML
   ↓
Controller
   ↓
Service Layer
   ↓
EncryptionUtil / BackupService / DatabaseHelper
   ↓
Lokale Datenbank und verschlüsselte Tresordaten
```

Die FXML Dateien beschreiben die Oberfläche. Die Controller reagieren auf Benutzeraktionen und steuern die Fenster, Dialoge und Tabellenlogik. Sicherheitsrelevante Funktionen wie Verschlüsselung und Backup Verarbeitung sind in eigene Hilfsklassen und Services ausgelagert. Dadurch bleibt die Oberfläche von der eigentlichen Fachlogik getrennt.

## Technologien

- Java 17
- JavaFX
- FXML
- CSS
- Maven
- SQLite
- NitriteDB
- Gson
- JUnit 5
- Git und GitHub

## Projekt lokal starten

### Voraussetzungen

- Java 17 oder neuer
- Maven
- IntelliJ IDEA oder eine andere Java IDE

### Start

Repository klonen:

```bash
git clone https://github.com/n-somas/pmx-password-manager.git
```

In den Projektordner wechseln:

```bash
cd pmx-password-manager
```

Tests ausführen:

```bash
mvn clean test
```

Anwendung starten:

```bash
mvn javafx:run
```

## Projektstatus

PMX ist ein Bewerbungs- und Lernprojekt mit Fokus auf Java, JavaFX, lokaler Datenhaltung, Verschlüsselung, UI Entwicklung und sauberer Dokumentation.

Es ist kein produktiv auditierter Passwortmanager. Für einen produktiven Einsatz wären weitere Security Reviews, Tests, Schutzmaßnahmen und Audits notwendig.

## Autor

**Niloshan Somasundaram**
