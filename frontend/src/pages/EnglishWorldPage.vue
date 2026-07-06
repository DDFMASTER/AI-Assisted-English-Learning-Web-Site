<template>
  <main class="max-w-[1000px] mx-auto px-6 mt-10 pb-8">
    <h1 class="text-3xl font-bold mb-2">英语天地</h1>
    <p class="text-gray-500 mb-10">在游戏中提升你的英语水平</p>

    <!-- ====== 游戏列表 ====== -->
    <template v-if="!pkState && !rpState && !wcPlaying && !wcModeSelect">
      <div class="space-y-5">
        <!-- 角色扮演 -->
        <div
          class="card cursor-pointer hover:shadow-2xl hover:-translate-y-1 transition-all group overflow-hidden p-0"
          @click="rpState = 'scene-select'"
        >
          <!-- 图片区 -->
          <div class="relative h-44 overflow-hidden">
            <img src="/photo/角色扮演图片 (1).png" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" alt="角色扮演" />
            <div class="absolute bottom-0 left-0 right-0 h-20 bg-gradient-to-b from-transparent to-white/85 pointer-events-none"></div>
          </div>
          <!-- 文字区 -->
          <div class="px-6 pb-5 pt-2">
            <h3 class="text-2xl font-bold mb-1.5 group-hover:text-purple-600 transition-colors">角色扮演</h3>
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
            <img src="/photo/真人pk图片 (2).png" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" alt="真人PK" />
            <div class="absolute bottom-0 left-0 right-0 h-20 bg-gradient-to-b from-transparent to-white/85 pointer-events-none"></div>
          </div>
          <div class="px-6 pb-5 pt-2">
            <h3 class="text-2xl font-bold mb-1.5 group-hover:text-red-500 transition-colors">真人PK</h3>
            <p class="text-sm text-gray-600 leading-relaxed max-w-2xl mb-3">与好友实时对战，比拼词汇量和答题速度。创建房间邀请好友加入，选择难度后开始答题挑战，10道选择题一决胜负，看谁的正确率更高！</p>
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
            <img src="/photo/单词接龙图片(3).png" class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" alt="单词接龙" />
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
      <div class="max-w-lg mx-auto">
        <button class="text-gray-400 hover:text-gray-600 mb-6 flex items-center gap-1 text-sm" @click="leaveLobby">
          <Icon icon="ph:arrow-left-bold" /> 返回游戏列表
        </button>

        <div class="card text-center relative overflow-hidden">
          <!-- 背景装饰 -->
          <div class="absolute top-0 left-0 right-0 h-32 bg-gradient-to-b from-red-50 to-transparent pointer-events-none"></div>
          <div class="absolute -top-10 -right-10 w-40 h-40 rounded-full bg-red-100/50 pointer-events-none"></div>
          <div class="relative z-10">
            <div class="w-24 h-24 mx-auto mb-4 rounded-2xl bg-gradient-to-br from-red-400 to-pink-500 flex items-center justify-center shadow-lg shadow-red-200">
              <Icon icon="ph:trophy-bold" class="text-white text-5xl" />
            </div>
            <h2 class="text-2xl font-bold mb-2">真人PK</h2>
            <p class="text-sm text-gray-400 mb-6">与好友实时对战，比拼词汇量</p>

            <div class="space-y-3">
              <button
                class="w-full py-4 rounded-xl bg-gradient-to-r from-red-400 to-pink-500 text-white font-bold text-lg hover:shadow-lg hover:shadow-red-200 transition-all active:scale-[0.98]"
                @click="startCreateRoom"
              >
                <Icon icon="ph:plus-circle-bold" class="inline mr-2" />创建房间
              </button>
              <button
                class="w-full py-4 rounded-xl border-2 border-gray-200 text-gray-600 font-bold text-lg hover:border-red-300 hover:text-red-500 hover:bg-red-50 transition-all active:scale-[0.98]"
                @click="startJoinRoom"
              >
                <Icon icon="ph:sign-in-bold" class="inline mr-2" />加入房间
              </button>
            </div>
          </div>
        </div>

        <!-- 创建房间 -->
        <div v-if="creatingRoom" class="card mt-4 text-center">
          <h3 class="font-bold mb-4">创建房间</h3>
          <p class="text-sm text-gray-400 mb-4">房间码已生成，等待好友输入房间码加入</p>
          <div class="text-5xl font-bold tracking-[0.3em] text-[#2563EB] mb-6">{{ roomCode }}</div>
          <div class="flex items-center justify-center gap-2 mb-4">
            <span class="w-3 h-3 rounded-full animate-pulse" :class="roomReady ? 'bg-green-500' : 'bg-yellow-500'"></span>
            <span class="text-sm" :class="roomReady ? 'text-green-600 font-bold' : 'text-gray-400'">
              {{ roomReady ? '双方已就位，请选择难度并开始！' : '等待另一位玩家加入...' }}
            </span>
          </div>

          <!-- 房主选择难度（仅双方就位后显示） -->
          <div v-if="roomReady" class="mb-4">
            <p class="text-sm font-bold text-gray-600 mb-3">选择难度</p>
            <div class="flex justify-center gap-2 flex-wrap">
              <button
                v-for="d in difficulties"
                :key="d.key"
                class="px-4 py-2 rounded-xl text-sm font-bold transition-all"
                :class="selectedDifficulty === d.key
                  ? 'bg-[#2563EB] text-white shadow-lg'
                  : 'bg-gray-100 text-gray-500 hover:bg-gray-200'"
                @click="selectedDifficulty = d.key"
              >{{ d.label }}</button>
            </div>
          </div>

          <button
            v-if="roomReady"
            class="mt-4 w-full py-3 rounded-xl bg-gradient-to-r from-red-400 to-pink-500 text-white font-bold hover:shadow-lg transition-all disabled:opacity-30"
            @click="startGame"
          >
            <Icon icon="ph:play-circle-bold" class="inline mr-1" />开始对战
          </button>
          <button
            v-if="!roomReady"
            class="mt-4 w-full py-3 rounded-xl bg-red-50 text-red-400 font-bold hover:bg-red-100 transition-all"
            @click="cancelRoom"
          >
            取消房间
          </button>
        </div>

        <!-- 加入房间 -->
        <div v-if="joiningRoom" class="card mt-4 text-center">
          <h3 class="font-bold mb-4">加入房间</h3>
          <p class="text-sm text-gray-400 mb-4">输入好友分享的4位房间码</p>
          <div class="flex justify-center gap-2 mb-4">
            <input
              v-for="(_, i) in 4"
              :key="i"
              :ref="el => { if (el) codeInputs[i] = el }"
              v-model="codeDigits[i]"
              type="text"
              inputmode="numeric"
              maxlength="1"
              class="w-14 h-16 text-center text-2xl font-bold border-2 border-gray-200 rounded-xl focus:border-[#2563EB] focus:outline-none transition-colors"
              @input="onCodeInput(i)"
              @keydown.backspace="onCodeBackspace(i)"
            />
          </div>
          <p v-if="joinError" class="text-xs text-red-400 mb-3">{{ joinError }}</p>
          <button
            class="w-full py-3 rounded-xl bg-gradient-to-r from-red-400 to-pink-500 text-white font-bold hover:shadow-lg transition-all disabled:opacity-30"
            :disabled="codeDigits.join('').length < 4 || joining"
            @click="doJoinRoom"
          >
            <span v-if="joining">加入中...</span>
            <span v-else>加入房间</span>
          </button>
          <button
            class="mt-3 w-full text-sm text-gray-400 hover:text-gray-600 transition-colors"
            @click="joiningRoom = false"
          >取消</button>
        </div>
      </div>
    </template>

    <!-- ====== PK 答题中 ====== -->
    <template v-if="pkState === 'playing'">
      <div class="max-w-2xl mx-auto">
        <div class="flex items-center justify-between mb-8">
          <button class="text-gray-400 hover:text-red-500 text-sm flex items-center gap-1 transition-colors" @click="showQuitConfirm = true">
            <Icon icon="ph:sign-out-bold" /> 退出对战
          </button>
          <div class="flex items-center gap-4">
            <span class="text-xs font-bold text-gray-400">题目 {{ currentQuestion + 1 }} / {{ totalQuestions }}</span>
            <span class="text-xs text-gray-300">{{ selectedDifficultyLabel }}</span>
          </div>
        </div>

        <!-- 进度条 -->
        <div class="w-full h-1.5 bg-gray-100 rounded-full mb-8">
          <div class="h-full bg-[#2563EB] rounded-full transition-all duration-500" :style="{ width: ((currentQuestion + 1) / totalQuestions * 100) + '%' }"></div>
        </div>

        <!-- 题目卡片 -->
        <div v-if="currentQ" class="card mb-8">
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
        </div>

        <!-- 下一题 / 查看结果 -->
        <div class="text-center">
          <button
            v-if="answered && currentQuestion < totalQuestions - 1"
            class="px-8 py-3 rounded-xl bg-[#2563EB] text-white font-bold hover:bg-blue-600 transition-all"
            @click="nextQuestion"
          >
            下一题
          </button>
          <button
            v-if="answered && currentQuestion >= totalQuestions - 1"
            class="px-8 py-3 rounded-xl bg-gradient-to-r from-red-400 to-pink-500 text-white font-bold hover:shadow-lg transition-all"
            @click="finishGame"
          >
            查看结果
          </button>
        </div>
      </div>

      <!-- 退出确认弹窗 -->
      <Teleport to="body">
        <div
          v-if="showQuitConfirm"
          class="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 backdrop-blur-sm"
          @click.self="showQuitConfirm = false"
        >
          <div class="bg-white rounded-2xl shadow-2xl p-6 w-full max-w-sm mx-4 text-center">
            <Icon icon="ph:warning-circle-bold" class="text-5xl text-yellow-500 mx-auto mb-3" />
            <h3 class="text-lg font-bold mb-2">确认退出对战</h3>
            <p class="text-sm text-gray-500 mb-6">
              中途退出将被视为<strong class="text-red-500">失败</strong>，对方将<strong class="text-green-500">自动获胜</strong>。确定要退出吗？
            </p>
            <div class="flex gap-3">
              <button
                class="flex-1 py-3 rounded-xl bg-gray-100 text-gray-600 font-bold hover:bg-gray-200 transition-all"
                @click="showQuitConfirm = false"
              >继续答题</button>
              <button
                class="flex-1 py-3 rounded-xl bg-red-500 text-white font-bold hover:bg-red-600 transition-all"
                @click="confirmQuit"
              >确认退出</button>
            </div>
          </div>
        </div>
      </Teleport>
    </template>

    <!-- ====== PK 结果 ====== -->
    <template v-if="pkState === 'result'">
      <div class="max-w-md mx-auto card text-center">
        <div class="text-6xl mb-4">{{ resultEmoji }}</div>
        <h2 class="text-2xl font-bold mb-2">{{ resultTitle }}</h2>
        <p class="text-gray-500 mb-6">{{ resultSub }}</p>

        <!-- 中途退出提示 -->
        <div v-if="quitBySomeone" class="mb-6 p-3 rounded-xl text-sm font-bold" :class="quitByMe ? 'bg-red-50 text-red-600' : 'bg-green-50 text-green-600'">
          {{ quitByMe ? '你因中途退出对战而被判失败' : '对方中途退出对战，你自动获胜' }}
        </div>

        <!-- 正常完成时的分数 -->
        <div v-if="!quitBySomeone" class="flex justify-center gap-8 mb-8">
          <div class="text-center">
            <div class="text-3xl font-bold text-[#2563EB]">{{ myCorrect }}/{{ totalQuestions }}</div>
            <div class="text-xs text-gray-400">我 · {{ Math.round(myCorrect / totalQuestions * 100) }}%</div>
          </div>
          <div class="w-px bg-gray-200"></div>
          <div class="text-center">
            <div class="text-3xl font-bold text-red-400">{{ opponentCorrect }}/{{ totalQuestions }}</div>
            <div class="text-xs text-gray-400">对手 · {{ Math.round(opponentCorrect / totalQuestions * 100) }}%</div>
          </div>
        </div>

        <div class="flex gap-3">
          <button
            class="flex-1 py-3 rounded-xl bg-gray-100 text-gray-600 font-bold hover:bg-gray-200 transition-all"
            @click="pkState = 'lobby'; resetRoom()"
          >
            再来一局
          </button>
          <button
            class="flex-1 py-3 rounded-xl bg-[#2563EB] text-white font-bold hover:bg-blue-600 transition-all"
            @click="pkState = null; resetRoom()"
          >
            返回首页
          </button>
        </div>
      </div>
    </template>

    <!-- ====== 角色扮演 - 场景选择 ====== -->
    <template v-if="rpState === 'scene-select'">
      <div class="max-w-2xl mx-auto">
        <button class="text-gray-400 hover:text-gray-600 mb-6 flex items-center gap-1 text-sm" @click="rpState = null">
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
              <h3 class="text-lg font-bold mb-1 group-hover:text-[#2563EB] transition-colors">{{ scene.label }}</h3>
              <p class="text-xs text-gray-400 leading-relaxed">{{ scene.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ====== 角色扮演 - 对话界面 ====== -->
    <template v-if="rpState === 'chatting'">
      <div class="max-w-3xl mx-auto">
        <div class="flex items-center justify-between mb-4">
          <button class="text-gray-400 hover:text-gray-600 text-sm flex items-center gap-1" @click="rpState = 'scene-select'; resetRP()">
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
            <div class="card sticky top-28">
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
          </div>
        </div>
      </div>
    </template>

    <!-- ====== 单词接龙 - 模式选择 ====== -->
    <template v-if="wcModeSelect">
      <div class="max-w-lg mx-auto">
        <button class="text-gray-400 hover:text-gray-600 mb-6 flex items-center gap-1 text-sm" @click="wcModeSelect = false">
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
      <div class="max-w-lg mx-auto">
        <button class="text-gray-400 hover:text-gray-600 mb-4 flex items-center gap-1 text-sm" @click="quitWordChain">
          <Icon icon="ph:arrow-left-bold" /> 退出游戏
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
          <button class="text-xs text-gray-400 hover:text-gray-600 transition-colors" @click="endWordChain">结束接龙</button>
        </div>

        <!-- 游戏结束 -->
        <div v-if="wcGameOver" class="card text-center mt-4">
          <div class="text-5xl mb-3">🎉</div>
          <h3 class="text-xl font-bold mb-2">接龙结束！</h3>
          <p class="text-lg text-gray-500 mb-1">{{ wcMode === 'endless' ? '无尽模式下，你和AI共完成了' : '5分钟内，你和AI共完成了' }}</p>
          <p class="text-4xl font-extrabold text-[#2563EB] mb-1">{{ wcChain.length }} 个单词</p>
          <p class="text-sm text-gray-400 mb-4">其中你贡献了 <strong class="text-green-600">{{ wcUserCount }}</strong> 个单词</p>
          <div v-if="wcChain.length > 0" class="flex flex-wrap justify-center gap-1.5 mb-6">
            <span v-for="(item, i) in wcChain" :key="i"
              class="px-2.5 py-1 rounded-lg text-xs font-bold"
              :class="item.role === 'ai' ? 'bg-purple-50 text-purple-700' : 'bg-green-50 text-green-700'"
            >{{ item.word }}</span>
          </div>
          <button class="px-8 py-3 rounded-xl bg-[#2563EB] text-white font-bold hover:bg-blue-600 transition-all" @click="quitWordChain">
            返回游戏列表
          </button>
        </div>
      </div>
    </template>

  </main>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { Icon } from '@iconify/vue'
import request from '@/utils/request'

const pkState = ref(null) // null | 'lobby' | 'playing' | 'result'

// ========== 角色扮演系统 ==========
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
  { key: 'shopping', label: '🛍️ 购物', emoji: '🛍️', desc: '在商店中与店员交流，选购商品、讨价还价、结账付款' },
  { key: 'travel', label: '✈️ 旅游', emoji: '✈️', desc: '旅行中的各种场景：酒店入住、景点咨询、购买门票、问路指引' },
  { key: 'ordering', label: '🍽️ 点餐', emoji: '🍽️', desc: '在餐厅点餐，与服务员交流菜品、特殊要求、付款结账' },
  { key: 'interview', label: '💼 职场面试', emoji: '💼', desc: '模拟求职面试，与HR和面试官进行英语面试对话' },
]

const currentSceneLabel = computed(() => {
  const s = scenes.find(s => s.key === rpScene.value)
  return s ? s.label : ''
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
const wcTimeLeft = ref(300)    // 标准模式：300秒总时长；无尽模式：8秒每轮
const wcLetterChanged = ref(false)
const wcTurn = ref('user')
const wcInputRef = ref(null)
let wcTimer = null

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
    }, { timeout: 15000 })

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
        }, { timeout: 15000 })

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

