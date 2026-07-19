# 真人PK — 在线用户实时房间创建与多人交互实现指南

## 一、总体架构概览

真人PK功能实现了一个**双人在线实时对战**系统，允许用户创建/加入房间、选择难度后进行英语词汇选择题PK。整个系统采用 **HTTP 短轮询（Polling）** 而非 WebSocket 实现"实时"交互，架构简洁、适合中小规模部署。

```
┌─────────────────────────────────────────────────────────────┐
│                    前端 (Vue 3 单页)                         │
│                 EnglishWorldPage.vue                        │
│                                                             │
│  ┌──────────┐  ┌──────────────┐  ┌──────────┐              │
│  │ 大厅页   │  │  答题页       │  │ 结果页    │              │
│  │ (lobby)  │  │  (playing)    │  │ (result)  │              │
│  │          │  │               │  │           │              │
│  │ 创建房间 │  │ 题目渲染      │  │ 胜负判定  │              │
│  │ 加入房间 │  │ 倒计时15s     │  │ 分数对比  │              │
│  │ 房间列表 │  │ 进度双条      │  │ 中途退出  │              │
│  │ 房间轮询 │  │ 对手轮询      │  │           │              │
│  └──────────┘  └──────────────┘  └──────────┘              │
│         │              │                │                   │
│         └──────────────┼────────────────┘                   │
│                        │ HTTP POST/GET (JSON)               │
└────────────────────────┼────────────────────────────────────┘
                         │
┌────────────────────────┼────────────────────────────────────┐
│                后端 (Java Servlet)                           │
│                         │                                    │
│   ┌─────────────────────┼─────────────────────┐             │
│   │ PkRoomServlet       │ PkGameServlet        │             │
│   │ /api/pk/room        │ /api/pk/game         │             │
│   │                     │                      │             │
│   │ POST: create,join,  │ POST: start,answer,  │             │
│   │   leave,disband,     │   opponent,quit,     │             │
│   │   myroom,update,list │   questions,result   │             │
│   │ GET: list,status     │                      │             │
│   └──────────┬───────────┴──────────┬───────────┘             │
│              │                      │                         │
│   ┌──────────┴──────────────────────┴───────────┐             │
│   │          GameRoomManager (Service)           │             │
│   │  - createRoom / joinRoom / leaveRoom         │             │
│   │  - disbandRoom / listPublicRooms             │             │
│   │  - 过期房间两级清理 (WAITING 30min/FINISHED 5min) │             │
│   └──────────────────┬───────────────────────────┘             │
│                      │                                        │
│   ┌──────────────────┴───────────────────────────┐             │
│   │         ServletContext (全局共享)              │             │
│   │  "pkRooms" → ConcurrentHashMap               │             │
│   │     key: roomCode ("1234")                    │             │
│   │     value: GameRoom 实体                      │             │
│   └──────────────────────────────────────────────┘             │
└──────────────────────────────────────────────────────────────┘
```

---

## 二、核心设计：为什么用 HTTP 轮询而非 WebSocket？

| 维度 | HTTP 轮询（本方案） | WebSocket |
|------|---------------------|-----------|
| 实现复杂度 | 低，普通 Servlet 即可 | 需要额外 WebSocket 端点 |
| 部署要求 | 无特殊要求 | 需要代理/负载均衡支持 |
| 实时性 | 2秒延迟（可接受） | 毫秒级 |
| 资源消耗 | 每2秒一个轻量请求 | 长连接常驻内存 |
| 适用场景 | 双人对战，状态变化不频繁 | 高频实时交互（如白板） |

**选择理由**：真人PK是双人回合制答题，状态变化频率低（每题15秒），2秒轮询间隔对用户体验影响极小，但大幅降低了系统复杂度和运维成本。

---

## 三、房间生命周期

