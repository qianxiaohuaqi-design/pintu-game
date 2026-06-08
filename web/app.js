import { SUPABASE_PUBLISHABLE_KEY, SUPABASE_URL } from "./supabase-config.js";

const images = [
  { key: "cat1", name: "小猫", src: "./image/cat/cat1/all.jpg" },
  { key: "cat2", name: "银猫", src: "./image/cat/cat2/all.jpg" },
  { key: "cat3", name: "萌猫", src: "./image/cat/cat3/all.jpg" },
  { key: "dog1", name: "小狗", src: "./image/dog/dog1/all.jpg" },
  { key: "dog2", name: "柴犬", src: "./image/dog/dog2/all.jpg" },
  { key: "dog3", name: "伙伴", src: "./image/dog/dog3/all.jpg" },
  { key: "emoji1", name: "表情", src: "./image/emoji/emoji1/all.jpg" },
  { key: "emoji2", name: "开心", src: "./image/emoji/emoji2/all.jpg" },
  { key: "emoji3", name: "可爱", src: "./image/emoji/emoji3/all.jpg" },
];

const state = {
  grid: 4,
  mode: "casual",
  imageIndex: 0,
  tiles: [],
  steps: 0,
  seconds: 0,
  timerId: null,
  running: false,
};

const els = {
  board: document.querySelector("#board"),
  steps: document.querySelector("#steps"),
  timer: document.querySelector("#timer"),
  hint: document.querySelector("#hintButton"),
  preview: document.querySelector("#previewImage"),
  player: document.querySelector("#playerName"),
  databaseStatus: document.querySelector("#databaseStatus"),
  leaderboard: document.querySelector("#leaderboard"),
  rankGrid: document.querySelector("#rankGrid"),
  resultDialog: document.querySelector("#resultDialog"),
  dialogTitle: document.querySelector("#dialogTitle"),
  dialogMessage: document.querySelector("#dialogMessage"),
};

const hasSupabase = Boolean(SUPABASE_URL && SUPABASE_PUBLISHABLE_KEY);
els.databaseStatus.textContent = hasSupabase ? "在线排行榜" : "本地排行榜";

document.querySelector("#startGame").addEventListener("click", () => startGame());
document.querySelector("#restartGame").addEventListener("click", () => startGame());
document.querySelector("#backToSetup").addEventListener("click", () => {
  stopTimer();
  document.querySelector("#setupPanel").scrollIntoView({ behavior: "smooth", block: "center" });
});
document.querySelector("#changeImage").addEventListener("click", () => {
  state.imageIndex = (state.imageIndex + 1) % images.length;
  startGame();
});
document.querySelector("#hintButton").addEventListener("click", showHint);
document.querySelector("#dialogRestart").addEventListener("click", () => {
  els.resultDialog.close();
  startGame();
});
document.querySelector("#dialogClose").addEventListener("click", () => els.resultDialog.close());
els.rankGrid.addEventListener("change", () => loadLeaderboard(Number(els.rankGrid.value)));

document.querySelectorAll("[data-mode]").forEach((button) => {
  button.addEventListener("click", () => {
    selectButton("[data-mode]", button);
    state.mode = button.dataset.mode;
    updateModeControls();
  });
});

document.querySelectorAll("[data-grid]").forEach((button) => {
  button.addEventListener("click", () => {
    selectButton("[data-grid]", button);
    state.grid = Number(button.dataset.grid);
    els.rankGrid.value = String(state.grid);
    loadLeaderboard(state.grid);
  });
});

document.querySelectorAll("[data-image]").forEach((button) => {
  button.addEventListener("click", () => {
    selectButton("[data-image]", button);
    const index = images.findIndex((image) => image.key === button.dataset.image);
    state.imageIndex = Math.max(0, index);
    updatePreview();
  });
});

function selectButton(selector, selected) {
  document.querySelectorAll(selector).forEach((button) => button.classList.remove("selected"));
  selected.classList.add("selected");
}

function updateModeControls() {
  els.hint.hidden = state.mode === "challenge";
}