function endWordChain() {
  clearInterval(wcTimer)
  wcGameOver.value = true
  wcTurn.value = 'user'
  wcLoading.value = false
}

function quitWordChain() {
  clearInterval(wcTimer)
  wcPlaying.value = false
  wcGameOver.value = false
}

// ========== 房间系统 ==========
const creatingRoom = ref(false)
const joiningRoom = ref(false)
const roomCode = ref('')
const isHost = ref(false)
const roomReady = ref(false)
const joinError = ref('')
const joining = ref(false)

// 房间码输入
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

function startCreateRoom() {
  creatingRoom.value = true
  joiningRoom.value = false
  joinError.value = ''
  isHost.value = true
  roomReady.value = false
  // 生成4位随机房间码
  roomCode.value = String(Math.floor(1000 + Math.random() * 9000))
}

function startJoinRoom() {
  joiningRoom.value = true
  creatingRoom.value = false
  joinError.value = ''
  codeDigits.value = ['', '', '', '']
  isHost.value = false
  roomReady.value = false
  nextTick(() => codeInputs.value[0]?.focus())
}

function onCodeInput(i) {
  codeDigits.value[i] = codeDigits.value[i].replace(/[^0-9]/g, '')
  if (codeDigits.value[i] && i < 3) {
    codeInputs.value[i + 1]?.focus()
  }
}