```
                  创建房间
                     │
                     ▼
              ┌─────────────┐
        ┌────▶│   WAITING    │◀──── 加入者离开
        │     │  (等待加入)   │──────▶ 房间变回单人
        │     └──────┬──────┘
        │            │ 房主解散 / 超时30分钟
        │            ▼
        │     ┌─────────────┐
        │     │   READY      │ (房主+加入者均已就位)
        │     └──────┬──────┘
        │            │ 房主点击"开始对战"
        │            ▼
        │     ┌─────────────┐
        │     │   PLAYING    │
        │     │  (答题中)     │
        │     └──────┬──────┘
        │            │
        │     ┌──────┼──────┐
        │     │             │
        │     ▼             ▼
        │  双方完成      有人退出
        │     │             │
        │     └──────┬──────┘
        │            ▼
        │     ┌─────────────┐
        └─────│  FINISHED   │
              │  (已结束)     │
              └─────────────┘
```

### 状态机详解

| 状态 | 含义 | 触发条件 |
|------|------|----------|
| `WAITING` | 等待玩家加入 | 房间创建后默认状态 |
| `PLAYING` | 答题进行中 | 房主调用 `start` 接口 |
| `FINISHED` | 对战已结束 | 双方答完 / 有人退出 |

---

## 四、前后端通信协议

### 4.1 房间管理 API (`/api/pk/room`)

所有接口需要用户登录（Session 中有 `userId`）。

| Action | 方法 | 说明 | 关键参数 |
|--------|------|------|----------|
| `create` | POST | 创建房间 | `isPublic`, `difficulty` |
| `join` | POST | 加入房间 | `roomCode` |
| `myroom` | POST | 查询我的房间 | — |
| `disband` | POST | 解散房间（仅房主） | — |
| `leave` | POST | 离开房间 | `roomCode` |
| `update` | POST | 修改设置（仅房主） | `roomCode`, `difficulty` |
| `list` | POST/GET | 公开房间列表（分页） | `page` |
| `status` | GET | 查询单个房间状态 | `roomCode` |

**返回的 `isReady` 含义**：`hostUserId != null && guestUserId != null`（双方都在房间内）

### 4.2 游戏对战 API (`/api/pk/game`)

| Action | 方法 | 说明 | 关键参数 |
|--------|------|------|----------|
| `start` | POST | 房主开始游戏 | `roomCode`, `difficulty` |
| `questions` | POST | 加入者获取题目 | `roomCode` |
| `answer` | POST | 提交答案 | `roomCode`, `questionIndex`, `selectedOption`, `elapsedMs` |
| `opponent` | POST | 查询对手进度 | `roomCode` |
| `result` | POST | 获取最终结果 | `roomCode` |
| `quit` | POST | 中途退出 | `roomCode` |

---

## 五、数据模型 — GameRoom 实体

```java
public class GameRoom {
    // ==== 基础信息 ====
    String  roomCode;       // 4位数字房间码（如 "1234"）
    Long    hostUserId;     // 房主用户ID
    Long    guestUserId;    // 加入者用户ID（null = 等待中）
    String  status;         // WAITING / PLAYING / FINISHED
    String  difficulty;     // junior / senior / cet4 / cet6 / graduate / toefl
    boolean isPublic;       // 公开房间（显示在列表）/ 私密房间（仅通过房码加入）
    long    createdAt;      // 创建时间戳（用于过期清理）

    // ==== 游戏数据 ====
    List<PkQuestion> questions;           // 20道题（房主开始游戏时生成）

    Map<Integer, Boolean> hostAnswers;    // 房主答案：题号→是否正确
    Map<Integer, Boolean> guestAnswers;   // 加入者答案：题号→是否正确
    Map<Integer, Long>  hostAnswerTimes;  // 房主每题耗时（毫秒）
    Map<Integer, Long>  guestAnswerTimes; // 加入者每题耗时（毫秒）

    volatile boolean hostFinished;        // 房主是否答完全部题目
    volatile boolean guestFinished;       // 加入者是否答完全部题目
}
```

**线程安全**：`hostAnswers`、`guestAnswers` 等 Map 使用 `ConcurrentHashMap`，`hostFinished`/`guestFinished` 使用 `volatile` 保证多线程可见性。

---

## 六、题目系统

### 6.1 难度与词库映射

