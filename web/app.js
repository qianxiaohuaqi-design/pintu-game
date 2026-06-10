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

const STORAGE_KEYS = {
  name: "pintu-player-name",
  muted: "pintu-muted",
  scores: "pintu-scores",
};

const state = {
  grid: 4,
  mode: "casual",
  imageIndex: 0,
  tiles: [],
  steps: 0,
  seconds: 0,
  timerId: null,
  running: false,
  muted: false,
  hintActive: false,
  lastResult: null,
};

const els = {
  board: document.querySelector("#board"),
  steps: document.querySelector("#steps"),
  timer: document.querySelector("#timer"),
  hint: document.querySelector("#hintButton"),
  preview: document.querySelector("#previewImage"),
  player: document.querySelector("#playerName"),
  leaderboard: document.querySelector("#leaderboard"),
  imageGallery: document.querySelector("#imageGallery"),
  modeLabel: document.querySelector("#modeLabel"),
  gameTip: document.querySelector("#gameTip"),
  resultDialog: document.querySelector("#resultDialog"),
  dialogTitle: document.querySelector("#dialogTitle"),
  dialogMessage: document.querySelector("#dialogMessage"),
  dialogStatus: document.querySelector("#dialogStatus"),
  dialogBackToSetup: document.querySelector("#dialogBackToSetup"),
  dialogLeaderboard: document.querySelector("#dialogLeaderboard"),
  dialogClose: document.querySelector("#dialogClose"),
};

const hasSupabase = Boolean(SUPABASE_URL && SUPABASE_PUBLISHABLE_KEY);
els.player.value = localStorage.getItem(STORAGE_KEYS.name) || "玩家";

const views = {
  lobby: document.querySelector("#setupPanel"),
  game: document.querySelector(".game-layout")
};

function showView(viewName) {
  Object.keys(views).forEach(key => {
    if (key === viewName) {
      views[key].classList.remove("view-hidden");
      views[key].classList.add("view-active");
    } else {
      views[key].classList.add("view-hidden");
      views[key].classList.remove("view-active");
    }
  });
}

document.querySelector("#startGame").addEventListener("click", () => {
  startGame();
  showView("game");
});
document.querySelector("#restartGame").addEventListener("click", () => startGame());
document.querySelector("#backToSetup").addEventListener("click", () => {
  stopTimer();
  showView("lobby");
});
document.querySelector("#changeImage").addEventListener("click", () => {
  selectNextImage();
  startGame();
});
document.querySelector("#randomImage").addEventListener("click", () => {
  selectRandomImage();
  updatePreview();
});
document.querySelector("#hintButton").addEventListener("click", toggleHint);
document.querySelector("#shareResult").addEventListener("click", shareCurrentResult);
document.querySelector("#dialogRestart").addEventListener("click", () => {
  els.resultDialog.close();
  startGame();
});
els.dialogBackToSetup.addEventListener("click", () => {
  els.resultDialog.close();
  showView("lobby");
});
els.dialogLeaderboard.addEventListener("click", () => {
  els.resultDialog.close();
  showView("lobby");
  document.querySelector("#leaderboardPanel").scrollIntoView({ behavior: "smooth", block: "center" });
});
els.dialogClose.addEventListener("click", () => els.resultDialog.close());
els.player.addEventListener("input", () => {
  localStorage.setItem(STORAGE_KEYS.name, sanitizeName(els.player.value));
});
document.addEventListener("keydown", (event) => {
  if (event.target.tagName === "INPUT" || event.target.tagName === "TEXTAREA") return;
  if (!state.running) return;
  const blank = state.tiles.indexOf(state.grid * state.grid - 1);
  const row = Math.floor(blank / state.grid);
  const col = blank % state.grid;
  const targets = {
    ArrowUp: row < state.grid - 1 ? blank + state.grid : -1,
    ArrowDown: row > 0 ? blank - state.grid : -1,
    ArrowLeft: col < state.grid - 1 ? blank + 1 : -1,
    ArrowRight: col > 0 ? blank - 1 : -1,
  };
  if (!(event.key in targets)) return;
  event.preventDefault();
  if (targets[event.key] !== -1) {
    moveTile(targets[event.key]);
  }
});