function onCodeBackspace(i) {
  if (!codeDigits.value[i] && i > 0) {
    codeInputs.value[i - 1]?.focus()
  }
}

async function doJoinRoom() {
  const code = codeDigits.value.join('')
  joining.value = true
  joinError.value = ''
  await new Promise(r => setTimeout(r, 800))
  // 模拟：检查房间码（需与当前已创建的房间码匹配）
  if (roomCode.value && code === roomCode.value) {
    roomReady.value = true
    joiningRoom.value = false
    isHost.value = false
    // 在创建房间那边也同步更新
  } else {
    joinError.value = '房间码无效或房间不存在，请检查后重试'
  }
  joining.value = false
}

function cancelRoom() {
  creatingRoom.value = false
  roomCode.value = ''
  roomReady.value = false
  isHost.value = false
}

function leaveLobby() {
  cancelRoom()
  joiningRoom.value = false
  pkState.value = null
}

// ========== 答题系统 ==========
const currentQuestion = ref(0)
const totalQuestions = 10
const answered = ref(false)
const selectedOption = ref(-1)
const myCorrect = ref(0)
const opponentCorrect = ref(0)
const feedbackText = ref('')
const feedbackCorrect = ref(true)
const allQuestions = ref([])

// 退出相关
const showQuitConfirm = ref(false)
const quitByMe = ref(false)
const quitBySomeone = ref(false)
const gameActive = ref(false)