| 难度 Key | 标签 | 词汇量 |
|----------|------|--------|
| `junior` | 初中 | 20词 |
| `senior` | 高中 | 20词 |
| `cet4` | 四级 | 20词（默认） |
| `cet6` | 六级 | 20词 |
| `graduate` | 考研 | 20词 |
| `toefl` | 托福 | 20词 |

### 6.2 题目生成流程

```
1. 根据 difficulty 选择对应词库
2. 打乱词库顺序 (Collections.shuffle)
3. 取前 20 个词
4. 对每道题的4个选项再次打乱
5. 重新计算 correctIndex（因为选项顺序变了）
6. 封装为 PkQuestion 列表存入 GameRoom
```

### 6.3 服务端答案校验

用户在客户端提交答案时，服务端会**二次校验**：
```java
// PkGameServlet.handleAnswer() 中
GameRoom.PkQuestion q = room.getQuestions().get(questionIndex);
correct = (selectedOption == q.getCorrectIndex());
```

**这是安全关键环节**：防止客户端篡改答案。客户端仅做 UI 展示判断，最终正确数由服务端的 `hostAnswers` / `guestAnswers` 决定。

---

## 七、实时交互 — 三层轮询机制

这是整个系统最关键的部分。由于不使用 WebSocket，前后端通过**三层定时轮询**实现"实时"感知对方状态：

### 7.1 房间状态轮询（2秒间隔）

**触发时机**：用户创建/加入房间后，或进入大厅时。

```javascript
// 前端 startRoomPolling()
roomPollTimer = setInterval(async () => {
    const data = await request.get('/pk/room', { params: { roomCode } })
    // 检测：玩家加入 / 游戏开始 / 游戏结束 / 对手退出
}, 2000)
```

**作用**：
- 房主检测到 `isReady` 变为 `true`（对手加入），解锁"开始对战"按钮
- 加入者检测到 `status` 变为 `PLAYING`（房主开始了），自动调用 `joinGameAsGuest()` 进入答题
- 检测到 `status === 'FINISHED'`（对手退出），自动显示结果
- 检测到房间不存在（房主解散），自动退出

### 7.2 大厅定时刷新（5秒间隔）

**触发时机**：用户在 PK 大厅页面（`pkState === 'lobby'`）。

```javascript
// 前端 startLobbyRefresh()
lobbyRefreshTimer = setInterval(async () => {
    await Promise.all([loadMyRoom(), loadRoomList(roomPage)])
}, 5000)
```

**作用**：自动更新公开房间列表和我的房间状态，无需手动刷新。

### 7.3 对手进度轮询（2秒间隔）

**触发时机**：答题进行中（`pkState === 'playing'`）。

```javascript
// 前端 startOpponentPolling()
opponentPollTimer = setInterval(async () => {
    const data = await request.post('/pk/game', { action: 'opponent', roomCode })
    opponentCorrect.value = data.opponentCorrect
    opponentAnswered.value = data.opponentAnswered
    opponentTimeSec.value = data.opponentTime
    // 检测双方完成 / 对手退出
    if (data.bothFinished) showGameResult()
    if (data.status === 'FINISHED') { /* 对手退出 */ }
}, 2000)
```

**作用**：
- 实时更新双人进度条（你答了N题、对手答了M题）
- 双方都完成后自动跳转结果页
- 检测对手中途退出

### 7.4 轮询流程图

```
时间轴 ──────────────────────────────────────────────────────▶

大厅阶段 (lobby):
  [2s] 查我的房间状态 → 检测对手是否加入 → 检测是否开始
  [2s] 查我的房间状态 → ...
  [5s] 刷新房间列表 + 我的房间状态
  ...

答题阶段 (playing):
  [2s] 查对手进度 (正确数/答题数/耗时) → 更新进度条
  [2s] 查对手进度 → ...
  [15s/题] 用户答题、前端倒计时
  ...
  [答完] → 停止轮询 → 获取结果 → 显示结果页
```

---

## 八、房间存储 — ServletContext 全局共享

### 8.1 存储方式

```java
// AppContextListener.contextInitialized()
ctx.setAttribute("pkRooms", new ConcurrentHashMap<String, GameRoom>());

// GameRoomManager 中读写
ConcurrentHashMap<String, GameRoom> map =
    (ConcurrentHashMap<String, GameRoom>) ctx.getAttribute("pkRooms");
```

