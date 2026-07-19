<template>
  <main class="max-w-[1200px] mx-auto px-6 mt-10">
    <!-- Toast 通知 -->
    <Teleport to="body">
      <Transition name="toast">
        <div
          v-if="toastMsg"
          class="fixed top-6 left-1/2 -translate-x-1/2 z-[400] px-5 py-3 rounded-xl text-sm font-bold shadow-lg"
          :class="toastType === 'success' ? 'bg-green-500 text-white' : 'bg-red-500 text-white'"
        >
          {{ toastMsg }}
        </div>
      </Transition>
    </Teleport>

    <!-- 用户信息卡片 -->
    <div class="card flex flex-col lg:flex-row items-center lg:items-start gap-6 lg:gap-8 mb-8">
      <div
        class="w-20 h-20 rounded-xl flex items-center justify-center flex-none cursor-pointer hover:ring-4 hover:ring-blue-200 transition-all overflow-hidden relative group"
        @click="showAvatarPicker = true"
        title="点击更换头像"
      >
        <img
          v-if="currentAvatarSrc"
          :src="currentAvatarSrc"
          :alt="'头像 ' + currentAvatarId"
          class="w-full h-full object-cover"
        />
        <span v-else class="text-white text-2xl font-bold bg-gradient-to-br from-blue-400 to-purple-500 w-full h-full flex items-center justify-center">
          {{ avatarLetter }}
        </span>
        <!-- 悬停提示 -->
        <div class="absolute inset-0 bg-black/30 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
          <Icon icon="ph:camera-bold" class="text-white text-xl" />
        </div>
      </div>
      <div class="flex-1">
        <div class="flex items-center gap-3 mb-1">
          <h1 class="text-2xl font-bold">{{ username }}</h1>
          <span class="px-3 py-1 bg-blue-50 text-[#2563EB] rounded-full text-xs font-bold">
            {{ userLevel }}
          </span>
          <span v-if="isVip" class="px-3 py-1 bg-yellow-100 text-yellow-700 rounded-full text-xs font-bold">
            ⭐ VIP {{ vipExpireText }}
          </span>
        </div>
        <!-- CEFR 等级进度条 -->
        <div class="mt-3 max-w-[250px]">
          <div class="flex items-center justify-between text-xs text-gray-400 mb-1">
            <span class="font-bold text-gray-500">{{ cefrCurrentLevel }}</span>
            <span>{{ cefrProgress }}%</span>
            <span class="font-bold text-gray-500">{{ cefrNextLevel }}</span>
          </div>
          <div class="w-full h-2 bg-gray-100 rounded-full overflow-hidden">
            <div
              class="h-full rounded-full transition-all duration-500"
              :class="cefrBarColor"
              :style="{ width: cefrProgress + '%' }"
            ></div>
          </div>
        </div>

        <div class="flex items-center gap-4 lg:gap-8 mt-4 flex-wrap justify-center lg:justify-start">
          <div class="text-center">
            <div class="text-xl font-bold">{{ streak }}</div>
            <div class="text-[10px] text-gray-400">连续学习 天</div>
          </div>
          <div class="w-px h-8 bg-gray-100"></div>
          <div class="text-center">
            <div class="text-xl font-bold">{{ totalRead }}</div>
            <div class="text-[10px] text-gray-400">累计阅读 篇</div>
          </div>
          <div class="w-px h-8 bg-gray-100"></div>
          <div class="text-center">
            <div class="text-xl font-bold text-[#F59E0B]">{{ totalXp }}</div>
            <div class="text-[10px] text-gray-400">总经验 XP</div>
          </div>
          <div class="w-px h-8 bg-gray-100"></div>
          <div class="text-center">
            <div class="text-xl font-bold text-[#2563EB]">{{ formattedLiteracy }}</div>
            <div class="text-[10px] text-gray-400">{{ literacyLabel }}</div>
          </div>
          <div class="w-px h-10 bg-gray-100"></div>
          <button
            class="flex flex-col items-center justify-center px-4 py-1 rounded-xl transition-all w-full lg:w-auto"
            :class="profileCheckinDone ? 'bg-green-50' : 'bg-[#2563EB] hover:bg-blue-600 cursor-pointer'"
            :disabled="profileCheckinLoading || profileCheckinDone"
            @click="doProfileCheckin"
          >
            <Icon v-if="profileCheckinDone" icon="ph:check-circle-fill" class="text-xl text-green-500" />
            <Icon v-else icon="ph:gift-fill" class="text-xl text-white" />
            <span v-if="profileCheckinLoading" class="text-[10px] text-white">签到中</span>
            <span v-else-if="profileCheckinDone" class="text-[10px] text-green-500 font-bold">已签到</span>
            <span v-else class="text-[10px] text-white font-bold">签到 +10</span>
          </button>
        </div>
      </div>

      <!-- 右侧 VIP 卡片 -->
      <div
        class="flex-none w-full lg:w-36 p-4 rounded-xl text-center cursor-pointer transition-all hover:scale-105"
        :class="isVip ? 'bg-gradient-to-br from-yellow-100 to-yellow-200 border border-yellow-300' : 'bg-gradient-to-br from-gray-50 to-gray-100 border border-gray-200 hover:border-yellow-300'"
        @click="showVipExchange = true"
      >
        <div class="text-2xl mb-1">⭐</div>
        <div v-if="isVip" class="text-xs font-bold text-yellow-700">
          VIP {{ vipExpireText }}
        </div>
        <div v-else class="text-xs font-bold text-gray-500">
          兑换 VIP
        </div>
        <div class="text-[10px] text-gray-400 mt-1">180经验/天</div>
      </div>
    </div>

    <!-- ========== 头像选择弹窗 ========== -->
    <Teleport to="body">
      <div
        v-if="showAvatarPicker"
        class="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 backdrop-blur-sm"
        @click.self="showAvatarPicker = false"
      >
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-lg max-h-[80vh] overflow-y-auto p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold">选择头像</h3>
            <button
              class="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors"
              @click="showAvatarPicker = false"
            >
              <Icon icon="ph:x-bold" class="text-gray-500" />
            </button>
          </div>
          <div class="grid grid-cols-5 gap-3">
            <div
              v-for="avatar in AVATAR_LIST"
              :key="avatar.id"
              class="aspect-square rounded-xl overflow-hidden cursor-pointer border-2 transition-all hover:scale-105"
              :class="currentAvatarId === avatar.id ? 'border-[#2563EB] ring-2 ring-blue-200' : 'border-gray-100 hover:border-gray-300'"
              @click="selectAvatar(avatar)"
            >
              <img
                :src="AVATAR_BASE_URL + avatar.file"
                :alt="avatar.name"
                class="w-full h-full object-cover"
              />
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- 抽屉式便签栏 -->
    <div class="drawer-tabs flex items-center gap-2 mb-0 flex-wrap flex-col lg:flex-row text-xs lg:text-sm">
      <button
        v-for="tab in drawerTabs"
        :key="tab.key"
        class="drawer-tab-label justify-between lg:justify-center w-full lg:w-auto text-left lg:text-center"
        :class="{ active: activeDrawer === tab.key }"
        @click="toggleDrawer(tab.key)"
      >
        <span class="text-lg">{{ tab.emoji }}</span>
        <span class="text-sm font-bold">{{ tab.label }}</span>
        <Icon
          :icon="activeDrawer === tab.key ? 'ph:caret-up-bold' : 'ph:caret-down-bold'"
          class="text-xs transition-transform duration-300"
        />
      </button>
    </div>

    <!-- 抽屉内容区域 -->
    <div class="drawer-content-wrapper">
      <Transition name="drawer">
        <!-- 学习趋势 -->
        <div v-if="activeDrawer === 'trend'" key="trend" class="card drawer-card relative">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold">📊 每日在线时长</h3>
            <div class="flex items-center gap-2 px-4 py-2 bg-indigo-50 rounded-xl">
              <span class="text-xs text-indigo-400">今日在线</span>
              <span class="text-lg font-bold text-indigo-600">{{ todayOnlineMinutes }}<span class="text-xs font-normal text-indigo-400"> 分钟</span></span>
            </div>
          </div>
          <div ref="trendChartRef" class="w-full h-72"></div>
        </div>

        <!-- 学习记录 -->
        <div v-else-if="activeDrawer === 'records'" key="records" class="card drawer-card relative">
          <h3 class="text-lg font-bold mb-6">📖 学习记录</h3>
          <div v-if="historyRecords.length === 0" class="text-center py-12 text-gray-400">
            <Icon icon="ph:book-open-bold" class="text-3xl mx-auto mb-3 opacity-30" />
            <p class="text-sm">暂无阅读记录</p>
            <router-link to="/materials" class="text-xs text-[#2563EB] hover:underline mt-1 inline-block">
              去发现一些文章吧 →
            </router-link>
          </div>
          <div v-else class="space-y-4 max-h-96 overflow-y-auto">
            <div
              v-for="record in historyRecords"
              :key="record.articleId"
              class="flex items-center justify-between p-4 bg-gray-50 rounded-xl cursor-pointer hover:bg-blue-50 hover:shadow-sm transition-all group"
              @click="goToReader(record.articleId)"
            >
              <div class="flex items-center gap-4">
                <div class="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center flex-none group-hover:bg-blue-100 transition-colors">
                  <Icon icon="ph:book-open-bold" class="text-xl text-[#2563EB]" />
                </div>
                <div>
                  <div class="text-sm font-bold group-hover:text-[#2563EB] transition-colors">{{ record.title }}</div>
                  <div class="text-[10px] text-gray-400 mt-0.5">{{ record.timeAgo }}</div>
                </div>
              </div>
              <div class="text-right flex items-center gap-2">
                <span class="text-[10px] text-gray-300">阅读</span>
                <Icon icon="ph:arrow-right-bold" class="text-gray-300 group-hover:text-[#2563EB] group-hover:translate-x-0.5 transition-all text-sm" />
              </div>
            </div>
          </div>
        </div>

        <!-- 生词本 -->
        <div v-else-if="activeDrawer === 'vocab'" key="vocab" class="card drawer-card relative">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold">📝 生词本</h3>
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-400">{{ vocabWords.length }} 个单词</span>
              <button
                class="px-2.5 py-1.5 rounded-lg text-xs font-bold transition-all"
                :class="vocabWords.length >= 10
                  ? 'bg-[#2563EB] text-white hover:bg-blue-600'
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-400 dark:text-gray-500'"
                @click="handleVocabReviewClick"
              >
                复习生词
              </button>
            </div>
          </div>
          <div v-if="vocabWords.length === 0" class="text-center py-12 text-gray-400">
            <Icon icon="ph:bookmark-simple-bold" class="text-3xl mx-auto mb-3 opacity-30" />
            <p class="text-sm">暂无生词</p>
            <p class="text-xs mt-1">在阅读时点击单词卡片右上角的 <span class="text-[#2563EB] font-bold">+</span> 即可收藏</p>
          </div>
          <div v-else class="max-h-96 overflow-y-auto">
            <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
              <div
                v-for="item in vocabWords"
                :key="item.wordLower"
                class="p-3 bg-gray-50 rounded-xl group hover:bg-blue-50 transition-colors relative"
              >
                <button
                  class="absolute top-1.5 right-1.5 text-gray-300 hover:text-red-400 transition-colors opacity-0 group-hover:opacity-100"
                  @click="removeVocabWord(item.wordLower)"
                  title="移出生词本"
                >
                  <Icon icon="ph:x-circle-fill" class="text-sm" />
                </button>
                <p class="text-sm font-bold text-gray-800 truncate pr-4">{{ item.word }}</p>
                <p v-if="item.phonetic" class="text-[10px] text-gray-400 font-mono truncate mt-0.5">{{ item.phonetic }}</p>
                <p class="text-xs text-gray-500 mt-1 line-clamp-2 leading-relaxed">{{ item.translation }}</p>
                <div class="flex items-center gap-1.5 mt-2">
                  <span v-if="item.source" class="text-[10px] text-gray-400 bg-gray-100 px-1.5 py-0.5 rounded">{{ item.source }}</span>
                  <span class="text-[10px] text-gray-300">{{ formatAddedTime(item.addedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 错题本 -->
        <div v-else-if="activeDrawer === 'wrongbook'" key="wrongbook" class="card drawer-card relative">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold">📋 错题本</h3>
            <div class="flex items-center gap-2">
              <span class="text-xs text-gray-400">{{ wrongQuestions.length }} 道错题</span>
              <!-- 导出下拉按钮 -->
              <div class="relative" v-if="wrongQuestions.length > 0">
                <button
                  class="flex items-center gap-1 px-2.5 py-1.5 rounded-lg text-xs font-bold text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-700 transition-all"
                  @click.stop="showExportMenu = !showExportMenu"
                >
                  <Icon icon="ph:export-bold" class="text-sm" />
                  <span class="hidden sm:inline">导出</span>
                  <Icon :icon="showExportMenu ? 'ph:caret-up-bold' : 'ph:caret-down-bold'" class="text-[10px]" />
                </button>
                <!-- 下拉菜单 -->
                <div
                  v-if="showExportMenu"
                  class="absolute right-0 top-full mt-1 w-52 bg-white dark:bg-gray-800 rounded-xl shadow-xl border border-gray-100 dark:border-gray-700 py-1 z-50"
                  @click.stop
                >
                  <button
                    class="w-full text-left px-4 py-2.5 text-xs font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-all flex items-center gap-2"
                    @click="handleExportEWB"
                  >
                    <Icon icon="ph:device-mobile-bold" class="text-sm text-blue-500" />
                    将错题迁移至其他设备
                  </button>
                  <button
                    class="w-full text-left px-4 py-2.5 text-xs font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-all flex items-center gap-2"
                    @click="handleExportJSON"
                  >
                    <Icon icon="ph:file-text-bold" class="text-sm text-green-500" />
                    导出为可编辑文件
                  </button>
                  <div class="border-t border-gray-100 dark:border-gray-700 my-1"></div>
                  <button
                    class="w-full text-left px-4 py-2.5 text-xs font-medium text-gray-600 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-all flex items-center gap-2"
                    @click="triggerImportFile"
                  >
                    <Icon icon="ph:upload-bold" class="text-sm text-amber-500" />
                    从其他设备导入错题
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 隐藏的文件导入 input -->
          <input
            ref="importFileInput"
            type="file"
            accept=".ewb"
            class="hidden"
            @change="handleImportFile"
          />

          <!-- 导入结果提示 -->
          <div
            v-if="importToast.show"
            class="mb-4 px-4 py-2.5 rounded-xl text-xs font-bold flex items-center gap-2"
            :class="importToast.success
              ? 'bg-green-50 dark:bg-green-900/30 text-green-600 dark:text-green-400 border border-green-200 dark:border-green-700'
              : 'bg-red-50 dark:bg-red-900/30 text-red-500 dark:text-red-400 border border-red-200 dark:border-red-700'"
          >
            <Icon :icon="importToast.success ? 'ph:check-circle-bold' : 'ph:warning-circle-bold'" class="text-sm" />
            {{ importToast.message }}
            <button class="ml-auto text-gray-400 hover:text-gray-600 dark:hover:text-gray-300" @click="importToast.show = false">
              <Icon icon="ph:x-bold" class="text-[10px]" />
            </button>
          </div>

          <div v-if="wrongQuestions.length === 0" class="text-center py-12 text-gray-400">
            <Icon icon="ph:clipboard-text-bold" class="text-3xl mx-auto mb-3 opacity-30" />
            <p class="text-sm">暂无错题</p>
            <p class="text-xs mt-1">在测评结果页点击题目旁的 <span class="text-[#2563EB] font-bold">+</span> 即可加入错题本</p>
          </div>
          <div v-else>
            <!-- 桌面端：双栏布局（左侧索引列表 + 右侧题目卡片） -->
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-4 lg:gap-6">
              <!-- 左侧：快捷索引列表（仅桌面端可见） -->
              <div class="hidden lg:block lg:col-span-4 xl:col-span-3">
                <div class="space-y-0.5 max-h-[480px] overflow-y-auto pr-1 rounded-lg">
                  <button
                    v-for="(q, idx) in wrongQuestions"
                    :key="q.id"
                    class="w-full text-left px-3 py-2.5 rounded-lg transition-all flex items-center gap-2.5 group"
                    :class="idx === wrongBookIndex
                      ? 'bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 font-bold shadow-sm'
                      : 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700/50'"
                    @click="wrongBookIndex = idx"
                  >
                    <span class="text-xs font-bold w-7 text-right flex-none"
                      :class="idx === wrongBookIndex ? 'text-blue-600 dark:text-blue-400' : 'text-gray-400 dark:text-gray-500 group-hover:text-gray-600 dark:group-hover:text-gray-300'"
                    >#{{ wrongQuestions.length - idx }}</span>
                    <span class="truncate text-xs leading-relaxed">{{ q.question }}</span>
                  </button>
                </div>
              </div>

              <!-- 右侧：移动端下拉按钮 + 题目卡片 -->
              <div class="lg:col-span-8 xl:col-span-9 min-w-0">
                <!-- 移动端：下拉选择按钮 -->
                <button
                  class="lg:hidden flex items-center justify-between w-full px-4 py-2.5 mb-4 bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl text-sm font-bold text-gray-600 dark:text-gray-300 transition-all hover:border-[#2563EB] dark:hover:border-blue-500"
                  @click="showWrongBookIndexOverlay = !showWrongBookIndexOverlay"
                >
                  <span>第 {{ wrongQuestions.length - wrongBookIndex }} / {{ wrongQuestions.length }} 题</span>
                  <Icon icon="ph:caret-down-bold" class="text-sm transition-transform duration-200" :class="{ 'rotate-180': showWrongBookIndexOverlay }" />
                </button>

                <!-- 当前错题卡片 -->
                <div class="card border-l-4 border-l-red-400 bg-white dark:bg-gray-800" :key="'wb-' + wrongBookIndex">
                  <!-- 题号 -->
                  <div class="flex items-center justify-between mb-4">
                    <div class="flex items-center gap-3">
                      <span class="w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold bg-red-50 dark:bg-red-900/30 text-red-600 dark:text-red-400">
                        #{{ wrongQuestions.length - wrongBookIndex }}
                      </span>
                      <span class="text-xs font-bold text-gray-400 dark:text-gray-500 uppercase tracking-widest">
                        错题 {{ wrongQuestions.length - wrongBookIndex }} / {{ wrongQuestions.length }}
                      </span>
                    </div>
                    <button
                      class="flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-bold text-gray-400 dark:text-gray-500 hover:text-red-500 dark:hover:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/30 transition-all"
                      @click="promptDeleteWrongQuestion(currentWrongQuestion.id)"
                      title="移出错题本"
                    >
                      <Icon icon="ph:trash-bold" class="text-sm" />
                      移除
                    </button>
                  </div>

                  <!-- 加入时间 -->
                  <p class="text-[11px] text-gray-400 dark:text-gray-500 mb-4">
                    于 {{ formatAddedAt(currentWrongQuestion.addedAt) }} 加入
                  </p>

                  <!-- 阅读文本 -->
                  <div v-if="currentWrongQuestion.passage" class="mb-4 p-4 bg-gray-50 dark:bg-gray-700/50 rounded-xl">
                    <p class="text-sm text-gray-500 dark:text-gray-400 leading-relaxed">{{ currentWrongQuestion.passage }}</p>
                  </div>

                  <!-- 问题 -->
                  <p class="font-bold text-lg mb-4 text-gray-800 dark:text-gray-200">{{ currentWrongQuestion.question }}</p>

                  <!-- 选项 -->
                  <div class="space-y-3 mb-4">
                    <div
                      v-for="opt in currentWrongQuestion.options"
                      :key="opt.id"
                      class="flex items-center gap-3 p-3 rounded-xl text-sm"
                      :class="getWrongOptClass(opt.id, currentWrongQuestion)"
                    >
                      <div
                        class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold flex-none"
                        :class="getWrongOptDotClass(opt.id, currentWrongQuestion)"
                      >
                        {{ opt.id }}
                      </div>
                      <span class="font-medium">{{ opt.text }}</span>
                      <span v-if="opt.id === currentWrongQuestion.correctAnswer" class="ml-auto text-xs font-bold text-green-600 dark:text-green-400">
                        ✓ 正确答案
                      </span>
                      <span v-else-if="opt.id === currentWrongQuestion.userAnswer" class="ml-auto text-xs font-bold text-red-500 dark:text-red-400">
                        ✗ 你的答案
                      </span>
                    </div>
                  </div>

                  <!-- 解析 -->
                  <div class="p-4 rounded-xl bg-amber-50 dark:bg-amber-900/20 border border-amber-100 dark:border-amber-800">
                    <div class="flex items-start gap-2">
                      <Icon icon="ph:info-fill" class="text-lg flex-none mt-0.5 text-amber-500 dark:text-amber-400" />
                      <div>
                        <p class="text-xs font-bold mb-1 text-amber-700 dark:text-amber-300">错题解析</p>
                        <p class="text-sm leading-relaxed text-amber-800 dark:text-amber-200">{{ currentWrongQuestion.explanation || '暂无解析' }}</p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 键盘提示 -->
            <p class="text-center text-xs text-gray-300 dark:text-gray-600 mt-4">
              使用 ← → 方向键切换题目，点击左侧列表快速定位
            </p>
          </div>
        </div>

        <!-- 收藏夹 -->
        <div v-else-if="activeDrawer === 'favorites'" key="favorites" class="card drawer-card relative">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-lg font-bold">📌 收藏夹</h3>
            <span class="text-xs text-gray-400">{{ favoriteArticles.length }} 篇文章</span>
          </div>
          <div v-if="favoriteArticles.length === 0" class="text-center py-12 text-gray-400">
            <Icon icon="ph:bookmark-simple-bold" class="text-3xl mx-auto mb-3 opacity-30" />
            <p class="text-sm">暂无收藏文章</p>
            <p class="text-xs mt-1">在阅读时点击右上角 <span class="text-yellow-500 font-bold">🔖</span> 书签按钮即可收藏</p>
            <router-link to="/materials" class="text-xs text-[#2563EB] hover:underline mt-2 inline-block">
              去发现文章 →
            </router-link>
          </div>
          <div v-else class="space-y-3 max-h-96 overflow-y-auto">
            <div
              v-for="fav in favoriteArticles"
              :key="fav.articleId"
              class="flex items-center gap-4 p-4 bg-gray-50 rounded-xl cursor-pointer hover:bg-blue-50 hover:shadow-sm transition-all group"
              @click="goToReader(fav.articleId)"
            >
              <!-- 难度封面缩略图 -->
              <div
                class="w-14 h-14 rounded-lg flex-none flex items-center justify-center text-xs font-extrabold select-none"
                :class="getFavCoverClass(fav.difficulty)"
              >
                {{ fav.difficulty || '阅读' }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="text-sm font-bold group-hover:text-[#2563EB] transition-colors truncate">{{ fav.title }}</div>
                <div class="flex items-center gap-3 mt-1">
                  <span class="text-[10px] text-gray-400">{{ fav.source || '未知来源' }}</span>
                  <span v-if="fav.readTime" class="text-[10px] text-gray-300">{{ fav.readTime }}</span>
                  <span class="text-[10px] text-gray-300">{{ formatFavTime(fav.addedAt) }}</span>
                </div>
              </div>
              <button
                class="flex-none p-2 text-gray-300 hover:text-red-400 hover:bg-red-50 rounded-lg transition-all opacity-0 group-hover:opacity-100"
                title="移出收藏夹"
                @click.stop="removeFavorite(fav.articleId)"
              >
                <Icon icon="ph:trash-bold" class="text-sm" />
              </button>
              <Icon icon="ph:arrow-right-bold" class="text-gray-300 group-hover:text-[#2563EB] group-hover:translate-x-0.5 transition-all text-sm flex-none" />
            </div>
          </div>
        </div>

        <!-- 设置 -->
        <div v-else-if="activeDrawer === 'settings'" key="settings" class="card drawer-card relative">
          <h3 class="text-lg font-bold mb-6">⚙️ 设置</h3>
          <div class="space-y-1">
            <!-- 学习目标时长 -->
            <div
              class="flex items-center justify-between p-4 hover:bg-gray-50 rounded-xl transition-colors"
              :class="{ 'cursor-pointer': !editingTarget }"
              @click="editingTarget || startEditTarget()"
            >
              <span class="text-sm font-medium text-gray-700">学习目标时长</span>
              <span v-if="!editingTarget" class="text-sm text-gray-400">{{ studyTarget }} 分钟/天</span>
              <div v-else class="flex items-center gap-2" @click.stop>
                <input
                  ref="targetInputRef"
                  v-model.number="targetDraft"
                  type="number"
                  min="1"
                  max="480"
                  class="w-16 h-8 text-sm text-center border border-[#2563EB] rounded-lg focus:outline-none"
                  @keydown.enter="saveTarget"
                  @keydown.escape="editingTarget = false"
                  @blur="saveTarget"
                />
                <span class="text-xs text-gray-400">分钟/天</span>
              </div>
            </div>
            <!-- 学习阶段 -->
            <div class="flex items-center justify-between p-4 hover:bg-gray-50 rounded-xl transition-colors">
              <span class="text-sm font-medium text-gray-700">学习阶段</span>
              <select
                :value="userStore.user?.studyPurpose || ''"
                class="text-sm text-gray-500 border border-gray-200 rounded-lg px-2 py-1 focus:outline-none focus:ring-2 focus:ring-[#2563EB]"
                @change="updateStudyPurpose($event.target.value)"
              >
                <option value="" disabled>请选择</option>
                <option v-for="s in allStages" :key="s" :value="s">{{ s }}</option>
              </select>
            </div>
            <!-- 关于我们 -->
            <div
              class="flex items-center justify-between p-4 hover:bg-gray-50 rounded-xl transition-colors cursor-pointer"
              @click="openAbout"
            >
              <span class="text-sm font-medium text-gray-700">关于我们</span>
              <Icon icon="ph:caret-right-bold" class="text-gray-300 text-lg" />
            </div>
            <!-- 退出登录 -->
            <div
              class="flex items-center justify-between p-4 hover:bg-red-50 rounded-xl transition-colors cursor-pointer"
              @click="handleLogout"
            >
              <span class="text-sm font-medium text-red-500">退出登录</span>
              <Icon icon="ph:sign-out-bold" class="text-red-300 text-lg" />
            </div>
          </div>
        </div>
      </Transition>
    </div>

    <!-- 关于我们弹窗 -->
    <Teleport to="body">
      <div
        v-if="showAboutModal"
        class="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 backdrop-blur-sm"
        @click.self="showAboutModal = false"
        @keydown.escape="showAboutModal = false"
      >
        <div class="relative bg-white rounded-2xl shadow-2xl overflow-hidden" style="width:192px;height:108px">
          <img src="/gggg.png" alt="关于我们" class="w-full h-full object-contain" />
        </div>
      </div>
    </Teleport>

    <!-- 退出登录确认浮窗 -->
    <Teleport to="body">
      <div
        v-if="showLogoutConfirm"
        class="fixed z-[200] glass-popover p-5 w-72"
        :style="{ left: logoutPopoverPos.x + 'px', top: logoutPopoverPos.y + 'px' }"
        @click.stop
      >
        <p class="text-sm font-medium text-gray-700 mb-4">确定要退出当前账号吗？</p>
        <div class="flex gap-2">
          <button
            class="flex-1 h-9 border border-gray-200 rounded-lg text-xs font-medium text-gray-500 hover:bg-gray-50 transition-all"
            @click="cancelLogout"
          >
            取消
          </button>
          <button
            class="flex-1 h-9 bg-red-500 text-white rounded-lg text-xs font-bold hover:bg-red-600 transition-all"
            @click="confirmLogout"
          >
            退出
          </button>
        </div>
      </div>
    </Teleport>

    <!-- 词汇量测试弹窗（精确版） -->
    <VocabTestModel :visible="showVocabTest" @close="onVocabTestClose" />

    <!-- 词汇量测试弹窗（快速版） -->
    <FirstVocabTestModal
      :visible="showQuickVocabTest"
      :is-initial="false"
      @done="onQuickVocabTestDone"
      @close="showQuickVocabTest = false"
      @skip="showQuickVocabTest = false"
    />

    <!-- 复习生词弹窗 -->
    <VocabReviewModal
      :visible="showVocabReview"
      :words="vocabWords"
      @close="showVocabReview = false"
    />

    <!-- 词汇量测试选择弹窗 -->
    <Teleport to="body">
      <div
        v-if="showVocabTestChoice"
        class="fixed inset-0 z-[250] flex items-end lg:items-center justify-center"
        @click.self="showVocabTestChoice = false"
      >
        <div class="absolute inset-0 bg-black/40 backdrop-blur-sm"></div>
        <div class="relative bg-white dark:bg-gray-800 rounded-t-2xl lg:rounded-2xl shadow-2xl w-full lg:max-w-sm p-6 z-10 lg:mx-4">
          <h3 class="text-lg font-bold mb-4 text-gray-800 dark:text-gray-200">选择测试模式</h3>
          <div class="space-y-3">
            <button
              class="w-full text-left p-4 rounded-xl border-2 border-gray-200 dark:border-gray-600 hover:border-[#2563EB] dark:hover:border-blue-500 transition-all group"
              @click="startQuickTest"
            >
              <div class="flex items-center gap-2 mb-1">
                <Icon icon="ph:lightning-bold" class="text-lg text-amber-500" />
                <span class="font-bold text-gray-700 dark:text-gray-200">快速测试</span>
              </div>
              <p class="text-xs text-gray-400 pl-7">勾选认识/不认识 · 约 5 分钟</p>
            </button>
            <button
              class="w-full text-left p-4 rounded-xl border-2 border-gray-200 dark:border-gray-600 hover:border-[#2563EB] dark:hover:border-blue-500 transition-all group"
              @click="startPreciseTest"
            >
              <div class="flex items-center gap-2 mb-1">
                <Icon icon="ph:target-bold" class="text-lg text-green-500" />
                <span class="font-bold text-gray-700 dark:text-gray-200">精确测试</span>
              </div>
              <p class="text-xs text-gray-400 pl-7">四选一 + 认识/不认识 · 约 10 分钟</p>
            </button>
          </div>
          <button
            class="mt-4 w-full py-2.5 rounded-xl text-xs font-medium text-gray-400 dark:text-gray-500 hover:bg-gray-50 dark:hover:bg-gray-700 transition-all"
            @click="showVocabTestChoice = false"
          >
            取消
          </button>
        </div>
      </div>
    </Teleport>

    <!-- VIP 兑换弹窗 -->
    <Teleport to="body">
      <div
        v-if="showVipExchange"
        class="fixed inset-0 z-[200] flex items-center justify-center bg-black/40 backdrop-blur-sm"
        @click.self="showVipExchange = false"
      >
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm p-6 mx-4">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-bold">⭐ VIP 兑换</h3>
            <button
              class="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors"
              @click="showVipExchange = false"
            >
              <Icon icon="ph:x-bold" class="text-gray-500" />
            </button>
          </div>

          <div class="bg-yellow-50 rounded-xl p-4 mb-4">
            <p class="text-sm text-yellow-700 font-medium mb-1">当前经验值：<strong>{{ totalXp }}</strong></p>
            <p v-if="isVip" class="text-sm text-yellow-700 font-medium">
              VIP 到期：<strong>{{ vipExpireText }}</strong>
            </p>
            <p v-else class="text-sm text-gray-500">你还不是 VIP 用户</p>
          </div>

          <p class="text-xs text-gray-400 mb-4">180 经验值 = 1 天 VIP，选择要兑换的天数：</p>

          <div class="grid grid-cols-4 gap-2 mb-4">
            <button
              v-for="d in vipDayOptions"
              :key="d"
              class="py-2 rounded-lg text-sm font-bold transition-all border"
              :class="vipSelectedDays === d
                ? 'bg-yellow-100 border-yellow-400 text-yellow-700'
                : 'bg-white border-gray-200 text-gray-500 hover:border-yellow-300'"
              @click="vipSelectedDays = d"
            >
              {{ d }}天
            </button>
          </div>

          <p class="text-center text-sm text-gray-500 mb-4">
            需要 <strong class="text-yellow-600">{{ vipSelectedDays * 180 }}</strong> 经验值
          </p>

          <button
            class="w-full py-3 bg-yellow-400 text-white rounded-xl font-bold hover:bg-yellow-500 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            :disabled="vipSelectedDays * 180 > totalXp || vipExchanging"
            @click="doVipExchange"
          >
            <span v-if="vipExchanging">兑换中...</span>
            <span v-else-if="isVip">续费 VIP {{ vipSelectedDays }} 天</span>
            <span v-else>兑换 VIP {{ vipSelectedDays }} 天</span>
          </button>
          <p v-if="vipSelectedDays * 180 > totalXp" class="text-xs text-red-400 text-center mt-2">
            经验值不足
          </p>
        </div>
      </div>
    </Teleport>

    <!-- 错题本移动端索引覆盖层 -->
    <Teleport to="body">
      <Transition name="sheet">
        <div
          v-if="showWrongBookIndexOverlay"
          class="fixed inset-0 z-[250] flex items-end lg:hidden"
          @click.self="showWrongBookIndexOverlay = false"
        >
          <div class="absolute inset-0 bg-black/40 backdrop-blur-sm" @click="showWrongBookIndexOverlay = false"></div>
          <div class="relative bg-white dark:bg-gray-800 rounded-t-2xl shadow-2xl w-full max-h-[65vh] overflow-y-auto p-5 z-10">
            <div class="w-10 h-1 bg-gray-300 dark:bg-gray-600 rounded-full mx-auto mb-4"></div>
            <div class="flex items-center justify-between mb-3">
              <h3 class="text-sm font-bold text-gray-500 dark:text-gray-400">选择错题（共 {{ wrongQuestions.length }} 道）</h3>
              <button class="w-7 h-7 rounded-full bg-gray-100 dark:bg-gray-700 flex items-center justify-center" @click="showWrongBookIndexOverlay = false">
                <Icon icon="ph:x-bold" class="text-xs text-gray-500 dark:text-gray-400" />
              </button>
            </div>
            <div class="space-y-0.5">
              <button
                v-for="(q, idx) in wrongQuestions"
                :key="q.id"
                class="w-full text-left px-3 py-3 rounded-lg transition-all flex items-center gap-3"
                :class="idx === wrongBookIndex
                  ? 'bg-blue-50 dark:bg-blue-900/30 text-blue-600 dark:text-blue-400 font-bold'
                  : 'text-gray-500 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700'"
                @click="wrongBookIndex = idx; showWrongBookIndexOverlay = false"
              >
                <span class="text-xs font-bold w-8 text-right flex-none"
                  :class="idx === wrongBookIndex ? 'text-blue-600 dark:text-blue-400' : 'text-gray-400 dark:text-gray-500'"
                >#{{ wrongQuestions.length - idx }}</span>
                <span class="text-sm truncate">{{ q.question }}</span>
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 错题本删除确认弹窗 -->
    <Teleport to="body">
      <div
        v-if="showDeleteConfirm"
        class="fixed inset-0 z-[250] flex items-center justify-center"
        @click.self="showDeleteConfirm = false"
      >
        <div class="absolute inset-0 bg-black/40 backdrop-blur-sm"></div>
        <div class="relative bg-white dark:bg-gray-800 rounded-2xl shadow-2xl w-full max-w-sm p-8 z-10 text-center mx-4">
          <div class="w-14 h-14 bg-red-50 dark:bg-red-900/30 text-red-400 rounded-2xl flex items-center justify-center mx-auto mb-5">
            <Icon icon="ph:warning-bold" class="text-3xl" />
          </div>
          <h3 class="text-lg font-bold text-gray-800 dark:text-gray-200 mb-2">确认删除</h3>
          <p class="text-sm text-gray-400 dark:text-gray-400 mb-6">
            该题目将被永久删除，请确认操作。
          </p>
          <div class="flex gap-3">
            <button
              class="flex-1 h-11 border border-gray-200 dark:border-gray-600 rounded-xl text-sm font-medium text-gray-500 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-all"
              @click="showDeleteConfirm = false"
            >
              取消
            </button>
            <button
              class="flex-1 h-11 bg-red-500 text-white rounded-xl text-sm font-bold hover:bg-red-600 transition-all"
              @click="executeDeleteWrongQuestion"
            >
              确认删除
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </main>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Icon } from '@iconify/vue'
let echartsMod = null  // 动态导入 ECharts
import { useUserStore } from '@/stores/user'
import { useTaskStore } from '@/stores/task'
import { getRecentOnlineTime, getTodayMinutes, getDailyTarget, setDailyTarget, getStreak } from '@/utils/onlineTimeDB'
import { getRecentHistory, countHistory } from '@/utils/historyDB'
import request from '@/utils/request'
import VocabTestModel from '@/components/VocabTestModel.vue'
import FirstVocabTestModal from '@/components/FirstVocabTestModal.vue'
import VocabReviewModal from '@/components/VocabReviewModal.vue'
import { AVATAR_LIST, AVATAR_BASE_URL, getAvatarSrc } from '@/constants/avatars.js'
const gggAudioModules = import.meta.glob('../ggg/*.mp3', { eager: true })
const gggAudioFiles = Object.values(gggAudioModules).map(m => m.default)

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const taskStore = useTaskStore()

// ========== 头像 ==========
const AVATAR_STORAGE_KEY = 'aael_selected_avatar'
const defaultAvatarId = 1

function loadSelectedAvatarId() {
  try {
    const val = localStorage.getItem(AVATAR_STORAGE_KEY)
    if (val !== null) {
      const num = parseInt(val, 10)
      if (!isNaN(num) && num >= 1 && num <= AVATAR_LIST.length) return num
    }
  } catch (_) { /* ignore */ }
  return defaultAvatarId
}

function saveSelectedAvatarId(id) {
  localStorage.setItem(AVATAR_STORAGE_KEY, String(id))
}

const currentAvatarId = ref(loadSelectedAvatarId())
const showAvatarPicker = ref(false)

const currentAvatarSrc = computed(() => {
  const avatar = AVATAR_LIST.find(a => a.id === currentAvatarId.value)
  return avatar ? AVATAR_BASE_URL + avatar.file : ''
})

function selectAvatar(avatar) {
  currentAvatarId.value = avatar.id
  saveSelectedAvatarId(avatar.id)
  showAvatarPicker.value = false
  // 通知导航栏同步更新头像
  window.dispatchEvent(new CustomEvent('avatar-changed'))
}

// ========== 用户信息 ==========
const showEditProfile = ref(false)
const username = computed(() => userStore.user?.username || 'Alex')
function literacyToLevel(literacy) {
  if (!literacy || literacy === 0) return null
  if (literacy < 1500) return 'A1 · 初级'
  if (literacy < 3000) return 'A2 · 初级上'
  if (literacy < 5000) return 'B1 · 中级'
  if (literacy < 8000) return 'B2 · 中高级'
  if (literacy < 12000) return 'C1 · 高级'
  return 'C2 · 精通'
}
function getLocalVocabResult() {
  try {
    const uid = userStore.user?.userId
    if (!uid) return null
    const raw = localStorage.getItem(`aael_vocab_result_${uid}`)
    return raw ? JSON.parse(raw) : null
  } catch { return null }
}
const userLevel = computed(() => {
  const local = getLocalVocabResult()
  if (local) return `${local.cefrLevel} · ${local.cefrLabel}`
  return literacyToLevel(userStore.user?.literacy) || userStore.user?.study_purpose || 'B1 · 中级'
})
const avatarLetter = computed(() => username.value.charAt(0).toUpperCase())
const streak = ref(0)
const totalRead = ref(0)
const totalXp = computed(() => userStore.user?.experience || 0)

// CEFR 进度条
const CEFR_LEVELS = ['A1', 'A2', 'B1', 'B2', 'C1', 'C2']
const cefrCurrentLevel = computed(() => {
  const local = getLocalVocabResult()
  if (local) return local.cefrLevel || 'A1'
  return literacyToLevel(userStore.user?.literacy)?.split(' · ')[0] || 'A1'
})
const isCefrMaxLevel = computed(() => cefrCurrentLevel.value === 'C2')
const cefrProgress = computed(() => {
  if (isCefrMaxLevel.value) return 100
  return userStore.user?.cefrProgress || 0
})
const cefrNextLevel = computed(() => {
  if (isCefrMaxLevel.value) return 'MAX'
  const idx = CEFR_LEVELS.indexOf(cefrCurrentLevel.value)
  return CEFR_LEVELS[idx + 1] || cefrCurrentLevel.value
})
const cefrBarColor = computed(() => {
  const p = cefrProgress.value
  if (p >= 90) return 'bg-green-500'
  if (p >= 60) return 'bg-[#2563EB]'
  if (p >= 30) return 'bg-yellow-500'
  return 'bg-gray-300'
})

// ========== 学习记录（浏览历史）==========
const historyRecords = ref([])

async function loadHistoryRecords() {
  try {
    const { getRecentHistory, relativeTime } = await import('@/utils/historyDB')
    const records = await getRecentHistory(50)
    historyRecords.value = records.map((r) => ({
      articleId: r.articleId,
      title: r.title || 'Untitled',
      timeAgo: relativeTime(r.visitedAt),
    }))
  } catch (e) {
    console.error('加载浏览历史失败:', e)
    historyRecords.value = []
  }
}

function goToReader(articleId) {
  if (!articleId) return
  router.push(`/reader?id=${articleId}`)
}

// ========== 抽屉便签 ==========
const drawerTabs = [
  { key: 'trend', label: '学习趋势', emoji: '📊' },
  { key: 'records', label: '学习记录', emoji: '📖' },
  { key: 'vocab', label: '生词本', emoji: '📝' },
  { key: 'wrongbook', label: '错题本', emoji: '📋' },
  { key: 'favorites', label: '收藏夹', emoji: '📌' },
  { key: 'vocabtest', label: '词汇量测试', emoji: '🧪' },
  { key: 'settings', label: '设置', emoji: '⚙️' },
]
const activeDrawer = ref('trend')
const showVocabTest = ref(false)
const showQuickVocabTest = ref(false)
const showVocabTestChoice = ref(false)

// ========== 词汇量显示 ==========
const literacyLabel = computed(() => {
  const lit = userStore.user?.literacy
  if (!lit) return '词汇量 未测试'
  const level = literacyToLevel(lit)
  return level ? `词汇量 ${level}` : `词汇量 ${lit}`
})

const formattedLiteracy = computed(() => {
  const lit = userStore.user?.literacy
  if (!lit) return '--'
  return String(lit)
})

// ========== VIP 兑换 ==========
const showVipExchange = ref(false)
const vipSelectedDays = ref(1)
const vipExchanging = ref(false)
const vipDayOptions = [1, 3, 7, 30]
const allStages = ['初中', '高中', '四级', '六级', '考研', '托福', '期刊', '原著', '网络新闻']

async function updateStudyPurpose(value) {
  if (!value) return
  try {
    const params = new URLSearchParams()
    params.append('userId', String(userStore.user?.userId))
    params.append('studyPurpose', value)
    const data = await request.post('/user/update-study-purpose', params)
    if (data.success) {
      showToast('学习阶段已更新为 ' + value)
      await userStore.fetchProfile()
    } else {
      showToast(data.message || '更新失败', 'error')
    }
  } catch (e) {
    showToast('更新失败，请重试', 'error')
  }
}
const toastMsg = ref('')
const toastType = ref('success')
let toastTimer = null

function showToast(msg, type = 'success') {
  toastMsg.value = msg
  toastType.value = type
  clearTimeout(toastTimer)
  toastTimer = setTimeout(() => { toastMsg.value = '' }, 2500)
}

// 签到
const profileCheckinLoading = ref(false)
const profileCheckinDone = ref(false)
async function doProfileCheckin() {
  if (profileCheckinLoading.value || profileCheckinDone.value) return
  profileCheckinLoading.value = true
  try {
    const data = await request.post('/user/checkin')
    if (data.success) {
      profileCheckinDone.value = true
      showToast('签到成功！经验值 +' + (data.experienceEarned || 10), 'success')
      await userStore.fetchProfile()
    } else {
      if (data.message && data.message.includes('今日已签到')) {
        profileCheckinDone.value = true
      }
      showToast(data.message || '签到失败', 'error')
    }
  } catch (_) {
    showToast('签到失败', 'error')
  } finally {
    profileCheckinLoading.value = false
  }
}
const vipExpireAt = computed(() => userStore.user?.vipExpireAt || '')

const isVip = computed(() => userStore.user?.profile === 'vip')
const vipExpireText = computed(() => {
  if (!vipExpireAt.value) return ''
  try {
    const d = new Date(vipExpireAt.value.replace(' ', 'T'))
    const now = new Date()
    const daysLeft = Math.ceil((d - now) / 86400000)
    return daysLeft > 0 ? `剩余 ${daysLeft} 天` : '已过期'
  } catch { return vipExpireAt.value }
})

async function doVipExchange() {
  vipExchanging.value = true
  try {
    // 先刷新 session 确保登录态有效
    await userStore.fetchProfile()
    if (!userStore.user?.userId) {
      showToast('登录已过期，请重新登录', 'error')
      return
    }

    const params = new URLSearchParams()
    params.append('userId', String(userStore.user.userId))
    params.append('days', String(vipSelectedDays.value))
    const data = await request.post('/user/vip-exchange', params)
    if (data.success) {
      showVipExchange.value = false
      await userStore.fetchProfile()
      showToast('VIP 兑换成功！已开通 ' + vipSelectedDays.value + ' 天', 'success')
    } else {
      showToast(data.message || '兑换失败', 'error')
    }
  } catch (e) {
    // 401 重试：刷新 session 后再试一次
    if (e.response?.status === 401) {
      try {
        await userStore.fetchProfile()
        const params2 = new URLSearchParams()
        params2.append('userId', String(userStore.user.userId))
        params2.append('days', String(vipSelectedDays.value))
        const retry = await request.post('/user/vip-exchange', params2)
        if (retry.success) {
          showVipExchange.value = false
          await userStore.fetchProfile()
          showToast('VIP 兑换成功！已开通 ' + vipSelectedDays.value + ' 天', 'success')
          return
        }
      } catch (_) {}
    }
    console.error('VIP兑换失败:', e)
    showToast('兑换失败，请稍后重试', 'error')
  } finally {
    vipExchanging.value = false
  }
}

function toggleDrawer(key) {
  // 词汇量测试：弹出测试模式选择
  if (key === 'vocabtest') {
    showVocabTestChoice.value = true
    return
  }
  // 点击已打开的便签 → 关闭；点击其他便签 → 切换
  activeDrawer.value = activeDrawer.value === key ? null : key
}

/** 开始快速词汇量测试 */
function startQuickTest() {
  showVocabTestChoice.value = false
  showQuickVocabTest.value = true
}

/** 开始精确词汇量测试 */
function startPreciseTest() {
  showVocabTestChoice.value = false
  showVocabTest.value = true
}

/** 快速测试完成 */
async function onQuickVocabTestDone() {
  showQuickVocabTest.value = false
  await userStore.fetchProfile()
}

/** 精确测试关闭 */
async function onVocabTestClose() {
  showVocabTest.value = false
  await userStore.fetchProfile()
}

// ========== 生词本 ==========
const vocabWords = ref([])
const showVocabReview = ref(false)
let vocabTimer = null
const VOCAB_REVIEW_SECONDS = 30  // 停留 30 秒视为完成复习任务

function handleVocabReviewClick() {
  if (vocabWords.value.length >= 10) {
    showVocabReview.value = true
  } else {
    showToast('当前单词少于 10 个，快去认识新词吧！')
  }
}

// ========== 收藏夹 ==========
const favoriteArticles = ref([])

async function loadFavorites() {
  try {
    const { getAllFavorites } = await import('@/utils/favoritesDB')
    favoriteArticles.value = await getAllFavorites()
  } catch (e) {
    console.error('加载收藏夹失败:', e)
  }
}

async function removeFavorite(articleId) {
  try {
    const { removeFromFavorites } = await import('@/utils/favoritesDB')
    await removeFromFavorites(articleId)
    favoriteArticles.value = favoriteArticles.value.filter(f => f.articleId !== articleId)
  } catch (e) {
    console.error('移除收藏失败:', e)
  }
}

function getFavCoverClass(difficulty) {
  const map = {
    '初中': 'bg-green-100 text-green-700',
    '高中': 'bg-teal-100 text-teal-700',
    '四级': 'bg-blue-100 text-blue-700',
    '六级': 'bg-yellow-100 text-yellow-700',
    '考研': 'bg-red-100 text-red-700',
    '托福': 'bg-purple-100 text-purple-700',
  }
  return map[difficulty] || 'bg-gray-100 text-gray-600'
}

function formatFavTime(timestamp) {
  const now = Date.now()
  const diff = now - timestamp
  const days = Math.floor(diff / 86400000)
  if (days < 1) return '今天'
  if (days < 2) return '昨天'
  if (days < 7) return `${days}天前`
  const d = new Date(timestamp)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

function startVocabTimer() {
  clearVocabTimer()
  vocabTimer = setTimeout(() => {
    taskStore.completeVocabReviewTask()
  }, VOCAB_REVIEW_SECONDS * 1000)
}

function clearVocabTimer() {
  if (vocabTimer) {
    clearTimeout(vocabTimer)
    vocabTimer = null
  }
}

async function loadVocabWords() {
  try {
    const { getAllVocab } = await import('@/utils/vocabDB')
    vocabWords.value = await getAllVocab()
  } catch (e) {
    console.error('加载生词本失败:', e)
  }
}

async function removeVocabWord(wordLower) {
  try {
    const { removeFromVocab } = await import('@/utils/vocabDB')
    await removeFromVocab(wordLower)
    vocabWords.value = vocabWords.value.filter(v => v.wordLower !== wordLower)
  } catch (e) {
    console.error('移出生词失败:', e)
  }
}

function formatAddedTime(timestamp) {
  const now = Date.now()
  const diff = now - timestamp
  const days = Math.floor(diff / 86400000)
  if (days < 1) return '今天'
  if (days < 2) return '昨天'
  if (days < 7) return `${days}天前`
  const d = new Date(timestamp)
  return `${d.getMonth() + 1}/${d.getDate()}`
}

// ========== 错题本 ==========
const wrongQuestions = ref([])
const wrongBookIndex = ref(0)
const showDeleteConfirm = ref(false)
const pendingDeleteId = ref(null)
const showWrongBookIndexOverlay = ref(false)
const showExportMenu = ref(false)
const importFileInput = ref(null)
const importToast = ref({ show: false, success: true, message: '' })

const currentWrongQuestion = computed(() =>
  wrongQuestions.value[wrongBookIndex.value] || {}
)

async function loadWrongQuestions() {
  try {
    const { getAllWrongQuestions } = await import('@/utils/wrongBookDB')
    wrongQuestions.value = await getAllWrongQuestions()
    // 如果当前索引超出范围，重置到第一题
    if (wrongBookIndex.value >= wrongQuestions.value.length) {
      wrongBookIndex.value = 0
    }
  } catch (e) {
    console.error('加载错题本失败:', e)
  }
}

/** 点击删除按钮 → 弹出确认框 */
function promptDeleteWrongQuestion(id) {
  pendingDeleteId.value = id
  showDeleteConfirm.value = true
}

/** 确认删除 → 执行实际删除 */
async function executeDeleteWrongQuestion() {
  const id = pendingDeleteId.value
  showDeleteConfirm.value = false
  pendingDeleteId.value = null
  if (id == null) return
  try {
    const { removeFromWrongBook } = await import('@/utils/wrongBookDB')
    await removeFromWrongBook(id)
    wrongQuestions.value = wrongQuestions.value.filter(q => q.id !== id)
    // 如果删除的是最后一题且不是第一题，索引前移
    if (wrongBookIndex.value >= wrongQuestions.value.length && wrongBookIndex.value > 0) {
      wrongBookIndex.value--
    }
  } catch (e) {
    console.error('移出错题失败:', e)
  }
}

// ========== 错题本导出/导入 ==========

/** 导出为设备迁移文件 (.ewb) */
async function handleExportEWB() {
  showExportMenu.value = false
  const { exportToEWB } = await import('@/utils/wrongBookExport')
  exportToEWB(wrongQuestions.value)
}

/** 导出为可编辑 JSON 文件 */
async function handleExportJSON() {
  showExportMenu.value = false
  const { exportToJSON } = await import('@/utils/wrongBookExport')
  exportToJSON(wrongQuestions.value)
}

/** 触发文件选择器 */
function triggerImportFile() {
  showExportMenu.value = false
  if (importFileInput.value) {
    importFileInput.value.value = ''
    importFileInput.value.click()
  }
}

/** 处理导入文件 */
async function handleImportFile(event) {
  const file = event.target.files?.[0]
  if (!file) return

  const { importFromEWB } = await import('@/utils/wrongBookExport')
  const result = await importFromEWB(file)

  if (result.success) {
    const { addToWrongBook } = await import('@/utils/wrongBookDB')
    let added = 0
    let skipped = 0
    for (const q of result.questions) {
      const res = await addToWrongBook({
        uuid: q.uuid,
        passage: q.passage,
        question: q.question,
        options: q.options,
        userAnswer: q.userAnswer,
        correctAnswer: q.correctAnswer,
        explanation: q.explanation,
      })
      if (res.success) added++
      else if (res.exists) skipped++
    }
    await loadWrongQuestions()
    importToast.value = {
      show: true,
      success: true,
      message: `导入完成：成功 ${added} 道，跳过 ${skipped} 道（已存在）`,
    }
  } else {
    importToast.value = {
      show: true,
      success: false,
      message: result.message || '导入失败',
    }
  }

  // 重置 file input 以允许重复导入同一文件
  event.target.value = ''
  // 自动隐藏提示
  setTimeout(() => { importToast.value = { ...importToast.value, show: false } }, 6000)
}

/** 格式化时间戳为本地日期时间字符串（精确到秒） */
function formatAddedAt(ts) {
  if (!ts) return '未知时间'
  const d = new Date(ts)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  const s = String(d.getSeconds()).padStart(2, '0')
  return `${y}-${m}-${day} ${h}:${min}:${s}`
}

function getWrongOptClass(optionId, item) {
  const isUserAnswer = optionId === item.userAnswer
  const isCorrectAnswer = optionId === item.correctAnswer

  if (isCorrectAnswer && isUserAnswer) return 'bg-green-50 border border-green-300'
  if (isCorrectAnswer && !isUserAnswer) return 'bg-green-50 border border-green-300'
  if (isUserAnswer && !isCorrectAnswer) return 'bg-red-50 border border-red-300'
  return 'bg-gray-50 border border-transparent'
}

function getWrongOptDotClass(optionId, item) {
  const isUserAnswer = optionId === item.userAnswer
  const isCorrectAnswer = optionId === item.correctAnswer

  if (isCorrectAnswer) return 'bg-green-500 text-white'
  if (isUserAnswer && !isCorrectAnswer) return 'bg-red-500 text-white'
  return 'bg-gray-200 text-gray-500'
}

/** 键盘左右方向键切换错题 */
function handleWrongBookKeydown(e) {
  if (e.key === 'ArrowLeft') {
    e.preventDefault()
    if (wrongBookIndex.value > 0) wrongBookIndex.value--
  } else if (e.key === 'ArrowRight') {
    e.preventDefault()
    if (wrongBookIndex.value < wrongQuestions.value.length - 1) wrongBookIndex.value++
  }
}

const studyTarget = ref(getDailyTarget())
// 学习目标行内编辑
const editingTarget = ref(false)
const targetDraft = ref(studyTarget.value)
const targetInputRef = ref(null)

function startEditTarget() {
  targetDraft.value = studyTarget.value
  editingTarget.value = true
  nextTick(() => {
    if (targetInputRef.value) {
      targetInputRef.value.focus()
      targetInputRef.value.select()
    }
  })
}

function saveTarget() {
  editingTarget.value = false
  const val = targetDraft.value
  if (!isNaN(val) && val > 0) {
    studyTarget.value = val
    setDailyTarget(val)
    updateChartTargetLine()
  }
}

const showAboutModal = ref(false)

function openAbout() {
  showAboutModal.value = true
  const randomSrc = gggAudioFiles[Math.floor(Math.random() * gggAudioFiles.length)]
  const audio = new Audio(randomSrc)
  audio.play().catch((e) => console.error('播放失败:', e.message))
}

const showLogoutConfirm = ref(false)
const logoutPopoverPos = reactive({ x: 0, y: 0 })

function handleLogout(e) {
  e.stopPropagation()
  // 计算弹窗位置：水平居中，垂直在点击位置附近
  const popoverWidth = 288 // w-72 = 288px
  const x = Math.max(10, (window.innerWidth - popoverWidth) / 2)
  const y = Math.max(10, e.clientY - 80)
  logoutPopoverPos.x = x
  logoutPopoverPos.y = y
  showLogoutConfirm.value = true
}

function confirmLogout() {
  userStore.logout()
  router.push('/login')
}

function cancelLogout() {
  showLogoutConfirm.value = false
}

// ========== ECharts 趋势图（每日在线时长）==========
const trendChartRef = ref(null)
let trendChart = null
const todayOnlineMinutes = ref(0)

async function initTrendChart() {
  if (!trendChartRef.value) return
  if (!echartsMod) echartsMod = await import('echarts')
  trendChart = echartsMod.init(trendChartRef.value)

  // 从 IndexedDB 读取最近 14 天的在线时长
  const records = await getRecentOnlineTime(14)

  // 格式化日期为短格式 (MM/DD)
  const dates = records.map((r) => {
    const parts = r.date.split('-')
    return `${parseInt(parts[1])}/${parseInt(parts[2])}`
  })
  const data = records.map((r) => r.minutes)
  const todayIdx = data.length - 1

  // 更新今日在线时长显示
  todayOnlineMinutes.value = data[todayIdx]

  const option = {
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E5E7EB',
      borderWidth: 1,
      textStyle: { color: '#1F2937', fontSize: 12 },
      formatter: function (params) {
        const val = params[0].value
        const h = Math.floor(val / 60)
        const m = Math.round(val % 60)
        if (h > 0) {
          return `${params[0].axisValue}<br/><b>${h} 小时 ${m} 分钟</b>`
        }
        return `${params[0].axisValue}<br/><b>${m} 分钟</b>`
      },
    },
    grid: { top: 20, bottom: 30, left: 45, right: 15 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: { color: '#9CA3AF', fontSize: 10 },
    },
    yAxis: {
      type: 'value',
      name: '分钟',
      nameTextStyle: { color: '#9CA3AF', fontSize: 10 },
      splitLine: { lineStyle: { type: 'dashed', color: '#F3F4F6' } },
      axisLabel: { color: '#9CA3AF', fontSize: 10 },
    },
    series: [
      {
        name: '在线时长',
        data: data,
        type: 'bar',
        barWidth: '12px',
        itemStyle: {
          borderRadius: [4, 4, 0, 0],
          color: function (params) {
            if (params.dataIndex === todayIdx) {
              return new echartsMod.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#6366F1' },
                { offset: 1, color: '#2563EB' },
              ])
            }
            return new echartsMod.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#A5B4FC' },
              { offset: 1, color: '#93C5FD' },
            ])
          },
        },
      },
      {
        name: '每日目标',
        type: 'line',
        data: Array(dates.length).fill(studyTarget.value),
        symbol: 'none',
        lineStyle: { color: '#F59E0B', type: 'dashed', width: 1.5 },
        itemStyle: { color: '#F59E0B' },
        silent: true,
      },
    ],
  }
  trendChart.setOption(option)
  window.addEventListener('resize', handleResize)
}