document.querySelectorAll("[data-mode]").forEach((button) => {
  button.addEventListener("click", () => {
    selectButton("[data-mode]", button);
    state.mode = button.dataset.mode;
    updateModeControls();
    updateHud();
  });
});

document.querySelectorAll("[data-grid]").forEach((button) => {
  button.addEventListener("click", () => {
    selectButton("[data-grid]", button);
    state.grid = Number(button.dataset.grid);
    selectButton("[data-rank-grid]", document.querySelector(`[data-rank-grid="${state.grid}"]`));
    loadLeaderboard(state.grid);
    updateHud();
  });
});

document.querySelectorAll("[data-rank-grid]").forEach((button) => {
  button.addEventListener("click", () => {
    selectButton("[data-rank-grid]", button);
    loadLeaderboard(Number(button.dataset.rankGrid));
  });
});

function renderImageGallery() {
  els.imageGallery.innerHTML = "";
  images.forEach((image, index) => {
    const button = document.createElement("button");
    button.type = "button";
    button.className = index === state.imageIndex ? "image-choice selected" : "image-choice";
    button.dataset.imageIndex = String(index);
    button.innerHTML = `<img src="${image.src}" alt="${image.name}" />`;
    button.addEventListener("click", () => {
      state.imageIndex = index;
      updatePreview();
    });
    els.imageGallery.append(button);
  });
}

function selectButton(selector, selected) {
  if (!selected) return;
  document.querySelectorAll(selector).forEach((button) => button.classList.remove("selected"));
  selected.classList.add("selected");
}

function updateModeControls() {
  els.hint.hidden = state.mode === "challenge";
  els.gameTip.textContent =
    state.mode === "challenge"
      ? "挑战模式会提交成绩到排行榜，智能提示已关闭。"
      : "休闲模式可以使用智能提示，通关不会写入排行榜。";
}

function startGame(silent = false) {
  stopTimer();
  state.steps = 0;
  state.seconds = 0;
  state.running = true;
  state.lastResult = null;
  state.hintActive = false;
  els.hint.textContent = "开启提示";
  els.hint.classList.remove("selected");
  state.tiles = createSolvableTiles(state.grid);
  updateModeControls();
  updatePreview();
  renderBoard();
  updateHud();
  startTimer();
  els.board.focus({ preventScroll: true });
  if (!silent) playTone(520, 0.06);
}

function startTimer() {
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
  let tiles = Array.from({ length: grid * grid }, (_, index) => index);
  let blank = grid * grid - 1;
  let lastMove = -1;
  const numMoves = grid === 3 ? 60 : grid === 4 ? 120 : 200;
  let path = [];

  for (let i = 0; i < numMoves; i++) {
    const neighbors = [];
    const row = Math.floor(blank / grid);
    const col = blank % grid;
    if (row > 0) neighbors.push(blank - grid);
    if (row < grid - 1) neighbors.push(blank + grid);
    if (col > 0) neighbors.push(blank - 1);
    if (col < grid - 1) neighbors.push(blank + 1);

    const validMoves = neighbors.filter(n => n !== lastMove);
    const move = validMoves[Math.floor(Math.random() * validMoves.length)];

    [tiles[move], tiles[blank]] = [tiles[blank], tiles[move]];
    path.push(blank);
    lastMove = blank;
    blank = move;
  }

  state.solutionPath = path.reverse();
  return tiles;
}

// Removed old shuffle and isSolvable functions as they are no longer needed

let domTiles = [];

function renderBoard() {
  const grid = state.grid;
  const image = images[state.imageIndex];
  els.board.innerHTML = "";
  els.board.style.setProperty("--grid", grid);
  domTiles = new Array(grid * grid);

  for (let tileValue = 0; tileValue < grid * grid; tileValue++) {
    const tile = document.createElement("button");
    tile.className = tileValue === grid * grid - 1 ? "tile blank" : "tile";
    tile.type = "button";
    tile.dataset.tileValue = String(tileValue);
    tile.setAttribute("aria-label", tile.classList.contains("blank") ? "空白格" : `拼图块 ${tileValue + 1}`);

    if (!tile.classList.contains("blank")) {
      const sourceRow = Math.floor(tileValue / grid);
      const sourceCol = tileValue % grid;
      tile.style.backgroundImage = `url("${image.src}")`;
      tile.style.backgroundSize = `${grid * 100}% ${grid * 100}%`;
      tile.style.backgroundPosition = `${(sourceCol / (grid - 1)) * 100}% ${(sourceRow / (grid - 1)) * 100}%`;
      tile.addEventListener("click", () => {
        const position = state.tiles.indexOf(tileValue);
        moveTile(position);
      });
    }

    domTiles[tileValue] = tile;
    els.board.append(tile);
  }
  updateBoardPositions();
}

