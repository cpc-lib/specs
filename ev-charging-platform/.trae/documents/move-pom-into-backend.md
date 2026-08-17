# 将根目录 Maven 资产移入 backend/

## Context

用户提问为什么 `pom.xml` 不在 `backend/` 里，随后要求"进行调整处理"。当前仓库是 Monorepo 结构，根 `pom.xml` 是聚合 POM（`<packaging>pom</packaging>`），通过 `<module>backend/charging-X</module>` 跨目录聚合 11 个后端子模块；`mvnw` / `mvnw.cmd` 也放在根目录。

经与用户确认，调整目标：

1. **移动范围**：`pom.xml` + `mvnw` + `mvnw.cmd` 三个配套文件一起移入 `backend/`，让 `backend/` 成为自包含的 Maven 聚合项目根。
2. **文档同步**：所有文档中 `mvn` / `./mvnw` 命令字面同步（加 `cd backend &&` 前缀或等价改写）。

预期结果：在 `backend/` 下执行 `./mvnw clean verify` 即可构建全部 11 个后端模块；根目录不再保留 Maven 构建文件。

## 影响面分析（已扫描确认）

### A. 需要移动的文件（3 个）

| 源路径 | 目标路径 |
|---|---|
| `pom.xml` | `backend/pom.xml` |
| `mvnw` | `backend/mvnw` |
| `mvnw.cmd` | `backend/mvnw.cmd` |

说明：
- 不存在 `.mvn/` 目录，无需处理。
- `.tools/`（mvnw 脚本下载 Maven 解压目录）由脚本基于自身位置计算，移动后落在 `backend/.tools/`，`.gitignore` 中 `.tools/` 通配仍生效。
- `scripts/mvnw.ps1`、`scripts/bootstrap-maven.ps1` **保留在 `scripts/`**（用户选了"三个文件一起移"而非"全部 Maven 资产移入"）。

### B. 移动后需要修改的文件

#### B1. 移动后的 `backend/pom.xml` — 模块路径