function updateChartTargetLine() {
  if (!trendChart) return
  const target = studyTarget.value
  trendChart.setOption({
    series: [
      {},
      { data: Array(14).fill(target) },
    ],
  })
}

function handleResize() {
  if (trendChart) trendChart.resize()
}

function handleGlobalClick() {
  showLogoutConfirm.value = false
  showExportMenu.value = false
}

async function loadUserStats() {
  try {
    const [s, count] = await Promise.all([
      getStreak(),
      countHistory(),
    ])
    streak.value = s
    totalRead.value = count
  } catch (e) {
    console.error('加载用户统计失败:', e)
  }
}

onMounted(async () => {
  // 如果从读物匹配「查看全部历史」跳转过来，自动打开学习记录
  if (route.query.tab === 'records') {
    activeDrawer.value = 'records'
  }
  await nextTick()
  await userStore.fetchProfile()  // 刷新后端 XP，完成后判断签到状态
  // 检查今日是否已签到
  const lastCheckin = userStore.user?.lastCheckin
  if (lastCheckin) {
    const last = new Date(lastCheckin.replace(' ', 'T'))
    const today = new Date()
    if (last.toDateString() === today.toDateString()) {
      profileCheckinDone.value = true
    }
  }
  loadUserStats()                 // 刷新本地统计
  if (activeDrawer.value === 'trend') {
    initTrendChart()
  }
  document.addEventListener('click', handleGlobalClick)
})

