Data assets for the Dress-Up game.

Folder layout (under assets/data/):
- features/          (JSON files per category, e.g. eyes.json)
- backgrounds.json
- avatars.json
- cosplay_scenes.json
- bots.json

Images are expected under assets/images/ as referenced by the JSON files.

Usage examples:
- Load features: FeatureRepository(context).loadFeatures("eyes")
- Load cosplay scenes: CosplayRepository(context).loadCosplayScenes()

Note: These files are intentionally minimal examples to bootstrap the catalog. Replace or extend with real content as needed.
