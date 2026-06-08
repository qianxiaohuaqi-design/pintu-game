# 拼图游戏 Pintu

这是一个从 Java Swing 课程设计继续升级出来的拼图小游戏项目，当前包含两种形态：

- Java 桌面版：`pintu.jar`，保留课程设计的完整可运行版本。
- Web 可玩版：`index.html`，用于发布到 GitHub Pages，让朋友打开网址直接游玩。

## Web 版功能

- 支持休闲模式和挑战模式。
- 支持 3 x 3、4 x 4、5 x 5 三种难度。
- 支持猫、狗、表情素材切换。
- 休闲模式提供智能提示。
- 挑战模式通关后提交排行榜成绩。
- 未配置 Supabase 时自动使用浏览器本地排行榜。

## Supabase 数据库配置

1. 在 Supabase 新建项目 `pintu-game`。
2. 打开 SQL Editor，执行 `supabase/schema.sql`。
3. 在 Project Settings -> API 里复制 Project URL 和 publishable/anon key。
4. 填入 `web/supabase-config.js`：

```js
export const SUPABASE_URL = "https://你的项目.supabase.co";
export const SUPABASE_PUBLISHABLE_KEY = "你的 publishable 或 anon key";
```

不要把 service_role key 放进网页代码。

## 本地预览

```powershell
python -m http.server 5173 --bind 127.0.0.1
```

然后打开 `http://127.0.0.1:5173/`。

## 发布

仓库内置 GitHub Pages 工作流：`.github/workflows/pages.yml`。

推送到 GitHub 后，在仓库 Settings -> Pages 中选择 GitHub Actions 作为 Pages 来源，后续每次推送 `master` 或 `main` 都会自动部署。
