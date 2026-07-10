# PMX - Lokaler Passwortmanager

**PMX** ist ein lokaler Offline-Passwortmanager als JavaFX-Desktopanwendung.  
Das Projekt zeigt eine vollständige Benutzeroberfläche mit Login, Tresoransicht, Eintragsverwaltung, Passwortgenerator und lokaler Verschlüsselung.

> **Hinweis:** Dieses Repository ist ein Demo- und Bewerbungsprojekt. Es enthält keine produktiven Benutzerdaten, keine echten Zugangsdaten und keine sensiblen lokalen Datenbanken.

## Projektfokus

- Lokale Desktop-Anwendung ohne Cloud-Synchronisierung
- Moderne JavaFX-Oberfläche mit dunklem PMX-Design
- Verschlüsselte Speicherung sensibler Zugangsdaten
- Benutzerregistrierung, Login und Recovery-Funktion
- Übersichtliche Tresoransicht mit Suche, Bearbeiten- und Löschfunktion
- Sicherheitsbewusster Aufbau mit Master Passwort, Schlüsselableitung und AES-GCM-Verschlüsselung

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
- Recovery-Funktion für das Zurücksetzen des Master Passworts
- Getrennte Speicherung von Benutzer- und Tresordaten

## Technologien

- Java 17
- JavaFX
- Maven
- SQLite
- NitriteDB
- JUnit 5

## Sicherheitskonzept

- **AES-256-GCM** zur Verschlüsselung sensibler Daten
- **PBKDF2-HMAC-SHA-256** zur Schlüsselableitung
- Trennung von Benutzerverwaltung und Tresordaten
- Keine produktiven Daten im Repository
- Lokaler Betrieb ohne externe Synchronisierung

## Projekt lokal starten

### Voraussetzungen

- Java 17 oder neuer
- Maven
- IntelliJ IDEA oder eine andere Java-IDE

### Start

Repository klonen:

```bash
git clone https://github.com/n-somas/pmx-password-manager.git
```

In den Projektordner wechseln:

```bash
cd pmx-password-manager
```

Abhängigkeiten laden und Tests ausführen:

```bash
mvn clean test
```

Anwendung starten:

```bash
mvn javafx:run
```

## Projektstatus

PMX ist ein Bewerbungs- und Lernprojekt mit Fokus auf Java, JavaFX, lokaler Datenhaltung, Verschlüsselung und sauberer Benutzerführung.

## Autor

**Niloshan Somasundaram**
