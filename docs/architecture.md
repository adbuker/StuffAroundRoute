# Architecture

```
presentation/        — Compose UI
  MainActivity.kt          — main screen with coordinate input and POI generation
  SettingsScreen.kt         — API keys and source configuration
  MapPickerSheet.kt         — bottom-sheet map for picking coordinates
  TrackCorridorScreen.kt    — Track Corridor workflow (OsmAnd integration)
  theme/                    — Material 3 theme (Color, Theme, Type)

domain/
  models/                   — PointData, Coordinates
  repository/               — SettingsRepository interface

data/repository/     — implementations
  SettingsRepositoryImpl.kt          — SharedPreferences-backed settings
  AllAttractionsGenerator.kt         — orchestrates all POI sources
  GooglePlaceGpxGenerator.kt         — Google Places API
  OsmPlaceGpxGenerator.kt            — OpenStreetMap / Overpass
  TripAdvisorGpxGenerator.kt         — TripAdvisor Content API
  WikidataAttractionsGpxGenerator.kt — Wikidata SPARQL
  WikipediaArticlesGpxGenerator.kt   — Wikipedia geo-search
  INaturalistGpxGenerator.kt         — iNaturalist species
  NeedPhotoWikidataGpxGenerator.kt   — Wikidata objects without photos
  NeedPhotoSettings.kt               — exclusion categories for photo routes
  GpxGeneratorBase.kt                — shared GPX-writing logic
  IGpxGenerator.kt                   — generator interface
  NominatimService.kt                — reverse geocoding
  OsmAndConnection.kt                — AIDL binding to OsmAnd
  GpxTrackParser.kt                  — SAX-based GPX track parser
  CorridorCalculator.kt              — haversine distance, sub-segment extraction, bounding box
  TrackCacheRepository.kt            — track-name → file-URI cache
```