function generateQuestions(difficulty) {
  const mockWords = {
    junior: [
      { word: 'abandon', options: ['放弃', '获得', '开始', '结束'], correct: 0 },
      { word: 'ability', options: ['能力', '残疾', '可能', '责任'], correct: 0 },
      { word: 'abroad', options: ['国内', '国外', '船上', '车上'], correct: 1 },
      { word: 'accept', options: ['拒绝', '接受', '发送', '等待'], correct: 1 },
      { word: 'accident', options: ['意图', '计划', '事故', '成功'], correct: 2 },
      { word: 'achieve', options: ['失去', '达到', '开始', '停止'], correct: 1 },
      { word: 'active', options: ['消极的', '积极的', '安静的', '快速的'], correct: 1 },
      { word: 'actual', options: ['虚拟的', '实际的', '理论的', '假设的'], correct: 1 },
      { word: 'admire', options: ['厌恶', '羡慕', '忽视', '批评'], correct: 1 },
      { word: 'adventure', options: ['冒险', '安全', '平凡', '休息'], correct: 0 },
    ],
    cet4: [
      { word: 'abandon', options: ['放弃', '获得', '开始', '结束'], correct: 0 },
      { word: 'elaborate', options: ['简略的', '精心制作的', '模糊的', '过时的'], correct: 1 },
      { word: 'fluctuate', options: ['稳定', '波动', '上升', '下降'], correct: 1 },
      { word: 'genuine', options: ['伪造的', '真正的', '临时的', '昂贵的'], correct: 1 },
      { word: 'hypothesis', options: ['结论', '事实', '假设', '证据'], correct: 2 },
      { word: 'inevitable', options: ['可避免的', '不可避免的', '随机的', '罕见的'], correct: 1 },
      { word: 'jeopardize', options: ['保护', '危及', '改善', '忽视'], correct: 1 },
      { word: 'negotiate', options: ['拒绝', '接受', '谈判', '发送'], correct: 2 },
      { word: 'perspective', options: ['图片', '计划', '观点', '比例'], correct: 2 },
      { word: 'substantial', options: ['微小的', '大量的', '临时的', '表面的'], correct: 1 },
    ],
    cet6: [
      { word: 'ambiguous', options: ['清晰的', '模糊的', '简短的', '冗长的'], correct: 1 },
      { word: 'benevolent', options: ['恶意的', '仁慈的', '冷漠的', '贪婪的'], correct: 1 },
      { word: 'conscientious', options: ['粗心的', '认真的', '懒惰的', '傲慢的'], correct: 1 },
      { word: 'deteriorate', options: ['改善', '恶化', '稳定', '波动'], correct: 1 },
      { word: 'extravagant', options: ['节俭的', '奢侈的', '普通的', '简朴的'], correct: 1 },
      { word: 'formidable', options: ['容易的', '可怕的', '微小的', '普通的'], correct: 1 },
      { word: 'gregarious', options: ['孤僻的', '群居的', '安静的', '害羞的'], correct: 1 },
      { word: 'homogeneous', options: ['多样的', '同质的', '异质的', '混合的'], correct: 1 },
      { word: 'indigenous', options: ['外来的', '本土的', '进口的', '出口的'], correct: 1 },
      { word: 'meticulous', options: ['粗心的', '一丝不苟的', '懒惰的', '随意的'], correct: 1 },
    ],
  }

  const words = mockWords[difficulty] || mockWords.cet4
  return words.slice(0, totalQuestions).map(q => {
    const correctText = q.options[q.correct]
    const shuffled = shuffleArray([...q.options])
    const newCorrect = shuffled.indexOf(correctText)
    return { word: q.word, options: shuffled, correct: newCorrect }
  })
}

