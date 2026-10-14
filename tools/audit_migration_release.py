"""Check the IC2 release against its original sprites and protected game files."""
from pathlib import Path
import hashlib
import json
import re
import zipfile
import argparse

import migrate_assets_2612
from project_artifacts import artifact_path

PROJECT = Path(__file__).resolve().parents[1]
GAME = PROJECT.parents[1]
PROOF = PROJECT.parent / "verification/ic2-20261012"
ORIGINAL = PROJECT / "original/industrialcraft-2-2.9.40+ex119-1.19.2-forge.jar"
PROPERTIES = dict(re.findall(r"^([^#=\n]+)=(.*)$", (PROJECT / "gradle.properties").read_text(), re.M))
RELEASE = artifact_path()
ORIGINAL_HASH = "5d06ca9146041043132a72bad9b5612e260e77a84a2f3773a3da9b0167ea1d3d"


def sha256(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def save(name, report):
    (PROOF / name).write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", "utf-8")


def main():
    global PROOF
    parser = argparse.ArgumentParser()
    parser.add_argument('--proof', type=Path, default=PROOF)
    parser.add_argument('--baseline', type=Path, default=PROOF / 'protected-game-hashes-before.json')
    args = parser.parse_args()
    PROOF = args.proof.resolve()
    PROOF.mkdir(parents=True, exist_ok=True)
    original_hash = sha256(ORIGINAL)
    assert original_hash == ORIGINAL_HASH, "The original IC2 input changed"
    assert not migrate_assets_2612.audit(), "A model or sprite reference is missing"
    with zipfile.ZipFile(RELEASE) as release, zipfile.ZipFile(ORIGINAL) as original:
        names = release.namelist()
        structural = {
            "jar": str(RELEASE), "sha256": sha256(RELEASE),
            "verification_classes_in_release": [n for n in names if n.startswith(("ic2/verification/", "ic2ce/verification/"))],
            "legacy_sprite_paths": [n for n in names if n.startswith(("assets/ic2/textures/blocks/", "assets/ic2/textures/items/"))],
            "empty_loader_class_present": any("Ic2ModelLoaders" in n for n in names),
            "game_classes_in_release": [n for n in names if n.startswith(("net/minecraft/", "org/lwjgl/"))],
            "legacy_model_loaders": [],
        }
        for name in names:
            if name.startswith("assets/ic2/models/") and name.endswith(".json"):
                loader = json.loads(release.read(name)).get("loader")
                if loader in ("ic2:be", "ic2:cable"):
                    structural["legacy_model_loaders"].append(name)
        save("release-structure-audit.json", structural)
        assert not json.loads(release.read('data/forge/tags/fluid/steam.json')).get('values'), 'Verification-only integration target leaked into release'
        assert all(not structural[k] for k in (
            "verification_classes_in_release", "legacy_sprite_paths", "empty_loader_class_present",
            "game_classes_in_release", "legacy_model_loaders")), "Release contains legacy or test content"
        sprites = {"checked": 0, "changed": []}
        for old in original.namelist():
            if old.startswith("assets/ic2/textures/") and old.endswith((".png", ".mcmeta")):
                sprites["checked"] += 1
                new = old.replace("/textures/blocks/", "/textures/block/").replace("/textures/items/", "/textures/item/")
                if new not in names or release.read(new) != original.read(old):
                    sprites["changed"].append({"original": old, "migrated": new})
        save("sprite-byte-audit.json", sprites)
        assert sprites["checked"] == 1537 and not sprites["changed"], "Original sprites were not preserved"
    before = json.loads(args.baseline.read_text("utf-8-sig"))
    jna_jar = GAME / "libraries/net/java/dev/jna/jna/5.17.0/jna-5.17.0.jar"
    with zipfile.ZipFile(jna_jar) as jna:
        jna_native_hash = hashlib.sha256(jna.read("com/sun/jna/win32-x86-64/jnidispatch.dll")).hexdigest()
    empty_hash = hashlib.sha256(b"").hexdigest()
    game = {"initial_snapshot_files": len(before), "checked": 0, "changed": [], "missing": [],
            "jna_runtime_temporary_files": []}
    for name, expected in before.items():
        path = Path(name)
        # JNA extracts its own DLL to a random name and creates an empty .x
        # cleanup marker. Native.removeTemporaryFiles/deleteOnExit remove these
        # between launches. Identify only byte-confirmed JNA files, preserving
        # the original snapshot and every fixed game/native library check.
        if path.parent == GAME / "natives" and re.fullmatch(r"jna[0-9]+\.dll(?:\.x)?", path.name):
            is_native = path.suffix == ".dll" and expected == jna_native_hash
            is_marker = path.suffix == ".x" and expected == empty_hash and before.get(name[:-2]) == jna_native_hash
            if is_native or is_marker:
                game["jna_runtime_temporary_files"].append({"path": name, "sha256": expected, "present": path.exists()})
                continue
        game["checked"] += 1
        if not path.is_file():
            game["missing"].append(name)
        elif sha256(path) != expected:
            game["changed"].append(name)
    save("protected-game-audit.json", game)
    assert not game["changed"] and not game["missing"], "Protected Minecraft files changed"
    summary = {
        "original_ic2_sha256": original_hash,
        "release_sha256": structural["sha256"],
        "original_sprites_preserved": sprites["checked"],
        "protected_game_files_unchanged": game["checked"],
        "release_contains_no_verification_classes": True,
        "legacy_model_loaders_removed": True,
    }
    save("release-audit-summary.json", summary)
    print(json.dumps(summary, indent=2))


if __name__ == "__main__":
    main()