function updateBoardPositions() {
  const grid = state.grid;
  state.tiles.forEach((tileValue, position) => {
    const tile = domTiles[tileValue];
    if (!tile) return;
    const row = Math.floor(position / grid);
    const col = position % grid;
    tile.style.transform = `translate(${col * 100}%, ${row * 100}%)`;
    tile.dataset.position = String(position);
  });
}

function moveTile(position) {
  if (!state.running) return;
  const blank = state.tiles.indexOf(state.grid * state.grid - 1);
  if (!isNeighbor(position, blank, state.grid)) return;

  if (state.solutionPath && state.solutionPath.length > 0) {
    if (position === state.solutionPath[0]) {
      state.solutionPath.shift();
    } else {
      state.solutionPath.unshift(blank);
    }
  }

  [state.tiles[position], state.tiles[blank]] = [state.tiles[blank], state.tiles[position]];
  state.steps += 1;
  updateBoardPositions();
  updateHintDisplay();
  updateHud();
  playTone(420, 0.035);
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
  renderImageGallery();
}

function updateHud() {
  els.steps.textContent = String(state.steps);
  els.timer.textContent = formatTime(state.seconds);
  els.modeLabel.textContent = state.mode === "challenge" ? "挑战模式" : "休闲模式";
}

function formatTime(seconds) {
  const min = String(Math.floor(seconds / 60)).padStart(2, "0");
  const sec = String(seconds % 60).padStart(2, "0");
  return `${min}:${sec}`;
}

function toggleHint() {
  if (state.mode === "challenge" || !state.running) return;
  state.hintActive = !state.hintActive;
  els.hint.textContent = state.hintActive ? "关闭提示" : "开启提示";
  els.hint.classList.toggle("selected", state.hintActive);
  updateHintDisplay();
}

function updateHintDisplay() {
  document.querySelectorAll(".tile.hint").forEach(t => t.classList.remove("hint"));
  if (!state.hintActive || state.mode === "challenge" || isSolved(state.tiles)) return;
  
  if (state.solutionPath && state.solutionPath.length > 0) {
    const position = state.solutionPath[0];
    const tile = els.board.querySelector(`[data-position="${position}"]`);
    if (tile) tile.classList.add("hint");
  }
}

async function finishGame() {
  stopTimer();
  playWinSound();
  let saveMessage = "休闲模式成绩不会写入排行榜。";
  if (state.mode === "challenge") {
    saveMessage = await saveScore();
    await loadLeaderboard(state.grid);
  }
  state.lastResult = {
    mode: state.mode,
    grid: state.grid,
    image: images[state.imageIndex].name,
    seconds: state.seconds,
    steps: state.steps,
  };
  if (state.mode === "casual") {
    els.dialogLeaderboard.style.display = "none";
    els.dialogClose.style.display = "none";
  } else {
    els.dialogLeaderboard.style.display = "";
    els.dialogClose.style.display = "";
  }
  
  els.dialogTitle.textContent = state.mode === "challenge" ? "挑战成功" : "拼图完成";
  els.dialogMessage.textContent = `用时 ${formatTime(state.seconds)}，共移动 ${state.steps} 步。`;
  els.dialogStatus.textContent = saveMessage;
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
    return "当前未连接在线数据库，成绩已保存到本地。";
  }
  try {
    const response = await fetch(`${SUPABASE_URL}/rest/v1/scores`, {
      method: "POST",
      headers: supabaseHeaders({ Prefer: "return=minimal" }),
      body: JSON.stringify(score),
    });
    if (!response.ok) throw new Error(`Supabase status ${response.status}`);
    return "挑战成绩已提交到在线排行榜。";
  } catch {
    saveLocalScore(score);
    return "在线提交失败，成绩已临时保存到本地。";
  }
}