**为什么用 ServletContext 而不是数据库？**
- 房间是**临时性**数据，PK 结束后即失效，无需持久化
- `ConcurrentHashMap` 提供 O(1) 读写，比数据库快几个数量级
- 应用重启后房间自然清空，符合预期

### 8.2 过期清理机制（两级 TTL）

系统采用**两级过期清理策略**，通过 `GameRoomManager.cleanupExpired()` 方法统一处理，在每次 `createRoom` 或 `joinRoom` 时触发：

```java
// GameRoomManager.cleanupExpired()
// 两级 TTL：

// 1. WAITING 房间：超过 30 分钟无人加入 → 清理
private static final long ROOM_TIMEOUT_MS = 30 * 60 * 1000;

// 2. FINISHED 房间：结束后超过 5 分钟 → 清理
private static final long FINISHED_ROOM_TTL_MS = 5 * 60 * 1000;
```

**清理逻辑**：
- **WAITING 房间**（等待玩家加入）：从房间创建时间 `createdAt` 起算，超过 30 分钟仍未开始对战的房间被移除，防止大量空房间占用内存。
- **FINISHED 房间**（对战已结束）：调用 `GameRoom.finish()` 时记录 `finishedAt` 时间戳。结束后保留 5 分钟供双方查看结果页，超时后自动移除，既保证用户体验又及时释放资源。
- `cleanupExpired()` 遍历 `ConcurrentHashMap`，对两类过期房间分别判断并移除，无需额外的定时任务线程。

---

## 九、核心交互流程详解

### 9.1 创建房间并开始对战

```
房主                           后端                         加入者
 │                              │                            │
 │──POST /pk/room (create)────▶│                            │
 │◀──{roomCode:"1234"}─────────│                            │
 │                              │                            │
 │──启动房间轮询(2s)───────────▶│◀──每2s查询状态─────────────│
 │                              │                            │
 │                              │◀──POST /pk/room (join)────│
 │                              │───{success:true}─────────▶│
 │                              │                            │
 │◀──轮询返回 isReady=true──────│                            │
 │   "开始对战"按钮解锁          │                            │
 │                              │                            │
 │──POST /pk/game (start)─────▶│                            │
 │   ┌─ 生成20道题              │                            │
 │   └─ 状态→PLAYING             │                            │
 │◀──{questions:[...]}─────────│                            │
 │   进入答题页                  │                            │
 │                              │◀──轮询检测到PLAYING────────│
 │                              │───POST /pk/game            │
 │                              │    (questions)────────────▶│
 │                              │◀──{questions:[...]}────────│
 │                              │                             │
 │   双方开始答题，各自启动对手轮询...                         │
```

### 9.2 答题与提交

```
每题流程 (15秒倒计时):
  
  用户选择答案
   │
   ├─ 客户端计算是否正确（仅UI展示用）
   ├─ myCorrect++ / myAnswered++
   │
   └─ POST /pk/game (answer) ──▶ 服务端校验
       { questionIndex,            │
         selectedOption,           ├─ 对比 correctIndex
         elapsedMs }               ├─ 记录 hostAnswers / guestAnswers
                                   ├─ 记录 hostAnswerTimes / guestAnswerTimes
                                   ├─ 判断是否答完全部 → markFinished()
                                   └─ 判断双方是否都完成 → bothFinished()
                                        │
   ◀────────────────────────────────────┘
   { myFinished, bothFinished }
   
   ┌─ 2秒后自动跳转下一题
   └─ 或 双方完成 → 显示结果
```

### 9.3 中途退出处理

```
玩家A点击"退出对战" → 确认弹窗 → confirmQuit()
   │
   ├─ 停止所有定时器
   ├─ POST /pk/game (quit) → 后端 setStatus(FINISHED)
   └─ 显示结果页 { quitByMe: true }
       玩家B的对手轮询检测到 status=FINISHED
       玩家B看到 "对方已退出，你自动获胜"
```

### 9.4 胜负判定规则

