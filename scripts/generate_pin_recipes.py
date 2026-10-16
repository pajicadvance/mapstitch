#!/usr/bin/env python3

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

MOD_ID = "mapstitch"
RESULT_ITEM = f"{MOD_ID}:atlas_pin"
RESULT_COUNT = 4
GROUP = "atlas_pin"
FILE_NAME = "atlas_pin_from_{color}_wool"
GENERATED_GLOB = "atlas_pin_from_*_wool.json"

COLORS = {
    "white":      0xF9FFFE,
    "orange":     0xF9801D,
    "magenta":    0xC74EBD,
    "light_blue": 0x3AB3DA,
    "yellow":     0xFED83D,
    "lime":       0x80C71F,
    "pink":       0xF38BAA,
    "gray":       0x474F52,
    "light_gray": 0x9D9D97,
    "cyan":       0x169C9C,
    "purple":     0x8932B8,
    "blue":       0x3C44AA,
    "brown":      0x835432,
    "green":      0x5E7C16,
    "red":        0xB02E26,
    "black":      0x1D1D21,
}

DEFAULT_COLOR = 0xF9FFFE

STRING_INGREDIENTS_FROM = (1, 21, 2)
RECIPES_CONDITION_FROM = (26, 3)

def parse_mc_version(folder: Path):
    mc = folder.name.rsplit("-", 1)[0]
    if not re.fullmatch(r"\d+(\.\d+)*", mc):
        return None
    return tuple(int(part) for part in mc.split("."))

def ingredient(item: str, string_format: bool):
    return item if string_format else {"item": item}

def make_recipe(color: str, rgb: int, string_format: bool) -> dict:
    result = {"id": RESULT_ITEM, "count": RESULT_COUNT}
    if rgb != DEFAULT_COLOR:
        result["components"] = {"minecraft:dyed_color": rgb}
    return {
        "type": "minecraft:crafting_shaped",
        "category": "misc",
        "group": GROUP,
        "pattern": ["W", "N"],
        "key": {
            "W": ingredient(f"minecraft:{color}_wool", string_format),
            "N": ingredient("minecraft:iron_nugget", string_format),
        },
        "result": result,
    }

def make_advancement(recipe_id: str, color: str, recipe_field: str) -> dict:
    return {
        "parent": "minecraft:recipes/root",
        "criteria": {
            "has_the_recipe": {
                "trigger": "minecraft:recipe_unlocked",
                "conditions": {recipe_field: recipe_id},
            },
            "has_wool": {
                "trigger": "minecraft:inventory_changed",
                "conditions": {"items": [{"items": f"minecraft:{color}_wool"}]},
            },
        },
        "requirements": [["has_the_recipe", "has_wool"]],
        "rewards": {"recipes": [recipe_id]},
    }

class Writer:
    def __init__(self):
        self.written = self.unchanged = self.removed = 0

    def write(self, path: Path, data: dict):
        text = json.dumps(data, indent=2) + "\n"
        if path.exists() and path.read_text(encoding="utf-8") == text:
            self.unchanged += 1
            return
        print(f"  write  {path}")
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(text, encoding="utf-8", newline="\n")
        self.written += 1

    def remove_stale(self, directory: Path, expected: set):
        if not directory.is_dir():
            return
        for stale in directory.glob(GENERATED_GLOB):
            if stale.name not in expected:
                print(f"  remove {stale}")
                stale.unlink()
                self.removed += 1

def main():
    versions_dir = ROOT / "versions"
    if not versions_dir.is_dir():
        raise SystemExit(f"No versions/ folder in {ROOT}")

    writer = Writer()
    expected = {FILE_NAME.format(color=c) + ".json" for c in COLORS}

    for folder in sorted(d for d in versions_dir.iterdir() if d.is_dir()):
        version = parse_mc_version(folder)
        if version is None:
            print(f"skip {folder.name} (not '<version>-<loader>')")
            continue
        string_format = version >= STRING_INGREDIENTS_FROM
        recipe_field = "recipes" if version >= RECIPES_CONDITION_FROM else "recipe"
        print(f"{folder.name}: {'string' if string_format else 'object'} ingredients, "
              f"advancement condition '{recipe_field}'")

        data_dir = folder / "src" / "main" / "resources" / "data" / MOD_ID
        recipe_dir = data_dir / "recipe"
        advancement_dir = data_dir / "advancement" / "recipes" / "misc"

        for color, rgb in COLORS.items():
            name = FILE_NAME.format(color=color)
            writer.write(recipe_dir / f"{name}.json", make_recipe(color, rgb, string_format))
            writer.write(advancement_dir / f"{name}.json",
                         make_advancement(f"{MOD_ID}:{name}", color, recipe_field))

        writer.remove_stale(recipe_dir, expected)
        writer.remove_stale(advancement_dir, expected)

    print(f"\n{writer.written} file(s) written, {writer.unchanged} unchanged, {writer.removed} removed.")

if __name__ == "__main__":
    main()