async function loadLeaderboard(grid) {
  els.leaderboard.innerHTML = '<li class="rank-empty">读取中...</li>';
  const rows = hasSupabase ? await fetchRemoteScores(grid) : getLocalScores(grid);
  if (!rows.length) {
    els.leaderboard.innerHTML = '<li class="rank-empty">暂无成绩，来拿第一个第一名。</li>';
    return;
  }
  els.leaderboard.innerHTML = "";
  rows.slice(0, 8).forEach((score, index) => {
    const item = document.createElement("li");
    item.innerHTML = `
      <span class="rank-no">${index + 1}</span>
      <span class="rank-player">${escapeHtml(score.player_name)}</span>
      <strong>${formatTime(score.time_seconds)} / ${score.steps}步</strong>
    `;
    els.leaderboard.append(item);
  });
}

async function fetchRemoteScores(grid) {
  const url = new URL(`${SUPABASE_URL}/rest/v1/scores`);
  url.searchParams.set("select", "player_name,time_seconds,steps,created_at");
  url.searchParams.set("grid_size", `eq.${grid}`);
  url.searchParams.set("order", "time_seconds.asc,steps.asc,created_at.asc");
  url.searchParams.set("limit", "8");
  try {
    const response = await fetch(url, { headers: supabaseHeaders() });
    if (!response.ok) return getLocalScores(grid);
    return response.json();
  } catch {
    return getLocalScores(grid);
  }
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

function selectNextImage() {
  state.imageIndex = (state.imageIndex + 1) % images.length;
  updatePreview();
}

function selectRandomImage() {
  if (images.length < 2) return;
  let next = state.imageIndex;
  while (next === state.imageIndex) {
    next = Math.floor(Math.random() * images.length);
  }
  state.imageIndex = next;
}

function saveLocalScore(score) {
  const scores = JSON.parse(localStorage.getItem(STORAGE_KEYS.scores) || "[]");
  scores.push({ ...score, created_at: new Date().toISOString() });
  localStorage.setItem(STORAGE_KEYS.scores, JSON.stringify(scores));
}

function getLocalScores(grid) {
  return JSON.parse(localStorage.getItem(STORAGE_KEYS.scores) || "[]")
    .filter((score) => Number(score.grid_size) === Number(grid))
    .sort((a, b) => a.time_seconds - b.time_seconds || a.steps - b.steps);
}

async function shareCurrentResult() {
  const result = state.lastResult || {
    mode: state.mode,
    grid: state.grid,
    image: images[state.imageIndex].name,
    seconds: state.seconds,
    steps: state.steps,
  };
  const text = `我在拼图游戏完成了 ${result.grid}x${result.grid} ${result.image}：用时 ${formatTime(result.seconds)}，${result.steps} 步。`;
  if (navigator.share) {
    try {
      await navigator.share({ title: "拼图游戏成绩", text });
      return;
    } catch {
      // Browser share can be cancelled; copying keeps the action useful.
    }
  }
  try {
    await navigator.clipboard?.writeText(text);
    els.gameTip.textContent = "成绩文案已复制，可以直接分享给朋友。";
  } catch {
    els.gameTip.textContent = text;
  }
}

function playTone(frequency, duration) {
  const AudioApi = window.AudioContext || window.webkitAudioContext;
  if (state.muted || !AudioApi) return;
  let context;
  try {
    context = new AudioApi();
  } catch {
    return;
  }
  const oscillator = context.createOscillator();
  const gain = context.createGain();
  oscillator.frequency.value = frequency;
  oscillator.type = "triangle";
  gain.gain.value = 0.05;
  oscillator.connect(gain);
  gain.connect(context.destination);
  oscillator.start();
  oscillator.stop(context.currentTime + duration);
  oscillator.addEventListener("ended", () => context.close());
}

function playWinSound() {
  playTone(660, 0.08);
  window.setTimeout(() => playTone(880, 0.1), 100);
}

function escapeHtml(value) {
  return String(value).replace(/[&<>"']/g, (char) => {
    const map = { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" };
    return map[char];
  });
}

updateModeControls();
updatePreview();
updateHud();
loadLeaderboard(state.grid);
startGame(true);
