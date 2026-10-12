"""Migrate IC2's legacy sprite paths, and audit every reachable model dependency.

Run from any directory. --apply is idempotent; PNG/mcmeta bytes are preserved.
Original 1.19.2 files under original/ are deliberately never changed.
"""
from pathlib import Path
import argparse
import json
from collections import Counter

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/ic2"


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def migrate():
    for old, new in (("blocks", "block"), ("items", "item")):
        source = ASSETS / "textures" / old
        target = ASSETS / "textures" / new
        if source.exists():
            if target.exists():
                raise RuntimeError(f"Both legacy and migrated sprite directories exist: {source}")
            source.rename(target)
    changed = 0
    for base, suffix in ((ASSETS, ".json"), (ROOT / "src/main/java/ic2", ".java")):
        for path in base.rglob("*" + suffix):
            original = path.read_text("utf-8")
            updated = original
            for old, new in (("ic2:blocks/", "ic2:block/"), ("ic2:items/", "ic2:item/"),
                             ('"blocks/', '"block/'), ('"textures/blocks/', '"textures/block/'),
                             ('"textures/items/', '"textures/item/')):
                updated = updated.replace(old, new)
            if updated != original:
                path.write_text(updated, encoding="utf-8")
                changed += 1
    # The current blockstates already use vanilla facing/active and multipart
    # models. Convert the old loader aliases too, rather than keeping empty SPI
    # implementations in the resource graph.
    aliases = 0
    thickness = {"tin": 4, "copper": 4, "gold": 3, "iron": 6, "glass": 4, "detector": 8, "splitter": 8}
    colored = {"tin": 1, "copper": 1, "gold": 1, "iron": 1, "glass": 0, "detector": 100, "splitter": 100}
    for path in (ASSETS / "models").rglob("*.json"):
        data = json.loads(path.read_text("utf-8"))
        if data.get("loader") == "ic2:be":
            write(path, {"parent": data["id"].replace("block/be/", "block/")})
            aliases += 1
        elif data.get("loader") == "ic2:cable":
            foam = data["foam"]
            if foam != "none":
                sprite = "foam" if foam == "soft" else "wall_" + foam.removeprefix("hard_")
                converted = {"parent": "minecraft:block/cube_all", "textures": {"all": "ic2:block/cf/" + sprite}}
            else:
                cable, insulation = data["type"], data["insulation"]
                sprite = f"{cable}_cable_{insulation}"
                if insulation >= colored[cable]:
                    sprite += "_black"
                if data.get("active") and cable in ("detector", "splitter"):
                    sprite += "_active"
                converted = {"parent": f"ic2:block/cable/shape/core_{thickness[cable] + insulation * 2}",
                             "textures": {"texture": "ic2:block/wiring/cable/" + sprite}}
            write(path, converted)
            aliases += 1
    write(ASSETS / "models/block/cf/obscured_wall.json", {"parent": "ic2:block/cf/wall_white"})
    # This file is a parent template, not an item. A standalone item definition
    # made the generated model try to resolve its intentionally absent layer0.
    template = ASSETS / "items/default.json"
    if template.exists():
        template.unlink()
    # Migrate the original charge predicates into the current item model API.
    ranges = 0
    for path in (ASSETS / "models/item").glob("*.json"):
        data = json.loads(path.read_text("utf-8"))
        charge = [entry for entry in data.get("overrides", []) if "ic2:charge" in entry.get("predicate", {})]
        if charge:
            fallback = {"type": "minecraft:model", "model": "ic2:item/" + path.stem}
            write(ASSETS / "items" / path.name, {"model": {
                "type": "minecraft:range_dispatch", "property": "ic2:charge", "fallback": fallback,
                "entries": [{"threshold": e["predicate"]["ic2:charge"], "model": {
                    "type": "minecraft:model", "model": e["model"]}} for e in charge]}})
            ranges += 1
    print(json.dumps({"rewritten_files": changed, "legacy_model_aliases": aliases, "charge_models": ranges}))
    # Seven source fluids never had a 1.19.2 bucket sprite. Use NeoForge's
    # current fluid container renderer and its bundled mask for those items.
    for name in ("air", "hydrogen", "oxygen", "steam", "superheated_steam", "biogas", "distilled_water"):
        write(ASSETS / "items" / (name + "_bucket.json"), {"model": {
            "type": "neoforge:fluid_container", "fluid": "ic2:" + name,
            "textures": {"base": "minecraft:item/bucket", "fluid": "neoforge:item/mask/bucket_fluid",
                         "cover": "neoforge:item/mask/bucket_fluid_cover"}}})


def audit():
    seen = set()
    missing = set()
    roots = list((ASSETS / "blockstates").glob("*.json")) + list((ASSETS / "items").glob("*.json"))

    def model(name, source):
        if not name.startswith("ic2:") or name in seen:
            return
        seen.add(name)
        path = ASSETS / "models" / (name[4:] + ".json")
        if not path.is_file():
            missing.add((source, "model", name))
            return
        walk(json.loads(path.read_text("utf-8")), str(path.relative_to(ASSETS)))

    def walk(value, source):
        if isinstance(value, dict):
            for key, child in value.items():
                if key in ("parent", "model") and isinstance(child, str):
                    model(child, source)
                elif key == "textures":
                    for sprite in child.values():
                        if isinstance(sprite, str) and sprite.startswith("ic2:"):
                            if not (ASSETS / "textures" / (sprite[4:] + ".png")).is_file():
                                missing.add((source, "texture", sprite))
                walk(child, source)
        elif isinstance(value, list):
            for child in value:
                walk(child, source)

    for path in roots:
        walk(json.loads(path.read_text("utf-8")), str(path.relative_to(ASSETS)))
    report = {"root_definitions": len(roots), "reachable_ic2_models": len(seen), "missing": sorted(missing)}
    print(json.dumps(report, indent=2))
    return bool(missing)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    if args.apply:
        migrate()
    raise SystemExit(audit())