// 离开趋势抽屉时销毁图表实例，保证下次进入时重新绑定新 DOM
function disposeChart() {
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
    echartsMod = null  // 释放 ECharts 模块引用
  }
}

// 监听抽屉切换
watch(activeDrawer, async (val, oldVal) => {
  // 离开趋势抽屉 → 销毁图表
  if (oldVal === 'trend') {
    disposeChart()
  }
  // 进入趋势抽屉 → 重新初始化图表
  if (val === 'trend') {
    await nextTick()
    try {
      const mins = await getTodayMinutes()
      todayOnlineMinutes.value = mins
    } catch (_) { /* ignore */ }
    initTrendChart()
  }
  // 进入学习记录抽屉 → 刷新浏览历史
  if (val === 'records') {
    loadHistoryRecords()
  }
  // 进入生词本抽屉 → 刷新生词列表 + 启动复习计时
  if (val === 'vocab') {
    loadVocabWords()
    startVocabTimer()
  }
  // 离开生词本 → 停止计时
  if (oldVal === 'vocab') {
    clearVocabTimer()
  }
  // 进入错题本 → 加载错题 + 绑定键盘事件
  if (val === 'wrongbook') {
    loadWrongQuestions()
    window.addEventListener('keydown', handleWrongBookKeydown)
  }
  // 离开错题本 → 解绑键盘事件
  if (oldVal === 'wrongbook') {
    window.removeEventListener('keydown', handleWrongBookKeydown)
    wrongBookIndex.value = 0
  }
  // 进入收藏夹 → 刷新收藏列表
  if (val === 'favorites') {
    loadFavorites()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('keydown', handleWrongBookKeydown)
  document.removeEventListener('click', handleGlobalClick)
  clearVocabTimer()
  clearTimeout(toastTimer)
  disposeChart()
})
</script>

<style scoped>
/* ========== 抽屉便签样式 ========== */
.drawer-tabs {
  position: relative;
  z-index: 10;
}

.drawer-tab-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: #F3F4F6;
  color: #6B7280;
  border: none;
  border-radius: 12px 12px 0 0;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
  position: relative;
}

