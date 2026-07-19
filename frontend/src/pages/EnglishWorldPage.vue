<template>
  <main class="max-w-[1000px] mx-auto px-6 mt-10 pb-8">
    <h1 class="text-3xl font-bold mb-2">英语天地</h1>
    <p class="text-gray-500 mb-10">在游戏中提升你的英语水平</p>

    <!-- ====== 游戏列表 ====== -->
    <template v-if="!pkState && !rpState && !wcPlaying && !wcModeSelect">
      <div class="space-y-5">
        <!-- 情景对话 -->
        <div
          class="card cursor-pointer hover:shadow-2xl hover:-translate-y-1 transition-all group overflow-hidden p-0"
          @click="rpState = 'scene-select'"
        >
          <!-- 图片区 -->
          <div class="relative h-44 overflow-hidden">
            <img src="/photo/情景对话图片 (1).webp" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" alt="情景对话" />
            <div class="absolute bottom-0 left-0 right-0 h-20 bg-gradient-to-b from-transparent to-white/85 pointer-events-none"></div>
          </div>
          <!-- 文字区 -->
          <div class="px-6 pb-5 pt-2">
            <h3 class="text-2xl font-bold mb-1.5 group-hover:text-purple-600 transition-colors">情景对话</h3>
            <p class="text-sm text-gray-600 leading-relaxed max-w-2xl mb-3">选择场景与AI进行英语对话，实战练习口语和表达能力。AI扮演商店店员、酒店前台、餐厅服务员等各种角色，提供双语旁白和回复建议，让你在真实场景中提升英语水平。</p>
            <span class="inline-flex items-center gap-1.5 px-5 py-2 bg-purple-500 text-white text-sm font-bold rounded-xl hover:bg-purple-600 transition-colors shadow-lg">
              进入游戏 <Icon icon="ph:arrow-right-bold" class="text-sm" />
            </span>
          </div>
        </div>

        <!-- 真人PK -->
        <div
          class="card cursor-pointer hover:shadow-2xl hover:-translate-y-1 transition-all group overflow-hidden p-0"
          @click="pkState = 'lobby'"
        >
          <div class="relative h-44 overflow-hidden">
            <img src="/photo/真人pk图片 (2).webp" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" alt="真人PK" />
            <div class="absolute bottom-0 left-0 right-0 h-20 bg-gradient-to-b from-transparent to-white/85 pointer-events-none"></div>
          </div>
          <div class="px-6 pb-5 pt-2">
            <h3 class="text-2xl font-bold mb-1.5 group-hover:text-red-500 transition-colors">真人PK</h3>
            <p class="text-sm text-gray-600 leading-relaxed max-w-2xl mb-3">与好友实时对战，比拼词汇量和答题速度。创建房间邀请好友加入，选择难度后开始答题挑战，20道选择题一决胜负，看谁的正确率更高！</p>
            <span class="inline-flex items-center gap-1.5 px-5 py-2 bg-red-500 text-white text-sm font-bold rounded-xl hover:bg-red-600 transition-colors shadow-lg">
              进入游戏 <Icon icon="ph:arrow-right-bold" class="text-sm" />
            </span>
          </div>
        </div>

        <!-- 单词接龙 -->
        <div
          class="card cursor-pointer hover:shadow-2xl hover:-translate-y-1 transition-all group overflow-hidden p-0"
          @click="wcModeSelect = true"
        >
          <div class="relative h-44 overflow-hidden">
            <img src="/photo/单词接龙图片(3).webp" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" alt="单词接龙" />
            <div class="absolute bottom-0 left-0 right-0 h-20 bg-gradient-to-b from-transparent to-white/85 pointer-events-none"></div>
          </div>
          <div class="px-6 pb-5 pt-2">
            <h3 class="text-2xl font-bold mb-1.5 group-hover:text-green-600 transition-colors">单词接龙</h3>
            <p class="text-sm text-gray-600 leading-relaxed max-w-2xl mb-3">AI与用户轮流进行英语单词接龙，首尾字母相接，挑战词汇储备极限。提供标准模式和无尽模式，适合不同水平的英语学习者。</p>
            <span class="inline-flex items-center gap-1.5 px-5 py-2 bg-green-500 text-white text-sm font-bold rounded-xl hover:bg-green-600 transition-colors shadow-lg">
              进入游戏 <Icon icon="ph:arrow-right-bold" class="text-sm" />
            </span>
          </div>
        </div>
      </div>
    </template>

    <!-- ====== PK 大厅 ====== -->
    <template v-if="pkState === 'lobby'">
      <div class="max-w-3xl mx-auto border-2 border-gray-200 rounded-2xl p-6">
        <button class="px-4 py-2 rounded-xl bg-blue-50 text-gray-700 font-bold hover:bg-blue-100 transition-all mb-6 flex items-center gap-1 text-sm" @click="leaveLobby">
          <Icon icon="ph:arrow-left-bold" /> 返回游戏列表
        </button>

        <!-- 标题 + 操作按钮 -->
        <div class="flex items-center justify-between mb-6">
          <div>
            <h2 class="text-2xl font-bold">真人PK</h2>
            <p class="text-sm text-gray-400">与好友实时对战，比拼词汇量</p>
          </div>
          <div class="flex gap-3">
            <button
              class="px-5 py-3 rounded-xl bg-gradient-to-r from-red-400 to-pink-500 text-white font-bold hover:shadow-lg transition-all active:scale-[0.98] text-sm"
              @click="showCreateDialog = true"
            >
              <Icon icon="ph:plus-circle-bold" class="inline mr-1" />创建房间
            </button>
            <button
              class="px-5 py-3 rounded-xl border-2 border-gray-200 text-gray-600 font-bold hover:border-red-300 hover:text-red-500 hover:bg-red-50 transition-all active:scale-[0.98] text-sm"
              @click="showCodeOverlay = true"
            >
              <Icon icon="ph:keyboard-bold" class="inline mr-1" />输入房间码
            </button>
          </div>
        </div>

        <!-- 创建房间弹窗 -->
        <Teleport to="body">
          <div v-if="showCreateDialog" class="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 backdrop-blur-sm" @click.self="showCreateDialog = false">
            <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm mx-4">
              <h3 class="text-lg font-bold mb-4">创建房间</h3>
              <!-- 公开/私密切换 -->
              <div class="flex gap-2 mb-4">
                <button
                  class="flex-1 py-3 rounded-xl text-sm font-bold transition-all"
                  :class="createIsPublic ? 'bg-green-500 text-white' : 'bg-gray-100 text-gray-500'"
                  @click="createIsPublic = true"
                >🌐 公开房间</button>
                <button
                  class="flex-1 py-3 rounded-xl text-sm font-bold transition-all"
                  :class="!createIsPublic ? 'bg-gray-700 text-white' : 'bg-gray-100 text-gray-500'"
                  @click="createIsPublic = false"
                >🔒 私密房间</button>
              </div>
              <p class="text-xs text-gray-400 mb-4">
                {{ createIsPublic ? '公开房间会显示在房间列表中，任何人都可以加入' : '私密房间仅通过房间码加入，不会在列表中显示' }}
              </p>
              <!-- 难度选择 -->
              <p class="text-sm font-bold text-gray-600 mb-2">选择难度</p>
              <div class="flex flex-wrap gap-2 mb-4">
                <button
                  v-for="d in difficulties" :key="d.key"
                  class="px-3 py-2 rounded-xl text-xs font-bold transition-all"
                  :class="selectedDifficulty === d.key ? 'bg-[#2563EB] text-white shadow-lg' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'"
                  @click="selectedDifficulty = d.key"
                >{{ d.label }}</button>
              </div>
              <p v-if="createError" class="text-xs text-red-400 mb-3 text-center">{{ createError }}</p>
              <button
                class="w-full py-3 rounded-xl bg-gradient-to-r from-red-400 to-pink-500 text-white font-bold hover:shadow-lg transition-all"
                @click="doCreateRoom"
              >
                <span v-if="creatingRoom">创建中...</span>
                <span v-else>确认创建</span>
              </button>
              <button class="mt-3 w-full text-sm text-gray-400 hover:text-gray-600 transition-colors" @click="showCreateDialog = false">取消</button>
            </div>
          </div>
        </Teleport>

        <!-- 我的房间 -->
        <div v-if="myRoom && myRoom.hasRoom" class="card mb-6 border-2 border-red-200">
          <div class="flex items-center justify-between mb-3">
            <h3 class="font-bold text-lg flex items-center gap-2">
              <Icon v-if="myRoom.isHost" icon="ph:crown-bold" class="text-yellow-500" />
              <Icon v-else icon="ph:user-bold" class="text-blue-500" />
              {{ myRoom.isHost ? '我的房间（房主）' : '已加入的房间' }}
              <button class="ml-2 text-gray-400 hover:text-[#2563EB] transition-colors" title="刷新" @click="loadMyRoom">
                <Icon icon="ph:arrow-clockwise-bold" :class="{ 'animate-spin': roomRefreshing }" />
              </button>
            </h3>
            <span class="text-xs px-2 py-1 rounded-full font-bold"
              :class="myRoom.isReady ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'">
              {{ myRoom.isReady ? '已就位' : '等待中' }}
            </span>
          </div>

          <div class="flex items-center gap-4 mb-3">
            <div>
              <p class="text-xs text-gray-400">房间码</p>
              <p class="text-3xl font-bold tracking-[0.2em] text-[#2563EB]">{{ myRoom.roomCode }}</p>
            </div>
            <div class="flex-1">
              <p class="text-xs text-gray-400">
                <Icon v-if="myRoom.isPublic" icon="ph:globe-bold" class="text-green-500 inline" />
                <Icon v-else icon="ph:lock-bold" class="text-gray-500 inline" />
                {{ myRoom.isPublic ? '公开房间' : '私密房间' }}
              </p>
              <p class="text-xs text-gray-400 mt-1">难度：{{ difficultyLabel(myRoom.difficulty) }}</p>
            </div>
          </div>

          <!-- 房主操作：难度调整 + 解散 -->
          <div v-if="myRoom.isHost" class="space-y-3">
            <div>
              <p class="text-xs font-bold text-gray-500 mb-2">调整难度</p>
              <div class="flex flex-wrap gap-1.5">
                <button
                  v-for="d in difficulties" :key="d.key"
                  class="px-3 py-1.5 rounded-lg text-xs font-bold transition-all"
                  :class="myRoom.difficulty === d.key ? 'bg-[#2563EB] text-white' : 'bg-gray-100 text-gray-500 hover:bg-gray-200'"
                  @click="updateRoomDifficulty(d.key)"
                >{{ d.label }}</button>
              </div>
            </div>
            <div class="flex gap-2">
              <button v-if="myRoom.isReady" class="flex-1 py-3 rounded-xl bg-gradient-to-r from-red-400 to-pink-500 text-white font-bold hover:shadow-lg transition-all" @click="startGame">
                <Icon icon="ph:play-circle-bold" class="inline mr-1" />开始对战
              </button>
              <button v-else class="flex-1 py-3 rounded-xl bg-gradient-to-r from-red-400 to-pink-500 text-white font-bold opacity-50 cursor-not-allowed text-sm">
                <Icon icon="ph:spinner-bold" class="inline mr-1 animate-spin" />等待玩家加入...
              </button>
              <button class="px-4 py-3 rounded-xl bg-red-50 text-red-400 font-bold hover:bg-red-100 transition-all text-sm" @click="disbandMyRoom">
                <Icon icon="ph:trash-bold" /> 解散
              </button>
            </div>
          </div>

          <!-- 加入者：等待 -->
          <div v-else class="text-center py-2">
            <Icon icon="ph:spinner-bold" class="text-3xl text-blue-400 animate-spin mx-auto mb-2" />
            <p class="text-sm text-gray-400">等待房主开启PK...</p>
            <button class="mt-3 text-xs text-red-400 hover:text-red-600 transition-colors" @click="leaveMyRoom">退出房间</button>
          </div>
        </div>

        <!-- 公开房间列表 -->
        <div class="mt-6">
          <div class="flex items-center justify-between mb-3">
            <h3 class="text-sm font-bold text-gray-500 flex items-center gap-2">
              <Icon icon="ph:globe-bold" /> 公开房间
            </h3>
            <button class="text-gray-400 hover:text-[#2563EB] transition-colors p-1" title="刷新列表" @click="loadRoomList(roomPage)">
              <Icon icon="ph:arrow-clockwise-bold" />
            </button>
          </div>
          <div v-if="roomList.length === 0" class="text-center py-10 text-gray-400">
            <Icon icon="ph:users-bold" class="text-4xl mx-auto mb-2 opacity-30" />
            <p class="text-sm">暂无公开房间，快来创建一个吧！</p>
          </div>
          <div v-else class="grid grid-cols-2 gap-3">
            <div
              v-for="room in roomList" :key="room.roomCode"
              class="card transition-all border-2"
              :class="room.isReady
                ? 'border-gray-100 opacity-60 cursor-not-allowed'
                : 'border-transparent hover:border-red-300 hover:shadow-lg hover:-translate-y-0.5 cursor-pointer'"
              @click="!room.isReady && joinPublicRoom(room.roomCode)"
            >
              <div class="flex items-center justify-between mb-2">
                <span class="text-lg font-bold tracking-wider" :class="room.isReady ? 'text-gray-400' : 'text-[#2563EB]'">{{ room.roomCode }}</span>
                <span class="text-[10px] px-2 py-0.5 rounded-full font-bold" :class="room.isReady ? 'bg-red-100 text-red-600' : 'bg-green-100 text-green-700'">
                  {{ room.isReady ? '已满' : '等待中' }}
                </span>
              </div>
              <p class="text-xs" :class="room.isReady ? 'text-gray-400' : 'text-gray-500'">房主：{{ room.hostName }}</p>
              <p class="text-xs" :class="room.isReady ? 'text-gray-300' : 'text-gray-400'">难度：{{ difficultyLabel(room.difficulty) }}</p>
            </div>
          </div>
          <!-- 分页 -->
          <div v-if="roomTotalPages > 1" class="flex items-center justify-center gap-3 mt-4">
            <button class="px-3 py-1.5 rounded-lg text-sm font-bold transition-all"
              :class="roomPage > 1 ? 'bg-gray-100 text-gray-600 hover:bg-gray-200' : 'text-gray-300 cursor-not-allowed'"
              :disabled="roomPage <= 1" @click="loadRoomList(roomPage - 1)">
              <Icon icon="ph:arrow-left-bold" />
            </button>
            <span class="text-xs text-gray-400">{{ roomPage }} / {{ roomTotalPages }}</span>
            <button class="px-3 py-1.5 rounded-lg text-sm font-bold transition-all"
              :class="roomPage < roomTotalPages ? 'bg-gray-100 text-gray-600 hover:bg-gray-200' : 'text-gray-300 cursor-not-allowed'"
              :disabled="roomPage >= roomTotalPages" @click="loadRoomList(roomPage + 1)">
              <Icon icon="ph:arrow-right-bold" />
            </button>
          </div>
        </div>

        <!-- 悬浮房间码输入 -->
        <Teleport to="body">
          <div v-if="showCodeOverlay" class="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 backdrop-blur-sm" @click.self="showCodeOverlay = false">
            <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm mx-4" @click.stop>
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-lg font-bold">输入房间码</h3>
                <button class="text-gray-400 hover:text-gray-600 text-xl leading-none" @click="showCodeOverlay = false">&times;</button>
              </div>
              <div class="flex justify-center gap-3 mb-4">
                <input
                  v-for="(_, i) in 4" :key="i"
                  :ref="el => { if (el) codeInputs[i] = el }"
                  v-model="codeDigits[i]"
                  type="text" inputmode="numeric" maxlength="1"
                  class="w-14 h-16 text-center text-2xl font-bold border-2 border-gray-200 rounded-xl focus:border-[#2563EB] focus:outline-none transition-colors"
                  @input="onCodeInput(i)"
                  @keydown.backspace="onCodeBackspace(i)"
                  @keydown.enter="doJoinRoom"
                />
              </div>
              <p v-if="joinError" class="text-xs text-red-400 mb-3 text-center">{{ joinError }}</p>
              <button
                class="w-full py-3 rounded-xl bg-gradient-to-r from-red-400 to-pink-500 text-white font-bold hover:shadow-lg transition-all disabled:opacity-30"
                :disabled="codeDigits.join('').length < 4 || joining"
                @click="doJoinRoom"
              >
                <span v-if="joining">加入中...</span>
                <span v-else>加入房间</span>
              </button>
            </div>
          </div>
        </Teleport>
      </div>
    </template>

    <!-- ====== PK 答题中 ====== -->
    <template v-if="pkState === 'playing'">
      <div class="max-w-2xl mx-auto border-2 border-gray-200 rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <button class="px-4 py-2 rounded-xl bg-blue-50 text-gray-700 font-bold hover:bg-blue-100 transition-all text-sm flex items-center gap-1" @click="showQuitConfirm = true">
            <Icon icon="ph:sign-out-bold" /> 退出对战
          </button>
          <div class="flex items-center gap-4">
            <span class="text-xs font-bold text-gray-400">题目 {{ currentQuestion + 1 }} / {{ totalQuestions }}</span>
            <span class="text-xs text-gray-300">{{ selectedDifficultyLabel }}</span>
          </div>
        </div>

        <!-- 双人进度条 -->
        <div class="mb-1">
          <div class="flex justify-between text-xs mb-1">
            <span class="text-[#2563EB] font-bold">你 ({{ myCorrect }}✓)</span>
            <span class="text-gray-400">{{ myAnswered }}/{{ totalQuestions }}</span>
          </div>
          <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
            <div class="h-full bg-[#2563EB] rounded-full transition-all duration-500" :style="{ width: (myAnswered / totalQuestions * 100) + '%' }"></div>
          </div>
        </div>
        <div class="mb-6">
          <div class="flex justify-between text-xs mb-1">
            <span class="text-red-400 font-bold">对手 ({{ opponentCorrect }}✓)</span>
            <span class="text-gray-400">{{ opponentAnswered }}/{{ totalQuestions }}</span>
          </div>
          <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
            <div class="h-full bg-red-400 rounded-full transition-all duration-500" :style="{ width: (opponentAnswered / totalQuestions * 100) + '%' }"></div>
          </div>
        </div>

        <!-- 等待提示 -->
        <div v-if="myFinished && !bothFinished" class="text-center py-4 mb-4">
          <Icon icon="ph:spinner-bold" class="text-3xl text-blue-400 animate-spin mx-auto mb-2" />
          <p class="text-sm text-gray-500">你已完成答题，等待对手完成...</p>
        </div>

        <!-- 倒计时 -->
        <div v-if="!answered && !myFinished" class="text-center mb-4">
          <span class="text-sm font-bold" :class="questionTimeLeft <= 5 ? 'text-red-500 animate-pulse' : 'text-gray-500'">
            剩余时间：{{ questionTimeLeft }}s
          </span>
          <div class="w-full h-1.5 bg-gray-100 rounded-full mt-1">
            <div class="h-full rounded-full transition-all duration-1000" :class="questionTimeLeft <= 5 ? 'bg-red-500' : 'bg-[#2563EB]'" :style="{ width: (questionTimeLeft / 15 * 100) + '%' }"></div>
          </div>
        </div>

        <!-- 题目卡片 -->
        <div v-if="currentQ && !myFinished" class="card mb-8">
          <p class="text-xs text-gray-400 mb-2">选择正确的释义</p>
          <h3 class="text-2xl font-bold mb-6">{{ currentQ.word }}</h3>
          <div class="space-y-3">
            <button
              v-for="(opt, idx) in currentQ.options"
              :key="idx"
              class="w-full p-4 rounded-xl border-2 text-left font-medium transition-all"
              :class="getOptionClass(idx)"
              :disabled="answered"
              @click="selectAnswer(idx)"
            >
              <span class="inline-block w-7 h-7 rounded-full text-xs font-bold text-center leading-7 mr-3"
                :class="getOptionDotClass(idx)"
              >{{ String.fromCharCode(65 + idx) }}</span>
              {{ opt }}
            </button>
          </div>
          <div v-if="answered" class="mt-4 p-3 rounded-xl text-sm" :class="feedbackCorrect ? 'bg-blue-50 text-blue-700' : 'bg-red-50 text-red-600'">
            {{ feedbackText }}
          </div>
          <div v-if="answered && currentQuestion < totalQuestions - 1" class="mt-4 text-center">
            <span class="text-xs text-gray-400">3秒后自动跳转下一题...</span>
          </div>
        </div>
      </div>

      <!-- 退出确认弹窗 -->
      <Teleport to="body">
        <div v-if="showQuitConfirm" class="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 backdrop-blur-sm" @click.self="showQuitConfirm = false">
          <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm mx-4 text-center">
            <Icon icon="ph:warning-circle-bold" class="text-5xl text-yellow-500 mx-auto mb-3" />
            <h3 class="text-lg font-bold mb-2">确认退出对战</h3>
            <p class="text-sm text-gray-500 mb-6">
              中途退出将被视为<strong class="text-red-500">失败</strong>，对方将<strong class="text-green-500">自动获胜</strong>。确定要退出吗？
            </p>
            <div class="flex gap-3">
              <button class="flex-1 py-3 rounded-xl bg-gray-100 text-gray-600 font-bold hover:bg-gray-200 transition-all" @click="showQuitConfirm = false">继续答题</button>
              <button class="flex-1 py-3 rounded-xl bg-red-500 text-white font-bold hover:bg-red-600 transition-all" @click="confirmQuit">确认退出</button>
            </div>
          </div>
        </div>
      </Teleport>
    </template>

    <!-- ====== PK 结果 ====== -->
    <template v-if="pkState === 'result'">
      <div class="max-w-md mx-auto border-2 border-gray-200 rounded-2xl p-6 text-center">
        <div class="text-6xl mb-4">{{ resultEmoji }}</div>
        <h2 class="text-2xl font-bold mb-2">{{ resultTitle }}</h2>
        <p class="text-gray-500 mb-6">{{ resultSub }}</p>

        <!-- 中途退出提示 -->
        <div v-if="quitBySomeone" class="mb-6 p-3 rounded-xl text-sm font-bold" :class="quitByMe ? 'bg-red-50 text-red-600' : 'bg-green-50 text-green-600'">
          {{ quitByMe ? '你因中途退出对战而被判失败' : '对方中途退出对战，你自动获胜' }}
        </div>

        <!-- 正常完成时的分数 -->
        <div v-if="!quitBySomeone" class="flex justify-center gap-8 mb-6">
          <div class="text-center">
            <div class="text-3xl font-bold text-[#2563EB]">{{ myCorrect }}/{{ totalQuestions }}</div>
            <div class="text-xs text-gray-400">我 · {{ Math.round(myCorrect / totalQuestions * 100) }}%</div>
            <div class="text-xs text-gray-400 mt-1">耗时 {{ myTimeSec }}s</div>
          </div>
          <div class="w-px bg-gray-200"></div>
          <div class="text-center">
            <div class="text-3xl font-bold text-red-400">{{ opponentCorrect }}/{{ totalQuestions }}</div>
            <div class="text-xs text-gray-400">对手 · {{ Math.round(opponentCorrect / totalQuestions * 100) }}%</div>
            <div class="text-xs text-gray-400 mt-1">耗时 {{ opponentTimeSec }}s</div>
          </div>
        </div>

        <div class="flex justify-center">
          <button
            class="px-8 py-3 rounded-xl bg-[#2563EB] text-white font-bold hover:bg-blue-600 transition-all"
            @click="pkState = 'lobby'; resetRoom()"
          >
            返回大厅
          </button>
        </div>
      </div>
    </template>

    <!-- ====== 情景对话 - 场景选择 ====== -->
    <template v-if="rpState === 'scene-select'">
      <div class="max-w-2xl mx-auto border-2 border-gray-200 rounded-2xl p-6">
        <button class="px-4 py-2 rounded-xl bg-blue-50 text-gray-700 font-bold hover:bg-blue-100 transition-all mb-6 flex items-center gap-1 text-sm" @click="rpState = null">
          <Icon icon="ph:arrow-left-bold" /> 返回游戏列表
        </button>
        <h2 class="text-2xl font-bold mb-2">选择场景</h2>
        <p class="text-gray-500 mb-6">选择一个场景，AI将扮演对应角色与你进行英语对话</p>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div
            v-for="scene in scenes"
            :key="scene.key"
            class="card cursor-pointer hover:shadow-xl hover:-translate-y-1 transition-all text-center group relative overflow-hidden"
            @click="startRolePlay(scene.key)"
          >
            <div class="absolute top-0 left-0 right-0 h-20 bg-gradient-to-b from-purple-50/60 to-transparent pointer-events-none"></div>
            <div class="relative z-10">
              <div class="text-5xl mb-3 group-hover:scale-110 transition-transform">{{ scene.emoji }}</div>
              <h3 class="text-lg font-bold mb-1 group-hover:text-[#2563EB] transition-colors">{{ scene.name }}</h3>
              <p class="text-xs text-gray-400 leading-relaxed">{{ scene.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ====== 情景对话 - 对话界面 ====== -->
    <template v-if="rpState === 'chatting'">
      <div class="max-w-3xl mx-auto border-2 border-gray-200 rounded-2xl p-6">
        <div class="flex items-center justify-between mb-4">
          <button class="px-4 py-2 rounded-xl bg-blue-50 text-gray-700 font-bold hover:bg-blue-100 transition-all text-sm flex items-center gap-1" @click="rpState = 'scene-select'; resetRP()">
            <Icon icon="ph:arrow-left-bold" /> 切换场景
          </button>
          <span class="text-xs font-bold text-[#2563EB] bg-blue-50 px-3 py-1 rounded-full">{{ currentSceneLabel }}</span>
        </div>

        <div class="grid grid-cols-1 lg:grid-cols-4 gap-4" style="min-height: 60vh;">
          <!-- 左侧：对话区 -->
          <div class="lg:col-span-3 flex flex-col">
            <div class="flex-1 space-y-4 mb-4 max-h-[55vh] overflow-y-auto pr-2" ref="chatContainer" style="scrollbar-gutter: stable;">
              <!-- 历史消息 -->
              <div v-for="(msg, i) in rpMessages" :key="i">
                <!-- 旁白 -->
                <div v-if="msg.type === 'narrator'">
                  <!-- 场景偏离警告（最醒目） -->
                  <div v-if="msg.isSceneWarn" class="p-4 rounded-xl border-2 border-red-400 bg-red-50 animate-pulse">
                    <p class="text-sm font-bold text-red-600 mb-1">🚫 场景偏离警告</p>
                    <p class="text-sm text-red-700 font-medium">{{ msg.zh }}</p>
                    <p v-if="msg.en" class="text-xs text-red-400 mt-1">{{ msg.en }}</p>
                  </div>
                  <!-- 英语错误提醒（醒目） -->
                  <div v-else-if="msg.isEngWarn" class="p-4 rounded-xl border-2 border-orange-400 bg-orange-50">
                    <p class="text-sm font-bold text-orange-600 mb-1">⚠️ 英语提醒</p>
                    <p class="text-sm text-orange-700 font-medium">{{ msg.zh }}</p>
                    <p v-if="msg.en" class="text-xs text-orange-400 mt-1">{{ msg.en }}</p>
                  </div>
                  <!-- 普通旁白 -->
                  <div v-else class="p-3 rounded-xl text-sm bg-gray-50 text-gray-600">
                    <p class="text-xs text-gray-400 mb-1">📢 旁白</p>
                    <p>{{ msg.en }}</p>
                    <p v-if="msg.zh" class="text-xs text-gray-400 mt-1">{{ msg.zh }}</p>
                  </div>
                </div>
                <!-- AI角色 -->
                <div v-if="msg.type === 'character'" class="flex gap-3">
                  <div class="w-8 h-8 rounded-full bg-gradient-to-br from-purple-400 to-indigo-500 flex items-center justify-center text-white text-xs font-bold flex-none">
                    {{ msg.name.charAt(0) }}
                  </div>
                  <div class="flex-1">
                    <p class="text-xs text-purple-500 font-bold mb-1">{{ msg.name }}</p>
                    <div class="bg-purple-50 rounded-xl rounded-tl-none p-3 text-sm text-gray-700">{{ msg.dialogue }}</div>
                  </div>
                </div>
                <!-- 用户 -->
                <div v-if="msg.type === 'user'" class="flex gap-3 justify-end">
                  <div class="flex-1 text-right">
                    <p class="text-xs text-gray-400 font-bold mb-1">你</p>
                    <div class="bg-[#2563EB] text-white rounded-xl rounded-tr-none p-3 text-sm inline-block text-left">{{ msg.text }}</div>
                  </div>
                  <div class="w-8 h-8 rounded-full bg-blue-400 flex items-center justify-center text-white text-xs font-bold flex-none">你</div>
                </div>
              </div>

              <!-- 加载状态 -->
              <div v-if="rpLoading" class="flex items-center gap-2 text-gray-400 text-sm p-3">
                <Icon icon="ph:spinner-bold" class="animate-spin" /> AI正在思考...
              </div>
            </div>

            <!-- 输入区 -->
            <div class="flex gap-2">
              <input
                v-model="rpInput"
                type="text"
                placeholder="输入你的英语回复..."
                class="flex-1 h-12 px-4 border-2 border-gray-200 rounded-xl text-sm focus:border-[#2563EB] focus:outline-none transition-colors"
                :disabled="rpLoading"
                @keydown.enter="sendRpMessage"
              />
              <button
                class="px-6 h-12 rounded-xl bg-[#2563EB] text-white font-bold hover:bg-blue-600 transition-all disabled:opacity-30"
                :disabled="!rpInput.trim() || rpLoading"
                @click="sendRpMessage"
              >
                <Icon icon="ph:paper-plane-right-bold" class="text-lg" />
              </button>
            </div>
          </div>

          <!-- 右侧：建议框 -->
          <div class="lg:col-span-1">
            <div class="card">
              <h4 class="text-sm font-bold text-gray-600 mb-3 flex items-center gap-1">
                <Icon icon="ph:lightbulb-bold" class="text-yellow-500" /> 回复建议
              </h4>
              <p v-if="rpSuggestion" class="text-sm text-gray-600 leading-relaxed">{{ rpSuggestion }}</p>
              <p v-else class="text-xs text-gray-400">输入你的第一条英语回复吧！</p>

              <div class="mt-4 pt-4 border-t border-gray-100">
                <h4 class="text-xs font-bold text-gray-400 mb-2">当前角色</h4>
                <div class="flex items-center gap-2">
                  <div class="w-6 h-6 rounded-full bg-gradient-to-br from-purple-400 to-indigo-500 flex items-center justify-center text-white text-[10px] font-bold">{{ rpCharName.charAt(0) }}</div>
                  <span class="text-xs text-gray-600 font-medium">{{ rpCharName || '等待中...' }}</span>
                </div>
              </div>
            </div>

            <!-- 刷新按钮 -->
            <div class="mt-4 text-center">
              <button
                class="px-4 py-2 rounded-xl bg-blue-50 text-gray-700 font-bold hover:bg-blue-100 transition-all text-sm flex items-center gap-1.5 mx-auto"
                @click="refreshRolePlay"
              >
                <Icon icon="ph:arrow-clockwise-bold" class="text-base" /> 刷新对话
              </button>
              <p class="text-xs text-gray-400 mt-1">如有问题可刷新重置</p>
            </div>
          </div>
        </div>

      </div>
    </template>

    <!-- ====== 单词接龙 - 模式选择 ====== -->
    <template v-if="wcModeSelect">
      <div class="max-w-lg mx-auto border-2 border-gray-200 rounded-2xl p-6">
        <button class="px-4 py-2 rounded-xl bg-blue-50 text-gray-700 font-bold hover:bg-blue-100 transition-all mb-6 flex items-center gap-1 text-sm" @click="wcModeSelect = false">
          <Icon icon="ph:arrow-left-bold" /> 返回游戏列表
        </button>
        <h2 class="text-2xl font-bold mb-2">单词接龙</h2>
        <p class="text-gray-500 mb-6">选择一个模式开始游戏</p>
        <div class="space-y-4">
          <div class="card cursor-pointer hover:shadow-xl hover:-translate-y-0.5 transition-all relative overflow-hidden" @click="startWordChain('standard')">
            <div class="absolute top-0 right-0 w-32 h-32 rounded-full bg-blue-50 -mr-10 -mt-10 pointer-events-none"></div>
            <div class="relative z-10 flex items-center gap-4">
              <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-blue-400 to-blue-600 flex items-center justify-center flex-none shadow-lg shadow-blue-200">
                <Icon icon="ph:timer-bold" class="text-white text-2xl" />
              </div>
              <div class="flex-1">
                <h3 class="font-bold mb-0.5">标准模式</h3>
                <p class="text-xs text-gray-400">总时长5分钟，AI验证和响应期间计时暂停</p>
              </div>
              <Icon icon="ph:arrow-right-bold" class="text-gray-300 text-lg flex-none" />
            </div>
          </div>
          <div class="card cursor-pointer hover:shadow-xl hover:-translate-y-0.5 transition-all relative overflow-hidden" @click="startWordChain('endless')">
            <div class="absolute top-0 right-0 w-32 h-32 rounded-full bg-red-50 -mr-10 -mt-10 pointer-events-none"></div>
            <div class="relative z-10 flex items-center gap-4">
              <div class="w-14 h-14 rounded-2xl bg-gradient-to-br from-red-400 to-orange-500 flex items-center justify-center flex-none shadow-lg shadow-red-200">
                <Icon icon="ph:lightning-bold" class="text-white text-2xl" />
              </div>
              <div class="flex-1">
                <h3 class="font-bold mb-0.5">无尽模式</h3>
                <p class="text-xs text-gray-400">无总时长限制，但每轮只有8秒思考时间，超时即结束</p>
              </div>
              <Icon icon="ph:arrow-right-bold" class="text-gray-300 text-lg flex-none" />
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ====== 单词接龙游戏 ====== -->
    <template v-if="wcPlaying">
      <div class="max-w-lg mx-auto border-2 border-gray-200 rounded-2xl p-6">
        <button class="px-4 py-2 rounded-xl bg-blue-50 text-gray-700 font-bold hover:bg-blue-100 transition-all mb-4 flex items-center gap-1 text-sm" @click="quitWordChain">
          <Icon icon="ph:arrow-left-bold" /> 返回模式选择
        </button>

        <!-- 计时器 + 计数 -->
        <div class="card text-center mb-6">
          <div class="flex items-center justify-center gap-6 mb-2">
            <div class="flex items-center gap-2">
              <Icon icon="ph:timer-bold" class="text-xl" :class="wcTimeWarning ? 'text-red-500 animate-pulse' : 'text-[#2563EB]'" />
              <span class="text-2xl font-bold tabular-nums" :class="wcTimeWarning ? 'text-red-500' : 'text-gray-700'">{{ wcTimeDisplay }}</span>
              <span class="text-xs text-gray-400">{{ wcMode === 'endless' ? '本轮' : '' }}</span>
            </div>
            <div class="w-px h-8 bg-gray-200"></div>
            <div class="flex items-center gap-2">
              <Icon icon="ph:link-bold" class="text-xl text-green-500" />
              <span class="text-2xl font-bold text-green-600">{{ wcUserCount }}</span>
              <span class="text-xs text-gray-400">个</span>
            </div>
          </div>
          <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
            <div class="h-full rounded-full transition-all duration-1000" :class="wcTimeWarning ? 'bg-red-500' : 'bg-[#2563EB]'" :style="{ width: wcTimePercent + '%' }"></div>
          </div>
          <p v-if="wcMode === 'standard'" class="text-[10px] text-gray-400 mt-1">总计5分钟，响应期间暂停</p>
          <p v-else class="text-[10px] text-gray-400 mt-1">每轮8秒，超时即结束</p>
        </div>

        <!-- 游戏提示 -->
        <div class="text-center mb-4">
          <p class="text-xs text-gray-400 leading-relaxed">
            ⚠️ 不能使用重复单词 · 禁止使用少于3个字母的单词 · 程序响应期间计时自动暂停，请放心思考
          </p>
        </div>

        <!-- 当前需要接的字母 -->
        <div class="text-center mb-6">
          <p class="text-xs text-gray-400 mb-2">{{ wcTurn === 'user' ? '你需要以此字母开头接龙' : 'AI 正在思考...' }}</p>
          <div class="inline-flex items-center justify-center w-20 h-20 rounded-2xl bg-gradient-to-br from-green-400 to-teal-500 text-white text-4xl font-extrabold shadow-lg transition-all duration-300" :class="{ 'scale-110': wcLetterChanged }">{{ wcLetter }}</div>
        </div>

        <!-- 接龙链条 -->
        <div v-if="wcChain.length > 0" class="mb-6">
          <p class="text-xs text-gray-400 mb-2 text-center">接龙记录</p>
          <div class="flex flex-wrap items-center justify-center gap-1.5">
            <template v-for="(item, i) in wcChain" :key="i">
              <span v-if="item.role === 'ai'" class="px-3 py-1.5 bg-purple-50 text-purple-700 rounded-lg text-sm font-bold" :title="'AI'">{{ item.word }}</span>
              <span v-else class="px-3 py-1.5 bg-green-50 text-green-700 rounded-lg text-sm font-bold" title="你">{{ item.word }}</span>
              <Icon v-if="i < wcChain.length - 1" icon="ph:arrow-right-bold" class="text-gray-300 text-xs" />
            </template>
          </div>
        </div>

        <!-- 消息 -->
        <div v-if="wcMessage" class="p-3 rounded-xl text-sm text-center mb-4 font-medium" :class="wcMessageType === 'error' ? 'bg-red-50 text-red-600' : 'bg-blue-50 text-blue-700'">
          {{ wcMessage }}
        </div>

        <!-- 输入区（仅用户回合） -->
        <div class="flex gap-2" v-if="!wcGameOver && wcTurn === 'user'">
          <input
            ref="wcInputRef"
            v-model="wcInput"
            type="text"
            :placeholder="'输入以 ' + wcLetter + ' 开头的单词...'"
            class="flex-1 h-12 px-4 border-2 border-gray-200 rounded-xl text-sm focus:border-[#2563EB] focus:outline-none transition-colors"
            :disabled="wcLoading"
            @keydown.enter="submitWordChain"
          />
          <button
            class="px-6 h-12 rounded-xl bg-[#2563EB] text-white font-bold hover:bg-blue-600 transition-all disabled:opacity-30"
            :disabled="!wcInput.trim() || wcLoading"
            @click="submitWordChain"
          >
            <Icon icon="ph:paper-plane-right-bold" class="text-lg" />
          </button>
        </div>

        <!-- AI思考中 -->
        <div v-if="wcTurn === 'ai' && !wcGameOver" class="text-center py-4">
          <Icon icon="ph:spinner-bold" class="text-3xl text-purple-400 animate-spin mx-auto mb-2" />
          <p class="text-sm text-gray-400">AI 正在接龙...</p>
        </div>

        <!-- 结束按钮 -->
        <div v-if="!wcGameOver" class="text-center mt-3">
          <button class="text-xs text-gray-400 hover:text-gray-600 transition-colors" @click="endWordChain(true)">结束接龙</button>
        </div>

        <!-- 游戏结束 -->
        <div v-if="wcGameOver" class="card text-center mt-4">
          <div class="text-5xl mb-3">{{ wcEarlyEnd ? '⏹️' : '🎉' }}</div>
          <h3 class="text-xl font-bold mb-2">{{ wcEarlyEnd ? '中途结束' : '接龙结束！' }}</h3>
          <p class="text-lg text-gray-500 mb-1">
            {{ wcEarlyEnd ? '你在 ' + wcElapsedDisplay + ' 内接龙了' : (wcMode === 'endless' ? '无尽模式下，你和AI共完成了' : '5分钟内，你和AI共完成了') }}
          </p>
          <p class="text-4xl font-extrabold text-[#2563EB] mb-1">{{ wcChain.length }} 个单词</p>
          <p class="text-sm text-gray-400 mb-4">其中你贡献了 <strong class="text-green-600">{{ wcUserCount }}</strong> 个单词</p>
          <div v-if="wcChain.length > 0" class="flex flex-wrap justify-center gap-1.5 mb-6">
            <span v-for="(item, i) in wcChain" :key="i"
              class="px-2.5 py-1 rounded-lg text-xs font-bold"
              :class="item.role === 'ai' ? 'bg-purple-50 text-purple-700' : 'bg-green-50 text-green-700'"
            >{{ item.word }}</span>
          </div>
          <button class="px-8 py-3 rounded-xl bg-[#2563EB] text-white font-bold hover:bg-blue-600 transition-all" @click="quitWordChain">
            返回模式选择
          </button>
        </div>
      </div>
    </template>

  </main>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import request from '@/utils/request'

const pkState = ref(null) // null | 'lobby' | 'playing' | 'result'

// ========== 情景对话系统 ==========
const rpState = ref(null) // null | 'scene-select' | 'chatting'
const rpScene = ref('')
const rpRole = ref('')
const rpInput = ref('')
const rpLoading = ref(false)
const rpSuggestion = ref('')
const rpCharName = ref('')
const rpMessages = ref([])
const chatContainer = ref(null)

const scenes = [
  { key: 'shopping', name: '购物', emoji: '🛍️', desc: '在商店中与店员交流，选购商品、讨价还价、结账付款' },
  { key: 'travel', name: '旅游', emoji: '✈️', desc: '旅行中的各种场景：酒店入住、景点咨询、购买门票、问路指引' },
  { key: 'ordering', name: '点餐', emoji: '🍽️', desc: '在餐厅点餐，与服务员交流菜品、特殊要求、付款结账' },
  { key: 'interview', name: '职场面试', emoji: '💼', desc: '模拟求职面试，与HR和面试官进行英语面试对话' },
]

const currentSceneLabel = computed(() => {
  const s = scenes.find(s => s.key === rpScene.value)
  return s ? s.emoji + ' ' + s.name : ''
})

function startRolePlay(sceneKey) {
  rpScene.value = sceneKey
  rpRole.value = getDefaultRole(sceneKey)
  rpMessages.value = []
  rpSuggestion.value = ''
  rpCharName.value = getDefaultRole(sceneKey)
  rpState.value = 'chatting'

  // 发送初始消息让AI开场
  sendToAI('', true)
}

function getDefaultRole(scene) {
  const map = { shopping: 'Shop Assistant', travel: 'Hotel Receptionist', ordering: 'Waiter', interview: 'HR Manager' }
  return map[scene] || 'Assistant'
}

async function sendRpMessage() {
  const text = rpInput.value.trim()
  if (!text || rpLoading.value) return
  rpInput.value = ''

  // 添加用户消息
  rpMessages.value.push({ type: 'user', text })
  scrollChatBottom()
  await sendToAI(text, false)
}

async function sendToAI(message, isInit) {
  rpLoading.value = true
  try {
    // 构建历史摘要
    const history = rpMessages.value.slice(-6).map(m => {
      if (m.type === 'user') return 'User: ' + m.text
      if (m.type === 'character') return m.name + ': ' + m.dialogue
      return ''
    }).filter(Boolean).join('\n')

    const data = await request.post('/roleplay/chat', {
      scene: rpScene.value,
      role: rpRole.value,
      message: message,
      history: history,
    }, { timeout: 35000 })

    if (data.success) {
      // 更新角色
      if (data.characterName) {
        rpRole.value = data.characterName
        rpCharName.value = data.characterName
      }
      // 添加旁白
      const zh = data.narratorZh || ''
      const en = data.narratorEn || ''
      if (en || zh) {
        const isSceneWarn = zh.includes('🚫') || zh.includes('场景提醒') || zh.includes('偏离')
        const isEngWarn = zh.includes('⚠️') || zh.includes('英语提醒') || zh.includes('语法') || zh.includes('拼写') || zh.includes('非英语') || zh.includes('中文')
        rpMessages.value.push({
          type: 'narrator',
          en, zh,
          isSceneWarn,
          isEngWarn,
        })
      }
      // 添加AI角色消息
      if (data.characterDialogue) {
        rpMessages.value.push({
          type: 'character',
          name: data.characterName || rpRole.value,
          dialogue: data.characterDialogue,
        })
      }
      // 更新建议
      if (data.suggestion) {
        rpSuggestion.value = data.suggestion
      }
    } else {
      // 后端失败时用模拟回复
      addMockReply(message, isInit)
    }
  } catch (e) {
    console.error('RolePlay API failed, using mock:', e.message)
    // 后备：模拟回复
    addMockReply(message, isInit)
  } finally {
    rpLoading.value = false
    scrollChatBottom()
  }
}

function addMockReply(message, isInit) {
  const scene = rpScene.value
  const mockData = {
    shopping: [
      { narrator: { en: 'You enter a boutique clothing store. A friendly shop assistant approaches you.', zh: '你走进一家精品服装店，一位热情的店员向你走来。', isSceneWarn: false, isEngWarn: false }, character: { name: 'Shop Assistant', dialogue: 'Hello! Welcome to our store. Are you looking for anything specific today?' }, suggest: '回答你想买什么类型的衣服' },
      { narrator: { en: 'The assistant nods and leads you to the right section.', zh: '店员点点头，带你到对应的区域。', isSceneWarn: false, isEngWarn: false }, character: { name: 'Shop Assistant', dialogue: 'Great choice! We have a variety of styles. What color do you prefer?' }, suggest: '说出你喜欢的颜色' },
    ],
    travel: [
      { narrator: { en: 'You arrive at the hotel lobby with your luggage.', zh: '你带着行李来到酒店大堂。', isSceneWarn: false, isEngWarn: false }, character: { name: 'Hotel Receptionist', dialogue: 'Good afternoon! Welcome to Sunshine Hotel. Do you have a reservation?' }, suggest: '回答是否有预订' },
      { narrator: { en: 'The receptionist checks the computer.', zh: '前台查看电脑记录。', isSceneWarn: false, isEngWarn: false }, character: { name: 'Hotel Receptionist', dialogue: 'Yes, I found your reservation. A deluxe room for two nights. May I see your ID please?' }, suggest: '回应并出示证件' },
    ],
    ordering: [
      { narrator: { en: 'You sit down at a cozy restaurant table. A waiter approaches with a menu.', zh: '你在舒适的餐厅坐下，服务员拿着菜单走来。', isSceneWarn: false, isEngWarn: false }, character: { name: 'Waiter', dialogue: 'Good evening! Welcome to La Maison. Here is our menu. Can I get you something to drink first?' }, suggest: '点一杯饮料' },
      { narrator: { en: 'The waiter brings your drink and takes out a notepad.', zh: '服务员端来饮料，拿出记事本。', isSceneWarn: false, isEngWarn: false }, character: { name: 'Waiter', dialogue: 'Here is your drink. Are you ready to order, or would you like a few more minutes?' }, suggest: '告诉服务员你要点什么菜' },
    ],
    interview: [
      { narrator: { en: 'You walk into a modern office for your job interview. The HR manager greets you with a smile.', zh: '你走进一间现代办公室参加面试，HR经理微笑着迎接你。', isSceneWarn: false, isEngWarn: false }, character: { name: 'HR Manager', dialogue: 'Good morning! Please have a seat. Thank you for coming in today. Could you start by telling me a little about yourself?' }, suggest: '简单介绍你的背景' },
      { narrator: { en: 'The HR manager nods, looking interested in your background.', zh: 'HR经理点头，对你的背景表现出兴趣。', isSceneWarn: false, isEngWarn: false }, character: { name: 'HR Manager', dialogue: 'That is impressive. Can you tell me about a challenging project you worked on and how you handled it?' }, suggest: '描述你处理过的一个挑战' },
    ],
  }

  const mockArr = mockData[scene] || mockData.shopping
  const idx = isInit ? 0 : Math.min(rpMessages.value.filter(m => m.type === 'user').length, mockArr.length - 1)
  const mock = mockArr[idx] || mockArr[mockArr.length - 1]

  rpMessages.value.push({ type: 'narrator', en: mock.narrator.en, zh: mock.narrator.zh, isSceneWarn: mock.narrator.isSceneWarn, isEngWarn: mock.narrator.isEngWarn })
  rpMessages.value.push({ type: 'character', name: mock.character.name, dialogue: mock.character.dialogue })
  rpRole.value = mock.character.name
  rpCharName.value = mock.character.name
  rpSuggestion.value = mock.suggest
}

function scrollChatBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}

function resetRP() {
  rpMessages.value = []
  rpInput.value = ''
  rpSuggestion.value = ''
  rpCharName.value = ''
  rpRole.value = ''
}

function refreshRolePlay() {
  resetRP()
  rpRole.value = getDefaultRole(rpScene.value)
  rpCharName.value = getDefaultRole(rpScene.value)
  sendToAI('', true)
}

// ========== 单词接龙系统 ==========
const wcModeSelect = ref(false)
const wcMode = ref('standard') // 'standard' | 'endless'
const wcPlaying = ref(false)
const wcLetter = ref('')
const wcInput = ref('')
const wcChain = ref([])
const wcUsedWords = ref(new Set())
const wcLoading = ref(false)
const wcMessage = ref('')
const wcMessageType = ref('info')
const wcGameOver = ref(false)
const wcEarlyEnd = ref(false)
const wcElapsedSeconds = ref(0)
let wcStartTime = 0
const wcTimeLeft = ref(300)    // 标准模式：300秒总时长；无尽模式：8秒每轮
const wcLetterChanged = ref(false)
const wcTurn = ref('user')
const wcInputRef = ref(null)
let wcTimer = null

const wcElapsedDisplay = computed(() => {
  const s = wcElapsedSeconds.value
  if (s < 60) return s + '秒'
  return Math.floor(s / 60) + '分' + (s % 60) + '秒'
})

const WC_LETTERS = 'ABCDEFGHIJKLMNOPQRSTUVWY'
const ENDLESS_PER_TURN = 8

const wcTimeDisplay = computed(() => {
  if (wcMode.value === 'endless') {
    return `${wcTimeLeft.value}s`
  }
  const m = Math.floor(wcTimeLeft.value / 60)
  const s = wcTimeLeft.value % 60
  return `${m}:${String(s).padStart(2, '0')}`
})
const wcTimeWarning = computed(() => {
  if (wcMode.value === 'endless') return wcTimeLeft.value <= 3
  return wcTimeLeft.value <= 60
})
const wcTimePercent = computed(() => {
  if (wcMode.value === 'endless') return (wcTimeLeft.value / ENDLESS_PER_TURN) * 100
  return (wcTimeLeft.value / 300) * 100
})
const wcUserCount = computed(() => wcChain.value.filter(c => c.role === 'user').length)

function randomStartLetter() {
  return WC_LETTERS[Math.floor(Math.random() * WC_LETTERS.length)]
}

function startWCTimer() {
  clearInterval(wcTimer)
  wcTimer = setInterval(() => {
    if (wcGameOver.value) return
    if (wcTurn.value === 'user' && !wcLoading.value) {
      wcTimeLeft.value--
      if (wcTimeLeft.value <= 0) {
        clearInterval(wcTimer)
        endWordChain()
      }
    }
  }, 1000)
}

/** 无尽模式：重置本轮倒计时 */
function resetEndlessTimer() {
  if (wcMode.value === 'endless') {
    wcTimeLeft.value = ENDLESS_PER_TURN
  }
}

async function startWordChain(mode) {
  wcMode.value = mode
  wcModeSelect.value = false
  wcPlaying.value = true
  wcChain.value = []
  wcUsedWords.value = new Set()
  wcInput.value = ''
  wcMessage.value = ''
  wcGameOver.value = false
  wcEarlyEnd.value = false
  wcElapsedSeconds.value = 0
  wcStartTime = Date.now()
  wcTimeLeft.value = mode === 'endless' ? ENDLESS_PER_TURN : 300

  const letter = randomStartLetter()
  wcLetter.value = letter
  wcTurn.value = 'user'
  wcMessage.value = `AI 给出起始字母 "${letter}"，请接一个以此字母开头的单词！`
  wcMessageType.value = 'info'
  startWCTimer()
  nextTick(() => wcInputRef.value?.focus())
}

async function submitWordChain() {
  if (wcTurn.value !== 'user' || wcLoading.value || wcGameOver.value) return
  const word = wcInput.value.trim().toLowerCase()
  if (!word) return

  // 前端快速校验
  if (!word.toUpperCase().startsWith(wcLetter.value)) {
    wcMessage.value = `❌ 单词必须以 "${wcLetter.value}" 开头，请重新输入！`
    wcMessageType.value = 'error'
    wcInput.value = ''
    wcInputRef.value?.focus()
    return
  }
  if (wcUsedWords.value.has(word)) {
    wcMessage.value = `❌ "${word}" 已经使用过了，请换一个单词！`
    wcMessageType.value = 'error'
    wcInput.value = ''
    wcInputRef.value?.focus()
    return
  }
  if (word.length < 3) {
    wcMessage.value = `❌ 单词至少需要3个字母，"${word}" 只有 ${word.length} 个字母，请重新输入！`
    wcMessageType.value = 'error'
    wcInput.value = ''
    wcInputRef.value?.focus()
    return
  }

  wcInput.value = ''
  wcLoading.value = true
  wcMessage.value = ''

  try {
    const data = await request.post('/wordchain', {
      action: 'validate', letter: wcLetter.value,
      userWord: word, usedWords: [...wcUsedWords.value].join(','),
    }, { timeout: 35000 })

    if (data.success && data.valid) {
      // 用户单词合法
      wcChain.value.push({ word, role: 'user' })
      wcUsedWords.value.add(word)
      wcLetter.value = word.slice(-1).toUpperCase()
      flashLetter()
      wcMessage.value = `✓ "${word}" 正确！轮到 AI 接龙`
      wcMessageType.value = 'info'

      // AI 回合
      wcTurn.value = 'ai'
      wcLoading.value = true
      try {
        const aiData = await request.post('/wordchain', {
          action: 'generate', letter: wcLetter.value,
          usedWords: [...wcUsedWords.value].join(','),
        }, { timeout: 35000 })

        if (aiData.success && aiData.word && aiData.word.length > 1) {
          const aiWord = aiData.word.toLowerCase()
          if (!wcUsedWords.value.has(aiWord)) {
            wcChain.value.push({ word: aiWord, role: 'ai' })
            wcUsedWords.value.add(aiWord)
            wcLetter.value = aiWord.slice(-1).toUpperCase()
            flashLetter()
            wcMessage.value = `AI: "${aiWord}" → 轮到你了，请接以 "${wcLetter.value}" 开头的单词`
            wcMessageType.value = 'info'
          } else {
            wcMessage.value = `AI 无法接出单词，跳过。请继续接以 "${wcLetter.value}" 开头的单词`
            wcMessageType.value = 'info'
          }
        } else {
          wcMessage.value = `AI 无法接出单词！请继续接以 "${wcLetter.value}" 开头的单词`
          wcMessageType.value = 'info'
        }
      } catch {
        // AI 调用失败，用本地词表
        const localWord = localAIWord(wcLetter.value)
        if (localWord) {
          wcChain.value.push({ word: localWord, role: 'ai' })
          wcUsedWords.value.add(localWord)
          wcLetter.value = localWord.slice(-1).toUpperCase()
          flashLetter()
          wcMessage.value = `AI: "${localWord}" → 轮到你了，请接以 "${wcLetter.value}" 开头的单词`
          wcMessageType.value = 'info'
        } else {
          wcMessage.value = `AI 无法接出单词！请继续接以 "${wcLetter.value}" 开头的单词`
          wcMessageType.value = 'info'
        }
      }
    } else {
      wcMessage.value = `❌ ${data.reason || '无效单词，请重新输入'}`
      wcMessageType.value = 'error'
      wcInput.value = ''
      nextTick(() => wcInputRef.value?.focus())
    }
  } catch {
    // 后端不可用，本地验证通过
    wcChain.value.push({ word, role: 'user' })
    wcUsedWords.value.add(word)
    wcLetter.value = word.slice(-1).toUpperCase()
    flashLetter()
    // AI 回合 - 本地词表
    wcTurn.value = 'ai'
    const localWord = localAIWord(wcLetter.value)
    if (localWord) {
      await new Promise(r => setTimeout(r, 600))
      wcChain.value.push({ word: localWord, role: 'ai' })
      wcUsedWords.value.add(localWord)
      wcLetter.value = localWord.slice(-1).toUpperCase()
      flashLetter()
      wcMessage.value = `AI: "${localWord}" → 轮到你了`
      wcMessageType.value = 'info'
    } else {
      wcMessage.value = `AI 无法接出！继续接以 "${wcLetter.value}" 开头`
      wcMessageType.value = 'info'
    }
  } finally {
    wcTurn.value = 'user'
    wcLoading.value = false
    resetEndlessTimer()
    nextTick(() => wcInputRef.value?.focus())
  }
}

function localAIWord(letter) {
  const pool = {
    'a': ['apple', 'animal', 'answer', 'active', 'above'],
    'b': ['book', 'bird', 'blue', 'break', 'bring'],
    'c': ['cat', 'come', 'call', 'city', 'clean'],
    'd': ['door', 'dark', 'deep', 'drink', 'dream'],
    'e': ['elephant', 'early', 'earth', 'eight', 'enjoy'],
    'f': ['fish', 'fire', 'food', 'free', 'front'],
    'g': ['game', 'garden', 'girl', 'glass', 'green'],
    'h': ['house', 'happy', 'heart', 'horse', 'human'],
    'i': ['island', 'idea', 'image', 'inner', 'iron'],
    'j': ['job', 'join', 'jump', 'just', 'journey'],
    'k': ['king', 'keep', 'kind', 'kitchen', 'know'],
    'l': ['lion', 'lake', 'land', 'learn', 'light'],
    'm': ['moon', 'magic', 'market', 'money', 'music'],
    'n': ['night', 'name', 'nature', 'north', 'number'],
    'o': ['ocean', 'open', 'order', 'other', 'owner'],
    'p': ['park', 'peace', 'phone', 'place', 'power'],
    'q': ['queen', 'quick', 'quiet', 'quite', 'quest'],
    'r': ['river', 'rain', 'read', 'right', 'round'],
    's': ['summer', 'safe', 'school', 'smile', 'story'],
    't': ['tree', 'table', 'think', 'tiger', 'train'],
    'u': ['umbrella', 'under', 'unit', 'upon', 'user'],
    'v': ['village', 'value', 'visit', 'voice', 'very'],
    'w': ['water', 'walk', 'warm', 'watch', 'world'],
    'y': ['yellow', 'year', 'young', 'youth', 'yard'],
  }
  const words = pool[letter.toLowerCase()] || []
  const available = words.filter(w => !wcUsedWords.value.has(w))
  return available.length > 0 ? available[0] : null
}

function flashLetter() {
  wcLetterChanged.value = true
  setTimeout(() => { wcLetterChanged.value = false }, 300)
}

function endWordChain(isEarly = false) {
  clearInterval(wcTimer)
  wcElapsedSeconds.value = Math.floor((Date.now() - wcStartTime) / 1000)
  wcEarlyEnd.value = isEarly
  wcGameOver.value = true
  wcTurn.value = 'user'
  wcLoading.value = false
}

function quitWordChain() {
  clearInterval(wcTimer)
  wcPlaying.value = false
  wcGameOver.value = false
  wcModeSelect.value = true
}


// ========== 房间系统（基于后端 API） ==========
const creatingRoom = ref(false)
const joiningRoom = ref(false)
const roomCode = ref('')
const isHost = ref(false)
const roomReady = ref(false)
const joinError = ref('')
const joining = ref(false)
let roomPollTimer = null
let opponentPollTimer = null

// 创建房间
const showCreateDialog = ref(false)
const createIsPublic = ref(true)
const createError = ref('')

// 我的房间
const myRoom = ref(null)
const roomRefreshing = ref(false)
let lobbyRefreshTimer = null

// 房间列表
const roomList = ref([])
const roomPage = ref(1)
const roomTotalPages = ref(1)
const roomTotal = ref(0)

// 悬浮房间码输入
const showCodeOverlay = ref(false)
const codeDigits = ref(['', '', '', ''])
const codeInputs = ref([])

// 难度
const difficulties = [
  { key: 'junior', label: '初中' },
  { key: 'senior', label: '高中' },
  { key: 'cet4', label: '四级' },
  { key: 'cet6', label: '六级' },
  { key: 'graduate', label: '考研' },
  { key: 'toefl', label: '托福' },
]
const selectedDifficulty = ref('cet4')
const selectedDifficultyLabel = computed(() => {
  const d = difficulties.find(d => d.key === selectedDifficulty.value)
  return d ? d.label : '四级'
})
function difficultyLabel(key) {
  const d = difficulties.find(d => d.key === key)
  return d ? d.label : key
}

// ===== 房间列表操作 =====
async function loadRoomList(page = 1) {
  try {
    const data = await request.get('/pk/room', { params: { action: 'list', page } })
    if (data.success) {
      roomList.value = data.rooms || []
      roomPage.value = data.page || 1
      roomTotalPages.value = data.totalPages || 1
      roomTotal.value = data.total || 0
    }
  } catch (e) { /* 静默 */ }
}

async function joinPublicRoom(code) {
  roomCode.value = ''
  codeDigits.value = code.split('')
  joining.value = true
  joinError.value = ''
  try {
    const data = await request.post('/pk/room', { action: 'join', roomCode: code })
    if (data.success) {
      roomCode.value = code
      isHost.value = false
      roomReady.value = true
      joiningRoom.value = true
      await loadMyRoom()
      startRoomPolling()
      loadRoomList(roomPage.value)
    } else {
      // 加入失败，通过悬浮框显示错误
      showCodeOverlay.value = true
      joinError.value = data.message || '房间码无效或房间不存在，请检查后重试'
      nextTick(() => codeInputs.value[0]?.focus())
    }
  } catch (e) {
    showCodeOverlay.value = true
    joinError.value = '网络错误，请重试'
  } finally {
    joining.value = false
  }
}

async function doJoinRoomWithCode(code) {
  joining.value = true
  joinError.value = ''
  try {
    const data = await request.post('/pk/room', { action: 'join', roomCode: code })
    if (data.success) {
      roomCode.value = code
      isHost.value = false
      roomReady.value = true
      joiningRoom.value = true
      showCodeOverlay.value = false
      await loadMyRoom()
      startRoomPolling()
    } else {
      joinError.value = data.message || '房间码无效或房间不存在，请检查后重试'
    }
  } catch (e) {
    joinError.value = '网络错误，请重试'
  } finally {
    joining.value = false
  }
}

// ===== 创建房间 =====
async function doCreateRoom() {
  createError.value = ''
  showCreateDialog.value = false
  isHost.value = true
  roomReady.value = false
  joiningRoom.value = false
  joinError.value = ''

  try {
    const data = await request.post('/pk/room', {
      action: 'create',
      isPublic: createIsPublic.value,
      difficulty: selectedDifficulty.value,
    })
    if (data.success) {
      creatingRoom.value = true
      roomCode.value = data.roomCode
      await loadMyRoom()
      startRoomPolling()
      loadRoomList(1)
    } else {
      createError.value = data.message || '创建房间失败'
      creatingRoom.value = false
    }
  } catch (e) {
    createError.value = '网络错误，请重试'
    creatingRoom.value = false
  }
}

// ===== 加入房间（通过悬浮输入框） =====
function startJoinRoom() {
  showCodeOverlay.value = true
  joinError.value = ''
  codeDigits.value = ['', '', '', '']
  nextTick(() => codeInputs.value[0]?.focus())
}

function onCodeInput(i) {
  codeDigits.value[i] = codeDigits.value[i].replace(/[^0-9]/g, '')
  if (codeDigits.value[i] && i < 3) codeInputs.value[i + 1]?.focus()
}

function onCodeBackspace(i) {
  if (!codeDigits.value[i] && i > 0) codeInputs.value[i - 1]?.focus()
}

async function doJoinRoom() {
  const code = codeDigits.value.join('')
  if (code.length !== 4) return
  await doJoinRoomWithCode(code)
}

// ===== 我的房间 =====
async function loadMyRoom() {
  try {
    const data = await request.post('/pk/room', { action: 'myroom' })
    if (data.success && data.hasRoom) {
      myRoom.value = {
        hasRoom: true,
        roomCode: data.roomCode,
        isHost: data.isHost,
        isPublic: data.isPublic,
        isReady: data.isReady,
        difficulty: data.difficulty,
        status: data.status,
      }
      roomCode.value = data.roomCode
      isHost.value = data.isHost
      roomReady.value = data.isReady
      if (data.isReady && !data.isHost) joiningRoom.value = true
      if (data.isHost) creatingRoom.value = true
    } else {
      myRoom.value = null
    }
  } catch (e) { myRoom.value = null }
}

async function disbandMyRoom() {
  try {
    await request.post('/pk/room', { action: 'disband' })
  } catch (e) {}
  myRoom.value = null
  creatingRoom.value = false
  roomCode.value = ''
  roomReady.value = false
  isHost.value = false
  stopRoomPolling()
  loadRoomList(roomPage.value)
}

async function updateRoomDifficulty(diffKey) {
  selectedDifficulty.value = diffKey
  try {
    const data = await request.post('/pk/room', {
      action: 'update',
      roomCode: roomCode.value,
      difficulty: diffKey,
    })
    if (data.success) {
      await loadMyRoom()
    }
  } catch (e) {}
}

async function leaveMyRoom() {
  try {
    await request.post('/pk/room', { action: 'leave', roomCode: roomCode.value })
  } catch (e) {}
  myRoom.value = null
  joiningRoom.value = false
  roomCode.value = ''
  roomReady.value = false
  isHost.value = false
  stopRoomPolling()
}

// ===== 轮询房间状态 =====
function startRoomPolling() {
  stopRoomPolling()
  roomPollTimer = setInterval(async () => {
    if (!roomCode.value) return
    try {
      const data = await request.get('/pk/room', { params: { roomCode: roomCode.value } })
      if (!data.success || !data.exists) {
        if (!isHost.value) {
          joinError.value = '房间已解散'
          joiningRoom.value = false
          roomReady.value = false
          roomCode.value = ''
        }
        stopRoomPolling()
        return
      }
      // 检测对手加入
      if (isHost.value && data.isReady && !roomReady.value) {
        roomReady.value = true
      }
      // 检测游戏开始
      if (data.status === 'PLAYING' && pkState.value !== 'playing') {
        stopRoomPolling()
        if (isHost.value) {
          // host 已经通过 startGame 进入
        } else {
          await joinGameAsGuest()
        }
      }
      // 检测游戏结束
      if (data.status === 'FINISHED' && pkState.value === 'playing') {
        stopRoomPolling()
        stopOpponentPolling()
        quitBySomeone.value = true
        quitByMe.value = false
        await showGameResult()
      }
      // 如果 guest 等待中且 roomReady 但游戏未开始
      if (!isHost.value && data.isReady && data.status === 'WAITING') {
        roomReady.value = true
      }
    } catch (e) { /* 静默 */ }
  }, 2000)
}

function stopRoomPolling() {
  if (roomPollTimer) { clearInterval(roomPollTimer); roomPollTimer = null }
}

function stopOpponentPolling() {
  if (opponentPollTimer) { clearInterval(opponentPollTimer); opponentPollTimer = null }
}

async function cancelRoom() {
  await disbandMyRoom()
}

async function leaveLobby() {
  if (myRoom.value?.isHost) await disbandMyRoom()
  else if (myRoom.value?.hasRoom) await leaveMyRoom()
  stopRoomPolling()
  stopLobbyRefresh()
  joiningRoom.value = false
  showCodeOverlay.value = false
  showCreateDialog.value = false
  pkState.value = null
}

// ========== 答题系统 ==========
const currentQuestion = ref(0)
const totalQuestions = ref(20)
const answered = ref(false)
const selectedOption = ref(-1)
const myCorrect = ref(0)
const opponentCorrect = ref(0)
const myAnswered = ref(0)
const opponentAnswered = ref(0)
const myTimeSec = ref(0)
const opponentTimeSec = ref(0)
const myFinished = ref(false)
const bothFinished = ref(false)
const feedbackText = ref('')
const feedbackCorrect = ref(true)
const allQuestions = ref([])
const questionTimeLeft = ref(15)
let questionTimer = null
let questionStartTime = 0

// 退出相关
const showQuitConfirm = ref(false)
const quitByMe = ref(false)
const quitBySomeone = ref(false)
const gameActive = ref(false)

const currentQ = computed(() => {
  if (allQuestions.value.length === 0) return null
  return allQuestions.value[currentQuestion.value]
})

// ===== 开始游戏（房主） =====
async function startGame() {
  if (!roomReady.value) return
  try {
    const data = await request.post('/pk/game', { action: 'start', roomCode: roomCode.value, difficulty: selectedDifficulty.value })
    if (data.success && data.questions) {
      setupGame(data.questions)
      startOpponentPolling()
    } else {
      alert(data.message || '开始游戏失败')
    }
  } catch (e) {
    alert('网络错误，无法开始游戏')
  }
}

// ===== 加入游戏（guest） =====
async function joinGameAsGuest() {
  try {
    const data = await request.post('/pk/game', { action: 'questions', roomCode: roomCode.value })
    if (data.success && data.questions) {
      setupGame(data.questions)
      startOpponentPolling()
    }
  } catch (e) {
    console.error('加入游戏失败:', e)
  }
}

function setupGame(questions) {
  allQuestions.value = questions.map(q => ({
    ...q,
    correct: q.correctIndex !== undefined ? q.correctIndex : 0,
  }))
  // 同步难度标签
  if (myRoom.value?.difficulty) {
    selectedDifficulty.value = myRoom.value.difficulty
  }
  totalQuestions.value = allQuestions.value.length
  currentQuestion.value = 0
  myCorrect.value = 0
  opponentCorrect.value = 0
  myAnswered.value = 0
  opponentAnswered.value = 0
  myTimeSec.value = 0
  opponentTimeSec.value = 0
  myFinished.value = false
  bothFinished.value = false
  answered.value = false
  selectedOption.value = -1
  quitByMe.value = false
  quitBySomeone.value = false
  showQuitConfirm.value = false
  gameActive.value = true
  pkState.value = 'playing'
  startQuestionTimer()
}

// ===== 单题倒计时 =====
function startQuestionTimer() {
  stopQuestionTimer()
  questionTimeLeft.value = 15
  questionStartTime = Date.now()
  questionTimer = setInterval(() => {
    if (!gameActive.value || myFinished.value) {
      stopQuestionTimer()
      return
    }
    questionTimeLeft.value--
    if (questionTimeLeft.value <= 0) {
      // 超时自动跳到下一题
      handleTimeout()
    }
  }, 1000)
}

function stopQuestionTimer() {
  if (questionTimer) { clearInterval(questionTimer); questionTimer = null }
}

function handleTimeout() {
  if (answered.value || !gameActive.value) return
  const elapsed = Date.now() - questionStartTime
  // 超时视为答错
  answered.value = true
  selectedOption.value = -1
  feedbackCorrect.value = false
  const q = allQuestions.value[currentQuestion.value]
  feedbackText.value = q ? `⏰ 时间到！正确答案是：${q.options[q.correct]}` : '⏰ 时间到！'

  submitAnswer(currentQuestion.value, -1, elapsed)

  // 2秒后自动跳转
  setTimeout(() => {
    if (!gameActive.value) return
    if (currentQuestion.value < totalQuestions.value - 1) {
      nextQuestion()
    } else {
      finishCurrentPlayer()
    }
  }, 2000)
}

// ===== 选择答案 =====
function selectAnswer(idx) {
  if (answered.value || !gameActive.value || myFinished.value) return
  stopQuestionTimer()
  const elapsed = Date.now() - questionStartTime
  answered.value = true
  selectedOption.value = idx

  const q = allQuestions.value[currentQuestion.value]
  if (!q) return
  const isCorrect = (idx === q.correct)

  if (isCorrect) {
    myCorrect.value++
    feedbackCorrect.value = true
    feedbackText.value = '✓ 回答正确！'
  } else {
    feedbackCorrect.value = false
    feedbackText.value = `✗ 回答错误，正确答案是：${q.options[q.correct]}`
  }

  myAnswered.value = currentQuestion.value + 1

  // 提交答案（发送 selectedOption，服务端校验）
  submitAnswer(currentQuestion.value, idx, elapsed)

  // 2秒后跳转或结束
  setTimeout(() => {
    if (!gameActive.value) return
    if (currentQuestion.value < totalQuestions.value - 1) {
      nextQuestion()
    } else {
      finishCurrentPlayer()
    }
  }, 2000)
}

async function submitAnswer(qIdx, selectedOpt, elapsedMs) {
  try {
    const data = await request.post('/pk/game', {
      action: 'answer', roomCode: roomCode.value,
      questionIndex: qIdx, selectedOption: selectedOpt, elapsedMs,
    })
    if (data.success) {
      myFinished.value = data.myFinished || false
      bothFinished.value = data.bothFinished || false
      if (data.bothFinished) {
        stopOpponentPolling()
        stopQuestionTimer()
        gameActive.value = false
        setTimeout(() => showGameResult(), 500)
      }
    }
  } catch (e) { console.error('提交答案失败:', e) }
}

function finishCurrentPlayer() {
  myFinished.value = true
  stopQuestionTimer()
  // 服务器已在最后一题提交时自动标记完成，无需额外通知
}

// ===== 对手轮询 =====
function startOpponentPolling() {
  stopOpponentPolling()
  opponentPollTimer = setInterval(async () => {
    if (!roomCode.value) { stopOpponentPolling(); return }
    try {
      const data = await request.post('/pk/game', { action: 'opponent', roomCode: roomCode.value })
      if (data.success) {
        opponentCorrect.value = data.opponentCorrect || 0
        opponentAnswered.value = data.opponentAnswered || 0
        opponentTimeSec.value = data.opponentTime || 0
        myCorrect.value = data.myCorrect || 0
        myAnswered.value = data.myAnswered || 0
        myTimeSec.value = data.myTime || 0
        bothFinished.value = data.bothFinished || false

        if (data.status === 'FINISHED' && !bothFinished.value) {
          // 对手退出
          stopOpponentPolling()
          stopQuestionTimer()
          quitBySomeone.value = true
          quitByMe.value = false
          gameActive.value = false
          await showGameResult()
        }
        if (data.bothFinished) {
          stopOpponentPolling()
          stopQuestionTimer()
          gameActive.value = false
          await showGameResult()
        }
      }
    } catch (e) { /* 静默 */ }
  }, 2000)
}

// ===== 答题 UI =====
function getOptionClass(idx) {
  if (!answered.value) return 'border-gray-200 hover:border-[#2563EB] hover:bg-blue-50 cursor-pointer'
  const q = allQuestions.value[currentQuestion.value]
  if (!q) return ''
  const correct = q.correct
  if (idx === correct) return 'border-green-400 bg-green-50 text-green-700'
  if (idx === selectedOption.value && idx !== correct) return 'border-red-400 bg-red-50 text-red-700'
  return 'border-gray-100 text-gray-300'
}

function getOptionDotClass(idx) {
  if (!answered.value) return 'bg-gray-100 text-gray-500'
  const q = allQuestions.value[currentQuestion.value]
  if (!q) return ''
  const correct = q.correct
  if (idx === correct) return 'bg-green-500 text-white'
  if (idx === selectedOption.value && idx !== correct) return 'bg-red-500 text-white'
  return 'bg-gray-100 text-gray-400'
}

function nextQuestion() {
  currentQuestion.value++
  answered.value = false
  selectedOption.value = -1
  feedbackText.value = ''
  startQuestionTimer()
}

// ===== 结果 =====
async function showGameResult() {
  try {
    const data = await request.post('/pk/game', { action: 'result', roomCode: roomCode.value })
    if (data.success) {
      myCorrect.value = data.myCorrect || 0
      opponentCorrect.value = data.opponentCorrect || 0
      myTimeSec.value = data.myTime || 0
      opponentTimeSec.value = data.opponentTime || 0
      bothFinished.value = true
    }
  } catch (e) { /* 使用本地数据 */ }
  pkState.value = 'result'
}

async function confirmQuit() {
  showQuitConfirm.value = false
  gameActive.value = false
  stopQuestionTimer()
  stopOpponentPolling()
  quitByMe.value = true
  quitBySomeone.value = true
  try { await request.post('/pk/game', { action: 'quit', roomCode: roomCode.value }) } catch (e) {}
  pkState.value = 'result'
}

// 再来一局
async function playAgain() {
  stopRoomPolling()
  stopOpponentPolling()
  // 保留房间，重置状态
  try {
    // 作为房主才能重新开始
    if (isHost.value) {
      // 重置游戏状态
      try { await request.post('/pk/room', { action: 'leave', roomCode: roomCode.value }) } catch (e) {}
    }
  } catch (e) {}
  resetRoom()
  pkState.value = 'lobby'
  loadRoomList(1)
}

function resetRoom() {
  stopRoomPolling()
  stopOpponentPolling()
  stopQuestionTimer()
  creatingRoom.value = false
  joiningRoom.value = false
  roomCode.value = ''
  roomReady.value = false
  isHost.value = false
  joinError.value = ''
  codeDigits.value = ['', '', '', '']
  quitByMe.value = false
  quitBySomeone.value = false
  gameActive.value = false
  myFinished.value = false
  bothFinished.value = false
  showCreateDialog.value = false
  showCodeOverlay.value = false
  allQuestions.value = []
  myRoom.value = null
  createError.value = ''
}

// ===== 计算属性 =====
const resultEmoji = computed(() => {
  if (quitByMe.value) return '🏳️'
  if (quitBySomeone.value && !quitByMe.value) return '🏆'
  if (myCorrect.value > opponentCorrect.value) return '🏆'
  if (myCorrect.value < opponentCorrect.value) return '😢'
  return '🤝'
})

const resultTitle = computed(() => {
  if (quitByMe.value) return '你已退出对战'
  if (quitBySomeone.value && !quitByMe.value) return '对方已退出'
  if (myCorrect.value > opponentCorrect.value) return '恭喜获胜！'
  if (myCorrect.value < opponentCorrect.value) return '惜败一局'
  if (myTimeSec.value < opponentTimeSec.value) return '恭喜获胜！（耗时更短）'
  if (myTimeSec.value > opponentTimeSec.value) return '惜败一局（对手更快）'
  return '势均力敌！'
})

const resultSub = computed(() => {
  if (quitByMe.value) return '因中途退出对战，你被判为失败'
  if (quitBySomeone.value && !quitByMe.value) return '对方中途退出对战，你自动获胜'
  if (myCorrect.value > opponentCorrect.value) return `你以 ${myCorrect.value}:${opponentCorrect.value} 赢得比赛`
  if (myCorrect.value < opponentCorrect.value) return `对手以 ${opponentCorrect.value}:${myCorrect.value} 赢得比赛`
  const myT = myTimeSec.value, opT = opponentTimeSec.value
  if (myT < opT) return `正确数相同，你耗时更短（${myT}s vs ${opT}s）获胜`
  if (myT > opT) return `正确数相同，对手耗时更短（${opT}s vs ${myT}s）获胜`
  return `正确数和耗时均相同（${myT}s），平局！`
})

watch(rpState, (val) => { if (val) nextTick(() => window.scrollTo(0, 0)) })
watch(wcModeSelect, (val) => { if (val) nextTick(() => window.scrollTo(0, 0)) })
watch(wcPlaying, (val) => { if (val) nextTick(() => window.scrollTo(0, 0)) })
watch(pkState, (val) => {
  if (val) {
    nextTick(() => window.scrollTo(0, 0))
    if (val === 'lobby') {
      loadRoomList(1)
      loadMyRoom()
      startLobbyRefresh()
    } else {
      stopLobbyRefresh()
    }
  }
})

// ===== 页面加载时检测是否有活跃房间 =====
onMounted(async () => {
  try {
    const data = await request.post('/pk/room', { action: 'myroom' })
    if (data.success && data.hasRoom) {
      // 有活跃房间，自动跳转到大厅
      pkState.value = 'lobby'
    }
  } catch (e) { /* 静默 */ }
})

// ===== 大厅5秒自动刷新 =====
function startLobbyRefresh() {
  stopLobbyRefresh()
  lobbyRefreshTimer = setInterval(async () => {
    if (pkState.value !== 'lobby') { stopLobbyRefresh(); return }
    roomRefreshing.value = true
    await Promise.all([loadMyRoom(), loadRoomList(roomPage.value)])
    roomRefreshing.value = false
  }, 5000)
}
function stopLobbyRefresh() {
  if (lobbyRefreshTimer) { clearInterval(lobbyRefreshTimer); lobbyRefreshTimer = null }
  roomRefreshing.value = false
}
</script>