[pom.xml#L28-L40](file:///d:/code/specs/ev-charging-platform/pom.xml#L28-L40) 中 11 个 `<module>backend/charging-X</module>` 改为 `<module>charging-X</module>`（去掉 `backend/` 前缀，因为新 pom 已经在 `backend/` 内）。

#### B2. 移动后的 `backend/mvnw.cmd` — 脚本指针

[mvnw.cmd#L2](file:///d:/code/specs/ev-charging-platform/mvnw.cmd#L2) 当前调用 `"%~dp0scripts\mvnw.ps1"`。`%~dp0` 是 mvnw.cmd 自身所在目录，移动后变成 `backend/`，需改为 `"%~dp0..\scripts\mvnw.ps1"`，让它仍能找到保留在根 `scripts/` 的 `mvnw.ps1`。

`mvnw`（bash 版）基于 `BASE_DIR="$(cd "$(dirname "$0")" && pwd)"` 自适应，`.tools` 会落在 `backend/.tools/`，逻辑无需改。

#### B3. 11 个 backend 子 pom 的 `<relativePath>`

所有子 pom 当前用 `<relativePath>../../pom.xml</relativePath>` 指向根 pom。移动后新 pom 在 `backend/pom.xml`，子 pom 在 `backend/charging-X/pom.xml`，相对路径应为 `../pom.xml`。需修改的 11 个文件：

- `backend/charging-asset/pom.xml`
- `backend/charging-core/pom.xml`
- `backend/charging-finance/pom.xml`
- `backend/charging-framework/pom.xml`
- `backend/charging-framework-webmvc/pom.xml`
- `backend/charging-gateway/pom.xml`
- `backend/charging-iot/pom.xml`
- `backend/charging-open/pom.xml`
- `backend/charging-operation/pom.xml`
- `backend/charging-payment/pom.xml`
- `backend/charging-system/pom.xml`

每个文件里 `<relativePath>../../pom.xml</relativePath>` → `<relativePath>../pom.xml</relativePath>`。

### C. 调用方脚本与 CI

#### C1. `scripts/validate_static.py`（静态校验，CI 强依赖）

需修改的行（基于已读取内容）：

- 第 41 行：`root_pom = ET.parse(root / 'pom.xml').getroot()` → `root / 'backend' / 'pom.xml'`
- 第 45 行：`if not (root / rel / 'pom.xml').exists()` → `if not (root / 'backend' / rel / 'pom.xml').exists()`（`rel` 来自新 pom 的 `<module>` 文本，移动后是 `charging-X` 而非 `backend/charging-X`）
- 第 279 行：`not ((root/'mvnw').stat().st_mode & 0o111)` → `not ((root/'backend'/'mvnw').stat().st_mode & 0o111)`
- 第 510 行：`root_pom82=(root/'pom.xml').read_text(...)` → `root/'backend'/'pom.xml'`
- 第 511 行：`if '<module>backend/charging-open</module>' not in root_pom82` → `if '<module>charging-open</module>' not in root_pom82`

**保持不变**（已确认这些路径在移动后仍然有效）：

- 第 57 行：`(p.parents[3] / 'pom.xml')` —— 对 `backend/charging-X/src/main/resources/application.yml`，`parents[3]` = `backend/charging-X/`，仍指向子 pom。
- 第 64、105、192、195、254、491、597、600 行：`root/'backend/charging-X/pom.xml'` —— 子 pom 没有移动，路径仍正确。

#### C2. `scripts/verify.sh` 第 13 行

```bash
./mvnw -B -ntp clean verify
```
→
```bash
( cd backend && ./mvnw -B -ntp clean verify )
```

#### C3. `scripts/verify.ps1` 第 16 行

```powershell
& .\mvnw.cmd -B -ntp clean verify
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
```
→
```powershell
Push-Location backend
& .\mvnw.cmd -B -ntp clean verify
$mvnExit = $LASTEXITCODE
Pop-Location
if ($mvnExit -ne 0) { exit $mvnExit }
```

#### C4. `.github/workflows/ci.yml` 第 28-29 行

给 "Maven verify" 步骤加 `working-directory: backend`：

```yaml
- name: Maven verify
  working-directory: backend
  run: ./mvnw -B -ntp clean verify
```

### D. 文档字面同步

所有 `mvn` / `./mvnw` 命令引用加 `cd backend && ` 前缀（PowerShell 文档则用 `Push-Location backend` 风格或注明 "from backend/"）。需修改的文件：

| 文件 | 行 | 当前 | 修改为 |
|---|---|---|---|
| `MANIFEST.md` | 483-485 | 根区列出 `mvnw` / `mvnw.cmd` / `pom.xml` | 从根区删除这三行，在 backend 区添加 `backend/mvnw` / `backend/mvnw.cmd` / `backend/pom.xml` |
| `VALIDATION.md` | 100 | `./mvnw -v` | `cd backend && ./mvnw -v` |
| `VALIDATION.md` | 109 | `mvn clean verify` | `cd backend && mvn clean verify` |
| `README.md` | 76 | `mvn clean verify` | `cd backend && mvn clean verify` |
| `docs/01-requirements/mvp-acceptance.md` | 10 | `mvn clean verify` | `cd backend && mvn clean verify` |
| `docs/11-testing/test-plan.md` | 10 | `mvn clean verify` | `cd backend && mvn clean verify` |
| `docs/13-project-management/release-gates.md` | 5 | `mvn clean verify` | `cd backend && mvn clean verify` |
| `docs/13-project-management/release-gates.md` | 90 | `mvn clean verify` | `cd backend && mvn clean verify` |
| `docs/tasks/SPEC-7.1-acceptance.md` | 5 | `mvn clean verify` | `cd backend && mvn clean verify` |
| `docs/tasks/SPEC-7.5-release-gate.md` | 14 | `mvn clean verify` | `cd backend && mvn clean verify` |
| `docs/tasks/SPEC-7.4-charging-hardening.md` | 33 | `./mvnw clean verify` | `cd backend && ./mvnw clean verify` |
| `docs/tasks/SPEC-7.2-release-gate.md` | 12 | `./mvnw -B -ntp clean verify` | `cd backend && ./mvnw -B -ntp clean verify` |

**保持不变**：
- `RELEASE_NOTES-7.8.md` 第 33 行（"restored executable `mvnw` / shell-script permissions"）—— 历史发布说明，不可改写。
- `scripts/bootstrap-maven.ps1` 第 19-20 行的 `mvn -version` / `mvn clean verify` —— 该脚本本身就把 Maven 安装到 PATH，调用方应在已 `cd backend` 的 shell 里执行；脚本内的提示文字 `Write-Host "Run: mvn clean verify"` 改成 `Write-Host "Run: cd backend; mvn clean verify"`（PowerShell 风格）。

### E. 无需改动的范围（已确认）

- 所有 `*_harness.sh` / `*_harness.ps1` —— 直接 `javac` 编译 Java 源码，不调用 mvn。
- `deploy/docker/docker-compose.yml` —— 仅基础设施容器，不构建 Java 镜像，无 pom.xml 拷贝。
- `.gitignore` —— `.tools/`、`**/target/` 通配仍生效。
- 11 个 backend 子 pom 的依赖声明、`<parent>` 的 groupId/artifactId/version —— 这些字段不变，只改 `<relativePath>`。

## 实施步骤

1. **移动文件**（用 `git mv` 保留历史，windows 下也可用 `Move-Item`）：
   - `git mv pom.xml backend/pom.xml`
   - `git mv mvnw backend/mvnw`
   - `git mv mvnw.cmd backend/mvnw.cmd`
   - 移动后需确保 `backend/mvnw` 仍保留可执行位（git 在 Windows 上可能丢失 mode bit；用 `git update-index --chmod=+x backend/mvnw` 修复）。

2. **修改 `backend/pom.xml`**：11 个 `<module>` 去掉 `backend/` 前缀。

3. **修改 `backend/mvnw.cmd`**：`scripts\mvnw.ps1` 指针改为 `..\scripts\mvnw.ps1`。

4. **批量修改 11 个子 pom**：`<relativePath>../../pom.xml</relativePath>` → `<relativePath>../pom.xml</relativePath>`（每个文件用一次 Edit）。

5. **修改 `scripts/validate_static.py`**：按 B3 / C1 列表精确修改 5 处。

6. **修改 `scripts/verify.sh` 与 `scripts/verify.ps1`**：按 C2 / C3 修改 mvn 调用段。

7. **修改 `.github/workflows/ci.yml`**：Maven verify 步骤加 `working-directory: backend`。

8. **修改 `scripts/bootstrap-maven.ps1`**：提示文字改为 `Run: cd backend; mvn clean verify`。

9. **文档字面同步**：按 D 表逐个 Edit。

10. **可选**：`git update-index --chmod=+x backend/mvnw`（Windows 上 git mv 后 mode bit 可能丢失，会导致 `validate_static.py` 第 279 行的非 Windows 检查在 CI 上失败）。

## 验证

完成所有修改后，按顺序执行：

1. **静态校验**（必须在仓库根执行）：
   ```bash
   python scripts/validate_static.py
   ```
   预期：无 error 输出；若有 error 会列出具体路径，按提示修复。

2. **Maven 构建**（在 backend/ 执行）：
   ```bash
   cd backend
   ./mvnw -B -ntp clean verify
   ```
   预期：reactor 识别全部 11 个模块并依次构建；如本地缺 JDK 21 或网络受限导致依赖下载失败，至少 `mvn validate` 应通过且 reactor 列表完整。

3. **PowerShell verify 脚本端到端**（可选，仓库根执行）：
   ```powershell
   .\scripts\verify.ps1
   ```
   预期：脚本能找到 `backend/mvnw.cmd` 并完成 Maven verify 阶段。

4. **路径一致性扫描**：
   ```bash
   rg -n "mvnw|mvn |pom\.xml" --glob '!**/node_modules/**' --glob '!.git/**' --glob '!backend/charging-*/pom.xml' --glob '!RELEASE_NOTES-*'
   ```
   预期：所有命中行都已体现 `backend/` 路径前缀或 `cd backend &&` 调用风格，无遗漏的根级 `./mvnw` 调用。

## 关键修改文件清单

- 移动：`pom.xml`、`mvnw`、`mvnw.cmd`
- 编辑（代码/脚本）：`backend/pom.xml`、`backend/mvnw.cmd`、`backend/charging-*/pom.xml`（11 个）、`scripts/validate_static.py`、`scripts/verify.sh`、`scripts/verify.ps1`、`scripts/bootstrap-maven.ps1`、`.github/workflows/ci.yml`
- 编辑（文档）：`MANIFEST.md`、`VALIDATION.md`、`README.md`、`docs/01-requirements/mvp-acceptance.md`、`docs/11-testing/test-plan.md`、`docs/13-project-management/release-gates.md`、`docs/tasks/SPEC-7.1-acceptance.md`、`docs/tasks/SPEC-7.2-release-gate.md`、`docs/tasks/SPEC-7.4-charging-hardening.md`、`docs/tasks/SPEC-7.5-release-gate.md`