function shuffleArray(arr) {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]]
  }
  return arr
}

const currentQ = computed(() => {
  if (allQuestions.value.length === 0) return null
  return allQuestions.value[currentQuestion.value]
})

function selectAnswer(idx) {
  if (answered.value || !gameActive.value) return
  answered.value = true
  selectedOption.value = idx
  const correct = allQuestions.value[currentQuestion.value].correct
  if (idx === correct) {
    myCorrect.value++
    feedbackCorrect.value = true
    feedbackText.value = '✓ 回答正确！'
  } else {
    feedbackCorrect.value = false
    feedbackText.value = `✗ 回答错误，正确答案是：${allQuestions.value[currentQuestion.value].options[correct]}`
  }
  // 模拟对手作答
  if (Math.random() > 0.35) opponentCorrect.value++
}

function getOptionClass(idx) {
  if (!answered.value) return 'border-gray-200 hover:border-[#2563EB] hover:bg-blue-50 cursor-pointer'
  const correct = allQuestions.value[currentQuestion.value].correct
  if (idx === correct) return 'border-green-400 bg-green-50 text-green-700'
  if (idx === selectedOption.value && idx !== correct) return 'border-red-400 bg-red-50 text-red-700'
  return 'border-gray-100 text-gray-300'
}

function getOptionDotClass(idx) {
  if (!answered.value) return 'bg-gray-100 text-gray-500'
  const correct = allQuestions.value[currentQuestion.value].correct
  if (idx === correct) return 'bg-green-500 text-white'
  if (idx === selectedOption.value && idx !== correct) return 'bg-red-500 text-white'
  return 'bg-gray-100 text-gray-400'
}

