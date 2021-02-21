# Restrições

Android mobile application with information of restrictions in Portugal.

It implements two widgets:
- risk of infection in a municipality;
- and daily status of a restriction
in a municipality.

## Data sources

- Risk of infection from services.arcgis.com (see `RiskClient.kt`)

- Restrictions from restricoes.pt/backend/v1_mini.json (see `RestrictionClient.kt`). Implemented [here](https://github.com/restricoes/restricoes.github.io/tree/main/backend)
