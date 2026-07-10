# PMX

Ein lokaler Offline-Passwortmanager, entwickelt mit **Java**, **JavaFX**, **SQLite** und **NitriteDB**.

> **Hinweis:** Dieses Repository ist ein Demo- und Bewerbungsprojekt. Es enthÃ¤lt keine produktiven Benutzerdaten, keine echten Zugangsdaten und keine sensiblen lokalen Datenbanken.

## Ãœberblick

Dieses Projekt wurde im Rahmen meiner Umschulung zum **Fachinformatiker fÃ¼r Anwendungsentwicklung** entwickelt.  
Ziel war die Umsetzung eines lokalen Passwort-Managers mit Fokus auf **Sicherheit**, **Offline-Betrieb** und **klarer BenutzerfÃ¼hrung**.

## Funktionen

- Benutzerregistrierung und Login
- Lokale, verschlÃ¼sselte Speicherung von Zugangsdaten
- Passwortgenerator
- Recovery-Funktion
- Ãœbersichtliche Tresoransicht fÃ¼r gespeicherte EintrÃ¤ge
- Offline-Nutzung ohne Cloud-Anbindung

## Technologien

- Java 17
- JavaFX
- Maven
- SQLite
- NitriteDB
- JUnit 5

## Sicherheitskonzept

- **AES-256-GCM** zur VerschlÃ¼sselung sensibler Daten
- **PBKDF2-HMAC-SHA-256** zur SchlÃ¼sselableitung
- Trennung von Benutzerverwaltung und Tresordaten
- Keine Speicherung produktiver Daten im Repository
- Keine Cloud-Dienste oder externe Synchronisierung

## Projektstruktur

```text
src/
â”œâ”€ main/
â”‚  â”œâ”€ java/
â”‚  â””â”€ resources/
â””â”€ test/
   â””â”€ java/
```
## Start des Projekts

### Voraussetzungen

- Java 17
- Maven
- IntelliJ IDEA oder eine andere Java-IDE

### Projekt lokal starten

1. Repository klonen:

   ```bash
   git clone https://github.com/n-somas/tozen-password-manager.git
   ```

2. In den Projektordner wechseln:

   ```bash
   cd tozen-password-manager
   ```

3. Das Projekt in IntelliJ IDEA Ã¶ffnen.

4. Maven-AbhÃ¤ngigkeiten laden.

5. Die Anwendung Ã¼ber die Main-Klasse in der IDE starten.

### Tests ausfÃ¼hren

```bash
mvn test
```
## Screenshots

### Login und Tresor

<p>
  <img src="docs/screenshots/login.png" alt="Login" width="360">
</p>

<p>
  <img src="docs/screenshots/vault.png" alt="Tresor" width="700">
</p>

### Einträge verwalten

<p>
  <img src="docs/screenshots/add-entry.png" alt="Eintrag hinzufügen" width="360">
  <img src="docs/screenshots/edit-entry.png" alt="Eintrag bearbeiten" width="360">
</p>

### Löschen-Dialog

<p>
  <img src="docs/screenshots/delete-dialog.png" alt="Löschen-Dialog" width="420">
</p>

## Autor

**Niloshan Somasundaram**  


