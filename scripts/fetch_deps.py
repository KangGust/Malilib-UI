"""构建前自动拉取编译所需的 mod jar（CI 环境用；本地已有 libs/*.jar 时也会跳过）"""
import json
import os
import sys
import urllib.request

LIBS = "libs"
API = "https://api.modrinth.com/v2/project/%s/version"

# (slug, 需要的游戏版本, 期望的版本号前缀, 目标文件名前缀)
WANTED = [
    ("malilib", "26.3", "0.30.2", "malilib"),
    ("modmenu", "26.3", None, "modmenu"),
]


def get(url):
    req = urllib.request.Request(url, headers={"User-Agent": "malilib-ui-ci/1.0"})
    with urllib.request.urlopen(req, timeout=60) as resp:
        return json.load(resp)


def pick(versions, game_version, prefix):
    candidates = [v for v in versions if game_version in v.get("game_versions", [])]
    if prefix:
        prefixed = [v for v in candidates if v.get("version_number", "").startswith(prefix)]
        if prefixed:
            candidates = prefixed
    if not candidates:
        return None
    candidates.sort(key=lambda v: v.get("date_published", ""), reverse=True)
    return candidates[0]


def download(file_info, target):
    url = file_info["url"]
    req = urllib.request.Request(url, headers={"User-Agent": "malilib-ui-ci/1.0"})
    with urllib.request.urlopen(req, timeout=180) as resp:
        data = resp.read()
    with open(target, "wb") as out:
        out.write(data)
    return len(data)


def main():
    os.makedirs(LIBS, exist_ok=True)
    existing = [f for f in os.listdir(LIBS) if f.endswith(".jar")]
    for slug, game_version, prefix, name_prefix in WANTED:
        if any(f.startswith(name_prefix) for f in existing):
            print("skip %s: 本地已有" % slug)
            continue
        try:
            versions = get(API % slug)
        except Exception as exc:
            print("warn %s: 获取版本列表失败 %s" % (slug, exc))
            continue
        chosen = pick(versions, game_version, prefix)
        if chosen is None:
            print("warn %s: 没有适用于 %s 的版本" % (slug, game_version))
            continue
        files = chosen.get("files", [])
        primary = next((f for f in files if f.get("primary")), files[0] if files else None)
        if primary is None:
            print("warn %s: 版本没有可用文件" % slug)
            continue
        target = os.path.join(LIBS, primary.get("filename", "%s.jar" % slug))
        size = download(primary, target)
        print("ok %s %s -> %s (%d KB)" % (slug, chosen.get("version_number"), target, size // 1024))
    print("libs:", sorted(os.listdir(LIBS)))
    return 0


if __name__ == "__main__":
    sys.exit(main())