function startGame() {
  stopTimer();
  state.steps = 0;
  state.seconds = 0;
  state.running = true;
  state.tiles = createSolvableTiles(state.grid);
  updateModeControls();
  updatePreview();
  renderBoard();
  updateHud();
  state.timerId = window.setInterval(() => {
    state.seconds += 1;
    updateHud();
  }, 1000);
}

function stopTimer() {
  if (state.timerId) {
    window.clearInterval(state.timerId);
    state.timerId = null;
  }
  state.running = false;
}

function createSolvableTiles(grid) {
  const solved = Array.from({ length: grid * grid }, (_, index) => index);
  let tiles = [...solved];
  do {
    tiles = shuffle([...solved]);
  } while (!isSolvable(tiles, grid) || isSolved(tiles));
  return tiles;
}

function shuffle(items) {
  for (let i = items.length - 1; i > 0; i -= 1) {
    const j = Math.floor(Math.random() * (i + 1));
    [items[i], items[j]] = [items[j], items[i]];
  }
  return items;
}

function isSolvable(tiles, grid) {
  const values = tiles.filter((tile) => tile !== grid * grid - 1);
  let inversions = 0;
  for (let i = 0; i < values.length; i += 1) {
    for (let j = i + 1; j < values.length; j += 1) {
      if (values[i] > values[j]) inversions += 1;
    }
  }
  if (grid % 2 === 1) return inversions % 2 === 0;
  const blankRowFromBottom = grid - Math.floor(tiles.indexOf(grid * grid - 1) / grid);
  return blankRowFromBottom % 2 === 0 ? inversions % 2 === 1 : inversions % 2 === 0;
}

function renderBoard() {
  const grid = state.grid;
  const image = images[state.imageIndex];
  els.board.innerHTML = "";
  els.board.style.setProperty("--grid", grid);
  els.board.style.setProperty("--image", `url("${image.src}")`);
  state.tiles.forEach((tileValue, position) => {
    const tile = document.createElement("button");
    tile.className = tileValue === grid * grid - 1 ? "tile blank" : "tile";
    tile.type = "button";
    tile.dataset.position = String(position);
    tile.setAttribute("aria-label", tile.classList.contains("blank") ? "空白格" : `拼图块 ${tileValue + 1}`);
    if (!tile.classList.contains("blank")) {
      const sourceRow = Math.floor(tileValue / grid);
      const sourceCol = tileValue % grid;
      tile.style.backgroundImage = `url("${image.src}")`;
      tile.style.backgroundSize = `${grid * 100}% ${grid * 100}%`;
      tile.style.backgroundPosition = `${(sourceCol / (grid - 1)) * 100}% ${(sourceRow / (grid - 1)) * 100}%`;
      tile.addEventListener("click", () => moveTile(position));
    }
    els.board.append(tile);
  });
}

function moveTile(position) {
  if (!state.running) return;
  const blank = state.tiles.indexOf(state.grid * state.grid - 1);
  if (!isNeighbor(position, blank, state.grid)) return;
  [state.tiles[position], state.tiles[blank]] = [state.tiles[blank], state.tiles[position]];
  state.steps += 1;
  renderBoard();
  updateHud();
  if (isSolved(state.tiles)) {
    finishGame();
  }
}

function isNeighbor(a, b, grid) {
  const rowA = Math.floor(a / grid);
  const colA = a % grid;
  const rowB = Math.floor(b / grid);
  const colB = b % grid;
  return Math.abs(rowA - rowB) + Math.abs(colA - colB) === 1;
}

function isSolved(tiles) {
  return tiles.every((tile, index) => tile === index);
}

function updatePreview() {
  const image = images[state.imageIndex];
  els.preview.src = image.src;
  els.preview.alt = `${image.name}原图`;
}

function updateHud() {
  els.steps.textContent = String(state.steps);
  els.timer.textContent = formatTime(state.seconds);
}

function formatTime(seconds) {
  const min = String(Math.floor(seconds / 60)).padStart(2, "0");
  const sec = String(seconds % 60).padStart(2, "0");
  return `${min}:${sec}`;
}