```
1. 正确数高者获胜
2. 正确数相同 → 总耗时短者获胜
3. 正确数和耗时都相同 → 平局
4. 有人退出 → 另一方自动获胜
```

---

## 十、前端状态管理

### 10.1 核心状态变量

```javascript
// ===== 状态机 =====
pkState: null | 'lobby' | 'playing' | 'result'

// ===== 房间 =====
roomCode: ''           // 当前房间码
isHost: false          // 是否是房主
roomReady: false       // 双方是否都就位
myRoom: null           // 我的房间信息对象

// ===== 答题 =====
allQuestions: []       // 全部20道题
currentQuestion: 0     // 当前题号(0-indexed)
answered: false        // 当前题是否已作答
myCorrect: 0           // 我的正确数
opponentCorrect: 0     // 对手正确数
myAnswered: 0          // 我的已答题数
opponentAnswered: 0    // 对手已答题数
questionTimeLeft: 15   // 当前题剩余秒数

// ===== 定时器 =====
roomPollTimer          // 房间状态轮询 (2s)
opponentPollTimer      // 对手进度轮询 (2s)
lobbyRefreshTimer      // 大厅刷新 (5s)
questionTimer          // 单题倒计时 (1s)
```

### 10.2 定时器生命周期管理

```
进入大厅 ──▶ startLobbyRefresh() + loadMyRoom()
               └─ 每5秒刷新
               
创建/加入房间 ──▶ startRoomPolling()
                   └─ 每2秒查询房间状态
                   
进入答题 ──▶ startOpponentPolling() + startQuestionTimer()
              └─ 每2秒查对手进度
              └─ 每1秒倒计时
              
离开页面 / 退出 ──▶ clearInterval(所有定时器)
                   所有状态 resetRoom()
```

---

## 十一、安全设计要点

| 安全点 | 实现方式 |
|--------|----------|
| 身份验证 | 所有 API 通过 `ServletUtil.getSessionUserId()` 检查登录态 |
| 答案防篡改 | 服务端存储正确答案索引，提交时二次校验 `correctIndex` |
| 越权防护 | 每个操作都检查 `isHost()` / `isGuest()` 身份 |
| 一人一房 | `getRoomByUser()` 遍历所有房间，防止同一用户创建多个房间 |
| 重复加入 | `joinRoom()` 检查 `guestUserId != null` |
| 加入自己房间 | 检查 `hostUserId.equals(guestUserId)` |
| 房间码唯一 | `generateRoomCode()` 循环检测直到生成不重复的4位码 |

---

## 十二、文件清单

| 层级 | 文件 | 职责 |
|------|------|------|
| 前端 | `frontend/src/pages/EnglishWorldPage.vue` | PK大厅、答题、结果全部UI和逻辑 |
| 后端-API | `src/main/java/Servlet/PkRoomServlet.java` | 房间 CRUD 接口 |
| 后端-API | `src/main/java/Servlet/PkGameServlet.java` | 游戏对战接口 + 词库 + 题目生成 |
| 后端-实体 | `src/main/java/Entities/GameRoom.java` | 房间数据模型 + 答案记录 |
| 后端-服务 | `src/main/java/Service/GameRoomManager.java` | 房间管理 + 过期清理 + 分页列表 |
| 后端-初始化 | `src/main/java/Listener/AppContextListener.java` | ServletContext 初始化 ROOMS_MAP |

---

## 十三、潜在优化方向

1. **WebSocket 升级**：如果用户量增大或需要更多实时特性（如聊天、实时进度条动画），可将轮询改为 WebSocket 长连接推送。

2. **房间持久化**：当前房间存储在内存中，应用重启即丢失。如需支持断线重连，可将房间状态存入 Redis。

3. **观战模式**：当前仅支持双人对战，可扩展为多人房间，支持观众围观。

4. **历史战绩**：当前 PK 结果不持久化。可新增战绩表记录对战历史。

5. **匹配系统**：当前仅支持手动创建/加入房间。可增加自动匹配功能（类似排位队列）。

6. **更多题型**：当前仅支持"看单词选释义"单选题。可扩展为填空题、听力题、拼写题等。

---

**文档版本**：v1.2  
**最后更新**：2026-07-19