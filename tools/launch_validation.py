"""Launch the existing isolated IC2 client using the installed game unchanged."""
from pathlib import Path
import argparse
import hashlib
import json
import subprocess
import uuid
import zipfile
from project_artifacts import installed_ic2_jars

ROOT = Path(__file__).resolve().parents[3]

def allowed(entry):
    if "rules" not in entry:
        return True
    result = False
    for rule in entry["rules"]:
        if "features" in rule:
            continue
        if "os" in rule and rule["os"].get("name", "windows") != "windows":
            continue
        result = rule["action"] == "allow"
    return result


def arguments(items, replacements):
    result = []
    for item in items:
        if isinstance(item, dict):
            if not allowed(item):
                continue
            item = item["value"]
        for value in item if isinstance(item, list) else [item]:
            for key, replacement in replacements.items():
                value = value.replace("${" + key + "}", replacement)
            if "${" in value:
                raise ValueError(f"Unresolved argument: {value}")
            result.append(value)
    return result

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--verification", action="store_true")
    parser.add_argument("--gui-cycle", action="store_true")
    parser.add_argument("--fluid-tooltips", action="store_true")
    parser.add_argument("--all-machines", action="store_true")
    parser.add_argument("--inventory-probe-only", action="store_true")
    parser.add_argument("--all-items", action="store_true")
    parser.add_argument("--recipe-book-only", action="store_true")
    parser.add_argument("--processing-recipes", action="store_true")
    parser.add_argument("--processing-only", action="store_true")
    parser.add_argument("--workflow", action="store_true")
    parser.add_argument("--item-functions", action="store_true")
    parser.add_argument("--functions-subset", default="all")
    parser.add_argument("--review-shutdown", action="store_true")
    parser.add_argument("--lifecycle", action="store_true")
    parser.add_argument("--cables", action="store_true")
    parser.add_argument("--painter", action="store_true")
    parser.add_argument("--tools", action="store_true")
    parser.add_argument("--dedicated", action="store_true")
    parser.add_argument("--crop-audience", action="store_true")
    parser.add_argument("--crop-audience-server-report", type=Path)
    parser.add_argument("--server")
    parser.add_argument("--name", default="IC2Test")
    parser.add_argument("--world", default="New World")
    parser.add_argument("--game-directory", type=Path, default=ROOT / ".work/test-ic2-client-20261012")
    args = parser.parse_args()
    if args.all_machines and not args.verification:
        parser.error("--all-machines requires --verification")
    if args.dedicated and (not args.verification or not args.server):
        parser.error("--dedicated requires --verification and --server")
    game = args.game_directory.resolve()
    if args.verification and not game.is_relative_to(ROOT / ".work"):
        parser.error("Verification fixtures require an isolated game directory under .work")
    ic2_jars = installed_ic2_jars(game / "mods")
    if len(ic2_jars) != 1:
        parser.error(f"Expected exactly one IC2 jar, found: {[p.name for p in ic2_jars]}")
    with zipfile.ZipFile(ic2_jars[0]) as ic2:
        has_verification = any(name in ic2.namelist() for name in ("ic2ce/verification/MigrationVerification.class", "ic2/verification/MigrationVerification.class"))
    if has_verification != args.verification:
        parser.error("Install the verification artifact for --verification, or the regular artifact for normal play")
    print(f"IC2 validation artifact: {ic2_jars[0].name} sha256={hashlib.sha256(ic2_jars[0].read_bytes()).hexdigest()}", flush=True)
    metadata = json.loads((ROOT / "project02.json").read_text("utf-8"))
    classpath = [str(ROOT / "libraries" / entry["downloads"]["artifact"]["path"])
                 for entry in metadata["libraries"] if allowed(entry)] + [str(ROOT / "project02.jar")]
    replacements = {
        "auth_player_name": args.name, "auth_uuid": uuid.UUID(bytes=hashlib.md5(('OfflinePlayer:'+args.name).encode()).digest(), version=3).hex,
        "auth_access_token": "0", "auth_xuid": "", "clientid": "", "user_type": "legacy",
        "version_name": "project02", "version_type": "release", "game_directory": str(game),
        "assets_root": str(ROOT / "assets"), "assets_index_name": metadata["assetIndex"]["id"],
        "natives_directory": str(ROOT / "natives"), "library_directory": str(ROOT / "libraries"),
        "launcher_name": "IndustrialCraft2-CE-development", "launcher_version": "1", "classpath": ";".join(classpath)
    }
    java = ROOT / ".work/devtools/jdk-25/bin/javaw.exe"
    command = [str(java), "-Xms1G", "-Xmx6G"]
    if args.verification:
        command += ["-Dic2.verifyMigration=true", "-Dic2.comp.energy.debugload=true"]
    if args.fluid_tooltips:
        if not args.verification:
            parser.error("--fluid-tooltips requires --verification")
        command += ["-Dic2.verifyFluidTooltip=true", "-Dfml.earlyWindowControl=false"]
    if args.all_machines:
        command += ["-Dic2.verifyAllMachines=true"]
    if args.crop_audience:
        if not args.verification or not args.dedicated:
            parser.error("--crop-audience requires --verification --dedicated")
        command += ["-Dic2.verifyCropAudience=true"]
        if args.crop_audience_server_report:
            command += ["-Dic2.cropAudienceServerReport=" + str(args.crop_audience_server_report.resolve())]
    if args.inventory_probe_only:
        if not args.verification or not args.all_machines: parser.error("--inventory-probe-only requires --verification --all-machines")
        command += ["-Dic2.inventoryProbeOnly=true"]
    if args.workflow:
        command += ["-Dic2.verifyWorkflow=true"]
    if args.processing_only:
        command += ["-Dic2.processingOnly=true"]
    if args.processing_recipes:
        command += ["-Dic2.verifyProcessingRecipes=true"]
    if args.item_functions:
        command += ["-Dic2.verifyItemFunctions=true", "-Dic2.itemFunctionSubset="+args.functions_subset]
    if args.recipe_book_only:
        command += ["-Dic2.verifyRecipeBookOnly=true"]
    if args.all_items:
        command += ["-Dic2.verifyAllItems=true"]
    if args.review_shutdown:
        command += ["-Dic2.reviewShutdown=true", "-Dic2.silentRemoteProbe=true"]
    if args.lifecycle:
        command += ["-Dic2.verifyLifecycle=true"]
    if args.tools:
        if not args.verification: parser.error("--tools requires --verification")
        command += ["-Dic2.verifyTools=true"]
    if args.painter:
        if not args.verification: parser.error("--painter requires --verification")
        command += ["-Dic2.verifyPainter=true"]
    if args.cables:
        if not args.verification: parser.error("--cables requires --verification")
        command += ["-Dic2.verifyCables=true"]
    if args.dedicated:
        command += ["-Dic2.verifyDedicated=true"]
    if args.gui_cycle:
        if not args.verification:
            parser.error("--gui-cycle requires --verification")
        command += ["-Dic2.verifyGuiCycle=true"]
    command += arguments(metadata["arguments"]["jvm"], replacements)
    command += [metadata["mainClass"]] + arguments(metadata["arguments"]["game"], replacements)
    command += ["--width", "1280", "--height", "720"]
    if args.server:
        command += ["--quickPlayMultiplayer", args.server]
    elif args.world:
        command += ["--quickPlaySingleplayer", args.world]
    startup = None
    if args.fluid_tooltips:
        startup = subprocess.STARTUPINFO()
        startup.dwFlags |= subprocess.STARTF_USESHOWWINDOW
        startup.wShowWindow = subprocess.SW_HIDE
    raise SystemExit(subprocess.call(command, cwd=game, startupinfo=startup))