function nextQuestion() {
  currentQuestion.value++
  answered.value = false
  selectedOption.value = -1
  feedbackText.value = ''
}

function startGame() {
  if (!roomReady.value) return
  allQuestions.value = generateQuestions(selectedDifficulty.value)
  currentQuestion.value = 0
  myCorrect.value = 0
  opponentCorrect.value = 0
  answered.value = false
  selectedOption.value = -1
  quitByMe.value = false
  quitBySomeone.value = false
  showQuitConfirm.value = false
  gameActive.value = true
  pkState.value = 'playing'
}

function finishGame() {
  gameActive.value = false
  pkState.value = 'result'
}

function confirmQuit() {
  showQuitConfirm.value = false
  gameActive.value = false
  quitByMe.value = true
  quitBySomeone.value = true
  pkState.value = 'result'
}

function resetRoom() {
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
}

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
  return '势均力敌！'
})

const resultSub = computed(() => {
  if (quitByMe.value) return '因中途退出对战，你被判为失败'
  if (quitBySomeone.value && !quitByMe.value) return '对方中途退出对战，你自动获胜'
  if (myCorrect.value > opponentCorrect.value) return `你以 ${myCorrect.value}:${opponentCorrect.value} 赢得比赛`
  if (myCorrect.value < opponentCorrect.value) return `对手以 ${opponentCorrect.value}:${myCorrect.value} 赢得比赛`
  return `双方打成 ${myCorrect.value}:${opponentCorrect.value} 平手`
})
</script>