.drawer-tab-label:hover {
  background: #E5E7EB;
  color: #374151;
}

.drawer-tab-label.active {
  background: white;
  color: #2563EB;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);
}

.drawer-tab-label.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #2563EB;
  border-radius: 3px 3px 0 0;
}

.drawer-content-wrapper {
  position: relative;
  min-height: 100px;
}

.drawer-card {
  border-radius: 0 16px 16px 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

/* ========== 抽屉切换动画 ========== */
.drawer-enter-active {
  animation: drawer-slide-in 0.5s cubic-bezier(0.22, 0.61, 0.36, 1);
}

.drawer-leave-active {
  animation: drawer-slide-out 0.35s cubic-bezier(0.55, 0.06, 0.68, 0.19);
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
}

@keyframes drawer-slide-in {
  0% {
    opacity: 0;
    transform: translateY(-20px);
  }
  60% {
    opacity: 0.85;
    transform: translateY(-4px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes drawer-slide-out {
  0% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateY(-12px) scale(0.98);
  }
}

/* ========== Toast 动画 ========== */
.toast-enter-active { animation: toast-in 0.3s ease-out; }
.toast-leave-active { animation: toast-out 0.3s ease-in; }
@keyframes toast-in {
  0% { opacity: 0; transform: translateY(-20px) translateX(-50%); }
  100% { opacity: 1; transform: translateY(0) translateX(-50%); }
}
@keyframes toast-out {
  0% { opacity: 1; transform: translateY(0) translateX(-50%); }
  100% { opacity: 0; transform: translateY(-20px) translateX(-50%); }
}
</style>