function showHint() {
  if (state.mode === "challenge") return;
  const blank = state.tiles.indexOf(state.grid * state.grid - 1);
  const target = state.tiles.findIndex((tile, position) => tile === position && isNeighbor(position, blank, state.grid));
  const position = target >= 0 ? target : state.tiles.findIndex((_, position) => isNeighbor(position, blank, state.grid));
  const tile = els.board.querySelector(`[data-position="${position}"]`);
  if (!tile) return;
  tile.classList.add("hint");
  window.setTimeout(() => tile.classList.remove("hint"), 900);
}

async function finishGame() {
  stopTimer();
  if (state.mode === "challenge") {
    await saveScore();
    await loadLeaderboard(state.grid);
  }
  els.dialogTitle.textContent = state.mode === "challenge" ? "挑战成功" : "拼图完成";
  els.dialogMessage.textContent = `用时 ${formatTime(state.seconds)}，共移动 ${state.steps} 步。`;
  els.resultDialog.showModal();
}

async function saveScore() {
  const score = {
    player_name: sanitizeName(els.player.value),
    grid_size: state.grid,
    mode: "challenge",
    image_key: images[state.imageIndex].key,
    time_seconds: state.seconds,
    steps: state.steps,
  };
  if (!hasSupabase) {
    saveLocalScore(score);
    return;
  }
  const response = await fetch(`${SUPABASE_URL}/rest/v1/scores`, {
    method: "POST",
    headers: supabaseHeaders({ Prefer: "return=minimal" }),
    body: JSON.stringify(score),
  });
  if (!response.ok) {
    saveLocalScore(score);
  }
}

async function loadLeaderboard(grid) {
  els.leaderboard.innerHTML = "<li>读取中...</li>";
  const rows = hasSupabase ? await fetchRemoteScores(grid) : getLocalScores(grid);
  if (!rows.length) {
    els.leaderboard.innerHTML = "<li>暂无成绩，来拿第一个第一名。</li>";
    return;
  }
  els.leaderboard.innerHTML = "";
  rows.slice(0, 8).forEach((score) => {
    const item = document.createElement("li");
    item.innerHTML = `<span>${escapeHtml(score.player_name)}</span><strong>${formatTime(score.time_seconds)} / ${score.steps}步</strong>`;
    els.leaderboard.append(item);
  });
}

async function fetchRemoteScores(grid) {
  const url = new URL(`${SUPABASE_URL}/rest/v1/scores`);
  url.searchParams.set("select", "player_name,time_seconds,steps,created_at");
  url.searchParams.set("grid_size", `eq.${grid}`);
  url.searchParams.set("order", "time_seconds.asc,steps.asc,created_at.asc");
  url.searchParams.set("limit", "8");
  const response = await fetch(url, { headers: supabaseHeaders() });
  if (!response.ok) return getLocalScores(grid);
  return response.json();
}

function supabaseHeaders(extra = {}) {
  return {
    apikey: SUPABASE_PUBLISHABLE_KEY,
    Authorization: `Bearer ${SUPABASE_PUBLISHABLE_KEY}`,
    "Content-Type": "application/json",
    ...extra,
  };
}

function sanitizeName(name) {
  const value = name.trim().slice(0, 16);
  return value || "玩家";
}

function saveLocalScore(score) {
  const scores = JSON.parse(localStorage.getItem("pintu-scores") || "[]");
  scores.push({ ...score, created_at: new Date().toISOString() });
  localStorage.setItem("pintu-scores", JSON.stringify(scores));
}

function getLocalScores(grid) {
  return JSON.parse(localStorage.getItem("pintu-scores") || "[]")
    .filter((score) => Number(score.grid_size) === Number(grid))
    .sort((a, b) => a.time_seconds - b.time_seconds || a.steps - b.steps);
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, (char) => {
    const map = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" };
    return map[char];
  });
}

updateModeControls();
updatePreview();
loadLeaderboard(state.grid);
startGame();
