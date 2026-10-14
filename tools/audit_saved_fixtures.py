"""Read the isolated test world's region NBT; never change its save files."""
from pathlib import Path
import argparse
import gzip
import io
import json
import struct
import zlib

PROJECT = Path(__file__).resolve().parents[1]
WORLD = PROJECT.parent / "test-ic2-client-20261012/saves/New World"
PROOF = PROJECT.parent / "verification/ic2-20261012"
POSITIONS = {
    "generator": (32, 101, 32), "macerator": (34, 101, 31),
    "electric_furnace": (35, 101, 31), "compressor": (36, 101, 31),
    "extractor": (37, 101, 31), "metal_former": (32, 101, 35),
    "ore_washing_plant": (34, 101, 35), "induction_furnace": (36, 101, 35),
    "canner": (38, 101, 35), "fluid_distributor": (40, 101, 35),
    "batbox": (42, 101, 35), "mfe": (44, 101, 35),
    "mfsu": (32, 101, 38), "bronze_tank": (44, 101, 38),
}


class NbtReader:
    def __init__(self, data):
        self.stream = io.BytesIO(data)

    def read(self, size):
        value = self.stream.read(size)
        if len(value) != size:
            raise ValueError("Truncated NBT")
        return value

    def number(self, fmt):
        return struct.unpack(">" + fmt, self.read(struct.calcsize(fmt)))[0]

    def string(self):
        return self.read(self.number("H")).decode("utf-8", errors="replace")

    def payload(self, tag):
        if 1 <= tag <= 6:
            return self.number({1: "b", 2: "h", 3: "i", 4: "q", 5: "f", 6: "d"}[tag])
        if tag == 8:
            return self.string()
        if tag == 10:
            result = {}
            while child := self.number("B"):
                name = self.string()
                result[name] = self.payload(child)
            return result
        if tag in (7, 9, 11, 12):
            child = self.number("B") if tag == 9 else {7: 1, 11: 3, 12: 4}[tag]
            count = self.number("i")
            if not 0 <= count <= 1_000_000:
                raise ValueError("Invalid NBT array size")
            return [self.payload(child) for _ in range(count)]
        raise ValueError(f"Unsupported NBT tag {tag}")

    def root(self):
        if self.number("B") != 10:
            raise ValueError("Expected a compound NBT root")
        self.string()
        return self.payload(10)


def chunk(x, z):
    path = WORLD / "dimensions/minecraft/overworld/region" / f"r.{x // 32}.{z // 32}.mca"
    with path.open("rb") as region:
        region.seek(4 * ((x % 32) + (z % 32) * 32))
        location = region.read(4)
        offset = int.from_bytes(location[:3], "big")
        if not offset:
            raise ValueError(f"Chunk {x}, {z} is not saved")
        region.seek(offset * 4096)
        length = int.from_bytes(region.read(4), "big")
        compression = region.read(1)[0]
        data = region.read(length - 1)
    decompress = {1: gzip.decompress, 2: zlib.decompress, 3: lambda raw: raw}.get(compression)
    if decompress is None:
        raise ValueError(f"Unsupported region compression {compression}")
    return NbtReader(decompress(data)).root()


def main():
    global WORLD, PROOF
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--expect-mfsu-mode", type=int)
    parser.add_argument("--runtime-report", type=Path)
    parser.add_argument("--world", type=Path, default=WORLD)
    parser.add_argument("--proof", type=Path, default=PROOF)
    args = parser.parse_args()
    WORLD = args.world.resolve()
    PROOF = args.proof.resolve()
    PROOF.mkdir(parents=True, exist_ok=True)
    chunks = {}
    fixtures = {}
    for name, pos in POSITIONS.items():
        key = (pos[0] // 16, pos[2] // 16)
        if key not in chunks:
            chunks[key] = chunk(*key)
        entities = chunks[key].get("block_entities", [])
        entity = next(e for e in entities if (e["x"], e["y"], e["z"]) == pos)
        assert "ic2" in entity, f"{name} has no persisted IC2 data"
        fixtures[name] = entity
    coolant = fixtures["bronze_tank"]["ic2"]["components"]["fluid"]["contents"]
    water = fixtures["ore_washing_plant"]["ic2"]["components"]["fluid"]["fluid"]
    assert coolant["FluidName"] == "ic2:coolant" and coolant["Amount"] == 2250, "Stored coolant changed"
    assert water["FluidName"] == "minecraft:water" and water["Amount"] == 3000, "Stored water changed"
    mfsu_mode = fixtures["mfsu"]["ic2"]["redstoneMode"]
    if args.expect_mfsu_mode is not None:
        assert mfsu_mode == args.expect_mfsu_mode, f"MFSU mode was not saved: {mfsu_mode}"
    verified_outputs = []
    if args.runtime_report:
        runtime = json.loads(args.runtime_report.read_text("utf-8-sig"))
        assert runtime["finished"] and runtime["clientGuiCycleComplete"] and not runtime["failures"]
        for name, result in (("macerator", "maceratorOutput"), ("electric_furnace", "electric_furnaceOutput"),
                             ("compressor", "compressorOutput"), ("extractor", "extractorOutput")):
            count, item_id = runtime[result].split(" ", 1)
            output = fixtures[name]["ic2"]["InvSlots"]["output"]["Contents"][0]
            assert output["count"] == int(count) and output["id"] == item_id, f"{name} output was not saved"
            verified_outputs.append(name)
    target = PROOF / "saved-fixtures.json"
    target.write_text(json.dumps(fixtures, ensure_ascii=False, indent=2) + "\n", "utf-8")
    summary = {"saved_fixtures": len(fixtures), "evidence": str(target), "mfsu_mode": mfsu_mode,
               "coolant_mb": coolant["Amount"], "water_mb": water["Amount"],
               "verified_machine_outputs": verified_outputs}
    (PROOF / "saved-fixtures-audit.json").write_text(json.dumps(summary, indent=2) + "\n", "utf-8")
    print(json.dumps(summary, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
